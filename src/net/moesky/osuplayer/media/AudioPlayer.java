/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.moesky.osuplayer.media;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import com.un4seen.bass.*;
import net.moesky.osuplayer.beatmap.Beatmap;

import java.io.File;
import java.io.IOException;

/**
 * 重写了系统中的MediaPlayer
 *
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class AudioPlayer {

    private final static String TAG = "AudioPlayer";
    private final static boolean D = true;

    /* Handler执行周期，数值越小越接近原始osu听感，同时更消耗系统资源 */
    private final static int INTERVAL = 1;

    /* 取消与HitSoundEffect同步 */
    private final static int EVENT_CANCEL_SYNC = 1;

    /* 播放完成事件 */
    private final static int EVENT_COMPLETE_PLAY = 2;

    private static AudioPlayer sInstance = null;

    private String mFilePath;
    private Beatmap mBeatmap;
    private MusicFile mMusicFile;
    private String mNextFilePath;
    private HitEffectPlayer mHitEffect;  // 打击音效管理
    private Handler mHandler;  // FIXME: 使用弱引用便于检查空指针

    private SurfaceHolder mSurfaceHolder;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;

    public AudioPlayer(final Looper looper) {
        mHandler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (D) Log.d(TAG, "Msg received: " + msg.what);
                switch (msg.what) {
                    case EVENT_CANCEL_SYNC:
                        this.removeCallbacks(refresh);
                        break;
                    case EVENT_COMPLETE_PLAY:
                        if (mOnCompletionListener != null) {
                            mOnCompletionListener.onCompletion(getInstance(null));
                        }
                        break;
                }
            }
        };
    }

    /**
     * @param looper The {@link Looper} to use
     * @return A new instance of this class
     */
    public static final synchronized AudioPlayer getInstance(final Looper looper) {
        if (sInstance == null) {
            sInstance = new AudioPlayer(looper);
        }
        return sInstance;
    }

    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            if (mMusicFile == null) {
                return;
            }
            if (mMusicFile.isChannelEndPosition()) {
                mHandler.sendEmptyMessage(EVENT_COMPLETE_PLAY);
                return;
            }
            mOnUpdateListener.onUpdate();
            mHandler.postDelayed(refresh, INTERVAL);
        }
    };

    public void CreateDevice() {
        BASS.BASS_Init(-1, 44100, 0);
    }

    public void start() {
        stayAwake(true);
        if (mMusicFile != null) {
            mMusicFile.play();
            mHandler.post(refresh);
        }
    }

    public void switchToNext() {
        try {
            if (D) Log.d(TAG, "Track switched: " + mNextFilePath);
            setDataSource(mNextFilePath);
        } catch (IOException ignored) {
        }
    }

    public boolean hasNext() {
        return mNextFilePath != null;
    }

    public void reset() {
        stayAwake(false);
        updateSurfaceScreenOn();
        mHandler.sendEmptyMessage(EVENT_CANCEL_SYNC);
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnErrorListener = null;
        mOnUpdateListener = null;
        if (mMusicFile != null) {
            mMusicFile.release();
            mMusicFile = null;
        }
        if (mHitEffect != null) {
            mHitEffect.release();
            mHitEffect = null;
        }
        if (mBeatmap != null) {
            mBeatmap.release();
            mBeatmap = null;
        }
    }

    public void stop() {
        stayAwake(false);
        mMusicFile.stop();
        mHandler.sendEmptyMessage(EVENT_CANCEL_SYNC);
    }

    public void pause() {
        stayAwake(false);
        mMusicFile.pause();
        mHandler.sendEmptyMessage(EVENT_CANCEL_SYNC);
    }

    public void release() {
        stayAwake(false);
        updateSurfaceScreenOn();
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnErrorListener = null;
        mOnUpdateListener = null;
        // run destructor
        BASS.BASS_Free();
    }

    public void seekTo(int position) {
        mMusicFile.seek(position);
        mHitEffect.reset();
    }

    public String getStoryBoard() {
        return mBeatmap.getStoryBoard();
    }

    /**
     * 设定下一个文件的路径
     *
     * @param nextFilePath 下一个文件的路径
     */
    public void setNextDataSource(String nextFilePath) {
        if (nextFilePath == null) {
            mNextFilePath = null;
            return;
        }
        final File file = new File(nextFilePath);
        if (file.exists()) {
            mNextFilePath = nextFilePath;
        }
    }

    /**
     * 返回播放经过时间
     */
    public double getDuration() {
        if (mMusicFile != null) {
            long length = mMusicFile.getChannelLength();
            return mMusicFile.Bytes2Second(length);
        }
        return 0;
    }

    /**
     * 返回媒体文件总时长
     */
    public double getCurrentPosition() {
        if (mMusicFile != null) {
            long position = mMusicFile.getChannelPosition();
            return mMusicFile.Bytes2Second(position);
        }
        return 0;
    }

    /**
     * 设定播放器音量
     *
     * @param volume 浮点数表示的音量，最大为1.0
     */
    public void setVolume(float volume) {
        if (mMusicFile != null) {
            mMusicFile.setVolume(volume);
        }
    }

    /**
     * 将osu文件的路径传递给播放器
     *
     * @param path osu文件的路径
     * @throws IOException
     */
    public void setDataSource(String path) throws IOException {
        final File file = new File(path);
        if (file.exists()) {
            mFilePath = path;
            String dir = file.getParent() + "/";
            // 开始初始哈地图，背景音乐，音效等等
            mBeatmap = new Beatmap(path);
            mMusicFile = new MusicFile(dir + mBeatmap.getAudioFileName());
            mHitEffect = new HitEffectPlayer(this, mBeatmap);  // 将音效与背景音乐绑定
        } else {
            throw new IOException("Can't open file, a wrong path was specified.");
        }
    }

    public interface onUpdateListener {
        void onUpdate();
    }

    public void setOnUpdateListener(onUpdateListener listener) {
        mOnUpdateListener = listener;
    }

    private onUpdateListener mOnUpdateListener;

    /*
    * 以下方法与接口定义拷贝自Android源代码
    */
    /**
     * <p>This function has the MediaPlayer access the low-level power manager
     * service to control the device's power usage while playing is occurring.
     * The parameter is a combination of {@link android.os.PowerManager} wake flags.
     * Use of this method requires {@link android.Manifest.permission#WAKE_LOCK}
     * permission.
     * By default, no attempt is made to keep the device awake during playback.
     *
     * @param context the Context to use
     * @param mode    the power/wake mode to set
     * @see android.os.PowerManager
     */
    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode|PowerManager.ON_AFTER_RELEASE, AudioPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }

    /**
     * Control whether we should use the attached SurfaceHolder to keep the
     * screen on while video playback is occurring.  This is the preferred
     * method over {@link #setWakeMode} where possible, since it doesn't
     * require that the application have permission for low-level wake lock
     * access.
     *
     * @param screenOn Supply true to keep the screen on, false to allow it
     * to turn off.
     */
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }
    }

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    public interface OnPreparedListener
    {
        void onPrepared(AudioPlayer ap);
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener listener)
    {
        mOnPreparedListener = listener;
    }

    private OnPreparedListener mOnPreparedListener;

    /* Do not change these values without updating their counterparts
     * in include/media/mediaplayer.h!
     */
    /** Unspecified media player error.
     * @see android.media.MediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_UNKNOWN = 1;

    /** Media server died. In this case, the application must release the
     * MediaPlayer object and instantiate a new one.
     * @see android.media.MediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_SERVER_DIED = 100;

    /** File or network related operation errors. */
    public static final int MEDIA_ERROR_IO = -1004;

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    public interface OnErrorListener
    {
        boolean onError(AudioPlayer mp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an error has happened
     * during an asynchronous operation.
     *
     * @param listener the callback that will be run
     */
    public void setOnErrorListener(OnErrorListener listener)
    {
        mOnErrorListener = listener;
    }

    private OnErrorListener mOnErrorListener;

    /**
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    public interface OnCompletionListener
    {
        void onCompletion(AudioPlayer mp);
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener)
    {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;
}

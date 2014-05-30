package com.dgsrz.osuTaste.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import com.dgsrz.osuTaste.R;
import com.dgsrz.osuTaste.activities.HomeActivity;
import com.dgsrz.osuTaste.beatmap.BeatmapParser;
import com.un4seen.bass.BASS;

import java.io.File;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class BassService extends Service {

    // Notification
    private Notification notification;

    // Pending Intent to be called if a user click on the notification
    private PendingIntent pendIntent;

    private CommandReceiver commandReceiver;

    private final static String MUSIC_SERVICE = "com.dgsrz.osuTaste.services.BassService";

    // Bass Service Binder Class
    public class BassServiceBinder extends Binder {
        public BassService getService() {
            return BassService.this;
        }
    }

    // Channel Handle
    private int chan;

    // Properties: BassInterface
    private String fileToPlay = "";
    private double duration = 0.0;
    private String _info = "";
    private double progress = 0.0;
    private SampleProvider softSamples[];
    private SampleProvider normalSamples[];

    private BeatmapParser beatmapParser;
    private int pos = 0;

    private Thread timer;

    // Bass Service Binder
    private final IBinder mBinder = new BassServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Activity with implemented BassInterface
    private BassInterface activity;

    // Set Activity
    public void setActivity(BassInterface activity) {
        this.activity = activity;
        if (activity != null) {
            activity.onFileLoaded(fileToPlay, duration, _info);
            activity.onProgressChanged(progress);
        }
    }

    private void initSample() {
        softSamples = new SampleProvider[64];
        softSamples[0] = new SampleProvider(getApplicationContext(), "soft-hitnormal.wav");
        softSamples[1] = new SampleProvider(getApplicationContext(), "soft-hitwhistle.wav");
        softSamples[2] = new SampleProvider(getApplicationContext(), "soft-hitfinish.wav");
        softSamples[3] = new SampleProvider(getApplicationContext(), "soft-hitclap.wav");
        normalSamples = new SampleProvider[64];
        normalSamples[0] = new SampleProvider(getApplicationContext(), "normal-hitnormal.wav");
        normalSamples[1] = new SampleProvider(getApplicationContext(), "normal-hitwhistle.wav");
        normalSamples[2] = new SampleProvider(getApplicationContext(), "normal-hitfinish.wav");
        normalSamples[3] = new SampleProvider(getApplicationContext(), "normal-hitclap.wav");
    }

    private void onUpdate() {
        long length = BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE);
        long position = BASS.BASS_ChannelGetPosition(chan, BASS.BASS_POS_BYTE);
        double total = BASS.BASS_ChannelBytes2Seconds(chan, length);
        double elapsed = BASS.BASS_ChannelBytes2Seconds(chan, position);

        while ((pos < beatmapParser.getHitObjects().size())
                && (elapsed * 1000 - 10 >= beatmapParser.getHitObjects().get(pos).getTime()
                + beatmapParser.getAudioOffset() / 1000.0)) {
            if (pos + 1 < beatmapParser.getHitObjects().size()
                    && (elapsed * 1000 - 10 >= beatmapParser.getHitObjects().get(pos + 1).getTime()
                    + beatmapParser.getAudioOffset() / 1000.0)) {
                ++pos;
                continue;
            }

            for (int i = 0; i < 4; i++) {
                if ((beatmapParser.getHitObjects().get(pos).getSound() & (1 << i)) != 0) {
                    int id = beatmapParser.getHitObjects().get(pos).getCustomSound() * 4 + i;
                    if (beatmapParser.getHitObjects().get(pos).getSoundType() == 1) {
                        if (normalSamples[id] != null) {
                            normalSamples[id].play();
                            normalSamples[id].setVolume(beatmapParser.getHitObjects().get(pos).getVolume());
                        } else {
                            normalSamples[i].play();
                            normalSamples[i].setVolume(beatmapParser.getHitObjects().get(pos).getVolume());
                        }
                    } else if (beatmapParser.getHitObjects().get(pos).getSound() == 2) {
                        if (softSamples[id] != null) {
                            softSamples[id].play();
                            softSamples[id].setVolume(beatmapParser.getHitObjects().get(pos).getVolume());
                        } else {
                            softSamples[i].play();
                            softSamples[i].setVolume(beatmapParser.getHitObjects().get(pos).getVolume());
                        }
                    } else {
                        if (beatmapParser.getSoundType().equals("normal")) {
                            if (normalSamples[id] != null) {
                                normalSamples[id].play();
                                normalSamples[id].setVolume(beatmapParser.getHitObjects().get(pos).getVolume());
                            } else {
                                normalSamples[i].play();
                                normalSamples[i].setVolume(beatmapParser.getHitObjects().get(pos).getVolume());
                            }
                        } else {
                            if (softSamples[id] != null) {
                                softSamples[id].play();
                                softSamples[id].setVolume(beatmapParser.getHitObjects().get(pos).getVolume());
                            } else {
                                softSamples[i].play();
                                softSamples[i].setVolume(beatmapParser.getHitObjects().get(pos).getVolume());
                            }
                        }
                    }
                }
            }
            ++pos;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize default output device
        if (!BASS.BASS_Init(-1, 44100, 0)) {
            return;
        }
        initSample();

        // Timer
        timer = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if(BASS.BASS_ChannelIsActive(chan) == BASS.BASS_ACTIVE_PLAYING) {
                        onUpdate();
//                        if(activity != null) {
//                            progress = BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetPosition(chan, BASS.BASS_POS_BYTE));
//                            activity.onProgressChanged(progress);
//                        }
                    } else {
                        Thread.yield();
                    }
                }
            }
        });
        timer.start();

        commandReceiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_SERVICE);
        registerReceiver(commandReceiver, filter);

        // Pending Intend
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Prepare Notification
        notification = new Notification(R.drawable.ic_launcher,
                getApplicationContext().getString(R.string.app_name), System.currentTimeMillis());
        // notification.flags |= Notification.FLAG_NO_CLEAR;
    }

    @Override
    public void onDestroy() {

        // "free" the output device and all plugins
        BASS.BASS_Free();

        // Stop foreground
        stopForeground(true);

        unregisterReceiver(commandReceiver);

        super.onDestroy();
    }

    public class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey("play")) {
                Stop();
                if (beatmapParser != null) {
                    beatmapParser.dispose();
                    beatmapParser = null;
                }
                String command = intent.getStringExtra("play");
                beatmapParser = new BeatmapParser(command);
                String directory = (new File(command)).getParent();
                Play(directory + "/" + beatmapParser.audioFileName);
            } else {
                Stop();
            }
        }
    }

    public void Stop() {
        this.fileToPlay = "";
        BASS.BASS_ChannelStop(chan);
        BASS.BASS_StreamFree(chan);
        stopForeground(true);
    }

    // Play file
    public void Play(String fileToPlay) {

        // Play File
        // BASS.BASS_StreamFree(chan);
        if ((chan=BASS.BASS_StreamCreateFile(fileToPlay, 0, 0, 0))==0) {

            // Update Properties
            this.fileToPlay = "press here to open a file";
            this.duration = 0.0;
            this._info = "";
            this.progress = 0.0;
            this.pos = 0;

            // Notify activity
//            if(activity != null) {
//                activity.onFileLoaded(this.fileToPlay, this.duration, this._info);
//                activity.onProgressChanged(progress);
//            }

            // Stop Foreground
            stopForeground(true);

            return;
        }

        // Play File
        long bytes = BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE);
        int time=(int)BASS.BASS_ChannelBytes2Seconds(chan, bytes);
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(chan, info);
        BASS.BASS_ChannelPlay(chan, true);

        // Update Properties
        this.fileToPlay = fileToPlay;
        this.duration = BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE));
        this._info = String.format("channel type = %x\nlength = %d (%d:%02d)", info.ctype, bytes, time/60,time%60);
        this.progress = 0.0;
        this.pos = 0;

        // Notify Activity
//        if(activity != null) {
//            activity.onFileLoaded(this.fileToPlay, this.duration, this._info);
//            activity.onProgressChanged(progress);
//        }

        // Start foreground
        notification.setLatestEventInfo(this, MUSIC_SERVICE, fileToPlay, pendIntent);
        startForeground(1, notification);
    }

    // Seek to position
    public void SeekTo(int progress) {
        BASS.BASS_ChannelSetPosition(chan, BASS.BASS_ChannelSeconds2Bytes(chan, progress), BASS.BASS_POS_BYTE);
    }

}

package com.dgsrz.osuTaste.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import com.dgsrz.osuTaste.R;
import com.dgsrz.osuTaste.adapters.PagerAdapter;
import com.dgsrz.osuTaste.services.BassService;
import com.viewpagerindicator.TitlePageIndicator;

public class HomeActivity extends BaseActivity {

    private BassService mBoundService = null;

    // Bass Service Connection
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((BassService.BassServiceBinder)service).getService();
            onBassServiceConnected();
        }

        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start Service
        startService(new Intent(HomeActivity.this, BassService.class));

        // Bind Service
        bindService(new Intent(this, BassService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        // Unbind Service
        unbindService(mConnection);
        super.onDestroy();
    }

    public void onBassServiceConnected() {
        setContentView(R.layout.main);

        PagerAdapter mAdapter = new PagerAdapter(this);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }

}

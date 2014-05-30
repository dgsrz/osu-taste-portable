package com.dgsrz.osuTaste.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import com.dgsrz.osuTaste.R;
import com.dgsrz.osuTaste.fragments.AlbumFragment;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class PagerAdapter extends FragmentPagerAdapter {

    FragmentActivity mFragmentActivity;

    protected static final int[] CONTENT = new int[] {
            R.string.page_collections,
            R.string.page_albums,
            R.string.page_artists,
            R.string.page_directories,
            R.string.page_songs
    };

    private int mCount = CONTENT.length;

    public PagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity.getSupportFragmentManager());
        mFragmentActivity = fragmentActivity;
    }

    @Override
    public Fragment getItem(int position) {
        return AlbumFragment.getInstance();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentActivity.getResources().getString(PagerAdapter.CONTENT[position % CONTENT.length]);
    }

    @Override
    public int getCount() {
        return mCount;
    }
}

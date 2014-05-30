package com.dgsrz.osuTaste.activities;

import android.app.ActionBar;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import com.dgsrz.osuTaste.R;
import com.dgsrz.osuTaste.services.BassService;
import com.viewpagerindicator.PageIndicator;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public abstract class BaseActivity extends FragmentActivity {

    ViewPager mPager;
    PageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        setUpSearchView(menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpSearchView(Menu menu){
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //showInfo("searchView : " + searchView);
        if(searchView == null){
            return;
        }
        searchView.setIconifiedByDefault(true);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //showInfo("searchManager : " + searchManager);
        ComponentName cn = new ComponentName(this, SearchActivity.class);
        //showInfo("ComponentName : " + cn);
        SearchableInfo info = searchManager.getSearchableInfo(cn);
        if(info == null){
        }
        //showInfo(info.toString());
        searchView.setSearchableInfo(info);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                return true;
            case R.id.action_settings:
                final Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_exit:
                stopService(new Intent(this, BassService.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
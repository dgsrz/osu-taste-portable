package com.dgsrz.osuTaste.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import com.dgsrz.osuTaste.R;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class SettingsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
package com.dgsrz.osuTaste.utils;

import android.app.Activity;
import android.content.Intent;
import com.dgsrz.osuTaste.activities.*;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class NavUtils {

    /**
     * Opens to {@link SettingsActivity}.
     *
     * @param activity The {@link Activity} to use.
     */
    public static void openSettings(final Activity activity) {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }

}

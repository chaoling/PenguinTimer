package com.lucassociate.penguintimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

/**
 * Created by chaol on 12/18/16.
 */

public class Utility {
    public static boolean isSoundEffectEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_enable_tick_sound_effect_key),
                false);
    }

    public static String getRingtoneResourceName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_ringtone_key),
                context.getString(R.string.pref_ringtone_cuckoo));
    }

    public static int getRingtoneResourceIdByName(Context context, String name) {
        Resources res = context.getResources();
        int soundId = R.raw.cuckoo;
        try {
            soundId = res.getIdentifier(name, "raw", context.getPackageName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //Log.v("Utility","sound Id for "+name+" is "+soundId);
        return soundId == 0 ? R.raw.cuckoo : soundId;
    }
}

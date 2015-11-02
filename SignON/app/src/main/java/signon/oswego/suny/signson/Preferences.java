package signon.oswego.suny.signson;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    private static final String CELLPHONE_ADDRESS = "PREF_MAC_ADDRESS";
    private static final String GLASS_ADDRESS = "PREF_GLASS_MAC_ADDRESS";

    private SharedPreferences mPrefs;

    public Preferences(Context context) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getMyoAddress() {

        return mPrefs.getString(CELLPHONE_ADDRESS, "");
    }

    public void setMyoAddress(String address) {

        mPrefs.edit().putString(CELLPHONE_ADDRESS, address).apply();
    }

    public String getGlassAddress() {

        return mPrefs.getString(GLASS_ADDRESS, "");
    }

    public void setGlassAddress(String address) {

        mPrefs.edit().putString(GLASS_ADDRESS, address).apply();
    }
}

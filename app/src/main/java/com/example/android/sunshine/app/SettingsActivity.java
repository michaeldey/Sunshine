package com.example.android.sunshine.app;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        //for all preferences, attach OnPreferenceChangeListener so the
        //UI summary can be upded when the preference changes
        bindPreferenceSummaryToValue(findPreference("location"));

        //display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    /*
    Attaches a listener so the summary is always updated with the
    preferences value. Also fires the listener once, to initialize the
    summary so it shows up before the value is changed
     */
    private void bindPreferenceSummaryToValue(Preference preference){
        //set the listener to watch for value changes
        preference.setOnPreferenceChangeListener(this);

        //trigger the listener immediately with the preferences'
        //current value
        onPreferenceChange(preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
    }//end of bindPreferenceSummary

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString(); //sets the key of the key value pair

        System.out.println(preference.getKey()+" : " + stringValue);

        if (preference instanceof ListPreference){
            //for list preferences, look up the correct display value
            ListPreference listPreference = (ListPreference) preference;

            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >=0){
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }else {
                // for other preferences, set the summary to the values
                // simple string representation
                preference.setSummary(stringValue);
            }
        }

        return true;
    }
}

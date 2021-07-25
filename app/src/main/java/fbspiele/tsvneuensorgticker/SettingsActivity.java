package fbspiele.tsvneuensorgticker;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_overview, target);
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public static class SpielInfoFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.spielinfo);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_gegner_erste_mannschaft_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_heimspiel_erste_mannschaft_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_gegner_zweite_mannschaft_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_heimspiel_zweite_mannschaft_key)));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class AppSettingsFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_settings);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_show_notification)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_auto_send_to_whatsapp)));


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    public static class spielerErsteMannschaftFragment extends PreferenceFragment{

        public String arrayToString(String[] array){

            MultiSelectListPreference preference = (MultiSelectListPreference) getPreferenceManager().findPreference(getString(R.string.key_spieler_erste_mannschaft));
            assert preference!=null;
            Set<String> valuesSet = preference.getValues();
            String[] ersteMannschaftArray = valuesSet.toArray(new String[valuesSet.size()]);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i<ersteMannschaftArray.length; i++){
                stringBuilder.append(ersteMannschaftArray[i]);
                if(i<ersteMannschaftArray.length-1){
                    stringBuilder.append("\n");
                }
            }
            return stringBuilder.toString();
        }
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);

            MultiSelectListPreference preference = (MultiSelectListPreference) getPreferenceManager().findPreference(getString(R.string.key_spieler_erste_mannschaft));
            assert preference!=null;
            Set<String> valuesSet = preference.getValues();
            String[] ersteMannschaftArray = valuesSet.toArray(new String[valuesSet.size()]);
            preference.setSummary(arrayToString(ersteMannschaftArray));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_spieler_erste_mannschaft)));


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class spielerZweiteMannschaftFragment extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_spieler_zweite_mannschaft)));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SpielInfoFragment.class.getName().equals(fragmentName)
                || AppSettingsFragment.class.getName().equals(fragmentName)
                || spielerErsteMannschaftFragment.class.getName().equals(fragmentName)
                || spielerZweiteMannschaftFragment.class.getName().equals(fragmentName);
    }


    public String getGesamtkaderString(){
        StringBuilder stringBuilder = new StringBuilder();
        String[] gesamtkaderStringArray = getResources().getStringArray(R.array.gesamtkader);
        for (int i = 0; i<gesamtkaderStringArray.length; i++){
            stringBuilder.append(gesamtkaderStringArray[i]);
            if(i<gesamtkaderStringArray.length-1){
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            Log.v("prefChanged","preference "+preference.toString()+"\nvalue"+value.toString());



            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                //preference.setSummary(stringValue);
                return true;
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        /*sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));*/
    }



}

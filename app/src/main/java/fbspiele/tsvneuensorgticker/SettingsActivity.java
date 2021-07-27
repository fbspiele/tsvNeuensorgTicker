package fbspiele.tsvneuensorgticker;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.DialogInterface;
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
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.text.PrecomputedText;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
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

            findPreference(getText(R.string.key_gesamtkader)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GesamtKaderFragment gesamtKaderFragment = new GesamtKaderFragment();

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager
                            .beginTransaction();
                    fragmentTransaction.replace(spielInfoFragmentContainer.getId(), gesamtKaderFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    return false;
                }
            });

            findPreference(getText(R.string.key_spieler_erste_mannschaft)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    aktiveSpielerAuswahlFragment aktiveSpielerAuswahlFragment = new aktiveSpielerAuswahlFragment();
                    aktiveSpielerAuswahlFragment.mannschaft = 1;
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager
                            .beginTransaction();
                    fragmentTransaction.replace(spielInfoFragmentContainer.getId(), aktiveSpielerAuswahlFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    return false;
                }
            });

            findPreference(getText(R.string.key_spieler_zweite_mannschaft)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    aktiveSpielerAuswahlFragment aktiveSpielerAuswahlFragment = new aktiveSpielerAuswahlFragment();
                    aktiveSpielerAuswahlFragment.mannschaft = 2;

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager
                            .beginTransaction();
                    fragmentTransaction.replace(spielInfoFragmentContainer.getId(), aktiveSpielerAuswahlFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    return false;
                }
            });
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_gegner_erste_mannschaft_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_heimspiel_erste_mannschaft_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_gegner_zweite_mannschaft_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_heimspiel_zweite_mannschaft_key)));

        }

        ViewGroup spielInfoFragmentContainer;


        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            spielInfoFragmentContainer = container;
            return v;
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


    public static class GesamtKaderFragment extends PreferenceFragment{
        PreferenceScreen preferenceScreen;
        List<Spieler> gesamtKaderList;
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.empty_xml);    //sonst returned getPreferenceScreen() null
        }

        @Override
        public void onResume() {
            super.onResume();
            final Context context = getContext();
            preferenceScreen = getPreferenceScreen();
            preferenceScreen.removeAll();




            final Preference spielerHinzufugenPref = new EditTextPreference(context);
            spielerHinzufugenPref.setTitle(getString(R.string.pref_spieler_hinzufugen_title));

            spielerHinzufugenPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Spieler neuerSpieler = new Spieler();
                    neuerSpieler.name = o.toString();
                    gesamtKaderList.add(neuerSpieler);
                    tsvNeuensorgTicker.saveMyGesamtKaderList(context, gesamtKaderList);
                    onResume();
                    return false;
                }
            });

            preferenceScreen.addPreference(spielerHinzufugenPref);

            PreferenceCategory preferenceCategoryGesamtkader = new PreferenceCategory(context);
            preferenceCategoryGesamtkader.setTitle(R.string.preferenceCategory_title_gesamtkader);
            preferenceScreen.addPreference(preferenceCategoryGesamtkader);
            gesamtKaderList = tsvNeuensorgTicker.loadMyGesamtKaderList(context);
            for (final Spieler spieler : gesamtKaderList) {
                Preference spielerPref = new Preference(context);
                spielerPref.setTitle(spieler.name);
                spielerPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("spieler löschen?");
                        builder.setMessage("willst du " + spieler.name + " aus der Gesamtkaderliste löschen?");
                        builder.setPositiveButton("jo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gesamtKaderList.remove(spieler);
                                tsvNeuensorgTicker.saveMyGesamtKaderList(context,gesamtKaderList);
                                onResume();
                            }
                        });
                        builder.setNeutralButton("doch ned",null);
                        builder.show();
                        return true;
                    }
                });
                preferenceScreen.addPreference(spielerPref);
            }
        }
    }


    public static class aktiveSpielerAuswahlFragment extends PreferenceFragment{
        PreferenceScreen preferenceScreen;
        int mannschaft;
        List<Spieler> gesamtKaderList;
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.empty_xml);    //sonst returned getPreferenceScreen() null
        }

        @Override
        public void onResume() {
            super.onResume();
            final Context context = getContext();
            preferenceScreen = getPreferenceScreen();
            preferenceScreen.removeAll();

            if(!(mannschaft==1||mannschaft==2)){
                Log.v("aktive mannschaft auswählen", "mannschaft weder 1 noch 2");
                return;
            }


            gesamtKaderList = tsvNeuensorgTicker.loadMyGesamtKaderList(context);
            List<Spieler> aktiveSpielerList = new ArrayList<>();
            List<Spieler> inaktiveSpielerList = new ArrayList<>();
            List<Spieler> nieAktiveSpielerList = new ArrayList<>();
            for(Spieler spieler:gesamtKaderList){
                if(mannschaft==1){
                    if(spieler.letztesMalInErsterMannschaft==null){
                        nieAktiveSpielerList.add(spieler);
                    }
                    else if(spieler.letztesMalInErsterMannschaft<=0){
                        aktiveSpielerList.add(spieler);
                    }
                    else {
                        inaktiveSpielerList.add(spieler);
                    }
                }
                if(mannschaft==2){
                    if(spieler.letztesMalInZweiterMannschaft==null){
                        nieAktiveSpielerList.add(spieler);
                    }
                    else if(spieler.letztesMalInZweiterMannschaft<=0){
                        aktiveSpielerList.add(spieler);
                    }
                    else {
                        inaktiveSpielerList.add(spieler);
                    }
                }
            }

            aktiveSpielerList.sort(new Comparator<Spieler>() {
                @Override
                public int compare(Spieler spieler1, Spieler spieler2) {
                    return spieler1.name.compareTo(spieler2.name);
                }
            });

            inaktiveSpielerList.sort(new Comparator<Spieler>() {
                @Override
                public int compare(Spieler spieler1, Spieler spieler2) {
                    if(mannschaft==1){
                        return -spieler1.letztesMalInErsterMannschaft.compareTo(spieler2.letztesMalInErsterMannschaft);
                    }
                    else if (mannschaft==2){
                        return -spieler1.letztesMalInZweiterMannschaft.compareTo(spieler2.letztesMalInZweiterMannschaft);
                    }
                    else return 1;
                }
            });


            PreferenceCategory preferenceCategoryActive = new PreferenceCategory(context);
            preferenceCategoryActive.setTitle(R.string.preferenceCategory_title_activeSpieler);
            preferenceScreen.addPreference(preferenceCategoryActive);

            for(final Spieler spieler:aktiveSpielerList){
                Preference spielerPref = new Preference(context);
                spielerPref.setTitle(spieler.name);
                spielerPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Calendar calendar = Calendar.getInstance();
                        if(mannschaft == 1){
                            spieler.letztesMalInErsterMannschaft = calendar.getTimeInMillis();
                            tsvNeuensorgTicker.saveMyGesamtKaderList(context,gesamtKaderList);
                            onResume();
                        }
                        else if(mannschaft == 2){
                            spieler.letztesMalInZweiterMannschaft = calendar.getTimeInMillis();
                            tsvNeuensorgTicker.saveMyGesamtKaderList(context,gesamtKaderList);
                            onResume();
                        }
                        return false;
                    }
                });
                preferenceScreen.addPreference(spielerPref);
            }



            PreferenceCategory preferenceCategoryInactive = new PreferenceCategory(context);
            preferenceCategoryInactive.setTitle(R.string.preferenceCategory_title_inactiveSpieler);
            preferenceScreen.addPreference(preferenceCategoryInactive);

            for(final Spieler spieler:inaktiveSpielerList){
                Preference spielerPref = new Preference(context);
                spielerPref.setTitle(spieler.name);
                spielerPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if(mannschaft == 1){
                            spieler.letztesMalInErsterMannschaft = (long) -1;
                            tsvNeuensorgTicker.saveMyGesamtKaderList(context,gesamtKaderList);
                            onResume();
                        }
                        else if(mannschaft == 2){
                            spieler.letztesMalInZweiterMannschaft = (long) -1;
                            tsvNeuensorgTicker.saveMyGesamtKaderList(context,gesamtKaderList);
                            onResume();
                        }
                        return false;
                    }
                });
                preferenceScreen.addPreference(spielerPref);
            }

            PreferenceCategory preferenceCategoryNeverActive = new PreferenceCategory(context);
            preferenceCategoryNeverActive.setTitle(R.string.preferenceCategory_title_neverActiveSpieler);
            preferenceScreen.addPreference(preferenceCategoryNeverActive);

            for(final Spieler spieler:nieAktiveSpielerList){
                Preference spielerPref = new Preference(context);
                spielerPref.setTitle(spieler.name);
                spielerPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if(mannschaft == 1){
                            spieler.letztesMalInErsterMannschaft = (long) -1;
                            tsvNeuensorgTicker.saveMyGesamtKaderList(context,gesamtKaderList);
                            onResume();
                        }
                        else if(mannschaft == 2){
                            spieler.letztesMalInZweiterMannschaft = (long) -1;
                            tsvNeuensorgTicker.saveMyGesamtKaderList(context,gesamtKaderList);
                            onResume();
                        }
                        return false;
                    }
                });
                preferenceScreen.addPreference(spielerPref);
            }

            Preference spielerPreference = new Preference(context);
            spielerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return false;
                }
            });
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



    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SpielInfoFragment.class.getName().equals(fragmentName)
                || AppSettingsFragment.class.getName().equals(fragmentName);


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

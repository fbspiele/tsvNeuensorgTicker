package fbspiele.tsvneuensorgticker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;


public class tsvNeuensorgTicker extends AppCompatActivity {


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tsv_neuensorg_ticker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadSettings();
        loadMyGesamtKaderList(getApplicationContext());

        setOnClickListeners();


        createNotification("");
        startTimer();

        fbloadeintrage();
        fbEintragAnzeigeAktualisiern();


    }

    @Override
    protected void onResume() {
        anzeigeAktualisieren();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        //notify = false;
        timer.cancel();
        notificationManagerCompat.cancelAll();
        super.onDestroy();
    }

    void loadSettings(){
        millisHZgestartet = getSharedPreferences("settings",0).getLong(getString(R.string.saveID_ms_halbzeit_gestartet),0);
        halbZeit = getSharedPreferences("settings",0).getInt(getString(R.string.saveID_halbzeit),1);
        team = getSharedPreferences("settings",0).getInt(getString(R.string.saveID_team),1);
        tsv1Tore = getSharedPreferences("settings",0).getInt(getString(R.string.tsv1_tore_speicher_key),0);
        tsv2Tore = getSharedPreferences("settings",0).getInt(getString(R.string.tsv2_tore_speicher_key),0);
        tsv1GegnerTore = getSharedPreferences("settings",0).getInt(getString(R.string.gegner1_tore_speicher_key),0);
        tsv2GegnerTore = getSharedPreferences("settings",0).getInt(getString(R.string.gegner2_tore_speicher_key),0);

        anzeigeAktualisieren();
    }
    void saveSettings(){
        SharedPreferences.Editor editor = getSharedPreferences("settings",0).edit();
        editor.putLong(getString(R.string.saveID_ms_halbzeit_gestartet),millisHZgestartet);
        editor.putInt(getString(R.string.saveID_halbzeit),halbZeit);
        editor.putInt(getString(R.string.saveID_team),team);
        editor.putInt(getString(R.string.tsv1_tore_speicher_key),tsv1Tore);
        editor.putInt(getString(R.string.tsv2_tore_speicher_key),tsv2Tore);
        editor.putInt(getString(R.string.gegner1_tore_speicher_key),tsv1GegnerTore);
        editor.putInt(getString(R.string.gegner2_tore_speicher_key),tsv2GegnerTore);

        editor.apply();
    }

    public static List<Spieler> myGesamtKaderList;

    public static void saveMyGesamtKaderList(Context context, List<Spieler> neueGesamtKaderListe){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(neueGesamtKaderListe);
        editor.putString(context.getString(R.string.pref_key_myGesamtKaderListSaveKey),json);
        editor.apply();
    }

    static List<Spieler> loadMyGesamtKaderList(Context context){
        boolean spielerListeResetten = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(context.getString(R.string.pref_key_myGesamtKaderListSaveKey),"");
        List<Spieler> spielerList = gson.fromJson(json, new TypeToken<List<Spieler>>(){}.getType());
        if(spielerList==null){
            spielerListeResetten = true;
            Toast.makeText(context, "keiner Spieler in der Gesamtkaderliste, deswegen wird die Standardliste geladen",Toast.LENGTH_LONG).show();
            String[] defaultSpielerArray = context.getResources().getStringArray(R.array.default_gesamtkader);
            Arrays.sort(defaultSpielerArray);

            spielerList = new ArrayList<>();
            for(String spielerName:defaultSpielerArray){
                Spieler spieler = new Spieler();
                spieler.name = spielerName;
                spielerList.add(spieler);
            }
        }

        spielerList.sort(new Comparator<Spieler>() {
            public int compare(Spieler s1, Spieler s2) {
                return s1.name.compareTo(s2.name);
            }
        });

        //log spieler list
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < spielerList.size(); i++){
            sb.append("\n").append(spielerList.get(i).name);
        }
        Log.v("MainAcitivity","loaded spielerList "+sb.toString());
        if(spielerListeResetten){
            saveMyGesamtKaderList(context, spielerList);
        }
        return spielerList;
    }

    void anzeigeAktualisieren(){
        halbzeitAktualisieren(halbZeit);
        mannschaftAktualisieren(team);
        Button toranzeigen = findViewById(R.id.toranzeige);
        String anzeigeText = getHeimTore()+":"+getAuswartsTore();
        toranzeigen.setText(anzeigeText);
    }

    void openSpielInfoScreen(){
        final Intent spielInfoIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        spielInfoIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,SettingsActivity.SpielInfoFragment.class.getName());
        startActivity(spielInfoIntent);
    }
    void setOnClickListeners(){
        //top row
        findViewById(R.id.minutenanzeige).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeAktualisierenDialog();
            }
        });
        findViewById(R.id.heimteamanzeige).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isHeimspiel()) {
                    showTeamWechselDialog();
                }
                else {
                    openSpielInfoScreen();
                }
            }
        });
        findViewById(R.id.auswartsteamanzeige).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isHeimspiel()){
                    openSpielInfoScreen();
                }
                else{
                    showTeamWechselDialog();
                }

            }
        });
        findViewById(R.id.toranzeige).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToreAktualisiernDialog();
            }
        });

        //table
        findViewById(R.id.imButAnAbPfiff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"noch ned programmiert",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        findViewById(R.id.imButTor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String[]> stringArrayList = new ArrayList<>();
                stringArrayList.add(getStringArrayToParseFromStringArray("toradjektiv","adjektiv",0,false,false,0,getResources().getStringArray(R.array.tor_adjektive)));
                stringArrayList.add(getStringArrayToParseFromStringArray("leerzeichen",-1,0," "));
                stringArrayList.add(getStringArrayToParseFromStringArray("tor",0,0,"tor"));
                stringArrayList.add(getStringArrayToParseFromStringArray("für",+1,0," für "));
                stringArrayList.add(getStringArrayToParseFromStringArray("torteam","torteam",0,false,false,5,getSpielendeMannschaftenStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray("nach",+1,0," nach "));
                stringArrayList.add(getStringArrayToParseFromStringArray("nachwas","nach was",0,false,false,0,getResources().getStringArray(R.array.tor_nach)));
                stringArrayList.add(getStringArrayToParseFromStringArray(" von ",+1,0," von "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spieler1","spieler1",0,false,false,0,getMannschaftsSpielerMitGegner(3)));
                stringArrayList.add(getStringArrayToParseFromStringArray("aus",+1,0," aus "));
                stringArrayList.add(getStringArrayToParseFromStringArray("position1","position1",0,false,false,0,getResources().getStringArray(R.array.positionen)));
                stringArrayList.add(getStringArrayToParseFromStringArray(", ",+1,0,", "));
                stringArrayList.add(getStringArrayToParseFromStringArray("entfernung1","entfernung1",0,false,false,1,getEmptyStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray(" meter entfernt vom tor",-1,0," meter entfernt vom tor"));
                stringArrayList.add(getStringArrayToParseFromStringArray("leerzeichen",+1,0," "));
                stringArrayList.add(getStringArrayToParseFromStringArray("zweiteAktion","2. aktion",0,false,false,0,getResources().getStringArray(R.array.zweite_tor_aktion)));
                stringArrayList.add(getStringArrayToParseFromStringArray(" ",+1,0," "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spieler2","spieler2",0,false,false,0,getMannschaftsSpielerMitGegner(1)));
                stringArrayList.add(getStringArrayToParseFromStringArray("denBall",-1,0," den ball"));
                stringArrayList.add(getStringArrayToParseFromStringArray("aus",+1,0," aus "));
                stringArrayList.add(getStringArrayToParseFromStringArray("position2","position2",0,false,false,0,getResources().getStringArray(R.array.positionen)));
                stringArrayList.add(getStringArrayToParseFromStringArray(", ",+1,0,", "));
                stringArrayList.add(getStringArrayToParseFromStringArray("entfernung2","entfernung2",0,false,false,1,getEmptyStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray(" meter entfernt vom tor",-1,0," meter entfernt vom tor"));
                stringArrayList.add(getStringArrayToParseFromStringArray(" ",+1,0," "));
                stringArrayList.add(getStringArrayToParseFromStringArray("torbereich","torbereich",0,false,false,0,getResources().getStringArray(R.array.tor_bereich)));
                stringArrayList.add(getStringArrayToParseFromStringArray(" ins tor",-9,-1,0," ins tor"));

                stringArrayList.add(getStringArrayToParseFromStringArray("neuerZwischenstand",0,0,"\nneuer zwischenstand "));
                stringArrayList.add(getStringArrayToParseFromStringArray("heimteam",0,0,getHeimMannschaftsString()+" "));
                stringArrayList.add(getStringArrayToParseFromStringArray("heimteamtore",getString(R.string.heim_team_tore_key),0,false,false,1,getEmptyStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray("zu",0,0," - "));
                stringArrayList.add(getStringArrayToParseFromStringArray("auswartsteamtore", getString(R.string.auswarts_team_tore_key),0,false,false,1,getEmptyStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray("auswartsteam",0,0," "+getAuswartsMannschaftsString()));
                showStandardDialog(stringArrayList);

            }
        });
        findViewById(R.id.imButChance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String[]> stringArrayList = new ArrayList<>();
                stringArrayList.add(getStringArrayToParseFromStringArray("chanceadjektiv","adjektiv",0,false,false,0,getResources().getStringArray(R.array.chance_adjektive)));
                stringArrayList.add(getStringArrayToParseFromStringArray("leerzeichen wenn -1",-1,0," "));
                stringArrayList.add(getStringArrayToParseFromStringArray("chance",0,0,"chance"));
                stringArrayList.add(getStringArrayToParseFromStringArray(" für ",1,0," für "));
                stringArrayList.add(getStringArrayToParseFromStringArray("chanceteam","chanceteam",0,false,false,5,getSpielendeMannschaftenStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray("\n",0,0,"\n"));

                stringArrayList.add(getStringArrayToParseFromStringArray("spieler1","spieler1",0,false,false,0,getMannschaftsSpielerMitGegner(1)));
                stringArrayList.add(getStringArrayToParseFromStringArray(" ",1,0," "));
                stringArrayList.add(getStringArrayToParseFromStringArray("aktion1","aktion1",0,false,false,0,getResources().getStringArray(R.array.chance_aktionen)));

                stringArrayList.add(getStringArrayToParseFromStringArray(" aus ",+1,0," aus "));
                stringArrayList.add(getStringArrayToParseFromStringArray(" position1","position1",0,false,false,0,getResources().getStringArray(R.array.positionen)));
                stringArrayList.add(getStringArrayToParseFromStringArray(", ",+1,0,", "));
                stringArrayList.add(getStringArrayToParseFromStringArray("entfernung1","entfernung1",0,false,false,1,getEmptyStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray(" meter entfernt vom tor",-1,0," meter entfernt vom tor"));

                stringArrayList.add(getStringArrayToParseFromStringArray(" zu ",1,0," zu "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spieler2","spieler2",0,false,false,0,getMannschaftsSpielerMitGegner(1)));
                stringArrayList.add(getStringArrayToParseFromStringArray(", der ",-1,0,", der "));
                stringArrayList.add(getStringArrayToParseFromStringArray("aktion2","aktion2",0,false,false,0,getResources().getStringArray(R.array.chance_aktionen)));

                stringArrayList.add(getStringArrayToParseFromStringArray(" aus ",+1,0," aus "));
                stringArrayList.add(getStringArrayToParseFromStringArray(" position2","position2",0,false,false,0,getResources().getStringArray(R.array.positionen)));
                stringArrayList.add(getStringArrayToParseFromStringArray(", ",+1,0,", "));
                stringArrayList.add(getStringArrayToParseFromStringArray("entfernung2","entfernung2",0,false,false,1,getEmptyStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray(" meter entfernt vom tor",-1,0," meter entfernt vom tor"));

                stringArrayList.add(getStringArrayToParseFromStringArray(" ",+1,0," "));
                stringArrayList.add(getStringArrayToParseFromStringArray("ergebnis","ergebnis",0,false,false,0,getResources().getStringArray(R.array.chance_ergebnis_moglichkeiten)));

                showStandardDialog(stringArrayList);
            }
        });
        findViewById(R.id.imButEigentor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"noch ned programmiert",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        findViewById(R.id.imButFoul).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String[]> stringArrayList = new ArrayList<>();
                stringArrayList.add(getStringArrayToParseFromStringArray("fouladjektiv","adjektiv",0,false,false,0,getResources().getStringArray(R.array.foul_adjektive)));
                stringArrayList.add(getStringArrayToParseFromStringArray("leerzeichen",-1,0," "));
                stringArrayList.add(getStringArrayToParseFromStringArray("foul",0,0,"foul"));
                stringArrayList.add(getStringArrayToParseFromStringArray(" von ",+1,0," von "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spieler1","fouler",0,false,false,0,getMannschaftsSpielerMitGegner(3)));
                stringArrayList.add(getStringArrayToParseFromStringArray(" an ",+1,0," an "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spieler2","gefoulter",0,0,0,getMannschaftsSpielerMitGegner(4)));
                stringArrayList.add(getStringArrayToParseFromStringArray("in",+1,0," in "));
                stringArrayList.add(getStringArrayToParseFromStringArray("position","position",0,false,false,0,getResources().getStringArray(R.array.positionen)));
                stringArrayList.add(getStringArrayToParseFromStringArray(", ",+1,0,", "));
                stringArrayList.add(getStringArrayToParseFromStringArray("entfernung","entfernung",0,false,false,1,getEmptyStringArray()));
                stringArrayList.add(getStringArrayToParseFromStringArray(" meter entfernt vom tor",-1,0," meter entfernt vom tor"));
                stringArrayList.add(getStringArrayToParseFromStringArray("ergebnis","ergebnis",0,false,false,0,getResources().getStringArray(R.array.foul_ergebnis)));
                showStandardDialog(stringArrayList);

            }
        });
        findViewById(R.id.imButKarte).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String[]> stringArrayList = new ArrayList<>();
                        stringArrayList.add(getStringArrayToParseFromStringArray("kartenadjektiv","kartenadjektiv",0,false,false,0,getResources().getStringArray(R.array.kartenadjektiv)));
                        stringArrayList.add(getStringArrayToParseFromStringArray("leerzeichen",-1,0," "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("karte","karte",0,false,false,0,getResources().getStringArray(R.array.kartenmoglichkeiten)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" für ",+1,0," für "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("spieler","fouler",0,0,0,getMannschaftsSpielerMitGegner(4)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" nach ",+1,0," nach "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("kartengrund","kartengrund",0,false,false,0,getResources().getStringArray(R.array.kartengrund)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" an ",+1,0," an "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("spieler","gefoulter",0,0,0,getMannschaftsSpielerMitGegner(4)));
                        showStandardDialog(stringArrayList);
                    }
                });
            }
        });
        findViewById(R.id.imButFreistos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String[]> stringArrayList = new ArrayList<>();
                        stringArrayList.add(getStringArrayToParseFromStringArray("freistosadjektiv","adjektiv",0,false,false,0,getResources().getStringArray(R.array.freistosadjektiv)));
                        stringArrayList.add(getStringArrayToParseFromStringArray("leerzeichen",-1,0," "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("freistoß",0,0,"freistoß"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("für",+1,0," für "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("team","freistoßteam",0,false,false,0,getSpielendeMannschaftenStringArray()));
                        stringArrayList.add(getStringArrayToParseFromStringArray("nach",+1,0," nach "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("nachwas","nach was",0,false,false,0,getResources().getStringArray(R.array.kartengrund)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" von ",+1,0," von "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("spieler1","fouler",0,false,false,0,getMannschaftsSpielerMitGegner(3)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" an ",+1,0," an "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("spieler2","gefoulter",0,0,0,getMannschaftsSpielerMitGegner(4)));
                        stringArrayList.add(getStringArrayToParseFromStringArray("in",+1,0," in "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("position","position",0,false,false,0,getResources().getStringArray(R.array.positionen)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(", ",+1,0,", "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("entfernung","entfernung",0,false,false,1,getEmptyStringArray()));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" meter entfernt vom tor",-1,0," meter entfernt vom tor"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("es schießt",+1,0,"\nes schießt "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("spieler3","schütze",0,false,false,0,getMannschaftsSpielerMitGegner(1)));

                        stringArrayList.add(getStringArrayToParseFromStringArray(" ",+1,0," "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("ergebnis","ergebnis",0,false,false,0,getResources().getStringArray(R.array.chance_ergebnis_moglichkeiten)));
                        showStandardDialog(stringArrayList);
                    }
                });
            }
        });
        findViewById(R.id.imButElfmeter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String[]> stringArrayList = new ArrayList<>();
                        stringArrayList.add(getStringArrayToParseFromStringArray("elfmeteradjektiv","adjektiv",0,false,false,0,getResources().getStringArray(R.array.freistosadjektiv)));
                        stringArrayList.add(getStringArrayToParseFromStringArray("leerzeichen",-1,0," "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("elfmeter",0,0,"elfmeter"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("für",+1,0," für "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("team","elfmeterteam",0,false,false,0,getSpielendeMannschaftenStringArray()));
                        stringArrayList.add(getStringArrayToParseFromStringArray("nach",+1,0," nach "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("nachwas","nach was",0,false,false,0,getResources().getStringArray(R.array.kartengrund)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" von ",+1,0," von "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("spieler1","fouler",0,false,false,0,getMannschaftsSpielerMitGegner(3)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" an ",+1,0," an "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("spieler2","gefoulter",0,0,0,getMannschaftsSpielerMitGegner(4)));
                        stringArrayList.add(getStringArrayToParseFromStringArray("in",+1,0," in "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("position","position",0,false,false,0,getResources().getStringArray(R.array.positionen)));
                        stringArrayList.add(getStringArrayToParseFromStringArray(", ",+1,0,", "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("entfernung","entfernung",0,false,false,1,getEmptyStringArray()));
                        stringArrayList.add(getStringArrayToParseFromStringArray(" meter entfernt vom tor",-1,0," meter entfernt vom tor"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("es schießt",+1,0,"\nes schießt "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("spieler3","schütze",0,false,false,0,getMannschaftsSpielerMitGegner(1)));
                        showStandardDialog(stringArrayList);
                    }
                });
            }
        });
        findViewById(R.id.imButAuswechslung).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String[]> stringArrayList = new ArrayList<>();
                stringArrayList.add(getStringArrayToParseFromStringArray("halbzeitauswechslung?",0,20,"halbzeit"));
                stringArrayList.add(getStringArrayToParseFromStringArray("doppel",11,13,0,"doppel"));
                stringArrayList.add(getStringArrayToParseFromStringArray("dreifach",12,0,"dreifach"));
                stringArrayList.add(getStringArrayToParseFromStringArray("wechsel",0,0,"wechsel beim "));
                stringArrayList.add(getStringArrayToParseFromStringArray(getGegnerMannschaftsString(),0,20,getGegnerMannschaftsString()));
                stringArrayList.add(getStringArrayToParseFromStringArray(getTSVmannschaftsString(),0,-1,0,getTSVmannschaftsString()));
                stringArrayList.add(getStringArrayToParseFromStringArray(": ",0,0,": "));
                stringArrayList.add(getStringArrayToParseFromStringArray("kommen",+5,0,"es kommen "));
                stringArrayList.add(getStringArrayToParseFromStringArray("kommt",+1,+4,0,"es kommt "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spielerRein1","rein 1",0,false,false,0,getMannschaftsSpielerMitGegner(1)));
                stringArrayList.add(getStringArrayToParseFromStringArray("undRein1",+2,+4,0," und "));
                stringArrayList.add(getStringArrayToParseFromStringArray("kommaRein1",+3,0,", "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spielerRein2","rein 2",0,false,false,0,getMannschaftsSpielerMitGegner(1)));
                stringArrayList.add(getStringArrayToParseFromStringArray("undRein2",+1,0," und "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spielerRein3","rein 3",0,false,false,0,getMannschaftsSpielerMitGegner(1)));
                stringArrayList.add(getStringArrayToParseFromStringArray("leerzeile",0,3,""));
                stringArrayList.add(getStringArrayToParseFromStringArray(" für ",+1,0," für "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spielerRaus1","raus 1",0,false,false,0,getMannschaftsSpielerMitGegner(4)));
                stringArrayList.add(getStringArrayToParseFromStringArray("undRaus1",+2,+4,0," und "));
                stringArrayList.add(getStringArrayToParseFromStringArray("kommaRaus1",+3,0,", "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spielerRaus2","raus 2",0,false,false,0,getMannschaftsSpielerMitGegner(4)));;
                stringArrayList.add(getStringArrayToParseFromStringArray("undRaus2",+1,0," und "));
                stringArrayList.add(getStringArrayToParseFromStringArray("spielerRaus3","raus 3",0,false,false,0,getMannschaftsSpielerMitGegner(4)));;
                stringArrayList.add(getStringArrayToParseFromStringArray("leerzeile",0,3,""));
                stringArrayList.add(getStringArrayToParseFromStringArray(", der aufgrund ",+1,0,", der aufgrund "));
                stringArrayList.add(getStringArrayToParseFromStringArray("grund","grund",0,false,false,0,getResources().getStringArray(R.array.auswechslungsgrunde)));
                stringArrayList.add(getStringArrayToParseFromStringArray(" das spielfeld verlassen muss",-1,0," das spielfeld verlassen muss"));
                showStandardDialog(stringArrayList);
            }
        });
        findViewById(R.id.imButSonstiges).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String[]> stringArrayList = new ArrayList<>();
                //stringArrayList.add(getStringArrayToParseFromStringArray("platzhalter",getString(R.string.langerplatzhalterTODOwegmachenundmatchparent),0,false,false,0,getEmptyStringArray()));
                stringArrayList = setFocusOnEdittext(stringArrayList);
                showStandardDialog(stringArrayList);

            }
        });
        findViewById(R.id.imButPretext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String[]> stringArrayList = new ArrayList<>();
                        StringBuilder pretextStringBuilderAnfang = new StringBuilder();
                        pretextStringBuilderAnfang.append("herzlich willkommen zum ");
                        if(!isHeimspiel()){
                            pretextStringBuilderAnfang.append("auswärtsspiel des ");
                            pretextStringBuilderAnfang.append(getTSVmannschaftsString());
                            pretextStringBuilderAnfang.append(" gegen ");
                            pretextStringBuilderAnfang.append(getHeimMannschaftsString());
                        }
                        else{
                            pretextStringBuilderAnfang.append("heimspiel des ");
                            pretextStringBuilderAnfang.append(getTSVmannschaftsString());
                            pretextStringBuilderAnfang.append(" gegen ");
                            pretextStringBuilderAnfang.append(getGegnerMannschaftsString());
                        }
                        String pretextAnfang = pretextStringBuilderAnfang.toString();
                        stringArrayList.add(getStringArrayToParseFromStringArray(pretextAnfang,0,0,pretextAnfang));

                        stringArrayList.add(getStringArrayToParseFromStringArray("",0,0,0,"\n"));

                        stringArrayList.add(getStringArrayToParseFromStringArray("die",0,+1,0,"die"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("zuschaueranzahl",0,1,"zuschauer"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("zuschauer",0,0," zuschauer freuen sich auf eine spannende partie"));

                        stringArrayList.add(getStringArrayToParseFromStringArray("",+1,0,0," bei "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("temperatur",0,1,"temperatur"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("grad",-1,0,"°c"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","-2,+5","+14,+23",0," und "));   //dep temp wetter           antidep wind platz
                        stringArrayList.add(getStringArrayToParseFromStringArray("","-3,+4,+13",+22,0,", "));             //dep temp wetter wind      antidep platz
                        stringArrayList.add(getStringArrayToParseFromStringArray("","-4,+3,+21",12,0,", "));             //dep temp wetter platz     antidep wind
                        stringArrayList.add(getStringArrayToParseFromStringArray("","-5,+2,+11,+20",0,0,", "));          //dep temp wetter wind platz
                        stringArrayList.add(getStringArrayToParseFromStringArray("",+1,-6,0," bei "));                  //dep wetter                antidep temp
                        stringArrayList.add(getStringArrayToParseFromStringArray("wetter","wetter",0,false,false,0,getResources().getStringArray(R.array.wetter)));
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","-8,+8","+17,-1",0," und "));    //dep wind temp             antidep platz wetter
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","-2,+7","+16,-9",0," und "));    //dep wind wetter           antidep platz temp
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","-3,-10,+6","+15",0," und "));     //dep wind temp wetter      antidep platz
                        stringArrayList.add(getStringArrayToParseFromStringArray("","+5,+14,-4",-11,0,", "));               //dep wind platz wetter     antidep temp
                        stringArrayList.add(getStringArrayToParseFromStringArray("","+4,+13,-12",-5,0,", "));               //dep wind platz temp       antidep wetter
                        stringArrayList.add(getStringArrayToParseFromStringArray("","-13,-6,+3,+12",0,0,", "));               //dep wind wetter temp platz
                        stringArrayList.add(getStringArrayToParseFromStringArray("",+2,"-14,-7",0," bei "));            //dep wind                  antidep temp wetter
                        stringArrayList.add(getStringArrayToParseFromStringArray("","+1",0,0,"windstärke "));
                        stringArrayList.add(getStringArrayToParseFromStringArray("windstärke",0,1,"windstärke"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("",8,"-17,-10,-1",0," bei "));          //dep platz                 antidep wetter temp wind
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","+7,-18","-11,-2",0," und "));    //dep platz temp            antidep wetter wind
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","+6,-12","-19,-3",0," und "));   //dep platz wetter          antidep temp wind
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","+5,-4","-20,-13",0," und "));   //dep platz wind            antidep temp wetter
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","+4,-5,-21","-14",0," und "));   //dep platz wind temp       antidep wetter
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","+3,-6,-15","-22",0," und "));   //dep platz wind wetter     antidep temp
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","+2,-16,-23","-7",0," und "));   //dep platz wetter temp     antidep wind
                        stringArrayList.add(getStringArrayToParseFromStringArray("und","+1,-8,-17,-24",0,0," und "));   //dep platz temp wetter wind
                        stringArrayList.add(getStringArrayToParseFromStringArray("platzverhältnisse","platzverhältnisse",0,false,false,0,getResources().getStringArray(R.array.platzverhaltnisse)));
                        stringArrayList.add(getStringArrayToParseFromStringArray("",-1,0,0," platzverhältnissen"));


                        stringArrayList.add(getStringArrayToParseFromStringArray("",0,0,0,"\n\n"));
                        stringArrayList.add(getStringArrayToParseFromStringArray("kader",0,0,21,"heute mit dabei im kader des "+getTSVmannschaftsString()+": "+getMannschaftsSpielerStringMitKommasUndUnd()));

                        showStandardDialog(stringArrayList);
                    }
                });
            }
        });
        findViewById(R.id.imButPosttext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getMannschaftsSpielerStringMitKommasUndUnd();
                        Toast.makeText(getApplicationContext(),"noch ned programmiert",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    String[] getMannschaftsSpielerMitGegner(int fall){
        List<String> spielerList = stringArrayToStringList(getMannschaftsSpieler());
        String gegnerspieler = "gegner spieler";
        switch (fall){
            case 1:
                gegnerspieler = "ein gegnerischer spieler";
                break;
            case 2:
                gegnerspieler = "eines gegnerischen spielers";
                break;
            case 3:
                gegnerspieler = "einem gegnerischen spieler";
                break;
            case 4:
                gegnerspieler = "einen gegnerischen spieler";
                break;
        }
        spielerList.add(0,gegnerspieler);
        return stringListToStringArray(spielerList);
    }
    String getMannschaftsSpielerStringMitKommasUndUnd(){
        StringBuilder stringBuilder = new StringBuilder();
        String vorletzterSpieler = getMannschaftsSpieler()[getMannschaftsSpieler().length-2];
        String letzterSpieler = getMannschaftsSpieler()[getMannschaftsSpieler().length-1];

        for(String spieler:getMannschaftsSpieler()){
            stringBuilder.append(spieler);
            if(spieler.equals(vorletzterSpieler)){
                stringBuilder.append(" und ");
            }
            else if (!spieler.equals(letzterSpieler)){
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }
    String[] getMannschaftsSpieler(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        List<String> mannschaftsSpielerStringList = new ArrayList<>();
        if(team == 1){
            for (Spieler spieler:loadMyGesamtKaderList(getApplicationContext())){
                if(spieler.letztesMalInErsterMannschaft!=null){
                    if(spieler.letztesMalInErsterMannschaft<=0){
                        mannschaftsSpielerStringList.add(spieler.name);
                    }
                }
            }
        }
        else {
            for (Spieler spieler:loadMyGesamtKaderList(getApplicationContext())){
                if(spieler.letztesMalInZweiterMannschaft!=null){
                    if(spieler.letztesMalInZweiterMannschaft<=0){
                        mannschaftsSpielerStringList.add(spieler.name);
                    }
                }
            }
        }
        if(mannschaftsSpielerStringList.size()==0){
            return getEmptyStringArray();
        }
        return mannschaftsSpielerStringList.toArray(new String[0]);
    }

    int team = 1;
    int tsv1Tore = 0;
    int tsv2Tore = 0;
    int tsv1GegnerTore = 0;
    int tsv2GegnerTore = 0;
    String notificationChannelName = "vOUeHxCCvq";    //random
    int notificationId = 281980;    //random
    String contentTitle = "tsv neuensorg ticker";
    boolean notify = true;
    NotificationCompat.Builder builder;
    NotificationManagerCompat notificationManagerCompat;
    void createNotification(String contentText){
        String notificationID = "notificationID";
        if(android.os.Build.VERSION.SDK_INT> android.os.Build.VERSION_CODES.O){
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // The user-visible name of the channel.
            CharSequence name = getString(R.string.notificationChannelName);

            // The user-visible description of the channel.
            String description = getString(R.string.notificationChannelDescription);

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(notificationID, name,importance);
            channel.setDescription(description);
            mNotificationManager.createNotificationChannel(channel);

        }
        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(getString(R.string.key_show_notification),true)){
            Intent goToAppIntent = new Intent(getApplicationContext(),tsvNeuensorgTicker.class);
            PendingIntent goToApppendingIntent = PendingIntent.getActivity(this,0,goToAppIntent,0);

            builder = new NotificationCompat.Builder(this,notificationChannelName)
                    .setSmallIcon(R.drawable.fussball)
                    .setContentTitle(contentTitle)
                    .setContentText("")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVisibility(VISIBILITY_PUBLIC)
                    .setContentIntent(goToApppendingIntent)
                    .setChannelId(notificationID);
            notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(notificationId, builder.build());
        }
    }
    void updateNotification(String contentText,int progress, int progressmax){
        if(progress>progressmax){
            progress=progressmax;
        }
        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(getString(R.string.key_show_notification),true)){
            if(builder == null){
                createNotification("");
            }
            builder.setContentText(contentText).setProgress(progressmax,progress,false);
            notificationManagerCompat.notify(notificationId,builder.build());
        }
    }
    Timer timer;
    void startTimer(){
        timer = new Timer();
        timer.scheduleAtFixedRate(UpdateSeconds,0,1000);
    }
    long getTimeSeitHzstart(){
        long timeMillis = System.currentTimeMillis();
        return timeMillis - millisHZgestartet;
    }
    String getZeitAnzeigeText(){
        long timeSeitHZstart = getTimeSeitHzstart();
        String anzeigetext = "";
        int msProMin = 60*1000;
        long seconds = (timeSeitHZstart%(msProMin))/1000;
        long mins = (timeSeitHZstart-seconds)/msProMin;
        if(halbZeit==2){
            mins = mins + 45;
            if(mins>=90){
                anzeigetext = anzeigetext + "90:00" + "+";
                mins = mins - 90;
            }
        }
        else {
            if(mins>=45){
                anzeigetext = anzeigetext + "45:00" + "+";
                mins = mins - 45;
            }
        }
        String anzeigeSecs = "";
        String anzeigeMins = "";
        if(seconds < 10){
            anzeigeSecs = anzeigeSecs + "0";
        }
        if(mins < 10){
            anzeigeMins= anzeigeMins + "0";
        }
        anzeigeSecs = anzeigeSecs + seconds;
        anzeigeMins = anzeigeMins + mins;
        return anzeigetext + anzeigeMins + ":" + anzeigeSecs;
    }
    TimerTask UpdateSeconds = new TimerTask() {
        @Override
        public void run() {
            final String finalanzeigetext = getZeitAnzeigeText();
                    runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Button minutenanzeige = findViewById(R.id.minutenanzeige);
                    minutenanzeige.setText(finalanzeigetext);
                }
            });
            updateNotification(finalanzeigetext,(int)getTimeSeitHzstart(),45*60*1000);
        }
    };

    public long millisHZgestartet = 0;
    public int halbZeit = 1;

    boolean getBooleanPref(String prefKeyId,boolean defaultReturnValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sharedPreferences.getBoolean(prefKeyId,defaultReturnValue);
    }

    List<String> alleEintraege = new ArrayList<>();

    void openWhatsappGroup(String groupUrl){
        Intent intentWhatsapp = new Intent(Intent.ACTION_VIEW);
        intentWhatsapp.setData(Uri.parse(groupUrl));
        intentWhatsapp.setPackage("com.whatsapp");
        startActivity(intentWhatsapp);
    }

    void sendToWhatsapp(String text){
        Log.v("sendToWhatsapp",text);
        final ComponentName name = new ComponentName("com.whatsapp", "com.whatsapp.ContactPicker");
        Intent oShareIntent = new Intent();
        oShareIntent.setComponent(name);
        oShareIntent.setType("text/plain");
        oShareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(oShareIntent);
    }

    void fbEintragAnzeigeAktualisiern(){
        LinearLayout anzeigeLayout = findViewById(R.id.linearLayoutEintragAnzeige);
        anzeigeLayout.removeAllViews();
        for(int i = alleEintraege.size()-1; i>=0; i--){
            final int index = i;
            final String eintrag = alleEintraege.get(i);
            TextView textview = new TextView(getBaseContext());
            textview.setText(eintrag);
            textview.setTextSize(20);
            int padding = 20;
            textview.setPadding(0, padding,0, padding);
            textview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(getString(R.string.eintrag_index_key_in_bundle),index);
                    bundle.putString(getString(R.string.eintrag_text_key_in_bundle),eintrag);
                    DialogFragment fragment = new fbspiele.tsvneuensorgticker.eintragAusgewaehltFragment();
                    fragment.setArguments(bundle);
                    fragment.show(getFragmentManager(),"eintragAusgewaehltDialog");
                }
            });
            anzeigeLayout.addView(textview);
        }
    }
    List<String[]> setFocusOnEdittext(List<String[]> stringArrayList){
        stringArrayList.add(getStringArrayToParseFromStringArray("setFocusOnEdittext",0,4,""));
        return stringArrayList;
    }
    void eingabeSpeichern(String eingabe){
        alleEintraege.add(eingabe);
        eintraegeSpeichern();
    }
    void eintraegeSpeichern(){
        fbwritelinestofile(StringArrayToStringMitTrennzeichen(stringListToStringArray(alleEintraege),getString(R.string.eintrage_trenner)),getString(R.string.eintrage_savefile_name));
        fbEintragAnzeigeAktualisiern();
    }
    void deleteEintrag(int index){
        alleEintraege.remove(index);
        eintraegeSpeichern();
    }
    void deleteEintragContext(final int index){

        AlertDialog alertDialog = new AlertDialog.Builder(tsvNeuensorgTicker.this).create();
        alertDialog.setTitle("löschen");
        alertDialog.setMessage("sicher, dass du den eintrag:\n"+alleEintraege.get(index)+"\nlöschen willst?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ja",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEintrag(index);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "nö",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    String[] getStringArrayFromFile(String filename, String splitter){
        String returnString;
        returnString = fbreadlinesfromfile(filename);
        returnString.split(splitter);
        return fbreadlinesfromfile(filename).split(splitter);
    }
    public String StringArrayToStringMitTrennzeichen(String[] array,String trennzeichen){
        StringBuilder stringBuilder = new StringBuilder();
        for (String string:array){
            stringBuilder.append(string).append(trennzeichen);
        }
        return stringBuilder.toString();
    }
    void fbwritelinestofile(String text, String filename){
        Log.v("writing",text);
        try{
            File file = new File(getApplicationContext().getFilesDir(),filename);
            FileOutputStream out = new FileOutputStream(file);
            out.write(text.getBytes());
            out.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    void fbloadeintrage(){
        fbeintrageverarbeiten(fbreadlinesfromfile(getString(R.string.eintrage_savefile_name)));
    }
    void fbeintrageverarbeiten(String message){
        String[] stringArrayMessage = message.split(getString(R.string.eintrage_trenner));
        alleEintraege = stringArrayToStringList(stringArrayMessage);
    }
    String fbreadlinesfromfile(String filename){
        try{
            File file = new File(getApplicationContext().getFilesDir(), filename);
            if(!file.exists()){
                if(!file.createNewFile()){
                    Log.w("readlinesfromfile","fbreadlinefromfile konnte file nicht erzeugen");
                }
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            boolean ersteline = true;
            while ((line = bufferedReader.readLine())!=null){
                if(ersteline){
                    ersteline = false;
                }
                else {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(line);
            }
            bufferedReader.close();
            String read = stringBuilder.toString();
            Log.v("readFromFile",read);
            return read;
        }
        catch (Exception e){
            e.printStackTrace();
            return "error loading file";
        }
    }

    void showTimeAktualisierenDialog(){
        DialogFragment standardFragment = new fbspiele.tsvneuensorgticker.timeAktualisierenDialogFragment();

        standardFragment.show(getFragmentManager(),"timeAktualisierenDialog");
    }
    void showToreAktualisiernDialog(){
        new fbspiele.tsvneuensorgticker.toreAktualisierenDialogFragment().show(getFragmentManager(),"toreAktualisierenDialog");
    }

    String intToStringMitNullern(int intToConvert, int stellen){
        if(stellen<1){
            return "error in intToStringMitNullern . stellen < 1";
        }
        if(intToConvert<0){
            return "-"+intToStringMitNullern(Math.abs(intToConvert),stellen-1);
        }
        if(intToConvert>=Math.pow(10,stellen)){
            return "error in intToStringMitNullern . intToConvert >= 10^stellen";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i<= stellen; i++){
            if(intToConvert<Math.pow(10,i)){
                for (int k = 1; k <= stellen - i; k++){
                    stringBuilder.append("0");
                }
                stringBuilder.append(String.valueOf(intToConvert));
                return stringBuilder.toString();
            }
        }
        return "error in intToStringMitNullern . ging bis zum return";
    }

    int stringarrayToParseToDialogHeadersize = 0;
    String[] getEmptyStringArray(){
        String[] emptyarray = new String[1];
        emptyarray[0]="";
        return emptyarray;
    }
    String[] getEndStringArrayToParseFromStringArray(){
        return getStringArrayToParseFromStringArray(getString(R.string.key_string_array_list_to_parse_to_std_action_dialog_last_array_indicator),0,0,"");
    }
    String[] getStringArrayToParseFromStringArray(String shortdescription, int dependency, int anzeigeViewObjectID, String einzigerAnzeigeString){
        return getStringArrayToParseFromStringArray(shortdescription,dependency,0,anzeigeViewObjectID,einzigerAnzeigeString);
    }
    String[] getStringArrayToParseFromStringArray(String shortdescription, String dependency, int antidependency, int anzeigeViewObjectID, String einzigerAnzeigeString){
        return getStringArrayToParseFromStringArray(shortdescription, dependency,String.valueOf(antidependency),anzeigeViewObjectID, einzigerAnzeigeString);
    }
    String[] getStringArrayToParseFromStringArray(String shortdescription, int dependency, String antidependency, int anzeigeViewObjectID, String einzigerAnzeigeString){
        return getStringArrayToParseFromStringArray(shortdescription, String.valueOf(dependency),antidependency,anzeigeViewObjectID, einzigerAnzeigeString);
    }
    String[] getStringArrayToParseFromStringArray(String shortdescription, int dependency, int antidependency, int anzeigeViewObjectID, String einzigerAnzeigeString){
        return getStringArrayToParseFromStringArray(shortdescription, String.valueOf(dependency),String.valueOf(antidependency),anzeigeViewObjectID, einzigerAnzeigeString);
    }
    String[] getStringArrayToParseFromStringArray(String shortdescription, String dependency, String antidependency, int anzeigeViewObjectID, String einzigerAnzeigeString){
        String[] einzigerAnzeigeStringArray = new String[1];
        einzigerAnzeigeStringArray[0]=einzigerAnzeigeString;
        return getStringArrayToParseFromStringArray(shortdescription,einzigerAnzeigeString,dependency,antidependency,false,false,anzeigeViewObjectID,einzigerAnzeigeStringArray);
    }

    String[] getStringArrayToParseFromStringArray(String shortdescription, String displayText, int dependency, boolean gegnerTeamAvailable, boolean gegnerSpielerAvailable, int anzeigeViewObjectID, String[] array){
        return getStringArrayToParseFromStringArray(shortdescription, displayText, dependency,0, gegnerTeamAvailable, gegnerSpielerAvailable, anzeigeViewObjectID, array);
    }
    String[] getStringArrayToParseFromStringArray(String shortdescription, String displayText, int dependency, int antidependency, int anzeigeViewObjectID, String[] array){
        return getStringArrayToParseFromStringArray(shortdescription,displayText,dependency,antidependency,false,false,anzeigeViewObjectID,array);
    }
    String[] getStringArrayToParseFromStringArray(String shortdescription, String displayText, int dependency, int antidependency, boolean gegnerTeamAvailable, boolean gegnerSpielerAvailable, int anzeigeViewObjectID, String[] array) {
        return getStringArrayToParseFromStringArray(shortdescription,displayText,String.valueOf(dependency),String.valueOf(antidependency),gegnerTeamAvailable,gegnerSpielerAvailable,anzeigeViewObjectID,array);
    }
    String[] getStringArrayToParseFromStringArray(String shortdescription, String displayText, String dependency, String antidependency, boolean gegnerTeamAvailable, boolean gegnerSpielerAvailable, int anzeigeViewObjectID, String[] array){
        List<String> stringListToBuildArray = new ArrayList<>();
        stringListToBuildArray.add(shortdescription);
        stringListToBuildArray.add(displayText);
        stringListToBuildArray.add(dependency);
        stringListToBuildArray.add(antidependency);
        if(gegnerTeamAvailable){
            stringListToBuildArray.add("1");
        }
        else{
            stringListToBuildArray.add("0");
        }
        if(gegnerSpielerAvailable){
            stringListToBuildArray.add("1");
        }
        else{
            stringListToBuildArray.add("0");
        }
        stringListToBuildArray.add(intToStringMitNullern(anzeigeViewObjectID,stellenDesBundleToParseKeyInt));

        stringarrayToParseToDialogHeadersize = stringListToBuildArray.size();

        //HEADER FERTIG

        stringListToBuildArray.addAll(Arrays.asList(array));

        return  stringListToBuildArray.toArray(new String[0]);
    }
    String[] stringListToStringArray(List<String> stringList){
        return  stringList.toArray(new String[0]);
    }
    List<String> stringArrayToStringList(String[] stringArray){
        return new ArrayList<>(Arrays.asList(stringArray));
    }
    List<String[]> stringArraysToStringArrayList(String[]... stringArrays){
        return new ArrayList<>(Arrays.asList(stringArrays));
    }
    int stellenDesBundleToParseKeyInt = 7;
    void showStandardDialog(String[]... gesStringarrays){
        showStandardDialog(stringArraysToStringArrayList(gesStringarrays));
    }
    void showStandardDialog(List<String[]> gesStringarray){
        gesStringarray.add(getEndStringArrayToParseFromStringArray());
        DialogFragment fragment = new fbspiele.tsvneuensorgticker.standardDialogFragment();
        Bundle bundle = new Bundle();
        String iString = "0";
        int i;
        for (i = 0; i<gesStringarray.size();i++){
            bundle.putStringArray(getString(R.string.key_string_array_list_to_parse_to_std_action_dialog)+intToStringMitNullern(i,stellenDesBundleToParseKeyInt),gesStringarray.get(i));
        }
        bundle.putStringArray(getString(R.string.key_string_array_list_to_parse_to_std_action_dialog)+intToStringMitNullern(i+1,stellenDesBundleToParseKeyInt),getEndStringArrayToParseFromStringArray());
        fragment.setArguments(bundle);
        fragment.show(getFragmentManager(),"standardDialog");
    }

    void showTeamWechselDialog(){
        DialogFragment fragment = new fbspiele.tsvneuensorgticker.teamWechselDialogFragment();
        fragment.show(getFragmentManager(),"teamWechselDialog");
    }

    void halbzeitAktualisieren(final int neueHalbzeit){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                halbZeit = neueHalbzeit;
                Log.v("halbzeitaktualisieren",""+halbZeit);
                saveSettings();
            }
        });
    }



    String getAuswartsMannschaftsString(){
        if(!isHeimspiel()){
            return getTSVmannschaftsString();
        }
        else {
            return getGegnerMannschaftsString();
        }
    }
    String getHeimMannschaftsString(){
        if(isHeimspiel()){
            return getTSVmannschaftsString();
        }
        else {
            return getGegnerMannschaftsString();
        }
    }
    String[] getSpielendeMannschaftenStringArray(){
        String[] mannschaften = new String[2];
        mannschaften[0] = getTSVmannschaftsString();
        mannschaften[1] = getGegnerMannschaftsString();
        return mannschaften;
    }
    Boolean isHeimspiel(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(team==1){
            return sharedPreferences.getBoolean(getString(R.string.pref_heimspiel_erste_mannschaft_key),true);
        }
        else {
            return sharedPreferences.getBoolean(getString(R.string.pref_heimspiel_zweite_mannschaft_key),true);
        }
    }
    String getGegnerMannschaftsString(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(team==1){
            return sharedPreferences.getString(getString(R.string.pref_gegner_erste_mannschaft_key),"gegner");
        }
        else {
            return sharedPreferences.getString(getString(R.string.pref_gegner_zweite_mannschaft_key),"gegner");
        }

    }
    String getTSVmannschaftsString(){
        if(team==1){
            return getString(R.string.name_erste_mannschaft);
        }
        else {
            return getString(R.string.name_zweite_mannschaft);
        }
    }
    void setGegnerTore(int tore){
        if(team==1){
            Log.v("setTore","gegner 1: "+ tore);
            tsv1GegnerTore = tore;
        }
        else {
            Log.v("setTore","gegner 2: "+ tore);
            tsv2GegnerTore = tore;
        }
        saveSettings();
        anzeigeAktualisieren();
    }
    void setTsvTore(int tore){
        if(team==1){
            Log.v("setTore","tsv 1: "+ tore);
            tsv1Tore = tore;
        }
        else {
            Log.v("setTore","tsv 2: "+ tore);
            tsv2Tore = tore;
        }
        saveSettings();
        anzeigeAktualisieren();
    }
    int getGegnerTore(){
        if(team==1){
            return tsv1GegnerTore;
        }
        else {
            return tsv2GegnerTore;
        }
    }
    void setHeimTore(int tore){
        Log.v("setTore","heim: "+ tore);
        if(isHeimspiel()){
            setTsvTore(tore);
        }
        else {
            setGegnerTore(tore);
        }
    }
    void setAuswartsTore(int tore){
        Log.v("setTore","auswarts: "+ tore);
        if(isHeimspiel()){
            setGegnerTore(tore);
        }
        else {
            setTsvTore(tore);
        }
    }
    int getTsvTore(){
        if(team==1){
            return tsv1Tore;
        }
        else {
            return tsv2Tore;
        }
    }
    int getHeimTore(){
        if(isHeimspiel()){
            return getTsvTore();
        }
        else {
            return getGegnerTore();
        }
    }
    int getAuswartsTore(){
        if(!isHeimspiel()){
            return getTsvTore();
        }
        else {
            return getGegnerTore();
        }
    }
    void mannschaftAktualisieren(int mannschaft){
        team = mannschaft;
        saveSettings();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button heimTeamAnzeige = findViewById(R.id.heimteamanzeige);
                Button auswartsTeamAnzeige = findViewById(R.id.auswartsteamanzeige);
                heimTeamAnzeige.setText(getHeimMannschaftsString());
                auswartsTeamAnzeige.setText(getAuswartsMannschaftsString());
                saveSettings();
            }
        });
    }


    void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            Log.v("imm","!=null");
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        else{
            Log.v("imm","null");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tsv_neuensorg_ticker, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent_settings = new Intent(this, SettingsActivity.class);
            startActivity(intent_settings);
            return true;
        }
        if(id == R.id.menu_action_text_delete){
            AlertDialog alertDialog = new AlertDialog.Builder(tsvNeuensorgTicker.this).create();
            alertDialog.setTitle("löschen");
            alertDialog.setMessage("sicher, dass du alles (einträge + tore) löschen willst?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ja",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            tsv1Tore = 0;
                            tsv1GegnerTore = 0;
                            tsv2Tore = 0;
                            tsv2GegnerTore = 0;
                            fbwritelinestofile("",getString(R.string.eintrage_savefile_name));
                            saveSettings();
                            fbloadeintrage();
                            fbEintragAnzeigeAktualisiern();
                            dialog.dismiss();
                            anzeigeAktualisieren();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "nö",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}

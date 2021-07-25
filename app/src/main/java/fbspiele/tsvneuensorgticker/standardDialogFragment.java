package fbspiele.tsvneuensorgticker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class standardDialogFragment extends DialogFragment {
    private static standardDialogFragment instance = null;
    View thisView;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        instance = this;
        final View view = inflater.inflate(R.layout.standardlayout_aktion, container, false);        //FINAL!!!!!

        final EditText eingabeTextFeldStandardLayout = view.findViewById(R.id.eingabeTextFeldStandardLayout);
        eingabeTextFeldStandardLayout.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                //TODO enter geht ned
                return false;
            }
        });

        String[] lastArrayStringIndicator = ((tsvNeuensorgTicker)getActivity()).getEndStringArrayToParseFromStringArray();
        boolean stopLoop = false;
        List<String[]> gesStringArray = new ArrayList<>();
        for (int i = 0;!stopLoop;i++){
            String[] newStringArray = getArguments().getStringArray(getString(R.string.key_string_array_list_to_parse_to_std_action_dialog)+((tsvNeuensorgTicker)getActivity()).intToStringMitNullern(i,((tsvNeuensorgTicker)getActivity()).stellenDesBundleToParseKeyInt));
            if(Arrays.equals(newStringArray,lastArrayStringIndicator)){
                if(newStringArray==null){
                    Log.v("Std Dialog Frag","newStringArray null");
                }
                stopLoop = true;
            }
            else {
                gesStringArray.add(newStringArray);
                if(newStringArray==null){
                    Log.v("newStringArray","null");
                }
            }
        }
        LinearLayout layout = view.findViewById(R.id.standardLayoutLinearLayoutSpieler);
        if(layout==null){
            Log.v("layout","null");
            return view;
        }

        timeAnzeige = ((tsvNeuensorgTicker)getActivity()).getZeitAnzeigeText();

        String[] shortdescription = new String[gesStringArray.size()];
        final String[] displayTextsInOnCreate = new String[gesStringArray.size()];
        dependencys = new String[gesStringArray.size()];
        antidependencys = new String[gesStringArray.size()];
        final boolean[] gegnerTeamAvailable = new boolean[gesStringArray.size()];
        final boolean[] gegnerSpielerAvailable = new boolean[gesStringArray.size()];
        final String[] zuschreibenderString = new String[gesStringArray.size()];
        int[] anzeigeViewObjectID= new int[gesStringArray.size()];
        final List<String[]> arrayList = new ArrayList<>();


        eingabeTexts = new String[gesStringArray.size()];


        int stringarrayToParseToDialogHeadersize = ((tsvNeuensorgTicker)getActivity()).stringarrayToParseToDialogHeadersize;
        for (int i = 0; i<gesStringArray.size();i++){
            String[] tempStringArray = gesStringArray.get(i);


            shortdescription[i] = tempStringArray[0];
            displayTextsInOnCreate[i] = tempStringArray[1];
            dependencys[i] = tempStringArray[2];
            antidependencys[i] = tempStringArray[3];
            gegnerTeamAvailable[i] = getTrueWennStringGleich1(tempStringArray[4]);
            gegnerSpielerAvailable[i] = getTrueWennStringGleich1(tempStringArray[5]);
            anzeigeViewObjectID[i] = Integer.parseInt(tempStringArray[6]);



            String[] restVonTempStringArray = new String[tempStringArray.length-stringarrayToParseToDialogHeadersize];
            System.arraycopy(tempStringArray,stringarrayToParseToDialogHeadersize,restVonTempStringArray,0,tempStringArray.length-stringarrayToParseToDialogHeadersize);
            arrayList.add(restVonTempStringArray);

        }

        for (int i = 0; i<displayTextsInOnCreate.length;i++){

            final int finalIntI = i;

            if(anzeigeViewObjectID[i]==0){      //standardeingabe radiobutton oder fester text
                if(arrayList.get(i).length>1){

                    Button button = new Button(getDialog().getContext());
                    button.setText(displayTextsInOnCreate[i]);
                    button.setAllCaps(false);
                    eingabeTexts[i]="";
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogFragment fragment = new fbspiele.tsvneuensorgticker.radioauswahlMitHeader();
                            Bundle bundle = new Bundle();
                            String[] stringArrayMitHeader;
                            if(gegnerSpielerAvailable[finalIntI]){
                                stringArrayMitHeader = new String[arrayList.get(finalIntI).length+2];
                                stringArrayMitHeader[0] = displayTextsInOnCreate[finalIntI];
                                stringArrayMitHeader[1] = "einem gegnerischen Spieler";
                                System.arraycopy(arrayList.get(finalIntI),0,stringArrayMitHeader,2,arrayList.get(finalIntI).length);
                            }
                            else if(gegnerTeamAvailable[finalIntI]){
                                stringArrayMitHeader = new String[arrayList.get(finalIntI).length+2];
                                stringArrayMitHeader[0] = displayTextsInOnCreate[finalIntI];
                                stringArrayMitHeader[1] = ((tsvNeuensorgTicker)getActivity()).getGegnerMannschaftsString();
                                System.arraycopy(arrayList.get(finalIntI),0,stringArrayMitHeader,2,arrayList.get(finalIntI).length);
                            }
                            else {
                                stringArrayMitHeader = new String[arrayList.get(finalIntI).length+1];
                                stringArrayMitHeader[0] = displayTextsInOnCreate[finalIntI];
                                System.arraycopy(arrayList.get(finalIntI),0,stringArrayMitHeader,1,arrayList.get(finalIntI).length);
                            }
                            bundle.putStringArray(getString(R.string.key_string_array_list_to_parse_to_radio_group_mit_header),stringArrayMitHeader);
                            Log.v("header",displayTextsInOnCreate[finalIntI]);
                            StringBuilder stringBuilder = new StringBuilder();
                            for (String string:arrayList.get(finalIntI)){
                                stringBuilder.append(string).append("\n");
                            }
                            Log.v("body",stringBuilder.toString());

                            stringBuilder = new StringBuilder();
                            for (String string:stringArrayMitHeader){
                                stringBuilder.append(string).append("\n");
                            }
                            Log.v("stringArrayMitHeader",stringBuilder.toString());
                            fragment.setArguments(bundle);

                            fragment.show(getFragmentManager(),"radioauswahlMitHeader");

                        }
                    });
                    layout.addView(button);
                }
                else {
                    //TextView textView = new TextView(getDialog().getContext());
                    //textView.setText(displayTextsInOnCreate[i]);
                    //layout.addView(textView);
                    eingabeTexts[i]=displayTextsInOnCreate[i];
                }
            }
            else if(anzeigeViewObjectID[i]==1){             //integereingabe
                final EditText integerEingabe = new EditText(getDialog().getContext());
                eingabeTexts[i]="";
                integerEingabe.setInputType(InputType.TYPE_CLASS_NUMBER);
                integerEingabe.setHint(displayTextsInOnCreate[i]);
                integerEingabe.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        eingabeTexts[finalIntI]=charSequence.toString();
                        anzeigeAktualisieren();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });
                //check if es das letzte element is dann wäre weiter gleich senden
                boolean letztesElement = true;
                for(int j = i+1; j < displayTextsInOnCreate.length;j++){
                    if(anzeigeViewObjectID[j]!=0){
                        letztesElement = false;
                    }
                    if(arrayList.get(j).length>1){
                        letztesElement = false;
                    }
                }
                if(letztesElement){
                    integerEingabe.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                            sendText();
                            dismiss();
                            return false;
                        }
                    });
                }

                layout.addView(integerEingabe);

                if(displayTextsInOnCreate[i].equals(getString(R.string.heim_team_tore_key))){
                    heimToreEdittext = integerEingabe;
                }
                if(displayTextsInOnCreate[i].equals(getString(R.string.auswarts_team_tore_key))){
                    auswartsToreEdittext = integerEingabe;
                }
            }
            else if (anzeigeViewObjectID[i]==2) {        //checkbox}
                Log.w("ERROR","anzeigeViewObjectID 2 ist veraltet\nnutze anzeigeobjectid 20 für unchecked checkbox und 21 für checked checkbox\nerror in "+shortdescription[i]+"   "+displayTextsInOnCreate[i]);

            }
            else if (anzeigeViewObjectID[i]==20||anzeigeViewObjectID[i]==21){        //checkbox
                CheckBox checkBox = new CheckBox(getDialog().getContext());
                checkBox.setText(shortdescription[i]);
                eingabeTexts[i] = "";
                if(anzeigeViewObjectID[i]==21){
                    checkBox.setChecked(true);
                    eingabeTexts[i] = displayTextsInOnCreate[i];
                }
                final int finalIntIinCheckbox = i;
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            eingabeTexts[finalIntIinCheckbox] = displayTextsInOnCreate[finalIntIinCheckbox];
                        }
                        else {
                            eingabeTexts[finalIntIinCheckbox] = "";
                        }
                        anzeigeAktualisieren();
                    }
                });
                layout.addView(checkBox);
            }
            else if (anzeigeViewObjectID[i]==3){        //leerzeile
                layout.addView(new TextView(getDialog().getContext()));
            }
            else if (anzeigeViewObjectID[i]==4) {        //open keyboard
                //TODO
                /*
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                eingabeTextFeldStandardLayout.requestFocus();
                assert imm != null;
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_HIDDEN);*/
            }
            else if (anzeigeViewObjectID[i]==5) {        //Radiogroup (immer geöffnet)
                eingabeTexts[i]="";     //sonst null dann kanns mit den dependencies schwierigkeiten geben
                RadioGroup radioGroup = new RadioGroup(getDialog().getContext());
                for(final String string:arrayList.get(i)){
                    RadioButton radioButton = new RadioButton(getDialog().getContext());
                    radioButton.setText(string);
                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            //check if Tor box um tor 1 hoch zu zählen
                            boolean torgefallen = (displayTexts[finalIntI].equals("torteam"));
                            toreHabenSichVeraendert = torgefallen;
                            int increment;
                            if(b){
                                increment = 1;
                                eingabeTexts[finalIntI]=string;
                            }
                            else{
                                increment = -1;
                            }
                            if(torgefallen){
                                if(string.equals(((tsvNeuensorgTicker)getActivity()).getHeimMannschaftsString())){
                                    neueHeimTore = neueHeimTore+increment;
                                }
                                else if(string.equals(((tsvNeuensorgTicker)getActivity()).getAuswartsMannschaftsString())){
                                    neueAuswartsTore = neueAuswartsTore+increment;
                                }
                                else{
                                    Log.e("error","in standarddialogfragment tor erkannt aber teams nicht erkannt");
                                }
                            }
                            toreAnzeigen();
                            anzeigeAktualisieren();
                        }
                    });
                    radioGroup.addView(radioButton);
                }
                layout.addView(radioGroup);
            }
        }


        Button sendenButton = new Button(getDialog().getContext());
        sendenButton.setText(getString(R.string.title_senden_key));
        sendenButton.setAllCaps(false);
        sendenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendText();
                dismiss();
            }
        });
        layout.addView(sendenButton);

        view.findViewById(R.id.imageButtonSenden).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendText();
                dismiss();
            }
        });


        CheckBox zeitAnzeigenCheckbox = view.findViewById(R.id.standardAktioncheckBoxZeitInEingabe);
        zeitAnzeigenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                zeitAnzeigen = b;
                anzeigeAktualisieren(view);
            }
        });


        Dialog dialog = getDialog();
        if(dialog!=null){
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        }

        displayTexts = displayTextsInOnCreate;


        thisView = view;

        toreLaden();
        anzeigeAktualisieren();

        return view;
    }

    @Override
    public void onStop(){
        ((tsvNeuensorgTicker)getActivity()).hideKeyboard();
        super.onStop();

    }

    void sendText(){
        EditText editText = thisView.findViewById(R.id.eingabeTextFeldStandardLayout);
        if(toreHabenSichVeraendert){
            ((tsvNeuensorgTicker)getActivity()).setHeimTore(neueHeimTore);
            ((tsvNeuensorgTicker)getActivity()).setAuswartsTore(neueAuswartsTore);
        }
        ((tsvNeuensorgTicker)getActivity()).eingabeSpeichern(editText.getText().toString());

        if(((tsvNeuensorgTicker)getActivity()).getBooleanPref(getString(R.string.key_auto_send_to_whatsapp),true)){
            sendToWhatsapp(editText.getText().toString());
        }
    }

    void openWhatsappGroup(String groupUrl){
        ((tsvNeuensorgTicker)getActivity()).openWhatsappGroup(groupUrl);
    }

    void sendToWhatsapp(String text){
        ((tsvNeuensorgTicker)getActivity()).sendToWhatsapp(text);
    }


    String[] displayTexts;
    String[] eingabeTexts;
    String[] dependencys;
    String[] antidependencys;
    String timeAnzeige;
    boolean zeitAnzeigen = true;

    public static standardDialogFragment getInstance(){
        return instance;
    }

    boolean getTrueWennStringGleich1(String string){
        return Integer.parseInt(string)==1;
    }

    void setEingabeText(String eingabe, String displayTextDerEditiertenEingabe){
        boolean stopLoop = false;
        for(int i = 0;i<displayTexts.length&&!stopLoop;i++){
            if(displayTexts[i].equals(displayTextDerEditiertenEingabe)){
                eingabeTexts[i] = eingabe;
                stopLoop = true;
            }
        }
        if(!stopLoop){
            Log.w("stdDialogFragmetn","setEingabeText stopLoog false\ndisplaytext "+displayTextDerEditiertenEingabe+"nicht gefunden");
        }
        anzeigeAktualisieren();
    }

    EditText heimToreEdittext, auswartsToreEdittext;

    void anzeigeAktualisieren(View view){
        StringBuilder stringBuilder = new StringBuilder();
        if(zeitAnzeigen){
            stringBuilder.append(timeAnzeige).append("\n");
        }
        EditText editText = view.findViewById(R.id.eingabeTextFeldStandardLayout);
        for (int i = 0; i<eingabeTexts.length;i++){
            String[] dependenciesArray = dependencys[i].split(",");
            String[] antidependenciesArray = antidependencys[i].split(",");
            Integer[] dependenciesIntArray = new Integer[dependenciesArray.length];
            Integer[] antidependenciesIntArray = new Integer[antidependenciesArray.length];
            for (int j = 0; j<dependenciesArray.length;j++){
                dependenciesIntArray[j]=Integer.valueOf(dependenciesArray[j]);
            }
            for (int j = 0; j<antidependenciesArray.length;j++){
                antidependenciesIntArray[j]=Integer.valueOf(antidependenciesArray[j]);
            }
            if(eingabeTexts[i]!=null){
                if(!eingabeTexts[i].equals("")){
                    boolean dependenciesOk = true;
                    boolean antidependenciesOk = true;
                    if(!dependenciesArray[0].equals("0")){
                        for(int dependency:dependenciesIntArray){
                            if(eingabeTexts[i+dependency]!=null){
                                if(eingabeTexts[i+dependency].length()==0){
                                    dependenciesOk = false;
                                }
                            }
                            else {
                                Log.w("error","checking dependencies\neingabetext["+i+" + "+dependency+"] wahrscheinlich null");
                                dependenciesOk = false;
                            }
                        }
                    }
                    if(!antidependenciesArray[0].equals("0")){
                        for(int anitdependency:antidependenciesIntArray){
                            if(eingabeTexts[i+anitdependency]!=null) {
                                if (eingabeTexts[i + anitdependency].length() != 0) {
                                    antidependenciesOk = false;
                                }
                            }
                        }
                    }
                    if(dependenciesOk&&antidependenciesOk){
                        stringBuilder.append(eingabeTexts[i]);
                    }
                }
            }
        }
        editText.setText(stringBuilder.toString());
        editText.setSelection(editText.getText().length());
    }

    int neueHeimTore = 0;
    int neueAuswartsTore = 0;
    boolean toreHabenSichVeraendert = false;
    void toreLaden(){
        if(heimToreEdittext!=null){
            neueHeimTore = ((tsvNeuensorgTicker)getActivity()).getHeimTore();
            heimToreEdittext.setText(String.valueOf(neueHeimTore));
        }
        if(auswartsToreEdittext!=null){
            neueAuswartsTore = ((tsvNeuensorgTicker)getActivity()).getAuswartsTore();
            auswartsToreEdittext.setText(String.valueOf(neueAuswartsTore));
        }
    }
    void toreAnzeigen(){
        if(heimToreEdittext!=null){
            heimToreEdittext.setText(String.valueOf(neueHeimTore));
        }
        if(auswartsToreEdittext!=null){
            auswartsToreEdittext.setText(String.valueOf(neueAuswartsTore));
        }
    }
    void anzeigeAktualisieren(){
        anzeigeAktualisieren(thisView);
    }

    String StringArrayToString(String[] stringArray){
        StringBuilder stringBuilder = new StringBuilder();
        for(String string:stringArray){
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }

}
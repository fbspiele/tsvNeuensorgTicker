package fbspiele.tsvneuensorgticker;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class timeAktualisierenDialogFragment extends DialogFragment {
   /* public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.content_time_aktualisieren,null));

        return builder.create();
    }
*/
    EditText editText;
    Button button;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.content_time_aktualisieren,container,false);        //FINAL!!!!!
        editText = view.findViewById(R.id.eingabeTextFeldTimeAktualisieren);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean dismissOnEnter = true;
                CharSequence txt = textView.getText();
                String text = txt.toString();
                String[] splitText = text.split(":");
                if(splitText.length>2||splitText.length<1){
                    textView.setText("");
                    textView.setHint("nicht das richtige format\ndu muss xx:xx min:sec oder x min eingeben");
                    return false;
                }
                Long secs_millis = 0L;
                Long mins_millis;
                try{
                    mins_millis = Long.valueOf(splitText[0]);
                    if(splitText.length==2){
                        secs_millis = Long.valueOf(splitText[1]);
                    }
                }
                catch (NumberFormatException e){
                    textView.setHint("nicht das richtige format\ndu muss xx:xx min:sec oder x min eingeben");
                    e.printStackTrace();
                    return false;
                }
                mins_millis = mins_millis * 60 * 1000;
                secs_millis = secs_millis * 1000;
                Long ges_millis = mins_millis + secs_millis;
                ((tsvNeuensorgTicker)getActivity()).millisHZgestartet = System.currentTimeMillis() - ges_millis;
                ((tsvNeuensorgTicker)getActivity()).saveSettings();
                dismiss();
                return false;
            }
        });
        button = view.findViewById(R.id.buttonHalbzeitbeginnJetzt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((tsvNeuensorgTicker)getActivity()).millisHZgestartet = System.currentTimeMillis();
                ((tsvNeuensorgTicker)getActivity()).saveSettings();
                dismiss();
            }
        });

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupHalbzeitWechseln);
        switch(((tsvNeuensorgTicker)getActivity()).halbZeit){
            case 1:
                radioGroup.check(R.id.radioButton1Halbzeit);
                break;
            case 2:
                radioGroup.check(R.id.radioButton2Halbzeit);
                break;
            default:
                radioGroup.check(R.id.radioButton1Halbzeit);
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int halbZeit;
                switch (i){
                    case R.id.radioButton1Halbzeit:
                        halbZeit=1;
                        break;
                    case R.id.radioButton2Halbzeit:
                        halbZeit=2;
                        break;
                    default:
                        halbZeit=1;
                        break;
                }
                ((tsvNeuensorgTicker)getActivity()).halbzeitAktualisieren(halbZeit);
            }
        });
        return view;
    }


}

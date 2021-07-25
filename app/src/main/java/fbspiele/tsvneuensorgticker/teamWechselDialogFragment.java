package fbspiele.tsvneuensorgticker;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class teamWechselDialogFragment extends DialogFragment {
   /* public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.content_time_aktualisieren,null));

        return builder.create();
    }
*/
    Button button;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.content_team_wechsel,container,false);        //FINAL!!!!!

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupTeamWechseln);
        switch(((tsvNeuensorgTicker)getActivity()).team){
            case 1:
                radioGroup.check(R.id.radioButton1Mannschaft);
                break;
            case 2:
                radioGroup.check(R.id.radioButton2Mannschaft);
                break;
            default:
                radioGroup.check(R.id.radioButton1Mannschaft);
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int team;
                switch (i){
                    case R.id.radioButton1Mannschaft:
                        team=1;
                        break;
                    case R.id.radioButton2Mannschaft:
                        team=2;
                        break;
                    default:
                        team=1;
                        break;
                }
                ((tsvNeuensorgTicker)getActivity()).mannschaftAktualisieren(team);
                ((tsvNeuensorgTicker)getActivity()).anzeigeAktualisieren();
                dismiss();
            }
        });
        return view;
    }



}

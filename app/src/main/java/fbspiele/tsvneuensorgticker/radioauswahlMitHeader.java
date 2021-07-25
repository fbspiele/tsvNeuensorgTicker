package fbspiele.tsvneuensorgticker;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Arrays;

public class radioauswahlMitHeader extends DialogFragment {
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        int auswahlTextSize = 20;

        final View view = inflater.inflate(R.layout.radioauswahl_mit_header,container,false);        //FINAL!!!!!
        final String[] stringArrayMitHeader = getArguments().getStringArray(getString(R.string.key_string_array_list_to_parse_to_radio_group_mit_header));

        TextView textView = view.findViewById(R.id.textViewHeader);
        if(stringArrayMitHeader != null){
            textView.setText(stringArrayMitHeader[0]);
            RadioGroup radioGroup = view.findViewById(R.id.radioMitHeaderLayoutRadioGroup);
            RadioButton radioButton = new RadioButton(getDialog().getContext());
            radioButton.setText(getString(R.string.displaytext_doch_nichts_auswaehlen));
            radioButton.setTextSize(auswahlTextSize);
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        standardDialogFragment.getInstance().setEingabeText("",stringArrayMitHeader[0]);
                    }
                }
            });
            radioGroup.addView(radioButton);
            for (int i = 1; i<stringArrayMitHeader.length;i++){
                RadioButton radioButtonInForLoop = new RadioButton(getDialog().getContext());
                radioButtonInForLoop.setText(stringArrayMitHeader[i]);
                radioButtonInForLoop.setTextSize(auswahlTextSize);
                final int finalIntI = i;
                radioButtonInForLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            standardDialogFragment.getInstance().setEingabeText(stringArrayMitHeader[finalIntI],stringArrayMitHeader[0]);

                            dismiss();
                        }
                    }
                });
                radioGroup.addView(radioButtonInForLoop);
            }
        }

        return view;
    }
}
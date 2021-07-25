package fbspiele.tsvneuensorgticker;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class toreAktualisierenDialogFragment extends DialogFragment {
    Button button;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.content_tore_aktualisieren,container,false);        //FINAL!!!!!

        final EditText heimToreEdittext = view.findViewById(R.id.heimToreEingabe);
        heimToreEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((tsvNeuensorgTicker)getActivity()).setHeimTore(Integer.valueOf(heimToreEdittext.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final EditText auswartsToreEdittext = view.findViewById(R.id.auswartsToreEingabe);
        auswartsToreEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((tsvNeuensorgTicker)getActivity()).setAuswartsTore(Integer.valueOf(auswartsToreEdittext.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return view;
    }



}

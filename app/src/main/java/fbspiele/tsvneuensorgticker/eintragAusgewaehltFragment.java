package fbspiele.tsvneuensorgticker;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class eintragAusgewaehltFragment extends DialogFragment {

    Activity activity;
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = activity;
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.eintrag_ausgewaehlt_layout,container,false);        //FINAL!!!!!


        final EditText editText = view.findViewById(R.id.editTextEintrag);
        final String eintragsText = getArguments().getString(getString(R.string.eintrag_text_key_in_bundle));
        editText.setText(eintragsText);

        final int eintragsIndex = getArguments().getInt(getString(R.string.eintrag_index_key_in_bundle));
        view.findViewById(R.id.imageButtonSendenAusEintrag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((tsvNeuensorgTicker)getActivity()).sendToWhatsapp(editText.getText().toString());
                dismiss();
            }
        });

        view.findViewById(R.id.imageButtonEintragLoeschen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((tsvNeuensorgTicker)getActivity()).deleteEintragContext(eintragsIndex);
                dismiss();
            }
        });

        return view;

    }
}

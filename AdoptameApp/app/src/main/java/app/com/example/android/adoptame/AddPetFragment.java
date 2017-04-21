package app.com.example.android.adoptame;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class AddPetFragment extends PetFormFragment {

    public AddPetFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void enviaDatos() {
        Toast.makeText(getActivity().getApplicationContext(), "Añadiendo...", Toast.LENGTH_LONG).show();
        new UploadData(ADD_CODE, null).execute();
    }

}

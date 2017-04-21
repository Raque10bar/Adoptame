package app.com.example.android.adoptame;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import app.com.example.android.adoptame.adapter.PetAdapter;
import app.com.example.android.adoptame.connection.FetchPetsTask;
import app.com.example.android.adoptame.data.PetContract;


public class PetListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;

    private static final String[] PET_COLUMNS = {
            PetContract.PetsEntry._ID,
            PetContract.PetsEntry.COLUMN_PICTURE,
            PetContract.PetsEntry.COLUMN_NAME,
            PetContract.PetsEntry.COLUMN_GENDER
    };

    private PetAdapter mPetAdapter;

    public PetListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        mPetAdapter = new PetAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_petlist, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.fragment_gridview);
        gridView.setAdapter(mPetAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), PetDetail.class)
                            .setData(PetContract.PetsEntry.buildPetIdUri(cursor.getInt(PetDetailFragment.COL_PET_ID)));
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PET_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
        updatePets();
    }

    public void updatePets(){
        FetchPetsTask petsTask = new FetchPetsTask(getActivity());
        petsTask.execute();
    }

    public void changeLoader(Bundle bundle) {
        Log.d("LISTA", "Cambia loader");
        getLoaderManager().restartLoader(PET_LOADER,bundle,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d("LISTA", "Crea loader");
        Uri petsUri;

        if (bundle != null) {
            petsUri = Uri.parse (bundle.getString("uri"));
        } else {
            petsUri = PetContract.PetsEntry.CONTENT_URI;
        }

        return new CursorLoader(
                    getActivity(),
                    petsUri,
                    PET_COLUMNS,
                    null,
                    null,
                PetContract.PetsEntry._ID + " DESC"
            );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d("LISTA", "Acaba loader");
        mPetAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d("LISTA", "Restart loader");
        mPetAdapter.swapCursor(null);
    }

}
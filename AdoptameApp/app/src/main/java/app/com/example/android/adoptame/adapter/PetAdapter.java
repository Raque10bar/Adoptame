package app.com.example.android.adoptame.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.io.File;

import app.com.example.android.adoptame.PetDetailFragment;
import app.com.example.android.adoptame.R;
import app.com.example.android.adoptame.connection.ConnectionHandler;

/**
 * Clase que adapta la vista de imagen y texto de las mascotas de la vista principal
 */
public class PetAdapter extends CursorAdapter {

    public PetAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private String[] convertCursorRowToUXFormat(Cursor cursor) {

        String[] petData = new String[4];

            petData[0] = cursor.getString(PetDetailFragment.COL_PET_ID);
            petData[1] = ConnectionHandler.PICTURE_DIR + "/" + ConnectionHandler.PICTURE_PREFIX + cursor.getString(PetDetailFragment.COL_PET_PICTURE);
            petData[2] = cursor.getString(PetDetailFragment.COL_PET_NAME);
            if (petData[2].equals("null")) {
                petData[2] = "???";
            }
            petData[3] = cursor.getString(PetDetailFragment.COL_PET_GENDER);

        return petData;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_pet, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String[] petData = convertCursorRowToUXFormat(cursor);

        ImageView imageView = (ImageView) view.findViewById(R.id.grid_item_pet_imageview);
        TextView textViewName = (TextView) view.findViewById(R.id.grid_item_petname_textview);
        TextView textViewSex = (TextView) view.findViewById(R.id.grid_item_petsex_textview);

        Uri uri = Uri.fromFile(new File(petData[1]));

        Glide.with(mContext).load(uri).signature(new StringSignature(String.valueOf(System.currentTimeMillis()))).into(imageView);

        textViewName.setText(petData[2]);

        if (petData[3].equalsIgnoreCase("macho")) {
            textViewSex.setText("♂");
            textViewSex.setTextColor(mContext.getResources().getColor(R.color.blue));
        } else {
            textViewSex.setText("♀");
            textViewSex.setTextColor(mContext.getResources().getColor(R.color.pink));
        }

    }
}

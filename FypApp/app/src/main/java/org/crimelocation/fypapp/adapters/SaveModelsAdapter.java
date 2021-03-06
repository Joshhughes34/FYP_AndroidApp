package org.crimelocation.fypapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.crimelocation.fypapp.R;

import org.crimelocation.fypapp.activities.SavedRequestsActivity;
import org.crimelocation.fypapp.models.SaveModel;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by joshuahughes on 23/03/2016.
 */
public class SaveModelsAdapter extends ArrayAdapter<SaveModel> {

    private final Context context;
    private ArrayList<SaveModel> saveModelArray;

    public SaveModelsAdapter(Context c, ArrayList<SaveModel> array){
        super(c, -1, array);
        this.context = c;
        this.saveModelArray = array;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflator = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflator.inflate(R.layout.list_row_save_models, parent, false);

        final SaveModel model = saveModelArray.get(position);

        TextView nameView = (TextView) rowView.findViewById(R.id.list_row_save_model_name);
        nameView.setText(model.Name);

        TextView dateView = (TextView) rowView.findViewById(R.id.list_row_save_model_date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.UK);
        dateView.setText(simpleDateFormat.format(model.SaveDate));

        TextView crimeTypeView = (TextView) rowView.findViewById(R.id.list_row_save_model_crime_type);
        crimeTypeView.setText(model.CrimeLocationTypeModel.Name);

        TextView crimesView = (TextView) rowView.findViewById(R.id.list_row_save_model_crimes);
        crimesView.setText(Integer.toString(model.CrimeLocationsRequestModel.CrimeLocations.size()) + " crime(s)");

        ImageButton button = (ImageButton) rowView.findViewById(R.id.list_row_save_model_delete_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SaveModelAdapter", "Delete button click");

                AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(getContext());
                confirmationDialog.setTitle("Warning!");
                confirmationDialog.setMessage("Are you sure you want to delete saved Request?");
                confirmationDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveModelArray.remove(position); //or some other task
                        notifyDataSetChanged();
                        ((SavedRequestsActivity)context).UpdateSavedModelsStorage();
                        dialog.dismiss();
                    }
                });
                confirmationDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                confirmationDialog.show();

            }
        });

        return rowView;
    }

}

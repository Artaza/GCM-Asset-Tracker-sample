package com.artazaaziz.android.client.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.artazaaziz.android.client.R;
import com.artazaaziz.android.client.util.Preferences;

public class ChooseAssetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_asset);
        setTitle("Choose your asset name");

        Spinner chooseAssetSpinner = (Spinner) findViewById(R.id.choose_asset);
        SpinnerAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item,
                Preferences.TOPICS);

        chooseAssetSpinner.setAdapter(adapter);

        chooseAssetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Preferences.TO = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Preferences.TO = Preferences.TOPICS[0];
            }
        });


    }

    public void btnClick(View view) {
        startActivity(new Intent(ChooseAssetActivity.this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}

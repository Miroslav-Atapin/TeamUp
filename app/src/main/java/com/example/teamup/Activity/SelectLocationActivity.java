package com.example.teamup.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamup.R;

public class SelectLocationActivity extends AppCompatActivity {

    private RadioGroup radioGroupCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        radioGroupCities = findViewById(R.id.radioGroupCities);

        imgbtnArrow.setOnClickListener(v -> finish());
        tvTitleHeader.setText(getResources().getString(R.string.title_location));

        Button btnSelectLocation = findViewById(R.id.btnSelectLocation);
        btnSelectLocation.setOnClickListener(v -> handleCitySelection());
    }

    private void handleCitySelection() {
        int checkedRadioButtonId = radioGroupCities.getCheckedRadioButtonId();
        if (checkedRadioButtonId != -1) {
            RadioButton selectedRadioButton = findViewById(checkedRadioButtonId);
            String selectedCity = selectedRadioButton.getText().toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("SELECTED_CITY_KEY", selectedCity);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Выберите город", Toast.LENGTH_SHORT).show();
        }
    }
}
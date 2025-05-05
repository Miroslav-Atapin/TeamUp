package com.example.teamup;

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

public class SelectCategoryActivity extends AppCompatActivity {

    private RadioGroup radioGroupCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        radioGroupCategory = findViewById(R.id.radioGroupCategory);

        imgbtnArrow.setOnClickListener(view -> finish());
        tvTitleHeader.setText("Местоположение");

        // Обрабатываем клик на кнопку подтверждения выбора города
        Button btnSelectCategory = findViewById(R.id.btnSelectCategory);
        btnSelectCategory.setOnClickListener(view -> handleCategorySelection());
    }

    private void handleCategorySelection() {
        int checkedRadioButtonId = radioGroupCategory.getCheckedRadioButtonId();
        if (checkedRadioButtonId != -1) {
            RadioButton selectedRadioButton = findViewById(checkedRadioButtonId);
            String selectedCity = selectedRadioButton.getText().toString();

            // Создаем объект Intent с результатом и возвращаем выбранный город
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SELECTED_CATEGORY_KEY", selectedCity);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            // Если ни один город не выбран, выводим сообщение
            Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show();
        }
    }
}
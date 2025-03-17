package com.example.teamup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        EditText edFirstName = findViewById(R.id.edFirstName);
        EditText edLastName = findViewById(R.id.edLastName);
        Button btnCreateProfile = findViewById(R.id.btnCreateProfile);
        ImageButton imgbtnAvtar = findViewById(R.id.imgbtnAvatar);

        imgbtnArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterProfileActivity.this, RegisterActivity.class));
            }
        });

        tvTitleHeader.setText("Создать аккаунт");

        btnCreateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = edFirstName.getText().toString();
                String lastName = edLastName.getText().toString();

                boolean hasErrors = false;

                if (firstName.isEmpty() || lastName.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Заполните, пожалуйста, все поля.", Toast.LENGTH_LONG).show();
                }

                if (!hasErrors) {
                    HashMap<String, Object> userInfo = new HashMap<>();
                    userInfo.put("FirstName", firstName);
                    userInfo.put("Lastname", lastName);

                    FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(userInfo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Профиль успешно создан!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                }

            }
        });



    }
}
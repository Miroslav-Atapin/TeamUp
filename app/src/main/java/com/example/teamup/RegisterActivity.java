package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        TextView tvGoToLogin= findViewById(R.id.tvGoToLogin);
        Button btnGoToRegisterProfile = findViewById(R.id.btnGoToRegisterProfile);
        EditText edEmail = findViewById(R.id.edEmail);
        EditText edPassword = findViewById(R.id.edPassword);
        TextView tvErrorEmail = findViewById(R.id.tvErrorEmail);
        TextView tvErrorPassword = findViewById(R.id.tvErrorPassword);

        imgbtnArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, StartActivity.class));
            }
        });

        tvTitleHeader.setText("Создать аккаунт");

        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        btnGoToRegisterProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edEmail.getText().toString();
                String password = edPassword.getText().toString();

                tvErrorEmail.setText("");
                tvErrorPassword.setText("");

                boolean hasErrors = false;

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Заполните, пожалуйста, все поля.", Toast.LENGTH_LONG).show();
                    hasErrors = true;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tvErrorEmail.setText("Проверьте правильность email.");
                    hasErrors = true;
                }

                if (password.length() < 8) {
                    tvErrorPassword.setText("Пароль должен содержать минимум 8 символов.");
                    hasErrors = true;
                }

                if (!hasErrors) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        HashMap<String, String> userInfo = new HashMap<>();
                                        userInfo.put("email", email);
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userInfo);
                                        startActivity(new Intent(RegisterActivity.this, RegisterProfileActivity.class));
                                    }
                                }
                            });
                }
            }
        });

    }
}
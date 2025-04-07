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
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        Button btnCreateProfile = findViewById(R.id.btnCreateProfile);
        EditText edEmail = findViewById(R.id.edEmail);
        EditText edPassword = findViewById(R.id.edPassword);
        TextView tvRegisterError = findViewById(R.id.tvRegisterError);
        EditText edFirstName = findViewById(R.id.edFirstName);
        EditText edLastName = findViewById(R.id.edLastName);

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

        btnCreateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edEmail.getText().toString();
                String password = edPassword.getText().toString();
                String firstName = edFirstName.getText().toString();
                String lastName = edLastName.getText().toString();

                boolean hasErrors = false;

                if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    hasErrors = true;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    hasErrors = true;
                }

                if (password.length() < 8) {
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
                                        userInfo.put("FirstName", firstName);
                                        userInfo.put("LastName", lastName);

                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userInfo);

                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    } else {
                                        tvRegisterError.setText("Ошибка регистрации. Попробуйте снова.");
                                    }
                                }
                            });
                } else {
                    tvRegisterError.setText("Заполните, пожалуйста, все поля и проверьте правильность введенных данных.");
                }
            }
        });
    }
}
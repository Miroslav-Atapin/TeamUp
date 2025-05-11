package com.example.teamup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword, tilFirstName, tilLastName;
    private TextInputEditText edEmail, edPassword, edFirstName, edLastName;
    private Button btnCreateProfile;
    private TextView tvGoToLogin;

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

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        edFirstName = findViewById(R.id.edFirstName);
        edLastName = findViewById(R.id.edLastName);
        btnCreateProfile = findViewById(R.id.btnCreateProfile);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvTitleHeader.setText("Создать аккаунт");

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        imgbtnArrow.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, StartActivity.class)));

        tvGoToLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        btnCreateProfile.setOnClickListener(v -> registerNewUser());
    }

    private void registerNewUser() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String firstName = edFirstName.getText().toString().trim();
        String lastName = edLastName.getText().toString().trim();

        boolean hasErrors = false;

        if (email.isEmpty()) {
            tilEmail.setError("Это поле обязательно для заполнения");
            hasErrors = true;
        } else if (!isValidEmail(email)) {
            tilEmail.setError("Неверный адрес электронной почты");
            hasErrors = true;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Это поле обязательно для заполнения");
            hasErrors = true;
        } else if (password.length() < 8) {
            tilPassword.setError("Пароль должен содержать минимум 8 символов");
            hasErrors = true;
        } else {
            tilPassword.setError(null);
        }

        if (firstName.isEmpty()) {
            tilFirstName.setError("Это поле обязательно для заполнения");
            hasErrors = true;
        } else {
            tilFirstName.setError(null);
        }

        if (lastName.isEmpty()) {
            tilLastName.setError("Это поле обязательно для заполнения");
            hasErrors = true;
        } else {
            tilLastName.setError(null);
        }

        if (!hasErrors) {
            createFirebaseUser(email, password, firstName, lastName);
        }
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void createFirebaseUser(String email, String password, String firstName, String lastName) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("FirstName", firstName);
                        userData.put("LastName", lastName);

                        usersRef.child(uid).setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("REGISTER", "Пользователь зарегистрирован и информация сохранена");
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("REGISTER", "Ошибка сохранения данных пользователя: " + e.getMessage());
                                    Toast.makeText(RegisterActivity.this, "Ошибка регистрации. Попробуйте позже.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("REGISTER", "Ошибка регистрации: " + task.getException().getMessage());
                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации. Проверьте данные и попробуйте ещё раз.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
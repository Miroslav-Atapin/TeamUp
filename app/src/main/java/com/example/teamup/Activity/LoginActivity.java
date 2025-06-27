package com.example.teamup.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teamup.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText edEmail, edPassword;
    private Button btnLoginAccount;
    private TextView tvGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btnLoginAccount = findViewById(R.id.btnLoginAccount);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        imgbtnArrow.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, StartActivity.class)));

        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvTitleHeader.setText(getResources().getString(R.string.title_loginAccount));

        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        btnLoginAccount.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();

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

        if (!hasErrors) {
            authenticateUser(email, password);
        }
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private void authenticateUser(String email, String password) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Ошибка входа: неверный пароль или электронная почта",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
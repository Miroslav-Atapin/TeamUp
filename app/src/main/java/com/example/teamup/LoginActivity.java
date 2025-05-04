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

        // Инициализация элементов интерфейса
        tilEmail = findViewById(R.id.tilEmail);       // Обертка поля Email
        tilPassword = findViewById(R.id.tilPassword); // Обертка поля Password
        edEmail = findViewById(R.id.edEmail);         // Поле Email
        edPassword = findViewById(R.id.edPassword);   // Поле Password
        btnLoginAccount = findViewById(R.id.btnLoginAccount); // Кнопка авторизации
        tvGoToRegister = findViewById(R.id.tvGoToRegister);   // Переход к регистрации

        // Перейти к стартовому экрану
        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        imgbtnArrow.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, StartActivity.class)));

        // Заголовок экрана
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvTitleHeader.setText("Войти в аккаунт");

        // Открыть форму регистрации
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Авторизация
        btnLoginAccount.setOnClickListener(view -> performLogin());
    }

    /**
     * Выполняем проверку полей формы и авторизацию пользователя
     */
    private void performLogin() {
        final String email = edEmail.getText().toString().trim();      // Получаем email
        final String password = edPassword.getText().toString().trim(); // Получаем пароль

        boolean hasErrors = false;

        // Проверяем поле email
        if (email.isEmpty()) {
            tilEmail.setError("Это поле обязательно для заполнения"); // Ошибка пустого поля
            hasErrors = true;
        } else if (!isValidEmail(email)) {                             // Неправильно указанный email
            tilEmail.setError("Неверный адрес электронной почты");
            hasErrors = true;
        } else {
            tilEmail.setError(null);                                  // Убираем ошибку, если данные верны
        }

        // Проверяем поле пароля
        if (password.isEmpty()) {
            tilPassword.setError("Это поле обязательно для заполнения"); // Пустое поле
            hasErrors = true;
        } else if (password.length() < 8) {                           // Короткий пароль
            tilPassword.setError("Пароль должен содержать минимум 8 символов");
            hasErrors = true;
        } else {
            tilPassword.setError(null);                               // Убираем ошибку, если данные верны
        }

        // Если ошибок нет — отправляем запрос на сервер
        if (!hasErrors) {
            authenticateUser(email, password);
        }
    }

    /**
     * Метод проверки правильности адреса электронной почты
     *
     * @param email Адрес электронной почты
     * @return true, если адрес валидный
     */
    private boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    /**
     * Запуск процесса аутентификации пользователя
     *
     * @param email    Электронная почта
     * @param password Пароль
     */
    private void authenticateUser(final String email, final String password) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Пользователь успешно вошел
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Аутентификация не удалась
                        Toast.makeText(getApplicationContext(),
                                "Ошибка входа: неверный пароль или электронная почта",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
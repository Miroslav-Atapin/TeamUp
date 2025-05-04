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

        // Инициализируем элементы UI
        tilEmail = findViewById(R.id.tilEmail);          // Обёртка поля Email
        tilPassword = findViewById(R.id.tilPassword);    // Обёртка поля Password
        tilFirstName = findViewById(R.id.tilFirstName);  // Обёртка поля First Name
        tilLastName = findViewById(R.id.tilLastName);    // Обёртка поля Last Name
        edEmail = findViewById(R.id.edEmail);            // Поле Email
        edPassword = findViewById(R.id.edPassword);      // Поле Password
        edFirstName = findViewById(R.id.edFirstName);    // Поле First Name
        edLastName = findViewById(R.id.edLastName);      // Поле Last Name
        btnCreateProfile = findViewById(R.id.btnCreateProfile); // Кнопка регистрации
        tvGoToLogin = findViewById(R.id.tvGoToLogin);    // Переход к форме входа

        // Настройка заголовка экрана
        TextView tvTitleHeader = findViewById(R.id.tvTitleHeader);
        tvTitleHeader.setText("Создать аккаунт");

        // Переход назад к начальной странице
        ImageButton imgbtnArrow = findViewById(R.id.imgbtnArrowHeader);
        imgbtnArrow.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, StartActivity.class)));

        // Переход к форме входа
        tvGoToLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        // Регистрация нового пользователя
        btnCreateProfile.setOnClickListener(view -> registerNewUser());
    }

    /**
     * Выполнение регистрации пользователя
     */
    private void registerNewUser() {
        final String email = edEmail.getText().toString().trim();      // Получаем email
        final String password = edPassword.getText().toString().trim(); // Получаем пароль
        final String firstName = edFirstName.getText().toString().trim(); // Имя
        final String lastName = edLastName.getText().toString().trim(); // Фамилия

        boolean hasErrors = false;

        // Проверка заполненности всех полей
        if (email.isEmpty()) {
            tilEmail.setError("Это поле обязательно для заполнения");
            hasErrors = true;
        } else if (!isValidEmail(email)) {
            tilEmail.setError("Неверный адрес электронной почты");
            hasErrors = true;
        } else {
            tilEmail.setError(null); // Скрываем ошибку, если поле верно
        }

        // Проверка пароля
        if (password.isEmpty()) {
            tilPassword.setError("Это поле обязательно для заполнения");
            hasErrors = true;
        } else if (password.length() < 8) {
            tilPassword.setError("Пароль должен содержать минимум 8 символов");
            hasErrors = true;
        } else {
            tilPassword.setError(null); // Скрываем ошибку, если поле верно
        }

        // Проверка имени
        if (firstName.isEmpty()) {
            tilFirstName.setError("Это поле обязательно для заполнения");
            hasErrors = true;
        } else {
            tilFirstName.setError(null); // Скрываем ошибку, если поле верно
        }

        // Проверка фамилии
        if (lastName.isEmpty()) {
            tilLastName.setError("Это поле обязательно для заполнения");
            hasErrors = true;
        } else {
            tilLastName.setError(null); // Скрываем ошибку, если поле верно
        }

        // Если ошибок нет — регистрируем пользователя
        if (!hasErrors) {
            createFirebaseUser(email, password, firstName, lastName);
        }
    }

    /**
     * Метод проверки корректности электронного адреса
     *
     * @param email электронный адрес
     * @return true, если адрес правильный
     */
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Создание аккаунта пользователя в Firebase Auth и запись данных в базу данных
     *
     * @param email     электронный адрес
     * @param password  пароль
     * @param firstName имя пользователя
     * @param lastName  фамилия пользователя
     */
    private void createFirebaseUser(final String email,
                                    final String password,
                                    final String firstName,
                                    final String lastName) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Добавление дополнительной информации пользователя в базу данных
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("FirstName", firstName);
                        userData.put("LastName", lastName);

                        usersRef.child(uid).setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    // Успех записи данных
                                    Log.d("REGISTER", "Пользователь зарегистрирован и информация сохранена");
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Неудача при сохранении данных
                                    Log.e("REGISTER", "Ошибка сохранения данных пользователя: " + e.getMessage());
                                    Toast.makeText(RegisterActivity.this, "Ошибка регистрации. Попробуйте позже.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Ошибка регистрации
                        Log.e("REGISTER", "Ошибка регистрации: " + task.getException().getMessage());
                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации. Проверьте данные и попробуйте ещё раз.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
package com.example.flowerking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUser, etRegEmail, etRegPassword, etRegConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etRegUser = findViewById(R.id.etRegUser);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> {
            String user = etRegUser.getText().toString().trim();
            String email = etRegEmail.getText().toString().trim();
            String pass = etRegPassword.getText().toString().trim();
            String confirmPass = etRegConfirmPassword.getText().toString().trim();

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);
            Toast.makeText(this, "Registrando en Firebase...", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                int nuevoUserId = dbHelper.registerUser(user, email, pass);

                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    if (nuevoUserId > 0) {
                        SessionManager.saveUser(RegisterActivity.this, nuevoUserId, user);
                        Toast.makeText(this, "¡Cuenta creada en Firebase!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    } else if (nuevoUserId == -1) {
                        Toast.makeText(this, "Ese nombre de usuario ya está registrado", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error de conexión con Firebase. Revisa tu internet", Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });

        tvGoToLogin.setOnClickListener(v -> finish());
    }
}
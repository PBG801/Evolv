package com.example.evolv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // NECESARIO: Se usa en setOnClickListener
import android.widget.Toast; // NECESARIO: Se usa para mostrar mensajes
import androidx.activity.OnBackPressedCallback; // NECESARIO: Se usa para el manejo personalizado del botón back
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton; // NECESARIO: Se usa para el botón de registro
import com.google.android.material.textfield.TextInputEditText; // NECESARIO: Se usa para los campos de texto

public class RegisterActivity extends AppCompatActivity {

    // --- Atributos privados ---

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- Inicialización de la UI y recursos ---
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);
        
        etEmail = findViewById(R.id.etRegUsername);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnConfirmRegister);

        // Configurar el comportamiento del botón back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Volver a MainActivity
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                // --- Validación de campos vacíos ---
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                    return;
                }

                // --- Validación de formato de email ---
                if (!isValidEmail(email)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                // --- Validación de coincidencia de contraseñas ---
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_passwords_not_match), Toast.LENGTH_SHORT).show();
                    return;
                }

                // --- Validación de disponibilidad de email ---
                if (!databaseHelper.isEmailAvailable(email)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_email_exists), Toast.LENGTH_SHORT).show();
                    return;
                }

                // --- Inserción en base de datos ---
                if (databaseHelper.insertUser(email, password)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.success_register), Toast.LENGTH_SHORT).show();
                    
                    // Volver a MainActivity para que el usuario inicie sesión
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_register), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static boolean isValidEmail(String email) {
        // --- Patrón simple para validar emails ---
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";
        return email != null && email.matches(emailPattern);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        databaseHelper = null;
    }
}


package com.example.evolv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private LanguageManager languageManager;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.applyLanguage();
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

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidEmail(email)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_passwords_not_match), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!databaseHelper.isEmailAvailable(email)) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.error_email_exists), Toast.LENGTH_SHORT).show();
                    return;
                }

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
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+";
        return email != null && email.matches(emailPattern);
    }
}

package com.example.evolv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister, btnAnonymous;
    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- Inicialización de la UI y recursos principales ---
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa el helper de base de datos
        databaseHelper = new DatabaseHelper(this);

        // Referencias a los campos y botones de la UI
        etEmail = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnAnonymous = findViewById(R.id.btnAnonymous);

        // (DEBUG) Muestra en Logcat la lista de usuarios existentes
        databaseHelper.listUsers();

        // --- BLOQUE: Login de usuario registrado ---
        // Establece el comportamiento del botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtiene los valores introducidos
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Valida que los campos no estén vacíos
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Comprueba las credenciales en la base de datos
                long loggedInUserId = databaseHelper.checkLogin(email, password);
                if (loggedInUserId != -1) {
                    // Guarda el userId en preferencias compartidas
                    getSharedPreferences("EvolvPrefs", MODE_PRIVATE)
                            .edit()
                            .putLong("userId", loggedInUserId)
                            .apply();

                    
                    // Lanza la HomeActivity con el userId
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra("userId", loggedInUserId);
                    startActivity(intent);
                    finish();
                } else {
                    // Credenciales incorrectas
                    Toast.makeText(MainActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // --- BLOQUE: Acceso como usuario anónimo (invitado) ---
        // Establece el comportamiento del botón de acceso anónimo
        btnAnonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Definir constante para el usuario anónimo
                final long ANONYMOUS_USER_ID = 0;
                // Guardar userId = 0 en SharedPreferences
                getSharedPreferences("EvolvPrefs", MODE_PRIVATE)
                        .edit().putLong("userId", ANONYMOUS_USER_ID).apply();
                Toast.makeText(MainActivity.this, getString(R.string.toast_user_id, ANONYMOUS_USER_ID), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("userId", ANONYMOUS_USER_ID);
                intent.putExtra("isAnonymous", true);
                startActivity(intent);
                finish();
            }
        });

        // --- BLOQUE: Ir a pantalla de registro ---
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpia los campos antes de abrir pantalla de registro
                etEmail.setText("");
                etPassword.setText("");
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        // --- BLOQUE: Comportamiento del botón físico 'Back' ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Cierra la aplicación completamente
                finishAffinity();
            }
        });

        // --- BLOQUE: Acceso como usuario anónimo (invitado) ---
        btnAnonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define el userId especial para usuario anónimo
                final long ANONYMOUS_USER_ID = 0;
                // Guarda el userId anónimo en preferencias compartidas
                getSharedPreferences("EvolvPrefs", MODE_PRIVATE)
                        .edit().putLong("userId", ANONYMOUS_USER_ID).apply();

                // Lanza la HomeActivity como invitado
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("userId", ANONYMOUS_USER_ID);
                intent.putExtra("isAnonymous", true);
                startActivity(intent);
                finish();
            }
        });
    }
}
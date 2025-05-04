package com.example.evolv;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {    
    private LanguageManager languageManager;
    private TextView tvWelcome;
    private MaterialButton btnLogout;
    private MaterialToolbar topAppBar;
    private boolean isAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.applyLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        topAppBar = findViewById(R.id.topAppBar);
        
        setSupportActionBar(topAppBar);

        // Configurar el comportamiento del botón back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isAnonymous) {
                    finish();
                } else {
                    moveTaskToBack(true);
                }
            }
        });
        
        // Configurar el menú
        addMenuProvider(new MenuProvider() {
            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                // Mostrar/ocultar items según el estado de autenticación
                menu.findItem(R.id.menu_create_account).setVisible(isAnonymous);
                menu.findItem(R.id.menu_home).setVisible(!isAnonymous);
                menu.findItem(R.id.menu_calendar).setVisible(!isAnonymous);
                menu.findItem(R.id.menu_logout).setVisible(!isAnonymous);
            }

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.top_app_bar_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                
                if (id == R.id.menu_create_account) {
                    // Si es usuario anónimo y quiere crear cuenta, vamos directamente a registro
                    Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.menu_home) {
                    // Ya estamos en home, no hacemos nada
                    return true;
                } else if (id == R.id.menu_calendar) {
                    Toast.makeText(HomeActivity.this, getString(R.string.calendar_coming_soon), Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_logout) {
                    logout();
                    return true;
                } else if (id == R.id.menu_language_es) {
                    languageManager.setLocale("es");
                    LanguageManager.recreateApp(HomeActivity.this);
                    return true;
                } else if (id == R.id.menu_language_en) {
                    languageManager.setLocale("en");
                    LanguageManager.recreateApp(HomeActivity.this);
                    return true;
                }
                return false;
            }
        }, this, Lifecycle.State.RESUMED);

        String email = getIntent().getStringExtra("email");
        isAnonymous = getIntent().getBooleanExtra("isAnonymous", false);

        if (email != null && !email.isEmpty()) {
            if (isAnonymous) {
                tvWelcome.setText(email);
            } else {
                tvWelcome.setText(getString(R.string.welcome_user, email));
            }
        }

        // Botón de logout (usa el mismo método que el menú)
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

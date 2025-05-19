package com.example.evolv;

import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper; // Aseguramos que sea campo de clase    
    private LanguageManager languageManager;
    private boolean isAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.applyLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        
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

        // --- Lógica para mostrar la lista de plantillas de entrenamiento ---
        RecyclerView recyclerView = findViewById(R.id.recyclerWorkoutTemplates);
        dbHelper = new DatabaseHelper(this);
        List<com.example.evolv.models.WorkoutTemplate> templateList = dbHelper.getAllWorkoutTemplates();

        com.example.evolv.adapters.WorkoutTemplateAdapter adapter = new com.example.evolv.adapters.WorkoutTemplateAdapter(templateList, templateId -> {
    Intent intent = new Intent(HomeActivity.this, com.example.evolv.activities.WorkoutTemplateDetailActivity.class);
    intent.putExtra("TEMPLATE_ID", templateId);
    startActivity(intent);
});
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        // --- Fin lógica lista de plantillas ---
    }

    private void logout() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

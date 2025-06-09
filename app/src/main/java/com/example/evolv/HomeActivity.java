package com.example.evolv;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    // --- Atributos privados ---
    private DatabaseHelper dbHelper;
    // TODO: Sustituir Object por el tipo real del adapter, por ejemplo: WorkoutTemplateAdapter
    private Object adapter; // Placeholder, ajustar al tipo real del adapter si está definido en la UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recuperar el ID de usuario
        long userId = getSharedPreferences("EvolvPrefs", MODE_PRIVATE).getLong("userId", -1);
        // Lanzar la pantalla principal de plantillas de entrenamiento
        Intent intent = new Intent(HomeActivity.this, com.example.evolv.activities.WorkoutTemplateListActivity_v2.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        // Limpiar preferencias solo si realmente se desea reiniciar el estado de usuario
        // getSharedPreferences("EvolvPrefs", MODE_PRIVATE).edit().clear().apply();
        // Finalizar esta actividad para que no quede en la pila
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTemplates();
    }

    //**
    //* Carga y filtra la lista de plantillas de entrenamiento para mostrar en la pantalla principal.
    //* - Muestra solo plantillas propias y plantillas por defecto no ocultas.
    //* - Oculta las plantillas por defecto (userId == -1 o 0) que el usuario haya ocultado.
    //* - Nunca muestra plantillas de otros usuarios.
    //**

    private void loadTemplates() {
        // Inicializar el helper de base de datos si es necesario
        if (dbHelper == null) dbHelper = new DatabaseHelper(this);

        // Obtener el ID del usuario actual desde SharedPreferences
        long userId = getSharedPreferences("EvolvPrefs", MODE_PRIVATE).getLong("userId", 0);

        // Recuperar el conjunto de IDs de plantillas por defecto ocultas para este usuario
        String key = "hidden_default_templates_user_" + userId;
        java.util.Set<String> hiddenSet = getSharedPreferences("EvolvPrefs", MODE_PRIVATE)
                .getStringSet(key, new java.util.HashSet<>());

        // Obtener todas las plantillas de la base de datos (propias y por defecto)
        java.util.List<com.example.evolv.models.WorkoutTemplate_v2> allTemplates = dbHelper.getAllWorkoutTemplate();
        java.util.List<com.example.evolv.models.WorkoutTemplate_v2> visibles = new java.util.ArrayList<>();

        // Filtrar plantillas (ver comentarios detallados arriba)
        for (com.example.evolv.models.WorkoutTemplate_v2 tpl : allTemplates) {
            boolean isDefault = (tpl.getUserId() == -1 || tpl.getUserId() == 0);
            if (isDefault && hiddenSet.contains(String.valueOf(tpl.getTemplateId()))) continue;
            if (!isDefault && tpl.getUserId() != userId) continue;
            visibles.add(tpl);
        }

        // Actualizar el adaptador del RecyclerView con la lista filtrada
        if (adapter != null) {
            // TODO: Implementar updateTemplates en el tipo real del adapter
            // ((WorkoutTemplateAdapter)adapter).updateTemplates(visibles);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        // Limpieza de referencias (opcional)
        dbHelper = null;
        adapter = null;
    }
}



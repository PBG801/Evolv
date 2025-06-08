package com.example.evolv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evolv.DatabaseHelper;
import com.example.evolv.MainActivity;
import com.example.evolv.R;
import com.example.evolv.adapters.WorkoutTemplateListAdapter_v2;
import com.example.evolv.models.WorkoutTemplate_v2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import java.util.List;

/**
 * Activity para mostrar la lista de plantillas de entrenamiento (v2).
 * Integra la lógica de sesiones pausadas y la UI visual.
 * Cumple con el protocolo de balanceo de llaves y comentarios aclaratorios.
 */
public class WorkoutTemplateListActivity_v2 extends AppCompatActivity {
    private static final int REQUEST_EDIT_TEMPLATE = 1001;
    private static final int REQUEST_ADD_TEMPLATE = 1002;
    private DatabaseHelper dbHelper; // Helper para operaciones de base de datos
    private RecyclerView recyclerView;
    private WorkoutTemplateListAdapter_v2 adapter;
    private List<WorkoutTemplate_v2> templates;
    private long currentUserId;
    private android.database.sqlite.SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_template_list_v2);

        // Inicializa la base de datos (ajusta según tu implementación)
        db = openOrCreateDatabase("evolv.db", MODE_PRIVATE, null);
        dbHelper = new DatabaseHelper(this);
        // Obtén el ID del usuario logado (ajusta según tu lógica)
        currentUserId = getIntent().getLongExtra("USER_ID", -1);
        
        // Actualizar el TextView con el email del usuario
        TextView textUserName = findViewById(R.id.textUserName);
        if (textUserName != null) {
            String userEmail = dbHelper.getUserEmail(currentUserId);
            textUserName.setText("Usuario: " + userEmail);
        }

        recyclerView = findViewById(R.id.recyclerViewTemplates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // FAB para añadir nueva plantilla
        FloatingActionButton fabAdd = findViewById(R.id.buttonAddTemplate);
        if (fabAdd != null) {
            boolean isAnonymous = (currentUserId == 0); // userId==0 es anónimo
            fabAdd.setEnabled(!isAnonymous);
            fabAdd.setClickable(!isAnonymous);
            fabAdd.setAlpha(isAnonymous ? 0.5f : 1.0f); // Visualmente atenuado si es anónimo

            if (!isAnonymous) {
                fabAdd.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EditWorkoutTemplateActivity.class);
                    intent.putExtra("USER_ID", currentUserId);
                    startActivityForResult(intent, REQUEST_ADD_TEMPLATE);
                });
            } else {
                fabAdd.setOnClickListener(null); // Asegura que no hace nada si es anónimo
            }
        }

        // Botón de logout
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setEnabled(true);
            btnLogout.setAlpha(1.0f);
            btnLogout.setOnClickListener(v -> {
                getSharedPreferences("EvolvPrefs", MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(this, MainActivity.class); // O la pantalla de login
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
        loadAndShowTemplates();

        // Mostrar siempre el userId al entrar en esta pantalla
        Toast.makeText(this, "UserId: " + currentUserId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Refrescar la lista si se vuelve de edición, creación o ejecución
        if ((requestCode == REQUEST_EDIT_TEMPLATE || requestCode == REQUEST_ADD_TEMPLATE || requestCode == 2001) && resultCode == RESULT_OK) {
            loadAndShowTemplates();
        }
    }

    /**
     * Carga la lista de plantillas y ejercicios desde la base de datos usando SQL directo,
     * mapea a modelos v2 y actualiza el adaptador. Marca sesiones pausadas según progreso.
     */
    private void loadAndShowTemplates() {
            templates = new java.util.ArrayList<>();
            String[] selectionArgs = {String.valueOf(currentUserId), "-1"};
            android.database.Cursor c = db.rawQuery("SELECT template_id, name, workout_type, notes, user_id FROM workout_template WHERE user_id = ? OR user_id = ?", selectionArgs);
            if (c != null) {
                while (c.moveToNext()) {
                    long templateId = c.getLong(0);
                    String name = c.getString(1);
                    String workoutType = c.getString(2);
                    String notes = c.getString(3);
                    int userId = c.getInt(4);
                    java.util.List<com.example.evolv.models.WorkoutTemplateExercise_v2> exercises = new java.util.ArrayList<>();
                    android.database.Cursor c2 = db.rawQuery("SELECT e.exercise_id, e.name, wte.sets, wte.repetitions, wte.target_duration, wte.rest_period, wte.duration_type, e.img_url " + "FROM workout_template_exercise wte JOIN exercise e ON wte.exercise_id = e.exercise_id WHERE wte.template_id = ?", new String[]{String.valueOf(templateId)});
                    if (c2 != null) {
                        while (c2.moveToNext()) {
                            long exerciseId = c2.getLong(0);
                            String exerciseName = c2.getString(1);
                            int sets = c2.getInt(2);
                            int reps = c2.getInt(3);
                            int duration = c2.getInt(4);
                            int rest = c2.getInt(5);
                            String durationType = c2.getString(6);
                            String imgUrl = c2.getString(7); // Nuevo campo
                            if (sets < 1) sets = 1;
                            if (reps < 1) reps = 10;
                            if (duration < 0) duration = 30;
                            if (rest < 0) rest = 30;
                            if (durationType == null) durationType = "";
                            exercises.add(new com.example.evolv.models.WorkoutTemplateExercise_v2(exerciseId, exerciseName, sets, reps, duration, rest, durationType, imgUrl // Añadido imgUrl
                            ));
                        }
                        c2.close();
                    }
                    com.example.evolv.models.WorkoutTemplate_v2 template = new com.example.evolv.models.WorkoutTemplate_v2(templateId, name, workoutType, notes, userId, exercises);
                    Log.d("EvolvDebug", "[REP][PLANTILLA] Ejercicios en plantilla: " + exercises.size());
                    for (com.example.evolv.models.WorkoutTemplateExercise_v2 wte : exercises) {
                        Log.d("EvolvDebug", "[REP][PLANTILLA] " + wte.getName() + " / " + wte.getImg_url());
                    }
                    // Marcar como pausada si corresponde
                    com.example.evolv.utils.SessionProgressManager_v2.Progress progress = com.example.evolv.utils.SessionProgressManager_v2.getProgress(this, (int) currentUserId, templateId);
                    template.setPaused(progress != null);
                    templates.add(template);
                }
                c.close();
            }
            adapter = new com.example.evolv.adapters.WorkoutTemplateListAdapter_v2(this, templates, new com.example.evolv.adapters.WorkoutTemplateListAdapter_v2.OnTemplateActionListener() {
                // Listener para manejar acciones de la plantilla
                @Override
                public void onPlay(WorkoutTemplate_v2 template) {
                    Intent intent = new Intent(WorkoutTemplateListActivity_v2.this, WorkoutPlayerActivity_v2.class);
                    intent.putExtra("USER_ID", currentUserId);
                    intent.putExtra("TEMPLATE_ID", template.getTemplateId());
                    intent.putExtra("RESUME", template.isPaused());
                    intent.putExtra("template", template);
                    startActivityForResult(intent, 2001);
                }

                @Override
                public void onPause(WorkoutTemplate_v2 template) {
                    Intent intent = new Intent(WorkoutTemplateListActivity_v2.this, WorkoutPlayerActivity_v2.class);
                    intent.putExtra("USER_ID", currentUserId);
                    intent.putExtra("TEMPLATE_ID", template.getTemplateId());
                    intent.putExtra("RESUME", true);
                    startActivityForResult(intent, 2001);
                }

                @Override
                public void onEdit(WorkoutTemplate_v2 template) {
                    Intent intent = new Intent(WorkoutTemplateListActivity_v2.this, EditWorkoutTemplateActivity.class);
                    intent.putExtra("USER_ID", currentUserId);
                    intent.putExtra("TEMPLATE_ID", template.getTemplateId());
                    startActivityForResult(intent, REQUEST_EDIT_TEMPLATE);
                }

                @Override
public void onDelete(WorkoutTemplate_v2 template) {
    new androidx.appcompat.app.AlertDialog.Builder(WorkoutTemplateListActivity_v2.this)
        .setTitle("¿Eliminar plantilla?")
        .setMessage("¿Seguro que deseas eliminar la plantilla '" + template.getName() + "'? Esta acción no se puede deshacer.")
        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
            int rows = dbHelper.deleteWorkoutTemplateById(template.getTemplateId());
            if (rows > 0) {
                templates.remove(template);
                adapter.notifyDataSetChanged();
                Toast.makeText(WorkoutTemplateListActivity_v2.this, "Plantilla eliminada correctamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WorkoutTemplateListActivity_v2.this, "No se pudo eliminar la plantilla (no corresponde al usuario actual).", Toast.LENGTH_SHORT).show();
            }
            loadAndShowTemplates();
        })
        .setNegativeButton(android.R.string.no, null)
        .show();
}
            });
            // Establecer el ID del usuario para controlar la visibilidad de botones de edición
            adapter.setCurrentUserId(currentUserId);
            recyclerView.setAdapter(adapter);


        }
    }

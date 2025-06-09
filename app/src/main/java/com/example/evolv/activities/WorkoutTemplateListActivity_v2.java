package com.example.evolv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evolv.DatabaseHelper;
import com.example.evolv.MainActivity;
import com.example.evolv.R;
import com.example.evolv.adapters.WorkoutTemplateListAdapter_v2;
import com.example.evolv.models.WorkoutTemplate_v2;
import com.example.evolv.models.WorkoutTemplateExercise_v2;
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
    private static final String TAG = "WorkoutTemplateList";
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
        
        // Configurar la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú de opciones desde XML
        getMenuInflater().inflate(R.menu.menu_workout_template_list, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Manejar clics en las opciones del menú
        int id = item.getItemId();
        Log.d(TAG, "CAL: onOptionsItemSelected - itemId: " + id);
        
        if (id == R.id.action_calendar) {
            // Navegar a la actividad de calendario y pasar el userId
            Log.d(TAG, "CAL: Iniciando actividad de calendario con userId: " + currentUserId);
            try {
                Intent calendarIntent = new Intent(this, WorkoutCalendarActivity.class);
                calendarIntent.putExtra("userId", currentUserId);
                Log.d(TAG, "CAL: Intent creado correctamente");
                startActivity(calendarIntent);
                Log.d(TAG, "CAL: startActivity ejecutado");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "CAL: Error al iniciar calendario: " + e.getMessage(), e);
                Toast.makeText(this, "Error al abrir calendario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return true;
            }
        }
        else if (id == R.id.action_update_expired) {
            Log.d(TAG, "CAL: Actualizando estados vencidos");
            try {
                updateExpiredWorkouts();
                Log.d(TAG, "CAL: Estados vencidos actualizados correctamente");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "CAL: Error al actualizar estados: " + e.getMessage(), e);
                Toast.makeText(this, "Error al actualizar estados: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return true;
            }
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Actualiza el estado de los entrenamientos vencidos y muestra el resultado al usuario
     */
    private void updateExpiredWorkouts() {
        Log.d(TAG, "CAL: Método updateExpiredWorkouts iniciado");
        int updatedCount = dbHelper.updateExpiredWorkouts();
        
        // Construir mensaje informativo según el resultado
        String message;
        if (updatedCount > 0) {
            message = getString(R.string.update_expired_success, updatedCount);
        } else {
            message = getString(R.string.update_expired_none);
        }
        
        // Mostrar resultado al usuario
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // Registrar en log para diagnóstico
        Log.d("EvolvDebug", "[UPDATE_EXPIRED] Entrenamientos actualizados: " + updatedCount);
        
        // Recargar lista para reflejar cualquier cambio (por si se muestran en el futuro)
        loadAndShowTemplates();
    }
    
    /**
     * Actualiza la lista de plantillas cada vez que se vuelve a la actividad,
     * asegurando que el estado de pausa se refleje correctamente.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("EvolvDebug", "[PAUSE] WorkoutTemplateList.onResume() llamado, userId=" + currentUserId);
        
        // Primero, comprobar si hay alguna plantilla pausada para este usuario
        java.util.List<Long> pausedTemplates = com.example.evolv.utils.SessionProgressManager_v2.getPausedTemplateIds(this, (int)currentUserId);
        Log.d("EvolvDebug", "[PAUSE] onResume: Plantillas pausadas detectadas: " + pausedTemplates.size());
        for (Long templateId : pausedTemplates) {
            Log.d("EvolvDebug", "[PAUSE] onResume: templateId pausada: " + templateId);
        }
        
        // Siempre actualizamos la lista al volver a la actividad para reflejar cualquier cambio
        // en el estado de pausa de las plantillas
        loadAndShowTemplates();
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Carga la lista de plantillas y ejercicios desde la base de datos usando SQL directo,
     * mapea a modelos v2 y actualiza el adaptador. Consulta el estado pausado desde SessionProgressManager_v2.
     */
    private void loadAndShowTemplates() {
            Log.d("EvolvDebug", "[PAUSE] WorkoutTemplateList.loadAndShowTemplates() INICIANDO, userId=" + currentUserId);
            templates = new java.util.ArrayList<>();
            String[] selectionArgs = {String.valueOf(currentUserId), "-1"};
            
            // Registro de SharedPreferences directamente
            android.content.SharedPreferences pausedPrefs = getSharedPreferences("paused_workout_sessions_v2", MODE_PRIVATE);
            Log.d("EvolvDebug", "[PAUSE] Contenido directo de SharedPreferences 'paused_workout_sessions_v2':");
            for (String key : pausedPrefs.getAll().keySet()) {
                Object value = pausedPrefs.getAll().get(key);
                Log.d("EvolvDebug", "[PAUSE] ------> key=" + key + ", value=" + value);
            }
            
            android.database.Cursor c = db.rawQuery("SELECT template_id, name, workout_type, notes, user_id FROM workout_template WHERE user_id = ? OR user_id = ?", selectionArgs);
            if (c != null) {
                Log.d("EvolvDebug", "[PAUSE] Se encontraron " + c.getCount() + " plantillas");
                while (c.moveToNext()) {
                    long templateId = c.getLong(0);
                    String name = c.getString(1);
                    String workoutType = c.getString(2);
                    String notes = c.getString(3);
                    int userId = c.getInt(4);
                    
                    Log.d("EvolvDebug", "[PAUSE] Procesando plantilla: id=" + templateId + 
                              ", name=" + name + ", userId=" + userId);
                    
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
                    
                    // Verificar si hay progreso guardado
                    com.example.evolv.utils.SessionProgressManager_v2.Progress progress = 
                        com.example.evolv.utils.SessionProgressManager_v2.getProgress(this, userId, templateId);
                    
                    // Consultar si la plantilla está pausada
                    boolean isPaused;
                    
                    // Para plantillas predeterminadas (userId = -1), usamos el ID del usuario actual
                    // para evitar que los estados pausados se compartan entre usuarios
                    int userIdToCheck;
                    if (userId == -1) {
                        // Si es usuario anónimo (userId = 0), usamos 0 como userId
                        // Si es usuario registrado, usamos su ID
                        userIdToCheck = (currentUserId == 0) ? 0 : (int)currentUserId;
                        isPaused = com.example.evolv.utils.SessionProgressManager_v2.isPaused(this, userIdToCheck, templateId);
                        Log.d("EvolvDebug", "[PAUSE] Consultando estado pausado de plantilla PREDETERMINADA con userIdToCheck=" + userIdToCheck + " (currentUserId=" + currentUserId + ")");
                    } else {
                        // Para plantillas de usuario normal, usar el userId de la plantilla
                        userIdToCheck = userId;
                        isPaused = com.example.evolv.utils.SessionProgressManager_v2.isPaused(this, userIdToCheck, templateId);
                    }
                    
                    Log.d("EvolvDebug", "[PAUSE] Plantilla " + templateId + ": progress=" + (progress != null ? "SI" : "NO") + 
                            ", isPaused=" + isPaused + ", isPausedSharedPref=" + 
                            pausedPrefs.getBoolean("paused_" + (userId == -1 ? currentUserId : userId) + "_" + templateId, false));
                    
                    template.setPaused(isPaused);
                    
                    if (isPaused) {
                        Log.d("EvolvDebug", "[PAUSE] Plantilla " + templateId + " MARCADA como PAUSADA en el modelo");
                    }
                    
                    templates.add(template);
                }
                c.close();
            }
            
            // Crear el adaptador con el listener de acciones
            adapter = new com.example.evolv.adapters.WorkoutTemplateListAdapter_v2(this, templates, new com.example.evolv.adapters.WorkoutTemplateListAdapter_v2.OnTemplateActionListener() {
                // Listener para manejar acciones de la plantilla
                @Override
                public void onPlay(WorkoutTemplate_v2 template) {
                    boolean isResuming = template.isPaused();
                    
                    Log.d("EvolvDebug", "[DIAG] onPlay: " + (isResuming ? "Reanudando" : "Iniciando") + " entrenamiento, template=" + 
                          (template != null ? template.getName() + " (id=" + template.getTemplateId() + ")" : "null"));
                    Log.d("EvolvDebug", "[DIAG] onPlay: Estado pausado=" + isResuming);
                    Log.d("EvolvDebug", "[DIAG] onPlay: Ejercicios=" + 
                          (template.getExercises() != null ? template.getExercises().size() : "null"));
                    
                    // Verificar estado de los ejercicios
                    if (template.getExercises() != null) {
                        for (int i = 0; i < template.getExercises().size(); i++) {
                            WorkoutTemplateExercise_v2 exercise = template.getExercises().get(i);
                            Log.d("EvolvDebug", "[DIAG] onPlay: Ejercicio[" + i + "]=" + 
                                  (exercise != null ? exercise.getName() : "null"));
                        }
                    }
                    
                    // Preparar intent para iniciar WorkoutPlayerActivity_v2
                    Intent intent = new Intent(WorkoutTemplateListActivity_v2.this, WorkoutPlayerActivity_v2.class);
                    intent.putExtra("USER_ID", currentUserId);
                    intent.putExtra("TEMPLATE_ID", template.getTemplateId());
                    intent.putExtra("RESUME", isResuming);
                    intent.putExtra("template", template);
                    
                    Log.d("EvolvDebug", "[DIAG] onPlay: Intent preparado con template serializado, iniciando actividad");
                    startActivityForResult(intent, 2001);
                }

                @Override
                public void onPause(WorkoutTemplate_v2 template) {
                    // Redirigir al método unificado de onPlay que maneja ambos casos
                    onPlay(template);
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
            
            // Logs finales para diagnóstico
            Log.d("EvolvDebug", "[PAUSE] Adaptador actualizado con " + templates.size() + " plantillas");
            
            // Verificamos los colores que se aplicarán a cada plantilla
            for (com.example.evolv.models.WorkoutTemplate_v2 tmpl : templates) {
                Log.d("EvolvDebug", "[PAUSE] Plantilla " + tmpl.getTemplateId() + ", nombre=" + tmpl.getName() + ", pausada=" + tmpl.isPaused());
            }
            
            Log.d("EvolvDebug", "[PAUSE] loadAndShowTemplates() COMPLETADO");
        }
    }

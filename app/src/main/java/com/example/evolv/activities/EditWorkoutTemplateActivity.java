package com.example.evolv.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ArrayAdapter;

import com.example.evolv.DatabaseHelper;
import com.example.evolv.R;
import com.example.evolv.adapters.WorkoutTemplateExerciseAdapter;
import com.example.evolv.models.WorkoutTemplateExercise_v2;
import com.example.evolv.models.WorkoutTemplate_v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class EditWorkoutTemplateActivity extends AppCompatActivity implements WorkoutTemplateExerciseAdapter.OnExerciseEditListener {

    // Campo de texto para el nombre de la plantilla de entrenamiento
    private EditText editWorkoutName;
    // Spinner para el tipo de entrenamiento
    private android.widget.Spinner spinnerWorkoutType;
    // Campo de texto para notas
    private EditText editNotes; // Nuevo campo para notas
    // Lista de todos los ejercicios disponibles que se pueden agregar a la plantilla
    private List<com.example.evolv.models.Exercise> allExercises = new ArrayList<>();
    // Lista de ejercicios seleccionados por el usuario para la plantilla actual (con valores editables)
    private final List<WorkoutTemplateExercise_v2> selectedExercises = new ArrayList<>();
    // Acceso a la base de datos de la aplicación
    private DatabaseHelper dbHelper;
    // Id de la plantilla que se está editando (null si es una nueva plantilla)
    private Long templateId = null;
    // Adaptador para mostrar y editar los ejercicios de la plantilla
    private WorkoutTemplateExerciseAdapter exerciseAdapter;
    // Plantilla actual (si se está editando)
    private WorkoutTemplate_v2 currentTemplate;
    // Id del usuario actual
    private long currentUserId = -1; // Valor por defecto para no logueado

    /**
     * Método requerido por la interfaz OnExerciseEditListener.
     * Abre el diálogo de edición para el ejercicio seleccionado.
     */
    @Override
    public void onEdit(WorkoutTemplateExercise_v2 wte, int position) {
        showEditExerciseDialog(wte, position);
    }

    /**
     * Muestra un diálogo para editar los valores del ejercicio seleccionado.
     * Actualiza el objeto y notifica al adapter.
     */
    private void showEditExerciseDialog(final WorkoutTemplateExercise_v2 wte, final int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_exercise_values, null);
        final EditText editSets = dialogView.findViewById(R.id.editSets);
        final EditText editReps = dialogView.findViewById(R.id.editReps);
        final EditText editDuration = dialogView.findViewById(R.id.editDuration);
        final EditText editRest = dialogView.findViewById(R.id.editRest);

        // Cargar valores actuales
        editSets.setText(String.valueOf(wte.getSets()));
        editReps.setText(String.valueOf(wte.getRepetitions()));
        editDuration.setText(String.valueOf(wte.getTargetDuration()));
        editRest.setText(String.valueOf(wte.getRestPeriod()));

        new AlertDialog.Builder(this).setTitle(R.string.edit_exercise_values).setView(dialogView).setPositiveButton(R.string.save, (dialog, which) -> {
            // Validar y guardar los nuevos valores
            try {
                wte.setSets(Integer.parseInt(editSets.getText().toString()));
                wte.setRepetitions(Integer.parseInt(editReps.getText().toString()));
                wte.setTargetDuration(Integer.parseInt(editDuration.getText().toString()));
                wte.setRestPeriod(Integer.parseInt(editRest.getText().toString()));
                exerciseAdapter.notifyItemChanged(position);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_invalid_number, Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton(R.string.cancel, null).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Obtener el userId desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("EvolvPrefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("userId", -1);
        Log.d("USER", "[onCreate] currentUserId obtenido de prefs: " + currentUserId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        Log.d("LAYOUT_DEBUG", "Se está usando activity_edit_workout.xml");

        // Inicialización de base de datos y campos de UI
        dbHelper = new DatabaseHelper(this);
        editWorkoutName = findViewById(R.id.editWorkoutName);
        spinnerWorkoutType = findViewById(R.id.spinnerWorkoutType);
        Log.d("SPIN", "Referencia Spinner obtenida: " + spinnerWorkoutType);
        editNotes = findViewById(R.id.editNotes);

        // Obtener el ID de la plantilla desde el Intent (clave TEMPLATE_ID)
        templateId = getIntent().getLongExtra("TEMPLATE_ID", -1);

        // --- SPIN: Inicialización del adapter y asignación al Spinner ---
        String[] workoutTypes = getResources().getStringArray(R.array.workout_type_options);
        Log.d("SPIN", "Tipos de entrenamiento disponibles: " + java.util.Arrays.toString(workoutTypes));
        ArrayAdapter<String> workoutTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workoutTypes);
        workoutTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkoutType.setAdapter(workoutTypeAdapter);
        Log.d("SPIN", "Adapter asignado al Spinner: " + spinnerWorkoutType.getAdapter());
        spinnerWorkoutType.setEnabled(true);
        spinnerWorkoutType.setClickable(true);

        spinnerWorkoutType.setOnTouchListener((v, event) -> {
            Log.d("SPIN", "Spinner tocado (onTouch)");
            return false;
        });
        spinnerWorkoutType.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("SPIN", "Spinner focus change: " + hasFocus);
        });
        spinnerWorkoutType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Log.d("SPIN", "onItemSelected: " + position + ", valor: " + parent.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                Log.d("SPIN", "onNothingSelected");
            }
        });

        // Referencia al layout raíz y al distintivo
        final View rootLayout = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        final TextView textDefaultWorkoutLabel = findViewById(R.id.textDefaultWorkoutLabel);

        // Configuración del RecyclerView para mostrar los ejercicios de la plantilla
        RecyclerView recyclerExercises = findViewById(R.id.recyclerExercises);
        allExercises = dbHelper.getAllExercises();
        exerciseAdapter = new WorkoutTemplateExerciseAdapter(allExercises, selectedExercises, (wte, position) -> {
            showEditExerciseDialog(wte, position);
        }, currentUserId);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(exerciseAdapter);

        // Si estamos en modo edición (templateId válido), cargar los datos actuales de la plantilla
        if (templateId != -1) {
            com.example.evolv.models.WorkoutTemplate_v2 template = dbHelper.getWorkoutTemplate_v2ById(templateId);
            if (template != null) {
                editWorkoutName.setText(template.getName());
                ArrayAdapter<String> workoutTypeAdapterEdit = (ArrayAdapter<String>) spinnerWorkoutType.getAdapter();
int spinnerPosition = workoutTypeAdapter != null ? workoutTypeAdapter.getPosition(template.getWorkoutType()) : 0;
                spinnerWorkoutType.setSelection(spinnerPosition >= 0 ? spinnerPosition : 0);
                editNotes.setText(template.getNotes());
                List<WorkoutTemplateExercise_v2> wteList = dbHelper.getExercisesForTemplate(templateId);
                selectedExercises.clear();
                selectedExercises.addAll(wteList);
                Log.d("SPIN", "selectedExercises tras cargar plantilla: " + selectedExercises.size()); // SPIN
                exerciseAdapter.notifyDataSetChanged();
                Log.d("SPIN", "notifyDataSetChanged() llamado tras cargar ejercicios seleccionados"); // SPIN

                // Lógica visual para plantillas por defecto
                if (template.getUserId() == -1) {
                    if (rootLayout != null)
                        rootLayout.setBackgroundResource(R.color.defaultWorkoutBackground);
                    if (textDefaultWorkoutLabel != null)
                        textDefaultWorkoutLabel.setVisibility(View.VISIBLE);
                } else {
                    if (rootLayout != null)
                        rootLayout.setBackgroundResource(android.R.color.background_light);
                    if (textDefaultWorkoutLabel != null)
                        textDefaultWorkoutLabel.setVisibility(View.GONE);
                }
            }
        }

        // Botón para guardar la plantilla
        Button btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
        btnSaveWorkout.setOnClickListener(v -> saveTemplate());

        // Botón para eliminar entrenamiento
        Button btnDeleteWorkout = findViewById(R.id.btnDeleteWorkout);

        // Comprobar si es edición o creación
        if (templateId != null && templateId != -1) {
            setTitle("Editar entrenamiento");
            btnDeleteWorkout.setVisibility(View.VISIBLE);
            btnDeleteWorkout.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("¿Eliminar entrenamiento?")
                        .setMessage("¿Estás seguro de que deseas eliminar este entrenamiento? Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            // Eliminar plantilla solo si es del usuario actual (no por defecto)
                            if (templateId != null && templateId != -1 && currentTemplate != null && currentTemplate.getUserId() != -1) {
                                int deleted = dbHelper.deleteWorkoutTemplateById(templateId);
                                if (deleted > 0) {
                                    Toast.makeText(this, "Entrenamiento eliminado", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "No se puede eliminar una plantilla por defecto", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        } else {
            setTitle("Nuevo entrenamiento");
            btnDeleteWorkout.setVisibility(View.GONE); // Ocultar botón eliminar en modo creación
        }
    }

    private void showEditExerciseDialog(WorkoutTemplateExercise_v2 item, Runnable onUpdated) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_exercise_values, null);

        TextView editSets = dialogView.findViewById(R.id.editSets);
        TextView editReps = dialogView.findViewById(R.id.editReps);
        TextView editDuration = dialogView.findViewById(R.id.editDuration);
        TextView editRest = dialogView.findViewById(R.id.editRest);

        // Inicializar campos
        editSets.setText(String.valueOf(item.getSets()));
        editReps.setText(String.valueOf(item.getRepetitions()));
        editDuration.setText(String.valueOf(item.getTargetDuration()));
        editRest.setText(String.valueOf(item.getRestPeriod()));

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_exercise_values)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    item.setSets(parseIntOrZero(editSets.getText().toString()));
                    item.setRepetitions(parseIntOrZero(editReps.getText().toString()));
                    item.setTargetDuration(parseIntOrZero(editDuration.getText().toString()));
                    item.setRestPeriod(parseIntOrZero(editRest.getText().toString()));
                    if (onUpdated != null) onUpdated.run();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int parseIntOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Guarda la plantilla de entrenamiento y sus ejercicios asociados.
     * - Obtiene los datos de la UI (nombre, tipo, notas).
     * - Obtiene la lista actualizada de ejercicios seleccionados desde el Adapter.
     * - Valida los campos requeridos.
     * - Inserta o actualiza la plantilla en la base de datos.
     * - Asocia los ejercicios seleccionados a la plantilla.
     * - Muestra mensajes de éxito o error y cierra la Activity tras guardar.
     */
    private void saveTemplate() {
        android.util.Log.d("CREA", "[saveTemplate][INICIO] Entrando en saveTemplate()");
        long templateIdLocal = 0; // Inicializa para evitar error de compilación

        // Obtener nombre
        String templateName = editWorkoutName.getText().toString().trim();
        android.util.Log.d("CREA", "[saveTemplate] templateName: " + templateName);

        // Obtener tipo desde el Spinner
        int workoutTypePosition = spinnerWorkoutType != null ? spinnerWorkoutType.getSelectedItemPosition() : 0;
        String workoutType = (workoutTypePosition > 0 && spinnerWorkoutType != null && spinnerWorkoutType.getSelectedItem() != null)
                ? spinnerWorkoutType.getSelectedItem().toString().trim() : "";
        android.util.Log.d("CREA", "[saveTemplate] workoutType: " + workoutType);
        if (workoutTypePosition == 0) {
            android.util.Log.w("CREA", "[saveTemplate][VALIDACION] Tipo de entrenamiento no seleccionado");
            runOnUiThread(() -> Toast.makeText(this, "Por favor, selecciona un tipo de entrenamiento", Toast.LENGTH_SHORT).show());
            return;
        }

        // Obtener notas
        String notes = editNotes != null ? editNotes.getText().toString().trim() : "";
        android.util.Log.d("CREA", "[saveTemplate] notes: " + notes);

        // Usar la lista de ejercicios seleccionados directamente del adapter
        List<WorkoutTemplateExercise_v2> exercises = exerciseAdapter.getSelectedExercises();
        android.util.Log.d("CREA", "[Activity][saveTemplate] selectedExercises.size()=" + exercises.size());
        for (WorkoutTemplateExercise_v2 ex : exercises) {
            android.util.Log.d("CREA", "[Activity][saveTemplate] seleccionado: " + ex.getName());
        }
        android.util.Log.d("CREA", "[saveTemplate] selectedExercises.size(): " + exercises.size());

        // Validación básica
        if (templateName.isEmpty() || exercises.isEmpty()) {
            android.util.Log.w("CREA", "[saveTemplate][VALIDACION] Nombre vacío o sin ejercicios seleccionados");
            runOnUiThread(() -> Toast.makeText(this, "Por favor, introduce un nombre y selecciona al menos un ejercicio", Toast.LENGTH_SHORT).show());
            return;
        }
        
        // Verificar si ya existe un entrenamiento con el mismo nombre (excepto si es el mismo que estamos editando)
        boolean isNameDuplicate = dbHelper.existsWorkoutTemplate_v2Name(templateName);
        boolean isSameTemplate = currentTemplate != null && 
                               currentTemplate.getUserId() != -1 && 
                               templateName.equals(currentTemplate.getName());
        
        if (isNameDuplicate && !isSameTemplate) {
            android.util.Log.w("CREA", "[saveTemplate][VALIDACION] Ya existe un entrenamiento con ese nombre");
            final String templateNameFinal = templateName;
            runOnUiThread(() -> Toast.makeText(this, "Ya existe un entrenamiento con el nombre '" + templateNameFinal + "'. Por favor, usa un nombre diferente.", Toast.LENGTH_LONG).show());
            return;
        }

        // Si es una plantilla predeterminada (userId = -1), creamos un nombre único para la copia
        if (currentTemplate != null && currentTemplate.getUserId() == -1) {
            android.util.Log.d("CREA", "[saveTemplate] Es una plantilla predeterminada, generando nombre para la copia");
            
            // Generar un nombre base para la copia
            String baseCopyName = templateName + " (copia)";
            String uniqueName = baseCopyName;
            int copyNumber = 2;
            
            // Verificar si ya existe una plantilla con ese nombre y generar alternativas
            while (dbHelper.existsWorkoutTemplate_v2Name(uniqueName)) {
                uniqueName = templateName + " (copia " + copyNumber + ")";
                copyNumber++;
                android.util.Log.d("CREA", "[saveTemplate] Nombre existente, probando: " + uniqueName);
            }
            
            // Usar el nombre único generado
            templateName = uniqueName;
            android.util.Log.d("CREA", "[saveTemplate] Nombre final para la copia: " + templateName);
        }
        
        if (currentTemplate != null && currentTemplate.getUserId() != -1) {
            android.util.Log.d("CREA", "[saveTemplate][MODO EDICION] Actualizando plantilla existente. templateId: " + currentTemplate.getTemplateId());
            android.util.Log.d("CREA", "[saveTemplate][MODO EDICION] userId usado para update: " + currentUserId);
            // Actualizar plantilla existente SOLO si es del usuario
            // Crea una nueva instancia de WorkoutTemplate_v2 con los valores actualizados
            currentTemplate = new WorkoutTemplate_v2(templateIdLocal, // o templateId si corresponde
                    templateName, workoutType, notes, (int) currentUserId, exercises);
            android.util.Log.d("CREA", "[saveTemplate][updateWorkoutTemplate_v2] Llamando a updateWorkoutTemplate_v2() con userId: " + currentUserId);
            dbHelper.updateWorkoutTemplate_v2(currentTemplate, currentUserId);
            templateIdLocal = currentTemplate.getTemplateId();
        } else {
            // Si es plantilla por defecto o nueva, crear SIEMPRE una nueva plantilla de usuario
            android.util.Log.d("CREA", "[saveTemplate][MODO NUEVA] userId usado para insert: " + currentUserId);
            WorkoutTemplate_v2 newTemplate = new WorkoutTemplate_v2(0, templateName, workoutType, notes, (int) currentUserId, exercises);
            android.util.Log.d("CREA", "[saveTemplate][MODO NUEVA] Insertando nueva plantilla de usuario con userId: " + currentUserId);
            WorkoutTemplate_v2 inserted = dbHelper.insertWorkoutTemplate_v2(newTemplate, currentUserId);
            android.util.Log.d("CREA", "[saveTemplate][insertWorkoutTemplate_v2] Resultado: " + (inserted != null ? inserted.getTemplateId() : "null"));
            templateIdLocal = (inserted != null) ? inserted.getTemplateId() : -1;
        }

        // Validar y rellenar valores críticos antes de guardar
        android.util.Log.d("CREA", "[saveTemplate] Validando y ajustando valores de ejercicios seleccionados");
        for (WorkoutTemplateExercise_v2 wte : exercises) {
            if (wte.getSets() < 1) wte.setSets(1);
            if (wte.getRepetitions() < 1) wte.setRepetitions(10);
            if (wte.getTargetDuration() < 0) wte.setTargetDuration(30);
            if (wte.getRestPeriod() < 0) wte.setRestPeriod(30);
            if (wte.getDurationType() == null) wte.setDurationType("");
        }

        // Asociar ejercicios seleccionados a la plantilla
        android.util.Log.d("CREA", "[saveTemplate] Asociando ejercicios a plantilla con templateIdLocal: " + templateIdLocal);
        dbHelper.replaceExercisesForTemplate(templateIdLocal, exercises);
        android.util.Log.d("CREA", "[saveTemplate][FIN] Guardado de plantilla y asociación de ejercicios completado");

        runOnUiThread(() -> {
            Toast.makeText(this, "Plantilla guardada correctamente", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }


}


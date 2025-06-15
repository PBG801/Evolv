package com.example.evolv.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evolv.DatabaseHelper;
import com.example.evolv.R;
import com.example.evolv.adapters.WorkoutTemplateExerciseAdapter;
import com.example.evolv.models.Exercise;
import com.example.evolv.models.WorkoutTemplate_v2;
import com.example.evolv.models.WorkoutTemplateExercise_v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Actividad para editar plantillas de entrenamiento.
 * Permite crear o editar plantillas, agregar o eliminar ejercicios,
 * y guardar los cambios en la base de datos.
 */




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
        showEditExerciseDialog(wte, (Runnable) () -> exerciseAdapter.notifyItemChanged(position));
    }

    // Eliminada la versión obsoleta de showEditExerciseDialog con EditText. Se debe usar la versión moderna con Spinner y Runnable.


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
            showEditExerciseDialog(wte, () -> exerciseAdapter.notifyItemChanged(position));
        }, currentUserId);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(exerciseAdapter);
        
        // Configuración de los botones para mover ejercicios arriba/abajo
        ImageButton btnMoveExerciseUp = findViewById(R.id.btnMoveExerciseUp);
        ImageButton btnMoveExerciseDown = findViewById(R.id.btnMoveExerciseDown);
        
        // Listener para mover ejercicio hacia arriba
        btnMoveExerciseUp.setOnClickListener(v -> {
            int selectedPosition = exerciseAdapter.getSelectedPosition();
            Log.d("EvolvDebug", "[MOVE] Intentando mover hacia arriba posición: " + selectedPosition);
            
            // Verificar que hay un ejercicio seleccionado y no es el primero
            if (selectedPosition > 0 && selectedPosition < selectedExercises.size()) {
                // Intercambiar el ejercicio seleccionado con el anterior
                WorkoutTemplateExercise_v2 selected = selectedExercises.get(selectedPosition);
                WorkoutTemplateExercise_v2 previous = selectedExercises.get(selectedPosition - 1);
                
                // Actualizar los órdenes (si se usan)
                int tempOrder = selected.getExecutionOrder();
                selected.setExecutionOrder(previous.getExecutionOrder());
                previous.setExecutionOrder(tempOrder);
                
                // Intercambiar posiciones en la lista
                selectedExercises.set(selectedPosition - 1, selected);
                selectedExercises.set(selectedPosition, previous);
                
                // Actualizar la posición seleccionada (para seguir el ejercicio movido)
                exerciseAdapter.setSelectedPosition(selectedPosition - 1);
                
                // Notificar al adaptador
                exerciseAdapter.notifyItemMoved(selectedPosition, selectedPosition - 1);
                exerciseAdapter.notifyItemRangeChanged(selectedPosition - 1, 2); // Actualizar ambos ítems
                
                Log.d("EvolvDebug", "[MOVE] Ejercicio movido hacia arriba: " + selected.getName());
            } else {
                Log.d("EvolvDebug", "[MOVE] No se puede mover hacia arriba: no hay selección o ya está al inicio");
            }
        });
        
        // Listener para mover ejercicio hacia abajo
        btnMoveExerciseDown.setOnClickListener(v -> {
            int selectedPosition = exerciseAdapter.getSelectedPosition();
            Log.d("EvolvDebug", "[MOVE] Intentando mover hacia abajo posición: " + selectedPosition);
            
            // Verificar que hay un ejercicio seleccionado y no es el último
            if (selectedPosition >= 0 && selectedPosition < selectedExercises.size() - 1) {
                // Intercambiar el ejercicio seleccionado con el siguiente
                WorkoutTemplateExercise_v2 selected = selectedExercises.get(selectedPosition);
                WorkoutTemplateExercise_v2 next = selectedExercises.get(selectedPosition + 1);
                
                // Actualizar los órdenes (si se usan)
                int tempOrder = selected.getExecutionOrder();
                selected.setExecutionOrder(next.getExecutionOrder());
                next.setExecutionOrder(tempOrder);
                
                // Intercambiar posiciones en la lista
                selectedExercises.set(selectedPosition + 1, selected);
                selectedExercises.set(selectedPosition, next);
                
                // Actualizar la posición seleccionada (para seguir el ejercicio movido)
                exerciseAdapter.setSelectedPosition(selectedPosition + 1);
                
                // Notificar al adaptador
                exerciseAdapter.notifyItemMoved(selectedPosition, selectedPosition + 1);
                exerciseAdapter.notifyItemRangeChanged(selectedPosition, 2); // Actualizar ambos ítems
                
                Log.d("EvolvDebug", "[MOVE] Ejercicio movido hacia abajo: " + selected.getName());
            } else {
                Log.d("EvolvDebug", "[MOVE] No se puede mover hacia abajo: no hay selección o ya está al final");
            }
        });

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
                                    //Toast.makeText(this, "Entrenamiento eliminado", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    //Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //Toast.makeText(this, "No se puede eliminar una plantilla por defecto", Toast.LENGTH_SHORT).show();
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

    private void setupView() {
        // Configuración del RecyclerView para la lista de ejercicios seleccionados
        RecyclerView recyclerExercises = findViewById(R.id.recyclerExercises);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        // Obtener ID de usuario activo
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getLong("userId", 1); // Default a 1 si no hay usuario
        
        // Crear adaptador con todos los parámetros requeridos
        exerciseAdapter = new WorkoutTemplateExerciseAdapter(allExercises, selectedExercises, this, currentUserId);
        recyclerExercises.setAdapter(exerciseAdapter);
        
        // NOTA: Botón eliminado del layout, código comentado para mantener referencia
        // Configuración del botón para agregar ejercicios
        // Button btnAddExercise = findViewById(R.id.btnAddExercise);
        // btnAddExercise.setOnClickListener(view -> {
        //     // Mostrar diálogo para seleccionar ejercicios
        //     showExerciseSelectionDialog();
        //     android.util.Log.d("SPIN", "Botón de añadir ejercicio pulsado");
        // });
        
        // Configuración del botón para guardar la plantilla
        Button btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
        btnSaveWorkout.setOnClickListener(view -> saveTemplate());
        
        // Configuración del botón para eliminar la plantilla
        ImageButton btnDeleteWorkout = findViewById(R.id.btnDeleteWorkout);
        
        // Verificar si estamos editando una plantilla existente para configurar UI acorde
        if (templateId != null && templateId > 0) {
            setTitle("Editar entrenamiento");
        }
    }
    
    /**
     * Asegura que todos los Spinner contenidos en una vista mantienen su interactividad
     * @param view La vista que contiene los Spinner
     */
    private void ensureSpinnersInteractivity(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                ensureSpinnersInteractivity(child);
                
                if (child instanceof Spinner) {
                    Spinner spinner = (Spinner) child;
                    Log.d("SPINNER_DEBUG", "Asegurando interactividad del spinner: " + spinner.getId());
                    
                    // Asegurar que el spinner está habilitado e interactivo
                    spinner.setEnabled(true);
                    spinner.setClickable(true);
                    spinner.setFocusable(true);
                }
            }
        }
    }
    
    /**
     * Configura un spinner para garantizar su completa funcionalidad e interactividad
     * Esta configuración permite que el spinner responda correctamente a los toques
     * y que los ítems del dropdown sean fácilmente seleccionables
     * 
     * @param spinner El spinner a configurar
     */
    private void ensureFullSpinnerFunctionality(Spinner spinner) {
        // Habilitar interactividad básica
        spinner.setEnabled(true);
        spinner.setClickable(true);
        spinner.setFocusable(true);
        
        // Añadir listener de toque que permite el despliegue adecuado
        spinner.setOnTouchListener((v, event) -> {
            Log.d("SPINNER", "Spinner tocado - permitiendo comportamiento normal");
            return false; // Importante: false permite que el evento siga propagándose
        });
        
        // Listener para cambios de foco
        spinner.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("SPINNER", "Spinner cambio de foco: " + hasFocus);
        });
        
        // Listener para selecciones de ítems
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("SPINNER", "Elemento seleccionado en posición " + position);
                // Intentamos darle estilos mejorados al texto seleccionado
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(spinner.getResources()
                            .getColor(android.R.color.black));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("SPINNER", "Ninguna selección");
            }
        });
    }

    /**
     * Aplica colores a todos los TextViews contenidos en una vista
     * @param view La vista que contiene los TextView
     * @param textColorResId El ID del recurso de color para texto
     */
    private void applyColorToAllTextViews(View view, int textColorResId) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                applyColorToAllTextViews(child, textColorResId);
            }
        } else if (view instanceof TextView && !(view instanceof Button)) {
            ((TextView) view).setTextColor(getResources().getColor(textColorResId));
            Log.d("COLOR", "Color aplicado a TextView: " + ((TextView) view).getText());
        }
    }
    
    /**
     * Aplica configuraciones a los spinners para asegurar su funcionalidad manteniendo la estética.
     * NO modifica el fondo ni reemplaza adaptadores para evitar problemas de interactividad.
     */
    private void applyColorToAllSpinners(View view, int bgColorResId, int textColorResId) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                applyColorToAllSpinners(child, bgColorResId, textColorResId);
                if (child instanceof Spinner) {
                    Spinner spinner = (Spinner) child;
                    // Solo aseguramos que sean interactivos sin modificar su estética
                    spinner.setEnabled(true);
                    spinner.setClickable(true);
                    spinner.setFocusable(true);
                }
            }
        }
    }
    
    /**
     * Muestra un diálogo para editar los valores de un ejercicio (series, repeticiones, etc.)
     * Aplica colores personalizados para modo oscuro directamente.
     * 
     * @param item El ejercicio a editar
     * @param onUpdated Callback que se ejecutará después de actualizar el ejercicio
     */
    private void showEditExerciseDialog(WorkoutTemplateExercise_v2 item, Runnable onUpdated) {
        android.util.Log.d("EDIT", "[showEditExerciseDialog] INICIANDO con ejercicio: " + item.getName() + 
                ", sets=" + item.getSets() + ", reps=" + item.getRepetitions());
        
        // Crear vista personalizada para el diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_exercise_values, null);
        
        // Verificar si estamos en modo oscuro para aplicar colores directamente
        boolean isNightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        
        if (isNightMode) {
            // Aplicar colores de modo oscuro directamente a la vista
            dialogView.setBackgroundColor(getResources().getColor(R.color.surface_dark));
            
            // Aplicar colores a todos los TextViews contenidos en el layout
            applyColorToAllTextViews(dialogView, R.color.on_primary);
            
            // Ya no llamamos a applyColorToAllSpinners para evitar interferencias
            // Los spinners usarán los estilos definidos en XML
            
            Log.d("EDIT_DIALOG", "Aplicados colores de modo oscuro a textos y fondo");
        }
        
        // Obtener referencias a los spinners
       // Spinner editSets = dialogView.findViewById(R.id.editSets);
        Spinner editReps = dialogView.findViewById(R.id.editReps);
        Spinner editDuration = dialogView.findViewById(R.id.editDuration);
        Spinner editRest = dialogView.findViewById(R.id.editRest);
        
        Log.d("EDIT_DIALOG", "Configurando spinners para máxima compatibilidad");
        
        // Preparar array adapter para todos los spinners usando layouts nativos estándar
        String[] numValues = getResources().getStringArray(R.array.numeric_1_30);
        
        // IMPORTANTE: Al usar esta combinación específica de layouts nativos de Android, funcionará correctamente
        int spinnerLayout = android.R.layout.simple_spinner_item;
        int dropdownLayout = android.R.layout.simple_spinner_dropdown_item; // Layout estándar de Android
        
        ArrayAdapter<String> setsAdapter = new ArrayAdapter<>(this, spinnerLayout, numValues);
        setsAdapter.setDropDownViewResource(dropdownLayout); 
        //editSets.setAdapter(setsAdapter);
        //editSets.setEnabled(true);
        //editSets.setClickable(true);

        /* editSets.setOnTouchListener((v, event) -> {
            Log.d("SPIN", "Spinner sets tocado (onTouch)");
            return false;
        });
        editSets.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("SPIN", "Spinner sets focus change: " + hasFocus);
        });
        editSets.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Log.d("SPIN", "Sets seleccionado en posición: " + position + ", valor: " + numValues[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                Log.d("SPIN", "Ningún sets seleccionado");
            }
        });*/

        ArrayAdapter<String> repsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numValues);
        repsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editReps.setAdapter(repsAdapter);
        editReps.setEnabled(true);
        editReps.setClickable(true);

        editReps.setOnTouchListener((v, event) -> {
            Log.d("SPIN", "Spinner reps tocado (onTouch)");
            return false;
        });
        editReps.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("SPIN", "Spinner reps focus change: " + hasFocus);
        });
        editReps.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Log.d("SPIN", "Reps seleccionado en posición: " + position + ", valor: " + numValues[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                Log.d("SPIN", "Ningún reps seleccionado");
            }
        });

        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numValues);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editDuration.setAdapter(durationAdapter);
        editDuration.setEnabled(true);
        editDuration.setClickable(true);

        editDuration.setOnTouchListener((v, event) -> {
            Log.d("SPIN", "Spinner duration tocado (onTouch)");
            return false;
        });
        editDuration.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("SPIN", "Spinner duration focus change: " + hasFocus);
        });
        editDuration.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Log.d("SPIN", "Duration seleccionado en posición: " + position + ", valor: " + numValues[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                Log.d("SPIN", "Ningún duration seleccionado");
            }
        });

        ArrayAdapter<String> restAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numValues);
        restAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editRest.setAdapter(restAdapter);
        editRest.setEnabled(true);
        editRest.setClickable(true);

        editRest.setOnTouchListener((v, event) -> {
            Log.d("SPIN", "Spinner rest tocado (onTouch)");
            return false;
        });
        editRest.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d("SPIN", "Spinner rest focus change: " + hasFocus);
        });
        editRest.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Log.d("SPIN", "Rest seleccionado en posición: " + position + ", valor: " + numValues[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                Log.d("SPIN", "Ningún rest seleccionado");
            }
        });

        Log.d("EDIT_DIALOG", "Spinners configurados exactamente igual que spinnerWorkoutType");
        
        // Ya no usamos radio buttons para modo temporizador/repeticiones
        // El ejercicio siempre se regirá por tiempo
        
        Log.d("EDIT_DIALOG", "Referencias a controles obtenidas");
        
        // Verificar que los spinners tienen adapter y elementos
       /* if (editSets.getAdapter() != null) {
            Log.d("EDIT_DIALOG", "editSets tiene " + editSets.getAdapter().getCount() + " elementos");
        } else {
            Log.e("EDIT_DIALOG", "ERROR: editSets no tiene adapter");
        }*/
        
        // Log para depurar los valores actuales configurados por el usuario
        Log.d("EDIT_DIALOG", "Usando valores configurados por el usuario: " +
              "sets=" + item.getSets() + ", " +
              "reps=" + item.getRepetitions() + ", " +
              "duration=" + item.getTargetDuration() + ", " +
              "rest=" + item.getRestPeriod() + ", " +
              "durationType=" + item.getDurationType());
        
        // Aplicar valores mínimos sólo si son necesarios
        if (item.getSets() <= 0) item.setSets(1);
        if (item.getRepetitions() <= 0) item.setRepetitions(1);
        if (item.getTargetDuration() <= 0) item.setTargetDuration(1);
        if (item.getRestPeriod() < 0) item.setRestPeriod(0); // El descanso puede ser 0
        
        // Asegurar que los índices están dentro de los límites válidos (array es 0-based, pero valores son 1-based)
       // int setsPos = Math.min(Math.max(0, item.getSets() - 1), (editSets.getAdapter() != null ? editSets.getAdapter().getCount() - 1 : 0));
        int repsPos = Math.min(Math.max(0, item.getRepetitions() - 1), (editReps.getAdapter() != null ? editReps.getAdapter().getCount() - 1 : 0));
        int durationPos = Math.min(Math.max(0, item.getTargetDuration() - 1), (editDuration.getAdapter() != null ? editDuration.getAdapter().getCount() - 1 : 0));
        int restPos = Math.min(Math.max(0, item.getRestPeriod() - 1), (editRest.getAdapter() != null ? editRest.getAdapter().getCount() - 1 : 0));
        
        // Registrar los valores que se van a mostrar en los spinners
        Log.d("EDIT_DIALOG", "Valores a mostrar: sets=" + item.getSets() + ", reps=" + 
               item.getRepetitions() + ", duration=" + item.getTargetDuration() + ", rest=" + item.getRestPeriod());
        

        
        // Configurar los spinners con valores actuales
        //editSets.setSelection(setsPos);
        editReps.setSelection(repsPos);
        editDuration.setSelection(durationPos);
        editRest.setSelection(restPos);
        
        // Establecer siempre el tipo de duración como "time"
        item.setDurationType("time");
        Log.d("EDIT_DIALOG", "Modo TEMPORIZADOR establecido por defecto");
        
        Log.d("EDIT_DIALOG", "Spinners configurados correctamente");
        
        // Determinar el tema adecuado según el modo
        int dialogTheme = isNightMode 
                ? R.style.ThemeOverlay_Evolv_Dialog_Night
                : com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert;
                     
        Log.d("EDIT_DIALOG", "Aplicando tema: " + (isNightMode ? "nocturno" : "diurno"));
        
        // Crear y mostrar el diálogo con el tema adecuado
        AlertDialog.Builder builder = new AlertDialog.Builder(this, dialogTheme)
                .setTitle(R.string.edit_exercise_values)
                .setView(dialogView);
        
        // Crear el diálogo primero para establecer adecuadamente el contexto
        AlertDialog dialog = builder.create();
        
        // Configurar los botones después de la creación del diálogo
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), 
                (dialogInterface, which) -> {
                    try {
                        Log.d("EDIT_DIALOG", "Botón OK pulsado");
                        //int newSets = Integer.parseInt(editSets.getSelectedItem().toString());
                        int newReps = Integer.parseInt(editReps.getSelectedItem().toString());
                        int newDuration = Integer.parseInt(editDuration.getSelectedItem().toString());
                        int newRest = Integer.parseInt(editRest.getSelectedItem().toString());
                        
                        // Ya no se usa el modo repeticiones, siempre es tiempo
                        String newDurationType = "time";
                        
                        // Aplicar los nuevos valores 
                        //item.setSets(newSets);
                        // Guardamos repeticiones por compatibilidad pero no se usarán
                        item.setRepetitions(newReps);
                        item.setTargetDuration(newDuration);
                        item.setRestPeriod(newRest);
                        item.setDurationType(newDurationType);
                        

                        Log.d("EDIT_DIALOG", "Nuevos valores guardados: sets=" + item.getSets() + 
                                ", reps=" + item.getRepetitions() + 
                                ", duration=" + item.getTargetDuration() + 
                                ", rest=" + item.getRestPeriod() + 
                                ", durationType=" + item.getDurationType());
                        if (onUpdated != null) onUpdated.run();
                        Log.d("EDIT_DIALOG", "Callback onUpdated ejecutado");
                    } catch (Exception e) {
                        Log.e("EDIT_DIALOG", "ERROR al guardar los valores: " + e.getMessage(), e);
                        Toast.makeText(EditWorkoutTemplateActivity.this, "Error al guardar los valores: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        
        // Agregar botón negativo (Cancelar)
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                (dialogInterface, which) -> {
                    Log.d("EDIT_DIALOG", "Diálogo cancelado");
                    // No hacer nada, simplemente cerrar el diálogo
                });
        
        // Mostrar el diálogo una vez configurados todos los elementos
        dialog.show();
        
        // Registrar la apertura exitosa del diálogo
        Log.d("EDIT_DIALOG", "Diálogo mostrado correctamente");
    }
    
    /**
     * Muestra un diálogo para seleccionar ejercicios de la lista completa
     * y añadirlos a la plantilla de entrenamiento
     */
    private void showExerciseSelectionDialog() {
        // Si aún no se han cargado los ejercicios, cargarlos de la base de datos
        if (allExercises.isEmpty()) {
            allExercises = dbHelper.getAllExercises();
        }
        
        if (allExercises.isEmpty()) {
            Toast.makeText(this, "No hay ejercicios disponibles", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Crear array de nombres de ejercicios para mostrar en el diálogo
        String[] exerciseNames = new String[allExercises.size()];
        for (int i = 0; i < allExercises.size(); i++) {
            exerciseNames[i] = allExercises.get(i).getName();
        }
        
        // Configurar el diálogo
        boolean[] checkedItems = new boolean[exerciseNames.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar ejercicios")
               .setMultiChoiceItems(exerciseNames, checkedItems, (dialog, which, isChecked) -> {
                   // Marcar/desmarcar el ejercicio
                   checkedItems[which] = isChecked;
               })
               .setPositiveButton("Añadir", (dialog, id) -> {
                   // Añadir los ejercicios seleccionados
                   for (int i = 0; i < checkedItems.length; i++) {
                       if (checkedItems[i]) {
                           // Crear un nuevo ejercicio de plantilla
                           WorkoutTemplateExercise_v2 newExercise = new WorkoutTemplateExercise_v2();
                           newExercise.setExerciseId(allExercises.get(i).getExercise_id());
                           newExercise.setName(allExercises.get(i).getName());
                           newExercise.setSets(3); // Valores predeterminados
                           newExercise.setRepetitions(3);
                           newExercise.setTargetDuration(0);
                           newExercise.setRestPeriod(15);
                           
                           selectedExercises.add(newExercise);
                       }
                   }
                   // Notificar al adaptador
                   exerciseAdapter.notifyDataSetChanged();
               })
               .setNegativeButton("Cancelar", (dialog, id) -> {
                   dialog.dismiss();
               });
        
        AlertDialog dialog = builder.create();
        dialog.show();
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
        // Usar templateId de la plantilla actual o -1 si no existe
        long templateIdLocal = (templateId != null) ? templateId : -1;
        android.util.Log.d("CREA", "[saveTemplate][INICIO] templateIdLocal inicializado a: " + templateIdLocal);

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
        
        // Verificar si ya existe un entrenamiento con el mismo nombre para el usuario actual
        boolean isNameDuplicate = false;
        WorkoutTemplate_v2 existingTemplate = null;
        
        android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] INICIANDO VERIFICACIÓN de nombre duplicado: nombre=\"" + templateName + "\", userId=" + currentUserId + ", templateId=" + (templateId != null ? templateId : "null"));
        
        // LOGS ADICIONALES: Verificación previa sobre si ya existe un entrenamiento con ese nombre (usando método directo)
        boolean duplicadoVerificaciónPrevia = dbHelper.existsWorkoutTemplate_v2Name(templateName, currentUserId);
        android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate][PRE-VERIFICACION] existsWorkoutTemplate_v2Name dice que nombre=\"" + templateName + "\" está duplicado: " + duplicadoVerificaciónPrevia);
        
        // Primero, buscar si existe una plantilla con este nombre para este usuario
        existingTemplate = dbHelper.getWorkoutTemplateByNameAndUserId(templateName, currentUserId);
        android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate][RESULTADO-BUSQUEDA] getWorkoutTemplateByNameAndUserId devolvió objeto nulo?: " + (existingTemplate == null));
        
        if (existingTemplate != null) {
            android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] Encontrada plantilla con nombre=\"" + templateName + "\", userId=" + currentUserId + ", templateId=" + existingTemplate.getTemplateId());
            android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate][DETALLE] Objeto encontrado: ID=" + existingTemplate.getTemplateId() + ", nombre=" + existingTemplate.getName() + ", userId=" + existingTemplate.getUserId());
            
            // Si estamos editando, verificar si es la misma plantilla u otra con el mismo nombre
            if (templateId != null && templateId > 0) {
                // Es una edición - verificar si la plantilla existente es diferente a la que estamos editando
                // Añadir logs detallados para diagnosticar el problema de comparación
                android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] COMPARANDO IDs: existingTemplate.getTemplateId()=" + existingTemplate.getTemplateId() + 
                    ", tipo: long, templateId=" + templateId + 
                    ", tipo: " + (templateId == null ? "null" : "Long (objeto)"));

                // Convertir a tipos compatibles para la comparación
                long existingId = existingTemplate.getTemplateId();
                long editingId = (templateId != null) ? templateId : -1L;
                android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] Valores convertidos para comparación: existingId=" + existingId + ", editingId=" + editingId);
                
                if (existingId != editingId) {
                    // Es otra plantilla con el mismo nombre - mostrar diálogo
                    isNameDuplicate = true;
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] Edición: El nombre ya existe en OTRA plantilla (id=" + existingTemplate.getTemplateId() + "), mostrando diálogo.");
                    android.util.Log.d("CREA", "[saveTemplate] Nombre duplicado detectado durante edición. Mostrando diálogo.");
                } else {
                    // Es la misma plantilla que estamos editando - no es duplicado
                    isNameDuplicate = false;
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] Edición: Es la misma plantilla, no es duplicado.");
                }
            } else {
                // Es una creación nueva - siempre será duplicado
                isNameDuplicate = true;
                android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] Creación: El nombre ya existe, mostrando diálogo.");
                android.util.Log.d("CREA", "[saveTemplate] Nombre duplicado detectado durante creación. Mostrando diálogo.");
            }
        } else {
            // No existe duplicado
            android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] No existe ninguna plantilla con nombre=\"" + templateName + "\" para userId=" + currentUserId);
            isNameDuplicate = false;
        }
        
        // VALIDACIÓN REFORZADA: Verificación final antes de continuar
        boolean duplicadoFinal = isNameDuplicate || dbHelper.existsWorkoutTemplate_v2Name(templateName, currentUserId);
        android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate][VALIDACIÓN-FINAL] isNameDuplicate=" + isNameDuplicate + ", verificación adicional=" + dbHelper.existsWorkoutTemplate_v2Name(templateName, currentUserId));
        
        if (duplicadoFinal) {
            android.util.Log.w("CREA", "[saveTemplate][VALIDACION] Ya existe un entrenamiento con ese nombre");
            android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] ¡DUPLICADO DETECTADO! Mostrando diálogo para nombre=\"" + templateName + "\", userId=" + currentUserId);
            
            // Si ya tenemos el objeto existente, usarlo directamente en vez de buscarlo otra vez
            // Si por alguna razón no tenemos el objeto, intentar buscarlo otra vez
            if (existingTemplate == null) {
                android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] No tenemos objeto existente, intentando recuperarlo");
                existingTemplate = dbHelper.getWorkoutTemplateByNameAndUserId(templateName, currentUserId);
            } else {
                android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] Usando objeto existente pasado como parámetro, templateId=" + existingTemplate.getTemplateId());
            }
            
            // Mostrar un diálogo para gestionar duplicados en lugar de simplemente mostrar un Toast
            showNameDuplicateDialog(templateName, workoutType, notes, exercises, existingTemplate);
            android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate][POST-DIALOG] Diálogo de duplicados mostrado y flujo detenido");
            return;
        } else {
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
            
            android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] No hay duplicados, continuando con el guardado normal.");
            
            // Crear plantilla nueva
            android.util.Log.d("SAVE", "[EditWorkoutActivity][saveTemplate] Creando plantilla sin duplicados.");
            
            // Validar y rellenar valores críticos antes de guardar
            android.util.Log.d("CREA", "[saveTemplate] Validando y ajustando valores de ejercicios seleccionados");
            for (WorkoutTemplateExercise_v2 wte : exercises) {
                if (wte.getSets() < 1) wte.setSets(1);
                if (wte.getRepetitions() < 1) wte.setRepetitions(3);
                if (wte.getTargetDuration() < 0) wte.setTargetDuration(10);
                if (wte.getRestPeriod() < 0) wte.setRestPeriod(10);
                
                // Asignar correctamente el tipo de duración basado en la configuración
                // Si las repeticiones > 1, entonces es un ejercicio basado en repeticiones
                // Si no, es un ejercicio basado en tiempo
                if (wte.getRepetitions() > 1) {
                    wte.setDurationType("reps");
                    android.util.Log.d("CREA", "Ejercicio " + wte.getName() + " configurado como tipo 'reps' con " + 
                            wte.getRepetitions() + " repeticiones");
                } else {
                    wte.setDurationType("time");
                    android.util.Log.d("CREA", "Ejercicio " + wte.getName() + " configurado como tipo 'time' con " + 
                            wte.getTargetDuration() + " segundos");
                }
            }
            
            // Como no hay setter para el nombre, creamos una nueva instancia
            WorkoutTemplate_v2 newTemplate;
            
            // Si es una edición de una plantilla existente del usuario actual
            if (templateId != null && templateId > 0 && currentTemplate != null && currentTemplate.getUserId() == currentUserId) {
                android.util.Log.d("CREA", "[saveTemplate][MODO EDICION] Actualizando plantilla existente. templateId: " + templateId);
                android.util.Log.d("CREA", "[saveTemplate][MODO EDICION] userId usado para update: " + currentUserId);
                
                newTemplate = new WorkoutTemplate_v2(
                    templateId,
                    templateName,  // Nuevo nombre
                    workoutType,   // Nuevo tipo
                    notes,         // Nuevas notas
                    (int) currentUserId,
                    exercises
                );
                
                android.util.Log.d("CREA", "[saveTemplate][MODO ACTUALIZACION] Actualizando plantilla: " + templateId);
                dbHelper.updateWorkoutTemplate_v2(newTemplate, currentUserId);
                dbHelper.replaceExercisesForTemplate(templateId, exercises);
                templateIdLocal = templateId;
            } else {
                // Si es plantilla por defecto o nueva, crear una nueva plantilla de usuario
                android.util.Log.d("CREA", "[saveTemplate][MODO CREACION] Insertando nueva plantilla para usuario: " + currentUserId);
                newTemplate = new WorkoutTemplate_v2(0, templateName, workoutType, notes, (int) currentUserId, exercises);
                WorkoutTemplate_v2 insertedTemplate = dbHelper.insertWorkoutTemplate_v2(newTemplate, currentUserId);
                android.util.Log.d("CREA", "[saveTemplate][insertWorkoutTemplate_v2] Resultado: " + (insertedTemplate != null ? insertedTemplate.getTemplateId() : "null"));
                
                if (insertedTemplate != null) {
                    templateIdLocal = insertedTemplate.getTemplateId();
                    dbHelper.replaceExercisesForTemplate(templateIdLocal, exercises);
                }
            }
            
            // Mensaje de éxito y fin de la actividad
            android.util.Log.d("CREA", "[saveTemplate][FIN] Guardado de plantilla y asociación de ejercicios completado. ID: " + templateIdLocal);
            // Crear una copia final de templateIdLocal para usar en el lambda
            final long finalTemplateId = templateIdLocal;
            runOnUiThread(() -> {
                //Toast.makeText(this, finalTemplateId > 0 ? "Plantilla actualizada correctamente" : "Plantilla creada correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }
    }
    

    /**
     * Muestra un diálogo cuando se detecta un nombre duplicado, permitiendo
     * sobreescribir el entrenamiento existente o guardar con un nuevo nombre.
     * 
    /**
     * Muestra un diálogo cuando se detecta un nombre duplicado, permitiendo
     * sobreescribir el entrenamiento existente o guardar con un nuevo nombre.
     * 
     * @param currentName Nombre actual del entrenamiento
     * @param workoutType Tipo de entrenamiento
     * @param notes Notas del entrenamiento
     * @param exercises Lista de ejercicios asociados al entrenamiento
     * @param existingTemplate Plantilla existente con el mismo nombre (opcional, puede ser null)
     */
    private void showNameDuplicateDialog(String currentName, String workoutType, String notes, List<WorkoutTemplateExercise_v2> exercises, WorkoutTemplate_v2 existingTemplate) {
        android.util.Log.d("SAVE", "[EditWorkoutActivity][showNameDuplicateDialog] INICIANDO DIÁLOGO para nombre=\"" + currentName + "\", userId=" + currentUserId);
        android.util.Log.d("SAVE", "[EditWorkoutActivity][showNameDuplicateDialog] Parámetros: workoutType=\"" + workoutType + "\", numExercises=" + (exercises != null ? exercises.size() : 0));
        android.util.Log.d("CREA", "[showNameDuplicateDialog] Mostrando diálogo de nombre duplicado para: " + currentName);
        
        // Crear vista personalizada con campo de texto para el nombre
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_duplicate_workout_name, null);
        
        // Verificar si estamos en modo oscuro para aplicar colores directamente
        boolean isNightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        
        if (isNightMode) {
            // Aplicar colores de modo oscuro directamente a la vista
            dialogView.setBackgroundColor(getResources().getColor(R.color.surface_dark));
            
            // Aplicar colores a todos los TextViews contenidos en el layout
            applyColorToAllTextViews(dialogView, R.color.on_primary);
        }
        
        EditText editNewName = dialogView.findViewById(R.id.editNewWorkoutName);
        editNewName.setText(currentName);
        android.util.Log.d("SAVE", "[EditWorkoutActivity][showNameDuplicateDialog] Vista del diálogo inicializada con nombre=\"" + currentName + "\"");
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Entrenamiento duplicado")
            .setMessage("El entrenamiento ya existe. ¿Desea reemplazarlo o guardar una copia con otro nombre?")
            .setView(dialogView)
            .setPositiveButton("Reemplazar", (dialogInterface, which) -> {
                android.util.Log.d("CREA", "[showNameDuplicateDialog] Usuario eligió sobreescribir entrenamiento existente");
                
                // Usar el entrenamiento existente si ya lo tenemos o buscar uno nuevo
                WorkoutTemplate_v2 templateToUpdate = existingTemplate;
                if (templateToUpdate == null) {
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] No tenemos objeto existente, intentando recuperarlo");
                    templateToUpdate = dbHelper.getWorkoutTemplateByNameAndUserId(currentName, currentUserId);
                } else {
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] Usando objeto existente pasado como parámetro, templateId=" + templateToUpdate.getTemplateId());
                }
                
                if (templateToUpdate != null) {
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] ENCONTRADO entrenamiento con ID: " + templateToUpdate.getTemplateId() + ", nombre=\"" + templateToUpdate.getName() + "\"");
                    android.util.Log.d("CREA", "[showNameDuplicateDialog] Entrenamiento encontrado con ID: " + templateToUpdate.getTemplateId());
                    
                    // Crear una nueva instancia con los mismos datos pero actualizados (ya que name es inmutable)
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] Actualizando workoutType=\"" + workoutType + "\" y notes=\"" + notes + "\"");
                    
                    WorkoutTemplate_v2 updatedTemplate = new WorkoutTemplate_v2(
                        templateToUpdate.getTemplateId(),
                        templateToUpdate.getName(),  // Mantenemos el mismo nombre (no podemos modificarlo directamente)
                        workoutType,                // Nuevo tipo
                        notes,                      // Nuevas notas
                        (int) currentUserId,
                        exercises
                    );
                    
                    // Actualizar en la base de datos
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] Llamando a updateWorkoutTemplate_v2 con templateId=" + updatedTemplate.getTemplateId() + ", userId=" + currentUserId);
                    dbHelper.updateWorkoutTemplate_v2(updatedTemplate, currentUserId);
                    
                    // Reemplazar los ejercicios asociados
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] Reemplazando " + exercises.size() + " ejercicios para templateId=" + templateToUpdate.getTemplateId());
                    
                    dbHelper.replaceExercisesForTemplate(templateToUpdate.getTemplateId(), exercises);
                    
                    // Mostrar mensaje y cerrar
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][sobreescribir] Sobreescritura COMPLETADA con éxito para templateId=" + templateToUpdate.getTemplateId());
                    Toast.makeText(this, "Plantilla actualizada correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    // Por alguna razón no se encontró, mostrar error
                    android.util.Log.e("SAVE", "[EditWorkoutActivity][sobreescribir] ERROR: No se encontró el entrenamiento a sobreescribir con nombre=\"" + currentName + "\", userId=" + currentUserId);
                    android.util.Log.e("CREA", "[showNameDuplicateDialog] No se encontró el entrenamiento a sobreescribir");
                    Toast.makeText(this, "Error al actualizar la plantilla", Toast.LENGTH_SHORT).show();
                }
            })
            .setNeutralButton("Guardar como...", null) // Se maneja después para evitar cierre automático
            .setNegativeButton("Cancelar", null)
            .create();
        
        dialog.show();
        
        // Reemplazar el comportamiento del botón "Guardar con nuevo nombre" para validar y evitar cierre automático
        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if (neutralButton != null) {
            neutralButton.setOnClickListener(v -> {
                // Obtener el nuevo nombre del campo de texto
                String newName = editNewName.getText().toString().trim();
                
                // Validar que no esté vacío
                android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] Validando nuevo nombre=\"" + newName + "\"");
                if (newName.isEmpty()) {
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] ERROR: Nombre vacío");
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Verificar si el nuevo nombre también está duplicado
                android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] Verificando si el nuevo nombre=\"" + newName + "\" está duplicado para userId=" + currentUserId);
                if (newName.equals(currentName)) {
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] ERROR: Nuevo nombre igual al actual");
                    Toast.makeText(this, "El nuevo nombre debe ser diferente al actual", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (dbHelper.existsWorkoutTemplate_v2Name(newName, currentUserId)) {
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] ERROR: Nuevo nombre también está duplicado");
                    Toast.makeText(this, "El nombre sigue estando duplicado, por favor elige otro", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] Creando plantilla con nuevo nombre=\"" + newName + "\"");
                android.util.Log.d("CREA", "[showNameDuplicateDialog] Guardando con nuevo nombre: " + newName);
                
                // Crear nuevo template con el nombre modificado
                WorkoutTemplate_v2 newTemplate = new WorkoutTemplate_v2(
                    0, newName, workoutType, notes, (int) currentUserId, exercises);
                android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] Objeto creado con userId=" + currentUserId + ", exercises=" + exercises.size());
                    
                // Guardar en base de datos
                android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] Llamando a insertWorkoutTemplate_v2");
                WorkoutTemplate_v2 inserted = dbHelper.insertWorkoutTemplate_v2(newTemplate, currentUserId);
                if (inserted != null) {
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] Plantilla insertada con éxito, nuevo templateId=" + inserted.getTemplateId());
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] Reemplazando " + exercises.size() + " ejercicios para templateId=" + inserted.getTemplateId());
                    dbHelper.replaceExercisesForTemplate(inserted.getTemplateId(), exercises);
                    android.util.Log.d("SAVE", "[EditWorkoutActivity][nuevoNombre] GUARDADO COMPLETADO con nuevo nombre");
                    Toast.makeText(this, "Plantilla guardada con nuevo nombre", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    android.util.Log.e("SAVE", "[EditWorkoutActivity][nuevoNombre] ERROR: La plantilla no pudo ser insertada");
                    Toast.makeText(this, "Error al guardar la plantilla", Toast.LENGTH_SHORT).show();
                }
                
                dialog.dismiss();
            });
        }
    }
}

// ... (rest of the code remains the same)

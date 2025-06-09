package com.example.evolv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evolv.R;
import com.example.evolv.models.WorkoutTemplateExercise_v2;
import com.example.evolv.models.WorkoutTemplate_v2;

public class WorkoutPlayerActivity_v2 extends AppCompatActivity {
    // Estado principal
    private CountDownTimer exerciseTimer;
    private int exerciseIndex = 0;
    private int setIndex = 0;
    private int repIndex = 0;
    private boolean isPaused = false;
    private long millisLeft = 0L;

    // Referencias UI
    private ProgressBar progressBar;
    private TextView textTimerCenter;
    private TextView textExerciseDescription;
    private ImageView imageExercise;
    private Button btnPause, btnNext, btnPrev, btnFinish;
    private android.os.Handler finishHandler;
    private Runnable finishRunnable;
    private Runnable hideDescriptionRunnable;

    // Plantilla de rutina (asegúrate de inicializarla correctamente)
    private WorkoutTemplate_v2 template;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // REP: Log de datos recibidos por Intent
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("workout_template_id")) {
                int templateId = intent.getIntExtra("workout_template_id", -1);
                Log.d("REP", "[WorkoutPlayer] Intent recibido: workout_template_id = " + templateId);
            }
            if (intent.hasExtra("workout_template")) {
                Object obj = intent.getSerializableExtra("workout_template");
                Log.d("REP", "[WorkoutPlayer] Intent recibido: workout_template (serializable): " + (obj != null ? obj.getClass().getSimpleName() : "null"));
            }
        } else {
            Log.d("REP", "[WorkoutPlayer] Intent es null");
        }

        // Logs detallados para diagnóstico de la deserialización
        Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: Verificando intent y extras");
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            Bundle extras = receivedIntent.getExtras();
            if (extras != null) {
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: Extras recibidos: " + extras.keySet());
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: RESUME=" + extras.getBoolean("RESUME"));
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: TEMPLATE_ID=" + extras.getInt("TEMPLATE_ID"));
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: USER_ID=" + extras.getInt("USER_ID"));
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: Contiene 'template'=" + extras.containsKey("template"));
            } else {
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: No hay extras en el intent");
            }
        }
        
        // Recupera el objeto completo con try-catch para capturar errores de deserialización
        try {
            Object rawTemplate = getIntent().getSerializableExtra("template");
            Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: rawTemplate=" + 
                  (rawTemplate != null ? rawTemplate.getClass().getName() : "null"));
            
            template = (WorkoutTemplate_v2) rawTemplate;
            Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: Cast exitoso a WorkoutTemplate_v2");
        } catch (Exception e) {
            Log.e("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: Error deserializando template", e);
            Toast.makeText(this, "Error: Problema al cargar la plantilla: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // REP: Log de plantilla recibida y validación ANTES de cualquier uso
        Log.d("REP", "[WorkoutPlayer] template = " + (template != null ? template.getName() : "null"));
        if (template == null) {
            Log.d("REP", "[WorkoutPlayer] ERROR: template es null. No se puede continuar.");
            Toast.makeText(this, "Error: No se pudo cargar la plantilla de entrenamiento.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // REP: Log de ejercicios en plantilla
        Log.d("REP", "[WorkoutPlayer] template.getExercises() = " + (template.getExercises() != null ? template.getExercises().size() : "null"));
        if (template.getExercises() == null || template.getExercises().isEmpty()) {
            Log.d("REP", "[WorkoutPlayer] ERROR: La plantilla no tiene ejercicios.");
            Toast.makeText(this, "No se pudo cargar la plantilla de entrenamiento.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d("EvolvDebug", "[REP][PLAYER] Ejercicios recibidos: " + template.getExercises().size()); // REP
        for (com.example.evolv.models.WorkoutTemplateExercise_v2 wte : template.getExercises()) {
            Log.d("EvolvDebug", "[REP][PLAYER] " + wte.getName() + " / " + wte.getImg_url());
        }

        setContentView(R.layout.activity_workout_player_v2);

        // Inicialización de vistas
        progressBar = findViewById(R.id.progressBar);
        textTimerCenter = findViewById(R.id.textTimerCenter);
        textExerciseDescription = findViewById(R.id.textExerciseDescription);
        imageExercise = findViewById(R.id.imageExercise);
        btnPause = findViewById(R.id.buttonPauseResume);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnFinish = findViewById(R.id.btnFinish);
        finishHandler = new android.os.Handler();
        finishRunnable = () -> finish();
        hideDescriptionRunnable = () -> {
            if (textExerciseDescription != null) {
                textExerciseDescription.setVisibility(View.GONE);
            }
        };

        // Listeners
        if (btnPause != null) btnPause.setOnClickListener(v -> togglePauseResume());
        if (btnNext != null) btnNext.setOnClickListener(v -> avanzar());
        if (btnPrev != null) btnPrev.setOnClickListener(v -> retroceder());
        
        // Listener para la imagen: muestra la descripción al hacer clic
        if (imageExercise != null) {
            imageExercise.setOnClickListener(v -> {
                showExerciseDescription();
            });
        }
        if (btnFinish != null) btnFinish.setOnClickListener(v -> {
            if (finishHandler != null) finishHandler.removeCallbacks(finishRunnable);
            
            Log.d("EvolvDebug", "[FINISH] Botón Finalizar pulsado");
            
            // Al salir por el botón Finalizar, realizamos todas las acciones de finalización
            if (template != null) {
                // 0. PRIMERO: Asegurar que no está en estado pausado en todas partes
                // Variable local
                isPaused = false;
                
                // Obtener userId correcto para operaciones de SharedPreferences
                int userIdToUse = template.getUserId();
                if (template.isDefault()) {
                    long currentUserId = getIntent().getLongExtra("USER_ID", 0);
                    userIdToUse = (int)((currentUserId == 0) ? 0 : currentUserId);
                    Log.d("EvolvDebug", "[FINISH] Utilizando userIdToUse=" + userIdToUse + " para limpiar estado pausado");
                }
                
                // 1. Eliminar PRIMERO el estado de pausa en SharedPreferences
                Log.d("EvolvDebug", "[FINISH] Eliminando estado pausado en SharedPreferences");
                com.example.evolv.utils.SessionProgressManager_v2.setPausedState(
                    this, userIdToUse, template.getTemplateId(), false);
                
                // 2. Modificar el objeto template
                template.setPaused(false);
                
                // 3. Eliminar progreso de la sesión
                com.example.evolv.utils.SessionProgressManager_v2.clearProgress(
                    this, template.getUserId(), template.getTemplateId());
                
                // 4. Marcar el entrenamiento como completado en la BD
                com.example.evolv.DatabaseHelper dbHelper = new com.example.evolv.DatabaseHelper(this);
                
                // Obtener la fecha actual
                String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", 
                        java.util.Locale.getDefault()).format(new java.util.Date());
                
                long updatedWorkoutId = dbHelper.markWorkoutCompletedByDate(
                    template.getUserId(), currentDate);
                
                if (updatedWorkoutId != -1) {
                    Toast.makeText(this, R.string.workout_completed, Toast.LENGTH_SHORT).show();
                    Log.d("EvolvDebug", "[FINISH] Entrenamiento marcado como completado en BD, workoutId=" + updatedWorkoutId);
                } else {
                    Log.d("EvolvDebug", "[FINISH] No se encontró entrenamiento para marcar como completado");
                }
                
                // Verificación final del estado de pausa
                boolean isPausedVerify = com.example.evolv.utils.SessionProgressManager_v2.isPaused(
                    this, userIdToUse, template.getTemplateId());
                Log.d("EvolvDebug", "[FINISH] ¿Se ha eliminado el estado pausado correctamente? isPaused=" + isPausedVerify);
            }
            finish();
        });
        
        // Si se está reanudando una rutina pausada, cargar el progreso
        boolean resumeSession = getIntent().getBooleanExtra("RESUME", false);
        if (resumeSession && template != null) {
            Log.d("EvolvDebug", "[WorkoutPlayer] Intentando reanudar sesión pausada");
            com.example.evolv.utils.SessionProgressManager_v2.Progress progress = 
                com.example.evolv.utils.SessionProgressManager_v2.getProgress(
                    this, template.getUserId(), template.getTemplateId());
                    
            if (progress != null) {
                Log.d("EvolvDebug", "[WorkoutPlayer] Progreso encontrado: exerciseIndex=" + 
                      progress.exerciseIndex + ", setIndex=" + progress.setIndex + ", repIndex=" + progress.repIndex);
                      
                // Restaurar el estado exacto donde quedó el usuario
                this.exerciseIndex = progress.exerciseIndex;
                this.setIndex = progress.setIndex;
                this.repIndex = progress.repIndex;
                
                Toast.makeText(this, "Retomando entrenamiento pausado", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("EvolvDebug", "[WorkoutPlayer] No se encontró progreso guardado");
            }
        }
        
        updateExerciseDisplay();
    }

    // Obtén la plantilla desde el Intent o donde corresponda
    private WorkoutTemplate_v2 obtenerTemplateDesdeIntent() {
        // TODO: Recupera la plantilla real según tu lógica de paso de datos entre actividades
        // Por ejemplo: return (WorkoutTemplate_v2) getIntent().getSerializableExtra("template");
        // return new WorkoutTemplate_v2();
        return (WorkoutTemplate_v2) getIntent().getSerializableExtra("template");
    }

    // Avanza a la siguiente repetición, set o ejercicio
    private void avanzar() {
        Log.d("DEPURACION", "[avanzar][ANTES] ejercicioIndex=" + exerciseIndex);
        if (template == null || template.getExercises() == null) return;

        if (exerciseIndex < template.getExercises().size() - 1) {
            exerciseIndex++;
            setIndex = 0;
            repIndex = 0;
            Log.d("DEPURACION", "[avanzar][DESPUES] ejercicioIndex=" + exerciseIndex);
            updateExerciseDisplay();
        } else {
            // Estado Final: incrementar el índice y lanzar la lógica de finalización
            Log.d("DEPURACION", "[avanzar] Último ejercicio alcanzado: Estado Final");
            exerciseIndex++; // <--- ESTE INCREMENTO ES CLAVE
            advanceToNextExercise();
        }
    }

    // Retrocede a la repetición, set o ejercicio anterior
    private void retroceder() {
        Log.d("DEPURACION", "[retroceder][ANTES] ejercicioIndex=" + exerciseIndex);
        if (template == null || template.getExercises() == null) return;

        if (exerciseIndex > 0) {
            exerciseIndex--;
            setIndex = 0;
            repIndex = 0;
            Log.d("DEPURACION", "[retroceder][DESPUES] ejercicioIndex=" + exerciseIndex);
            updateExerciseDisplay();
        } else {
            // Si ya es el primer ejercicio, puedes mostrar un mensaje o hacer nada
            Log.d("DEPURACION", "[retroceder] Primer ejercicio alcanzado");
        }
    }

    // Lógica para avanzar y finalizar sesión si corresponde
    private void advanceToNextExercise() {
        Log.d("DEPURACION", "[advanceToNextExercise][ANTES] ejercicioIndex=" + exerciseIndex + ", setIndex=" + setIndex + ", repIndex=" + repIndex);
        if (template != null && template.getExercises() != null && exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
            Log.d("DEPURACION", "[advanceToNextExercise][ANTES] sets=" + currentExercise.getSets() + ", reps=" + currentExercise.getRepetitions());
        }
        if (exerciseIndex < template.getExercises().size()) {
            updateExerciseDisplay();
            Log.d("DEPURACION", "[advanceToNextExercise][DESPUES] ejercicioIndex=" + exerciseIndex + ", setIndex=" + setIndex + ", repIndex=" + repIndex);
        } else {
            // Detener el temporizador para evitar que siga actualizando la UI
            if (exerciseTimer != null) {
                exerciseTimer.cancel();
                exerciseTimer = null;
            }

            textTimerCenter.setText("¡Fin!");
            progressBar.setProgress(0);
            progressBar.setVisibility(View.INVISIBLE);

            // Ocultar el contenedor de los botones de navegación (Anterior, Pausar, Siguiente)
            findViewById(R.id.navigationButtonsContainer).setVisibility(View.GONE);
            
            if (btnNext != null) btnNext.setEnabled(false);
            if (btnPause != null) btnPause.setEnabled(false);
            if (btnFinish != null) {
                btnFinish.setVisibility(View.VISIBLE);
                btnFinish.setEnabled(true);
            }
            // Programar cierre automático en 3 segundos
            if (finishHandler != null && finishRunnable != null) {
                finishHandler.postDelayed(finishRunnable, 3000);
            }
        }
    }

    // Actualiza la UI para mostrar el ejercicio actual
    private void updateExerciseDisplay() {
        Log.d("DEPURACION", "[updateExerciseDisplay][ANTES] ejercicioIndex=" + exerciseIndex + ", setIndex=" + setIndex + ", repIndex=" + repIndex);
        if (template != null && template.getExercises() != null && exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
            Log.d("DEPURACION", "[updateExerciseDisplay][ANTES] sets=" + currentExercise.getSets() + ", reps=" + currentExercise.getRepetitions());
        }
        Log.d("REP", "updateExerciseDisplay: ejercicioIndex=" + exerciseIndex + ", setIndex=" + setIndex + ", repIndex=" + repIndex);
        if (repIndex == 0 && setIndex == 0) {
            showExerciseIntroPanel();
        } else {
            updateExerciseDisplayCore();
        }
        Log.d("DEPURACION", "[updateExerciseDisplay][DESPUES] ejercicioIndex=" + exerciseIndex + ", setIndex=" + setIndex + ", repIndex=" + repIndex);
        if (template != null && template.getExercises() != null && exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
            Log.d("DEPURACION", "[updateExerciseDisplay][DESPUES] sets=" + currentExercise.getSets() + ", reps=" + currentExercise.getRepetitions());
        }
    }

    // Panel de introducción al ejercicio
    private void showExerciseIntroPanel() {
        Log.d("REP", "showExerciseIntroPanel: ejercicioIndex=" + exerciseIndex + ", setIndex=" + setIndex + ", repIndex=" + repIndex);

        if (template == null || template.getExercises() == null || template.getExercises().isEmpty()) {
            Log.e("EVOLV-DEBUG", "Trying to access exercises but list is empty or null");
            return;
        }

        TextView textIntroTitle = findViewById(R.id.textIntroTitle);
        TextView textIntroExerciseName = findViewById(R.id.textIntroExerciseName);
        TextView textIntroExerciseDesc = findViewById(R.id.textIntroExerciseDesc);

        WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);

        // Buscar la descripción en tu lista de ejercicios base si la tienes
        String descripcion = currentExercise.getName(); // Placeholder
        textIntroExerciseName.setText(currentExercise.getName());
        textIntroExerciseDesc.setText(descripcion);

        Log.d("REP", "showExerciseIntroPanel: llamando a updateExerciseDisplayCore()");
        updateExerciseDisplayCore();
    }

    // Lógica principal de actualización de la UI del ejercicio
    private void updateExerciseDisplayCore() {
        Log.d("DEPURACION", "[updateExerciseDisplayCore][ANTES] ejercicioIndex=" + exerciseIndex + ", setIndex=" + setIndex + ", repIndex=" + repIndex);
        if (template != null && template.getExercises() != null && exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
            Log.d("DEPURACION", "[updateExerciseDisplayCore][ANTES] sets=" + currentExercise.getSets() + ", reps=" + currentExercise.getRepetitions());
        }
        // Cancelar temporizador anterior si existe
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
        }
        Log.d("EvolvDebug", "[REP][UI] updateExerciseDisplayCore ejecutado para index: " + exerciseIndex); // REP
        WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
        Log.d("EvolvDebug", "[REP][UI] Mostrando: " + currentExercise.getName() + " / " + currentExercise.getImg_url()); // REP

        // Cargar imagen del ejercicio
        ImageView imageExercise = findViewById(R.id.imageExercise);
        if (currentExercise.getImg_url() != null) {
            int resId = getResources().getIdentifier(
                    currentExercise.getImg_url(), "drawable", getPackageName());
            if (resId != 0) {
                imageExercise.setImageResource(resId);
            } else {
                imageExercise.setImageResource(R.drawable.ic_exercise_placeholder); // fallback
            }
        } else {
            imageExercise.setImageResource(R.drawable.ic_exercise_placeholder); // fallback
        }

        TextView textProgress = findViewById(R.id.textProgress);
        String progreso = String.format(
                "Ejercicio %d/%d, Set %d/%d, Rep %d/%d",
                exerciseIndex + 1,
                template.getExercises().size(),
                setIndex + 1,
                template.getExercises().get(exerciseIndex).getSets(),
                repIndex + 1,
                template.getExercises().get(exerciseIndex).getRepetitions()
        );
        textProgress.setText(progreso);

        // Actualiza los textos, progresos, etc.
        textTimerCenter.setText(String.valueOf(currentExercise.getDuration()));
        progressBar.setMax(currentExercise.getDuration());
        progressBar.setProgress(currentExercise.getDuration());

        // Inicia el temporizador para el ejercicio actual
        startExerciseCountdown(currentExercise.getDuration() * 1000L, btnNext, true);
        Log.d("DEPURACION", "[updateExerciseDisplayCore][DESPUES] ejercicioIndex=" + exerciseIndex + ", setIndex=" + setIndex + ", repIndex=" + repIndex);
        if (template != null && template.getExercises() != null && exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 currentExercise2 = template.getExercises().get(exerciseIndex);
            Log.d("DEPURACION", "[updateExerciseDisplayCore][DESPUES] sets=" + currentExercise2.getSets() + ", reps=" + currentExercise2.getRepetitions());
        }
    }

    // Pausa y reanuda
    private void togglePauseResume() {
        if (isPaused) {
            resumeTimer();
        } else {
            pauseTimer();
        }
    }

    private void pauseTimer() {
        isPaused = true;
        if (btnPause != null) btnPause.setText("Reanudar");
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
        }
        progressBar.setProgress(progressBar.getProgress());
        progressBar.setVisibility(View.INVISIBLE);
        
        // Guardar inmediatamente el progreso cuando el usuario pausa explícitamente
        Log.d("EvolvDebug", "[PAUSE] Usuario ha pulsado botón de pausa, guardando progreso...");
        saveCurrentProgress();
        
        // Informar al usuario que su progreso se ha guardado
        Toast.makeText(this, "Entrenamiento pausado", Toast.LENGTH_SHORT).show();
    }

    private void resumeTimer() {
        // Cambiar el estado local de pausa
        isPaused = false;
        progressBar.setVisibility(View.VISIBLE);
        if (btnPause != null) btnPause.setText("Pausar");
        
        // NUEVO: Actualizar también el estado en el objeto template y en SharedPreferences
        if (template != null) {
            template.setPaused(false);
            
            // Obtener el userId correcto (igual que en saveCurrentProgress)
            int userIdToUse = template.getUserId();
            if (template.isDefault()) {
                long currentUserId = getIntent().getLongExtra("USER_ID", 0);
                userIdToUse = (int)((currentUserId == 0) ? 0 : currentUserId);
            }
            
            // Limpiar el estado de pausa en SharedPreferences
            Log.d("EvolvDebug", "[PAUSE] resumeTimer: Limpiando estado pausado para userId=" + 
                  userIdToUse + ", templateId=" + template.getTemplateId());
                  
            com.example.evolv.utils.SessionProgressManager_v2.setPausedState(
                this, userIdToUse, template.getTemplateId(), false);
        }
        
        startExerciseCountdown(millisLeft, btnNext, true);
    }

    // Inicia o reanuda el temporizador del ejercicio
    private void startExerciseCountdown(long millis, Button btnNext, boolean avanzarAlFinal) {
        millisLeft = millis;
        exerciseTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisLeft = millisUntilFinished;
                textTimerCenter.setText(String.valueOf(millisUntilFinished / 1000));
                progressBar.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                textTimerCenter.setText("0");
                progressBar.setProgress(0);
                if (btnNext != null) btnNext.setEnabled(true);
                if (avanzarAlFinal) avanzar();
            }
        };
        if (!isPaused) exerciseTimer.start();
    }

    // Guardar progreso cuando la actividad pierde foco
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("EvolvDebug", "[WorkoutPlayer] onPause() llamado");
        // Solo guardamos el progreso si no estamos finalizando la actividad
        if (!isFinishing()) {
            saveCurrentProgress();
        }
    }

    // Guardar progreso cuando la actividad se detiene (usuario sale completamente)
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("EvolvDebug", "[WorkoutPlayer] onStop() llamado");
        // Solo guardamos el progreso si no estamos finalizando la actividad
        if (!isFinishing()) {
            saveCurrentProgress();
            // Marcar la plantilla como pausada
            if (template != null) {
                template.setPaused(true);
            }
        }
    }
    
    /**
     * Guarda el progreso actual de la sesión.
     * Será recuperado cuando el usuario vuelva a abrir la plantilla.
     */
    private void saveCurrentProgress() {
        try {
            if (template != null && template.getExercises() != null && exerciseIndex < template.getExercises().size()) {
                Log.d("EvolvDebug", "[PAUSE] WorkoutPlayer.saveCurrentProgress(): exerciseIndex=" + exerciseIndex + 
                        ", setIndex=" + setIndex + ", repIndex=" + repIndex + ", userId=" + template.getUserId() + 
                        ", templateId=" + template.getTemplateId());
                
                // Guardar el progreso
                com.example.evolv.utils.SessionProgressManager_v2.saveProgress(
                    this, template.getUserId(), template.getTemplateId(),
                    exerciseIndex, setIndex, repIndex);
                    
                // Marcar la plantilla como pausada (en memoria y en SharedPreferences)
                template.setPaused(true);
                
                // Obtener el ID de usuario actual del intent
                long currentUserId = getIntent().getLongExtra("USER_ID", 0);
                
                // Para plantillas predeterminadas (userId = -1), usamos el ID del usuario actual
                // para evitar que los estados pausados se compartan entre usuarios
                int userIdToUse = template.getUserId();
                if (template.isDefault()) {
                    // Si es usuario anónimo (userId = 0), usamos 0 como userId
                    // Si es usuario registrado, usamos su ID
                    userIdToUse = (currentUserId == 0) ? 0 : (int)currentUserId;
                    Log.d("EvolvDebug", "[PAUSE] Guardando estado pausado de plantilla PREDETERMINADA con userIdToUse=" + userIdToUse + " (currentUserId=" + currentUserId + ")");
                    
                    // Limpiar posibles estados duplicados (para evitar que otros usuarios vean este estado pausado)
                    // Solo para plantillas predeterminadas, eliminamos estados pausados con otras IDs de usuario
                    if (userIdToUse == 0) {
                        // Si es usuario anónimo, eliminar posibles estados de usuarios registrados
                        com.example.evolv.utils.SessionProgressManager_v2.cleanupOtherPausedStates(this, userIdToUse, template.getTemplateId());
                    } else {
                        // Si es usuario registrado, eliminar posible estado de usuario anónimo
                        com.example.evolv.utils.SessionProgressManager_v2.cleanupOtherPausedStates(this, 0, template.getTemplateId());
                    }
                }
                
                Log.d("EvolvDebug", "[PAUSE] Llamando a setPausedState con userId=" + userIdToUse + 
                       " (original=" + template.getUserId() + "), templateId=" + template.getTemplateId() + ", paused=true");
                       
                com.example.evolv.utils.SessionProgressManager_v2.setPausedState(
                    this, userIdToUse, template.getTemplateId(), true);
                    
                // Verificar inmediatamente que se haya guardado correctamente
                boolean isPausedVerify = com.example.evolv.utils.SessionProgressManager_v2.isPaused(
                    this, userIdToUse, template.getTemplateId());
                    
                Log.d("EvolvDebug", "[PAUSE] Verificación después de guardar: isPaused=" + isPausedVerify + 
                      ", userId usado=" + userIdToUse);
                Log.d("EvolvDebug", "[PAUSE] Sesión marcada como pausada para templateId=" + template.getTemplateId());
            } else {
                Log.e("EvolvDebug", "[PAUSE] ERROR: No se pudo guardar progreso, template=" + template + 
                      ", exerciseIndex=" + exerciseIndex);
            }
        } catch (Exception e) {
            Log.e("EvolvDebug", "[PAUSE] Error guardando progreso", e);
            e.printStackTrace();
        }
    }
    
    // Limpieza de recursos si la actividad se destruye
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Cancelar temporizador si existe
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
        }
        
        // Eliminar callbacks pendientes
        if (finishHandler != null && finishRunnable != null) {
            finishHandler.removeCallbacks(finishRunnable);
        }
        
        // Si estamos finalizando la actividad y no por cambio de configuración,
        // limpiar el estado de pausa para evitar que quede marcado como pausado
        if (isFinishing() && template != null) {
            Log.d("EvolvDebug", "[CLEANUP] Finalizando actividad, verificando si debemos limpiar estado de pausa");
            
            // Obtener userId correcto
            int userIdToUse = template.getUserId();
            if (template.isDefault()) {
                long currentUserId = getIntent().getLongExtra("USER_ID", 0);
                userIdToUse = (int)((currentUserId == 0) ? 0 : currentUserId);
            }
            
            // Verificar si el entrenamiento estaba marcado como completado antes de limpiar
            com.example.evolv.DatabaseHelper dbHelper = new com.example.evolv.DatabaseHelper(this);
            String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            
            // Asumimos que si estamos finalizando, el workout se ha completado
            // Por lo tanto, debemos eliminar cualquier estado de pausa
            Log.d("EvolvDebug", "[CLEANUP] Limpiando estado de pausa en onDestroy() para userId=" + userIdToUse + ", templateId=" + template.getTemplateId());
            
            com.example.evolv.utils.SessionProgressManager_v2.setPausedState(
                this, userIdToUse, template.getTemplateId(), false);
                
            // También actualizamos el objeto template
            template.setPaused(false);
            
            // Verificar que se haya limpiado el estado
            boolean stillPaused = com.example.evolv.utils.SessionProgressManager_v2.isPaused(
                this, userIdToUse, template.getTemplateId());
                
            Log.d("EvolvDebug", "[CLEANUP] Verificación después de limpiar: isPaused=" + stillPaused);
        }
    }
    
    /**
     * Muestra la descripción del ejercicio actual por un tiempo limitado
     * al tocar la imagen del ejercicio.
     */
    private void showExerciseDescription() {
        // Asegurarse de que tengamos un ejercicio válido
        if (template == null || template.getExercises() == null || 
            exerciseIndex >= template.getExercises().size() || 
            textExerciseDescription == null) {
            return;
        }
        
        // Cancelar cualquier ocultamiento pendiente
        if (finishHandler != null && hideDescriptionRunnable != null) {
            finishHandler.removeCallbacks(hideDescriptionRunnable);
        }
        
        // Obtener el ID del ejercicio actual
        WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
        long exerciseId = currentExercise.getExerciseId();
        
        // Obtener la descripción desde la base de datos
        com.example.evolv.DatabaseHelper dbHelper = new com.example.evolv.DatabaseHelper(this);
        String description = dbHelper.getExerciseDescription(exerciseId);
        
        // Si la descripción está vacía, mostrar un mensaje alternativo
        if (description == null || description.trim().isEmpty()) {
            description = "No hay descripción disponible para este ejercicio";
        }
        
        // Mostrar la descripción
        textExerciseDescription.setText(description);
        textExerciseDescription.setVisibility(View.VISIBLE);
        
        // Configurar un temporizador para ocultar la descripción después de 3 segundos
        finishHandler.postDelayed(hideDescriptionRunnable, 3000);
        
        // Log para debugging
        Log.d("EvolvDebug", "[DESCRIPTION] Mostrando descripción del ejercicio " + currentExercise.getName() + ": " + 
              description.substring(0, Math.min(50, description.length())) + "...");
    }
}

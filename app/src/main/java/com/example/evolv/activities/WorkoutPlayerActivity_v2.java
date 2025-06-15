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
import com.example.evolv.utils.EnhancedSoundManager;

public class WorkoutPlayerActivity_v2 extends AppCompatActivity {
    // Estados posibles del reproductor de entrenamiento
    private enum WorkoutState {
        EXERCISE,   // Mostrando ejercicio actual
        REST,       // En período de descanso entre ejercicios
        FINISHED    // Entrenamiento finalizado
    }

    // Estado principal
    private CountDownTimer exerciseTimer;
    private CountDownTimer restTimer;
    private int exerciseIndex = 0;
    private int repIndex = 0;
    private boolean isPaused = false;
    private long millisLeft = 0L;
    private WorkoutState currentState = WorkoutState.EXERCISE;

    // Referencias UI - Pantalla de ejercicio
    private ProgressBar progressBar;
    private TextView textTimerCenter;
    private TextView textExerciseDescription;
    private ImageView imageExercise;
    private Button btnPause, btnNext, btnPrev, btnFinish;
    private android.os.Handler finishHandler;
    private Runnable finishRunnable;
    private Runnable hideDescriptionRunnable;

    // Referencias UI - Pantalla de descanso
    private View restPeriodView;
    private TextView textRestTimer;
    private ImageView imageNextExercise;
    private TextView textNextExerciseName;
    private TextView textNextExerciseDescription;
    private Button btnSkipRest;
    private android.os.Handler hideRestDescriptionHandler;
    private Runnable hideRestDescriptionRunnable;

    // Gestor de sonidos para notificaciones
    private EnhancedSoundManager soundManager;

    // Plantilla de rutina (asegúrate de inicializarla correctamente)
    private WorkoutTemplate_v2 template;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TIEMPO_EJERCICIO", "[onCreate] Iniciando WorkoutPlayerActivity_v2");

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
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: TEMPLATE_ID=" + extras.getLong("TEMPLATE_ID"));
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: USER_ID=" + extras.getLong("USER_ID"));
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: Contiene 'template'=" + extras.containsKey("template"));
            } else {
                Log.d("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: No hay extras en el intent");
            }
        }

        // Recupera el objeto completo con try-catch para capturar errores de deserialización
        try {
            Object rawTemplate = getIntent().getSerializableExtra("template");
            Log.d("REP", "[DIAG] WorkoutPlayer.onCreate: rawTemplate=" +
                    (rawTemplate != null ? rawTemplate.getClass().getName() : "null"));

            template = (WorkoutTemplate_v2) rawTemplate;
            Log.d("REP", "[DIAG] WorkoutPlayer.onCreate: Cast exitoso a WorkoutTemplate_v2");
        } catch (Exception e) {
            Log.e("EvolvDebug", "[DIAG] WorkoutPlayer.onCreate: Error deserializando template", e);
            //Toast.makeText(this, "Error: Problema al cargar la plantilla: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // REP: Log de plantilla recibida y validación ANTES de cualquier uso
        Log.d("REP", "[WorkoutPlayer] template = " + (template != null ? template.getName() : "null"));
        if (template == null) {
            Log.d("REP", "[WorkoutPlayer] ERROR: template es null. No se puede continuar.");
            //Toast.makeText(this, "Error: No se pudo cargar la plantilla de entrenamiento.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // REP: Log de ejercicios en plantilla
        Log.d("REP", "[WorkoutPlayer] template.getExercises() = " + (template.getExercises() != null ? template.getExercises().size() : "null"));
        if (template.getExercises() == null || template.getExercises().isEmpty()) {
            Log.d("REP", "[WorkoutPlayer] ERROR: La plantilla no tiene ejercicios.");
            //Toast.makeText(this, "No se pudo cargar la plantilla de entrenamiento.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d("EvolvDebug", "[REP][PLAYER] Ejercicios recibidos: " + template.getExercises().size()); // REP
        for (com.example.evolv.models.WorkoutTemplateExercise_v2 wte : template.getExercises()) {
            Log.d("EvolvDebug", "[REP][PLAYER] " + wte.getName() + " / " + wte.getImg_url());
        }

        setContentView(R.layout.activity_workout_player_v2);

        // Obtener el userId correcto para la configuración de sonidos
        int userIdForSounds = 0; // Valor por defecto
        if (template != null) {
            userIdForSounds = template.getUserId();
            if (template.isDefault()) {
                long currentUserId = getIntent().getLongExtra("USER_ID", 0);
                userIdForSounds = (int) ((currentUserId == 0) ? 0 : currentUserId);
            }
        }

        // Inicializar EnhancedSoundManager con el ID del usuario
        soundManager = new EnhancedSoundManager(this, userIdForSounds);

        // Inicializar la vista de descanso
        initRestPeriodView();
        Log.d("SONIDO", "Inicializado EnhancedSoundManager para userId=" + userIdForSounds + ", soundEnabled=" +
                (soundManager != null ? soundManager.isSoundEnabled() : "null"));
        Log.d("EvolvDebug", "[SOUND] Inicializado EnhancedSoundManager para userId=" + userIdForSounds);

        // Reproducir sonido de inicio de entrenamiento
        if (soundManager != null) {
            Log.d("SONIDO", "Reproduciendo sonido de INICIO DE ENTRENAMIENTO");
            soundManager.playStartWorkoutSound();
        }

        // Inicialización de los elementos de la UI
        initializeUIElements();

        // Si se está reanudando una rutina pausada, cargar el progreso
        boolean resumeSession = getIntent().getBooleanExtra("RESUME", false);
        if (resumeSession && template != null) {
            Log.d("EvolvDebug", "[WorkoutPlayer] Intentando reanudar sesión pausada");
            com.example.evolv.utils.SessionProgressManager_v2.Progress progress =
                    com.example.evolv.utils.SessionProgressManager_v2.getProgress(
                            this, template.getUserId(), template.getTemplateId());

            if (progress != null) {
                Log.d("EvolvDebug", "[WorkoutPlayer] Progreso encontrado: exerciseIndex=" +
                        progress.exerciseIndex + ", repIndex=" + progress.repIndex);

                // Restaurar el estado exacto donde quedó el usuario
                this.exerciseIndex = progress.exerciseIndex;
                this.repIndex = progress.repIndex;

                //Toast.makeText(this, "Retomando entrenamiento pausado", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("EvolvDebug", "[WorkoutPlayer] No se encontró progreso guardado");
            }
        }

        updateExerciseDisplay();
    }

    // Obtén la plantilla desde el Intent o donde corresponda
    private WorkoutTemplate_v2 obtenerTemplateDesdeIntent() {

        return (WorkoutTemplate_v2) getIntent().getSerializableExtra("template");
    }

    /**
     * Avanza al siguiente ejercicio completo SIN reproducir sonidos.
     * Método llamado desde el botón Siguiente - SALTA EJERCICIO COMPLETO.
     */
    private void avanzar() {
        Log.d("SONIDO", "Llamada a avanzar() desde botón - Saltando ejercicio completo");
        
        // CRÍTICO: Cancelar temporizador actual para evitar números superpuestos
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
            Log.d("TIMER_DEBUG", "[avanzar] Temporizador cancelado correctamente");
        }
        
        // Saltar al siguiente ejercicio completo
        if (exerciseIndex < template.getExercises().size() - 1) {
            exerciseIndex++;
            repIndex = 0;
            Log.d("DEPURACION", "[avanzar][MANUAL] Saltando a ejercicio " + exerciseIndex);
            advanceToNextExercise();
        } else {
            // Estado Final
            Log.d("DEPURACION", "[avanzar][MANUAL] Último ejercicio alcanzado: Estado Final");
            exerciseIndex++;
            advanceToNextExercise();
        }
    }

    /**
     * Avanza automáticamente después del temporizador - MANEJA REPETICIONES.
     * Método llamado desde onFinish() del temporizador.
     */
    private void avanzarAutomatico() {
        Log.d("SONIDO", "Llamada a avanzarAutomatico() - CON manejo de repeticiones");
        
        if (template == null || template.getExercises() == null) return;

        // Obtener ejercicio actual y sus configuraciones
        if (exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
            int configuredReps = currentExercise.getRepetitions();
            
            Log.d("REP", "[avanzarAutomatico] Evaluando: repIndex=" + repIndex + ", configuredReps=" + configuredReps);

            // ¿Hay más repeticiones del ejercicio actual?
            if (repIndex < configuredReps - 1) {
                repIndex++;
                Log.d("REP", "[avanzarAutomatico] Avanzando a repetición " + (repIndex + 1) + " de " + configuredReps);
                
                // Reiniciar el mismo ejercicio con nueva repetición
                updateExerciseDisplay();
                return;
            }
        }

        // Si llegamos aquí, completamos todas las repeticiones → siguiente ejercicio
        if (exerciseIndex < template.getExercises().size() - 1) {
            exerciseIndex++;
            repIndex = 0;
            Log.d("DEPURACION", "[avanzarAutomatico] Completado ejercicio, avanzando a " + exerciseIndex);
            
            // Reproducir sonido de cambio de ejercicio para avance automático
            if (soundManager != null) {
                Log.d("SONIDO", "Reproduciendo sonido de EJERCICIO " + exerciseIndex + " (automático)");
                soundManager.playExerciseChangeSound();
            }
            
            advanceToNextExercise();
        } else {
            // Estado Final
            Log.d("DEPURACION", "[avanzarAutomatico] Último ejercicio completado: Estado Final");
            exerciseIndex++;
            advanceToNextExercise();
        }
    }

    /**
     * Avanza al siguiente ejercicio completo
     *
     * @param playSounds Si es true, reproducirá los sonidos correspondientes al tipo de avance
     */
    private void avanzar(boolean playSounds) {
        // Este método ya no se usa, mantenido para compatibilidad
        Log.d("DEPURACION", "[avanzar] Método obsoleto llamado - redirigiendo a avanzarAutomatico()");
        avanzarAutomatico();
    }

    // Retrocede a la repetición, set o ejercicio anterior
    private void retroceder() {
        Log.d("SONIDO", "Método retroceder() - ejercicioIndex=" + exerciseIndex);
        Log.d("DEPURACION", "[retroceder][ANTES] ejercicioIndex=" + exerciseIndex);
        if (template == null || template.getExercises() == null) return;

        // CRÍTICO: Cancelar temporizador actual para evitar números superpuestos
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
            Log.d("TIMER_DEBUG", "[retroceder] Temporizador cancelado correctamente");
        }

        if (exerciseIndex > 0) {
            exerciseIndex--;
            repIndex = 0;
            Log.d("DEPURACION", "[retroceder][DESPUES] ejercicioIndex=" + exerciseIndex);
            updateExerciseDisplay();
        } else {
            // Si ya es el primer ejercicio, puedes mostrar un mensaje o hacer nada
            Log.d("DEPURACION", "[retroceder] Primer ejercicio alcanzado");
        }
    }

    // Lógica para avanzar y finalizar sesión si corresponde

    /**
     * Método específico para avanzar automáticamente con sonido.
     * Este método debe llamarse SOLO desde eventos automáticos, nunca desde botones o UI.
     */
    private void avanzarAutomaticoConSonido() {
        Log.d("SONIDO", "Llamada a avanzarAutomaticoConSonido() - CON sonido");
        avanzarAutomatico(); // Reproduce sonidos
    }

    /**
     * Método específico para reproducir el sonido de cambio de ejercicio.
     * Debe usarse SOLO en transiciones automáticas, nunca en interacciones de usuario.
     */
    private void reproducirSonidoCambioEjercicio() {
        Log.d("SONIDO", "Reproducción de sonido automático: CAMBIO DE EJERCICIO");
        if (soundManager != null && soundManager.isSoundEnabled()) {
            soundManager.playExerciseChangeSound();
        }
    }

    /**
     * Método específico para reproducir el sonido de fin de entrenamiento.
     * Debe usarse SOLO al finalizar un entrenamiento.
     */
    private void reproducirSonidoFinEntrenamiento() {
        Log.d("SONIDO", "Reproducción de sonido automático: FIN DE ENTRENAMIENTO");
        if (soundManager != null && soundManager.isSoundEnabled()) {
            soundManager.playEndWorkoutSound();
        }
    }

    /**
     * Método específico para reproducir el sonido de inicio de entrenamiento.
     * Debe usarse SOLO al iniciar un entrenamiento.
     */
    private void reproducirSonidoInicioEntrenamiento() {
        Log.d("SONIDO", "Reproducción de sonido automático: INICIO DE ENTRENAMIENTO");
        if (soundManager != null && soundManager.isSoundEnabled()) {
            soundManager.playStartWorkoutSound();
        }
    }

    /**
     * Inicializa la vista de período de descanso
     */
    private void initRestPeriodView() {
        Log.d("REST", "[initRestPeriodView] Inicializando la vista de descanso");
        // Inflamos la vista de descanso pero no la mostramos aún
        restPeriodView = getLayoutInflater().inflate(R.layout.view_rest_period, null);
        Log.d("REST", "[initRestPeriodView] Vista de descanso inflada: " + (restPeriodView != null ? "correctamente" : "ERROR: null!"));


        // Inicializamos las referencias a los elementos de la vista de descanso
        textRestTimer = restPeriodView.findViewById(R.id.textRestTimer);
        imageNextExercise = restPeriodView.findViewById(R.id.imageNextExercise);
        textNextExerciseName = restPeriodView.findViewById(R.id.textNextExerciseName);
        textNextExerciseDescription = restPeriodView.findViewById(R.id.textNextExerciseDescription);
        btnSkipRest = restPeriodView.findViewById(R.id.btnSkipRest);

        // Configuramos el comportamiento de la imagen (mostrar descripción al tocar)
        imageNextExercise.setOnClickListener(v -> {
            // Mostrar u ocultar descripción al tocar la imagen
            if (textNextExerciseDescription.getVisibility() == View.VISIBLE) {
                textNextExerciseDescription.setVisibility(View.GONE);
            } else {
                textNextExerciseDescription.setVisibility(View.VISIBLE);

                // Configurar timer para ocultar la descripción después de un tiempo
                if (hideRestDescriptionHandler == null) {
                    hideRestDescriptionHandler = new android.os.Handler();
                }

                if (hideRestDescriptionRunnable == null) {
                    hideRestDescriptionRunnable = () -> {
                        if (textNextExerciseDescription != null) {
                            textNextExerciseDescription.setVisibility(View.GONE);
                        }
                    };
                }

                // Primero removemos cualquier callback pendiente
                hideRestDescriptionHandler.removeCallbacks(hideRestDescriptionRunnable);
                // Luego programamos la ocultación después de 5 segundos
                hideRestDescriptionHandler.postDelayed(hideRestDescriptionRunnable, 5000);
            }
        });

        // Configuramos el botón para saltar el descanso
        btnSkipRest.setOnClickListener(v -> skipRestPeriod());
    }

    /**
     * Muestra la pantalla de descanso y configura el temporizador para el siguiente ejercicio
     */
    private void showRestPeriod() {
        Log.d("REST", "[showRestPeriod] INICIANDO método de pantalla de descanso");

        // Inicializamos una nueva instancia de la vista de descanso cada vez
        // para evitar el error "The specified child already has a parent"
        initRestPeriodView();
        Log.d("REST", "[showRestPeriod] Nueva instancia de la vista de descanso creada");

        // Cancelamos cualquier temporizador activo del ejercicio
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
        }

        // Obtenemos el ejercicio actual (que acabamos de completar)
        WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex - 1);

        // Obtenemos el tiempo de descanso en segundos
        int restTimeSeconds = currentExercise.getRestPeriod();
        Log.d("DESCANSO", "Iniciando período de descanso de " + restTimeSeconds + " segundos");

        // Cambiamos el estado a REST
        currentState = WorkoutState.REST;

        // Obtenemos el próximo ejercicio
        if (exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 nextExercise = template.getExercises().get(exerciseIndex);

            // Configuramos la información del próximo ejercicio
            String rest_time_header = getString(R.string.rest_time_header );
            textNextExerciseName.setText(rest_time_header +" "+ nextExercise.getName());

            // Cargamos la imagen del próximo ejercicio
            String imageResourceName = nextExercise.getImg_url();
            if (imageResourceName != null && !imageResourceName.isEmpty()) {
                try {
                    int imageResourceId = getResources().getIdentifier(
                            imageResourceName, "drawable", getPackageName());
                    if (imageResourceId != 0) {
                        imageNextExercise.setImageResource(imageResourceId);
                    }
                } catch (Exception e) {
                    Log.e("DESCANSO", "Error al cargar la imagen del próximo ejercicio: " + e.getMessage());
                }
            }

            // Establecemos la descripción textual del ejercicio
            // Este texto no se muestra inicialmente, se muestra al tocar la imagen
            // Obtenemos la descripción completa desde la base de datos
            com.example.evolv.DatabaseHelper dbHelper = new com.example.evolv.DatabaseHelper(this);
            String description = dbHelper.getExerciseDescription(nextExercise.getExerciseId());

            // Si la descripción está vacía, mostrar un mensaje alternativo
            if (description == null || description.trim().isEmpty()) {
                description = "No hay descripción disponible para este ejercicio";
            }

            textNextExerciseDescription.setText(description);
            Log.d("DESCANSO", "Descripción del próximo ejercicio configurada: " +
                    description.substring(0, Math.min(50, description.length())) + "...");
        }

        // Añadimos la vista de descanso al layout actual
        android.widget.LinearLayout mainLayout = findViewById(R.id.mainPlayerLayout);
        Log.d("REST", "[showRestPeriod] Layout principal encontrado: " + (mainLayout != null ? "sí" : "NO - NULL!"));
        if (mainLayout == null) {
            Log.e("REST", "[showRestPeriod] ERROR CRÍTICO: No se pudo encontrar el layout principal con ID mainPlayerLayout");
            return;
        }
        mainLayout.removeAllViews(); // Eliminamos la vista actual
        Log.d("REST", "[showRestPeriod] Vistas previas eliminadas del layout principal");
        mainLayout.addView(restPeriodView);  // Añadimos la vista de descanso
        Log.d("REST", "[showRestPeriod] Vista de descanso añadida al layout principal");

        // Configuramos el temporizador para el período de descanso
        Log.d("REST", "[showRestPeriod] Configurando temporizador de descanso para " + restTimeSeconds + " segundos");
        restTimer = new CountDownTimer(restTimeSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Actualizar la UI con el tiempo restante
                int secondsLeft = (int) (millisUntilFinished / 1000);
                int minutes = secondsLeft / 60;
                int seconds = secondsLeft % 60;
                textRestTimer.setText(String.format("%02d:%02d", minutes, seconds));

                // Registrar los ticks del temporizador cada 5 segundos para no saturar el log
                if (secondsLeft % 5 == 0) {
                    Log.d("REST", "[showRestPeriod] Temporizador de descanso: " + minutes + ":" + seconds + " restantes");
                }
            }

            @Override
            public void onFinish() {
                // Cuando termina el tiempo de descanso, mostramos el siguiente ejercicio
                Log.d("REST", "[showRestPeriod] ¡TEMPORIZADOR DE DESCANSO COMPLETADO! Llamando a continueToNextExercise()");
                continueToNextExercise();
            }
        };

        // Iniciamos el temporizador
        restTimer.start();
        Log.d("REST", "[showRestPeriod] Temporizador de descanso INICIADO");
    }

    /**
     * Salta el período de descanso y muestra inmediatamente el siguiente ejercicio
     */
    private void skipRestPeriod() {
        Log.d("REST", "[skipRestPeriod] Usuario ha saltado el período de descanso. Estado actual=" + currentState + ", exerciseIndex=" + exerciseIndex);

        // Cancelamos el temporizador de descanso si está activo
        if (restTimer != null) {
            restTimer.cancel();
            restTimer = null;
            Log.d("REST", "[skipRestPeriod] Temporizador de descanso cancelado");
        } else {
            Log.d("REST", "[skipRestPeriod] No había temporizador de descanso activo para cancelar");
        }

        // Mostramos el siguiente ejercicio
        Log.d("REST", "[skipRestPeriod] Llamando a continueToNextExercise() para mostrar siguiente ejercicio");
        continueToNextExercise();
    }

    /**
     * Continúa al siguiente ejercicio después del descanso
     */
    private void continueToNextExercise() {
        Log.d("REST", "[continueToNextExercise] INICIO - exerciseIndex=" + exerciseIndex + ", estado anterior=" + currentState);

        // Cambiamos el estado de vuelta a EXERCISE
        currentState = WorkoutState.EXERCISE;
        Log.d("REST", "[continueToNextExercise] Estado cambiado a EXERCISE");

        // Eliminamos la vista de descanso y mostramos la de ejercicio
        Log.d("REST", "[continueToNextExercise] Reconfigurando layout principal con setContentView()");
        setContentView(R.layout.activity_workout_player_v2);

        // Reinicializamos las referencias necesarias
        Log.d("REST", "[continueToNextExercise] Reinicializando referencias de UI con initializeUIElements()");
        initializeUIElements();

        // Actualizamos la pantalla para mostrar el siguiente ejercicio
        Log.d("REST", "[continueToNextExercise] Actualizando pantalla para mostrar ejercicio " + exerciseIndex);
        updateExerciseDisplay();

        Log.d("REST", "[continueToNextExercise] FIN - Se debería ver el ejercicio " + exerciseIndex + " en pantalla");
    }

    /**
     * Inicializa todas las referencias de la UI y configura los listeners
     */
    private void initializeUIElements() {
        // Inicialización de vistas principales
        progressBar = findViewById(R.id.progressBar);
        textTimerCenter = findViewById(R.id.textTimerCenter);
        textExerciseDescription = findViewById(R.id.textExerciseDescription);
        imageExercise = findViewById(R.id.imageExercise);
        btnPause = findViewById(R.id.buttonPauseResume);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnFinish = findViewById(R.id.btnFinish);

        // Inicialización de handlers y runnables
        if (finishHandler == null) {
            finishHandler = new android.os.Handler();
        }
        if (finishRunnable == null) {
            finishRunnable = () -> finish();
        }
        if (hideDescriptionRunnable == null) {
            hideDescriptionRunnable = () -> {
                if (textExerciseDescription != null) {
                    textExerciseDescription.setVisibility(View.GONE);
                }
            };
        }

        // Configuración de listeners
        if (btnPause != null) btnPause.setOnClickListener(v -> {
            Log.d("SONIDO", "Botón PAUSA presionado");
            togglePauseResume();
        });
        if (btnNext != null) btnNext.setOnClickListener(v -> {
            Log.d("SONIDO", "Botón SIGUIENTE presionado - NO sonido");
            avanzar(false); // Pasar false para que NO reproduzca sonidos
        });
        if (btnPrev != null) btnPrev.setOnClickListener(v -> {
            Log.d("SONIDO", "Botón ANTERIOR presionado");
            retroceder();
        });

        // Configuración del botón Finalizar
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
                    userIdToUse = (int) ((currentUserId == 0) ? 0 : currentUserId);
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

        // Listener para la imagen: muestra la descripción al hacer clic
        if (imageExercise != null) {
            imageExercise.setOnClickListener(v -> {
                showExerciseDescription();
            });
        }
    }

    private void advanceToNextExercise() {
        Log.d("REST", "[advanceToNextExercise] INICIO del método - exerciseIndex=" + exerciseIndex);
        Log.d("DEPURACION", "[advanceToNextExercise][ANTES] ejercicioIndex=" + exerciseIndex + ", repIndex=" + repIndex);

        if (template != null && template.getExercises() != null) {
            Log.d("REST", "[advanceToNextExercise] Número total de ejercicios: " + template.getExercises().size());

            if (exerciseIndex < template.getExercises().size()) {
                WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
                Log.d("DEPURACION", "[advanceToNextExercise][ANTES] reps=" + currentExercise.getRepetitions());
                Log.d("REST", "[advanceToNextExercise] Ejercicio actual: " + currentExercise.getName() + ", restPeriod=" + currentExercise.getRestPeriod() + " segundos");
            } else {
                Log.d("REST", "[advanceToNextExercise] exerciseIndex está fuera de los límites: " + exerciseIndex);
            }
        } else {
            Log.e("REST", "[advanceToNextExercise] ¡Template o ejercicios nulos!");
        }

        // Si el ejercicio actual acaba de completarse y vamos a otro ejercicio (no el final)
        // mostramos la pantalla de descanso
        if (exerciseIndex > 0 && exerciseIndex < template.getExercises().size()) {
            Log.d("REST", "[advanceToNextExercise] Condición cumplida para mostrar la pantalla de descanso - Llamando a showRestPeriod()");
            showRestPeriod();
            return;
        } else {
            Log.d("REST", "[advanceToNextExercise] No se cumple la condición para mostrar descanso: exerciseIndex=" + exerciseIndex + ", límite=" + (template != null ? template.getExercises().size() : "template nulo"));
        }

        if (exerciseIndex < template.getExercises().size()) {
            updateExerciseDisplay();
            Log.d("DEPURACION", "[advanceToNextExercise][DESPUES] ejercicioIndex=" + exerciseIndex + ", repIndex=" + repIndex);
        } else {
            // Cambiamos el estado a FINISHED
            currentState = WorkoutState.FINISHED;

            // Detener el temporizador para evitar que siga actualizando la UI
            if (exerciseTimer != null) {
                exerciseTimer.cancel();
                exerciseTimer = null;
            }

            // Reproducir sonido de fin de entrenamiento
            if (soundManager != null && soundManager.isSoundEnabled()) {
                Log.d("SONIDO", "Reproduciendo sonido de FIN DE ENTRENAMIENTO");
                soundManager.playEndWorkoutSound();
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
        Log.d("REST", "[updateExerciseDisplay] INICIO - exerciseIndex=" + exerciseIndex + ", estado actual=" + currentState);
        Log.d("REP", "[updateExerciseDisplay][ANTES] ejercicioIndex=" + exerciseIndex + ", repIndex=" + repIndex);

        if (template != null && template.getExercises() != null && exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
            Log.d("REP", "[REP][UI] Mostrando: " + currentExercise.getName() + " / " + currentExercise.getImg_url());

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

            // Mostrar progreso SIN referencias a "Serie"
            TextView textProgress = findViewById(R.id.textProgress);
            String progreso = String.format(
                    "Ejercicio %d/%d, Rep %d/%d",
                    exerciseIndex + 1,
                    template.getExercises().size(),
                    repIndex + 1,
                    currentExercise.getRepetitions()
            );
            textProgress.setText(progreso);
        }
        
        if (repIndex == 0) {
            showExerciseIntroPanel();
        } else {
            updateExerciseDisplayCore();
        }
    }

    // Panel de introducción al ejercicio
    private void showExerciseIntroPanel() {
        Log.d("REP", "showExerciseIntroPanel: ejercicioIndex=" + exerciseIndex + ", repIndex=" + repIndex);

        if (template == null || template.getExercises() == null || template.getExercises().isEmpty()) {
            Log.e("EVOLV-DEBUG", "Trying to access exercises but list is empty or null");
            return;
        }

        //TextView textIntroTitle = findViewById(R.id.textIntroTitle);
        TextView textIntroExerciseName = findViewById(R.id.textIntroExerciseName);
        TextView textIntroExerciseDesc = findViewById(R.id.textIntroExerciseDesc);

        WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);

        // Buscar la descripción en tu lista de ejercicios base si la tienes
        String descripcion = currentExercise.getName(); // Placeholder
        textIntroExerciseName.setText(currentExercise.getName());
        textIntroExerciseDesc.setText("");

        Log.d("REP", "showExerciseIntroPanel: llamando a updateExerciseDisplayCore()");
        updateExerciseDisplayCore();
    }
    // Lógica principal de actualización de la UI del ejercicio
    private void updateExerciseDisplayCore() {
        Log.d("REP", "[updateExerciseDisplayCore][ANTES] ejercicioIndex=" + exerciseIndex + ", repIndex=" + repIndex);
        if (template != null && template.getExercises() != null && exerciseIndex < template.getExercises().size()) {
            WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
            Log.d("REP", "[updateExerciseDisplayCore][ANTES] reps=" + currentExercise.getRepetitions());
        }
        // Cancelar temporizador anterior si existe
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        // Obtener ejercicio actual
        WorkoutTemplateExercise_v2 currentExercise = template.getExercises().get(exerciseIndex);
        Log.d("REP", "[REP][UI] Mostrando: " + currentExercise.getName() + " / " + currentExercise.getImg_url());

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

        // Obtener configuraciones del ejercicio
        int configuredReps = currentExercise.getRepetitions();
        int configuredDuration = currentExercise.getTargetDuration();
        
        // LOGS DE DIAGNÓSTICO CRÍTICO para valor 60 vs 10
        Log.e("DURATION_BUG", "=== INVESTIGACIÓN CRÍTICA DEL BUG 60 vs 10 ===");
        Log.e("DURATION_BUG", "Ejercicio: " + currentExercise.getName());
        Log.e("DURATION_BUG", "exerciseId: " + currentExercise.getExerciseId());
        Log.e("DURATION_BUG", "getTargetDuration(): " + currentExercise.getTargetDuration());
        Log.e("DURATION_BUG", "getDuration(): " + currentExercise.getDuration());
        Log.e("DURATION_BUG", "durationType: " + currentExercise.getDurationType());
        Log.e("DURATION_BUG", "repetitions: " + currentExercise.getRepetitions());
        Log.e("DURATION_BUG", "sets: " + currentExercise.getSets());
        Log.e("DURATION_BUG", "restPeriod: " + currentExercise.getRestPeriod());
        Log.e("DURATION_BUG", "configuredDuration usado: " + configuredDuration);
        
        // VERIFICAR SI HAY ALGÚN MÉTODO ADICIONAL QUE SOBRESCRIBA EL VALOR
        if (configuredDuration != 10) {
            Log.e("DURATION_BUG", "🚨 VALOR INCORRECTO DETECTADO: configuredDuration=" + configuredDuration + " (debería ser 10)");
            Log.e("DURATION_BUG", "Verificando si getTargetDuration() != getDuration():");
            Log.e("DURATION_BUG", "getTargetDuration()=" + currentExercise.getTargetDuration() + 
                                  ", getDuration()=" + currentExercise.getDuration());
        }
        Log.e("DURATION_BUG", "===============================");
        
        // 🔥 LOG CRÍTICO: MOSTRAR EXACTAMENTE QUÉ VALOR SE USARÁ EN EL TEMPORIZADOR
        Log.e("TIMER_CRITICAL", "🔥🔥🔥 TEMPORIZADOR USARÁ: " + configuredDuration + " segundos 🔥🔥🔥");
        if (configuredDuration == 60) {
            Log.e("TIMER_CRITICAL", "❌❌❌ ERROR: USANDO 60 SEGUNDOS EN LUGAR DE 10 ❌❌❌");
        }
        
        // Mostrar progreso SIN referencias a "Serie"
        TextView textProgress = findViewById(R.id.textProgress);
        String progreso = String.format(
                "Ejercicio %d/%d, Rep %d/%d",
                exerciseIndex + 1,
                template.getExercises().size(),
                repIndex + 1,
                configuredReps
        );
        textProgress.setText(progreso);

        // Asegurar que estamos usando exactamente la duración configurada
        final int totalSeconds = configuredDuration;
        progressBar.setMax(totalSeconds);
        progressBar.setProgress(totalSeconds);

        // Mostrar el valor inicial exacto antes de iniciar el temporizador
        textTimerCenter.setText(String.valueOf(totalSeconds));
        
        // Guardar el tiempo de inicio para cálculos precisos
        final long startTimeMillis = totalSeconds * 1000L;
        millisLeft = startTimeMillis; // Inicializar el tiempo restante
        
        Log.d("REP", "[startExerciseCountdown] Iniciando temporizador con: " + startTimeMillis + " ms (" + totalSeconds + " segundos)");
        // CRÍTICO: Cancelar temporizador anterior para evitar números superpuestos
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
            Log.d("TIMER_DEBUG", "[updateExerciseDisplayCore] Temporizador anterior cancelado");
        }
        
        exerciseTimer = new CountDownTimer(startTimeMillis, 50) {  // Actualizar cada 50ms para animación suave
            @Override
            public void onTick(long millisUntilFinished) {
                // Actualizar el tiempo restante directamente sin compensación
                millisLeft = millisUntilFinished;
                int seconds = (int) (millisLeft / 1000);
                
                // Para una animación más suave, podemos calcular el progreso con precisión de milisegundos
                float progress = (float)millisLeft / (float)startTimeMillis * totalSeconds;
                
                textTimerCenter.setText(String.valueOf(seconds));
                progressBar.setProgress(seconds);
            }

            @Override
            public void onFinish() {
                Log.d("REP", "[onFinish] Temporizador FINALIZADO - Duración original: " + (startTimeMillis/1000) + " segundos");
                textTimerCenter.setText("0");
                progressBar.setProgress(0);
                if (btnNext != null) btnNext.setEnabled(true);

                Log.d("SONIDO", "Finalizó temporizador automático - llamando a avanzarAutomatico()");
                // Llamar a avance automático que maneja repeticiones
                avanzarAutomatico();
            }
        };
        if (!isPaused) exerciseTimer.start();
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
        
        // Deshabilitar los botones de navegación cuando se pausa
        if (btnPrev != null) btnPrev.setEnabled(false);
        if (btnNext != null) btnNext.setEnabled(false);
        
        Log.d("EvolvDebug", "[PAUSE] Botones de navegación deshabilitados");

        // Guardar inmediatamente el progreso cuando el usuario pausa explícitamente
        Log.d("EvolvDebug", "[PAUSE] Usuario ha pulsado botón de pausa, guardando progreso...");
        saveCurrentProgress();

        // Informar al usuario que su progreso se ha guardado
        //Toast.makeText(this, "Entrenamiento pausado", Toast.LENGTH_SHORT).show();
    }

    private void resumeTimer() {
        // Cambiar el estado local de pausa
        isPaused = false;
        progressBar.setVisibility(View.VISIBLE);
        if (btnPause != null) btnPause.setText("Pausar");
        
        // Habilitar los botones de navegación cuando se reanuda
        if (btnPrev != null) btnPrev.setEnabled(true);
        if (btnNext != null) btnNext.setEnabled(true);
        
        Log.d("EvolvDebug", "[RESUME] Botones de navegación habilitados nuevamente");

        // NUEVO: Actualizar también el estado en el objeto template y en SharedPreferences
        if (template != null) {
            template.setPaused(false);

            // Obtener el userId correcto (igual que en saveCurrentProgress)
            int userIdToUse = template.getUserId();
            if (template.isDefault()) {
                long currentUserId = getIntent().getLongExtra("USER_ID", 0);
                userIdToUse = (int) ((currentUserId == 0) ? 0 : currentUserId);
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
        // Si hay un temporizador anterior, lo cancelamos primero
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        // Asegurar que estamos usando exactamente la duración configurada
        final int totalSeconds = (int) (millis / 1000);
        progressBar.setMax(totalSeconds);
        progressBar.setProgress(totalSeconds);

        // Mostrar el valor inicial exacto antes de iniciar el temporizador
        textTimerCenter.setText(String.valueOf(totalSeconds));
        
        // Guardar el tiempo de inicio para cálculos precisos
        final long startTimeMillis = millis;
        millisLeft = millis; // Inicializar el tiempo restante
        
        Log.d("REP", "[startExerciseCountdown] Iniciando temporizador con: " + millis + " ms (" + totalSeconds + " segundos)");
        exerciseTimer = new CountDownTimer(millis, 50) {  // Actualizar cada 50ms para animación suave
            @Override
            public void onTick(long millisUntilFinished) {
                // Actualizar el tiempo restante directamente sin compensación
                millisLeft = millisUntilFinished;
                int seconds = (int) (millisLeft / 1000);
                
                // Para una animación más suave, podemos calcular el progreso con precisión de milisegundos
                float progress = (float)millisLeft / (float)startTimeMillis * totalSeconds;
                
                textTimerCenter.setText(String.valueOf(seconds));
                progressBar.setProgress(seconds);
            }

            @Override
            public void onFinish() {
                Log.d("REP", "[onFinish] Temporizador FINALIZADO - Duración original: " + (millis/1000) + " segundos");
                textTimerCenter.setText("0");
                progressBar.setProgress(0);
                if (btnNext != null) btnNext.setEnabled(true);

                Log.d("SONIDO", "Finalizó temporizador automático - llamando a avanzarAutomatico()");
                // Llamar a avance automático que maneja repeticiones
                if (avanzarAlFinal) avanzarAutomatico();
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
                        ", repIndex=" + repIndex + ", userId=" + template.getUserId() +
                        ", templateId=" + template.getTemplateId());

                // Guardar el progreso
                com.example.evolv.utils.SessionProgressManager_v2.saveProgress(
                        this, template.getUserId(), template.getTemplateId(),
                        exerciseIndex, 0, repIndex);

                // Marcar la plantilla como pausada (en memoria y en SharedPreferences)
                template.setPaused(true);

                // Obtener el userId correcto (igual que en saveCurrentProgress)
                int userIdToUse = template.getUserId();
                if (template.isDefault()) {
                    long currentUserId = getIntent().getLongExtra("USER_ID", 0);
                    userIdToUse = (int) ((currentUserId == 0) ? 0 : currentUserId);
                }

                // Limpiar el estado de pausa en SharedPreferences
                Log.d("EvolvDebug", "[PAUSE] Guardando estado pausado de plantilla con userId=" + userIdToUse +
                        ", templateId=" + template.getTemplateId());

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

        // Liberar recursos de EnhancedSoundManager
        if (soundManager != null) {
            // Asegurarse de guardar cualquier configuración pendiente antes de liberar recursos
            soundManager.saveSettings();
            soundManager.release();
        }

        // Eliminar callbacks pendientes
        if (finishHandler != null) {
            finishHandler.removeCallbacks(finishRunnable);
            finishHandler.removeCallbacks(hideDescriptionRunnable);
        }

        // Si estamos finalizando la actividad y no por cambio de configuración,
        // limpiar el estado de pausa para evitar que quede marcado como pausado
        if (isFinishing() && template != null) {
            Log.d("EvolvDebug", "[CLEANUP] Finalizando actividad, verificando si debemos limpiar estado de pausa");

            // Obtener userId correcto
            int userIdToUse = template.getUserId();
            if (template.isDefault()) {
                long currentUserId = getIntent().getLongExtra("USER_ID", 0);
                userIdToUse = (int) ((currentUserId == 0) ? 0 : currentUserId);
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

        // Log para debugging (comentado)
        //Log.d("EvolvDebug", "[DESCRIPTION] Mostrando descripción del ejercicio " + currentExercise.getName() + ": " +
        //        description.substring(0, Math.min(50, description.length())) + "...");
    }
}

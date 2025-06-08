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
    private Button btnPause, btnNext, btnPrev, btnFinish;
    private android.os.Handler finishHandler;
    private Runnable finishRunnable;

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

        // Recupera el objeto completo
        template = (WorkoutTemplate_v2) getIntent().getSerializableExtra("template");

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
        btnPause = findViewById(R.id.buttonPauseResume);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnFinish = findViewById(R.id.btnFinish);
        finishHandler = new android.os.Handler();
        finishRunnable = () -> finish();

        // Listeners
        if (btnPause != null) btnPause.setOnClickListener(v -> togglePauseResume());
        if (btnNext != null) btnNext.setOnClickListener(v -> avanzar());
        if (btnPrev != null) btnPrev.setOnClickListener(v -> retroceder());
        if (btnFinish != null) btnFinish.setOnClickListener(v -> {
            if (finishHandler != null) finishHandler.removeCallbacks(finishRunnable);
            finish();
        });

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
    }

    private void resumeTimer() {
        isPaused = false;
        progressBar.setVisibility(View.VISIBLE);
        if (btnPause != null) btnPause.setText("Pausar");
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

    // Limpieza de recursos si la actividad se destruye
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
            exerciseTimer = null;
        }
        if (finishHandler != null && finishRunnable != null) {
            finishHandler.removeCallbacks(finishRunnable);
        }
    }
}

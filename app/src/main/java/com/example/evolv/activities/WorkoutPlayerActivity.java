package com.example.evolv.activities;

import com.example.evolv.models.Exercise;
import com.example.evolv.DatabaseHelper;
import com.example.evolv.models.WorkoutTemplate;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.evolv.R;
import com.example.evolv.models.Exercise;
import java.util.ArrayList;

public class WorkoutPlayerActivity extends AppCompatActivity {
    private ArrayList<Exercise> exerciseList;
    private int currentIndex = 0;
    private boolean isPaused = false;
    private boolean isRest = false;
    private CountDownTimer timer;
    private long timeLeftMs;

    // UI
    private TextView textExerciseName, textExerciseDesc, textNextInfo, textTimer;
    private ImageView imageExercise;
    private ProgressBar progressBar;
    private Button btnPause, btnNext;

    // Duraciones
    private static final int DEFAULT_EXERCISE_DURATION = 30000; // 30s por ejercicio
    private static final int DEFAULT_REST_DURATION = 10000; // 10s descanso

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_player);

        // UI
        textExerciseName = findViewById(R.id.textExerciseName);
        textExerciseDesc = findViewById(R.id.textExerciseDesc);
        textNextInfo = findViewById(R.id.textNextInfo);
        textTimer = findViewById(R.id.textTimer);

        imageExercise = findViewById(R.id.imageExercise);
        progressBar = findViewById(R.id.progressBar);
        btnPause = findViewById(R.id.btnPause);
        btnNext = findViewById(R.id.btnNext);

        // Intro UI
        LinearLayout introContainer = findViewById(R.id.introContainer);
        TextView textIntroTitle = findViewById(R.id.textIntroTitle);
        TextView textIntroExerciseName = findViewById(R.id.textIntroExerciseName);
        TextView textIntroExerciseDesc = findViewById(R.id.textIntroExerciseDesc);

        TextView textWorkoutNameIntro = findViewById(R.id.textWorkoutNameIntro);
        com.google.android.material.button.MaterialButton btnIntroNext = findViewById(R.id.btnIntroNext);

        // Ocultar UI principal al inicio
        findViewById(R.id.cardCentral).setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textTimer.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);


        // Obtener el nombre del entrenamiento usando TEMPLATE_ID
        String workoutName = "";
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TEMPLATE_ID")) {
            long templateId = intent.getLongExtra("TEMPLATE_ID", -1);
            if (templateId != -1) {
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                WorkoutTemplate template = dbHelper.getWorkoutTemplateById(templateId);
                if (template != null) {
                    workoutName = template.getName();
                }
            }
        }
        textWorkoutNameIntro.setText(workoutName);

        // Recibir lista de ejercicios
        intent = getIntent();
        exerciseList = (ArrayList<Exercise>) intent.getSerializableExtra("exercise_list");
        if (exerciseList == null || exerciseList.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_exercises_to_play), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // LOG: Verificar datos recibidos por el intent
        for (Exercise ex : exerciseList) {
            android.util.Log.d("EvolvDebug", "RECIBIDO Ejercicio: " + ex.getName() + " desc: " + ex.getDescription_text());
        }

        // Inicializar intro con datos del primer ejercicio
        if (exerciseList != null && !exerciseList.isEmpty()) {
            Exercise first = exerciseList.get(0);
            textIntroExerciseName.setText(first.getName());
            textIntroExerciseDesc.setText(first.getDescription_text());
        }

        // Handler para auto-inicio
        final android.os.Handler introHandler = new android.os.Handler();
        final Runnable introRunnable = () -> {
            if (introContainer.getVisibility() == View.VISIBLE) {
                currentIndex = 0;
                introContainer.setVisibility(View.GONE);
                findViewById(R.id.cardCentral).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                textTimer.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                startExercise();
            }
        };
        introHandler.postDelayed(introRunnable, 3000);

        btnIntroNext.setOnClickListener(v -> {
            introHandler.removeCallbacks(introRunnable);
            if (introContainer.getVisibility() == View.VISIBLE) {
                currentIndex = 0;
                introContainer.setVisibility(View.GONE);
                findViewById(R.id.cardCentral).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                textTimer.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                startExercise();
            }
        });

        // Listeners de botones principales
        btnPause.setOnClickListener(view -> {
            if (isPaused) {
                resumeTimer();
            } else {
                pauseTimer();
            }
        });
        btnNext.setOnClickListener(view -> {
            advance();
        });
    }

    private void startExercise() {
        android.util.Log.d("EvolvDebug", "startExercise() called");
        isRest = false;
        Exercise exercise = exerciseList.get(currentIndex);
        android.util.Log.d("EvolvDebug", "img_url: " + exercise.getImg_url());
        textExerciseName.setText(exercise.getName());
        textExerciseDesc.setText(exercise.getDescription_text());
        // Restaurar tamaño de fuente normal para ejercicio
        textExerciseName.setTextSize(24); // sp
        textExerciseDesc.setTextSize(16); // sp
        // Cargar imagen real si existe, si no el placeholder
        if (exercise.getImg_url() != null && !exercise.getImg_url().isEmpty()) {
            String imgUrl = exercise.getImg_url();
            // Si parece una URL (http/https), usar Glide
            if (imgUrl.startsWith("http://") || imgUrl.startsWith("https://")) {
                try {
                    com.bumptech.glide.Glide.with(this)
                            .load(imgUrl)
                            .placeholder(R.drawable.ic_exercise_placeholder)
                            .error(R.drawable.ic_exercise_placeholder)
                            .into(imageExercise);
                } catch (Exception e) {
                    imageExercise.setImageResource(R.drawable.ic_exercise_placeholder);
                }
            } else {
                // Tratar cualquier otro valor como nombre de recurso local
                String resName = imgUrl;
                int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
                android.util.Log.d("EvolvDebug", "resName: " + resName + ", resId: " + resId);
                if (resId != 0) {
                    imageExercise.setImageResource(resId);
                } else {
                    imageExercise.setImageResource(R.drawable.ic_exercise_placeholder);
                }
            }
        } else {
            imageExercise.setImageResource(R.drawable.ic_exercise_placeholder);
        }
        imageExercise.setVisibility(View.VISIBLE);
        textNextInfo.setVisibility(View.GONE);


        progressBar.setMax(DEFAULT_EXERCISE_DURATION);
        progressBar.setProgress(DEFAULT_EXERCISE_DURATION);
        timeLeftMs = DEFAULT_EXERCISE_DURATION;
        startTimer(DEFAULT_EXERCISE_DURATION);
    }

    private void startRest() {
        // Si ya estamos en el último ejercicio, no mostrar descanso, ir directo a pantalla de fin
        if (currentIndex + 1 >= exerciseList.size()) {
            finishWorkout();
            return;
        }
        android.util.Log.d("EvolvDebug", "startRest() - currentIndex: " + currentIndex + ", exerciseList.size(): " + (exerciseList != null ? exerciseList.size() : -1));


        isRest = true;
        // Mostrar nombre, descripción e imagen igual que en startExercise
        if (currentIndex + 1 < exerciseList.size()) {
            Exercise next = exerciseList.get(currentIndex + 1);
            android.util.Log.d("EvolvDebug", "Siguiente ejercicio: " + next.getName() + " - " + next.getDescription_text());
            textNextInfo.setText(getString(R.string.next_exercise));
            textNextInfo.setVisibility(View.VISIBLE);
            textExerciseName.setVisibility(View.VISIBLE);
            textExerciseDesc.setVisibility(View.VISIBLE);
            textExerciseName.setText(next.getName());
            textExerciseDesc.setText(next.getDescription_text() != null ? next.getDescription_text() : "");
            textExerciseName.setTextSize(24); // sp
            textExerciseDesc.setTextSize(16); // sp
            // Imagen igual que en startExercise
            if (next.getImg_url() != null && !next.getImg_url().isEmpty()) {
                int resId = getResources().getIdentifier(next.getImg_url(), "drawable", getPackageName());
                if (resId != 0) {
                    imageExercise.setImageResource(resId);
                } else {
                    imageExercise.setImageResource(R.drawable.ic_exercise_placeholder);
                }
            } else {
                imageExercise.setImageResource(R.drawable.ic_exercise_placeholder);
            }
            imageExercise.setVisibility(View.VISIBLE);
        } else {
            textNextInfo.setText("");
            textNextInfo.setVisibility(View.GONE);
            textExerciseName.setVisibility(View.GONE);
            textExerciseDesc.setVisibility(View.GONE);
            imageExercise.setVisibility(View.GONE);
        }
        progressBar.setMax(DEFAULT_REST_DURATION);
        progressBar.setProgress(DEFAULT_REST_DURATION);
        timeLeftMs = DEFAULT_REST_DURATION;
        startTimer(DEFAULT_REST_DURATION);
    }

    private void startTimer(long duration) {
        timer = new CountDownTimer(duration, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMs = millisUntilFinished;
                progressBar.setProgress((int) millisUntilFinished);
                textTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }
            @Override
            public void onFinish() {
                progressBar.setProgress(0);
                if (!isRest) {
                    startRest();
                } else {
            
                    advance();
                }
            }
        };
        timer.start();
        isPaused = false;
        btnPause.setText(R.string.pause);
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
            isPaused = true;
            btnPause.setText(R.string.resume);
        }
    }

    private void resumeTimer() {
        startTimer(timeLeftMs);
    }

    private void advance() {
        if (timer != null) timer.cancel();
        if (isRest) {
            currentIndex++;
            if (currentIndex < exerciseList.size()) {
                startExercise();
            } else {
                finishWorkout();
            }
        } else {
            startRest();
        }
    }

    // Mostrar pantalla de fin de entrenamiento
    private void showFinishScreen() {
        findViewById(R.id.cardCentral).setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textTimer.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);

        findViewById(R.id.finalContainer).setVisibility(View.VISIBLE);
        // Cerrar la actividad automáticamente después de 3 segundos
        new android.os.Handler().postDelayed(this::finish, 3000);
    }

    private void finishWorkout() {
        showFinishScreen();
    }
}

package com.example.evolv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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
    private TextView textExerciseName, textExerciseDesc, textNextInfo, textTimer, textRestLabel;
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
        textRestLabel = findViewById(R.id.textRestLabel);
        imageExercise = findViewById(R.id.imageExercise);
        progressBar = findViewById(R.id.progressBar);
        btnPause = findViewById(R.id.btnPause);
        btnNext = findViewById(R.id.btnNext);


        // Recibir lista de ejercicios
        Intent intent = getIntent();
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

        btnPause.setOnClickListener(v -> {
            if (isPaused) {
                resumeTimer();
            } else {
                pauseTimer();
            }
        });
        btnNext.setOnClickListener(v -> advance());

        startExercise();
    }

    private void startExercise() {
    android.util.Log.d("EvolvDebug", "startExercise() called");
    isRest = false;
    Exercise exercise = exerciseList.get(currentIndex);
    android.util.Log.d("EvolvDebug", "img_url: " + exercise.getImg_url());
    textExerciseName.setText(exercise.getName());
    textExerciseDesc.setText(exercise.getDescription_text());
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
    textRestLabel.setVisibility(View.GONE);

    progressBar.setMax(DEFAULT_EXERCISE_DURATION);
    progressBar.setProgress(DEFAULT_EXERCISE_DURATION);
    timeLeftMs = DEFAULT_EXERCISE_DURATION;
    startTimer(DEFAULT_EXERCISE_DURATION);
}

    private void startRest() {
    textRestLabel.setText(getString(R.string.rest_time));
    textRestLabel.setVisibility(View.VISIBLE);
    isRest = true;
    // Mostrar texto de descanso personalizado
    if (currentIndex + 1 < exerciseList.size()) {
        Exercise next = exerciseList.get(currentIndex + 1);
        textNextInfo.setText("Prepárate para el siguiente ejercicio:");
        textNextInfo.setVisibility(View.VISIBLE);
        textExerciseName.setText(next.getName());
        textExerciseDesc.setText(next.getDescription_text());
        textExerciseName.setVisibility(View.VISIBLE);
        textExerciseDesc.setVisibility(View.VISIBLE);
    } else {
        textNextInfo.setText("");
        textExerciseName.setText("");
        textExerciseDesc.setText("");
        textNextInfo.setVisibility(View.GONE);
        textExerciseName.setVisibility(View.VISIBLE);
        textExerciseDesc.setVisibility(View.VISIBLE);
    }
    imageExercise.setVisibility(View.GONE);

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
                    textRestLabel.setVisibility(View.GONE);
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

    private void finishWorkout() {
        Toast.makeText(this, getString(R.string.workout_complete), Toast.LENGTH_LONG).show();
        finish();
    }
}

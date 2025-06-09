package com.example.evolv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evolv.DatabaseHelper;
import com.example.evolv.R;
import com.example.evolv.adapters.WorkoutCalendarAdapter;
import com.example.evolv.models.Workout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Actividad que muestra un calendario con los entrenamientos programados.
 * Permite visualizar los entrenamientos por fecha y acceder a sus detalles.
 */
public class WorkoutCalendarActivity extends AppCompatActivity implements WorkoutCalendarAdapter.OnWorkoutClickListener {
    private static final String TAG = "WorkoutCalendar";
    
    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private TextView tvNoWorkouts;
    private RecyclerView rvWorkouts;
    private WorkoutCalendarAdapter adapter;
    private DatabaseHelper dbHelper;
    private long userId;
    private String currentDate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_calendar);
        
        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_workout_calendar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Obtener el userId del intent o SharedPreferences
        userId = getIntent().getLongExtra("userId", -1);
        if (userId == -1) {
            // Obtener de SharedPreferences si no está en el intent
            userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getLong("userId", -1);
        }
        
        if (userId == -1) {
            Log.e(TAG, "No se pudo obtener el userId");
            finish();
            return;
        }
        
        // Inicializar vistas
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvNoWorkouts = findViewById(R.id.tvNoWorkouts);
        rvWorkouts = findViewById(R.id.rvWorkouts);
        
        // Configurar RecyclerView
        rvWorkouts.setLayoutManager(new LinearLayoutManager(this));
        List<Workout> workoutList = new ArrayList<>();
        adapter = new WorkoutCalendarAdapter(this, workoutList, this);
        rvWorkouts.setAdapter(adapter);
        
        // Inicializar base de datos
        dbHelper = new DatabaseHelper(this);
        
        // Configurar fecha actual
        currentDate = getCurrentDateFormatted();
        updateSelectedDateText(currentDate);
        
        // Cargar entrenamientos para hoy
        loadWorkoutsForDate(currentDate);
        
        // Configurar listener para el calendario
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Los meses en Calendar están en base 0 (enero = 0)
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            currentDate = sdf.format(selectedCalendar.getTime());
            
            updateSelectedDateText(currentDate);
            loadWorkoutsForDate(currentDate);
        });
    }
    
    /**
     * Actualiza el texto que muestra la fecha seleccionada
     */
    private void updateSelectedDateText(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            String formattedDate = date != null ? outputFormat.format(date) : dateStr;
            tvSelectedDate.setText(getString(R.string.selected_date, formattedDate));
        } catch (Exception e) {
            Log.e(TAG, "Error al formatear fecha", e);
            tvSelectedDate.setText(getString(R.string.selected_date, dateStr));
        }
    }
    
    /**
     * Obtiene la fecha actual en formato yyyy-MM-dd
     */
    private String getCurrentDateFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    /**
     * Carga los entrenamientos para la fecha seleccionada
     */
    private void loadWorkoutsForDate(String date) {
        List<Workout> workouts = dbHelper.getWorkoutsByDate(userId, date);
        
        if (workouts.isEmpty()) {
            tvNoWorkouts.setVisibility(View.VISIBLE);
            rvWorkouts.setVisibility(View.GONE);
        } else {
            tvNoWorkouts.setVisibility(View.GONE);
            rvWorkouts.setVisibility(View.VISIBLE);
            adapter.updateWorkouts(workouts);
        }
    }
    
    @Override
    public void onWorkoutClick(Workout workout) {
        // Aquí se puede implementar la navegación al detalle del workout cuando se hace clic
        // Por ahora mostraremos un log
        Log.d(TAG, "Workout seleccionado: " + workout.getName());
        
        // Ejemplo de navegación a una actividad de detalle (pendiente de implementar)
        // Intent intent = new Intent(this, WorkoutDetailActivity.class);
        // intent.putExtra("workoutId", workout.getWorkoutId());
        // startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando la actividad se reanude
        loadWorkoutsForDate(currentDate);
    }
}

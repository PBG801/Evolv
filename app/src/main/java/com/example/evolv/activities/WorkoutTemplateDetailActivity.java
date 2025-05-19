package com.example.evolv.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import com.example.evolv.DatabaseHelper;
import com.example.evolv.R;
import com.example.evolv.adapters.ExerciseBlockAdapter;
import com.example.evolv.models.WorkoutTemplate;
import com.example.evolv.models.WorkoutTemplateExercise;
import java.util.List;

/**
 * Activity que muestra el detalle de una plantilla y sus ejercicios asociados.
 */
public class WorkoutTemplateDetailActivity extends AppCompatActivity {
    private TextView textTemplateName, textTemplateType, textTemplateNotes;
    private RecyclerView recyclerView;
    private ExerciseBlockAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("WorkoutDetail", "onCreate iniciado");
        setContentView(R.layout.activity_workout_template_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textTemplateName = findViewById(R.id.textTemplateName);
        textTemplateType = findViewById(R.id.textTemplateType);
        textTemplateNotes = findViewById(R.id.textTemplateNotes);
        recyclerView = findViewById(R.id.recyclerTemplateExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        android.util.Log.d("WorkoutDetail", "DatabaseHelper inicializado");
        long templateId = getIntent().getLongExtra("TEMPLATE_ID", -1);
        android.util.Log.d("WorkoutDetail", "templateId recibido: " + templateId);
        if (templateId != -1) {
            android.util.Log.d("WorkoutDetail", "Consultando WorkoutTemplate por ID...");
            WorkoutTemplate template = dbHelper.getWorkoutTemplateById(templateId);
            if (template != null) {
                android.util.Log.d("WorkoutDetail", "WorkoutTemplate obtenido: " + template.getName());
                textTemplateName.setText(template.getName());
                textTemplateType.setText(template.getWorkoutType());
                textTemplateNotes.setText(template.getNotes());
            } else {
                android.util.Log.e("WorkoutDetail", "WorkoutTemplate es null para ID: " + templateId);
            }
            android.util.Log.d("WorkoutDetail", "Consultando ejercicios para la plantilla...");
            List<WorkoutTemplateExercise> exercises = dbHelper.getExercisesForTemplate(templateId);
android.util.Log.d("WorkoutDetail", "Ejercicios obtenidos: " + (exercises != null ? exercises.size() : "null"));
// Construir la lista de bloques para el Adapter SOLO con ejercicios individuales
// No se agrupan ni se suman sets, y no se incluyen descansos
java.util.List<ExerciseBlockAdapter.Item> blocks = new java.util.ArrayList<>();
if (exercises != null && !exercises.isEmpty()) {
    for (WorkoutTemplateExercise curr : exercises) {
        // Crear un bloque por cada ejercicio, usando sus valores individuales
        // Se asume que getSets(), getRepetitions() y getTargetDuration() nunca son null, si pueden ser null usar valores por defecto
        blocks.add(new ExerciseBlockAdapter.Item(
            ExerciseBlockAdapter.TYPE_EXERCISE,
            curr.getExercise(),
            curr.getSets() != null ? curr.getSets() : 1, // Si es null, usar 1
            curr.getRepetitions() != null ? curr.getRepetitions() : 1, // Si es null, usar 1
            curr.getTargetDuration() != null ? curr.getTargetDuration() : 0, // Si es null, usar 0
            0 // No se usa restDuration para ejercicios
        ));
    }
}
// Fin de la lista de bloques. No se agregan descansos ni lógica de agrupamiento.


adapter = new ExerciseBlockAdapter(blocks);
recyclerView.setAdapter(adapter);
android.util.Log.d("WorkoutDetail", "Adapter asignado al RecyclerView");
        } else {
            android.util.Log.e("WorkoutDetail", "templateId no válido recibido");
        }
    }
}

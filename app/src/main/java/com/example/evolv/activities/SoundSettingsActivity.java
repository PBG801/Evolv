package com.example.evolv.activities;

import android.media.ToneGenerator;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.evolv.R;
import com.example.evolv.utils.EnhancedSoundManager;

/**
 * Actividad que permite al usuario configurar los sonidos de notificación
 * para los distintos eventos durante la ejecución de un entrenamiento.
 */
public class SoundSettingsActivity extends AppCompatActivity {
    
    private EnhancedSoundManager soundManager;
    private Switch switchSoundEnabled;
    private SeekBar seekBarVolume;
    private TextView textVolume;
    private Spinner spinnerStartWorkoutSound, spinnerEndWorkoutSound, spinnerExerciseSound;
    private Button btnTestStartWorkoutSound, btnTestEndWorkoutSound, btnTestExerciseSound, btnSaveSettings;
    
    private String[] toneNames;
    private int[] toneCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_settings);
        
        // Obtener el userId del intent para mantener consistencia entre actividades
        long userId = getIntent().getLongExtra("USER_ID", 0L); // 0L como valor por defecto si no se proporciona
        android.util.Log.d("SONIDO", "SoundSettingsActivity: Recibido userId desde intent: " + userId);
        
        // Inicializar el EnhancedSoundManager con el userId recibido
        soundManager = new EnhancedSoundManager(this, userId);
        
        // Inicializar arrays de tonos
        toneNames = EnhancedSoundManager.getToneNames();
        toneCodes = EnhancedSoundManager.getToneCodes();
        
        // Configurar la ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_sound_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Inicializar vistas
        initViews();
        
        // Cargar configuración actual
        loadCurrentSettings();
        
        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        switchSoundEnabled = findViewById(R.id.switchSoundEnabled);
        seekBarVolume = findViewById(R.id.seekBarVolume);
        textVolume = findViewById(R.id.textVolume);
        
        // Añadir TextView para mostrar el estado del sonido
        TextView textSoundStatus = new TextView(this);
        textSoundStatus.setId(View.generateViewId());
        textSoundStatus.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textSoundStatus.setTag("soundStatusIndicator");

        // Insertar después del switch
        ViewGroup switchContainer = (ViewGroup) switchSoundEnabled.getParent();
        if (switchContainer != null) {
            switchContainer.addView(textSoundStatus);
        }
        
        spinnerStartWorkoutSound = findViewById(R.id.spinnerStartWorkoutSound);
        spinnerEndWorkoutSound = findViewById(R.id.spinnerEndWorkoutSound);
        spinnerExerciseSound = findViewById(R.id.spinnerExerciseSound);
        
        btnTestStartWorkoutSound = findViewById(R.id.btnTestStartWorkoutSound);
        btnTestEndWorkoutSound = findViewById(R.id.btnTestEndWorkoutSound);
        btnTestExerciseSound = findViewById(R.id.btnTestExerciseSound);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        
        // Configurar adaptadores para los spinners
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, toneNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        spinnerStartWorkoutSound.setAdapter(adapter);
        spinnerEndWorkoutSound.setAdapter(adapter);
        spinnerExerciseSound.setAdapter(adapter);
    }
    
    private void loadCurrentSettings() {
        // Cargar configuración del SoundManager
        boolean isSoundEnabled = soundManager.isSoundEnabled();
        switchSoundEnabled.setChecked(isSoundEnabled);
        seekBarVolume.setProgress(soundManager.getVolume());
        updateVolumeText(soundManager.getVolume());
        
        // Actualizar la visualización del estado del sonido
        updateSoundIndicator(isSoundEnabled);
        
        // Seleccionar los tonos actuales en los spinners
        setSpinnerToTone(spinnerStartWorkoutSound, soundManager.getStartWorkoutTone());
        setSpinnerToTone(spinnerEndWorkoutSound, soundManager.getEndWorkoutTone());
        setSpinnerToTone(spinnerExerciseSound, soundManager.getExerciseChangeTone());
    }
    
    private void setupListeners() {
        // Listener para el switch de activar/desactivar sonidos
        switchSoundEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundManager.setSoundEnabled(isChecked);
            // Guardar inmediatamente el cambio para que se aplique
            soundManager.saveSettings();
            
            // Activar/desactivar controles dependientes
            seekBarVolume.setEnabled(isChecked);
            btnTestStartWorkoutSound.setEnabled(isChecked);
            btnTestEndWorkoutSound.setEnabled(isChecked);
            btnTestExerciseSound.setEnabled(isChecked);
            
            // Actualizar la visualización del estado del sonido
            updateSoundIndicator(isChecked);
        });
        
        // Listener para la barra de volumen
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateVolumeText(progress);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No se necesita implementación
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                soundManager.setVolume(seekBar.getProgress());
            }
        });
        
        // Listeners para botones de prueba de sonidos
        btnTestStartWorkoutSound.setOnClickListener(v -> {
            int selectedTonePosition = spinnerStartWorkoutSound.getSelectedItemPosition();
            if (selectedTonePosition >= 0 && selectedTonePosition < toneCodes.length) {
                soundManager.testTone(toneCodes[selectedTonePosition]);
            }
        });
        
        btnTestEndWorkoutSound.setOnClickListener(v -> {
            int selectedTonePosition = spinnerEndWorkoutSound.getSelectedItemPosition();
            if (selectedTonePosition >= 0 && selectedTonePosition < toneCodes.length) {
                soundManager.testTone(toneCodes[selectedTonePosition]);
            }
        });
        
        btnTestExerciseSound.setOnClickListener(v -> {
            int selectedTonePosition = spinnerExerciseSound.getSelectedItemPosition();
            if (selectedTonePosition >= 0 && selectedTonePosition < toneCodes.length) {
                soundManager.testTone(toneCodes[selectedTonePosition]);
            }
        });
        
        // Listener para guardar configuración
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }
    
    private void updateVolumeText(int volume) {
        textVolume.setText(volume + "%");
    }
    
    private void setSpinnerToTone(Spinner spinner, int toneCode) {
        for (int i = 0; i < toneCodes.length; i++) {
            if (toneCodes[i] == toneCode) {
                spinner.setSelection(i);
                return;
            }
        }
        // Si no se encuentra, establecer el primero
        spinner.setSelection(0);
    }
    
    private void saveSettings() {
        // Aplicar configuraciones al SoundManager
        boolean isSoundEnabled = switchSoundEnabled.isChecked();
        soundManager.setSoundEnabled(isSoundEnabled);
        soundManager.setVolume(seekBarVolume.getProgress());
        
        int selectedStartWorkoutTone = toneCodes[spinnerStartWorkoutSound.getSelectedItemPosition()];
        int selectedEndWorkoutTone = toneCodes[spinnerEndWorkoutSound.getSelectedItemPosition()];
        int selectedExerciseTone = toneCodes[spinnerExerciseSound.getSelectedItemPosition()];
        
        soundManager.setStartWorkoutTone(selectedStartWorkoutTone);
        soundManager.setEndWorkoutTone(selectedEndWorkoutTone);
        soundManager.setExerciseChangeTone(selectedExerciseTone);
        
        // Guardar configuración
        soundManager.saveSettings();
        
        // Mostrar mensaje de confirmación
        //Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        
        // Cerrar la actividad automáticamente
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos del EnhancedSoundManager
        if (soundManager != null) {
            soundManager.release();
        }
    }
    
    /**
     * Actualiza el indicador visual que muestra si el sonido está habilitado o deshabilitado
     * @param enabled true si el sonido está habilitado, false si está deshabilitado
     */
    private void updateSoundIndicator(boolean enabled) {
        // Buscar el TextView por tag en la jerarquía de vistas
        View root = findViewById(android.R.id.content);
        View view = findViewWithTagRecursive(root, "soundStatusIndicator");
        
        if (view instanceof TextView) {
            TextView textSoundStatus = (TextView) view;
            if (enabled) {
                textSoundStatus.setText("Sonido: Activado");
                textSoundStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            } else {
                textSoundStatus.setText("Sonido: DESACTIVADO");
                textSoundStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                textSoundStatus.setTextSize(16); // Texto más grande
            }
            textSoundStatus.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Método recursivo para encontrar una vista por su tag en la jerarquía de vistas
     * @param view Vista raíz donde empezar la búsqueda
     * @param tag Tag a buscar
     * @return La vista si se encuentra, null en caso contrario
     */
    private View findViewWithTagRecursive(View view, Object tag) {
        if (tag.equals(view.getTag())) {
            return view;
        }
        
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                View result = findViewWithTagRecursive(child, tag);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
}

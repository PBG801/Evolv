package com.example.evolv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Clase singleton para gestionar la reproducción de sonidos durante la ejecución de entrenamientos.
 * Utiliza ToneGenerator para reproducir tonos nativos de Android para los eventos de cambio de 
 * repetición, serie y ejercicio.
 * 
 * La configuración de sonidos es específica para cada usuario, identificados por su ID.
 */
public class SoundManager {
    private static final String PREF_NAME = "sound_settings";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_SOUND_VOLUME = "sound_volume";
    // Mantenemos los mismos keys en SharedPreferences para compatibilidad
    private static final String KEY_START_WORKOUT_TONE = "rep_change_tone"; // Antiguo KEY_REP_TONE
    private static final String KEY_END_WORKOUT_TONE = "set_change_tone";   // Antiguo KEY_SET_TONE
    private static final String KEY_EXERCISE_TONE = "exercise_change_tone";

    // Valores por defecto
    private static final boolean DEFAULT_SOUND_ENABLED = true;
    private static final int DEFAULT_VOLUME = 80;  // 0-100 (se convertirá al rango 0-100 de ToneGenerator)
    private static final int DEFAULT_START_WORKOUT_TONE = ToneGenerator.TONE_CDMA_ABBR_ALERT;
    private static final int DEFAULT_END_WORKOUT_TONE = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE;
    private static final int DEFAULT_EXERCISE_TONE = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
    
    // Constantes para definir los tipos de eventos
    public static final int EVENT_START_WORKOUT = 0; // Antiguo EVENT_REP_CHANGE
    public static final int EVENT_END_WORKOUT = 1;   // Antiguo EVENT_SET_CHANGE
    public static final int EVENT_EXERCISE_CHANGE = 2;

    private static SoundManager instance;
    private ToneGenerator toneGenerator;
    private Handler mainHandler;
    private Context context;
    
    // ID del usuario actual para el que se carga la configuración
    private long userId = 0L; // 0L como valor por defecto para compatibilidad
    
    // Settings
    private boolean soundEnabled;
    private int volume;
    private int startWorkoutTone;
    private int endWorkoutTone;
    private int exerciseChangeTone;
    
    private SoundManager(Context context, long userId) {
        this.context = context.getApplicationContext();
        this.userId = userId;
        this.mainHandler = new Handler(Looper.getMainLooper());
        //Log.d("SONIDO", "Inicializando SoundManager para usuario " + userId);
        loadSettings();
        //Log.d("SONIDO", "SoundEnabled: " + soundEnabled + ", Volume: " + volume);
        initToneGenerator();
    }
    
    /**
     * Obtiene la instancia del SoundManager, creándola si es necesario.
     * Este método usa el userId=0 por defecto para mantener compatibilidad.
     * 
     * @param context Contexto de la aplicación
     * @return La instancia del SoundManager
     */
    public static synchronized SoundManager getInstance(Context context) {
        return getInstance(context, 0L);
    }
    
    /**
     * Obtiene la instancia del SoundManager para un usuario específico, creándola si es necesario.
     * 
     * @param context Contexto de la aplicación
     * @param userId ID del usuario para el que se cargarán las preferencias
     * @return La instancia del SoundManager con las preferencias del usuario
     */
    public static synchronized SoundManager getInstance(Context context, long userId) {
        //Log.d("SONIDO", "Solicitando instancia de SoundManager para userId: " + userId);
        if (instance == null) {
            //Log.d("SONIDO", "Creando nueva instancia de SoundManager");
            instance = new SoundManager(context, userId);
        } else if (instance.userId != userId) {
            // Si ya existe una instancia pero con diferente userId, actualizar
            //Log.d("SONIDO", "Actualizando instancia existente de SoundManager de userId: " + instance.userId + " a userId: " + userId);
            instance.userId = userId;
            instance.loadSettings(); // Cargar configuración del nuevo usuario
            instance.initToneGenerator(); // Reiniciar con la nueva configuración
        } else {
            //Log.d("SONIDO", "Devolviendo instancia existente de SoundManager");
        }
        return instance;
    }
    
    /**
     * Inicializa el ToneGenerator con los ajustes actuales
     */
    private void initToneGenerator() {
        //Log.d("SONIDO", "Inicializando ToneGenerator - soundEnabled: " + soundEnabled);
        if (toneGenerator != null) {
            //Log.d("SONIDO", "Liberando ToneGenerator existente");
            toneGenerator.release();
            toneGenerator = null;
        }
        
        if (soundEnabled) {
            // Convertir el volumen de 0-100 a 0-100 (el rango que acepta ToneGenerator)
            int streamVolume = volume;
            try {
                //Log.d("SONIDO", "Creando nuevo ToneGenerator con volumen: " + streamVolume);
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, streamVolume);
            } catch (Exception e) {
                //Log.e("SONIDO", "Error al crear ToneGenerator: " + e.getMessage());
                e.printStackTrace();
                // Si falla, intentamos con volumen por defecto
                //Log.d("SONIDO", "Intentando crear ToneGenerator con volumen por defecto: " + DEFAULT_VOLUME);
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, DEFAULT_VOLUME);
            }
        } else {
            //Log.d("SONIDO", "No se crea ToneGenerator porque soundEnabled es false");
        }
    }
    
    /**
     * Genera una clave específica para el usuario actual
     * @param baseKey Clave base
     * @return Clave con prefijo del usuario
     */
    private String getUserKey(String baseKey) {
        return userId + "_" + baseKey;
    }
    
    /**
     * Carga los ajustes guardados para el usuario actual
     */
    private void loadSettings() {
        //Log.d("SONIDO", "Cargando configuración de sonido para userId: " + userId);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        soundEnabled = prefs.getBoolean(getUserKey(KEY_SOUND_ENABLED), DEFAULT_SOUND_ENABLED);
        volume = prefs.getInt(getUserKey(KEY_SOUND_VOLUME), DEFAULT_VOLUME);
        startWorkoutTone = prefs.getInt(getUserKey(KEY_START_WORKOUT_TONE), DEFAULT_START_WORKOUT_TONE);
        endWorkoutTone = prefs.getInt(getUserKey(KEY_END_WORKOUT_TONE), DEFAULT_END_WORKOUT_TONE);
        exerciseChangeTone = prefs.getInt(getUserKey(KEY_EXERCISE_TONE), DEFAULT_EXERCISE_TONE);
        //Log.d("SONIDO", "Settings cargados - SoundEnabled: " + soundEnabled + 
        //       ", Volumen: " + volume + 
        //       ", Tonos: Inicio=" + startWorkoutTone + 
        //       ", Fin=" + endWorkoutTone + 
        //       ", Ejercicio=" + exerciseChangeTone);
    }
    
    /**
     * Guarda los ajustes actuales para el usuario actual
     */
    public void saveSettings() {
        //Log.d("SONIDO", "Guardando configuración de sonido para userId: " + userId);
        //Log.d("SONIDO", "Guardando - SoundEnabled: " + soundEnabled + 
        //       ", Volumen: " + volume + 
        //       ", Tonos: Inicio=" + startWorkoutTone + 
        //       ", Fin=" + endWorkoutTone + 
        //       ", Ejercicio=" + exerciseChangeTone);
              
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(getUserKey(KEY_SOUND_ENABLED), soundEnabled);
        editor.putInt(getUserKey(KEY_SOUND_VOLUME), volume);
        editor.putInt(getUserKey(KEY_START_WORKOUT_TONE), startWorkoutTone);
        editor.putInt(getUserKey(KEY_END_WORKOUT_TONE), endWorkoutTone);
        editor.putInt(getUserKey(KEY_EXERCISE_TONE), exerciseChangeTone);
        editor.apply();
        
        // Reiniciar el ToneGenerator con los nuevos ajustes
        initToneGenerator();
    }
    
    /**
     * Reproduce el sonido correspondiente al tipo de evento
     * @param eventType El tipo de evento (repetición, serie, ejercicio)
     */
    public void playSound(int eventType) {
        //Log.d("SONIDO", "Intento reproducir sonido tipo: " + eventType + 
        //       " (SoundEnabled: " + soundEnabled + ")");
        
        if (!soundEnabled || toneGenerator == null) {
            //Log.d("SONIDO", "Sonido NO reproducido - soundEnabled: " + soundEnabled + 
            //       ", toneGenerator: " + (toneGenerator != null));
            return;
        }
        
        int toneType;
        int duration = 300; // Duración predeterminada en ms
        
        switch (eventType) {
            case EVENT_START_WORKOUT:
                toneType = startWorkoutTone;
                duration = 600; // Más largo para inicio de entrenamiento
                //Log.d("SONIDO", "Reproduciendo sonido INICIO DE ENTRENAMIENTO");
                break;
            case EVENT_END_WORKOUT:
                toneType = endWorkoutTone;
                duration = 800; // El más largo para fin de entrenamiento
                //Log.d("SONIDO", "Reproduciendo sonido FIN DE ENTRENAMIENTO");
                break;
            case EVENT_EXERCISE_CHANGE:
                toneType = exerciseChangeTone;
                duration = 500; // Duración media para cambios de ejercicio
                //Log.d("SONIDO", "Reproduciendo sonido CAMBIO DE EJERCICIO");
                break;
            default:
                //Log.d("SONIDO", "Tipo de evento desconocido: " + eventType);
                return;
        }
        
        // Creamos copias finales para usarlas en el lambda
        final int finalToneType = toneType;
        final int finalDuration = duration;
        
        // Necesitamos reproducir el sonido en el hilo principal
        mainHandler.post(() -> {
            try {
                //Log.d("SONIDO", "Ejecutando toneGenerator.startTone con tono: " + finalToneType + 
                //       ", duración: " + finalDuration);
                toneGenerator.startTone(finalToneType, finalDuration);
            } catch (Exception e) {
                //Log.e("SONIDO", "Error al reproducir sonido: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Reproduce el sonido para inicio de entrenamiento
     */
    public void playStartWorkoutSound() {
        //Log.d("SONIDO", "Llamada a playStartWorkoutSound()");
        playSound(EVENT_START_WORKOUT);
    }
    
    /**
     * Reproduce el sonido para fin de entrenamiento
     */
    public void playEndWorkoutSound() {
        //Log.d("SONIDO", "Llamada a playEndWorkoutSound()");
        playSound(EVENT_END_WORKOUT);
    }
    
    /**
     * Reproduce el sonido para cambio de ejercicio
     */
    public void playExerciseChangeSound() {
        //Log.d("SONIDO", "Llamada a playExerciseChangeSound()");
        playSound(EVENT_EXERCISE_CHANGE);
    }
    
    /**
     * Método para probar un tono específico
     * @param tone El tono a probar
     */
    public void testTone(int tone) {
        //Log.d("SONIDO", "Probando tono: " + tone);
        if (toneGenerator == null) {
            //Log.d("SONIDO", "toneGenerator no inicializado, inicializando...");
            initToneGenerator();
        }
        if (toneGenerator != null) {
            mainHandler.post(() -> {
                try {
                    //Log.d("SONIDO", "Ejecutando test de tono: " + tone);
                    toneGenerator.startTone(tone, 300);
                } catch (Exception e) {
                    //Log.e("SONIDO", "Error al probar tono: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            //Log.d("SONIDO", "No se puede probar tono, toneGenerator sigue siendo null");
        }
    }
    
    /**
     * Libera recursos cuando ya no se necesita el SoundManager
     */
    public void release() {
        //Log.d("SONIDO", "Liberando recursos del SoundManager");
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
    
    // Métodos get/set para las configuraciones
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        //Log.d("SONIDO", "Cambiando estado de sonido: " + this.soundEnabled + " -> " + soundEnabled);
        this.soundEnabled = soundEnabled;
        initToneGenerator();
    }
    
    public int getVolume() {
        return volume;
    }
    
    public void setVolume(int volume) {
        if (volume < 0) volume = 0;
        if (volume > 100) volume = 100;
        //Log.d("SONIDO", "Cambiando volumen: " + this.volume + " -> " + volume);
        this.volume = volume;
        initToneGenerator();
    }
    
    public int getStartWorkoutTone() {
        return startWorkoutTone;
    }
    
    public void setStartWorkoutTone(int tone) {
        //Log.d("SONIDO", "Cambiando tono de inicio de entrenamiento: " + this.startWorkoutTone + " -> " + tone);
        this.startWorkoutTone = tone;
    }
    
    public int getEndWorkoutTone() {
        return endWorkoutTone;
    }
    
    public void setEndWorkoutTone(int tone) {
        //Log.d("SONIDO", "Cambiando tono de fin de entrenamiento: " + this.endWorkoutTone + " -> " + tone);
        this.endWorkoutTone = tone;
    }
    
    public int getExerciseChangeTone() {
        return exerciseChangeTone;
    }
    
    public void setExerciseChangeTone(int tone) {
        //Log.d("SONIDO", "Cambiando tono de ejercicio: " + this.exerciseChangeTone + " -> " + tone);
        this.exerciseChangeTone = tone;
    }
    
    /**
     * Devuelve los tonos disponibles con sus nombres descriptivos para mostrar en la UI
     */
    public static String[] getToneNames() {
        return new String[] {
            "Beep corto", // ToneGenerator.TONE_PROP_BEEP
            "Prompt", // ToneGenerator.TONE_PROP_PROMPT
            "Alerta llamada", // ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            "Bip confirm", // ToneGenerator.TONE_CDMA_CONFIRM
            "Tono estándar", // ToneGenerator.TONE_PROP_ACK
            "Tono ping", // ToneGenerator.TONE_SUP_PIP
            "Tono de alerta" // ToneGenerator.TONE_SUP_INTERCEPT
        };
    }
    
    /**
     * Devuelve los códigos de tonos correspondientes a los nombres
     */
    public static int[] getToneCodes() {
        return new int[] {
            ToneGenerator.TONE_PROP_BEEP,
            ToneGenerator.TONE_PROP_PROMPT,
            ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
            ToneGenerator.TONE_CDMA_CONFIRM,
            ToneGenerator.TONE_PROP_ACK,
            ToneGenerator.TONE_SUP_PIP,
            ToneGenerator.TONE_SUP_INTERCEPT
        };
    }
}

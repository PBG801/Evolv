package com.example.evolv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Sistema mejorado de gestión de sonidos para la aplicación Evolv.
 * Esta clase NO utiliza el patrón Singleton para evitar problemas de estado compartido.
 * Cada actividad debe crear su propia instancia y asegurarse de liberarla cuando ya no sea necesaria.
 */
public class EnhancedSoundManager {
    private static final String TAG = "SONIDO_ENHANCED";
    private static final String PREF_NAME = "sound_settings";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_SOUND_VOLUME = "sound_volume";
    private static final String KEY_START_WORKOUT_TONE = "rep_change_tone";
    private static final String KEY_END_WORKOUT_TONE = "set_change_tone";
    private static final String KEY_EXERCISE_TONE = "exercise_change_tone";

    // Valores por defecto
    private static final boolean DEFAULT_SOUND_ENABLED = true;
    private static final int DEFAULT_VOLUME = 80;
    private static final int DEFAULT_START_WORKOUT_TONE = ToneGenerator.TONE_CDMA_ABBR_ALERT;
    private static final int DEFAULT_END_WORKOUT_TONE = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE;
    private static final int DEFAULT_EXERCISE_TONE = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
    
    // Constantes para definir los tipos de eventos
    public static final int EVENT_START_WORKOUT = 0;
    public static final int EVENT_END_WORKOUT = 1;
    public static final int EVENT_EXERCISE_CHANGE = 2;

    // Variables de instancia
    private final Context context;
    private final long userId;
    private final Handler mainHandler;
    private ToneGenerator toneGenerator;
    private boolean toneGeneratorFailed = false;
    private long lastToneGeneratorInitTime = 0;
    private static final long RETRY_DELAY_MS = 5000; // 5 segundos entre reintentos
    
    /**
     * Constructor. Crea una nueva instancia para el usuario especificado.
     * 
     * @param context Contexto de la aplicación
     * @param userId ID del usuario para el que se aplicará la configuración
     */
    public EnhancedSoundManager(Context context, long userId) {
        this.context = context.getApplicationContext();
        this.userId = userId;
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        //Log.d(TAG, "Nueva instancia creada para usuario " + userId);
        
        // Inicializar el ToneGenerator si el sonido está habilitado
        if (isSoundEnabled()) {
            initToneGenerator();
        }
    }

    /**
     * Inicializa el ToneGenerator de manera segura
     * @return true si se inicializó correctamente, false en caso contrario
     */
    private boolean initToneGenerator() {
        // Verificar si ya hay un intento de inicialización reciente
        long now = System.currentTimeMillis();
        if (toneGeneratorFailed && (now - lastToneGeneratorInitTime < RETRY_DELAY_MS)) {
            //Log.d(TAG, "Saltando inicialización del ToneGenerator, intento reciente fallido");
            return false;
        }
        
        lastToneGeneratorInitTime = now;
        
        // Liberar el ToneGenerator existente si hay uno
        releaseToneGenerator();
        
        // Comprobar si el sonido está habilitado
        if (!isSoundEnabled()) {
            //Log.d(TAG, "No se inicializa ToneGenerator porque el sonido está deshabilitado");
            return false;
        }
        
        try {
            int volume = getVolume();
            //Log.d(TAG, "Inicializando ToneGenerator con volumen: " + volume);
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, volume);
            toneGeneratorFailed = false;
            return true;
        } catch (Exception e) {
            //Log.e(TAG, "Error al inicializar ToneGenerator: " + e.getMessage());
            toneGeneratorFailed = true;
            return false;
        }
    }
    
    /**
     * Libera los recursos del ToneGenerator de manera segura
     */
    private void releaseToneGenerator() {
        if (toneGenerator != null) {
            //Log.d(TAG, "Liberando recursos del ToneGenerator");
            try {
                toneGenerator.release();
            } catch (Exception e) {
                //Log.e(TAG, "Error al liberar ToneGenerator: " + e.getMessage());
            } finally {
                toneGenerator = null;
            }
        }
    }
    
    /**
     * Genera la clave para SharedPreferences específica de este usuario
     * @param baseKey Clave base
     * @return Clave completa con prefijo de userId
     */
    private String getUserKey(String baseKey) {
        return userId + "_" + baseKey;
    }
    
    /**
     * Obtiene el objeto SharedPreferences para esta instancia
     * @return SharedPreferences
     */
    private SharedPreferences getPreferences() {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Verifica si el sonido está habilitado para este usuario
     * @return true si el sonido está habilitado, false en caso contrario
     */
    public boolean isSoundEnabled() {
        return getPreferences().getBoolean(getUserKey(KEY_SOUND_ENABLED), DEFAULT_SOUND_ENABLED);
    }
    
    /**
     * Establece si el sonido está habilitado para este usuario
     * @param enabled true para habilitar el sonido, false para deshabilitarlo
     */
    public void setSoundEnabled(boolean enabled) {
        //Log.d(TAG, "Cambiando estado del sonido a: " + enabled + " para userId: " + userId);
        
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(getUserKey(KEY_SOUND_ENABLED), enabled);
        editor.apply();
        
        // Si se habilitó el sonido, inicializar ToneGenerator
        if (enabled) {
            initToneGenerator();
        } else {
            // Si se deshabilitó, liberar recursos
            releaseToneGenerator();
        }
    }
    
    /**
     * Obtiene el volumen configurado para este usuario
     * @return Valor del volumen (0-100)
     */
    public int getVolume() {
        return getPreferences().getInt(getUserKey(KEY_SOUND_VOLUME), DEFAULT_VOLUME);
    }
    
    /**
     * Establece el volumen para este usuario
     * @param volume Valor del volumen (0-100)
     */
    public void setVolume(int volume) {
        if (volume < 0) volume = 0;
        if (volume > 100) volume = 100;
        
        //Log.d(TAG, "Estableciendo volumen: " + volume + " para userId: " + userId);
        
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(getUserKey(KEY_SOUND_VOLUME), volume);
        editor.apply();
        
        // Reinicializar el ToneGenerator si está habilitado el sonido
        if (isSoundEnabled()) {
            initToneGenerator();
        }
    }
    
    /**
     * Obtiene el tono para inicio de entrenamiento
     */
    public int getStartWorkoutTone() {
        return getPreferences().getInt(getUserKey(KEY_START_WORKOUT_TONE), DEFAULT_START_WORKOUT_TONE);
    }
    
    /**
     * Establece el tono para inicio de entrenamiento
     */
    public void setStartWorkoutTone(int tone) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(getUserKey(KEY_START_WORKOUT_TONE), tone);
        editor.apply();
    }
    
    /**
     * Obtiene el tono para fin de entrenamiento
     */
    public int getEndWorkoutTone() {
        return getPreferences().getInt(getUserKey(KEY_END_WORKOUT_TONE), DEFAULT_END_WORKOUT_TONE);
    }
    
    /**
     * Establece el tono para fin de entrenamiento
     */
    public void setEndWorkoutTone(int tone) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(getUserKey(KEY_END_WORKOUT_TONE), tone);
        editor.apply();
    }
    
    /**
     * Obtiene el tono para cambio de ejercicio
     */
    public int getExerciseChangeTone() {
        return getPreferences().getInt(getUserKey(KEY_EXERCISE_TONE), DEFAULT_EXERCISE_TONE);
    }
    
    /**
     * Establece el tono para cambio de ejercicio
     */
    public void setExerciseChangeTone(int tone) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(getUserKey(KEY_EXERCISE_TONE), tone);
        editor.apply();
    }
    
    /**
     * Reproduce un sonido según el tipo de evento
     * @param eventType Tipo de evento (EVENT_START_WORKOUT, EVENT_END_WORKOUT, EVENT_EXERCISE_CHANGE)
     */
    public void playSound(final int eventType) {
        // Verificar siempre si el sonido está habilitado
        if (!isSoundEnabled()) {
            //Log.d(TAG, "Sonido deshabilitado para userId: " + userId + ", no se reproduce");
            return;
        }
        
        // Verificar o crear ToneGenerator si es necesario
        if (toneGenerator == null && !initToneGenerator()) {
            //Log.d(TAG, "No se pudo inicializar el ToneGenerator, no se reproduce sonido");
            return;
        }
        
        // Determinar el tono y duración según el evento
        int toneType;
        int duration;
        
        switch (eventType) {
            case EVENT_START_WORKOUT:
                toneType = getStartWorkoutTone();
                duration = 600;
                //Log.d(TAG, "Reproduciendo sonido INICIO DE ENTRENAMIENTO");
                break;
            case EVENT_END_WORKOUT:
                toneType = getEndWorkoutTone();
                duration = 800;
                //Log.d(TAG, "Reproduciendo sonido FIN DE ENTRENAMIENTO");
                break;
            case EVENT_EXERCISE_CHANGE:
                toneType = getExerciseChangeTone();
                duration = 500;
                //Log.d(TAG, "Reproduciendo sonido CAMBIO DE EJERCICIO");
                break;
            default:
                //Log.d(TAG, "Tipo de evento desconocido: " + eventType);
                return;
        }
        
        final int finalToneType = toneType;
        final int finalDuration = duration;
        
        // Reproducir el sonido en el hilo principal
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // Verificar de nuevo si el sonido sigue habilitado
                    if (isSoundEnabled() && toneGenerator != null) {
                        //Log.d(TAG, "Ejecutando startTone con tono: " + finalToneType + ", duración: " + finalDuration);
                        toneGenerator.startTone(finalToneType, finalDuration);
                    }
                } catch (Exception e) {
                    //Log.e(TAG, "Error al reproducir sonido: " + e.getMessage());
                    
                    // Intentar reinicializar para la próxima vez
                    toneGeneratorFailed = true;
                    releaseToneGenerator();
                }
            }
        });
    }
    
    /**
     * Reproduce el sonido para inicio de entrenamiento
     */
    public void playStartWorkoutSound() {
        playSound(EVENT_START_WORKOUT);
    }
    
    /**
     * Reproduce el sonido para fin de entrenamiento
     */
    public void playEndWorkoutSound() {
        playSound(EVENT_END_WORKOUT);
    }
    
    /**
     * Reproduce el sonido para cambio de ejercicio
     */
    public void playExerciseChangeSound() {
        playSound(EVENT_EXERCISE_CHANGE);
    }
    
    /**
     * Método para prueba de un tono específico
     * @param tone El código del tono a probar
     */
    public void testTone(int tone) {
        //Log.d(TAG, "Probando tono: " + tone);
        
        // Verificar si el sonido está habilitado
        if (!isSoundEnabled()) {
            //Log.d(TAG, "Sonido deshabilitado, no se puede probar tono");
            return;
        }
        
        // Inicializar el ToneGenerator si es necesario
        if (toneGenerator == null && !initToneGenerator()) {
            //Log.d(TAG, "No se pudo inicializar ToneGenerator, no se puede probar tono");
            return;
        }
        
        // Reproducir el tono en el hilo principal
        final int finalTone = tone;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (toneGenerator != null) {
                        //Log.d(TAG, "Reproduciendo tono de prueba: " + finalTone);
                        toneGenerator.startTone(finalTone, 300);
                    }
                } catch (Exception e) {
                    //Log.e(TAG, "Error al probar tono: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Libera todos los recursos. Debe llamarse cuando la actividad se destruye.
     */
    public void release() {
        //Log.d(TAG, "Liberando recursos para usuario: " + userId);
        releaseToneGenerator();
    }
    
    /**
     * Método de compatibilidad con el SoundManager original.
     * En esta implementación no es necesario llamarlo explícitamente ya que todas las
     * configuraciones se guardan automáticamente al establecerlas.
     */
    public void saveSettings() {
        Log.d(TAG, "saveSettings() llamado - No se requiere acción ya que las configuraciones se guardan automáticamente");
        // No se requiere acción ya que las configuraciones se guardan automáticamente
        // cuando se establecen mediante los métodos setter
    }
    
    /**
     * Devuelve los nombres de los tonos disponibles
     */
    public static String[] getToneNames() {
        return new String[] {
            "Beep corto", 
            "Prompt", 
            "Alerta llamada", 
            "Bip confirm", 
            "Tono estándar", 
            "Tono ping", 
            "Tono de alerta"
        };
    }
    
    /**
     * Devuelve los códigos de los tonos correspondientes a los nombres
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

package com.example.evolv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utilidad para guardar, recuperar y eliminar el progreso de una sesión de entrenamiento (v2) en SharedPreferences.
 * Permite pausar y reanudar sesiones de entrenamiento por usuario y plantilla.
 * Ahora también gestiona el estado de pausa de manera persistente.
 * Cumple con el protocolo de balanceo de llaves y claridad en comentarios.
 */
public class SessionProgressManager_v2 {
    private static final String PREFS_NAME = "workout_session_progress_v2";
    private static final String PAUSED_SESSIONS_PREFS = "paused_workout_sessions_v2";

    /**
     * Guarda el progreso de la sesión actual en SharedPreferences.
     * @param context Contexto de la aplicación
     * @param userId ID del usuario
     * @param templateId ID de la plantilla
     * @param exerciseIndex Índice del ejercicio actual
     * @param setIndex Índice de la serie actual
     * @param repIndex Índice de la repetición actual
     */
    public static void saveProgress(Context context, int userId, long templateId, int exerciseIndex, int setIndex, int repIndex) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONObject obj = new JSONObject();
        try {
            obj.put("exerciseIndex", exerciseIndex);
            obj.put("setIndex", setIndex);
            obj.put("repIndex", repIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        prefs.edit().putString(getKey(userId, templateId), obj.toString()).apply();
    }

    /**
     * Recupera el progreso guardado de una sesión, o null si no existe.
     */
    public static Progress getProgress(Context context, int userId, long templateId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(getKey(userId, templateId), null);
        if (json == null) return null;
        try {
            JSONObject obj = new JSONObject(json);
            int exerciseIndex = obj.getInt("exerciseIndex");
            int setIndex = obj.getInt("setIndex");
            int repIndex = obj.getInt("repIndex");
            return new Progress(exerciseIndex, setIndex, repIndex);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Elimina el progreso guardado de una sesión.
     */
    public static void clearProgress(Context context, int userId, long templateId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(getKey(userId, templateId)).apply();
        
        // También eliminamos el estado pausado
        setPausedState(context, userId, templateId, false);
    }
    
    /**
     * Establece el estado de pausa de una sesión
     * @param context Contexto de la aplicación
     * @param userId ID del usuario
     * @param templateId ID de la plantilla
     * @param paused true si está pausada, false en caso contrario
     */
    public static void setPausedState(Context context, int userId, long templateId, boolean paused) {
        SharedPreferences prefs = context.getSharedPreferences(PAUSED_SESSIONS_PREFS, Context.MODE_PRIVATE);
        String key = getPausedKey(userId, templateId);
        
        //Log.d("EvolvDebug", "[PAUSE] SessionProgressManager_v2.setPausedState: userId=" + userId + 
        //       ", templateId=" + templateId + ", paused=" + paused + ", key=" + key);
        
        if (paused) {
            prefs.edit().putBoolean(key, true).apply();
            //Log.d("EvolvDebug", "[PAUSE] Sesión MARCADA como pausada en SharedPreferences");
        } else {
            prefs.edit().remove(key).apply();
            //Log.d("EvolvDebug", "[PAUSE] Estado pausado ELIMINADO de SharedPreferences");
        }
    }
    
    /**
     * Comprueba si una sesión está pausada
     * @param context Contexto de la aplicación
     * @param userId ID del usuario
     * @param templateId ID de la plantilla
     * @return true si está pausada, false en caso contrario
     */
    public static boolean isPaused(Context context, int userId, long templateId) {
        SharedPreferences prefs = context.getSharedPreferences(PAUSED_SESSIONS_PREFS, Context.MODE_PRIVATE);
        String key = getPausedKey(userId, templateId);
        boolean isPaused = prefs.getBoolean(key, false);
        
        //Log.d("EvolvDebug", "[PAUSE] SessionProgressManager_v2.isPaused: userId=" + userId + 
        //       ", templateId=" + templateId + ", key=" + key + ", isPaused=" + isPaused);
        
        // Inspeccionar todas las entradas en SharedPreferences para debugging
        //Log.d("EvolvDebug", "[PAUSE] Todas las entradas en " + PAUSED_SESSIONS_PREFS + ":");
        for (String k : prefs.getAll().keySet()) {
            Object value = prefs.getAll().get(k);
            //Log.d("EvolvDebug", "[PAUSE] -----> key=" + k + ", value=" + value);
        }
        
        return isPaused;
    }
    
    /**
     * Obtiene la lista de IDs de plantillas pausadas para un usuario
     * @param context Contexto de la aplicación
     * @param userId ID del usuario
     * @return Lista de IDs de plantillas pausadas
     */
    public static java.util.List<Long> getPausedTemplateIds(Context context, int userId) {
        SharedPreferences prefs = context.getSharedPreferences(PAUSED_SESSIONS_PREFS, Context.MODE_PRIVATE);
        java.util.List<Long> result = new java.util.ArrayList<>();
        
        String prefix = "paused_" + userId + "_";
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(prefix)) {
                try {
                    String[] parts = key.split("_");
                    if (parts.length >= 3) {
                        long templateId = Long.parseLong(parts[2]);
                        result.add(templateId);
                    }
                } catch (NumberFormatException e) {
                    // Ignorar claves mal formateadas
                }
            }
        }
        return result;
    }

    private static String getKey(int userId, long templateId) {
        return "session_progress_" + userId + "_" + templateId;
    }
    
    private static String getPausedKey(int userId, long templateId) {
        return "paused_" + userId + "_" + templateId;
    }
    
    /**
     * Limpia los estados de pausa de otros usuarios para una plantilla predeterminada
     * @param context Contexto de la aplicación
     * @param currentUserId ID del usuario actual (que establece la pausa)
     * @param templateId ID de la plantilla predeterminada
     */
    public static void cleanupOtherPausedStates(Context context, int currentUserId, long templateId) {
        SharedPreferences prefs = context.getSharedPreferences(PAUSED_SESSIONS_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Buscar todas las entradas que coincidan con la plantilla pero con distintos usuarios
        String keyPrefix = "paused_";
        String keySuffix = "_" + templateId;
        int count = 0;
        
        //Log.d("EvolvDebug", "[PAUSE] Limpiando posibles estados duplicados para templateId=" + templateId + ", excluyendo userId=" + currentUserId);
        
        // Recorrer todas las claves y borrar las que correspondan a la misma plantilla pero de otros usuarios
        for (String key : prefs.getAll().keySet()) {
            if (key.endsWith(keySuffix) && key.startsWith(keyPrefix)) {
                // Extraer el userId de la clave
                try {
                    String[] parts = key.split("_");
                    if (parts.length >= 3) {
                        int keyUserId = Integer.parseInt(parts[1]);
                        
                        // Si es un userId diferente para la misma plantilla, eliminarla
                        if (keyUserId != currentUserId) {
                            //Log.d("EvolvDebug", "[PAUSE] Eliminando estado pausado duplicado: " + key + " (userId=" + keyUserId + ")");
                            editor.remove(key);
                            count++;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignorar claves malformadas
                }
            }
        }
        
        // Si se encontraron entradas para eliminar, aplicar los cambios
        if (count > 0) {
            editor.apply();
            //Log.d("EvolvDebug", "[PAUSE] Se eliminaron " + count + " estados pausados duplicados");
        } else {
            //Log.d("EvolvDebug", "[PAUSE] No se encontraron estados pausados duplicados para eliminar");
        }
    }

    /**
     * Clase interna para encapsular el progreso de una sesión.
     */
    public static class Progress {
        public int exerciseIndex;
        public int setIndex;
        public int repIndex;
        public Progress(int exerciseIndex, int setIndex, int repIndex) {
            this.exerciseIndex = exerciseIndex;
            this.setIndex = setIndex;
            this.repIndex = repIndex;
        }
    }
}

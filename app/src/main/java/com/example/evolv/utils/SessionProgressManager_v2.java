package com.example.evolv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utilidad para guardar, recuperar y eliminar el progreso de una sesión de entrenamiento (v2) en SharedPreferences.
 * Permite pausar y reanudar sesiones de entrenamiento por usuario y plantilla.
 * Cumple con el protocolo de balanceo de llaves y claridad en comentarios.
 */
public class SessionProgressManager_v2 {
    private static final String PREFS_NAME = "workout_session_progress_v2";

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
    }

    private static String getKey(int userId, long templateId) {
        return "session_progress_" + userId + "_" + templateId;
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

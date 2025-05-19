package com.example.evolv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "evolv.db";
    private static final int DATABASE_VERSION = 1;
    
    // Tabla user
    private static final String TABLE_USERS = "user";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_WEIGHT = "weight";
    private static final String COL_BIRTH_DATE = "birth_date";
    private static final String COL_GENDER = "gender";
    private static final String COL_CREATED_AT = "created_at";

    // Tabla exercise
    private static final String TABLE_EXERCISES = "exercise";
    private static final String COL_EXERCISE_ID = "exercise_id";
    private static final String COL_NAME = "name";
    private static final String COL_IMG_URL = "img_url";
    private static final String COL_DESCRIPTION_TEXT = "description_text";
    private static final String COL_CREATED_AT_EXERCISE = "created_at";

    // Tabla workout_template
    private static final String TABLE_WORKOUT_TEMPLATE = "workout_template";
    private static final String COL_TEMPLATE_ID = "template_id";
    private static final String COL_TEMPLATE_NAME = "name";
    private static final String COL_WORKOUT_TYPE = "workout_type";
    private static final String COL_TEMPLATE_NOTES = "notes";

    // Tabla workout_template_exercise
    private static final String TABLE_WORKOUT_TEMPLATE_EXERCISE = "workout_template_exercise";
    private static final String COL_WTE_TEMPLATE_ID = "template_id";
    private static final String COL_WTE_EXERCISE_ID = "exercise_id";
    private static final String COL_EXECUTION_ORDER = "execution_order";
    private static final String COL_SETS = "sets";
    private static final String COL_REPETITIONS = "repetitions";
    private static final String COL_TARGET_DURATION = "target_duration";
    private static final String COL_REST_PERIOD = "rest_period";
    private static final String COL_REST_PERIOD_SERIES = "rest_period_series";
    private static final String COL_DURATION_TYPE = "duration_type";

    private final Context context;
    private static final String DATABASE_PATH = "/data/data/com.example.evolv/databases/";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        try {
            copyDatabaseIfNeeded();
        } catch (IOException e) {
            Log.e("DatabaseHelper", "Error copiando la base de datos desde assets", e);
        }
    }

    /**
     * Copia la base de datos desde assets si no existe en la ruta interna.
     */
    private void copyDatabaseIfNeeded() throws IOException {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs();
            InputStream input = context.getAssets().open("databases/" + DATABASE_NAME);
            OutputStream output = new FileOutputStream(dbFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            input.close();
            Log.i("DatabaseHelper", "Base de datos copiada desde assets a " + dbFile.getAbsolutePath());
        } else {
            Log.i("DatabaseHelper", "Base de datos ya existe en " + dbFile.getAbsolutePath());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL, " +
                COL_WEIGHT + " REAL, " +
                COL_BIRTH_DATE + " TEXT, " +
                COL_GENDER + " TEXT, " +
                COL_CREATED_AT + " TEXT NOT NULL)";

        String createExerciseTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISES + " (" +
                COL_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_IMG_URL + " TEXT, " +
                COL_DESCRIPTION_TEXT + " TEXT, " +
                COL_CREATED_AT_EXERCISE + " TEXT NOT NULL)";

        db.execSQL(createTableSQL);
        db.execSQL(createExerciseTableSQL);
        Log.i("DatabaseHelper", "Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No necesitamos hacer nada aquí ya que la base de datos se copia de assets
    }

    public void checkTableStructure() {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table' AND name='" + TABLE_USERS + "'", null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                int sqlIndex = cursor.getColumnIndex("sql");
                if (sqlIndex != -1) {
                    String createTableSQL = cursor.getString(sqlIndex);
                    Log.d("DatabaseHelper", "Table structure: " + createTableSQL);
                } else {
                    Log.w("DatabaseHelper", "Column 'sql' not found in table structure");
                }
            }

            // Verificar las columnas de la tabla
            try (Cursor columnsCursor = db.rawQuery("PRAGMA table_info(" + TABLE_USERS + ")", null)) {
                if (columnsCursor != null) {
                    while (columnsCursor.moveToNext()) {
                        int nameIndex = columnsCursor.getColumnIndex("name");
                        int typeIndex = columnsCursor.getColumnIndex("type");
                        
                        String columnName = nameIndex != -1 ? columnsCursor.getString(nameIndex) : "Unknown";
                        String columnType = typeIndex != -1 ? columnsCursor.getString(typeIndex) : "Unknown";
                        
                        Log.d("DatabaseHelper", "Column: " + columnName + " Type: " + columnType);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking table structure", e);
        }
    }

    public boolean insertUser(String email, String password) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COL_EMAIL, email);
            values.put(COL_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
            values.put(COL_CREATED_AT, getCurrentTimestamp());
            return db.insert(TABLE_USERS, null, values) != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting user", e);
            return false;
        }
    }

    public String checkLogin(String email, String password) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS,
                     new String[]{COL_EMAIL, COL_PASSWORD},
                     COL_EMAIL + "=?",
                     new String[]{email},
                     null, null, null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(COL_PASSWORD);
                int emailIndex = cursor.getColumnIndex(COL_EMAIL);
                
                if (passwordIndex >= 0 && emailIndex >= 0) {
                    String storedHash = cursor.getString(passwordIndex);
                    String storedEmail = cursor.getString(emailIndex);
                    return BCrypt.checkpw(password, storedHash) ? storedEmail : null;
                }
            }
            return null;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking login", e);
            return null;
        }
    }

    public boolean isEmailAvailable(String email) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS,
                     new String[]{COL_EMAIL},
                     COL_EMAIL + "=?",
                     new String[]{email},
                     null, null, null)) {
            return cursor == null || !cursor.moveToFirst();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking email availability", e);
            return false;
        }
    }

    private String getCurrentTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    public void listUsers() {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS,
                     new String[]{COL_USER_ID, COL_EMAIL, COL_CREATED_AT},
                     null, null, null, null, COL_CREATED_AT + " DESC")) {

            if (cursor != null && cursor.getCount() > 0) {
                Log.i("DatabaseUsers", "=== Lista de Usuarios Registrados ===");
                Log.i("DatabaseUsers", String.format("%-5s | %-30s | %s", "ID", "Email", "Fecha de Registro"));
                Log.i("DatabaseUsers", "-----------------------------------------------------------");

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
                    String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT));

                    Log.i("DatabaseUsers", String.format("%-5d | %-30s | %s", id, email, createdAt));
                }
                Log.i("DatabaseUsers", "=== Total: " + cursor.getCount() + " usuarios ===");
            } else {
                Log.i("DatabaseUsers", "No hay usuarios registrados en la base de datos.");
            }
        } catch (Exception e) {
            Log.e("DatabaseUsers", "Error listando usuarios", e);
        }
    }

    // --- MÉTODOS PARA WORKOUT TEMPLATE Y EJERCICIOS ASOCIADOS ---

    /**
     * Devuelve todas las plantillas de entrenamiento.
     */
    public List<com.example.evolv.models.WorkoutTemplate> getAllWorkoutTemplates() {
        List<com.example.evolv.models.WorkoutTemplate> templates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT template_id, name, workout_type, notes FROM workout_template", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long templateId = cursor.getLong(cursor.getColumnIndexOrThrow("template_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("workout_type"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                templates.add(new com.example.evolv.models.WorkoutTemplate(templateId, name, type, notes));
            }
            cursor.close();
        }
        return templates;
    }

    /**
     * Devuelve una plantilla de entrenamiento por ID.
     */
    public com.example.evolv.models.WorkoutTemplate getWorkoutTemplateById(long templateId) {
        SQLiteDatabase db = this.getReadableDatabase();
        com.example.evolv.models.WorkoutTemplate template = null;
        Cursor cursor = db.rawQuery("SELECT template_id, name, workout_type, notes FROM workout_template WHERE template_id = ?", new String[]{String.valueOf(templateId)});
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("template_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("workout_type"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            template = new com.example.evolv.models.WorkoutTemplate(id, name, type, notes);
            cursor.close();
        }
        return template;
    }

    /**
     * Devuelve los ejercicios asociados a una plantilla.
     */
    public List<com.example.evolv.models.WorkoutTemplateExercise> getExercisesForTemplate(long templateId) {
        List<com.example.evolv.models.WorkoutTemplateExercise> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT wte.template_id, wte.exercise_id, wte.execution_order, wte.sets, wte.repetitions, " +
                "wte.target_duration, wte.rest_period, wte.rest_period_series, wte.duration_type, " +
                "e.exercise_id, e.name, e.img_url, e.description_text, e.created_at " +
                "FROM workout_template_exercise wte " +
                "JOIN exercise e ON wte.exercise_id = e.exercise_id " +
                "WHERE wte.template_id = ? " +
                "ORDER BY wte.execution_order ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(templateId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long tId = cursor.getLong(cursor.getColumnIndexOrThrow("template_id"));
                long eId = cursor.getLong(cursor.getColumnIndexOrThrow("exercise_id"));
                int order = cursor.getInt(cursor.getColumnIndexOrThrow("execution_order"));
                Integer sets = cursor.isNull(cursor.getColumnIndexOrThrow("sets")) ? null : cursor.getInt(cursor.getColumnIndexOrThrow("sets"));
                Integer reps = cursor.isNull(cursor.getColumnIndexOrThrow("repetitions")) ? null : cursor.getInt(cursor.getColumnIndexOrThrow("repetitions"));
                Integer duration = cursor.isNull(cursor.getColumnIndexOrThrow("target_duration")) ? null : cursor.getInt(cursor.getColumnIndexOrThrow("target_duration"));
                Integer rest = cursor.isNull(cursor.getColumnIndexOrThrow("rest_period")) ? null : cursor.getInt(cursor.getColumnIndexOrThrow("rest_period"));
                Integer restSeries = cursor.isNull(cursor.getColumnIndexOrThrow("rest_period_series")) ? null : cursor.getInt(cursor.getColumnIndexOrThrow("rest_period_series"));
                String durationType = cursor.getString(cursor.getColumnIndexOrThrow("duration_type"));
                // Datos del ejercicio asociado
                String exName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String exImg = cursor.getString(cursor.getColumnIndexOrThrow("img_url"));
                String exDesc = cursor.getString(cursor.getColumnIndexOrThrow("description_text"));
                String exCreated = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                com.example.evolv.models.Exercise exercise = new com.example.evolv.models.Exercise(eId, exName, exImg, exDesc, exCreated);
                result.add(new com.example.evolv.models.WorkoutTemplateExercise(tId, eId, order, sets, reps, duration, rest, restSeries, durationType, exercise));
            }
            cursor.close();
        }
        return result;
    }

}


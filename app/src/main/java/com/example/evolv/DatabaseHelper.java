package com.example.evolv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.mindrot.jbcrypt.BCrypt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper implements AutoCloseable {
    private static final String DATABASE_NAME = "evolv.db";
    private static final int DATABASE_VERSION = 1;
    
    // Tabla user

    /**
     * Obtiene la descripción de un ejercicio por su ID.
     * @param exerciseId ID del ejercicio
     * @return Descripción del ejercicio o cadena vacía si no se encuentra
     */
    public String getExerciseDescription(long exerciseId) {
        String description = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT description_text FROM exercise WHERE exercise_id = ?", 
                                   new String[]{String.valueOf(exerciseId)});
        if (cursor != null && cursor.moveToFirst()) {
            description = cursor.getString(cursor.getColumnIndexOrThrow("description_text"));
            cursor.close();
        }
        return description;
    }
    
    /**
     * Devuelve la lista completa de ejercicios ordenada alfabéticamente por nombre.
     */
    public List<com.example.evolv.models.Exercise> getAllExercises() {
        List<com.example.evolv.models.Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT exercise_id, name, img_url, description_text, created_at FROM exercise ORDER BY name ASC", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("exercise_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String imgUrl = cursor.getString(cursor.getColumnIndexOrThrow("img_url"));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow("description_text"));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                exercises.add(new com.example.evolv.models.Exercise(id, name, imgUrl, desc, createdAt));
            }
            cursor.close();
        }
        return exercises;
    }


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

    // Tabla workout (entrenamiento)
    private static final String TABLE_WORKOUT = "workout";
    private static final String COL_WORKOUT_ID = "workout_id";
    private static final String COL_WORKOUT_NAME = "name";
    private static final String COL_WORKOUT_DATE = "planned_date"; // Nombre real de la columna en la BD
    private static final String COL_WORKOUT_NOTES = "notes";
    private static final String COL_WORKOUT_STATE = "status";    // Nombre real de la columna en la BD
    private static final String COL_WORKOUT_USER_ID = "user_id";
    
    // Estados para los entrenamientos (valores reales en la BD)
    public static final String WORKOUT_STATE_PENDING = "planned";
    public static final String WORKOUT_STATE_COMPLETED = "completed";
    public static final String WORKOUT_STATE_NOT_COMPLETED = "missed";  // Renombrado para reflejar el valor real

    // Tabla workout_exercise (ejercicios en entrenamiento)
    private static final String TABLE_WORKOUT_EXERCISE = "workout_exercise";
    private static final String COL_WE_WORKOUT_ID = "workout_id";
    private static final String COL_WE_EXERCISE_ID = "exercise_id";
    private static final String COL_WE_EXECUTION_ORDER = "execution_order";
    private static final String COL_WE_SETS = "sets";
    private static final String COL_WE_REPETITIONS = "repetitions";
    private static final String COL_WE_TARGET_DURATION = "target_duration";
    private static final String COL_WE_REST_PERIOD = "rest_period";
    private static final String COL_WE_REST_PERIOD_SERIES = "rest_period_series";
    private static final String COL_WE_DURATION_TYPE = "duration_type";
    private static final String COL_WE_COMPLETED = "completed";
    private static final String COL_WE_NOTES = "notes";

    /**
     * Inserta una nueva plantilla de entrenamiento asociada a un usuario y devuelve el objeto creado.
     * @param template Objeto WorkoutTemplate_v2 con los datos a guardar (name, type, notes)
     * @param userId ID del usuario propietario (debe ser distinto de 0)
     * @return el objeto WorkoutTemplate_v2 insertado, o null si falla o usuario anónimo
     */
    public com.example.evolv.models.WorkoutTemplate_v2 insertWorkoutTemplate_v2(com.example.evolv.models.WorkoutTemplate_v2 template, long userId) {
    Log.d("CREA", "[insertWorkoutTemplate_v2][INICIO] userId param: " + userId + ", template.userId: " + (template != null ? template.getUserId() : "null"));
    // Log inicio
    Log.d("EvolvDebug", "[insertWorkoutTemplate_v2] INICIO. userId param: " + userId + ", template.userId: " + (template != null ? template.getUserId() : "null"));
    // No guardar si es usuario anónimo
    if (userId == 0) {
        Log.d("EvolvDebug", "[insertWorkoutTemplate_v2] userId==0, no se guarda plantilla");
        return null;
    }
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(COL_TEMPLATE_NAME, template.getName());
    values.put(COL_WORKOUT_TYPE, template.getWorkoutType());
    values.put(COL_TEMPLATE_NOTES, template.getNotes());
    values.put(COL_USER_ID, userId); // Asociar plantilla al usuario
    Log.d("EvolvDebug", "[insertWorkoutTemplate_v2] Valores a insertar: name=" + template.getName() + ", type=" + template.getWorkoutType() + ", notes=" + template.getNotes() + ", userId=" + userId);
    Log.d("CREA", "[insertWorkoutTemplate_v2] Valores a insertar: name=" + template.getName() + ", type=" + template.getWorkoutType() + ", notes=" + template.getNotes() + ", userId=" + userId);
    long id = db.insert(TABLE_WORKOUT_TEMPLATE, null, values);
    Log.d("CREA", "[insertWorkoutTemplate_v2] Resultado inserción: id=" + id);
    db.close();
    Log.d("EvolvDebug", "[insertWorkoutTemplate_v2] Resultado inserción: id=" + id);
    if (id == -1) {
        Log.d("EvolvDebug", "[insertWorkoutTemplate_v2] Error al insertar plantilla");
        return null;
    }
    com.example.evolv.models.WorkoutTemplate_v2 result = getWorkoutTemplate_v2ById(id);
    Log.d("CREA", "[insertWorkoutTemplate_v2] Plantilla insertada: " + (result != null ? result.getTemplateId() + ", userId=" + result.getUserId() : "null"));
    Log.d("EvolvDebug", "[insertWorkoutTemplate_v2] Plantilla insertada: " + (result != null ? result.getTemplateId() + ", userId=" + result.getUserId() : "null"));
    return result;
}

    /**
     * Actualiza una plantilla de entrenamiento existente asociada a un usuario y devuelve el objeto actualizado.
     * @param template Objeto WorkoutTemplate_v2 con los datos a actualizar (debe tener el ID)
     * @param userId ID del usuario propietario (debe ser distinto de 0)
     * @return el objeto WorkoutTemplate_v2 actualizado, o null si falla o usuario anónimo
     */
    public com.example.evolv.models.WorkoutTemplate_v2 updateWorkoutTemplate_v2(com.example.evolv.models.WorkoutTemplate_v2 template, long userId) {
    Log.d("CREA", "[updateWorkoutTemplate_v2][INICIO] userId param: " + userId + ", templateId: " + (template != null ? template.getTemplateId() : "null"));
    // Log inicio
    Log.d("EvolvDebug", "[updateWorkoutTemplate_v2] INICIO. userId param: " + userId + ", template.userId: " + (template != null ? template.getUserId() : "null") + ", templateId: " + (template != null ? template.getTemplateId() : "null"));
    // No actualizar si es usuario anónimo
    if (userId == 0) {
        Log.d("EvolvDebug", "[updateWorkoutTemplate_v2] userId==0, no se actualiza plantilla");
        return null;
    }
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(COL_TEMPLATE_NAME, template.getName());
    values.put(COL_WORKOUT_TYPE, template.getWorkoutType());
    values.put(COL_TEMPLATE_NOTES, template.getNotes());
    Log.d("CREA", "[updateWorkoutTemplate_v2] Valores a actualizar: name=" + template.getName() + ", type=" + template.getWorkoutType() + ", notes=" + template.getNotes() + ", templateId=" + template.getTemplateId() + ", userId=" + userId);
    int rows = db.update(TABLE_WORKOUT_TEMPLATE, values, COL_TEMPLATE_ID + "=? AND " + COL_USER_ID + "=?", new String[]{String.valueOf(template.getTemplateId()), String.valueOf(userId)});
    Log.d("CREA", "[updateWorkoutTemplate_v2] Filas modificadas: " + rows);
    db.close();
    if (rows > 0) {
        return getWorkoutTemplate_v2ById(template.getTemplateId());
    } else {
        return null;
    }
}


    // Tabla workout_template_exercise

    /**
     * Obtiene una plantilla de entrenamiento por su ID, incluyendo el campo userId.
     * @param templateId ID de la plantilla
     * @return WorkoutTemplate_v2 o null si no existe
     */
    public com.example.evolv.models.WorkoutTemplate_v2 getWorkoutTemplate_v2ById(long templateId) {
        SQLiteDatabase db = this.getReadableDatabase();
        com.example.evolv.models.WorkoutTemplate_v2 template = null;
        Cursor cursor = db.rawQuery("SELECT template_id, name, workout_type, notes, user_id FROM workout_template WHERE template_id = ? LIMIT 1", new String[]{String.valueOf(templateId)});
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("template_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("workout_type"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            long userId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"));
            template = new com.example.evolv.models.WorkoutTemplate_v2(id, name, type, notes, (int) userId, new java.util.ArrayList<com.example.evolv.models.WorkoutTemplateExercise_v2>());
        }
        if (cursor != null) cursor.close();
        return template;
    }

    /**
     * Devuelve todas las plantillas de entrenamiento (sin filtrar por usuario).
     * @return Lista de WorkoutTemplate_v2
     */
    public java.util.List<com.example.evolv.models.WorkoutTemplate_v2> getAllWorkoutTemplate() {
        java.util.List<com.example.evolv.models.WorkoutTemplate_v2> templates = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT template_id, name, workout_type, notes, user_id FROM workout_template", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("template_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("workout_type"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                long userId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"));
                templates.add(new com.example.evolv.models.WorkoutTemplate_v2(id, name, type, notes, (int) userId, new java.util.ArrayList<com.example.evolv.models.WorkoutTemplateExercise_v2>()));
            }
            cursor.close();
        }
        return templates;
    }


    /**
     * Comprueba si ya existe una plantilla de entrenamiento con el nombre proporcionado.
     * @param name Nombre de la plantilla a comprobar
     * @return true si ya existe una plantilla con ese nombre, false en caso contrario
     */
    public boolean existsWorkoutTemplate_v2Name(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_WORKOUT_TEMPLATE + " WHERE " + COL_TEMPLATE_NAME + " = ? LIMIT 1", new String[]{name});
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }
    
    /**
     * Obtiene el email del usuario por su ID
     * @param userId ID del usuario
     * @return Email del usuario o "Anónimo" si es usuario anónimo o no se encuentra
     */
    public String getUserEmail(long userId) {
        // Si es usuario anónimo, devolver directamente
        if (userId == 0) {
            return "Anónimo";
        }
        
        SQLiteDatabase db = this.getReadableDatabase();
        String email = "Usuario " + userId;
        
        Cursor cursor = db.rawQuery("SELECT " + COL_EMAIL + " FROM " + TABLE_USERS + 
                " WHERE " + COL_USER_ID + " = ? LIMIT 1", 
                new String[] { String.valueOf(userId) });
                
        if (cursor != null && cursor.moveToFirst()) {
            email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
            cursor.close();
        }
        
        return email;
    }
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
        Log.d("EvolvDB", "Intentando copiar base de datos a: " + dbFile.getAbsolutePath());
        if (!dbFile.exists()) {
            Log.d("EvolvDB", "Base de datos NO existe. Copiando desde assets...");
            File parent = dbFile.getParentFile();
            if (parent != null) {
                boolean created = parent.mkdirs();
                if (!created && !parent.exists()) {
                    Log.e("DatabaseHelper", "No se pudo crear el directorio para la base de datos: " + parent.getAbsolutePath());
                    // Puedes lanzar una excepción aquí si quieres abortar la operación
                }
            }
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
            Log.d("EvolvDB", "Copia completada. Archivo existe: " + dbFile.exists());
        } else {
            Log.i("DatabaseHelper", "Base de datos ya existe en " + dbFile.getAbsolutePath()); 
            Log.d("EvolvDB", "No se copió la base de datos porque ya existía.");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla workout_template
        // Esta tabla almacena las plantillas de entrenamiento con su tipo y notas asociadas
        String createWorkoutTemplate_v2TableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT_TEMPLATE + " ("
            + COL_TEMPLATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_TEMPLATE_NAME + " TEXT NOT NULL, "
            + COL_WORKOUT_TYPE + " TEXT, "
            + COL_TEMPLATE_NOTES + " TEXT" 
            + ")";
        db.execSQL(createWorkoutTemplate_v2TableSQL);
        // Crear tabla workout_template_exercise
        String createWorkoutTemplateExercise_v2TableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT_TEMPLATE_EXERCISE + " ("
            + COL_WTE_TEMPLATE_ID + " INTEGER NOT NULL, "
            + COL_WTE_EXERCISE_ID + " INTEGER NOT NULL, "
            + COL_EXECUTION_ORDER + " INTEGER NOT NULL, "
            + COL_SETS + " INTEGER, "
            + COL_REPETITIONS + " INTEGER, "
            + COL_TARGET_DURATION + " INTEGER, "
            + COL_REST_PERIOD + " INTEGER, "
            + COL_REST_PERIOD_SERIES + " INTEGER, "
            + COL_DURATION_TYPE + " TEXT, "
            + "PRIMARY KEY (" + COL_WTE_TEMPLATE_ID + ", " + COL_WTE_EXERCISE_ID + ", " + COL_EXECUTION_ORDER + "), "
            + "FOREIGN KEY (" + COL_WTE_TEMPLATE_ID + ") REFERENCES " + TABLE_WORKOUT_TEMPLATE + "(" + COL_TEMPLATE_ID + ") ON DELETE CASCADE, "
            + "FOREIGN KEY (" + COL_WTE_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COL_EXERCISE_ID + ") ON DELETE CASCADE"
            + ")";
        db.execSQL(createWorkoutTemplateExercise_v2TableSQL);

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

        String createWorkoutTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT + " (" +
                COL_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WORKOUT_NAME + " TEXT NOT NULL, " +
                COL_WORKOUT_DATE + " TEXT NOT NULL, " +
                COL_WORKOUT_NOTES + " TEXT, " +
                COL_WORKOUT_STATE + " TEXT NOT NULL, " +
                COL_WORKOUT_USER_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COL_WORKOUT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE)";

        String createWorkoutExerciseTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT_EXERCISE + " (" +
                COL_WE_WORKOUT_ID + " INTEGER NOT NULL, " +
                COL_WE_EXERCISE_ID + " INTEGER NOT NULL, " +
                COL_WE_EXECUTION_ORDER + " INTEGER NOT NULL, " +
                COL_WE_SETS + " INTEGER, " +
                COL_WE_REPETITIONS + " INTEGER, " +
                COL_WE_TARGET_DURATION + " INTEGER, " +
                COL_WE_REST_PERIOD + " INTEGER, " +
                COL_WE_REST_PERIOD_SERIES + " INTEGER, " +
                COL_WE_DURATION_TYPE + " TEXT, " +
                COL_WE_COMPLETED + " INTEGER NOT NULL, " +
                COL_WE_NOTES + " TEXT, " +
                "PRIMARY KEY (" + COL_WE_WORKOUT_ID + ", " + COL_WE_EXERCISE_ID + ", " + COL_WE_EXECUTION_ORDER + "), " +
                "FOREIGN KEY (" + COL_WE_WORKOUT_ID + ") REFERENCES " + TABLE_WORKOUT + "(" + COL_WORKOUT_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COL_WE_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COL_EXERCISE_ID + ") ON DELETE CASCADE)";

        db.execSQL(createTableSQL);
        db.execSQL(createExerciseTableSQL);
        db.execSQL(createWorkoutTableSQL);
        db.execSQL(createWorkoutExerciseTableSQL);
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

    /**
     * Verifica el login del usuario y retorna su userId si es correcto, o -1 si falla.
     * Este método es la base para asociar datos al usuario autenticado en toda la app.
     */
    public long checkLogin(String email, String password) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS,
                     new String[]{COL_USER_ID, COL_EMAIL, COL_PASSWORD},
                     COL_EMAIL + "=?",
                     new String[]{email},
                     null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndex(COL_PASSWORD);
                int userIdIndex = cursor.getColumnIndex(COL_USER_ID);

                if (passwordIndex >= 0 && userIdIndex >= 0) {
                    String storedHash = cursor.getString(passwordIndex);
                    long userId = cursor.getLong(userIdIndex);

                    // Comprobar si la contraseña coincide usando BCrypt
                    if (BCrypt.checkpw(password, storedHash)) {
                        return userId; // Login correcto, retornar userId
                    }
                }
            }
            return -1; // Login incorrecto
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking login", e);
            return -1;
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
     * Obtiene el nombre de una plantilla de entrenamiento por su ID.
     * @param templateId ID de la plantilla
     * @return Nombre de la plantilla o null si no existe
     */
    public String getWorkoutTemplate_v2Name(long templateId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WORKOUT_TEMPLATE, new String[]{COL_TEMPLATE_NAME}, COL_TEMPLATE_ID + "=?", new String[]{String.valueOf(templateId)}, null, null, null);
        String name = null;
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_TEMPLATE_NAME));
            cursor.close();
        }
        return name;
    }

    /**
     * Devuelve la lista de ejercicios asociados a una plantilla de entrenamiento.
     * @param templateId ID de la plantilla
     * @return Lista de WorkoutTemplateExercise_v2 asociados, ordenados por ejecución
     */
    public List<com.example.evolv.models.WorkoutTemplateExercise_v2> getExercisesForTemplate(long templateId) {
        List<com.example.evolv.models.WorkoutTemplateExercise_v2> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT wte.*, e." + COL_NAME + ", e." + COL_IMG_URL + ", e." + COL_DESCRIPTION_TEXT + ", e." + COL_CREATED_AT_EXERCISE +
                " FROM " + TABLE_WORKOUT_TEMPLATE_EXERCISE + " wte " +
                "INNER JOIN " + TABLE_EXERCISES + " e ON wte." + COL_WTE_EXERCISE_ID + " = e." + COL_EXERCISE_ID + " " +
                "WHERE wte." + COL_WTE_TEMPLATE_ID + " = ? ORDER BY wte." + COL_EXECUTION_ORDER + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(templateId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WTE_EXERCISE_ID));
                int executionOrder = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXECUTION_ORDER));
                int sets = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SETS));
                int repetitions = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REPETITIONS));
                int targetDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TARGET_DURATION));
                int restPeriod = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REST_PERIOD));
                int restPeriodSeries = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REST_PERIOD_SERIES));
                String durationType = cursor.getString(cursor.getColumnIndexOrThrow(COL_DURATION_TYPE));
                // Objeto Exercise asociado
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String imgUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMG_URL));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION_TEXT));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT_EXERCISE));
                com.example.evolv.models.Exercise exercise = new com.example.evolv.models.Exercise(exerciseId, name, imgUrl, desc, createdAt);
                list.add(new com.example.evolv.models.WorkoutTemplateExercise_v2(
    exerciseId,
    exercise.getName(),
    sets,
    repetitions,
    targetDuration,
    restPeriod,
    durationType,
    exercise.getImg_url() // Usa el campo img_url si está disponible
));
            }
            cursor.close();
        }
        android.util.Log.d("EvolvDebug", "[getExercisesForTemplate] templateId=" + templateId + " -> ejercicios recuperados: " + list.size());
        for (com.example.evolv.models.WorkoutTemplateExercise_v2 wte : list) {
            android.util.Log.d("EvolvDebug", "[getExercisesForTemplate] exerciseId=" + wte.getExerciseId() + ", order=" + 0);
        }
        Log.d("EvolvDebug", "[REP][DB] Ejercicios recuperados: " + list.size()); // REP
    for (com.example.evolv.models.WorkoutTemplateExercise_v2 wte : list) {
        Log.d("EvolvDebug", "[REP][DB] " + wte.getName() + " / " + wte.getImg_url()); // REP
    }
    return list;
    }
    /**
     * Reemplaza todos los ejercicios asociados a una plantilla de entrenamiento por una nueva lista.
     * Si alguna inserción falla, se hace rollback y no se modifica la plantilla.
     * @param templateId ID de la plantilla
     * @param exercises Nueva lista de ejercicios a asociar
     * @return true si la operación fue exitosa, false si falló (por ejemplo, si el template_id no existe)
     */
    public boolean replaceExercisesForTemplate(long templateId, List<com.example.evolv.models.WorkoutTemplateExercise_v2> exercises) {
    Log.d("CREA", "[replaceExercisesForTemplate][INICIO] templateId: " + templateId + ", exercises.size(): " + (exercises != null ? exercises.size() : -1));
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        try {
            // Validar existencia de template_id
            Cursor checkTemplate = db.rawQuery("SELECT 1 FROM " + TABLE_WORKOUT_TEMPLATE + " WHERE " + COL_TEMPLATE_ID + " = ? LIMIT 1", new String[]{String.valueOf(templateId)});
            boolean templateExists = checkTemplate != null && checkTemplate.moveToFirst();
            if (checkTemplate != null) checkTemplate.close();
            if (!templateExists) {
                Log.e("CREA", "[replaceExercisesForTemplate] ABORTADO: template_id no existe: " + templateId);
                return false;
            }
            deleteExercisesForTemplate(templateId);
            Log.d("CREA", "[replaceExercisesForTemplate] Ejercicios antiguos eliminados para templateId=" + templateId);

            // Inserción atómica: si alguna falla, rollback automático
            for (int i = 0; i < exercises.size(); i++) {
                com.example.evolv.models.WorkoutTemplateExercise_v2 wte = exercises.get(i);
                Log.d("CREA", "[replaceExercisesForTemplate] Intentando insertar ejercicio: exerciseId=" + wte.getExerciseId() + ", name=" + wte.getName() + ", order=" + i + ", sets=" + wte.getSets() + ", repetitions=" + wte.getRepetitions() + ", targetDuration=" + wte.getTargetDuration() + ", restPeriod=" + wte.getRestPeriod() + ", durationType=" + wte.getDurationType() + ", img_url=" + wte.getImg_url());
                // Asegura que durationType sea válido
                if (wte.getDurationType() == null || (!wte.getDurationType().equals("secs") && !wte.getDurationType().equals("reps"))) {
                    Log.d("CREA", "[CHECK][DURATION_TYPE] durationType inválido ('" + wte.getDurationType() + "'), se asigna 'reps'");
                    wte.setDurationType("reps");
                }
                Log.d("CREA", "[CHECK][INSERT] sets=" + wte.getSets() + ", repetitions=" + wte.getRepetitions() + ", targetDuration=" + wte.getTargetDuration() + ", restPeriod=" + wte.getRestPeriod() + ", restPeriodSeries=0, durationType='" + wte.getDurationType() + "'");
                // Si durationType es vacío o nulo, pon 'reps' por defecto
                if (wte.getDurationType() == null || wte.getDurationType().trim().isEmpty()) {
                    wte.setDurationType("reps");
                }
                long insertResult = insertWorkoutTemplateExercise_v2(
                        templateId,
                        wte.getExerciseId(),
                        i,
                        wte.getSets(),
                        wte.getRepetitions(),
                        wte.getTargetDuration(),
                        wte.getRestPeriod(),
                        0,
                        wte.getDurationType()
                );
                if (insertResult == -1) {
                    throw new RuntimeException("[replaceExercisesForTemplate] ERROR: Fallo al insertar exerciseId=" + wte.getExerciseId() + ", order=" + i);
                }
            }

            Log.d("CREA", "[replaceExercisesForTemplate] TODOS los ejercicios insertados para templateId=" + templateId);
            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            Log.e("CREA", "[replaceExercisesForTemplate] ERROR en transacción: " + e.getMessage(), e);
            success = false;
        } finally {
            db.endTransaction();
            Log.d("CREA", "[replaceExercisesForTemplate][FIN] transacción para templateId=" + templateId);
        }
        return success;
    }

    /**
     * Inserta un ejercicio asociado a una plantilla de entrenamiento.
     * @param templateId ID de la plantilla
     * @param exerciseId ID del ejercicio
     * @param executionOrder Orden de ejecución
     * @param sets Series
     * @param repetitions Repeticiones
     * @param targetDuration Duración objetivo (segundos)
     * @param restPeriod Descanso entre repeticiones
     * @param restPeriodSeries Descanso entre series
     * @param durationType Tipo de duración
     * @return id del registro insertado o -1 si falla
     */
    public long insertWorkoutTemplateExercise_v2(long templateId, long exerciseId, int executionOrder, int sets, int repetitions, int targetDuration, int restPeriod, int restPeriodSeries, String durationType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WTE_TEMPLATE_ID, templateId);
        values.put(COL_WTE_EXERCISE_ID, exerciseId);
        values.put(COL_EXECUTION_ORDER, executionOrder);
        values.put(COL_SETS, sets);
        values.put(COL_REPETITIONS, repetitions);
        values.put(COL_TARGET_DURATION, targetDuration);
        values.put(COL_REST_PERIOD, restPeriod);
        values.put(COL_REST_PERIOD_SERIES, restPeriodSeries);
        values.put(COL_DURATION_TYPE, durationType);
        long result = db.insert(TABLE_WORKOUT_TEMPLATE_EXERCISE, null, values);
        android.util.Log.d("EvolvDebug", "[insertWorkoutTemplateExercise_v2] Insert result: " + result + " para exerciseId=" + exerciseId);
        if (result == -1) {
            android.util.Log.e("EvolvDebug", "[insertWorkoutTemplateExercise_v2] ERROR: Insert fallido para exerciseId=" + exerciseId);
        }
        return result;
    }

    /**
     * Elimina todos los ejercicios asociados a una plantilla de entrenamiento.
     * @param templateId ID de la plantilla
     */
    public void deleteExercisesForTemplate(long templateId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORKOUT_TEMPLATE_EXERCISE, COL_WTE_TEMPLATE_ID + "=?", new String[]{String.valueOf(templateId)});
    }

    /**
    List<com.example.evolv.models.WorkoutTemplate_v2> templates = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.query(TABLE_WORKOUT_TEMPLATE, null, null, null, null, null, COL_TEMPLATE_NAME + " ASC");
    if (cursor != null) {
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TEMPLATE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_TEMPLATE_NAME));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_TYPE));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_TEMPLATE_NOTES));
            // Si tu modelo tiene userId, obtén aquí también el userId
            long userId = -1;
            int userIdIndex = -1;
            try { userIdIndex = cursor.getColumnIndexOrThrow("user_id"); } catch (Exception ignored) {}
            if (userIdIndex >= 0) userId = cursor.getLong(userIdIndex);
            templates.add(new com.example.evolv.models.WorkoutTemplate_v2(id, name, type, notes, (int) userId, new java.util.ArrayList<com.example.evolv.models.WorkoutTemplateExercise_v2>()));
        }
        cursor.close();
    }
    return templates;
}

    /**
     * Inserta un ejercicio si no existe (por nombre) y devuelve su ID.
     * Si ya existe, devuelve el ID existente.
     */
    public long insertExerciseIfNotExists(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Buscar si ya existe un ejercicio con ese nombre
        Cursor cursor = db.query(TABLE_EXERCISES,
                new String[]{COL_EXERCISE_ID},
                COL_NAME + "=?",
                new String[]{name},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EXERCISE_ID));
            cursor.close();
            return id;
        }
        if (cursor != null) cursor.close();
        // Si no existe, lo insertamos
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_CREATED_AT_EXERCISE, getCurrentTimestamp());
        long newId = db.insert(TABLE_EXERCISES, null, values);
        return newId;
    }
    
    /***************************
    * MÉTODOS PARA WORKOUT_EXERCISE
    ***************************/
    
    /**
     * Convierte un cursor de la tabla workout_exercise a un objeto modelo WorkoutExercise.
     * Si el cursor también contiene información del ejercicio (de un JOIN), se incluye.
     * @param cursor Cursor posicionado en el registro a convertir
     * @return Objeto WorkoutExercise con los datos del cursor
     */
    private com.example.evolv.models.WorkoutExercise cursorToWorkoutExercise(Cursor cursor) {
        long workoutId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WE_WORKOUT_ID));
        long exerciseId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WE_EXERCISE_ID));
        int executionOrder = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WE_EXECUTION_ORDER));
        int sets = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WE_SETS));
        int repetitions = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WE_REPETITIONS));
        int targetDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WE_TARGET_DURATION));
        int restPeriod = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WE_REST_PERIOD));
        int restPeriodSeries = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WE_REST_PERIOD_SERIES));
        String durationType = cursor.getString(cursor.getColumnIndexOrThrow(COL_WE_DURATION_TYPE));
        int completed = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WE_COMPLETED));
        String notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_WE_NOTES));
        
        String exerciseName = null;
        String imgUrl = null;
        String description = null;
        
        try {
            exerciseName = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
            imgUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMG_URL));
            description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION_TEXT));
        } catch (IllegalArgumentException e) {
            Log.d("EvolvDebug", "No hay datos adicionales del ejercicio en el cursor");
        }
        
        com.example.evolv.models.WorkoutExercise workoutExercise = 
            new com.example.evolv.models.WorkoutExercise(
                workoutId, exerciseId, executionOrder, sets, repetitions,
                targetDuration, restPeriod, restPeriodSeries, durationType,
                completed == 1, notes);
        
        if (exerciseName != null) {
            workoutExercise.setExerciseName(exerciseName);
            workoutExercise.setImgUrl(imgUrl);
            workoutExercise.setDescription(description);
        }
        
        return workoutExercise;
    }
    
    /**
     * Inserta un ejercicio en un entrenamiento y devuelve el objeto creado.
     * @param workoutExercise Objeto con los datos a insertar
     * @return El objeto insertado o null si falla
     */
    public com.example.evolv.models.WorkoutExercise insertWorkoutExercise(
            com.example.evolv.models.WorkoutExercise workoutExercise) {
        
        Log.d("EvolvDebug", "Insertando workout_exercise: " + workoutExercise.toString());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COL_WE_WORKOUT_ID, workoutExercise.getWorkoutId());
        values.put(COL_WE_EXERCISE_ID, workoutExercise.getExerciseId());
        values.put(COL_WE_EXECUTION_ORDER, workoutExercise.getExecutionOrder());
        values.put(COL_WE_SETS, workoutExercise.getSets());
        values.put(COL_WE_REPETITIONS, workoutExercise.getRepetitions());
        values.put(COL_WE_TARGET_DURATION, workoutExercise.getTargetDuration());
        values.put(COL_WE_REST_PERIOD, workoutExercise.getRestPeriod());
        values.put(COL_WE_REST_PERIOD_SERIES, workoutExercise.getRestPeriodSeries());
        values.put(COL_WE_DURATION_TYPE, workoutExercise.getDurationType());
        values.put(COL_WE_COMPLETED, workoutExercise.isCompleted() ? 1 : 0);
        values.put(COL_WE_NOTES, workoutExercise.getNotes());
        
        long result = db.insert(TABLE_WORKOUT_EXERCISE, null, values);
        
        if (result == -1) {
            Log.e("EvolvDebug", "Error al insertar workout_exercise");
            return null;
        } else {
            Log.d("EvolvDebug", "Workout_exercise insertado correctamente");
            return workoutExercise;
        }
    }
    
    /**
     * Actualiza los datos de un ejercicio en un entrenamiento.
     * @param workoutExercise Objeto con los datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean updateWorkoutExercise(com.example.evolv.models.WorkoutExercise workoutExercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COL_WE_SETS, workoutExercise.getSets());
        values.put(COL_WE_REPETITIONS, workoutExercise.getRepetitions());
        values.put(COL_WE_TARGET_DURATION, workoutExercise.getTargetDuration());
        values.put(COL_WE_REST_PERIOD, workoutExercise.getRestPeriod());
        values.put(COL_WE_REST_PERIOD_SERIES, workoutExercise.getRestPeriodSeries());
        values.put(COL_WE_DURATION_TYPE, workoutExercise.getDurationType());
        values.put(COL_WE_COMPLETED, workoutExercise.isCompleted() ? 1 : 0);
        values.put(COL_WE_NOTES, workoutExercise.getNotes());
        
        int result = db.update(
            TABLE_WORKOUT_EXERCISE, 
            values, 
            COL_WE_WORKOUT_ID + " = ? AND " + COL_WE_EXERCISE_ID + " = ? AND " + COL_WE_EXECUTION_ORDER + " = ?",
            new String[] {
                String.valueOf(workoutExercise.getWorkoutId()),
                String.valueOf(workoutExercise.getExerciseId()),
                String.valueOf(workoutExercise.getExecutionOrder())
            }
        );
        
        return result > 0;
    }
    
    /**
     * Obtiene un ejercicio específico de un entrenamiento por su ID compuesto.
     * @param workoutId ID del entrenamiento
     * @param exerciseId ID del ejercicio
     * @param executionOrder Orden de ejecución
     * @return Objeto WorkoutExercise o null si no existe
     */
    public com.example.evolv.models.WorkoutExercise getWorkoutExerciseById(
            long workoutId, long exerciseId, int executionOrder) {
        
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
            TABLE_WORKOUT_EXERCISE + " we LEFT JOIN " + TABLE_EXERCISES + " e ON we." + 
                COL_WE_EXERCISE_ID + " = e." + COL_EXERCISE_ID,
            null,  // Todas las columnas
            "we." + COL_WE_WORKOUT_ID + " = ? AND we." + COL_WE_EXERCISE_ID + " = ? AND we." + 
                COL_WE_EXECUTION_ORDER + " = ?",
            new String[] {
                String.valueOf(workoutId),
                String.valueOf(exerciseId),
                String.valueOf(executionOrder)
            },
            null, null, null
        );
        
        com.example.evolv.models.WorkoutExercise workoutExercise = null;
        if (cursor != null && cursor.moveToFirst()) {
            workoutExercise = cursorToWorkoutExercise(cursor);
            cursor.close();
        }
        
        return workoutExercise;
    }
    
    /**
     * Elimina un ejercicio específico de un entrenamiento.
     * @param workoutId ID del entrenamiento
     * @param exerciseId ID del ejercicio
     * @param executionOrder Orden de ejecución
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean deleteWorkoutExercise(long workoutId, long exerciseId, int executionOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        int result = db.delete(
            TABLE_WORKOUT_EXERCISE,
            COL_WE_WORKOUT_ID + " = ? AND " + COL_WE_EXERCISE_ID + " = ? AND " + COL_WE_EXECUTION_ORDER + " = ?",
            new String[] {
                String.valueOf(workoutId),
                String.valueOf(exerciseId),
                String.valueOf(executionOrder)
            }
        );
        
        return result > 0;
    }
    
    /**
     * Elimina todos los ejercicios asociados a un entrenamiento.
     * @param workoutId ID del entrenamiento
     * @return Número de ejercicios eliminados
     */
    public int deleteAllWorkoutExercises(long workoutId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        return db.delete(
            TABLE_WORKOUT_EXERCISE,
            COL_WE_WORKOUT_ID + " = ?",
            new String[] {String.valueOf(workoutId)}
        );
    }
    
    /**
     * Obtiene todos los ejercicios asociados a un entrenamiento, ordenados por orden de ejecución.
     * @param workoutId ID del entrenamiento
     * @return Lista de ejercicios del entrenamiento
     */
    public List<com.example.evolv.models.WorkoutExercise> getWorkoutExercisesByWorkoutId(long workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<com.example.evolv.models.WorkoutExercise> exercises = new ArrayList<>();
        
        String query = "SELECT we.*, e." + COL_NAME + ", e." + COL_IMG_URL + ", e." + COL_DESCRIPTION_TEXT + 
                       " FROM " + TABLE_WORKOUT_EXERCISE + " we" + 
                       " LEFT JOIN " + TABLE_EXERCISES + " e ON we." + COL_WE_EXERCISE_ID + " = e." + COL_EXERCISE_ID + 
                       " WHERE we." + COL_WE_WORKOUT_ID + " = ?" + 
                       " ORDER BY we." + COL_WE_EXECUTION_ORDER + " ASC";
        
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(workoutId)});
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                exercises.add(cursorToWorkoutExercise(cursor));
            }
            cursor.close();
        }
        
        return exercises;
    }
    
    /**
     * Crea un nuevo entrenamiento a partir de una plantilla existente, copiando todos sus ejercicios.
     * @param templateId ID de la plantilla a utilizar
     * @param userId ID del usuario propietario
     * @param workoutName Nombre para el nuevo entrenamiento (opcional, si es null se usa el nombre de la plantilla)
     * @param workoutDate Fecha del entrenamiento (si es null se usa la fecha actual)
     * @return El ID del nuevo entrenamiento creado, o -1 si falla
     */
    public long createWorkoutFromTemplate(long templateId, long userId, String workoutName, String workoutDate) {
        Log.d("EvolvDebug", "Creando workout desde template. templateId: " + templateId + ", userId: " + userId);
        
        // Obtener plantilla
        com.example.evolv.models.WorkoutTemplate_v2 template = getWorkoutTemplate_v2ById(templateId);
        if (template == null) {
            Log.e("EvolvDebug", "Template no encontrado");
            return -1;
        }
        
        // Obtener ejercicios de la plantilla
        List<com.example.evolv.models.WorkoutTemplateExercise_v2> templateExercises = getExercisesForTemplate(templateId);
        
        SQLiteDatabase db = this.getWritableDatabase();
        long newWorkoutId = -1;
        
        // Iniciamos transacción para garantizar que todo se guarde o nada
        db.beginTransaction();
        try {
            // Crear nuevo workout
            ContentValues workoutValues = new ContentValues();
            workoutValues.put(COL_WORKOUT_NAME, workoutName != null ? workoutName : template.getName());
            workoutValues.put(COL_WORKOUT_DATE, workoutDate != null ? workoutDate : getCurrentTimestamp());
            workoutValues.put(COL_WORKOUT_NOTES, template.getNotes());
            workoutValues.put(COL_WORKOUT_STATE, "pending"); // Estado inicial: pendiente
            workoutValues.put(COL_WORKOUT_USER_ID, userId);
            
            newWorkoutId = db.insert(TABLE_WORKOUT, null, workoutValues);
            
            if (newWorkoutId == -1) {
                Log.e("EvolvDebug", "Error al crear el nuevo workout");
                return -1;
            }
            
            // Copiar cada ejercicio de la plantilla al nuevo workout
            for (com.example.evolv.models.WorkoutTemplateExercise_v2 templateExercise : templateExercises) {
                ContentValues exerciseValues = new ContentValues();
                exerciseValues.put(COL_WE_WORKOUT_ID, newWorkoutId);
                exerciseValues.put(COL_WE_EXERCISE_ID, templateExercise.getExerciseId());
                exerciseValues.put(COL_WE_EXECUTION_ORDER, templateExercise.getExecutionOrder());
                exerciseValues.put(COL_WE_SETS, templateExercise.getSets());
                exerciseValues.put(COL_WE_REPETITIONS, templateExercise.getRepetitions());
                exerciseValues.put(COL_WE_TARGET_DURATION, templateExercise.getTargetDuration());
                exerciseValues.put(COL_WE_REST_PERIOD, templateExercise.getRestPeriod());
                exerciseValues.put(COL_WE_REST_PERIOD_SERIES, templateExercise.getRestPeriodSeries());
                exerciseValues.put(COL_WE_DURATION_TYPE, templateExercise.getDurationType());
                exerciseValues.put(COL_WE_COMPLETED, 0); // Inicialmente no completado
                exerciseValues.put(COL_WE_NOTES, ""); // Notas iniciales vacías
                
                long result = db.insert(TABLE_WORKOUT_EXERCISE, null, exerciseValues);
                if (result == -1) {
                    Log.e("EvolvDebug", "Error al insertar ejercicio en el nuevo workout");
                    return -1;
                }
            }
            
            // Si llegamos aquí, todo ha ido bien, confirmamos la transacción
            db.setTransactionSuccessful();
            Log.d("EvolvDebug", "Workout creado correctamente con ID: " + newWorkoutId);
        } finally {
            // Siempre terminamos la transacción, sea exitosa o no
            db.endTransaction();
        }
        
        return newWorkoutId;
    }


    /**
     * Actualiza los datos principales de una plantilla de entrenamiento existente.
     * @param templateId ID de la plantilla a actualizar
     * @param name Nuevo nombre para la plantilla
     * @param type Nuevo tipo de entrenamiento
     * @param notes Nuevas notas
     * @return Número de filas afectadas
     */
    public int updateWorkoutTemplate_v2(long templateId, String name, String type, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TEMPLATE_NAME, name);
        values.put(COL_WORKOUT_TYPE, type);
        values.put(COL_TEMPLATE_NOTES, notes);
        return db.update(TABLE_WORKOUT_TEMPLATE, values, COL_TEMPLATE_ID + "=?", new String[]{String.valueOf(templateId)});
    }

    /**
     * Elimina una plantilla de entrenamiento y todos sus ejercicios asociados.
     * @param templateId ID de la plantilla
     * @return Número de filas eliminadas en la tabla de plantillas
     */
    public int deleteWorkoutTemplateById(long templateId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Primero elimina los ejercicios asociados
        deleteExercisesForTemplate(templateId);
        // Luego elimina la plantilla
        return db.delete(TABLE_WORKOUT_TEMPLATE, COL_TEMPLATE_ID + "=?", new String[]{String.valueOf(templateId)});
    }

    /**
     * Elimina una plantilla de entrenamiento solo si pertenece al usuario especificado.
     * @param templateId ID de la plantilla
     * @param userId ID del usuario propietario
     * @return Número de filas eliminadas en la tabla de plantillas (1 si éxito, 0 si no corresponde)
     */
    public int deleteWorkoutTemplate_v2(long templateId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Primero elimina los ejercicios asociados solo si la plantilla es del usuario
        int rows = db.delete(TABLE_WORKOUT_TEMPLATE, COL_TEMPLATE_ID + "=? AND " + COL_USER_ID + "=?", new String[]{String.valueOf(templateId), String.valueOf(userId)});
        if (rows > 0) {
            deleteExercisesForTemplate(templateId);
        }
        return rows;
    }
    
    /**
     * Obtiene todos los entrenamientos de un usuario
     * @param userId ID del usuario
     * @return Lista de objetos Workout con todos los entrenamientos del usuario
     */
    public List<com.example.evolv.models.Workout> getAllWorkouts(long userId) {
        List<com.example.evolv.models.Workout> workouts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_WORKOUT + 
                       " WHERE " + COL_WORKOUT_USER_ID + " = ? " + 
                       "ORDER BY " + COL_WORKOUT_DATE + " DESC";
                       
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long workoutId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WORKOUT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_NAME));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_DATE));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_NOTES));
                String state = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_STATE));
                
                // Si no hay estado, establece el predeterminado como pendiente
                if (state == null || state.isEmpty()) {
                    state = WORKOUT_STATE_PENDING;
                }
                
                com.example.evolv.models.Workout workout = new com.example.evolv.models.Workout(
                    workoutId, name, date, notes, state, userId
                );
                
                workouts.add(workout);
            }
            cursor.close();
        }
        
        return workouts;
    }
    
    /**
     * Obtiene los entrenamientos de un usuario para una fecha específica
     * @param userId ID del usuario
     * @param date Fecha en formato yyyy-MM-dd
     * @return Lista de entrenamientos para ese día
     */
    public List<com.example.evolv.models.Workout> getWorkoutsByDate(long userId, String date) {
        List<com.example.evolv.models.Workout> workouts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_WORKOUT + 
                       " WHERE " + COL_WORKOUT_USER_ID + " = ? AND " +
                       COL_WORKOUT_DATE + " = ? " +
                       "ORDER BY " + COL_WORKOUT_ID;
                       
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), date});
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long workoutId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WORKOUT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_NAME));
                String workoutDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_DATE));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_NOTES));
                String state = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_STATE));
                
                // Si no hay estado, establece el predeterminado como pendiente
                if (state == null || state.isEmpty()) {
                    state = WORKOUT_STATE_PENDING;
                }
                
                com.example.evolv.models.Workout workout = new com.example.evolv.models.Workout(
                    workoutId, name, workoutDate, notes, state, userId
                );
                
                workouts.add(workout);
            }
            cursor.close();
        }
        
        return workouts;
    }
    
    /**
     * Obtiene los entrenamientos agrupados por fecha para un mes específico
     * @param userId ID del usuario
     * @param year Año (ej: 2025)
     * @param month Mes (1-12)
     * @return Map con clave fecha (yyyy-MM-dd) y valor lista de entrenamientos para ese día
     */
    public java.util.Map<String, List<com.example.evolv.models.Workout>> getWorkoutsByMonth(long userId, int year, int month) {
        java.util.Map<String, List<com.example.evolv.models.Workout>> result = new java.util.HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Formato de mes para consulta SQL: asegurar que tenga dos dígitos
        String monthStr = month < 10 ? "0" + month : String.valueOf(month);
        String datePattern = year + "-" + monthStr + "-%";
        
        String query = "SELECT * FROM " + TABLE_WORKOUT + 
                       " WHERE " + COL_WORKOUT_USER_ID + " = ? AND " +
                       COL_WORKOUT_DATE + " LIKE ? " +
                       "ORDER BY " + COL_WORKOUT_DATE;
                       
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), datePattern});
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long workoutId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WORKOUT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_NAME));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_DATE));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_NOTES));
                String state = cursor.getString(cursor.getColumnIndexOrThrow(COL_WORKOUT_STATE));
                
                // Si no hay estado, establece el predeterminado como pendiente
                if (state == null || state.isEmpty()) {
                    state = WORKOUT_STATE_PENDING;
                }
                
                com.example.evolv.models.Workout workout = new com.example.evolv.models.Workout(
                    workoutId, name, date, notes, state, userId
                );
                
                // Añadir al mapa agrupado por fecha
                if (!result.containsKey(date)) {
                    result.put(date, new ArrayList<>());
                }
                result.get(date).add(workout);
            }
            cursor.close();
        }
        
        return result;
    }
    
    /**
     * Actualiza el estado de un entrenamiento
     * @param workoutId ID del entrenamiento
     * @param state Nuevo estado (usar constantes WORKOUT_STATE_*)
     * @return Número de filas afectadas (1 si tuvo éxito, 0 si falló)
     */
    public int updateWorkoutState(long workoutId, String state) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WORKOUT_STATE, state);
        
        return db.update(
            TABLE_WORKOUT, 
            values, 
            COL_WORKOUT_ID + "=?", 
            new String[]{String.valueOf(workoutId)}
        );
    }
    

    
/**
 * Actualiza todos los entrenamientos vencidos (con fecha anterior a hoy) a estado no completado
 * Solo afecta a los entrenamientos que están en estado 'planned'
 * @return Número de entrenamientos actualizados
 */
public int updateExpiredWorkouts() {
    SQLiteDatabase db = this.getWritableDatabase();
    
    // Obtener la fecha actual en formato yyyy-MM-dd
    String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(new java.util.Date());
    
    // Actualizar todos los entrenamientos con fecha anterior a hoy y estado pendiente
    ContentValues values = new ContentValues();
    values.put(COL_WORKOUT_STATE, WORKOUT_STATE_NOT_COMPLETED); // Usar la constante actualizada
    
    Log.d("CAL", "updateExpiredWorkouts: Actualizando entrenamientos con " + COL_WORKOUT_DATE + " < " + currentDate + 
          " y " + COL_WORKOUT_STATE + "='" + WORKOUT_STATE_PENDING + "' a " + COL_WORKOUT_STATE + "='" + WORKOUT_STATE_NOT_COMPLETED + "'");
    
    return db.update(
        TABLE_WORKOUT, 
        values, 
        COL_WORKOUT_DATE + "<? AND " + COL_WORKOUT_STATE + "=?", // Usar las constantes actualizadas
        new String[]{currentDate, WORKOUT_STATE_PENDING}
    );
}

/**
 * Marca como completado el entrenamiento de un usuario para la fecha actual
 * Si hay múltiples entrenamientos planificados, se actualizará el primero encontrado.
 *
 * @param userId ID del usuario
 * @param date Fecha del entrenamiento (formato yyyy-MM-dd)
 * @return ID del workout actualizado o -1 si no se actualizó ninguno
 */
public long markWorkoutCompletedByDate(long userId, String date) {
    SQLiteDatabase db = this.getWritableDatabase();
    
    // Primero localizamos el entrenamiento específico
    String query = "SELECT workout_id FROM " + TABLE_WORKOUT + 
                   " WHERE " + COL_WORKOUT_USER_ID + " = ? AND " + 
                   COL_WORKOUT_DATE + " = ? AND " + 
                   COL_WORKOUT_STATE + " = ?" +
                   " LIMIT 1";
    
    Cursor cursor = db.rawQuery(query, new String[]{
        String.valueOf(userId), date, WORKOUT_STATE_PENDING
    });
    
    long workoutId = -1;
    if (cursor != null && cursor.moveToFirst()) {
        workoutId = cursor.getLong(cursor.getColumnIndexOrThrow("workout_id"));
        cursor.close();
    }
    
    if (workoutId == -1) {
        Log.d("CAL", "markWorkoutCompletedByDate: No se encontró workout con estado pending para userId=" + userId + ", date=" + date);
        return -1;
    }
    
    // Preparar los valores para actualizar
    ContentValues values = new ContentValues();
    values.put(COL_WORKOUT_STATE, WORKOUT_STATE_COMPLETED);
    
    // También actualizamos la fecha de finalización real
    String currentDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
            java.util.Locale.getDefault()).format(new java.util.Date());
    values.put("actual_end", currentDateTime);
    
    // Actualizar el entrenamiento específico por su ID
    int rowsAffected = db.update(
        TABLE_WORKOUT,
        values,
        "workout_id = ?",
        new String[]{String.valueOf(workoutId)}
    );
    
    Log.d("CAL", "markWorkoutCompletedByDate: workoutId=" + workoutId + 
          ", actualizado=" + (rowsAffected > 0) + 
          ", fecha fin=" + currentDateTime);
          
    return rowsAffected > 0 ? workoutId : -1;
}
}

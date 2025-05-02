package com.example.evolv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {
    private DatabaseHelper dbHelper;
    private Context context;
    private static final String TEST_EMAIL = "test@email.com";
    private static final String TEST_PASSWORD = "password123";

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = new DatabaseHelper(context);
        // Limpiar datos de pruebas anteriores
        cleanupTestData();
    }

    @After
    public void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("user", "email = ?", new String[]{TEST_EMAIL});
        db.close();
    }

    @Test
    public void testInsertUserSuccess() {
        // Probar inserción de usuario
        assertTrue("La inserción debería ser exitosa", 
            dbHelper.insertUser(TEST_EMAIL, TEST_PASSWORD));

        // Verificar que el email ya no está disponible
        assertFalse("El email no debería estar disponible después de insertar", 
            dbHelper.isEmailAvailable(TEST_EMAIL));
    }

    @Test
    public void testInsertDuplicateUser() {
        // Primera inserción
        assertTrue("Primera inserción debería ser exitosa",
            dbHelper.insertUser(TEST_EMAIL, TEST_PASSWORD));

        // Intentar insertar el mismo email
        assertFalse("Segunda inserción con mismo email debería fallar",
            dbHelper.insertUser(TEST_EMAIL, "otherpassword"));
    }

    @Test
    public void testLoginSuccess() {
        // Insertar usuario de prueba
        assertTrue("Inserción para prueba de login debería ser exitosa",
            dbHelper.insertUser(TEST_EMAIL, TEST_PASSWORD));

        // Intentar login
        String result = dbHelper.checkLogin(TEST_EMAIL, TEST_PASSWORD);
        assertNotNull("El login debería ser exitoso", result);
        assertEquals("El email retornado debería coincidir", TEST_EMAIL, result);
    }

    @Test
    public void testLoginFailWrongPassword() {
        // Insertar usuario de prueba
        assertTrue("Inserción para prueba de login debería ser exitosa",
            dbHelper.insertUser(TEST_EMAIL, TEST_PASSWORD));

        // Intentar login con contraseña incorrecta
        String result = dbHelper.checkLogin(TEST_EMAIL, "wrongpassword");
        assertNull("El login debería fallar con contraseña incorrecta", result);
    }

    @Test
    public void testLoginFailUserNotFound() {
        // Intentar login con usuario que no existe
        String result = dbHelper.checkLogin("nonexistent@email.com", TEST_PASSWORD);
        assertNull("El login debería fallar con usuario inexistente", result);
    }

    @Test
    public void testIsEmailAvailable() {
        // Verificar email disponible
        assertTrue("Email debería estar disponible inicialmente",
            dbHelper.isEmailAvailable(TEST_EMAIL));

        // Insertar usuario
        assertTrue("La inserción debería ser exitosa",
            dbHelper.insertUser(TEST_EMAIL, TEST_PASSWORD));

        // Verificar que ya no está disponible
        assertFalse("Email no debería estar disponible después de insertar",
            dbHelper.isEmailAvailable(TEST_EMAIL));
    }

    @Test
    public void testPasswordHashing_WorksCorrectly() {
        // Arrange
        String password = "mySecurePassword123";
        
        // Act
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        // Assert
        assertTrue("El hash de la contraseña debe ser verificable", 
                 BCrypt.checkpw(password, hashedPassword));
        assertFalse("El hash de una contraseña incorrecta no debe verificar", 
                   BCrypt.checkpw("wrongPassword", hashedPassword));
    }
    
    @Test
    public void testPasswordHashing_ProducesDifferentHashesForSamePassword() {
        // Arrange
        String password = "mySecurePassword123";
        
        // Act
        String hash1 = BCrypt.hashpw(password, BCrypt.gensalt());
        String hash2 = BCrypt.hashpw(password, BCrypt.gensalt());
        
        // Assert
        assertNotEquals("Dos hashes de la misma contraseña deben ser diferentes", 
                       hash1, hash2);
        assertTrue("Ambos hashes deben verificar la contraseña correcta", 
                  BCrypt.checkpw(password, hash1) && BCrypt.checkpw(password, hash2));
    }
}

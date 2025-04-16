package com.example.evolv;

import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.Assert.*;

public class DatabaseHelperTest {

    @Test
    public void passwordHashing_WorksCorrectly() {
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
    public void passwordHashing_ProducesDifferentHashesForSamePassword() {
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

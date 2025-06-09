// Archivo eliminado por decisión del usuario. Test obsoleto desactivado.

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import com.example.evolv.DatabaseHelper;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTemplateTest {
    private DatabaseHelper dbHelper;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    /**
     * Test de inserción y recuperación de plantilla con campos workout_type y notes.
     */



}

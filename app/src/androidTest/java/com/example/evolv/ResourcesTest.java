package com.example.evolv;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResourcesTest {
    private Context context;
    private Resources resources;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        resources = context.getResources();
    }

    @Test
    public void testColorsExist() {
        // Verificar que los colores están definidos
        int black = resources.getColor(R.color.black, null);
        int white = resources.getColor(R.color.white, null);
        int purplePrimary = resources.getColor(R.color.purple_primary, null);
        int purpleLight = resources.getColor(R.color.purple_light, null);

        // Verificar valores específicos
        assertEquals(Color.BLACK, black);
        assertEquals(Color.WHITE, white);
        assertEquals(Color.parseColor("#645195"), purplePrimary);
        assertEquals(Color.parseColor("#8B7AB0"), purpleLight);
    }

    @Test
    public void testStringsExist() {
        // Verificar que los strings existen y tienen el valor correcto
        String welcome = resources.getString(R.string.welcome);
        assertNotNull("El string 'welcome' debe existir", welcome);
        assertFalse("El string 'welcome' no debe estar vacío", welcome.isEmpty());
    }

    @Test
    public void testThemeAttributes() {
        // Crear un contexto con el tema de la app
        Context themedContext = new ContextThemeWrapper(context, R.style.Theme_Evolv);
        
        // Obtener los atributos del tema
        int[] attrs = {
            android.R.attr.colorControlNormal,
            com.google.android.material.R.attr.actionOverflowButtonStyle,
            com.google.android.material.R.attr.actionOverflowMenuStyle
        };
        
        try (android.content.res.TypedArray a = themedContext.obtainStyledAttributes(attrs)) {
            // Verificar que los atributos están definidos
            assertTrue("colorControlNormal debe estar definido", 
                      a.hasValue(0));
            assertTrue("actionOverflowButtonStyle debe estar definido", 
                      a.hasValue(1));
            assertTrue("actionOverflowMenuStyle debe estar definido", 
                      a.hasValue(2));
        }
    }

    @Test
    public void testOverflowMenuStyle() {
        Context themedContext = new ContextThemeWrapper(context, R.style.Widget_Material3_PopupMenu_Overflow_Custom);
        
        // Verificar atributos del estilo del overflow menu
        int[] attrs = {
            android.R.attr.popupBackground
        };
        
        try (android.content.res.TypedArray a = themedContext.obtainStyledAttributes(attrs)) {
            assertTrue("popupBackground debe estar definido en el estilo del overflow menu",
                      a.hasValue(0));
            
            // Verificar que el color de fondo es el morado claro
            int backgroundColor = a.getColor(0, Color.TRANSPARENT);
            assertEquals("El color de fondo debe ser purple_light",
                        Color.parseColor("#8B7AB0"), backgroundColor);
        }
    }
}

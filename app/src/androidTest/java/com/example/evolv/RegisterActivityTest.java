package com.example.evolv;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegisterActivityTest {
    private ActivityScenario<RegisterActivity> scenario;

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void emptyFields_ShowError() {
        scenario = ActivityScenario.launch(RegisterActivity.class);

        // Intentar registrar sin completar campos
        onView(withId(R.id.btnConfirmRegister)).perform(click());

        // Verificar mensaje de error
        onView(withText("Por favor complete todos los campos"))
                .inRoot(withDecorView(not(is(scenario.getResult().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void invalidEmail_ShowError() {
        scenario = ActivityScenario.launch(RegisterActivity.class);

        // Ingresar email inválido
        onView(withId(R.id.etRegUsername)).perform(typeText("invalid_email"), closeSoftKeyboard());
        onView(withId(R.id.etRegPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.etRegConfirmPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmRegister)).perform(click());

        // Verificar mensaje de error
        onView(withText("Por favor ingrese un email válido"))
                .inRoot(withDecorView(not(is(scenario.getResult().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void passwordMismatch_ShowError() {
        scenario = ActivityScenario.launch(RegisterActivity.class);

        // Ingresar contraseñas diferentes
        onView(withId(R.id.etRegUsername)).perform(typeText("test@email.com"), closeSoftKeyboard());
        onView(withId(R.id.etRegPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.etRegConfirmPassword)).perform(typeText("different123"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmRegister)).perform(click());

        // Verificar mensaje de error
        onView(withText("Las contraseñas no coinciden"))
                .inRoot(withDecorView(not(is(scenario.getResult().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void emailAlreadyExists_ShowError() {
        // Primero registramos un usuario
        DatabaseHelper db = new DatabaseHelper(ApplicationProvider.getApplicationContext());
        db.insertUser("existing@email.com", "password123");

        scenario = ActivityScenario.launch(RegisterActivity.class);

        // Intentar registrar con el mismo email
        onView(withId(R.id.etRegUsername)).perform(typeText("existing@email.com"), closeSoftKeyboard());
        onView(withId(R.id.etRegPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.etRegConfirmPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmRegister)).perform(click());

        // Verificar mensaje de error
        onView(withText("El email ya está registrado"))
                .inRoot(withDecorView(not(is(scenario.getResult().getDecorView()))))
                .check(matches(isDisplayed()));

        // Limpiar la base de datos
        db.close();
    }

    @Test
    public void successfulRegistration_NavigateToMainActivity() {
        scenario = ActivityScenario.launch(RegisterActivity.class);

        // Registrar un nuevo usuario
        onView(withId(R.id.etRegUsername)).perform(typeText("new@email.com"), closeSoftKeyboard());
        onView(withId(R.id.etRegPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.etRegConfirmPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.btnConfirmRegister)).perform(click());

        // Verificar mensaje de éxito
        onView(withText("Registro exitoso. Por favor, inicie sesión."))
                .inRoot(withDecorView(not(is(scenario.getResult().getDecorView()))))
                .check(matches(isDisplayed()));

        // Verificar navegación a MainActivity
        intended(allOf(
            hasComponent(MainActivity.class.getName()),
            hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ));

        // Limpiar la base de datos
        DatabaseHelper db = new DatabaseHelper(ApplicationProvider.getApplicationContext());
        db.close();
    }
}
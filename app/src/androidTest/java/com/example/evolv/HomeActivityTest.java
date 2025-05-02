package com.example.evolv;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeActivityTest {

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void anonymousUser_ClickCreateAccount_OpensRegisterActivity() {
        // Preparar intent para usuario anónimo
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        intent.putExtra("isAnonymous", true);
        intent.putExtra("email", "anonymous@user.com");

        // Lanzar HomeActivity
        ActivityScenario.launch(intent);

        // Verificar que el menú "Crear cuenta" está visible
        onView(withId(R.id.menu_create_account))
                .check(matches(isDisplayed()));

        // Hacer clic en "Crear cuenta"
        onView(withId(R.id.menu_create_account)).perform(click());

        // Verificar que se inicia RegisterActivity y se limpia el stack
        intended(hasComponent(RegisterActivity.class.getName()));
        intended(hasFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        ));
    }

    @Test
    public void loggedInUser_ClickLogout_OpensMainActivity() {
        // Preparar intent para usuario logueado
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        intent.putExtra("isAnonymous", false);
        intent.putExtra("email", "test@email.com");

        // Lanzar HomeActivity
        ActivityScenario.launch(intent);

        // Verificar que el botón de logout está visible
        onView(withId(R.id.btnLogout))
                .check(matches(isDisplayed()));

        // Hacer clic en logout
        onView(withId(R.id.btnLogout)).perform(click());

        // Verificar que se inicia MainActivity y se limpia el stack
        intended(hasComponent(MainActivity.class.getName()));
        intended(hasFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        ));
    }

    @Test
    public void anonymousUser_ClickLogout_OpensMainActivity() {
        // Preparar intent para usuario anónimo
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HomeActivity.class);
        intent.putExtra("isAnonymous", true);
        intent.putExtra("email", "anonymous@user.com");

        // Lanzar HomeActivity
        ActivityScenario.launch(intent);

        // Verificar que el botón de logout está visible
        onView(withId(R.id.btnLogout))
                .check(matches(isDisplayed()));

        // Hacer clic en logout
        onView(withId(R.id.btnLogout)).perform(click());

        // Verificar que se inicia MainActivity y se limpia el stack
        intended(hasComponent(MainActivity.class.getName()));
        intended(hasFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        ));
    }
}

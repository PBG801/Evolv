package com.example.evolv;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void loginScreen_HasAllRequiredElements() {
        // Verificar que todos los elementos de login están presentes
        onView(withId(R.id.etUsername))
                .check(matches(isDisplayed()));
        onView(withId(R.id.etPassword))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btnRegister))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btnAnonymous))
                .check(matches(isDisplayed()));
    }

    @Test
    public void anonymousLogin_ShowsCorrectWelcomeMessage() {
        // Click en el botón de acceso anónimo
        onView(withId(R.id.btnAnonymous))
                .perform(click());

        // Verificar que estamos en la pantalla Home
        onView(withId(R.id.tvWelcome))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.anonymous_user)));
    }
}

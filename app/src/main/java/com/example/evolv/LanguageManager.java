package com.example.evolv;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import java.util.Locale;

public class LanguageManager {
    private static final String PREFS_NAME = "LanguagePrefs";
    private static final String LANGUAGE_KEY = "SelectedLanguage";
    private final Context context;

    public LanguageManager(Context context) {
        this.context = context;
    }

    public void setLocale(String languageCode) {
        saveLanguage(languageCode);
        updateResources(languageCode);
    }

    private void saveLanguage(String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LANGUAGE_KEY, languageCode);
        editor.apply();
    }

    public String getLanguage() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LANGUAGE_KEY, "es"); // Español por defecto
    }

    private void updateResources(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        
        Configuration config = new Configuration();
        config.setLocales(localeList);
        
        context.createConfigurationContext(config);
        Locale.setDefault(locale);
    }

    public void applyLanguage() {
        String savedLanguage = getLanguage();
        updateResources(savedLanguage);
    }

    public static void recreateApp(Activity activity) {
        activity.recreate();
    }
}

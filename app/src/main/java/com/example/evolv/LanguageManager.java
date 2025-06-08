

import android.app.Activity; // NECESARIO: Para el método recreateApp(Activity activity)
import android.content.Context; // NECESARIO: Para obtener recursos y preferencias
import android.content.SharedPreferences; // NECESARIO: Para persistencia de idioma
import android.content.res.Configuration; // NECESARIO: Para actualizar configuración de idioma
import android.content.res.Resources; // NECESARIO: Para acceder a recursos
import android.os.LocaleList; // NECESARIO: Para soportar múltiples locales

import java.util.Locale; // NECESARIO: Para gestionar el idioma actual

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

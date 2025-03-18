package com.eriksandsten.homeautomation2.activity.main;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Consumer;

public class BaseActivity extends AppCompatActivity {
    private final Properties properties = new Properties();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try (InputStream applicationProps = getAssets().open("application.properties"); InputStream secretsProps = getAssets().open("secrets.properties")) {
            properties.load(applicationProps);
            properties.load(secretsProps);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public String getStringById(String stringId) {
        int resId = getResources().getIdentifier(stringId, "string", getPackageName());
        return resId != 0 ? getString(resId) : null;
    }

    public void getLocalizedResources(Locale locale, Consumer<Resources> localizedResourcesCallback) {
        Context context = getBaseContext();
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        Resources localizedResources = context.createConfigurationContext(config).getResources();
        localizedResourcesCallback.accept(localizedResources);
    }

    public String getLocalizedStringById(String stringId, Locale locale) {
        Context context = getBaseContext();
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale); // Set the desired locale

        Resources localizedResources = context.createConfigurationContext(config).getResources();

        int resId = getResources().getIdentifier(stringId, "string", getPackageName());
        return localizedResources.getString(resId);
    }

    public int getStringIdentifier(String stringId) {
        return getResources().getIdentifier(stringId, "string", getPackageName());
    }

    public String getLocalizedString(int resId, Locale locale) {
        // Create a new configuration with the specified locale
        Context context = getBaseContext();
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale); // Set the desired locale

        // Get a localized Resources instance
        Resources localizedResources = context.createConfigurationContext(config).getResources();

        // Fetch and return the localized string
        return localizedResources.getString(resId);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Bitmap fetchDrawable(String filename) {
        try {
            InputStream inputStream = getAssets().open(filename);
            return BitmapFactory.decodeStream(inputStream);
            // return Drawable.createFromStream(inputStream, null);
        } catch (final IOException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}

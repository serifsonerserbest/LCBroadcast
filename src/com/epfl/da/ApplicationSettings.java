package com.epfl.da;

public class ApplicationSettings {


    private static final ApplicationSettings applicationSettings = new ApplicationSettings();

    private ApplicationSettings() {
    }

    public static ApplicationSettings getInstance() {
        return applicationSettings;
    }
}

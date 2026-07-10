package de.a12.studio.ui;

import javafx.application.Application;

/**
 * Indirection around A12StudioApp so the shaded jar's Main-Class isn't a
 * javafx.application.Application subclass directly - launching an Application
 * subclass from a fat jar's manifest without this wrapper trips the
 * "JavaFX runtime components are missing" check on some JVMs.
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(A12StudioApp.class, args);
    }
}

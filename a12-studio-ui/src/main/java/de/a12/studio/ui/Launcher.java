package de.a12.studio.ui;

import de.a12.studio.ui.updater.UpdateApplier;
import javafx.application.Application;

/**
 *
 */
public class Launcher {

    public static void main(String[] args) {
        if (args.length > 0 && UpdateApplier.APPLY_UPDATE_FLAG.equals(args[0])) {
            UpdateApplier.run(args);
            return;
        }
        Application.launch(Studio.class, args);
    }
}

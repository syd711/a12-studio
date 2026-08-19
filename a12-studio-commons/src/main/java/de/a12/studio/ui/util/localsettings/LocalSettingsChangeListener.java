package de.a12.studio.ui.util.localsettings;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface LocalSettingsChangeListener {

  void localSettingsChanged(@NonNull String key, @Nullable String value);
}

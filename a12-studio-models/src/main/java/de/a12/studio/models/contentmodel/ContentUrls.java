package de.a12.studio.models.contentmodel;

import org.jspecify.annotations.NonNull;

import java.util.regex.Pattern;

/** The URL check SME's URL settings (Link, Video, Image source) apply to what is stored. */
public final class ContentUrls {

  // The scheme allow-list DOMPurify applies to href/src attributes: known safe schemes, or no scheme at all.
  private static final Pattern SAFE_URL = Pattern.compile(
      "^(?:(?:(?:f|ht)tps?|mailto|tel|callto|sms|cid|xmpp|matrix):|[^a-z]|[a-z+.\\-]+(?:[^a-z+.\\-:]|$))",
      Pattern.CASE_INSENSITIVE);

  private ContentUrls() {
  }

  /** Whether {@code url} passes SME's URL check: only known safe schemes, or a relative URL. */
  public static boolean isSafe(@NonNull String url) {
    return SAFE_URL.matcher(url.strip()).find();
  }
}

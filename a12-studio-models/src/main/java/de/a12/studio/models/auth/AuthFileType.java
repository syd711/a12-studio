package de.a12.studio.models.auth;

public enum AuthFileType {

  ROLES("roles.yaml"),
  USERS("users.yaml");

  private final String fileName;

  AuthFileType(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  public static AuthFileType fromFileName(String fileName) {
    for (AuthFileType type : values()) {
      if (type.fileName.equals(fileName)) {
        return type;
      }
    }
    return null;
  }
}

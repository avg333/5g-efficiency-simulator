package exception;

public class PropertiesLoadingException extends RuntimeException {
  public PropertiesLoadingException(String filename, Throwable cause) {
    super("Error loading the properties file " + filename, cause);
  }
}

package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
  private final Properties properties;

  public Config(String propertiesFileName) {
    this.properties = new Properties();
    try (final InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream(propertiesFileName)) {
      this.properties.load(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("Error loading the properties. Execution completed", e);
    }
  }

  public String getProperty(String key) {
    return this.properties.getProperty(key);
  }

  public int getIntProperty(String key) {
    return Integer.parseInt(this.properties.getProperty(key));
  }

  public double getDoubleProperty(String key) {
    return Double.parseDouble(this.properties.getProperty(key));
  }

  public boolean getBooleanProperty(String key) {
    return Boolean.parseBoolean(this.properties.getProperty(key));
  }
}

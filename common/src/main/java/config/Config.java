package config;

import exception.PropertiesLoadingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Config {
  private final Properties properties;

  public Config(final String propertiesFileName) {
    this.properties = new Properties();
    try (final InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream(propertiesFileName)) {
      this.properties.load(inputStream);
    } catch (IOException e) {
      log.error("Error loading the properties file {}", propertiesFileName, e);
      throw new PropertiesLoadingException(propertiesFileName, e);
    }
  }

  public String getString(final String key) {
    return this.properties.getProperty(key);
  }

  public int getInt(final String key) {
    return Integer.parseInt(this.properties.getProperty(key));
  }

  public double getDouble(final String key) {
    return Double.parseDouble(this.properties.getProperty(key));
  }

  public boolean getBoolean(final String key) {
    return Boolean.parseBoolean(this.properties.getProperty(key));
  }
}

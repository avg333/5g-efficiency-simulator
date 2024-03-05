package utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

class CommonUtilsTest {

  @Test
  void shouldCloseResource() throws Exception {
    final String resourceName = Instancio.create(String.class);
    final AutoCloseable resource = mock(AutoCloseable.class);

    assertDoesNotThrow(() -> CommonUtils.closeResource(resource, resourceName));
    verify(resource).close();
  }

  @Test
  void shouldDoNothingWhenResourceIsNull() {
    final String resourceName = Instancio.create(String.class);
    final AutoCloseable resource = null;

    assertDoesNotThrow(() -> CommonUtils.closeResource(resource, resourceName));
  }

  @Test
  void shouldNotThrowExceptionWhenAutoClosableThrowsException() throws Exception {
    final String resourceName = Instancio.create(String.class);
    final AutoCloseable resource = mock(AutoCloseable.class);
    final Exception exception = Instancio.create(Exception.class);

    doThrow(exception).when(resource).close();

    assertDoesNotThrow(() -> CommonUtils.closeResource(resource, resourceName));
  }
}

package com.eduskit.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EduskitConfigTest {
  @Test
  void missingWhiteboardThrows() {
    Eduskit sdk = new Eduskit(new ClientConfig("http://edu.test", "k", "s"), null);
    EduskitError error = assertThrows(EduskitError.class, sdk::whiteboardClient);
    assertEquals("SDK_CLIENT_NOT_CONFIGURED", error.getErrorCode());
  }
}

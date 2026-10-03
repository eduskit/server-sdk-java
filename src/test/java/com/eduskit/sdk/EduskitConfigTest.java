package com.eduskit.sdk;

import org.junit.jupiter.api.Test;
import com.google.gson.JsonParser;
import java.util.Map;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EduskitConfigTest {
  @Test
  void sharedRoomTokenIncludesExplicitNullGeneration() {
    ClientConfig config = new ClientConfig("http://wb.test", "app_wb", "k", "s");
    String token = TokenSigner.room(config, Map.of("roomId", "r1", "userId", "u1", "role", "host"))
        .getAsJsonObject().get("token").getAsString();
    String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
    var claims = JsonParser.parseString(payload).getAsJsonObject();
    assertTrue(claims.has("access_generation"));
    assertTrue(claims.get("access_generation").isJsonNull());
  }
  @Test
  void missingWhiteboardThrows() {
    Eduskit sdk = new Eduskit(new ClientConfig("http://edu.test", "k", "s"), null);
    EduskitError error = assertThrows(EduskitError.class, sdk::whiteboardClient);
    assertEquals("SDK_CLIENT_NOT_CONFIGURED", error.getErrorCode());
  }
}

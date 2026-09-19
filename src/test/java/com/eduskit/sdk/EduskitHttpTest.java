package com.eduskit.sdk;

import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EduskitHttpTest {
  private HttpServer server;
  private final List<String> calls = new ArrayList<>();
  private String lastKey = "";
  private int status = 200;
  private String body = "{\"code\":0,\"data\":{\"eduUserId\":\"eu_1\",\"token\":\"rt\"},\"traceId\":\"body-trace\"}";

  @BeforeEach
  void start() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/", exchange -> {
      calls.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());
      lastKey = exchange.getRequestHeaders().getFirst("x-app-key");
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.getResponseHeaders().add("x-trace-id", "hdr-trace");
      exchange.sendResponseHeaders(status, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
  }

  @AfterEach
  void stop() {
    server.stop(0);
  }

  private String baseUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  @Test
  void classroomAndWhiteboardPaths() {
    Eduskit sdk = new Eduskit(
        new ClientConfig(baseUrl(), "app_edu", "edu-key", "secret-edu"),
        new ClientConfig(baseUrl(), "app_wb", "wb-key", "secret-wb")
    );
    sdk.client().users.register(Map.of("nickname", "n", "avatar", "https://a"));
    assertEquals("edu-key", lastKey);
    assertEquals("eu_1", sdk.client().users.register(Map.of("nickname", "n", "avatar", "https://a")).getAsJsonObject().get("eduUserId").getAsString());

    JsonElement eduToken = sdk.client().auth.issueToken(Map.of("eduUserId", "eu_1", "originId", "stu"));
    JsonElement roomToken = sdk.whiteboardClient().auth.issueRoomToken(Map.of("roomId", "r1", "userId", "u1", "role", "host"));
    sdk.whiteboardClient().files.getConvertJob("job_1");

    assertTrue(calls.contains("POST /v1/users"));
    assertEquals("app_edu", eduToken.getAsJsonObject().get("appId").getAsString());
    assertEquals(3, roomToken.getAsJsonObject().get("token").getAsString().split("\\.").length);
    assertTrue(calls.contains("GET /v1/files/convert/job_1"));
    assertEquals("wb-key", lastKey);
  }

  @Test
  void errorEnvelope() {
    status = 404;
    body = "{\"code\":404,\"errorCode\":\"EDU_USER_NOT_FOUND\",\"message\":\"missing\",\"traceId\":\"err\"}";
    Eduskit sdk = new Eduskit(new ClientConfig(baseUrl(), "app_edu", "k", "secret-edu"), null);
    EduskitError error = assertThrows(EduskitError.class, () ->
        sdk.client().auth.issueToken(Map.of("eduUserId", "")));
    assertEquals("SDK_TOKEN_INPUT_INVALID", error.getErrorCode());
  }
}

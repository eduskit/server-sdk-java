package com.eduskit.sdk;

import org.junit.jupiter.api.Test;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PrivateRoomTest {
  @Test void privateRoomMethodsUseServerAuthorizationAndExactStringGenerations() throws Exception {
    var paths = new ArrayList<String>();
    var bodies = new ArrayList<com.google.gson.JsonObject>();
    var keys = new ArrayList<String>();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> {
      paths.add(exchange.getRequestURI().getPath());
      bodies.add(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
      keys.add(exchange.getRequestHeaders().getFirst("x-app-key"));
      byte[] response = "{\"code\":0,\"data\":{\"marker\":\"server-result\"}}".getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("content-type", "application/json");
      exchange.sendResponseHeaders(200, response.length);
      exchange.getResponseBody().write(response); exchange.close();
    });
    server.start();
    try {
      var wb = new WhiteboardClient(new ClientConfig("http://127.0.0.1:" + server.getAddress().getPort(), "app_wb", "wk", "ws"));
      var outputs = List.of(
        wb.rooms.provisionPrivateRoom("room_a", "assignment_a"),
        wb.rooms.changePrivateRoomGrant("room_a", Map.of("userId", "student_a", "requestId", "grant_a", "expectedGeneration", "9223372036854775806", "action", "grant", "role", "participant")),
        wb.rooms.getPrivateRoomAccess("room_a", "student_a"),
        wb.rooms.issuePrivateRoomToken("room_a", Map.of("userId", "student_a", "role", "participant")),
        wb.rooms.sealPrivateRoom("room_a"),
        wb.rooms.createFrozenSnapshot("room_a", "snapshot_a"),
        wb.rooms.getFrozenSnapshot("room_a", "snapshot_a"),
        wb.rooms.getFrozenSnapshotDownload("room_a", "snapshot_a"),
        wb.rooms.initializePrivateWorkspace("room_a", "assignment_a", null),
        wb.rooms.initializePrivateWorkspace("room_a", "assignment_a", "snapshot_a"),
        wb.rooms.getPrivateWorkspaceInitialization("room_a"),
        wb.rooms.schedulePrivateRoomWrites("room_a", "window_a", "2026-10-02T00:00:00.000Z", "2026-10-02T00:10:00.000Z")
      );
      var suffixes = List.of("", "/grants", "/access/query", "/token", "/seal", "/snapshots", "/snapshots/query", "/snapshots/download", "/initializations", "/initializations", "/initializations/query", "/write-window");
      assertEquals(12, paths.size());
      for (int i=0; i<12; i++) {
        assertEquals("/v1/rooms/private" + suffixes.get(i), paths.get(i));
        assertEquals("room_a", bodies.get(i).get("roomId").getAsString());
        assertEquals("wk", keys.get(i));
        assertEquals("server-result", outputs.get(i).getAsJsonObject().get("marker").getAsString());
      }
      assertTrue(bodies.get(1).getAsJsonPrimitive("expectedGeneration").isString());
      assertEquals("9223372036854775806", bodies.get(1).get("expectedGeneration").getAsString());
      assertFalse(bodies.get(3).has("accessGeneration"));
      assertTrue(bodies.get(8).has("sourceSnapshotId"));
      assertTrue(bodies.get(8).get("sourceSnapshotId").isJsonNull());
      assertEquals("assignment_a", bodies.get(8).get("assignmentId").getAsString());
      assertEquals("snapshot_a", bodies.get(9).get("sourceSnapshotId").getAsString());
      assertEquals(1, bodies.get(10).size());
      assertEquals("window_a", bodies.get(11).get("requestId").getAsString());
      assertEquals("2026-10-02T00:00:00.000Z", bodies.get(11).get("opensAt").getAsString());
      assertEquals("2026-10-02T00:10:00.000Z", bodies.get(11).get("closesAt").getAsString());
      assertEquals(4, bodies.get(11).size());
    } finally { server.stop(0); }
  }
}

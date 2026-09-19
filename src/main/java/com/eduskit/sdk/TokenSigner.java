package com.eduskit.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

final class TokenSigner {
  private static final Gson GSON = new Gson();
  private TokenSigner() {}

  static JsonElement edu(ClientConfig config, Map<String, Object> input) {
    String userId = text(input.get("eduUserId"), "eduUserId", "classroom");
    long expiresIn = expiry(input, 86400, "classroom");
    JsonObject claims = new JsonObject();
    claims.addProperty("appId", require(config.appId, "appId", "classroom"));
    optional(claims, "originId", input.get("originId"));
    optional(claims, "role", input.get("role"));
    Signed signed = sign(config, userId, "eduskit-edu-auth", "eduskit-edu", expiresIn, claims, "classroom");
    JsonObject out = new JsonObject();
    out.addProperty("accessToken", signed.token); out.addProperty("expiresIn", expiresIn);
    out.addProperty("expiresAt", iso(signed.expiresAt)); out.addProperty("tokenType", "Bearer");
    out.addProperty("appId", config.appId); out.addProperty("eduUserId", userId);
    out.addProperty("originId", stringOrEmpty(input.get("originId")));
    out.addProperty("role", stringOrEmpty(input.get("role")));
    return out;
  }

  static JsonElement room(ClientConfig config, Map<String, Object> input) {
    String roomId = text(input.get("roomId"), "roomId", "whiteboard");
    String userId = text(input.get("userId"), "userId", "whiteboard");
    String role = text(input.get("role"), "role", "whiteboard");
    if (!role.equals("host") && !role.equals("participant") && !role.equals("observer")) error("invalid role", "whiteboard");
    long expiresIn = expiry(input, 3600, "whiteboard");
    JsonObject claims = new JsonObject(); claims.addProperty("app_id", config.appId);
    claims.addProperty("room_id", roomId); claims.addProperty("role", role); claims.addProperty("source", "server_sdk");
    Signed signed = sign(config, userId, "eduskit", "eduskit-room", expiresIn, claims, "whiteboard");
    JsonObject out = new JsonObject(); out.addProperty("token", signed.token); out.addProperty("appId", config.appId);
    out.addProperty("roomId", roomId); out.addProperty("userId", userId); out.addProperty("role", role);
    out.addProperty("expiresIn", expiresIn); out.addProperty("expiresAt", iso(signed.expiresAt)); return out;
  }

  private static Signed sign(ClientConfig config, String subject, String issuer, String audience,
                             long expiresIn, JsonObject claims, String source) {
    require(config.appId, "appId", source); require(config.appSecret, "appSecret", source);
    if (expiresIn < 60 || expiresIn > 604800) error("expiresIn must be between 60 and 604800", source);
    long now = Instant.now().getEpochSecond(), expiresAt = now + expiresIn;
    claims.addProperty("iss", issuer); claims.addProperty("aud", audience); claims.addProperty("sub", subject);
    claims.addProperty("iat", now); claims.addProperty("exp", expiresAt);
    String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    String payload = b64(GSON.toJson(claims).getBytes(StandardCharsets.UTF_8)); String content = header + "." + payload;
    try {
      Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(config.appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return new Signed(content + "." + b64(mac.doFinal(content.getBytes(StandardCharsets.UTF_8))), expiresAt);
    } catch (Exception exception) { throw new IllegalStateException("cannot sign SDK token", exception); }
  }

  private static long expiry(Map<String, Object> input, long fallback, String source) {
    Object value = input.get("expiresIn"); if (value == null) return fallback;
    if (!(value instanceof Number) || ((Number) value).doubleValue() != ((Number) value).longValue()) error("expiresIn must be an integer", source);
    return ((Number) value).longValue();
  }
  private static String text(Object value, String field, String source) { return require(value instanceof String ? (String)value : "", field, source); }
  private static String require(String value, String field, String source) { if (value == null || value.isBlank()) error(field + " is required", source); return value; }
  private static String stringOrEmpty(Object value) { return value instanceof String ? (String)value : ""; }
  private static void optional(JsonObject object, String key, Object value) { if (value instanceof String && !((String)value).isBlank()) object.addProperty(key, (String)value); }
  private static String b64(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
  private static String iso(long seconds) { return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(seconds)); }
  private static void error(String message, String source) { throw new EduskitError(message, null, "SDK_TOKEN_INPUT_INVALID", null, null, source); }
  private static final class Signed { final String token; final long expiresAt; Signed(String token, long expiresAt) { this.token = token; this.expiresAt = expiresAt; } }
}

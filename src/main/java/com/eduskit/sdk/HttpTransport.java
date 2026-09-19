package com.eduskit.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class HttpTransport {
  private static final Gson GSON = new Gson();
  private final String baseUrl;
  private final String appKey;
  private final String appSecret;
  private final String lang;
  private final String source;
  private final HttpClient httpClient;
  private final ClientConfig config;
  String lastTraceId = "";

  HttpTransport(ClientConfig config, String source) {
    this.config = config;
    this.baseUrl = config.baseUrl.replaceAll("/+$", "");
    this.appKey = config.appKey;
    this.appSecret = config.appSecret;
    this.lang = config.lang;
    this.source = source;
    this.httpClient = config.httpClient != null ? config.httpClient : HttpClient.newBuilder().connectTimeout(config.timeout).build();
  }

  JsonElement request(String method, String path, Object body) {
    try {
      String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + path))
          .timeout(config.timeout)
          .header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .header("x-app-key", appKey)
          .header("x-app-secret", appSecret)
          .header("x-lang", lang)
          .header("x-trace-id", traceId);
      String payload = body == null ? "" : GSON.toJson(body);
      builder.method(method, payload.isEmpty()
          ? HttpRequest.BodyPublishers.noBody()
          : HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
      HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      JsonObject envelope = response.body() == null || response.body().isBlank()
          ? new JsonObject()
          : GSON.fromJson(response.body(), JsonObject.class);
      String headerTrace = response.headers().firstValue("x-trace-id").orElse(traceId);
      lastTraceId = envelope.has("traceId") && !envelope.get("traceId").isJsonNull()
          ? envelope.get("traceId").getAsString()
          : headerTrace;
      int code = envelope.has("code") ? envelope.get("code").getAsInt() : 0;
      if (response.statusCode() >= 400 || code != 0) {
        String message = envelope.has("message") ? envelope.get("message").getAsString() : ("HTTP " + response.statusCode());
        String errorCode = envelope.has("errorCode") ? envelope.get("errorCode").getAsString() : ("HTTP_" + response.statusCode());
        String errPath = envelope.has("path") ? envelope.get("path").getAsString() : path;
        throw new EduskitError(message, response.statusCode(), errorCode, lastTraceId, errPath, source);
      }
      return envelope.has("data") ? envelope.get("data") : envelope;
    } catch (EduskitError e) {
      throw e;
    } catch (Exception e) {
      throw new EduskitError(e.getMessage(), null, "SDK_NETWORK_ERROR", null, path, source);
    }
  }
}

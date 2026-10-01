package com.eduskit.sdk;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class WhiteboardClient {
  public final Auth auth;
  public final Recordings recordings;
  public final Captures captures;
  public final Files files;
  private final HttpTransport http;

  public WhiteboardClient(ClientConfig config) {
    this.http = new HttpTransport(config, "whiteboard");
    this.auth = new Auth(config);
    this.recordings = new Recordings(http);
    this.captures = new Captures(http);
    this.files = new Files(http);
  }

  public String lastTraceId() { return http.lastTraceId; }

  public static class Auth {
    private final ClientConfig config;
    Auth(ClientConfig config) { this.config = config; }
    public JsonElement issueRoomToken(Map<String, Object> input) {
      return TokenSigner.room(config, input);
    }
  }

  public static class Recordings {
    private final HttpTransport http;
    Recordings(HttpTransport http) { this.http = http; }
    public JsonElement start(String roomId, Map<String, Object> input) {
      return http.request("POST", "/v1/rooms/recording/start", HttpTransport.withIdentifiers(input, Map.of("roomId", roomId)));
    }
    public JsonElement stop(String roomId, Map<String, Object> input) {
      return http.request("POST", "/v1/rooms/recording/stop", HttpTransport.withIdentifiers(input, Map.of("roomId", roomId)));
    }
    public JsonElement list(String roomId) {
      return http.request("GET", HttpTransport.withQuery("/v1/rooms/recordings", Map.of("roomId", roomId)), null);
    }
    public JsonElement get(String recordingId) {
      return http.request("GET", HttpTransport.withQuery("/v1/recordings", Map.of("recordingId", recordingId)), null);
    }
    public JsonElement registerMediaAsset(String recordingId, Map<String, Object> input) {
      return http.request("POST", "/v1/recordings/media-assets", HttpTransport.withIdentifiers(input, Map.of("recordingId", recordingId)));
    }
    public JsonElement deleteMediaAsset(String recordingId, String assetId) {
      return http.request("DELETE", HttpTransport.withQuery("/v1/recordings/media-assets", Map.of("recordingId", recordingId, "assetId", assetId)), null);
    }
    public JsonElement enqueueVideoExport(String recordingId, Map<String, Object> input) {
      return http.request("POST", "/v1/recordings/video-exports", HttpTransport.withIdentifiers(input, Map.of("recordingId", recordingId)));
    }
    public JsonElement getVideoExport(String recordingId, String jobId) {
      return http.request("GET", HttpTransport.withQuery("/v1/recordings/video-exports", Map.of("recordingId", recordingId, "jobId", jobId)), null);
    }
  }

  public static class Captures {
    private final HttpTransport http;
    Captures(HttpTransport http) { this.http = http; }
    public JsonElement create(String roomId, Map<String, Object> input) {
      Map<String, Object> body = input == null ? new HashMap<>() : new HashMap<>(input);
      body.put("roomId", roomId);
      return http.request("POST", "/v1/rooms/captures", HttpTransport.withIdentifiers(body, Map.of("roomId", roomId)));
    }
    public JsonElement list(String roomId) {
      return http.request("GET", HttpTransport.withQuery("/v1/rooms/captures", Map.of("roomId", roomId)), null);
    }
  }

  public static class Files {
    private final HttpTransport http;
    Files(HttpTransport http) { this.http = http; }
    public JsonElement convert(Map<String, Object> input) {
      return http.request("POST", "/v1/files/convert", input);
    }
    public JsonElement getConvertJob(String jobId) {
      return http.request("GET", HttpTransport.withQuery("/v1/files/convert", Map.of("jobId", jobId)), null);
    }
  }
}

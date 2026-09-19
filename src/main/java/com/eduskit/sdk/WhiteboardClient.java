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
      return http.request("POST", "/v1/rooms/" + roomId + "/recording/start", input);
    }
    public JsonElement stop(String roomId, Map<String, Object> input) {
      return http.request("POST", "/v1/rooms/" + roomId + "/recording/stop", input);
    }
    public JsonElement list(String roomId) {
      return http.request("GET", "/v1/rooms/" + roomId + "/recordings", null);
    }
    public JsonElement get(String recordingId) {
      return http.request("GET", "/v1/recordings/" + recordingId, null);
    }
    public JsonElement registerMediaAsset(String recordingId, Map<String, Object> input) {
      return http.request("POST", "/v1/recordings/" + recordingId + "/media-assets", input);
    }
    public JsonElement deleteMediaAsset(String recordingId, String assetId) {
      return http.request("DELETE", "/v1/recordings/" + recordingId + "/media-assets/" + assetId, null);
    }
    public JsonElement enqueueVideoExport(String recordingId, Map<String, Object> input) {
      return http.request("POST", "/v1/recordings/" + recordingId + "/video-exports", input);
    }
    public JsonElement getVideoExport(String recordingId, String jobId) {
      return http.request("GET", "/v1/recordings/" + recordingId + "/video-exports/" + jobId, null);
    }
  }

  public static class Captures {
    private final HttpTransport http;
    Captures(HttpTransport http) { this.http = http; }
    public JsonElement create(String roomId, Map<String, Object> input) {
      Map<String, Object> body = input == null ? new HashMap<>() : new HashMap<>(input);
      body.put("roomId", roomId);
      return http.request("POST", "/v1/rooms/" + roomId + "/captures", body);
    }
    public JsonElement list(String roomId) {
      return http.request("GET", "/v1/rooms/" + roomId + "/captures", null);
    }
  }

  public static class Files {
    private final HttpTransport http;
    Files(HttpTransport http) { this.http = http; }
    public JsonElement convert(Map<String, Object> input) {
      return http.request("POST", "/v1/files/convert", input);
    }
    public JsonElement getConvertJob(String jobId) {
      return http.request("GET", "/v1/files/convert/" + jobId, null);
    }
  }
}

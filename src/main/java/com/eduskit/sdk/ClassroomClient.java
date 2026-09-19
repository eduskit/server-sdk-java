package com.eduskit.sdk;

import com.google.gson.JsonElement;

import java.util.Map;

public class ClassroomClient {
  public final Users users;
  public final Auth auth;
  public final Classrooms classrooms;
  public final App app;
  private final HttpTransport http;

  public ClassroomClient(ClientConfig config) {
    this.http = new HttpTransport(config, "classroom");
    this.users = new Users(http);
    this.auth = new Auth(config);
    this.classrooms = new Classrooms(http);
    this.app = new App(http);
  }

  public String lastTraceId() { return http.lastTraceId; }

  public static class Users {
    private final HttpTransport http;
    Users(HttpTransport http) { this.http = http; }
    public JsonElement register(Map<String, Object> input) {
      return http.request("POST", "/v1/users", input);
    }
  }

  public static class Auth {
    private final ClientConfig config;
    Auth(ClientConfig config) { this.config = config; }
    public JsonElement issueToken(Map<String, Object> input) {
      return TokenSigner.edu(config, input);
    }
  }

  public static class Members {
    private final HttpTransport http;
    Members(HttpTransport http) { this.http = http; }
    public JsonElement add(String classroomId, Map<String, Object> input) {
      return http.request("POST", "/v1/classrooms/" + classroomId + "/members", input);
    }
    public JsonElement list(String classroomId) {
      return http.request("GET", "/v1/classrooms/" + classroomId + "/members", null);
    }
    public JsonElement replaceStudents(String classroomId, Map<String, Object> input) {
      return http.request("PUT", "/v1/classrooms/" + classroomId + "/members/students", input);
    }
  }

  public static class Permissions {
    private final HttpTransport http;
    Permissions(HttpTransport http) { this.http = http; }
    public JsonElement get(String classroomId, String eduUserId) {
      return http.request("GET", "/v1/classrooms/" + classroomId + "/members/" + eduUserId + "/permissions", null);
    }
    public JsonElement set(String classroomId, String eduUserId, Map<String, Object> input) {
      return http.request("POST", "/v1/classrooms/" + classroomId + "/members/" + eduUserId + "/permissions", input);
    }
    public JsonElement clear(String classroomId, String eduUserId, String permission, Map<String, Object> input) {
      return http.request("DELETE", "/v1/classrooms/" + classroomId + "/members/" + eduUserId + "/permissions/" + permission, input);
    }
  }

  public static class Coursewares {
    private final HttpTransport http;
    Coursewares(HttpTransport http) { this.http = http; }
    public JsonElement list(String classroomId) {
      return http.request("GET", "/v1/classrooms/" + classroomId + "/coursewares", null);
    }
    public JsonElement bind(String classroomId, Map<String, Object> input) {
      return http.request("POST", "/v1/classrooms/" + classroomId + "/coursewares", input);
    }
    public JsonElement unbind(String classroomId, Map<String, Object> input) {
      return http.request("DELETE", "/v1/classrooms/" + classroomId + "/coursewares", input);
    }
  }

  public static class Classrooms {
    public final Members members;
    public final Permissions permissions;
    public final Coursewares coursewares;
    private final HttpTransport http;
    Classrooms(HttpTransport http) {
      this.http = http;
      this.members = new Members(http);
      this.permissions = new Permissions(http);
      this.coursewares = new Coursewares(http);
    }
    public JsonElement create(Map<String, Object> input) {
      return http.request("POST", "/v1/classrooms", input);
    }
    public JsonElement start(String classroomId) {
      return http.request("POST", "/v1/classrooms/" + classroomId + "/start", null);
    }
    public JsonElement end(String classroomId) {
      return http.request("POST", "/v1/classrooms/" + classroomId + "/end", null);
    }
  }

  public static class App {
    private final HttpTransport http;
    App(HttpTransport http) { this.http = http; }
    public JsonElement getUiConfig() {
      return http.request("GET", "/v1/app/ui-config", null);
    }
    public JsonElement setUiConfig(Map<String, Object> input) {
      return http.request("PUT", "/v1/app/ui-config", input);
    }
  }
}

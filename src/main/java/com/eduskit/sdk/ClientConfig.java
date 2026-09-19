package com.eduskit.sdk;

import java.net.http.HttpClient;
import java.time.Duration;

public class ClientConfig {
  public final String baseUrl;
  public final String appId;
  public final String appKey;
  public final String appSecret;
  public final Duration timeout;
  public final String lang;
  public final HttpClient httpClient;

  public ClientConfig(String baseUrl, String appKey, String appSecret) {
    this(baseUrl, "", appKey, appSecret, Duration.ofSeconds(10), "zh-CN", null);
  }

  public ClientConfig(String baseUrl, String appId, String appKey, String appSecret) {
    this(baseUrl, appId, appKey, appSecret, Duration.ofSeconds(10), "zh-CN", null);
  }

  public ClientConfig(String baseUrl, String appKey, String appSecret, Duration timeout, String lang, HttpClient httpClient) {
    this(baseUrl, "", appKey, appSecret, timeout, lang, httpClient);
  }

  public ClientConfig(String baseUrl, String appId, String appKey, String appSecret, Duration timeout, String lang, HttpClient httpClient) {
    this.baseUrl = baseUrl;
    this.appId = appId;
    this.appKey = appKey;
    this.appSecret = appSecret;
    this.timeout = timeout == null ? Duration.ofSeconds(10) : timeout;
    this.lang = lang == null ? "zh-CN" : lang;
    this.httpClient = httpClient;
  }
}

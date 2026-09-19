package com.eduskit.sdk;

public class EduskitError extends RuntimeException {
  private final Integer status;
  private final String errorCode;
  private final String traceId;
  private final String path;
  private final String source;

  public EduskitError(String message, Integer status, String errorCode, String traceId, String path, String source) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
    this.traceId = traceId;
    this.path = path;
    this.source = source;
  }

  public Integer getStatus() { return status; }
  public String getErrorCode() { return errorCode; }
  public String getTraceId() { return traceId; }
  public String getPath() { return path; }
  public String getSource() { return source; }
}

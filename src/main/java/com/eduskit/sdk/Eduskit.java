package com.eduskit.sdk;

public class Eduskit {
  private final ClassroomClient client;
  private final WhiteboardClient whiteboardClient;

  public Eduskit(ClientConfig client, ClientConfig whiteboardClient) {
    this.client = client == null ? null : new ClassroomClient(client);
    this.whiteboardClient = whiteboardClient == null ? null : new WhiteboardClient(whiteboardClient);
  }

  public ClassroomClient client() {
    if (client == null) {
      throw new EduskitError("classroom client is not configured", null, "SDK_CLIENT_NOT_CONFIGURED", null, null, "classroom");
    }
    return client;
  }

  public WhiteboardClient whiteboardClient() {
    if (whiteboardClient == null) {
      throw new EduskitError("whiteboard client is not configured", null, "SDK_CLIENT_NOT_CONFIGURED", null, null, "whiteboard");
    }
    return whiteboardClient;
  }
}

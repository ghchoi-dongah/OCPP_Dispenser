package com.dongah.dispenser.websocket.socket;

import org.json.JSONObject;

public interface SocketMessageListener {
    void onMessageReceiveEvent(JSONObject jsonObject);

    void onMessageReceiveDebugEvent(JSONObject jsonObject, String actionName);

    void onMessageReceiveDebugEvent(int type, String text, String actionName);
}

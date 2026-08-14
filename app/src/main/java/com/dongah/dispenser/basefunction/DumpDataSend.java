package com.dongah.dispenser.basefunction;

import android.os.Handler;
import android.os.Looper;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.websocket.ocpp.common.JSONCommunicator;
import com.dongah.dispenser.websocket.ocpp.common.model.Message;
import com.dongah.dispenser.websocket.socket.SendHashMapObject;
import com.dongah.dispenser.websocket.socket.SocketState;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class DumpDataSend extends JSONCommunicator {

    private static final Logger logger = LoggerFactory.getLogger(DumpDataSend.class);


    Message message = null;
    String uuid = null;
    int connectorId = 0;
    Handler handler = new Handler(Looper.getMainLooper());


    String startTransactionMessage = null, stopTransactionMessage = null;


    public void onDumpSend() {

        // 미전송 데이터 유뮤 판단.
        startTransactionMessage = null;
        stopTransactionMessage = null;

        String line;
        ArrayList<String> meterValuesList = new ArrayList<>();
        BufferedReader bufferedReader;

        String path = GlobalVariables.getRootPath() + File.separator + "dump" + File.separator + "dump";
        File file = new File(path);

        if (!file.exists()) return;

        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            while ((line = bufferedReader.readLine()) != null) {
                message = parse(line);
                String actionName = message.getAction();
                if (Objects.equals(actionName, "StartTransaction")) {
                    startTransactionMessage = line;
                    JSONObject jsonObject = new JSONObject(message.getPayload().toString());
                    uuid = message.getId();
                    connectorId = jsonObject.getInt("connectorId");

                    SendHashMapObject sendHashMapObject = new SendHashMapObject();
                    sendHashMapObject.setConnectorId(connectorId);
                    sendHashMapObject.setActionName(actionName);

                    ((MainActivity) MainActivity.mContext).getSocketReceiveMessage()
                            .setNewHashMapUuid(uuid, sendHashMapObject);

                    ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().onSend(line);

                } else if (Objects.equals(actionName, "MeterValues")) {
                    // MeterValues는 저장해두고 나중에 반복 실행
                    meterValuesList.add(line);
                } else if (Objects.equals(actionName, "StopTransaction")) {
                    stopTransactionMessage = line;
                } else {
                    meterValuesList.add(line);
                }
            }

            bufferedReader.close();
            file.delete();
            if (!meterValuesList.isEmpty()) {
                Iterator<String> iterator = meterValuesList.iterator();
                boolean sendResult  = !Objects.equals(startTransactionMessage, null) && !Objects.equals(stopTransactionMessage, null);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (iterator.hasNext()) {
                            if (stopTransactionMessage != null) {
                                JsonArray arr = JsonParser.parseString(stopTransactionMessage).getAsJsonArray();
                                arr.get(3).getAsJsonObject().addProperty("transactionId", GlobalVariables.dumpTransactionId);
                                ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().onSend(arr.toString());
                                stopTransactionMessage = null;
                            } else {
                                String meterLine = iterator.next();
                                message = parse(meterLine);
                                JsonArray arr = JsonParser.parseString(meterLine).getAsJsonArray();
                                //MeterValues
                                if (Objects.equals(message.getAction(),  "MeterValues")) {
                                    arr.get(3).getAsJsonObject().addProperty("transactionId", GlobalVariables.dumpTransactionId);
                                    ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().onSend(arr.toString());
                                } else if (Objects.equals(message.getAction(),  "StatusNotification")) {
                                    if (!sendResult) {
                                        ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().onSend(arr.toString());
                                    }
                                }
                            }
                            handler.postDelayed(this, 500);
                        } else {
                            handler.removeCallbacks(this);
                            handler.removeMessages(0);
                        }
                    }
                }, 500);
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopTask() {
        handler.removeCallbacksAndMessages(null);
        handler.removeMessages(0);
    }
}
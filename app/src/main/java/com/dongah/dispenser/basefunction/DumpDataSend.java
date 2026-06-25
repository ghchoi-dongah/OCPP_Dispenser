package com.dongah.dispenser.basefunction;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.websocket.ocpp.common.JSONCommunicator;
import com.dongah.dispenser.websocket.ocpp.common.model.Message;
import com.dongah.dispenser.websocket.socket.SendHashMapObject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;

public class DumpDataSend extends JSONCommunicator {


    private static final Logger logger = LoggerFactory.getLogger(DumpDataSend.class);


    Message message = null;
    String uuid = null;
    int connectorId = 0;

    public void onDumpSend() {
        // 이전 연결에서 미처리된 플래그 초기화
        GlobalVariables.dumpPendingStop = false;
        GlobalVariables.dumpChargingNotifSent = false;
        GlobalVariables.pendingBootAfterDump = false;
        GlobalVariables.dumpPendingFinishing = false;
        GlobalVariables.dumpFinishingConnectorId = 0;

        try {
            String line, actionName;
            JSONObject jsonObject;
            FileReader fileReader;
            BufferedReader bufferedReader;
            String path = GlobalVariables.getRootPath() + File.separator + "dump" + File.separator + "dump";
            File file = new File(path);
            if (file.exists()) {
                // 1패스: TC_039 감지 — StopTransaction.transactionId == 0이면 오프라인 시작이므로 Charging 스킵
                // transactionId != 0 → StartTx.conf를 받은 적 있는 케이스 → Charging 전송 허용
                boolean skipChargingFromDump = false;
                try {
                    BufferedReader scanReader = new BufferedReader(new FileReader(file));
                    String scanLine;
                    while ((scanLine = scanReader.readLine()) != null) {
                        Message scanMsg = parse(scanLine);
                        if (Objects.equals(scanMsg.getAction(), "StopTransaction")) {
                            try {
                                JSONObject stopScanJson = new JSONObject(scanMsg.getPayload().toString());
                                if (stopScanJson.optInt("transactionId", 0) == 0) {
                                    skipChargingFromDump = true;
                                }
                            } catch (Exception ignored2) {}
                            break;
                        }
                    }
                    scanReader.close();
                } catch (Exception ignored) {}

                // 2패스: 전송 처리
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);

                while ((line = bufferedReader.readLine()) != null) {
                    message = parse(line);
                    actionName = message.getAction();

                    if (Objects.equals(actionName, "StartTransaction")) {
                        jsonObject = new JSONObject(message.getPayload().toString());
                        uuid = message.getId();
                        connectorId = jsonObject.getInt("connectorId");

                        SendHashMapObject sendHashMapObject = new SendHashMapObject();
                        sendHashMapObject.setConnectorId(connectorId);
                        sendHashMapObject.setActionName(actionName);
                        ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().setNewHashMapUuid(uuid, sendHashMapObject);
                        ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().onSend(line);

                    } else if (Objects.equals(actionName, "StopTransaction")) {
                        jsonObject = new JSONObject(message.getPayload().toString());
                        int storedTransactionId = jsonObject.optInt("transactionId", 0);
                        uuid = message.getId();
                        if (storedTransactionId == 0) {
                            // TC_039: 오프라인 거래 — transactionId는 StartTransaction.conf 후 확정
                            GlobalVariables.dumpPendingStop = true;
                            break;
                        }
                        // PowerLoss 시나리오: StopTx.conf 이후 Finishing 전송을 위한 sentinel 확인
                        int powerLossConnector = 0;
                        for (int cid = 1; cid <= GlobalVariables.maxChannel; cid++) {
                            File plFile = new File(GlobalVariables.getRootPath() + File.separator + "PowerLossFinishing_" + cid);
                            if (plFile.exists()) {
                                powerLossConnector = cid;
                                plFile.delete();
                                break;
                            }
                        }
                        int regConnectorId = powerLossConnector > 0 ? powerLossConnector : (connectorId > 0 ? connectorId : 1);
                        SendHashMapObject stopHashMap = new SendHashMapObject();
                        stopHashMap.setConnectorId(regConnectorId);
                        stopHashMap.setActionName(actionName);
                        ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().setNewHashMapUuid(uuid, stopHashMap);
                        if (powerLossConnector > 0) {
                            GlobalVariables.dumpPendingFinishing = true;
                            GlobalVariables.dumpFinishingConnectorId = powerLossConnector;
                        } else {
                            // TC_038: StopTx.conf 수신 후 Finishing 전송을 pendingStopTxConf 경로에 위임
                            int ch = regConnectorId - 1;
                            if (ch >= 0) {
                                ((MainActivity) MainActivity.mContext).getClassUiProcess(ch).getChargingCurrentData().setPendingStopTxConf(true);
                            }
                        }
                        ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().onSend(line);

                    } else if (Objects.equals(actionName, "StatusNotification") && !skipChargingFromDump) {
                        // TC_037_x / StartTx+유효transactionId 케이스: Charging만 전송
                        // Finishing/Available은 StopTx.conf 이후 정상 흐름이 처리
                        // skipChargingFromDump=true(TC_039: StopTx.transactionId==0)이면 전송하지 않음
                        try {
                            jsonObject = new JSONObject(message.getPayload().toString());
                            if (Objects.equals(jsonObject.optString("status", ""), "Charging")) {
                                GlobalVariables.dumpChargingNotifSent = true;
                                ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().onSend(line);
                            }
                        } catch (Exception ignored) {}
                    }
                }
                boolean result = file.delete();
                fileReader.close();
                bufferedReader.close();
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

}

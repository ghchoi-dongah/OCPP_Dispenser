package com.dongah.dispenser.handler;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.DumpDataSend;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.LogDataSave;
import com.dongah.dispenser.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.HeartbeatRequest;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class HeartbeatThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatThread.class);

    boolean stopped = false;
    int delayTime;
    int count = 0;
    boolean sendCheck = false;

    HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
    SocketReceiveMessage socketReceiveMessage;
    LogDataSave logDataSave = new LogDataSave("log");

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public HeartbeatThread(int delayTime) {
        this.delayTime = delayTime;
        socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
        try {
            socketReceiveMessage.onSend(heartbeatRequest.getActionName(), heartbeatRequest);
        } catch (OccurenceConstraintException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        super.run();
        while (!isStopped()) {
            try {
                try {
                    Thread.sleep(1000);
                    count++;
                } catch (Exception e) {
                    logger.error("thread sleep error : {} ", e.getMessage());
                }
                if (count >= (delayTime)) {
                    count = 0;
                    sendCheck = false;
                    socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                    ClassUiProcess[] classUiProcesses = ((MainActivity) MainActivity.mContext).getClassUiProcess();
                    for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                        ChargingCurrentData chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(i);
                        if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Charging)) {
                            sendCheck = true;
                        }
                    }
                    if (sendCheck)
                        socketReceiveMessage.onSend(100, heartbeatRequest.getActionName(), heartbeatRequest);
                    //30일 이상 로그 데이터 삭제
                    logDataSave.removeLogData();
                    // 미전송 데이터: dumpPendingStop 로직이 포함된 DumpDataSend 사용
                    SocketState socketState = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().getSocket().getState();
                    if (Objects.equals(socketState, SocketState.OPEN)) {
                        DumpDataSend dumpDataSend = new DumpDataSend();
                        dumpDataSend.onDumpSend();
                    }
                }
            } catch (Exception e) {
                logger.error("HeartbeatThread error : {} ", e.getMessage());
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }


}

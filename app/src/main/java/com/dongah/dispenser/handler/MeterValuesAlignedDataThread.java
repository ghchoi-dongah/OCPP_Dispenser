package com.dongah.dispenser.handler;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.websocket.ocpp.core.MeterValue;
import com.dongah.dispenser.websocket.ocpp.core.MeterValuesRequest;
import com.dongah.dispenser.websocket.ocpp.core.SampledValue;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Objects;

public class MeterValuesAlignedDataThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MeterValuesAlignedDataThread.class);

    boolean stopped = false;
    int delayTime;
    int connectorId;

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public int getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public MeterValuesAlignedDataThread(int connectorId, int delayTime) {
        super();
        this.connectorId = connectorId;
        this.delayTime = delayTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        super.run();
        int lastSentSecond = -1;
        while (!isStopped()) {
            try {
                sleep(1000);

                Calendar now = Calendar.getInstance();
                int second = now.get(Calendar.SECOND);

                // clock-aligned: 현재 초가 interval의 배수일 때만 전송 (중복 방지: lastSentSecond)
                if (second % delayTime == 0 && second != lastSentSecond) {
                    SocketReceiveMessage socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                    ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
                    ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime();
                    if (Objects.equals(socketReceiveMessage.getSocket().getState(), SocketState.OPEN)) {
                        ChargingCurrentData chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(getConnectorId() - 1);
                        SampledValue[] sampledValues = chargingCurrentData.getSampleAlignedValueData().getSampledValues(chargingCurrentData);
                        for (SampledValue sv : sampledValues) { sv.setContext("Sample.Clock"); }
                        MeterValue[] meterValues = {new MeterValue(timestamp, sampledValues)};
                        MeterValuesRequest meterValuesRequest = new MeterValuesRequest(chargingCurrentData.getConnectorId());
                        meterValuesRequest.setMeterValue(meterValues);
                        meterValuesRequest.setTransactionId(chargingCurrentData.getTransactionId());
                        socketReceiveMessage.onSend(getConnectorId(), meterValuesRequest.getActionName(), meterValuesRequest);
                        lastSentSecond = second;
                    }
                }
            } catch (Exception e) {
                logger.error(" thread error : {}", e.getMessage());
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        setStopped(true);
    }
}

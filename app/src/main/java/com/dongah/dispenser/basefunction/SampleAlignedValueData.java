package com.dongah.dispenser.basefunction;

import com.dongah.dispenser.controlboard.ControlBoardUtil;
import com.dongah.dispenser.websocket.ocpp.core.SampledValue;
import com.dongah.dispenser.websocket.ocpp.core.ValueFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class SampleAlignedValueData {
    private static final Logger logger = LoggerFactory.getLogger(SampleAlignedValueData.class);

    ChargingCurrentData chargingCurrentData;
    ControlBoardUtil controlBoardUtil;
    SampledValue[] sampledValues = new SampledValue[1];
    DecimalFormat powerFormatter = new DecimalFormat("######0.00");


    public SampleAlignedValueData() {
        //2. 유효입력 전력량
        sampledValues[0] = new SampledValue();
        sampledValues[0].setFormat(ValueFormat.Raw);
        sampledValues[0].setMeasurand("Energy.Active.Import.Register");
        sampledValues[0].setUnit("kWh");
        sampledValues[0].setValue("0");

        initSampledValues();
    }


    public SampledValue[] getSampledValues(ChargingCurrentData chargingCurrentData) {
        this.chargingCurrentData = chargingCurrentData;
        controlBoardUtil = new ControlBoardUtil();
        updateSampleValues();
        return sampledValues;
    }

    public void initSampledValues() {
        try {
            sampledValues[0].setValue("0");             //1. 유효입력 전력량
        } catch (Exception e) {
            logger.error("initSampledValues setting error....: {} ", e.getMessage());
        }
    }

    public void updateSampleValues() {
        try {
            // Meter values 콜할때 charge point current value (충전중일때만)
            //충전기 현재의 값을 갖고 온다.
            sampledValues[0].setValue(powerFormatter.format(chargingCurrentData.getPowerMeter() * 0.01));              //2. 유효입력 전력량 (kWh) cpCurrentData.getPowerKwh() ==> 충전사용량으로 수정 (2023.01.05)
        } catch (Exception e) {
            logger.error("Update SampledValues setting error : {}", e.getMessage());
        }
    }
}

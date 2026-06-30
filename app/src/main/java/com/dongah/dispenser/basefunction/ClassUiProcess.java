package com.dongah.dispenser.basefunction;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.controlboard.ControlBoard;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.controlboard.TxData;
import com.dongah.dispenser.handler.MeterValueThread;
import com.dongah.dispenser.handler.MeterValuesAlignedDataThread;
import com.dongah.dispenser.handler.ProcessHandler;
import com.dongah.dispenser.pages.FaultFragment;
import com.dongah.dispenser.rfcard.RfCardReaderListener;
import com.dongah.dispenser.rfcard.RfCardReaderReceive;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.ocpp.core.ResetType;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ClassUiProcess implements RfCardReaderListener {


    private static final Logger logger = LoggerFactory.getLogger(ClassUiProcess.class);

    int ch;
    UiSeq uiSeq;
    UiSeq oSeq;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    RfCardReaderReceive rfCardReaderReceive;
    FragmentChange fragmentChange;
    NotifyFaultCheck notifyFaultCheck;
    ControlBoard controlBoard;
    Timer eventTimer;

    SocketReceiveMessage socketReceiveMessage;
    ProcessHandler processHandler;

    int targetSoc = 0;
    double powerUnitPrice = 0f;
    int powerMeterCheck = 0;
    int plugCheckCounter = 0;
    boolean prevCsPilot = false; // csPilot 상승 에지 감지용 (INIT→MEMBER_CARD 중복 전환 방지)
    boolean rebootInitiated = false; // REBOOTING 상태에서 FaultFragment 중복 교체 방지


    ZonedDateTimeConvert zonedDateTimeConvert;
    /**
     * MeterValue Thread
     */
    MeterValueThread meterValueThread;
    MeterValuesAlignedDataThread meterValuesAlignedDataThread;

    /**
     * custom Status Notification
     */


    public int getCh() {
        return ch;
    }

    public UiSeq getUiSeq() {
        return uiSeq;
    }

    public void setUiSeq(UiSeq uiSeq) {
        this.uiSeq = uiSeq;
    }

    public UiSeq getoSeq() {
        return oSeq;
    }

    public void setoSeq(UiSeq oSeq) {
        this.oSeq = oSeq;
    }


    public double getPowerUnitPrice() {
        return powerUnitPrice;
    }

    public void setPowerUnitPrice(double powerUnitPrice) {
        this.powerUnitPrice = powerUnitPrice;
    }

    public int getPowerMeterCheck() {
        return powerMeterCheck;
    }

    public void setPowerMeterCheck(int powerMeterCheck) {
        this.powerMeterCheck = powerMeterCheck;
    }

    public ChargingCurrentData getChargingCurrentData() {
        return chargingCurrentData;
    }

    public ClassUiProcess(int ch) {
        this.ch = ch;
        try {
            setUiSeq(UiSeq.INIT);
            zonedDateTimeConvert = new ZonedDateTimeConvert();
            //rf card
            rfCardReaderReceive = ((MainActivity) MainActivity.mContext).getRfCardReaderReceive();
            rfCardReaderReceive.setRfCardReaderListener(this);
            // configuration
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            //fragment change
            fragmentChange = ((MainActivity) MainActivity.mContext).getFragmentChange();
            //control board
            controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
            // alarm check
            notifyFaultCheck = new NotifyFaultCheck(ch);
            //process handler
            processHandler = ((MainActivity) MainActivity.mContext).getProcessHandler();
            //loop
            eventTimer = new Timer();
            eventTimer.schedule(new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    onEventAction();
                }
            }, 5000, 2000);
        } catch (Exception e) {
            logger.error("ClassUiProcess - construct error : {}", e.getMessage());
        }
    }

    int getId = 0;
    int channel;
    boolean check;

    /**
     * charging sequence loop
     * server data send : 서버와 연결이 안된 경우 ProcessHandler dump data save
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onEventAction() {
        try {
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(ch);
            RxData rxData = controlBoard.getRxData(getCh());
            TxData txData = controlBoard.getTxData(getCh());
            chargingCurrentData.setIntegratedPower(rxData.getPowerMeter());
            boolean faultCheckAllowed = GlobalVariables.isConnectRetry() || Objects.equals(chargerConfiguration.getAuthMode(), "2");
            if (getUiSeq().getValue() < 17 && faultCheckAllowed) onFaultCheck(rxData);

            //sequence check
            switch (getUiSeq()) {
                case NONE:
                case INIT:
                    handleInit(rxData);
                    break;
                case REBOOTING:
                    handleRebooting();
                    break;
                case MEMBER_CARD:
                case MEMBER_CARD_WAIT:
                case CREDIT_CARD:
                case CREDIT_CARD_WAIT:
                    break;
                case PLUG_CHECK:
                    handlePlugCheck(rxData);
                    break;
                case CONNECT_CHECK:
                    handleConnectCheck(rxData);
                    break;
                case CHARGING:
                    handleCharging(rxData, txData);
                    break;
                case FINISH_WAIT:
                    handleFinishWait(rxData, txData);
                    break;
                case FINISH:
                    handleFinish(rxData);
                    break;
                case FAULT:
                    handleFault(rxData, txData);
                    break;
            }
            prevCsPilot = rxData.getCpVoltage() < 110;
        } catch (Exception e) {
            logger.error(" onEventAction() exception error : {}", e.getMessage());
        }
    }

    /**
     * TLS3800 call back
     *
     * @param ch          ch
     * @param type        response type
     * @param returnValue hashMap {idTag : value}
     */
//    @Override
//    public void onTLS3800ResponseCallBack(int ch, TLS3800ResponseType type, HashMap<String, String> returnValue) {
//        try {
//            String cancelType;
//            for (String key : returnValue.keySet()) {
//                if (Objects.equals(key, "idTag")) {
//                    chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(ch);
//                    UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(ch).getUiSeq();
//
//                    if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
//                        chargingCurrentData.setIdTagStop(returnValue.get("idTag"));
//                    } else {
//                        chargingCurrentData.setIdTag(returnValue.get("idTag"));
//                    }
////                    fragmentChange.onFragmentChange(ch, UiSeq.MEMBER_CARD_WAIT, null, null);
//                } else if (Objects.equals(key, "MID")) {
//                    chargerConfiguration.setMID(returnValue.get("MID"));
//                } else if (Objects.equals(key, "tradeCode")) {
//                    chargingCurrentData.setPrePaymentResult(!Objects.equals(returnValue.get("tradeCode"), "X"));
//                } else if (Objects.equals(key, "cancelType")) {
//                    //(4:무카드 취소)(5:부분 취소)
//                    cancelType = returnValue.get("cancelType");
//                } else if (Objects.equals(key, "responseCode")) {
//                    chargingCurrentData.setResponseCode(returnValue.get("responseCode"));
//                } else if (Objects.equals(key, "responseMessage")) {
//                    chargingCurrentData.setResponseMessage(returnValue.get("responseMessage"));
//                }
//            }
//
//            if (Objects.equals(TLS3800ResponseType.PAYG, type)) {
//                if (chargingCurrentData.isPrePaymentResult()) {
//                    //server send
//                    socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
//                    processHandler.sendMessage(
//                            socketReceiveMessage.onMakeHandlerMessage(
//                                    GlobalVariables.MESSAGE_HANDLER_PAY_INFO,
//                                    chargingCurrentData.getConnectorId(),
//                                    0,
//                                    chargingCurrentData.getCreditCardNumber(),
//                                    null,
//                                    "HUMAX",        //alarmCode : CPO 구분 ==> HUMAX / ""
//                                    !Objects.equals(returnValue.get("tradeCode"), "X")
//                            ));
//                }
//            } else if (Objects.equals(TLS3800ResponseType.CANCEL, type)) {
//                if (chargingCurrentData.isPrePaymentResult()) {
//                    socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
//                    processHandler.sendMessage(
//                            socketReceiveMessage.onMakeHandlerMessage(
//                                    GlobalVariables.MESSAGE_HANDLER_PARTIAL_CANCEL,
//                                    chargingCurrentData.getConnectorId(),
//                                    0,
//                                    chargingCurrentData.getCreditCardNumber(),
//                                    null,
//                                    "HUMAX",
//                                    !Objects.equals(returnValue.get("tradeCode"), "X")
//                            ));
//                }
//            } else if (Objects.equals(TLS3800ResponseType.RF_READ, type))  {
//                if (uiSeq != UiSeq.MEMBER_CARD_WAIT) {
//                    ((MainActivity) MainActivity.mContext).getClassUiProcess(ch).setUiSeq(UiSeq.MEMBER_CARD_WAIT);
//                    fragmentChange.onFragmentChange(ch, UiSeq.MEMBER_CARD_WAIT, "MEMBER_CARD_WAIT", null);
//                }
//            } else {
//                onHome();
//            }
//
//
//        } catch (Exception e) {
//            logger.error("onTLS3800ResponseCallBack error : {}", e.getMessage());
//        }
//    }


    /**
     * ChargerCurrentData 나중에 init 에서 삭제
     */
    public void onHome() {
        rebootInitiated = false;
        // INIT 재진입 시 prevCsPilot을 현재 CP 상태로 동기화 (getCpVoltage 기준으로 통일)
        // ChargingFinish/Reset 후 케이블이 연결된 채 INIT 진입 시 오상승 에지 방지
        prevCsPilot = controlBoard.getRxData(getCh()).getCpVoltage() < 110;
        setUiSeq(UiSeq.INIT);
        fragmentChange.onFragmentChange(ch, UiSeq.INIT, "INIT", null);
        rfCardReaderReceive.rfCardReadRelease();
    }


    private void onFinish() {
        //충전 완료
        if (chargingCurrentData.isReBoot()) {
            setUiSeq(UiSeq.INIT);
        }
        // 케이블 분리 감지 시 Available 전송 후 INIT 전환 (cpVoltage >= 11.0V = 미연결)

//        short tmp = controlBoard.getRxData(getCh()).getCpVoltage();
//        if (controlBoard.getRxData(getCh()).getCpVoltage() >= 110) {
//            chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
//            socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
//            if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
//                processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
//                        GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
//                        chargingCurrentData.getConnectorId(), 0, null, null, null, false));
//            }
//            setUiSeq(UiSeq.INIT);
//            fragmentChange.onFragmentChange(getCh(), UiSeq.INIT, "INIT", null);
//            rfCardReaderReceive.rfCardReadRelease();
//        }
    }


    /**
     * 현재 Fragment 찾기
     *
     * @return fragment;
     */
    private Fragment getCurrentFragment() {
        return ((MainActivity) MainActivity.mContext).getSupportFragmentManager().findFragmentById(getCh() == 0 ? R.id.ch0 : R.id.ch1);
    }

    /**
     * Remote Transaction stop
     */
    public void onRemoteTransactionStop(int channel, Reason reason) {
        try {
            controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(ch);
            controlBoard.getTxData(channel).setStop(true);
            controlBoard.getTxData(channel).setStart(false);
            chargingCurrentData.setUserStop(false);
            chargingCurrentData.setStopReason(reason);
        } catch (Exception e) {
            logger.error("remote stop error : {} ", e.getMessage());
        }
    }

    public void onResetStop(int channel, ResetType resetType) {
        try {
            controlBoard.getTxData(getCh()).setStop(true);
            controlBoard.getTxData(getCh()).setStart(false);
            chargingCurrentData.setUserStop(false);
            chargingCurrentData.setStopReason(resetType == ResetType.Hard ? Reason.HardReset : Reason.SoftReset);
            setUiSeq(UiSeq.FINISH_WAIT);
//            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getUiSeq();
//            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
//            }
        } catch (Exception e) {
            logger.error("reset stop error : {} ", e.getMessage());
        }
    }

    public void initDisconnectState(boolean faulted) {
        notifyFaultCheck.initDisconnectState(faulted);
    }

    private boolean onRebootCheck() {
        boolean result = false;
        try {
            UiSeq uiSeq1 = ((MainActivity) MainActivity.mContext).getClassUiProcess(0).getUiSeq();
            UiSeq uiSeq2 = ((MainActivity) MainActivity.mContext).getClassUiProcess(1).getUiSeq();
            result = (Objects.equals(UiSeq.REBOOTING, uiSeq1) || Objects.equals(UiSeq.INIT, uiSeq1))
                    && (Objects.equals(UiSeq.REBOOTING, uiSeq2) || Objects.equals(UiSeq.INIT, uiSeq2));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    /**
     * Meter value
     *
     * @param connectorId connector id
     * @param delay       delay time
     */
    public void onMeterValueStart(int connectorId, int delay) {
        onMeterValueStop();
        meterValueThread = new MeterValueThread(connectorId, delay);
        meterValueThread.setStopped(false);
        meterValueThread.start();
    }

    public void onMeterValueStop() {
        if (meterValueThread != null) {
            meterValueThread.setStopped(true);
            meterValueThread.interrupt();
            meterValueThread = null;
        }
    }

    public void onMeterValuesAlignedDataStart(int connectorId, int delay) {
        onMeterValuesAlignedDataStop();
        meterValuesAlignedDataThread = new MeterValuesAlignedDataThread(connectorId, delay);
        meterValuesAlignedDataThread.setStopped(false);
        meterValuesAlignedDataThread.start();
    }

    //meterValuesAlignedData
    public void onMeterValuesAlignedDataStop() {
        if (meterValuesAlignedDataThread != null) {
            meterValuesAlignedDataThread.setStopped(true);
            meterValuesAlignedDataThread.interrupt();
            meterValuesAlignedDataThread = null;
        }
    }

    private void onFaultCheck(RxData rxData) {
        try {
            //충전중 일 때 fault 가 발생한 경우
            if (controlBoard.isDisconnected() || rxData.csFault) {
                if (Objects.equals(getUiSeq(), UiSeq.CHARGING)) {
                    controlBoard.getTxData(getCh()).setStop(true);
                    controlBoard.getTxData(getCh()).setStart(false);
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Faulted);
                    chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.OtherError);
                    //비회원 충전 요금 단가 조정을 한다.
                    if (Objects.equals(chargingCurrentData.getPaymentType().value(), 2) &&
                            chargingCurrentData.getPrePayment() <= chargingCurrentData.getPowerMeterUsePay()) {
                        chargingCurrentData.setPowerMeterUsePay(chargingCurrentData.getPrePayment());
                    }
                }
                // fault 발생하기 전에 충전 스퀀스 저장
                if (getUiSeq() != UiSeq.FAULT) setoSeq(getUiSeq());
                setUiSeq(UiSeq.FAULT);
            }
            notifyFaultCheck.onErrorMessageMake(rxData);
        } catch (Exception e) {
            logger.error("onFaultCheck error.... : {}", e.toString());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onReservationExpiryDate(ChargingCurrentData chargingCurrentData) {
        try {
            if (chargingCurrentData.getReservedStatus() == ChargePointStatus.Reserved) {
                String currentTime = zonedDateTimeConvert.doGetUtcDatetimeAsString();

                if (currentTime.compareTo(chargingCurrentData.getResExpiryDate()) > 0) {
                    // available
                    int savedResConnectorId = chargingCurrentData.getResConnectorId();
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                    chargingCurrentData.setResConnectorId(0);
                    chargingCurrentData.setResIdTag("");
                    chargingCurrentData.setResExpiryDate("");
                    chargingCurrentData.setResReservationId("");
                    chargingCurrentData.setResParentIdTag("");
                    chargingCurrentData.setReservedStatus(ChargePointStatus.Available);

                    socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                    processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                            savedResConnectorId,
                            0,
                            null,
                            null,
                            null,
                            false));
                }
            }
        } catch (Exception e) {
            logger.error(" onReservationExpiryDate error : {} ", e.getMessage());
        }
    }

    /**
     * 충전 사용량 계산
     *
     * @param rxData power meter raw data pick
     */
    private void onUsePowerMeter(int ch, RxData rxData) {
        try {
            long gapPower = 0;
            double gapPay = 0;
            if (rxData.getPowerMeter() > 0) {
                //current power meter --> chargingCurrentData .powerKwh
                //전력량 변화 여부 체크
                chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(ch);
                gapPower = rxData.getPowerMeter() - chargingCurrentData.getPowerMeterCalculate();
                gapPower = (gapPower <= 0) ? 0 : (gapPower > 10) ? 1 : gapPower;
                //전력량 변화 여부 체크 892 = 8.92kW
                powerMeterCheck = gapPower == 0 ? powerMeterCheck + 1 : 0;

                chargingCurrentData.setPowerMeterUse(chargingCurrentData.getPowerMeterUse() + gapPower);
                gapPay = gapPower * 0.01 * powerUnitPrice;

                chargingCurrentData.setPowerMeterUsePay(chargingCurrentData.getPowerMeterUsePay() + gapPay);
                chargingCurrentData.setPowerMeterCalculate(rxData.getPowerMeter());

                chargingCurrentData.setRemaintime(rxData.getRemainTime());
            }
            chargingCurrentData.setOutPutCurrent(rxData.getOutCurrent());  //출력전류
            chargingCurrentData.setOutPutVoltage(rxData.getOutVoltage());  //출력전압
            chargingCurrentData.setPowerMeter(rxData.getPowerMeter());  //전력량
            chargingCurrentData.setTargetCurrent(rxData.getCsmEVTargetCurrent());   // 요청전류
            chargingCurrentData.setFrequency(60);    //주파수
            chargingCurrentData.setChargingRemainTime(rxData.getRemainTime());  //충전 남은 시간
            chargingCurrentData.setSoc(rxData.getSoc());
        } catch (Exception e) {
            logger.error("power meter calculate error : {}", e.getMessage());
        }
    }

    /**
     * TargetSoc check
     */
    private boolean onSocStop(int targetSoc, int soc) {
        boolean result = false;
        try {
            if (Objects.equals(targetSoc, 0)) {
                result = (soc >= 99);
            } else {
                result = soc > targetSoc;
            }
        } catch (Exception e) {
            logger.error("Soc Check  erro : {}", e.getMessage());
        }
        return result;
    }

    public void onStop() {
        try {
            UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(getCh()).getUiSeq();
            controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
            //충전기 정지
            controlBoard.getTxData(getCh()).setStart(false);
            controlBoard.getTxData(getCh()).setStop(true);
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
//            if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
//            }
        } catch (Exception e) {
            logger.error(" onStop error : {}", e.getMessage());
        }
    }

    /**
     * Rf CARD reader
     *
     * @param cardNum card number
     * @param value   boolean
     */
    public void onRfCardDataReceive(int ch, String cardNum, boolean value) {
        try {
            UiSeq currentSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(ch).getUiSeq();
            // MEMBER_CARD(시작 인증), CHARGING(종료 인증), INIT(card-first 오프라인 TC_039) 외에는 무시
            if (!Objects.equals(UiSeq.CHARGING, currentSeq) &&
                    !Objects.equals(UiSeq.MEMBER_CARD, currentSeq) &&
                    !Objects.equals(UiSeq.INIT, currentSeq)) {
                return;
            }
            if (cardNum.isEmpty() || Objects.equals(cardNum, "0000000000000000")) {
                // 카드 미인식 — MEMBER_CARD 상태에서만 재요청 (CHARGING 중에는 무시)
                if (Objects.equals(UiSeq.MEMBER_CARD, currentSeq)) {
                    rfCardReaderReceive.rfCardReadRequest(ch);
                }
            } else {
                onRfCardDataReceiveEvent(ch, cardNum, true);
            }
        } catch (Exception e) {
            logger.error("onRfCardDataReceive error : {} ", e.getMessage());
        }
    }

    private void onRfCardDataReceiveEvent(int ch, String cardNum, boolean b) {
        if (b && !cardNum.isEmpty()) {
            try {
                chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(ch);
                UiSeq seq = ((MainActivity) MainActivity.mContext).getClassUiProcess(ch).getUiSeq();
                if (Objects.equals(UiSeq.CHARGING, seq)) {
                    chargingCurrentData.setIdTagStop(cardNum);
                    rfCardReaderReceive.rfCardReadRelease();
                    // MEMBER_CARD_WAIT으로 전환 → MemberCardWaitFragment CHARGING 분기가 처리
                    fragmentChange.onFragmentChange(ch, UiSeq.MEMBER_CARD_WAIT, null, null);
                } else if (Objects.equals(UiSeq.INIT, seq)) {
                    // TC_039 card-first 오프라인: INIT에서 카드 태그 → MEMBER_CARD_WAIT으로 바로 이동
                    chargingCurrentData.onCurrentDataClear();
                    chargingCurrentData.setConnectorId(ch + 1);
                    chargingCurrentData.setIdTag(cardNum);
                    setUiSeq(UiSeq.MEMBER_CARD_WAIT);
                    fragmentChange.onFragmentChange(ch, UiSeq.MEMBER_CARD_WAIT, null, null);
                    rfCardReaderReceive.rfCardReadRelease();
                } else {
                    setUiSeq(UiSeq.MEMBER_CARD_WAIT);
                    chargingCurrentData.setIdTag(cardNum);
                    fragmentChange.onFragmentChange(ch, UiSeq.MEMBER_CARD_WAIT, null, null);
                    rfCardReaderReceive.rfCardReadRelease();
                }
            } catch (Exception e) {
                logger.error("onRfCardDataReceiveEvent error : {} ", e.getMessage());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleInit(RxData rxData) {
        setoSeq(UiSeq.INIT);
        setPowerMeterCheck(0);
        plugCheckCounter = 0;
        onMeterValueStop();
        onMeterValuesAlignedDataStop();
        if (chargingCurrentData.isReBoot() && onRebootCheck()) {
            setUiSeq(UiSeq.REBOOTING);
        }
        if (chargingCurrentData.getChargePointStatus() == ChargePointStatus.Reserved) {
            String currentTime = zonedDateTimeConvert.doGetUtcDatetimeAsStringSimple();
            if (currentTime.compareTo(chargingCurrentData.getResExpiryDate()) > 0) {
                // available
                chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                        GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                        chargingCurrentData.getResConnectorId(),
                        0,
                        null,
                        null,
                        null,
                        false));
                chargingCurrentData.setResConnectorId(0);
                chargingCurrentData.setResIdTag("");
                chargingCurrentData.setResExpiryDate("");
                chargingCurrentData.setResReservationId("");
                chargingCurrentData.setResParentIdTag("");
                chargingCurrentData.setReservedStatus(ChargePointStatus.Available);
            }
        }
        chargingCurrentData.setUserStop(false);
        targetSoc = 0;
        // OCPP TC_003 cable-first flow: cpVoltage 상승 에지(>=110→<110)일 때만 MEMBER_CARD로 전환
        // 충전 종료 후 케이블이 연결된 채 INIT 재진입 시 중복 전환 방지
        // RemoteStart 진행 중이면 MEMBER_CARD 전환하지 않음 — 케이블 연결 시 PLUG_CHECK로 재진입
        if (Objects.equals(chargerConfiguration.getAuthMode(), "0") &&
                !chargingCurrentData.isReBoot() &&
                !chargingCurrentData.isRemoteStart() &&
                GlobalVariables.isConnectRetry() &&
                rxData.getCpVoltage() < 110 && !prevCsPilot) {
            socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
            if (socketReceiveMessage != null &&
                    socketReceiveMessage.getSocket() != null) {
                if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing)) {
                    chargingCurrentData.onCurrentDataClear();
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                    // StatusNotification(Preparing)은 온라인일 때만 전송 (오프라인 오프차지 시 덤프 불필요)
                    if (GlobalVariables.isConnectRetry() &&
                            socketReceiveMessage.getSocket().getState() == SocketState.OPEN) {
                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                ch + 1, 0, null, null, null, false));
                    }
                }
                chargingCurrentData.setConnectorId(ch + 1);
                setUiSeq(UiSeq.MEMBER_CARD);
                fragmentChange.onFragmentChange(ch, UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
            }
        } else if (chargingCurrentData.isRemoteStart() &&
                !chargingCurrentData.isReBoot() &&
                rxData.getCpVoltage() < 110 && !prevCsPilot) {
            // RemoteStart 진행 중 케이블 감지 시 PLUG_CHECK 재진입
            setUiSeq(UiSeq.PLUG_CHECK);
            fragmentChange.onFragmentChange(ch, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
        } else if (Objects.equals(chargerConfiguration.getAuthMode(), "2") &&
                !chargingCurrentData.isReBoot() &&
                rxData.getCpVoltage() < 110 && !prevCsPilot) {
            chargingCurrentData.onCurrentDataClear();
            chargingCurrentData.setConnectorId(ch + 1);
            chargingCurrentData.setIdTag("");
            setUiSeq(UiSeq.PLUG_CHECK);
            fragmentChange.onFragmentChange(ch, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
        }
        // Preparing 상태에서 케이블 제거(cpVoltage >= 11.0V, 하강 에지) → Available 전송
        if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing)
                && rxData.getCpVoltage() >= 110 && prevCsPilot) {
            chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
            socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
            processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                    ch + 1, 0, null, null, null, false));
        }
    }

    private void handleRebooting() {
        if (!rebootInitiated) {
            rebootInitiated = true;
            String rebootType = chargingCurrentData.getStopReason() == Reason.HardReset ? "Hard" : "Soft";
            fragmentChange.onFragmentChange(getCh(), UiSeq.REBOOTING, "REBOOTING", rebootType);
        }
    }

    private void handlePlugCheck(RxData rxData) {
        if (rxData.isCsPilot()) {
            plugCheckCounter = 0;
            controlBoard.getTxData(getCh()).setStart(true);
            controlBoard.getTxData(getCh()).setStop(false);
            setUiSeq(UiSeq.CONNECT_CHECK);
        } else {
            plugCheckCounter++;
            if (!chargingCurrentData.isRemoteStart() && GlobalVariables.getConnectionTimeOut() > 0 && plugCheckCounter * 2 >= GlobalVariables.getConnectionTimeOut()) {
                plugCheckCounter = 0;
                chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                        GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                        chargingCurrentData.getConnectorId(),
                        0, null, null, null, false));
                setUiSeq(UiSeq.INIT);
                fragmentChange.onFragmentChange(ch, UiSeq.INIT, "INIT", null);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleConnectCheck(RxData rxData) {
        if (rxData.isCsStart()) {
            chargingCurrentData.setChargePointStatus(ChargePointStatus.Charging);
            chargingCurrentData.setTransactionId(0);
            powerUnitPrice = Objects.equals(chargerConfiguration.getAuthMode(), "0") ?
                    chargingCurrentData.getPowerUnitPrice() : Double.parseDouble(chargerConfiguration.getTestPrice());
            chargingCurrentData.setPowerMeterStart(rxData.getPowerMeter() * 10);
            chargingCurrentData.setPowerMeterCalculate(rxData.getPowerMeter());
            chargingCurrentData.setChargingStartTime(zonedDateTimeConvert.getStringCurrentTimeZone());
            //Auto 및 Test
            //socket receive message get instance
            if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                //meter values start
                if (GlobalVariables.getMeterValueSampleInterval() > 0) {
                    onMeterValueStart(chargingCurrentData.getConnectorId(), GlobalVariables.getMeterValueSampleInterval());
                }
                //ClockAlignedDataInterval
                if (GlobalVariables.getClockAlignedDataInterval() > 0) {
                    onMeterValuesAlignedDataStart(chargingCurrentData.getConnectorId(), GlobalVariables.getClockAlignedDataInterval());
                }

                //start transaction send to server
                setUiSeq(UiSeq.CHARGING);
                if (socketReceiveMessage.getSocket().getState() != SocketState.OPEN) {
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(getCh(), UiSeq.CHARGING, "CHARGING", null);
                }
                processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                        GlobalVariables.MESSAGE_HANDLER_START_TRANSACTION,
                        chargingCurrentData.getConnectorId(),
                        0,
                        chargingCurrentData.getIdTag(),
                        null,
                        null,
                        false));
                // StatusNotification(Charging)은 StartTransaction.conf(Accepted) 수신 후 전송
            } else {
                setUiSeq(UiSeq.CHARGING);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(getCh(), UiSeq.CHARGING, "CHARGING", null);
            }
        }
    }

    private void handleCharging(RxData rxData, TxData txData) {
        try {
            //충전 사용량 계산
            onUsePowerMeter(ch, rxData);
            txData.setUiSequence((short) 2);
            //stop 조건
            if (!GlobalVariables.isStopTransactionOnEVSideDisconnect() &&
                    !GlobalVariables.isUnlockConnectorOnEVSideDisconnect()) {
                if (rxData.isCsStop() || !rxData.isCsPilot()) { //|| targetSoc >= 100 ) {
                    if (chargingCurrentData.getStopReason() == Reason.Remote || chargingCurrentData.isUserStop()) {
                        onStop();
                        if (!rxData.isCsPilot()) {
                            //status notification send to server : ChargePointStatus.SuspendedEV
                            //2.4.5. EV Side Disconnected
                            chargingCurrentData.setStopReason(Reason.EVDisconnected);
                            processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                    chargingCurrentData.getConnectorId(),
                                    0,
                                    null,
                                    null,
                                    null,
                                    false));
                        }
                        setUiSeq(UiSeq.FINISH_WAIT);
                    }
                }
            } else {
                if (rxData.isCsStop() || !rxData.isCsPilot() || chargingCurrentData.isUserStop() ||
                        (rxData.getSoc() >= chargerConfiguration.getTargetSoc() && chargerConfiguration.getTargetSoc() != 0)) {
//                                    (chargingCurrentData.getHmChargingLimitFee() <= chargingCurrentData.getPowerMeterUsePay())) {
                    controlBoard.getTxData(getCh()).setStop(true);
                    controlBoard.getTxData(getCh()).setStart(false);
                    if (!rxData.isCsPilot()) {
                        //status notification send to server : ChargePointStatus.SuspendedEV
                        //2.4.5. EV Side Disconnected
                        chargingCurrentData.setStopReason(Reason.EVDisconnected);
//                                    processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
//                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
//                                            chargingCurrentData.getConnectorId(),
//                                            0,
//                                            null,
//                                            null,
//                                            null,
//                                            false));
                    }
                    setUiSeq(UiSeq.FINISH_WAIT);
                } else if (chargingCurrentData.isPrePaymentResult() &&
                        (chargingCurrentData.getPrePayment() <= chargingCurrentData.getPowerMeterUsePay())) {
//                                            || chargingCurrentData.getHmChargingLimitFee() <= chargingCurrentData.getPowerMeterUsePay())) {
                    controlBoard.getTxData(getCh()).setStop(true);
                    controlBoard.getTxData(getCh()).setStart(false);
                    chargingCurrentData.setPowerMeterUsePay(chargingCurrentData.getPrePayment());
                    chargingCurrentData.setStopReason(Reason.Other);
                    setUiSeq(UiSeq.FINISH_WAIT);
                }
            }
        } catch (Exception e) {
            logger.error("handleCharging error : {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleFinishWait(RxData rxData, TxData txData) {
        try {
            txData.setUiSequence((short) 3);

            if (chargingCurrentData.getChargePointStatus() != ChargePointStatus.Finishing) {
                // ── 최초 진입: 미터 정지, 종료 이유 결정, StopTransaction 전송 ──
                onMeterValueStop();
                if (GlobalVariables.isStopTransactionOnEVSideDisconnect() &&
                        (rxData.isCsStop() || !rxData.isCsPilot()) &&
                        !chargingCurrentData.isUserStop() &&
                        !(rxData.getSoc() >= chargerConfiguration.getTargetSoc() && chargerConfiguration.getTargetSoc() != 0) &&
                        chargingCurrentData.getStopReason() != Reason.Remote &&
                        chargingCurrentData.getStopReason() != Reason.DeAuthorized &&
                        chargingCurrentData.getStopReason() != Reason.HardReset &&
                        chargingCurrentData.getStopReason() != Reason.SoftReset &&
                        chargingCurrentData.getStopReason() != Reason.PowerLoss) {
                    chargingCurrentData.setStopReason(Reason.EVDisconnected);
                } else if (chargingCurrentData.isUserStop() && chargingCurrentData.getStopReason() != Reason.EVDisconnected) {
                    chargingCurrentData.setStopReason(Reason.Local);
                }
                chargingCurrentData.setPowerMeterStop(rxData.getPowerMeter()*10);
                chargingCurrentData.setChargingEndTime(zonedDateTimeConvert.getStringCurrentTimeZone());
                chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing); // 최초 진입 완료 표시
                socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                    // StopTx 메시지 enqueue 전에 먼저 플래그 설정:
                    // ProcessHandler(메인 스레드)보다 ClassUiProcess(백그라운드)가 먼저 csStop을
                    // 검사하는 레이스 컨디션 방지 → csStop 경로가 Finishing을 중복 전송하지 않도록
                    chargingCurrentData.setPendingStopTxConf(true);
                    processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                            GlobalVariables.MESSAGE_HANDLER_STOP_TRANSACTION,
                            chargingCurrentData.getConnectorId(),
                            0,
                            chargingCurrentData.getIdTag(),
                            null,
                            null,
                            false));
                    // HardReset/SoftReset: 재부팅 후 boot StatusNotification에서 Finishing 전송용 플래그 저장
                    if (chargingCurrentData.getStopReason() == Reason.HardReset ||
                            chargingCurrentData.getStopReason() == Reason.SoftReset) {
                        new com.dongah.dispenser.utils.FileManagement().fileCreate(
                                "HardResetFinishing_" + chargingCurrentData.getConnectorId(),
                                String.valueOf(chargingCurrentData.getConnectorId()));
                    }
                }
            }

            // csStop 확인 후 ChargingFinish 전환 + StatusNotification(Finishing) 전송
            // pendingStopTxConf=true: StopTx.conf 대기 중 → 건너뜀
            // finishingNotifSent=true: StopTx.conf 핸들러가 이미 전송 → 중복 방지 (TC_32)
            if (rxData.isCsStop()) {
                socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                if (Objects.equals(chargerConfiguration.getAuthMode(), "0") &&
                        !chargingCurrentData.isPendingStopTxConf() &&
                        !chargingCurrentData.isFinishingNotifSent()) {
                    chargingCurrentData.setFinishingNotifSent(true);
                    processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_ONCE,
                            chargingCurrentData.getConnectorId(),
                            0,
                            null,
                            null,
                            "Finishing",
                            false));
                }
                setUiSeq(UiSeq.FINISH);
                fragmentChange.onFragmentChange(getCh(), UiSeq.FINISH, "FINISH", null);
            }
        } catch (Exception e) {
            logger.error("handleFinishWaitFINISH_WAIT error : {} ", e.getMessage());
        }
    }

    private void handleFinish(RxData rxData) {
        try {
            onMeterValueStop();
            onMeterValuesAlignedDataStop();
            Thread.sleep(5000);
            onFinish();
            //reserved check
            if (Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getResIdTag()) ||
                    Objects.equals(chargingCurrentData.getParentIdTag(), chargingCurrentData.getResParentIdTag())) {
                chargingCurrentData.setResConnectorId(0);
                chargingCurrentData.setResIdTag("");
                chargingCurrentData.setResExpiryDate("");
                chargingCurrentData.setResReservationId("");
                chargingCurrentData.setResParentIdTag("");
                chargingCurrentData.setReservedStatus(ChargePointStatus.Available);
            }
            if (!rxData.isCsPilot() && Objects.equals(ChargePointStatus.Finishing, chargingCurrentData.getChargePointStatus())) {
                chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                        GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                        chargingCurrentData.getConnectorId(),
                        0,
                        null,
                        null,
                        null,
                        false));
            }
        } catch (Exception e) {
            logger.error("handleFinish error : {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleFault(RxData rxData, TxData txData) {
        //* fault check */
        if (getUiSeq().getValue() < 15) {
            if (!(getCurrentFragment() instanceof FaultFragment)) {
                // server mode 및 charging
                if (Objects.equals(chargerConfiguration.getAuthMode(), "0") &&
                        Objects.equals(getoSeq(), UiSeq.CHARGING)) {
                    // meter values stop
                    onMeterValueStop();
                    txData.setStart(false);
                    txData.setStop(true);
                    //PLC USed
//                                if (chargerConfiguration.isUsedPLC()) {
//                                    onBatteryInfoStop();
//                                }
                    chargingCurrentData.setUserStop(false);
                    chargingCurrentData.setPowerMeterStop(rxData.getPowerMeter()*10);
                    chargingCurrentData.setChargingEndTime(zonedDateTimeConvert.getStringCurrentTimeZone());
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);
                    //socket receive message get instance
                    socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                    SocketState state = socketReceiveMessage.getSocket().getState();
                    if (Objects.equals(state.getValue(), 7) && Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                        //server send
                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                GlobalVariables.MESSAGE_HANDLER_STOP_TRANSACTION,
                                chargingCurrentData.getConnectorId(),
                                0,
                                chargingCurrentData.getIdTag(),
                                null,
                                null,
                                false));
                        //status notification send to server
                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                chargingCurrentData.getConnectorId(),
                                0,
                                null,
                                null,
                                null,
                                false));
                    }
                }
                fragmentChange.onFragmentChange(getCh(), UiSeq.FAULT, "FAULT", "1");
            }
        }
        //fault 가 해제가 되면..........
        if (controlBoard.isConnected() && !rxData.isCsFault()) {
            if (Objects.equals(getoSeq(), UiSeq.CHARGING)) {
                txData.setUiSequence((short) 3);
                chargingCurrentData.setChargePointStatus(ChargePointStatus.Finishing);
                chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.NoError);
                setUiSeq(UiSeq.FINISH);
                fragmentChange.onFragmentChange(getCh(), UiSeq.FINISH, "FINISH", null);
            } else {
                if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                        rxData.getCpVoltage() >= 110) {
                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                    //socket receive message get instance
                    socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
                    SocketState state = socketReceiveMessage.getSocket().getState();
                    if (Objects.equals(state.getValue(), 7) && Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                chargingCurrentData.getConnectorId(),
                                0,
                                null,
                                null,
                                null,
                                false));
                    }
                }
                onHome();
            }
        }
    }
}

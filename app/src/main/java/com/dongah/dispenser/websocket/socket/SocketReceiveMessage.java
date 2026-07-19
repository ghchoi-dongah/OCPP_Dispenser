package com.dongah.dispenser.websocket.socket;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.DataTransformation;
import com.dongah.dispenser.basefunction.DumpDataSend;
import com.dongah.dispenser.basefunction.FileTransType;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.FtpRxJava;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.HTTPHelper;
import com.dongah.dispenser.basefunction.PaymentType;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.controlboard.TxData;
import com.dongah.dispenser.handler.ProcessHandler;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.utils.LogDataSave;
import com.dongah.dispenser.utils.ToastPositionMake;
import com.dongah.dispenser.websocket.ocpp.common.JSONCommunicator;
import com.dongah.dispenser.websocket.ocpp.common.OccurenceConstraintException;
import com.dongah.dispenser.websocket.ocpp.common.model.Confirmation;
import com.dongah.dispenser.websocket.ocpp.common.model.Message;
import com.dongah.dispenser.websocket.ocpp.common.model.Request;
import com.dongah.dispenser.websocket.ocpp.core.AuthorizationStatus;
import com.dongah.dispenser.websocket.ocpp.core.AvailabilityType;
import com.dongah.dispenser.websocket.ocpp.core.BootNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.StatusNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.core.StopTransactionRequest;
import com.dongah.dispenser.websocket.ocpp.core.ChargingProfileKindType;
import com.dongah.dispenser.websocket.ocpp.core.ChargingProfilePurposeType;
import com.dongah.dispenser.websocket.ocpp.core.ChargingSchedule;
import com.dongah.dispenser.websocket.ocpp.core.ChargingSchedulePeriod;
import com.dongah.dispenser.websocket.ocpp.core.DataTransferConfirmation;
import com.dongah.dispenser.websocket.ocpp.core.DataTransferStatus;
import com.dongah.dispenser.websocket.ocpp.core.HeartbeatRequest;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.ocpp.core.RecurrencyKindType;
import com.dongah.dispenser.websocket.ocpp.core.RegistrationStatus;
import com.dongah.dispenser.websocket.ocpp.core.RemoteStartStopStatus;
import com.dongah.dispenser.websocket.ocpp.core.RemoteStopTransactionConfirmation;
import com.dongah.dispenser.websocket.ocpp.core.ResetConfirmation;
import com.dongah.dispenser.websocket.ocpp.core.ResetStatus;
import com.dongah.dispenser.websocket.ocpp.core.ResetType;
import com.dongah.dispenser.websocket.ocpp.core.UnlockConnectorConfirmation;
import com.dongah.dispenser.websocket.ocpp.core.UnlockStatus;
import com.dongah.dispenser.websocket.ocpp.firmware.DiagnosticsStatus;
import com.dongah.dispenser.websocket.ocpp.firmware.DiagnosticsStatusNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.firmware.FirmwareStatus;
import com.dongah.dispenser.websocket.ocpp.firmware.FirmwareStatusNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.firmware.GetDiagnosticsConfirmation;
import com.dongah.dispenser.websocket.ocpp.firmware.UpdateFirmwareConfirmation;
import com.dongah.dispenser.websocket.ocpp.localauthlist.GetLocalListVersionConfirmation;
import com.dongah.dispenser.websocket.ocpp.localauthlist.SendLocalListConfirmation;
import com.dongah.dispenser.websocket.ocpp.localauthlist.UpdateStatus;
import com.dongah.dispenser.websocket.ocpp.localauthlist.UpdateType;
import com.dongah.dispenser.websocket.ocpp.remotetrigger.TriggerMessageConfirmation;
import com.dongah.dispenser.websocket.ocpp.remotetrigger.TriggerMessageRequestType;
import com.dongah.dispenser.websocket.ocpp.remotetrigger.TriggerMessageStatus;
import com.dongah.dispenser.websocket.ocpp.reservation.CancelReservationConfirmation;
import com.dongah.dispenser.websocket.ocpp.reservation.CancelReservationStatus;
import com.dongah.dispenser.websocket.ocpp.reservation.ReservationStatus;
import com.dongah.dispenser.websocket.ocpp.reservation.ReserveNowConfirmation;
import com.dongah.dispenser.websocket.ocpp.security.CertificateHashDataType;
import com.dongah.dispenser.websocket.ocpp.security.CertificateStatus;
import com.dongah.dispenser.websocket.ocpp.security.CertificateUse;
import com.dongah.dispenser.websocket.ocpp.security.DeleteCertificateConfirmation;
import com.dongah.dispenser.websocket.ocpp.security.DeleteCertificateStatus;
import com.dongah.dispenser.websocket.ocpp.security.ExtendedTriggerMessageConfirmation;
import com.dongah.dispenser.websocket.ocpp.security.GetInstalledCertificateIdsConfirmation;
import com.dongah.dispenser.websocket.ocpp.security.GetInstalledCertificateStatus;
import com.dongah.dispenser.websocket.ocpp.security.GetLogConfirmation;
import com.dongah.dispenser.websocket.ocpp.security.HashAlgorithm;
import com.dongah.dispenser.websocket.ocpp.security.InstallCertificateConfirmation;
import com.dongah.dispenser.websocket.ocpp.security.LogStatus;
import com.dongah.dispenser.websocket.ocpp.security.LogStatusNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.security.LogType;
import com.dongah.dispenser.websocket.ocpp.security.SecurityEventNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.security.SignedFirmwareStatus;
import com.dongah.dispenser.websocket.ocpp.security.SignedFirmwareStatusNotificationRequest;
import com.dongah.dispenser.websocket.ocpp.security.SignedUpdateFirmwareConfirmation;
import com.dongah.dispenser.websocket.ocpp.security.UpdateFirmwareStatus;
import com.dongah.dispenser.websocket.ocpp.smartcharging.ChargingProfileStatus;
import com.dongah.dispenser.websocket.ocpp.smartcharging.ChargingRateUnitType;
import com.dongah.dispenser.websocket.ocpp.smartcharging.ClearChargingProfileConfirmation;
import com.dongah.dispenser.websocket.ocpp.smartcharging.ClearChargingProfileStatus;
import com.dongah.dispenser.websocket.ocpp.smartcharging.GetCompositeScheduleConfirmation;
import com.dongah.dispenser.websocket.ocpp.smartcharging.GetCompositeScheduleStatus;
import com.dongah.dispenser.websocket.ocpp.smartcharging.SetChargingProfileConfirmation;
import com.dongah.dispenser.websocket.ocpp.utilities.Stopwatch;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.google.gson.Gson;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import okhttp3.WebSocket;

public class SocketReceiveMessage extends JSONCommunicator implements SocketInterface {

    private static final Logger logger = LoggerFactory.getLogger(SocketReceiveMessage.class);


    static {
        // BouncyCastle 프로바이더 추가 (Android에서 RSASSA-PSS 사용 가능하도록)
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Okhttp3 web-socket
     */
    WebSocket webSocket = null;
    /**
     * user define socket blue-networks socket
     */
    Socket socket = null;
    String url;
    String actionName;
    Message message = null;
    /**
     * message send/receive list (UUID,Action)
     */
    HashMap<String, String> hashMapUuid = null;
    HashMap<String, Object> newHashMapUuid = null;
    HashMap<Integer, Integer> getConnectorIdHashMap;
    int channel;
    int connectorId;
    SendHashMapObject sendHashMapObject;
    JSONObject jsonObjectData;
    UiSeq uiSeq;
    /**
     * LogData save class
     */
    LogDataSave logDataSave = new LogDataSave("log");
    /**
     * dump data save actions
     */
    String[] actionNames = {"StopTransaction", "partialCancel", "resultPrice"};
    ArrayList<String> actionList = new ArrayList<>();
    LogDataSave logDataSaveDump = new LogDataSave("dump");
    FragmentChange fragmentChange;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    ProcessHandler processHandler;

    FileManagement fileManagement;
    ZonedDateTimeConvert zonedDateTimeConvert;
    ToastPositionMake toastPositionMake = new ToastPositionMake(((MainActivity) MainActivity.mContext));
    /**
     * web socket listener register
     */
    SocketMessageListener socketMessageListener;

    DiagnosticsStatusNotificationRequest diagnosticsStatusNotificationRequest;
    Date compositeTime;


    public SocketMessageListener getSocketMessageListener() {
        return socketMessageListener;
    }

    public void setSocketMessageListener(SocketMessageListener socketMessageListener) {
        this.socketMessageListener = socketMessageListener;
    }

    public ToastPositionMake getToastPositionMake() {
        return toastPositionMake;
    }

    /**
     * web socket debug listener register
     */
    private SocketMessageListener socketMessageDebugListener;

    public void setSocketMessageDebugListener(SocketMessageListener listener) {
        this.socketMessageDebugListener = listener;
    }

    public void setSocketMessageDebugListenerStop() {
        this.socketMessageDebugListener = null;
    }

    /**
     * socket getter
     */
    public Socket getSocket() {
        return socket;
    }


    /**
     * socket constructor
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public SocketReceiveMessage(String url) {
        this.url = url;
        fragmentChange = ((MainActivity) MainActivity.mContext).getFragmentChange();
        processHandler = ((MainActivity) MainActivity.mContext).getProcessHandler();
        zonedDateTimeConvert = new ZonedDateTimeConvert();
        fileManagement = new FileManagement();
        Collections.addAll(actionList, actionNames);
        onSocketInitialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onSocketInitialize() {
        try {
            // SecurityProfile이 바뀌어도 항상 올바른 ws:// / wss:// 로 접속하도록 매번 URL 재구성
            try {
                ChargerConfiguration cfg = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
                String sp = GlobalVariables.getSecurityProfile();
                boolean useTls = Objects.equals(sp, "2") || Objects.equals(sp, "3");
                url = (useTls ? "wss://" : "ws://")
                        + cfg.getServerConnectingString() + ":"
                        + cfg.getServerPort() + "/"
                        + cfg.getChargerId();
            } catch (Exception e) {
                logger.warn("URL rebuild failed, using stored url: {}", e.getMessage());
            }

            if (socket != null) {
                socket.fullClose();
                socket = null;
            }

            // request  ==> UUID, ActionName  hashmap 저장
            // response ==> hashmap find uuid 삭제
            // (key:UUID, value:Action) ==> hashMap
            if (hashMapUuid != null) hashMapUuid = null;
            hashMapUuid = new HashMap<String, String>();

            if (newHashMapUuid != null) newHashMapUuid = null;
            newHashMapUuid = new HashMap<String, Object>();

            // connectorId to channel (remoteStart ==> remoteStop)
            if (getConnectorIdHashMap != null) getConnectorIdHashMap = null;
            getConnectorIdHashMap = new HashMap<Integer, Integer>();

            socket = new Socket(url);
            socket.setState(SocketState.OPENING);
            socket.getInstance(this);

        } catch (Exception e) {
            logger.error(" socket receive message  : {}", e.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        socket.setState(SocketState.OPEN);

        if (GlobalVariables.SecurityEventNotification) {
            if (GlobalVariables.InvalidCSMSCertificate) {
                sendSecurityEventNotification("InvalidCentralSystemCertificate",
                        "CSMS certificate common name mismatch");
                GlobalVariables.InvalidCSMSCertificate = false;
            } else {
                sendSecurityEventNotification("InvalidCentralSystemCertificate",
                        "SSL Handshake failed: certificate_unknown");
            }
            GlobalVariables.SecurityEventNotification = false;
        }

        // 미전송 데이터 확인: main Looper에서 실행하여 offline 기간에 쌓인
        // processHandler 메시지가 모두 처리(dump 저장)된 후 dump 파일을 읽도록 보장
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                DumpDataSend dumpDataSend = new DumpDataSend();
                dumpDataSend.onDumpSend();
            } catch (Exception e) {
                logger.error(" bootNotification Dump error : {}", e.getMessage());
            }
            // dump 처리 완료 후 BootNotification/Heartbeat 시작
            // dumpPendingStop/dumpPendingFinishing=true이면 conf 수신 후 시작 (메시지 순서 보장)
            if (!GlobalVariables.dumpPendingStop && !GlobalVariables.dumpPendingFinishing) {
                onStartBootOrHeartbeat();
            } else {
                GlobalVariables.pendingBootAfterDump = true;
            }
        });

        // 재접속 후 CHARGING 상태인데 transactionId가 없으면 StartTransaction 재전송 (race condition 방어)
        processHandler.postDelayed(() -> {
            for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                try {
                    ClassUiProcess uiProcess = ((MainActivity) MainActivity.mContext).getClassUiProcess(i);
                    UiSeq pendingSeq = uiProcess.getUiSeq();
                    ChargingCurrentData pendingData = uiProcess.getChargingCurrentData();
                    if (Objects.equals(pendingSeq, UiSeq.CHARGING)
                            && pendingData.getTransactionId() == 0
                            && !TextUtils.isEmpty(pendingData.getIdTag())) {
                        processHandler.sendMessage(onMakeHandlerMessage(
                                GlobalVariables.MESSAGE_HANDLER_START_TRANSACTION,
                                pendingData.getConnectorId(),
                                0,
                                pendingData.getIdTag(),
                                null,
                                null,
                                false));
                    }
                } catch (Exception e) {
                    logger.error("charging fallback error: {}", e.getMessage());
                }
            }
        }, 3000);

    }

    private void onStartBootOrHeartbeat() {
        try {
            if (!GlobalVariables.isConnectRetry()) {
                processHandler.sendMessage(onMakeHandlerMessage(
                        GlobalVariables.MESSAGE_HANDLER_BOOT_NOTIFICATION,
                        100, 5, null, null, null, false));
            } else {
                processHandler.sendMessage(onMakeHandlerMessage(
                        GlobalVariables.MESSAGE_HANDLER_HEART_BEAT,
                        100,
                        Integer.parseInt(getConfigurationValue("HeartbeatInterval")),
                        null, null, null, false));
            }
        } catch (Exception e) {
            logger.error("onStartBootOrHeartbeat error: {}", e.getMessage());
        }
    }

    @Override
    public void onGetMessage(WebSocket webSocket, String text) throws JSONException {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void run() {
                try {
                    actionName = null;
                    message = parse(text);
                    int resultType = message.getResultType();
                    switch (resultType) {
                        case 2:
                            actionName = message.getAction();
                            break;
                        case 3:
                            //request 에 대한 response
                            sendHashMapObject = (SendHashMapObject) newHashMapUuid.get(message.getId());
                            if (sendHashMapObject != null) {
                                connectorId = sendHashMapObject.getConnectorId();
                                // connectorId=0: 전체 충전기 대상 메시지(CP 자체 StatusNotification 등), channel 매핑 불필요
                                setChannel(connectorId == 1 ? 0 : connectorId == 2 ? 1 : -1);
                                actionName = sendHashMapObject.getActionName();
                            }
                            break;
                    }

                    JSONObject jsonObject = new JSONObject(message.getPayload().toString());
                    logDataSave.makeLogDate(actionName, text);
                    //* debug event listener register */
                    if (socketMessageDebugListener != null)
                        socketMessageDebugListener.onMessageReceiveDebugEvent(1, text, actionName);
                    //* receive message type */
                    switch (resultType) {
                        case 4:
                            //* Central System response --> call error message  */
                            break;
                        case 3:
                            //* Central System response --> success * SEND ACTION 의 UUID find , actionName parser */
                            if (!newHashMapUuid.containsKey(message.getId())) return;
                            //* default info */

                            if (!Objects.equals(100, connectorId) && connectorId != 0) {
                                chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
                                chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(getChannel());
                            }
                            /////////////////////
                            if (Objects.equals("BootNotification", actionName)) {
                                try {
                                    // BootNotification 응답 수신 시 항상 루프 중단 (interval 파싱 실패해도 루프 멈춰야 함)
                                    processHandler.onBootNotificationStop();
                                    GlobalVariables.setConnectRetry(true);
                                    int interval = jsonObject.getInt("interval");
                                    ZonedDateTime currentTime = ZonedDateTime.parse(jsonObject.getString("currentTime"));
                                    // bootNotification 응답을 받아서 Heartbeat delayTime 만큼 데이터 전송
                                    RegistrationStatus status = RegistrationStatus.valueOf(jsonObject.getString("status"));
                                    if (Objects.equals(status, RegistrationStatus.Accepted)) {
                                        // CSMS 응답 interval 값을 ConfigurationKey에 저장 (TC_001)
                                        setConfigurationValue("HeartbeatInterval", String.valueOf(interval));
                                        if (!GlobalVariables.isTriggerBootNotification()) {
                                            // Status Notification */
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_BOOT,
                                                    100,
                                                    0,
                                                    null,
                                                    null,
                                                    null,
                                                    false));
                                            // heart beat */
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_HEART_BEAT,
                                                    100,
                                                    interval,
                                                    null,
                                                    null,
                                                    null,
                                                    false));
                                        } else {
                                            GlobalVariables.setTriggerBootNotification(false);
                                        }

                                    } else if (Objects.equals(status, RegistrationStatus.Rejected)) {
                                        processHandler.sendMessage(onMakeHandlerMessage(
                                                GlobalVariables.MESSAGE_HANDLER_BOOT_NOTIFICATION,
                                                100,
                                                interval,
                                                null,
                                                null,
                                                null,
                                                false));

                                    } else if (Objects.equals(status, RegistrationStatus.Pending)) {
                                        processHandler.onBootNotificationStart(interval);
                                        GlobalVariables.setReconnectCheck(false);
                                    }
                                } catch (Exception e) {
                                    logger.error("BootNotification error ; {} ", e.getMessage());
                                }

                            } else if (Objects.equals("Heartbeat", actionName)) {
                                // Heartbeat */
                                try {
                                    ZonedDateTime currentTime = ZonedDateTime.parse(jsonObject.getString("currentTime"));
                                } catch (Exception e) {
                                    logger.error("Heartbeat error : {} ", e.getMessage());
                                }
                            } else if (Objects.equals("Authorize", actionName)) {
                                jsonObjectData = jsonObject.getJSONObject("idTagInfo");
                                AuthorizationStatus status = AuthorizationStatus.valueOf(jsonObjectData.getString("status"));
                                String parentIdTag = jsonObjectData.has("parentIdTag") ? jsonObjectData.getString("parentIdTag") : "";
                                String expiryDate = jsonObjectData.has("expiryDate") ? jsonObjectData.getString("expiryDate") : "";
                                uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(getChannel()).getUiSeq();
                                if (Objects.equals(status, AuthorizationStatus.Accepted)) {
                                    //인증 성공(getPrice 단가 요청)
                                    chargingCurrentData.setAuthorizeResult(true);
                                    if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                                        chargingCurrentData.setParentIdTagStop(parentIdTag);
                                        if (Objects.equals(chargingCurrentData.getParentIdTag(), chargingCurrentData.getParentIdTagStop())) {
                                            // parentIdTag 일치: 자동 정지 트리거 (OCPP 1.6 TC_069)
                                            chargingCurrentData.setUserStop(true);
                                        } else {
                                            ((MainActivity) MainActivity.mContext).getClassUiProcess(getChannel()).setUiSeq(UiSeq.CHARGING);
                                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(getChannel(), UiSeq.CHARGING, "CHARGING", null);
                                        }
                                    } else {
                                        chargingCurrentData.setParentIdTag(parentIdTag);
                                        boolean ocppMode = ((MainActivity) MainActivity.mContext).getChargerConfiguration().isStopConfirm();
                                        // RemoteStartTransaction 플로우는 getPrice 불필요 (OCPP 컴플라이언스 테스트 도구가 미지원)
                                        boolean skipGetPrice = chargingCurrentData.isRemoteStart();
                                        if (!ocppMode && Objects.equals(chargerConfiguration.getAuthMode(), "0") && !skipGetPrice) {
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_GET_PRICE,
                                                    chargingCurrentData.getConnectorId(),
                                                    0,
                                                    chargingCurrentData.getIdTag(),
                                                    null,
                                                    null,
                                                    false));
                                        } else {
                                            chargingCurrentData.setPowerUnitPrice(Double.parseDouble(chargerConfiguration.getTestPrice()));
                                            ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).setUiSeq(UiSeq.PLUG_CHECK);
                                            fragmentChange.onFragmentChange(channel,UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                                        }

                                        // 로컬카드(card-first): 케이블 미연결 시 Preparing 전송
                                        // RemoteStart/cable-first: 이미 Preparing → 중복 방지
                                        if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing)) {
                                            chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                    chargingCurrentData.getConnectorId(),
                                                    0,
                                                    null,
                                                    null,
                                                    null,
                                                    false));
                                        }
                                    }
                                } else {
                                    String certificationReason = status.name();
                                    if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).setUiSeq(UiSeq.CHARGING);
                                        fragmentChange.onFragmentChange(channel,UiSeq.CHARGING, "CHARGING", null);
                                        Toast.makeText((MainActivity.mContext).getApplicationContext(), "충전 중지 인증 실패 : " + certificationReason, Toast.LENGTH_SHORT).show();
                                    } else {
                                        //
                                        if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                                Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                                            chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                    chargingCurrentData.getConnectorId(),
                                                    0,
                                                    null,
                                                    null,
                                                    null,
                                                    false));
                                        }

                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getChargingCurrentData().setAuthorizeResult(false);
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).onHome();
                                        Toast.makeText((MainActivity.mContext).getApplicationContext(), "인증 실패: " + certificationReason, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else if (Objects.equals("getPrice", actionName)) {
                                boolean getPriceAccepted = false;
                                try {
                                    DataTransferStatus status = DataTransferStatus.valueOf(jsonObject.getString("status"));
                                    JSONObject jsonObjectDataPrices = new JSONObject(jsonObject.getString("data"));
                                    JSONArray jsonArrayUnitPrice = jsonObjectDataPrices.getJSONArray("unitPrice");
                                    JSONObject jsonObjectUnitPrice = jsonArrayUnitPrice.getJSONObject(0);
                                    if (Objects.equals(DataTransferStatus.Accepted, status)) {
                                        getPriceAccepted = true;
                                        chargingCurrentData.setPowerUnitPrice(jsonObjectUnitPrice.getDouble("unitPrice"));
                                    }
                                } catch (Exception e) {
                                    logger.error("getPrice response parse error: {}", e.getMessage());
                                }
                                // Accepted든 실패든 PLUG_CHECK로 전환 (실패 시 testPrice 사용)
                                if (!getPriceAccepted) {
                                    chargingCurrentData.setPowerUnitPrice(Double.parseDouble(chargerConfiguration.getTestPrice()));
                                }
                                //비회원 회원인 경우 구분 (회원 : plug_wait  /  비회원 : auth_credit)
                                switch (chargingCurrentData.getPaymentType().value()) {
                                    case 1:
                                    case 3:
                                    case 4:
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(getChannel()).setUiSeq(UiSeq.PLUG_CHECK);
                                        fragmentChange.onFragmentChange(getChannel(), UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                                        break;
                                    case 2:
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(getChannel()).setUiSeq(UiSeq.CREDIT_CARD);
                                        fragmentChange.onFragmentChange(getChannel(), UiSeq.CREDIT_CARD, "CREDIT_CARD", null);
                                        break;
                                    default:
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(getChannel()).setUiSeq(UiSeq.PLUG_CHECK);
                                        fragmentChange.onFragmentChange(getChannel(), UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                                        break;
                                }
                                //단가 Accept ==> status preparing
                                if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing)) {
                                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                            chargingCurrentData.getConnectorId(),
                                            0,
                                            null,
                                            null,
                                            null,
                                            false));
                                }
                            } else if (Objects.equals("StartTransaction", actionName)) {
                                chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getChargingCurrentData();
                                /** remote stop 에서 connectorId(채널) 정보 필요 ==> transactionId로 connectorId */
                                getConnectorIdHashMap.put(jsonObject.getInt("transactionId"), connectorId);
                                //서버에서 transactionId 받음 ==> stopTransaction 계속하여 사용.
                                chargingCurrentData.setTransactionId(jsonObject.getInt("transactionId"));

                                GlobalVariables.dumpTransactionId = jsonObject.getInt("transactionId");

                                jsonObjectData = jsonObject.getJSONObject("idTagInfo");
                                AuthorizationStatus status = AuthorizationStatus.valueOf(jsonObjectData.getString("status"));
                                String parentIdTag = jsonObjectData.has("parentIdTag") ? jsonObjectData.getString("parentIdTag") : "";
                                String expiryDate = jsonObjectData.has("expiryDate") ? jsonObjectData.getString("expiryDate") : "";
                                //accept continue
                                uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getUiSeq();
                                if (Objects.equals(status, AuthorizationStatus.Accepted)) {
                                    if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).setUiSeq(UiSeq.CHARGING);
                                        fragmentChange.onFragmentChange(channel,UiSeq.CHARGING, "CHARGING", null);
                                        // dump에서 StatusNoti(Charging)이 이미 전송된 경우 중복 방지
                                        if (!GlobalVariables.dumpChargingNotifSent) {
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                    connectorId,
                                                    0, null, null, null, false));
                                        }
                                    }
                                    GlobalVariables.dumpChargingNotifSent = false; // 항상 리셋
                                    if (GlobalVariables.dumpPendingStop) {
                                        GlobalVariables.dumpPendingStop = false;
                                        try {
                                            ZonedDateTime endTs = zonedDateTimeConvert.doZonedDateTimeToDatetime(chargingCurrentData.getChargingEndTime());
                                            StopTransactionRequest stopReq = new StopTransactionRequest(
                                                    chargingCurrentData.getPowerMeterStop(), endTs,
                                                    chargingCurrentData.getTransactionId(),
                                                    chargingCurrentData.getStopReason());
                                            stopReq.setIdTag(TextUtils.isEmpty(chargingCurrentData.getIdTagStop()) ?
                                                    chargingCurrentData.getIdTag() : chargingCurrentData.getIdTagStop());
                                            // TC_039: Finishing은 StopTx.conf 수신 후 pendingStopTxConf 경로로 전송
                                            chargingCurrentData.setPendingStopTxConf(true);
                                            onSend(connectorId, stopReq.getActionName(), stopReq);
                                        } catch (Exception e) {
                                            logger.error("dumpPendingStop send error: {}", e.getMessage());
                                        }
                                        // pendingBootAfterDump는 StopTx.conf → pendingStopTxConf 핸들러로 이동
                                    }
                                } else if (Objects.equals(status, AuthorizationStatus.ConcurrentTx)) {
                                    // 다른 트랜잭션이 이미 진행 중 — 충전 중지 처리
                                    if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                                        chargingCurrentData.setStopReason(Reason.DeAuthorized);
                                        TxData txDataConcurrent = ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(channel);
                                        txDataConcurrent.setStart(false);
                                        txDataConcurrent.setStop(true);
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).setUiSeq(UiSeq.FINISH_WAIT);
                                    } else {
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).onHome();
                                    }
                                } else if (Objects.equals(status, AuthorizationStatus.Invalid)) {
                                    if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                                        if (GlobalVariables.isStopTransactionOnInvalidId()) {
                                            chargingCurrentData.setStopReason(Reason.DeAuthorized);
                                            TxData txDataStop = ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(channel);
                                            txDataStop.setStart(false);
                                            txDataStop.setStop(true);
                                            ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).setUiSeq(UiSeq.FINISH_WAIT);
                                        } else {
                                            // StopTransactionOnInvalidId=false: SuspendedEVSE 전송 후 충전 유지 (StopTransaction 금지)
                                            processHandler.postDelayed(() -> {
                                                processHandler.sendMessage(
                                                        onMakeHandlerMessage(
                                                                GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                                chargingCurrentData.getConnectorId(),
                                                                0,
                                                                null,
                                                                "Suspend",
                                                                null,
                                                                false
                                                        )
                                                );
                                            }, 2000);
                                        }
                                    }
                                } else {
                                    //충전기 정지
                                    TxData txData = ((MainActivity) MainActivity.mContext).getControlBoard().getTxData(channel);
                                    txData.setStart(false);
                                    txData.setStop(true);

                                    ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).onMeterValueStop();
                                    //PLC USed
//                                    if (chargerConfiguration.isUsedPLC()) {
//                                        ((MainActivity) MainActivity.mContext).getClassUiProcess().onBatteryInfoStop();
//                                    }
                                    // CREDIT 선 결제가  있는 경우
                                    if (chargingCurrentData.isPrePaymentResult()) {
                                        // 부분 취소.....
//                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(getChannel()).onNoCardCancelTrigger();
                                    }
                                    ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).onHome();
                                }
                            } else if (Objects.equals("StopTransaction", actionName)) {
                                // idTagInfo is Optional in OCPP 1.6 StopTransactionConfirmation
                                if (jsonObject.has("idTagInfo")) {
                                    try {
                                        jsonObjectData = jsonObject.getJSONObject("idTagInfo");
                                        AuthorizationStatus status = AuthorizationStatus.valueOf(jsonObjectData.getString("status"));
                                        String parentIdTag = jsonObjectData.has("parentIdTag") ? jsonObjectData.getString("parentIdTag") : "";
                                        String expiryDate = jsonObjectData.has("expiryDate") ? jsonObjectData.getString("expiryDate") : "";
                                        logger.info("StopTransaction confirmed: status={}", status);
                                    } catch (Exception e) {
                                        logger.error("StopTransaction idTagInfo parse error: {}", e.getMessage());
                                    }
                                }
                                // TC_032/TC_038: StopTransaction.conf 수신 후 StatusNotification(Finishing) 전송
                                // TC_039 dump 오프라인 플로우(pendingBootAfterDump=true)에서는 Finishing 전송 안함
                                // finishingNotifSent 체크 제거: pendingStopTxConf=true는 ClassUiProcess가 csStop
                                // 경로를 건너뛰도록 설정된 것이므로, conf 수신 시 무조건 Finishing 전송
                                if (chargingCurrentData.isPendingStopTxConf()) {
                                    chargingCurrentData.setPendingStopTxConf(false);
                                    if (!GlobalVariables.pendingBootAfterDump &&
                                            Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                                        chargingCurrentData.setFinishingNotifSent(true);
                                        processHandler.sendMessage(onMakeHandlerMessage(
                                                GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_ONCE,
                                                chargingCurrentData.getConnectorId(),
                                                0,
                                                null,
                                                null,
                                                "Finishing",
                                                false));
                                    }
                                    if (GlobalVariables.pendingBootAfterDump) {
                                        GlobalVariables.pendingBootAfterDump = false;
                                        onStartBootOrHeartbeat();
                                    }
                                }
                                // PowerLoss dump: StopTx.conf → StatusNoti(Finishing) → BootNotification 순서 보장
                                if (GlobalVariables.dumpPendingFinishing) {
                                    GlobalVariables.dumpPendingFinishing = false;
                                    if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                                        processHandler.sendMessage(onMakeHandlerMessage(
                                                GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_ONCE,
                                                GlobalVariables.dumpFinishingConnectorId,
                                                0,
                                                null,
                                                null,
                                                "Finishing",
                                                false));
                                    }
                                    if (GlobalVariables.pendingBootAfterDump) {
                                        GlobalVariables.pendingBootAfterDump = false;
                                        onStartBootOrHeartbeat();
                                    }
                                }
                            } else if (Objects.equals("payInfo", actionName)) {
                                DataTransferStatus status = DataTransferStatus.valueOf(jsonObject.getString("status"));
                                if (Objects.equals(status, DataTransferStatus.Accepted)) {
                                    jsonObjectData = jsonObject.getJSONObject("data");
                                    DataTransferStatus statusData = DataTransferStatus.valueOf(jsonObjectData.getString("status"));
                                    if (Objects.equals(chargingCurrentData.getPayId(), jsonObjectData.getString("payId"))) {
                                        //커플러 연결
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).setUiSeq(UiSeq.PLUG_CHECK);
                                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(channel,UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                                    }
                                } else {
                                    if (chargingCurrentData.isPrePaymentResult()) {
//                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(getChannel()).onNoCardCancelTrigger();
                                        Toast.makeText((MainActivity.mContext).getApplicationContext(),
                                                "선결제 취소 금액 : " + chargingCurrentData.getPrePayment() + "원", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else if (Objects.equals("PartialCancel", actionName)) {
                                DataTransferStatus status = DataTransferStatus.valueOf(jsonObject.getString("status"));
                                if (Objects.equals(status, DataTransferStatus.Accepted)) {
                                    JSONObject data = jsonObject.has("data") ? new JSONObject(jsonObject.getString("data")) : null;
                                    if (data != null) {
                                        //DataTransferStatus statusData = DataTransferStatus.valueOf(data.getString("status"));
                                    }
                                }
                            } else if (Objects.equals("resultPrice", actionName)) {
                                DataTransferStatus status = DataTransferStatus.valueOf(jsonObject.getString("status"));
                                if (Objects.equals(status, DataTransferStatus.Accepted)) {
                                    //accept
                                    JSONObject jsonObjectDataResult = new JSONObject(jsonObject.getString("data"));
//                                    int connectorId = jsonObjectDataResult.getInt("connectorId");
//                                    int resultPrice = jsonObjectDataResult.getInt("resultPrice");
                                }
                            } else if (Objects.equals("smsMessage", actionName)) {
                                DataTransferStatus status = DataTransferStatus.valueOf(jsonObject.getString("status"));
                                if (Objects.equals(status, DataTransferStatus.Accepted)) {
                                    //sms success
                                }
                            } else if (Objects.equals("FirmwareStatusNotification", actionName)) {
                                //DownLoading
                                if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.Downloading)) {
                                    // downloading start
                                    // 나중에 삭제 해야 함.
//                                    String location = "211.44.234.112";
//                                    location = "192.168.30.120";
//                                    SftpRxJava sftpRxJava = new SftpRxJava(FileTransType.FIRMWARE,location);
//                                    sftpRxJava.downloadTask();
                                    //////////////////////////////////////////////////////////////////
                                } else if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.Downloaded)) {
                                    //installing reboot
                                    chargerConfiguration.setFirmwareStatus(FirmwareStatus.Installing);
                                    FirmwareStatusNotificationRequest firmwareStatusNotificationRequest = new FirmwareStatusNotificationRequest(chargerConfiguration.getFirmwareStatus());
                                    onSend(100, firmwareStatusNotificationRequest.getActionName(), firmwareStatusNotificationRequest);
                                } else if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.Installing)) {
                                    // FirmwareStatusNotification file create
                                    String fileName = "FirmwareStatusNotification";
                                    boolean check = fileManagement.fileCreate(fileName, "Firmware");
                                    ((MainActivity) MainActivity.mContext).onRebooting("Hard");

                                } else if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.Installed)) {
                                    // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = true ==> Unavailable
                                    for (int i = 0; i < GlobalVariables.maxPlugCount; i++)
                                        GlobalVariables.ChargerOperation[i] = true;
                                    onChargerOperateSave();
                                    chargerConfiguration.setFirmwareStatus(FirmwareStatus.Idle);
                                } else if (Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.DownloadFailed) ||
                                        Objects.equals(chargerConfiguration.getFirmwareStatus(), FirmwareStatus.InstallationFailed)) {
                                    // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = false ==> Unavailable
                                    for (int i = 0; i < GlobalVariables.maxPlugCount; i++)
                                        GlobalVariables.ChargerOperation[i] = true;
                                    onChargerOperateSave();
                                    // Status Notification
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_BOOT,
                                            100,
                                            0,
                                            null,
                                            null,
                                            null,
                                            false));
                                } else if (Objects.equals("LogStatusNotification", actionName)) {
                                    // not response
                                }
                            } else if (Objects.equals("SignedFirmwareStatusNotification", actionName)) {
                                if (Objects.equals(chargerConfiguration.getSignedFirmwareStatus(), SignedFirmwareStatus.Downloaded)) {

                                    //   Downloaded
                                    try {
                                        X509Certificate cert = loadCertificate(GlobalVariables.getSigningCertificate());
                                        PublicKey publicKey = cert.getPublicKey();

                                        byte[] signature = Base64.decode(GlobalVariables.getSignature(), Base64.DEFAULT);

                                        String fwFileName = GlobalVariables.getFirmwareFileName();
                                        if (fwFileName == null || fwFileName.isEmpty()) fwFileName = "DEVW007.apk";
                                        String LOCAL_PATH = GlobalVariables.getRootPath() + File.separator + fwFileName;
                                        boolean isVerified = verifySignature(publicKey, signature, LOCAL_PATH);

                                        //downlaod 완료 후.......
                                        chargerConfiguration.setSignedFirmwareStatus(isVerified ? SignedFirmwareStatus.SignatureVerified : SignedFirmwareStatus.InvalidSignature);
                                        SignedFirmwareStatusNotificationRequest signedFirmwareStatusNotificationRequest =
                                                new SignedFirmwareStatusNotificationRequest(chargerConfiguration.getSignedFirmwareStatus());
                                        signedFirmwareStatusNotificationRequest.setRequestId(GlobalVariables.getRequestId());
                                        onSend(100, signedFirmwareStatusNotificationRequest.getActionName(), signedFirmwareStatusNotificationRequest);
                                        onChargerOperateSave();
                                        for (int i = 0; i < GlobalVariables.maxPlugCount; i++)
                                            GlobalVariables.ChargerOperation[i] = true;

                                        if (!isVerified) {
                                            ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime();
                                            SecurityEventNotificationRequest securityEventNotificationRequest = new SecurityEventNotificationRequest("InvalidFirmwareSignature", timestamp);
                                            onSend(100, securityEventNotificationRequest.getActionName(), securityEventNotificationRequest);
                                        }
                                    } catch (Exception e) {
                                        logger.error(e.getMessage());
                                    }
                                } else if (Objects.equals(chargerConfiguration.getSignedFirmwareStatus(), SignedFirmwareStatus.SignatureVerified)) {
                                    // installing reboot
                                    chargerConfiguration.setSignedFirmwareStatus(SignedFirmwareStatus.Installing);
                                    SignedFirmwareStatusNotificationRequest signedFirmwareStatusNotificationRequest =
                                            new SignedFirmwareStatusNotificationRequest(chargerConfiguration.getSignedFirmwareStatus());
                                    signedFirmwareStatusNotificationRequest.setRequestId(GlobalVariables.getRequestId());
                                    onSend(100, signedFirmwareStatusNotificationRequest.getActionName(), signedFirmwareStatusNotificationRequest);
                                } else if (Objects.equals(chargerConfiguration.getSignedFirmwareStatus(), SignedFirmwareStatus.Installing)) {
                                    // FirmwareStatusNotification file create
                                    String fileName = "FirmwareStatusNotification";
                                    boolean check = fileManagement.fileCreate(fileName, "SignedFirmware-Installed");
                                    // request Id save
                                    fileName = "SignedRequestId";
                                    check = fileManagement.fileCreate(fileName,  String.valueOf(GlobalVariables.getRequestId()));
                                    // reboot 후 STATUS_NOTIFICATION_BOOT가 Available을 전송하도록 ChargerOperation = true 로 저장
                                    for (int i = 0; i < GlobalVariables.maxPlugCount; i++)
                                        GlobalVariables.ChargerOperation[i] = true;
                                    onChargerOperateSave();
                                    ((MainActivity) MainActivity.mContext).onRebooting("Hard");

                                } else if (Objects.equals(chargerConfiguration.getSignedFirmwareStatus(), SignedFirmwareStatus.Installed)) {
                                    for (int i = 0; i < GlobalVariables.maxPlugCount; i++)
                                        GlobalVariables.ChargerOperation[i] = true;
                                    onChargerOperateSave();
                                    chargerConfiguration.setSignedFirmwareStatus(SignedFirmwareStatus.Idle);
                                    // Installed 후 Available StatusNotification 전송
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_BOOT,
                                            100,
                                            0,
                                            null,
                                            null,
                                            null,
                                            false));
                                } else if (Objects.equals(chargerConfiguration.getSignedFirmwareStatus(), SignedFirmwareStatus.DownloadFailed) ||
                                        Objects.equals(chargerConfiguration.getSignedFirmwareStatus(), SignedFirmwareStatus.InstallationFailed)) {
                                    // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = false ==> Unavailable
                                    for (int i = 0; i < GlobalVariables.maxPlugCount; i++)
                                        GlobalVariables.ChargerOperation[i] = true;
                                    onChargerOperateSave();
                                    // Status Notification ???
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_BOOT,
                                            100,
                                            0,
                                            null,
                                            null,
                                            null,
                                            false));
                                }
                            } else if (Objects.equals("DiagnosticsStatusNotification", actionName)) {
                                if (chargerConfiguration.getDiagnosticsStatus() == DiagnosticsStatus.Uploaded) {
                                    chargerConfiguration.setDiagnosticsStatus(DiagnosticsStatus.Idle);
                                }
                            }
                            //central system response 받아 hashMapUid 삭제 StatusNotification
                            if (!TextUtils.isEmpty(actionName)) {
                                newHashMapUuid.remove(message.getId());
                            }
                            break;
                        case 2:
                            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
//                            chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getChargingCurrentData();
                            if (Objects.equals("Reset", actionName)) {
                                ResetType type;
                                try {
                                    type = ResetType.valueOf(jsonObject.getString("type"));
                                } catch (Exception e) {
                                    logger.error("Reset invalid type: {}", e.getMessage());
                                    onResultSend(actionName, message.getId(), new ResetConfirmation(ResetStatus.Rejected));
                                    return;
                                }
                                ResetConfirmation resetConfirmation = new ResetConfirmation(ResetStatus.Accepted);
                                onResultSend(actionName, message.getId(), resetConfirmation);

                                // charging status check
                                boolean isCharging = false;
                                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                                    // accept
                                    uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(i).getUiSeq();
                                    if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                                        isCharging = true;
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(i).onResetStop(i, type);
                                    }
                                    if (Objects.equals(type, ResetType.Hard)) {
                                        ((MainActivity) MainActivity.mContext).getChargingCurrentData(i).setStopReason(Reason.HardReset);
                                        ((MainActivity) MainActivity.mContext).getChargingCurrentData(i).setReBoot(true);
                                    } else if (Objects.equals(type, ResetType.Soft)) {
                                        ((MainActivity) MainActivity.mContext).getChargingCurrentData(i).setStopReason(Reason.SoftReset);
                                        ((MainActivity) MainActivity.mContext).getChargingCurrentData(i).setReBoot(true);
                                    }
                                }
                                final String resetTypeStr = Objects.equals(type, ResetType.Hard) ? "Hard" : "Soft";
                                if (!isCharging) {
                                    // 비충전 중: 3초 후 즉시 재부팅
                                    new Handler(Looper.getMainLooper()).postDelayed(() ->
                                            ((MainActivity) MainActivity.mContext).onRebooting(resetTypeStr), 3000);
                                } else {
                                    // 충전 중: StopTransaction + StatusNotification Finishing 전송 완료 대기 후 재부팅
                                    // 2초 타이머 기준: csStop(2s) + FINISH(2s) + 여유 = 10s
                                    new Handler(Looper.getMainLooper()).postDelayed(() ->
                                            ((MainActivity) MainActivity.mContext).onRebooting(resetTypeStr), 10000);
                                }
                            } else if (Objects.equals("RemoteStartTransaction", actionName)) {
                                // connectorId ==> 채널 정보

                                int realConnectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
                                chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess(realConnectorId-1).getChargingCurrentData();
                                // ReserveNow로 저장된 예약 데이터는 onCurrentDataClear 후에도 보존해야
                                // StartTransaction 시 reservationId를 포함할 수 있음 (OCPP 1.6 TC_046_2)
                                String savedResIdTag = chargingCurrentData.getResIdTag();
                                String savedResParentIdTag = chargingCurrentData.getResParentIdTag();
                                String savedResReservationId = chargingCurrentData.getResReservationId();
                                int savedResConnectorId = chargingCurrentData.getResConnectorId();
                                String savedResExpiryDate = chargingCurrentData.getResExpiryDate();
                                chargingCurrentData.onCurrentDataClear();
                                chargingCurrentData.setResIdTag(savedResIdTag);
                                chargingCurrentData.setResParentIdTag(savedResParentIdTag);
                                chargingCurrentData.setResReservationId(savedResReservationId);
                                chargingCurrentData.setResConnectorId(savedResConnectorId);
                                chargingCurrentData.setResExpiryDate(savedResExpiryDate);
                                chargingCurrentData.setConnectorId(realConnectorId);
                                String idTag = jsonObject.has("idTag") ? jsonObject.getString("idTag") : "";
                                chargingCurrentData.setIdTag(idTag);
                                chargingCurrentData.setPaymentType(PaymentType.MEMBER);
                                chargingCurrentData.setRemoteStart(true);
                                uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(realConnectorId-1).getUiSeq();
                                //ChargingProfile
                                ChargingProfilePurposeType purposeType = ChargingProfilePurposeType.TxProfile;
                                JSONObject chargingProfile;
                                try {
                                    chargingProfile = jsonObject.has("chargingProfile") ? jsonObject.getJSONObject("chargingProfile") : null;
                                } catch (Exception e) {
                                    logger.error(e.getMessage());
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_REMOTE_START_TRANSACTION,
                                            realConnectorId,
                                            0,
                                            idTag,
                                            message.getId(),
                                            null,
                                            false));
                                    return;
                                }
                                if (chargingProfile != null) {
                                    purposeType = ChargingProfilePurposeType.valueOf(chargingProfile.getString("chargingProfilePurpose"));
                                    if (purposeType == ChargingProfilePurposeType.TxProfile) {
                                        chargingCurrentData.setRemoteStartSmartCharging(true);
                                        ///저장 remoteStartTransaction chargingProfile 저장
                                        onUpdateChargingProfile(jsonObject);
                                    } else {
                                        chargingCurrentData.setRemoteStartSmartCharging(false);
                                    }
                                }

                                // 충전중 또는 connectorId == 0 이면 reject
                                if (uiSeq == UiSeq.CHARGING || realConnectorId == 0) {
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_REMOTE_START_TRANSACTION,
                                            realConnectorId,
                                            0,
                                            idTag,
                                            message.getId(),
                                            null,
                                            false));
                                } else {
                                    // remoteStart 응답
                                    boolean startCheck = !jsonObject.has("chargingProfile") || ChargingProfilePurposeType.TxProfile == purposeType;
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_REMOTE_START_TRANSACTION,
                                            realConnectorId,
                                            0,
                                            idTag,
                                            message.getId(),
                                            null,
                                            startCheck));
                                    // reject 인 경우 retrun
                                    if (!startCheck) return;

                                    // 2026.02.09 추가
                                    String[] idTagInfo = getLocalAuthorizationListStrings(uiSeq == UiSeq.CHARGING ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag());
                                    boolean localFind =  (Objects.equals(idTagInfo[0], chargingCurrentData.getIdTag()));
                                    //Authoriza Send
                                    if (Objects.equals(socket.getState(), SocketState.OPEN) && GlobalVariables.isAuthorizeRemoteTxRequests()) {
                                        if (localFind && GlobalVariables.LocalAuthListEnabled && GlobalVariables.isLocalPreAuthorize()) {
                                            // 생략
                                            chargingCurrentData.setAuthorizeResult(true);
                                            chargingCurrentData.setParentIdTag(idTagInfo[1]);
                                            //preparing
                                            if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                                    Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                                                chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                                                processHandler.sendMessage(onMakeHandlerMessage(
                                                        GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                        chargingCurrentData.getConnectorId(),
                                                        0,
                                                        null,
                                                        null,
                                                        null,
                                                        false));
                                            }
                                            ((MainActivity) MainActivity.mContext).getClassUiProcess(realConnectorId - 1).setUiSeq(UiSeq.PLUG_CHECK);
                                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(realConnectorId - 1, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                                        } else {
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
                                                    realConnectorId,
                                                    0,
                                                    idTag,
                                                    null,
                                                    null,
                                                    false));
                                            // StatusNoti(Preparing)은 Authorize.conf(Accepted) 수신 후 전송 (TC: Authorize → Accept → StatusNoti 순서)
                                        }
                                    } else if (!GlobalVariables.isAuthorizeRemoteTxRequests()) {
                                        // AuthorizeRemoteTxRequests=false: Authorize 없이 바로 Preparing + PLUG_CHECK
                                        chargingCurrentData.setAuthorizeResult(true);
                                        if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing)
                                                && Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                                            chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                    chargingCurrentData.getConnectorId(),
                                                    0,
                                                    null,
                                                    null,
                                                    null,
                                                    false));
                                        }
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).setUiSeq(UiSeq.PLUG_CHECK);
                                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(channel, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                                    }
                                }
                            } else if (Objects.equals("RemoteStopTransaction", actionName)) {
                                try {
                                    boolean result = false;
                                    int remoteCh = 0;
                                    int transactionId = jsonObject.has("transactionId") ? jsonObject.getInt("transactionId") : 0;

                                    for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                                        chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess(i).getChargingCurrentData();
                                        uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(i).getUiSeq();
                                        if (chargingCurrentData.getTransactionId() == transactionId && Objects.equals(uiSeq, UiSeq.CHARGING)) {
                                            result = true;
                                            remoteCh = i;
                                            break;
                                        }
                                    }

                                    RemoteStartStopStatus remoteStartStopStatus = result ? RemoteStartStopStatus.Accepted : RemoteStartStopStatus.Rejected;
                                    RemoteStopTransactionConfirmation remoteStopTransactionConfirmation = new RemoteStopTransactionConfirmation(remoteStartStopStatus);
                                    onResultSend(remoteStopTransactionConfirmation.getActionName(), message.getId(), remoteStopTransactionConfirmation);

                                    if (result) {
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(remoteCh).onRemoteTransactionStop(remoteCh,Reason.Remote);
                                        ((MainActivity) MainActivity.mContext).getClassUiProcess(remoteCh).setUiSeq(UiSeq.FINISH_WAIT);
                                        //hash map delete
                                        getConnectorIdHashMap.remove(transactionId);
                                    }
                                } catch (Exception e) {
                                    logger.error("RemoteStopTransaction receive error : {}", e.getMessage(), e);
                                }
                            } else if (Objects.equals("DataTransfer", actionName)) {
                                //* dataTransfer-(messageId) */
                                if (jsonObject.has("messageId")) {
                                    if (Objects.equals(jsonObject.getString("messageId"), "announce")) {
                                        String vendorId = jsonObject.has("vendorId") ? jsonObject.getString("vendorId") : null;
                                        JSONObject jsonObjectDataAnnounce = new JSONObject(jsonObject.getString("data"));
                                        fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "announce", jsonObjectDataAnnounce.toString(), false);
                                        processHandler.sendMessage(onMakeHandlerMessage(
                                                GlobalVariables.MESSAGE_HANDLER_ANNOUNCE,
                                                100,
                                                0,
                                                null,
                                                message.getId(),
                                                null,
                                                true));
                                    } else {
                                        DataTransferConfirmation dataTransferConfirmation = new DataTransferConfirmation(DataTransferStatus.Rejected);
                                        onResultSend(actionName, message.getId(), dataTransferConfirmation);
                                    }
                                } else {
                                    DataTransferConfirmation dataTransferConfirmation = new DataTransferConfirmation(DataTransferStatus.Rejected);
                                    onResultSend(actionName, message.getId(), dataTransferConfirmation);
                                }
                            } else if (Objects.equals("TriggerMessage", actionName)) {
                                int triggerConnectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : 0;
                                // 유효 범위: connectorId=0 ~ maxPlugCount-1 (2채널 충전기: 0,1,2 유효)
                                boolean validConnector = triggerConnectorId >= 0 && triggerConnectorId <= GlobalVariables.maxPlugCount - 1;
                                TriggerMessageRequestType triggerMessageRequestType;
                                try {
                                    triggerMessageRequestType = TriggerMessageRequestType.valueOf(jsonObject.getString("requestedMessage"));
                                } catch (IllegalArgumentException e) {
                                    logger.error("TriggerMessage unknown requestedMessage: {}", e.getMessage());
                                    onResultSend(actionName, message.getId(), new TriggerMessageConfirmation(TriggerMessageStatus.NotImplemented));
                                    return;
                                }
                                TriggerMessageStatus status = validConnector ? TriggerMessageStatus.Accepted : TriggerMessageStatus.Rejected;
                                TriggerMessageConfirmation triggerMessageConfirmation = new TriggerMessageConfirmation(status);
                                onResultSend(actionName, message.getId(), triggerMessageConfirmation);

                                //TriggerMessageRequestType 별 응답
                                if (Objects.equals(status, TriggerMessageStatus.Rejected)) return;
                                if (Objects.equals(triggerMessageRequestType, TriggerMessageRequestType.MeterValues) && triggerConnectorId >= 1) {
                                    processHandler.onMeterValueSendOne(triggerConnectorId - 1);
                                } else if (Objects.equals(triggerMessageRequestType, TriggerMessageRequestType.Heartbeat)) {
                                    HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
                                    onSend(heartbeatRequest.getActionName(), heartbeatRequest);
                                } else if (Objects.equals(triggerMessageRequestType, TriggerMessageRequestType.StatusNotification)) {
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                            triggerConnectorId,
                                            0,
                                            null,
                                            null,
                                            null,
                                            false));
                                } else if (Objects.equals(triggerMessageRequestType, TriggerMessageRequestType.DiagnosticsStatusNotification)) {
                                    diagnosticsStatusNotificationRequest =
                                            new DiagnosticsStatusNotificationRequest(chargerConfiguration.getDiagnosticsStatus());
                                    onSend(100, diagnosticsStatusNotificationRequest.getActionName(), diagnosticsStatusNotificationRequest);

                                } else if (Objects.equals(triggerMessageRequestType, TriggerMessageRequestType.FirmwareStatusNotification)) {
                                    FirmwareStatusNotificationRequest firmwareStatusNotificationRequest = new FirmwareStatusNotificationRequest(FirmwareStatus.Idle);
                                    onSend(100, firmwareStatusNotificationRequest.getActionName(), firmwareStatusNotificationRequest);
                                } else if (Objects.equals(triggerMessageRequestType, TriggerMessageRequestType.BootNotification)) {
                                    GlobalVariables.setTriggerBootNotification(true);
                                    BootNotificationRequest bootNotificationRequest = new BootNotificationRequest(
                                            chargerConfiguration.getChargerPointVendor(),
                                            chargerConfiguration.getChargerPointModel());
                                    bootNotificationRequest.setFirmwareVersion(GlobalVariables.VERSION);
                                    bootNotificationRequest.setImsi(chargerConfiguration.getImsi());
                                    bootNotificationRequest.setChargePointSerialNumber(chargerConfiguration.getChargerId());
                                    onSend(100, bootNotificationRequest.getActionName(), bootNotificationRequest);
                                }


                                //// original 소스
//                                int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
//                                TriggerMessageRequestType triggerMessageRequestType = TriggerMessageRequestType.valueOf(jsonObject.getString("requestedMessage"));
//                                processHandler.sendMessage(onMakeHandlerMessage(
//                                        GlobalVariables.MESSAGE_HANDLER_TRIGGER_MESSAGE,
//                                        connectorId,
//                                        0,
//                                        null,
//                                        message.getId(),
//                                        null,
//                                        connectorId >= 1));
//                                if (Objects.equals(triggerMessageRequestType, TriggerMessageRequestType.MeterValues)) {
//                                    //Meter Value response
//                                    processHandler.onMeterValueSendOne(connectorId);
//                                }
                            } else if (Objects.equals("ChangeConfiguration", actionName)) {
                                boolean result;
                                GlobalVariables.setNotSupportedKey(false);
                                String key = jsonObject.has("key") ? jsonObject.getString("key") : "";
                                String value = jsonObject.has("value") ? jsonObject.getString("value") : "";
                                //valid check

                                if (Objects.equals(key, "MeterValueSampleInterval") && Integer.parseInt(value) == -1) {
                                    result = false;
                                }  else if (Objects.equals(key, "SecurityProfile")) {
                                    result = Integer.parseInt(GlobalVariables.getSecurityProfile()) <= Integer.parseInt(value);
                                    if (result) {
                                        setConfigurationValue(key, value);
                                        // config 파일도 업데이트하여 reboot 후에도 일관성 유지
                                        chargerConfiguration.setSecurityProfile(value);
                                        chargerConfiguration.onSaveConfiguration();
                                        GlobalVariables.setSecurityProfile(value);
                                    }
                                } else {
                                    result = setConfigurationValue(key, value);
//                                    if (Objects.equals(key, "ClockAlignedDataInterval") && Integer.parseInt(value) == 0 ) {
//                                        ((MainActivity) MainActivity.mContext).getClassUiProcess().onMeterValuesAlignedDataStop();
//                                    } else if (Objects.equals(key, "MeterValueSampleInterval") && Integer.parseInt(value) == 0) {
//                                        ((MainActivity) MainActivity.mContext).getClassUiProcess().onMeterValueStop();
//                                    }
                                }

                                final String changeConfigUuid = message.getId();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_CHANGE_CONFIGURATION,
                                            100,
                                            1,
                                            key,                //AuthorizationKey
                                            changeConfigUuid,
                                            value,              //4F43415F4F4354545F61646D696E5F74657374
                                            result));
                                }, 200);

                                if (result) ((MainActivity) MainActivity.mContext).getConfigurationKeyRead().onRead();
                            } else if (Objects.equals("ChangeAvailability", actionName)) {
                                int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
                                AvailabilityType type;
                                try {
                                    type = AvailabilityType.valueOf(jsonObject.getString("type"));
                                } catch (IllegalArgumentException e) {
                                    logger.error("ChangeAvailability invalid type: {}", e.getMessage());
                                    return;
                                }
                                //change availability response
                                boolean checkType = type == AvailabilityType.Operative;
                                final String changeAvailUuid = message.getId();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_CHANGE_AVAILABILITY,
                                            connectorId,
                                            1,
                                            null,
                                            changeAvailUuid,
                                            null,
                                            true));
                                }, 200);
                                switch (connectorId) {
                                    case 0:
                                        for (int i = 0; i < GlobalVariables.maxPlugCount; i++) {
                                            if (!Objects.equals(GlobalVariables.ChargerOperation[i], checkType)) {
                                                //send
                                                GlobalVariables.ChargerOperation[i] = checkType;
                                            }
                                        }
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_CHANGE_AVAILABILITY,
                                                    connectorId,
                                                    1,
                                                    null,
                                                    null,
                                                    null,
                                                    false));
                                        }, 200);
                                        break;
                                    case 1:
                                    case 2:
                                        //
                                        GlobalVariables.ChargerOperation[connectorId] = checkType;
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            processHandler.sendMessage(onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_CHANGE_AVAILABILITY,
                                                    connectorId,
                                                    1,
                                                    null,
                                                    null,
                                                    null,
                                                    checkType));

                                        },200);
                                        break;
                                }
                                //저장을 한다,
                                onChargerOperateSave();
                            } else if (Objects.equals("GetConfiguration", actionName)) {
                                try {
                                    GlobalVariables.setConfigurationKey(jsonObject.has("key") ? jsonObject.getString("key").replaceAll("[^a-zA-Z0-9_]", "") : "");
                                    final String getConfigUuid = message.getId();
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        processHandler.sendMessage(onMakeHandlerMessage(
                                                GlobalVariables.MESSAGE_GET_CONFIGURATION,
                                                0,
                                                1,
                                                null,
                                                getConfigUuid,
                                                null,
                                                false));
                                    }, 200);
                                } catch (Exception e) {
                                    logger.error("GetConfiguration error : {}", e.getMessage());
                                }
                            } else if (Objects.equals("ClearCache", actionName)) {
                                try {
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_CLEAR_CACHE,
                                            0,
                                            1,
                                            null,
                                            message.getId(),
                                            null,
                                            false));
                                } catch (Exception e) {
                                    logger.error("ClearCache error : {}", e.getMessage());
                                }
                            } else if (Objects.equals("SendLocalList", actionName)) {
                                try {
                                    boolean status = false, authSupported;
                                    //configuration key SupportedFeatureProfiles check
                                    authSupported = onSupportedFeatureProfiles("LocalAuthListManagement");
                                    if (authSupported) {
                                        String resultValue = "none";
                                        //서버 에서 받은 데이터
                                        int newListVersion = jsonObject.getInt("listVersion");
                                        UpdateType updateType = UpdateType.valueOf(jsonObject.getString("updateType"));
                                        //configurationKey get list version

                                        //localAuthorizationList file not found
                                        File file = new File(GlobalVariables.getRootPath() + File.separator + "localAuthorizationList");
                                        if (!file.exists() || !jsonObject.has("localAuthorizationList")) {
                                            fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "localAuthorizationList", message.getPayload().toString(), false);
                                            GlobalVariables.updateStatus = UpdateStatus.Accepted;
                                        } else {
                                            String oldData = fileManagement.getStringFromFile(
                                                    GlobalVariables.getRootPath() + File.separator + "localAuthorizationList");
                                            if (oldData == null || oldData.trim().isEmpty()) {
                                                // 파일은 있는데 내용이 없음 → 새로 저장
                                                fileManagement.stringToFileSave(
                                                        GlobalVariables.getRootPath(),
                                                        "localAuthorizationList",
                                                        message.getPayload().toString(),
                                                        false
                                                );
                                                GlobalVariables.updateStatus = UpdateStatus.Accepted;
                                            }
                                            //old authorization list
                                            JSONObject oldLocalAuthorizationList = new JSONObject(fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + "localAuthorizationList"));

                                            // 현재 localAuthorizationList list 버전
                                            int fileListVersion = oldLocalAuthorizationList.getInt("listVersion");
                                            //configurationKey localAuthorizationList check
                                            resultValue = getConfigurationValue("LocalAuthListEnabled");

                                            GlobalVariables.updateStatus = Objects.equals(resultValue, "none") || Objects.equals(resultValue, "false") ? UpdateStatus.NotSupported : UpdateStatus.Accepted;

                                            //Accepted, Failed, Not supported, versionMismatch
                                            //서버로 부터 받은 세로운 리스트
                                            JSONArray newList = jsonObject.getJSONArray("localAuthorizationList");
                                            if (Objects.equals(GlobalVariables.updateStatus, UpdateStatus.Accepted)) {
                                                if (Objects.equals(UpdateType.Full, updateType)) {
                                                    if (newList.length() == 0) {
                                                        String localList = "{\"listVersion\":0,\"localAuthorizationList\":[]}";
                                                        status = fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "localAuthorizationList", localList, false);
                                                    } else {
                                                        status = fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "localAuthorizationList", jsonObject.toString(), false);
                                                    }
                                                    GlobalVariables.updateStatus = status ? UpdateStatus.Accepted : UpdateStatus.Failed;
                                                } else if (Objects.equals(UpdateType.Differential, updateType)) {
                                                    //부분변경
                                                    try {
                                                        if (fileListVersion < newListVersion) {
                                                            oldLocalAuthorizationList.put("listVersion", jsonObject.getInt("listVersion"));
                                                            oldLocalAuthorizationList.put("updateType", jsonObject.getString("updateType"));
                                                            //localAuthorizationList 병합
                                                            JSONArray oldList = oldLocalAuthorizationList.getJSONArray("localAuthorizationList");
                                                            for (int i = 0; i < newList.length(); i++) {
                                                                oldList.put(newList.getString(i));
                                                            }
                                                            status = fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "localAuthorizationList", oldLocalAuthorizationList.toString(), false);
                                                            GlobalVariables.updateStatus = status ? UpdateStatus.Accepted : UpdateStatus.Failed;
                                                        } else {
                                                            GlobalVariables.updateStatus = UpdateStatus.VersionMismatch;
                                                        }
                                                    } catch (Exception e) {
                                                        logger.error("{}", e.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        GlobalVariables.updateStatus = UpdateStatus.NotSupported;
                                    }
                                    final String sendLocalListUuid = message.getId();
                                    final String sendLocalListAction = actionName;
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        //send
                                        SendLocalListConfirmation sendLocalListConfirmation = new SendLocalListConfirmation(GlobalVariables.updateStatus);
                                        try {
                                            onResultSend(sendLocalListAction, sendLocalListUuid, sendLocalListConfirmation);
                                        } catch (OccurenceConstraintException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }, 200);
                                } catch (Exception e) {
                                    logger.error("SendLocalList error : {}", e.getMessage());
                                }
                            } else if (Objects.equals("ClearChargingProfile", actionName)) {
                                try {
                                    int id = -1, stackLevel = -1;
                                    id = jsonObject.has("id") ? jsonObject.getInt("id") : -1;
                                    int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
                                    ChargingProfilePurposeType chargingProfilePurposeType = jsonObject.has("chargingProfilePurpose") ?
                                            ChargingProfilePurposeType.valueOf(jsonObject.getString("chargingProfilePurpose")) : null;

                                    stackLevel = jsonObject.has("stackLevel") ? jsonObject.getInt("stackLevel") : -1;

                                    //response
                                    boolean result = onClearChargingProfile(id, stackLevel, chargingProfilePurposeType);

                                    ClearChargingProfileConfirmation clearChargingProfileConfirmation =
                                            new ClearChargingProfileConfirmation(result ? ClearChargingProfileStatus.Accepted : ClearChargingProfileStatus.Unknown);
                                    onResultSend(actionName, message.getId(), clearChargingProfileConfirmation);

                                } catch (Exception e) {
                                    logger.error("ClearChargingProfile error : {}", e.getMessage());
                                }
                            } else if (Objects.equals("UnlockConnector", actionName)) {
                                UnlockConnectorConfirmation unlockConnectorConfirmation = new UnlockConnectorConfirmation(UnlockStatus.NotSupported);
                                onResultSend(actionName, message.getId(), unlockConnectorConfirmation);
                                //Unlock Connector 지원할 경우 (2024.12.19)
//                                uiSeq = ((MainActivity)MainActivity.mContext).getClassUiProcess(getChannel()).getUiSeq();
//                                if (uiSeq == UiSeq.CHARGING) {
//                                    unlockConnectorConfirmation.setStatus(UnlockStatus.Unlocked);
//                                    ////
//                                    ((MainActivity) MainActivity.mContext).getClassUiProcess(getChannel()).onRemoteTransactionStop(Reason.Remote);
//
//                                } else {
//                                    onResultSend(actionName, message.getId(), unlockConnectorConfirmation);
//                                }

                            } else if (Objects.equals("GetLocalListVersion", actionName)) {
                                //Local List
                                int localListVersion, listVersion;
                                File file = new File(GlobalVariables.getRootPath() + File.separator + "localAuthorizationList");
                                if (!file.exists()) {
                                    listVersion = 0;
                                } else {
                                    JSONObject jsonLocalAuthorizationList = new JSONObject(fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + "localAuthorizationList"));
                                    JSONArray localAuthList;
                                    try {
                                        localAuthList = jsonLocalAuthorizationList.getJSONArray("localAuthorizationList");
                                        localListVersion = jsonLocalAuthorizationList.has("listVersion") ? jsonLocalAuthorizationList.getInt("listVersion") : 0;
                                        listVersion = !GlobalVariables.isLocalAuthListEnabled() ?  -1 : localAuthList.length() == 0 ? 0 : localListVersion;
                                    } catch (Exception e) {
                                        listVersion = 0;
                                    }
                                }
                                GetLocalListVersionConfirmation getLocalListVersionConfirmation = new GetLocalListVersionConfirmation(listVersion);
                                onResultSend(actionName, message.getId(), getLocalListVersionConfirmation);
                            } else if (Objects.equals("UpdateFirmware", actionName)) {
                                String location = jsonObject.has("location") ? jsonObject.getString("location") : "";
                                int retries = jsonObject.has("retries") ? jsonObject.getInt("retries") : 1;
                                //response 즉시 send
                                UpdateFirmwareConfirmation updateFirmwareConfirmation = new UpdateFirmwareConfirmation();
                                onResultSend(actionName, message.getId(), updateFirmwareConfirmation);


                                // 1. firmware status : Downloading
                                FirmwareStatusNotificationRequest firmwareStatusNotificationRequest = new FirmwareStatusNotificationRequest(FirmwareStatus.Downloading);
                                chargerConfiguration.setFirmwareStatus(FirmwareStatus.Downloading);
                                onSend(100, firmwareStatusNotificationRequest.getActionName(), firmwareStatusNotificationRequest);

                                // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = false ==> Unavailable
                                for (int i = 0; i < GlobalVariables.maxPlugCount; i++)
                                    GlobalVariables.ChargerOperation[i] = false;
                                onChargerOperateSave();


                                // Status Notification
                                processHandler.sendMessage(onMakeHandlerMessage(
                                        GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_BOOT,
                                        100,
                                        0,
                                        null,
                                        null,
                                        null,
                                        false));
                                // 나중에 삭제 해야 함.
//                                location = "211.44.234.112";1
//                                location = "192.168.30.120";

//                                SftpRxJava sftpRxJava = new SftpRxJava(FileTransType.FIRMWARE, location);
//                                sftpRxJava.downloadTask();

                                /** FTP*/
                                FtpRxJava ftpRxJava = new FtpRxJava(FileTransType.FIRMWARE, location);
                                ftpRxJava.downloadTask();


                            } else if (Objects.equals("GetDiagnostics", actionName)) {
                                String location = jsonObject.has("location") ? jsonObject.getString("location") : "";
                                int retries = jsonObject.has("retries") ? jsonObject.getInt("retries") : -1;
                                int retryInterval = jsonObject.has("retryInterval") ? jsonObject.getInt("retryInterval") : -1;
                                String startTime = jsonObject.has("startTime") ? jsonObject.getString("startTime") : "1999-02-01";
                                String stopTime = jsonObject.has("stopTime") ? jsonObject.getString("stopTime") : "1999-02-02";
                                // diagnostics file name return : response
                                GetDiagnosticsConfirmation getDiagnosticsConfirmation = new GetDiagnosticsConfirmation("diagnostics");
                                onResultSend(actionName, message.getId(), getDiagnosticsConfirmation);

                                // DiagnosticsStatus uploading
                                chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
                                chargerConfiguration.setDiagnosticsStatus(DiagnosticsStatus.Uploading);
                                diagnosticsStatusNotificationRequest = new DiagnosticsStatusNotificationRequest(DiagnosticsStatus.Uploading);
                                onSend(100, diagnosticsStatusNotificationRequest.getActionName(), diagnosticsStatusNotificationRequest);

                                //Diagnostics file make & Sftp 연결
                                boolean fileMakeCheck = onDiagnosticsFileMake(startTime, stopTime, location);
                            } else if (Objects.equals("ReserveNow", actionName)) {
                                ReservationStatus reservationStatus;
                                int resConnectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : 0;
                                String resExpiryDate = jsonObject.has("expiryDate") ? jsonObject.getString("expiryDate") : "";
                                String resIdTag = jsonObject.has("idTag") ? jsonObject.getString("idTag") : "";
                                String resParentIdTag = jsonObject.has("parentIdTag") ? jsonObject.getString("parentIdTag") : "";
                                String resReservationId = jsonObject.has("reservationId") ? jsonObject.getString("reservationId") : "";
                                boolean faultedCase = false, occupiedCase = false, unavailableCase = false;
                                if (GlobalVariables.isReserveConnectorZeroSupported() && resConnectorId == 0) {
                                    RxData rxData0 = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData(channel);
                                    faultedCase = rxData0.isCsFault();
                                    occupiedCase = ((MainActivity) MainActivity.mContext).getClassUiProcess(channel).getChargingCurrentData().getChargePointStatus() == ChargePointStatus.Available;

                                    unavailableCase = GlobalVariables.ChargerOperation[1];
                                } else if (resConnectorId > 0) {
                                    int resChannel = resConnectorId - 1;
                                    chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess(resChannel).getChargingCurrentData();
                                    RxData rxData = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData(resChannel);
                                    faultedCase = rxData.isCsFault();
                                    // OCPP 1.6 스펙 Section 5.12: Available만 Accepted, 나머지는 Occupied
                                    // Preparing/Charging/SuspendedEV/SuspendedEVSE/Finishing/Reserved → Occupied
                                    ChargePointStatus resConnStatus = chargingCurrentData.getChargePointStatus();
                                    occupiedCase = chargingCurrentData.getReservedStatus() == ChargePointStatus.Reserved
                                            || resConnStatus == ChargePointStatus.Preparing
                                            || resConnStatus == ChargePointStatus.Charging
                                            || resConnStatus == ChargePointStatus.SuspendedEV
                                            || resConnStatus == ChargePointStatus.SuspendedEVSE
                                            || resConnStatus == ChargePointStatus.Finishing;
                                    unavailableCase = GlobalVariables.ChargerOperation[resConnectorId];
                                }

                                //configuration key SupportedFeatureProfiles check
                                boolean reserveSupported = onSupportedFeatureProfiles("Reservation") ;

                                reservationStatus = (!reserveSupported || resConnectorId == 0 ? ReservationStatus.Rejected : faultedCase ? ReservationStatus.Faulted :
                                        !unavailableCase ? ReservationStatus.Unavailable : occupiedCase ? ReservationStatus.Occupied :
                                                ReservationStatus.Accepted);
                                //reserve now response
                                ReserveNowConfirmation reserveNowConfirmation = new ReserveNowConfirmation(reservationStatus);
                                onResultSend(actionName, message.getId(), reserveNowConfirmation);

                                if (Objects.equals(reservationStatus, ReservationStatus.Accepted)) {
                                    chargingCurrentData.setResConnectorId(resConnectorId);
                                    chargingCurrentData.setResExpiryDate(resExpiryDate);
                                    chargingCurrentData.setResIdTag(resIdTag);
                                    chargingCurrentData.setResParentIdTag(resParentIdTag);
                                    chargingCurrentData.setResReservationId(resReservationId);
                                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Reserved);
                                    chargingCurrentData.setReservedStatus(ChargePointStatus.Reserved);
                                    // status notification : reserved
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                            resConnectorId,
                                            1,
                                            null,
                                            null,
                                            "RESERVATION",     //
                                            false));
                                }
                            } else if (Objects.equals("CancelReservation", actionName)) {
                                String resReservationId = jsonObject.has("reservationId") ? jsonObject.getString("reservationId") : "-1";
                                int cancelConnectorId = onFindConnectorId(resReservationId, true);
                                CancelReservationStatus cancelReservationStatus = cancelConnectorId > 0 ?
                                        CancelReservationStatus.Accepted : CancelReservationStatus.Rejected;
                                //cancelReservationConfirmation response
                                CancelReservationConfirmation cancelReservationConfirmation = new CancelReservationConfirmation(cancelReservationStatus);
                                onResultSend(actionName, message.getId(), cancelReservationConfirmation);
                                if (cancelReservationStatus == CancelReservationStatus.Accepted) {
                                    processHandler.sendMessage(onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                            cancelConnectorId,
                                            0,
                                            null,
                                            null,
                                            null,
                                            false));
                                }
                            } else if (Objects.equals("SetChargingProfile", actionName)) {
                                try {
                                    int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
                                    JSONObject chargingProfiles = jsonObject.getJSONObject("csChargingProfiles");
                                    int chargingProfileId = chargingProfiles.getInt("chargingProfileId");
                                    int transactionId = chargingProfiles.has("transactionId") ? chargingProfiles.getInt("transactionId") : -1;
                                    int stackLevel = chargingProfiles.has("stackLevel") ? chargingProfiles.getInt("stackLevel") : -1;

                                    ChargingProfilePurposeType chargingProfilePurpose = ChargingProfilePurposeType.valueOf(chargingProfiles.getString("chargingProfilePurpose"));

                                    JSONObject chargingSchedule = chargingProfiles.getJSONObject("chargingSchedule");
                                    int duration = chargingSchedule.has("duration") ? chargingSchedule.getInt("duration") : -1;
                                    GlobalVariables.setStartSchedule(chargingSchedule.has("startSchedule") ? chargingSchedule.getString("startSchedule") : "");
                                    ChargingRateUnitType chargingRateUnit = ChargingRateUnitType.valueOf(chargingSchedule.getString("chargingRateUnit"));
                                    long minChargingRate = chargingSchedule.has("minChargingRate") ? chargingSchedule.getLong("minChargingRate") : -1;

                                    compositeTime = zonedDateTimeConvert.doStringDateToDate(GlobalVariables.getStartSchedule());

                                    ChargingProfileKindType chargingProfileKind = ChargingProfileKindType.valueOf(chargingProfiles.getString("chargingProfileKind"));
                                    if (chargingProfiles.has("recurrencyKind")) {
                                        RecurrencyKindType recurrencyKind = RecurrencyKindType.valueOf(chargingProfiles.getString("recurrencyKind"));
                                    }

                                    String validFrom = chargingProfiles.has("validFrom") ? chargingProfiles.getString("validFrom") : "";
                                    String validTo = chargingProfiles.has("validTo") ? chargingProfiles.getString("validTo") : "";



                                    if (!Objects.equals(chargingProfilePurpose, ChargingProfilePurposeType.ChargePointMaxProfile)) {
                                        chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess(connectorId-1).getChargingCurrentData();
                                    }

                                    boolean result = true;
                                    if (chargingProfilePurpose == ChargingProfilePurposeType.TxProfile) {
                                        result = chargingCurrentData.getChargePointStatus() == ChargePointStatus.Charging && chargingCurrentData.getTransactionId() == transactionId;
                                    }

                                    //SetChargingProfile response ==> ChargingProfileStatus.Accepted
                                    SetChargingProfileConfirmation setChargingProfileConfirmation =
                                            new SetChargingProfileConfirmation(result ? ChargingProfileStatus.Accepted : ChargingProfileStatus.Rejected);
                                    onResultSend(actionName, message.getId(), setChargingProfileConfirmation);
                                    if (result) onUpdateChargingProfile(jsonObject);
                                } catch (Exception e) {
                                    logger.error(" SetChargingProfile error :  {}", e.getMessage());
                                    try {
                                        SetChargingProfileConfirmation rejected =
                                                new SetChargingProfileConfirmation(ChargingProfileStatus.Rejected);
                                        onResultSend(actionName, message.getId(), rejected);
                                    } catch (Exception ex) {
                                        logger.error("SetChargingProfile fallback response error: {}", ex.getMessage());
                                    }
                                }

                            } else if (Objects.equals("GetCompositeSchedule", actionName)) {
                                int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
                                int duration = jsonObject.has("duration") ? jsonObject.getInt("duration") : -1;
                                ChargingRateUnitType chargingRateUnit = jsonObject.has("chargingRateUnit") ? ChargingRateUnitType.valueOf(jsonObject.getString("chargingRateUnit")) : null;
                                // GetCompositeSchedule response
                                GetCompositeScheduleConfirmation getCompositeScheduleConfirmation = new GetCompositeScheduleConfirmation(GetCompositeScheduleStatus.Accepted);
                                getCompositeScheduleConfirmation.setConnectorId(connectorId);
                                getCompositeScheduleConfirmation.setScheduleStart(zonedDateTimeConvert.doZonedDateTimeToDatetime1());
                                JSONArray jsonArray = onChargingSchedulePeriodCurrentData(connectorId - 1, duration);

                                Gson gson = new Gson();
                                ChargingSchedulePeriod[] chargingSchedulePeriods;
                                ChargingSchedule chargingSchedule;
                                if (jsonArray != null) {
                                    chargingSchedulePeriods = gson.fromJson(jsonArray.toString(), ChargingSchedulePeriod[].class);
                                    chargingSchedule = new ChargingSchedule(ChargingRateUnitType.W, chargingSchedulePeriods);
                                    chargingSchedule.setDuration(duration);
                                    chargingSchedule.setStartSchedule(zonedDateTimeConvert.doZonedDateTimeToDatetime1());
                                    getCompositeScheduleConfirmation.setChargingSchedule(chargingSchedule);
                                    onResultSend(actionName, message.getId(), getCompositeScheduleConfirmation);
                                } else {
                                    try {
                                        // chargingSchedulePeriod 배열 생성
                                        JSONArray chargingSchedulePeriod = new JSONArray();
                                        JSONObject period = new JSONObject();
                                        period.put("startPeriod", 0);
                                        period.put("limit", 7);
                                        chargingSchedulePeriod.put(period);

                                        chargingSchedulePeriods = gson.fromJson(chargingSchedulePeriod.toString(), ChargingSchedulePeriod[].class);
                                        // chargingSchedule 객체 생성
                                        ChargingSchedule cSchedule = new  ChargingSchedule(ChargingRateUnitType.W, chargingSchedulePeriods);
                                        cSchedule.setDuration(duration);
                                        cSchedule.setStartSchedule(zonedDateTimeConvert.doZonedDateTimeToDatetime1());
                                        cSchedule.setChargingSchedulePeriod(chargingSchedulePeriods);

                                        // 최상위 JSON 생성
                                        getCompositeScheduleConfirmation.setStatus(GetCompositeScheduleStatus.Accepted);
                                        getCompositeScheduleConfirmation.setConnectorId(connectorId);
                                        getCompositeScheduleConfirmation.setChargingSchedule(cSchedule);

                                        onResultSend(actionName, message.getId(), getCompositeScheduleConfirmation);


                                    } catch (JSONException e) {
                                        logger.error(" {}", e.getMessage());
                                    }
                                }
//                                int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
//                                int duration = jsonObject.has("duration") ? jsonObject.getInt("duration") : -1;
//                                ChargingRateUnitType chargingRateUnit = null;
//                                if (jsonObject.has("chargingRateUnit")) {
//                                    try {
//                                        chargingRateUnit = ChargingRateUnitType.valueOf(jsonObject.getString("chargingRateUnit"));
//                                    } catch (IllegalArgumentException e) {
//                                        logger.error("GetCompositeSchedule invalid chargingRateUnit: {}", e.getMessage());
//                                        GetCompositeScheduleConfirmation rejected = new GetCompositeScheduleConfirmation(GetCompositeScheduleStatus.Rejected);
//                                        onResultSend(actionName, message.getId(), rejected);
//                                        return;
//                                    }
//                                }
//                                // GetCompositeSchedule response
//                                GetCompositeScheduleConfirmation getCompositeScheduleConfirmation = new GetCompositeScheduleConfirmation(GetCompositeScheduleStatus.Accepted);
//                                getCompositeScheduleConfirmation.setConnectorId(connectorId);
//                                getCompositeScheduleConfirmation.setScheduleStart(zonedDateTimeConvert.doZonedDateTimeToDatetime1());
//                                JSONArray jsonArray = onChargingSchedulePeriodCurrentData(connectorId - 1, duration);
//
//                                Gson gson = new Gson();
//                                ChargingSchedulePeriod[] chargingSchedulePeriods;
//                                ChargingSchedule chargingSchedule;
//                                if (jsonArray != null) {
//                                    chargingSchedulePeriods = gson.fromJson(jsonArray.toString(), ChargingSchedulePeriod[].class);
//                                    chargingSchedule = new ChargingSchedule(ChargingRateUnitType.W, chargingSchedulePeriods);
//                                    chargingSchedule.setDuration(duration);
//                                    chargingSchedule.setStartSchedule(zonedDateTimeConvert.doZonedDateTimeToDatetime1());
//                                    getCompositeScheduleConfirmation.setChargingSchedule(chargingSchedule);
//                                    onResultSend(actionName, message.getId(), getCompositeScheduleConfirmation);
//                                } else {
//                                    // 적용 가능한 충전 스케줄 없음 → OCPP 1.6 스펙에 따라 Rejected 반환
////                                    GetCompositeScheduleConfirmation rejected = new GetCompositeScheduleConfirmation(GetCompositeScheduleStatus.Rejected);
////                                    onResultSend(actionName, message.getId(), rejected);
//                                    try {
//                                        // chargingSchedulePeriod 배열 생성
//                                        JSONArray chargingSchedulePeriod = new JSONArray();
//                                        JSONObject period = new JSONObject();
//                                        period.put("startPeriod", 0);
//                                        period.put("limit", 7);
//                                        chargingSchedulePeriod.put(period);
//
//                                        chargingSchedulePeriods = gson.fromJson(chargingSchedulePeriod.toString(), ChargingSchedulePeriod[].class);
//                                        // chargingSchedule 객체 생성
//                                        ChargingSchedule cSchedule = new  ChargingSchedule(ChargingRateUnitType.W, chargingSchedulePeriods);
//                                        cSchedule.setDuration(duration);
//                                        cSchedule.setStartSchedule(zonedDateTimeConvert.doZonedDateTimeToDatetime1());
//                                        cSchedule.setChargingSchedulePeriod(chargingSchedulePeriods);
//
//                                        // 최상위 JSON 생성
//                                        getCompositeScheduleConfirmation.setStatus(GetCompositeScheduleStatus.Accepted);
//                                        getCompositeScheduleConfirmation.setConnectorId(1);
//                                        getCompositeScheduleConfirmation.setChargingSchedule(cSchedule);
//
//                                        onResultSend(actionName, message.getId(), getCompositeScheduleConfirmation);
//
//
//                                    } catch (JSONException e) {
//                                        logger.error(" {}", e.getMessage());
//                                    }
//                                }

                            } else if (Objects.equals("extendedTriggerMessage", actionName)) {
                                // requestedMessage는 TriggerMessageRequestType 값이므로 String으로 받아야 함
                                // TriggerMessageStatus.valueOf()로 파싱하면 "BootNotification" 등에서 exception 발생 → confirmation 미전송 버그
                                String requestedMessageStr = jsonObject.has("requestedMessage") ? jsonObject.getString("requestedMessage") : "";
                                int connectorId = jsonObject.has("connectorId") ? jsonObject.getInt("connectorId") : -1;
                                ExtendedTriggerMessageConfirmation extendedTriggerMessageConfirmation = new ExtendedTriggerMessageConfirmation(TriggerMessageStatus.Accepted);
                                onResultSend(actionName, message.getId(), extendedTriggerMessageConfirmation);
                                // TriggerMessage 핸들러와 동일한 처리
                                switch (requestedMessageStr) {
                                    case "BootNotification":
                                        GlobalVariables.setTriggerBootNotification(true);
                                        BootNotificationRequest extBootNotificationRequest = new BootNotificationRequest(
                                                chargerConfiguration.getChargerPointVendor(),
                                                chargerConfiguration.getChargerPointModel());
                                        extBootNotificationRequest.setFirmwareVersion(GlobalVariables.VERSION);
                                        extBootNotificationRequest.setImsi(chargerConfiguration.getImsi());
                                        extBootNotificationRequest.setChargePointSerialNumber(chargerConfiguration.getChargerId());
                                        onSend(100, extBootNotificationRequest.getActionName(), extBootNotificationRequest);
                                        break;
                                    case "Heartbeat":
                                        HeartbeatRequest extHeartbeatRequest = new HeartbeatRequest();
                                        onSend(extHeartbeatRequest.getActionName(), extHeartbeatRequest);
                                        break;
                                    case "StatusNotification":
                                        processHandler.sendMessage(onMakeHandlerMessage(
                                                GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                connectorId,
                                                0, null, null, null, false));
                                        break;
                                    case "FirmwareStatusNotification":
                                        FirmwareStatusNotificationRequest extFwStatusReq = new FirmwareStatusNotificationRequest(FirmwareStatus.Idle);
                                        onSend(100, extFwStatusReq.getActionName(), extFwStatusReq);
                                        break;
                                    case "DiagnosticsStatusNotification":
                                        diagnosticsStatusNotificationRequest =
                                                new DiagnosticsStatusNotificationRequest(chargerConfiguration.getDiagnosticsStatus());
                                        onSend(100, diagnosticsStatusNotificationRequest.getActionName(), diagnosticsStatusNotificationRequest);
                                        break;
                                    case "LogStatusNotification":
                                        LogStatusNotificationRequest extLogStatusReq = new LogStatusNotificationRequest(chargerConfiguration.getUploadLogStatus());
                                        extLogStatusReq.setRequestId(GlobalVariables.getRequestId());
                                        onSend(100, extLogStatusReq.getActionName(), extLogStatusReq);
                                        break;
                                    default:
                                        logger.error("extendedTriggerMessage unknown requestedMessage: {}", requestedMessageStr);
                                        break;
                                }
                            } else if (Objects.equals("InstallCertificate", actionName)) {
                                boolean validPeriod = false, result = false;
                                CertificateUse certificateType = jsonObject.has("certificateType") ? CertificateUse.valueOf(jsonObject.getString("certificateType")) : null;
                                String certificate = jsonObject.has("certificate") ? jsonObject.getString("certificate") : "";
                                // 인증서 유효 기간 check
                                validPeriod = getCertificateValidity(certificate);

                                //인증서 화일 이름 변경 해야 함.  2025.9.28
                                if (validPeriod) {
                                    result = fileManagement.stringToFileSave(GlobalVariables.getRootPath(),
                                            certificateType == CertificateUse.CentralSystemRootCertificate ? "cert.pem" : "dongahtest.p-e.kr.crt", certificate, false);
                                }

                                // 인증서 저장을 한다.
                                InstallCertificateConfirmation installCertificateConfirmation =
                                        new InstallCertificateConfirmation(!validPeriod || !result ? CertificateStatus.Rejected : CertificateStatus.Accepted);
                                onResultSend(actionName, message.getId(), installCertificateConfirmation);

                                if (!result) {
                                    ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime();
                                    SecurityEventNotificationRequest securityEventNotificationRequest = new SecurityEventNotificationRequest("InvalidCentralSystemCertificate", timestamp);
                                    onSend(100, securityEventNotificationRequest.getActionName(), securityEventNotificationRequest);
                                }

                            } else if (Objects.equals("GetInstalledCertificateIds", actionName)) {
                                CertificateUse certificateType = jsonObject.has("certificateType") ? CertificateUse.valueOf(jsonObject.getString("certificateType")) : null;
                                JSONArray jsonArray = getCertification(certificateType);
                                Gson gson = new Gson();
                                GetInstalledCertificateIdsConfirmation getInstalledCertificateIdsConfirmation =
                                        new GetInstalledCertificateIdsConfirmation(jsonArray != null ? GetInstalledCertificateStatus.Accepted : GetInstalledCertificateStatus.NotFound);
                                if (jsonArray != null) {
                                    CertificateHashDataType[] certificateHashDataTypes = gson.fromJson(jsonArray.toString(), CertificateHashDataType[].class);
                                    getInstalledCertificateIdsConfirmation.setCertificateHashData(certificateHashDataTypes);
                                }
                                onResultSend(actionName, message.getId(), getInstalledCertificateIdsConfirmation);
                            } else if (Objects.equals("DeleteCertificate", actionName)) {
                                String certificateHashDataString = jsonObject.has("certificateHashData") ? jsonObject.getString("certificateHashData") : null;
                                Gson gson = new Gson();
                                CertificateHashDataType certificateHashDataType = gson.fromJson(certificateHashDataString, CertificateHashDataType.class);

                                // result = 1:CentralSystemRootCertificate   2:ManufacturerRootCertificate
                                CertificateHashDataType certificateData;
                                int deleteCheck = -1;
                                boolean deleteChk = false;
                                JSONArray jsonArray = getCertification(CertificateUse.CentralSystemRootCertificate);
                                if (jsonArray == null) {
                                    jsonArray = getCertification(CertificateUse.ManufacturerRootCertificate);
                                    if (jsonArray != null) deleteCheck = 2;
                                } else {
                                    certificateData = gson.fromJson(certificateHashDataString, CertificateHashDataType.class);
                                    if (Objects.equals(certificateData.getSerialNumber(), certificateHashDataType.getSerialNumber()))
                                        deleteCheck = 1;
                                }

                                if (deleteCheck > 0) {
                                    String filename = deleteCheck == 1 ? "cert.pem" : "dongahtest.p-e.kr.crt";
                                    File file = new File(GlobalVariables.getRootPath() + File.separator + filename);
                                    deleteChk = file.delete();
                                }
                                DeleteCertificateStatus deleteCertificateStatus = deleteCheck == -1 ? DeleteCertificateStatus.NotFound :
                                        deleteChk ? DeleteCertificateStatus.Accepted : DeleteCertificateStatus.Failed;
                                DeleteCertificateConfirmation deleteCertificateConfirmation = new DeleteCertificateConfirmation(deleteCertificateStatus);
                                onResultSend(actionName, message.getId(), deleteCertificateConfirmation);
                            } else if (Objects.equals("GetLog", actionName)) {
                                // LogType ==> DiagnosticsLog, SecurityLog;
                                LogType logType = jsonObject.has("logType") ? LogType.valueOf(jsonObject.getString("logType")) : null;
                                int requestId = jsonObject.has("requestId") ? jsonObject.getInt("requestId") : -1;
                                int retries = jsonObject.has("retries") ? jsonObject.getInt("retries") : -1;
                                int retryInterval = jsonObject.has("retryInterval") ? jsonObject.getInt("retryInterval") : -1;
                                jsonObjectData = jsonObject.getJSONObject("log");
                                String remoteLocation = jsonObjectData.getString("remoteLocation");
                                String oldestTimestamp = jsonObjectData.has("oldestTimestamp") ? jsonObjectData.getString("oldestTimestamp") : "";
                                String latestTimestamp = jsonObjectData.has("latestTimestamp") ? jsonObjectData.getString("latestTimestamp") : "";

                                GetLogConfirmation getLogConfirmation = new GetLogConfirmation(LogStatus.Accepted);
                                getLogConfirmation.setFilename(logType == LogType.SecurityLog ? "securityLog.dongah" : "diagnostics.dongah");
                                onResultSend(actionName, message.getId(), getLogConfirmation);

                                // security log upload
                                boolean securityLogFileMake = onSecurityLogFileMake(oldestTimestamp, latestTimestamp, remoteLocation, requestId);

                            } else if (Objects.equals("SignedUpdateFirmware", actionName)) {
                                int retries = jsonObject.has("retries") ? jsonObject.getInt("retries") : -1;
                                int retryInterval = jsonObject.has("retryInterval") ? jsonObject.getInt("retryInterval") : -1;
                                GlobalVariables.setRequestId(jsonObject.has("requestId") ? jsonObject.getInt("requestId") : -1);
                                jsonObjectData = jsonObject.getJSONObject("firmware");
                                String location = jsonObjectData.getString("location");
//                                String[] locations = location.split(":");
//                                location = "/" +  locations[1];

                                String retrieveDateTime = jsonObjectData.getString("retrieveDateTime");
                                String installDateTime = jsonObjectData.getString("installDateTime");
                                String signingCertificate = jsonObjectData.getString("signingCertificate");
                                String signature = jsonObjectData.getString("signature");

                                GlobalVariables.setSignature(signature);
                                GlobalVariables.setSigningCertificate(signingCertificate);

                                UpdateFirmwareStatus updateFirmwareStatus = UpdateFirmwareStatus.Accepted;
                                SignedUpdateFirmwareConfirmation signedUpdateFirmwareConfirmation = new SignedUpdateFirmwareConfirmation(updateFirmwareStatus);
                                onResultSend(actionName, message.getId(), signedUpdateFirmwareConfirmation);

                                //SignedFirmwareStatus
                                SignedFirmwareStatusNotificationRequest signedFirmwareStatusNotificationRequest = new SignedFirmwareStatusNotificationRequest(SignedFirmwareStatus.Downloading);
                                chargerConfiguration.setSignedFirmwareStatus(SignedFirmwareStatus.Downloading);
                                signedFirmwareStatusNotificationRequest.setRequestId(GlobalVariables.getRequestId());
                                onSend(100, signedFirmwareStatusNotificationRequest.getActionName(), signedFirmwareStatusNotificationRequest);
                                // update firmware 다운 전에 GlobalVariables.ChargerOperation[] = false ==> Unavailable
                                for (int i = 0; i < GlobalVariables.maxPlugCount; i++)
                                    GlobalVariables.ChargerOperation[i] = false;
                                onChargerOperateSave();

                                // Status Notification
                                processHandler.sendMessage(onMakeHandlerMessage(
                                        GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION_BOOT,
                                        100,
                                        0,
                                        null,
                                        null,
                                        null,
                                        false));

                                /** SFTP */
//                                SftpRxJava sftpRxJava = new SftpRxJava(FileTransType.SIGNED_FIRMWARE, location);
//                                sftpRxJava.downloadTask();

                                /** Ftp */
                                FtpRxJava ftpRxJava = new FtpRxJava(FileTransType.SIGNED_FIRMWARE, location);
                                ftpRxJava.downloadTask();
                            }
                            break;
                    }

                } catch (Exception e) {
                    logger.error("receive error : {}", e.getMessage());
                    // central system response 받아 hashMapUid 삭제 */
                    if (!TextUtils.isEmpty(actionName)) hashMapUuid.remove(message.getId());
                }
            }
        });
    }

    private boolean setLocalAuthorizationList(int version , JSONObject jsonObject) {
        boolean result = false;
        try {
            String idTag = jsonObject.getString("idTag");
            JSONObject idTagInfo = new JSONObject(jsonObject.getString("idTagInfo"));

            //저장된 리스트
            String orgList = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + "localAuthorizationList");
            JSONObject orgJsonList = new JSONObject(orgList);
            int listVersion = orgJsonList.getInt("listVersion");
            JSONArray orgLocalAuthorizationList = orgJsonList.getJSONArray("localAuthorizationList");
            JSONArray jArray = new JSONArray();
            //not found
            boolean emptyChk = false;

            for (int i = 0; i < orgLocalAuthorizationList.length(); i++) {
                JSONObject orgDetail = orgLocalAuthorizationList.getJSONObject(i);
                if (Objects.equals(idTag, orgDetail.getString("idTag"))) {
                    JSONObject obj = new JSONObject();
                    obj.put("idTag", idTag);
                    JSONObject objIdTagInfo = new JSONObject();
                    objIdTagInfo.put("expiryDate", idTagInfo.getString("expiryDate"));
                    objIdTagInfo.put("parentIdTag", idTagInfo.getString("parentIdTag"));
                    objIdTagInfo.put("status", idTagInfo.getString("status"));
                    obj.put("idTagInfo", objIdTagInfo);
                    jArray.put(obj);
                    result = emptyChk = true;
                } else {
                    jArray.put(orgDetail);
                }
            }
            if (!emptyChk) {
                JSONObject obj = new JSONObject();
                obj.put("idTag", idTag);
                JSONObject objIdTagInfo = new JSONObject();
                objIdTagInfo.put("expiryDate", idTagInfo.getString("expiryDate"));
                objIdTagInfo.put("parentIdTag", idTagInfo.getString("parentIdTag"));
                objIdTagInfo.put("status", idTagInfo.getString("status"));
                obj.put("idTagInfo", objIdTagInfo);
                jArray.put(obj);
            }
            JSONObject sObject = new JSONObject();
            sObject.put("listVersion", version);
            sObject.put("localAuthorizationList", jArray);
            sObject.put("updateType", "Differential");
            result = fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "localAuthorizationList", sObject.toString(), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getConfigurationValue(String key) {
        String result = "none";
        try {
            String configurationString = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + "ConfigurationKey");
            JSONObject jsonObjectData = new JSONObject(configurationString);
            JSONArray jsonArrayContent = jsonObjectData.getJSONArray("values");
            for (int i = 0; i < jsonArrayContent.length(); i++) {
                JSONObject contDetail = jsonArrayContent.getJSONObject(i);
                if (Objects.equals(contDetail.get("key"), key)) {
                    result = contDetail.getString("value");
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
        }
        return result;
    }

    public boolean setConfigurationValue(String key, String value) {
        boolean result = false;

        try {
            String configurationString =
                    fileManagement.getStringFromFile(
                            GlobalVariables.getRootPath() + File.separator + "ConfigurationKey");

            JSONArray jsonArrayContent = new JSONObject(configurationString).getJSONArray("values");
            JSONArray jsonArray = new JSONArray();

            boolean notFound = true;

            for (int i = 0; i < jsonArrayContent.length(); i++) {
                JSONObject contDetail = jsonArrayContent.getJSONObject(i);

                if (Objects.equals(contDetail.getString("key"), key)) {

                    notFound = false;

                    // readonly면 기존 값 유지
                    if (contDetail.getBoolean("readonly")) {
                        jsonArray.put(contDetail);
                    } else {
                        JSONObject obj = new JSONObject();
                        obj.put("key", key);
                        obj.put("readonly", false);
                        obj.put("value", doAuthorizationKeyConvert(key, value));
                        jsonArray.put(obj);
                    }

                } else {
                    jsonArray.put(contDetail);
                }
            }

            GlobalVariables.setNotSupportedKey(notFound);

            JSONObject sObject = new JSONObject();
            sObject.put("values", jsonArray);

            result = fileManagement.stringToFileSave(
                    GlobalVariables.getRootPath(),
                    "ConfigurationKey",
                    sObject.toString(),
                    false
            );

        } catch (Exception e) {
            logger.error("SetConfigurationValue {}", e.getMessage(), e);
        }

        return result;
    }


    private String doAuthorizationKeyConvert(String key, String value) {
        try {
            if (Objects.equals(key, "AuthorizationKey")) {
                DataTransformation dataTransformation = new DataTransformation();
                return dataTransformation.hexToString(value);
            } else {
                return value;
            }
        } catch (Exception e) {
            logger.error(" doAuthorizationKeyConvert error : {}", e.getMessage());
            return "";
        }
    }

    // localAuthorizationList ==> 사용자 인증
    public boolean getLocalAuthorizationListFind(String idTag) {
        boolean result = false;
        try {
            String authorizationList = GlobalVariables.getRootPath() + File.separator + "localAuthorizationList";
            File targetFile = new File(authorizationList);
            if (targetFile.exists()) {
                JSONObject jsonObject = new JSONObject(fileManagement.getStringFromFile(authorizationList));
                JSONArray jsonArray = jsonObject.getJSONArray("localAuthorizationList");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject contDetail = jsonArray.getJSONObject(i);
                    if (Objects.equals(idTag, contDetail.getString("idTag"))) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(" getLocalAuthorizationListFind error : {}", e.getMessage());
        }
        return result;
    }

    // idTag, parentIdTag return
    public String[] getLocalAuthorizationListStrings(String idTag) {
        boolean idTagCheck = false;
        String[] result = new String[2];
        try {
            String authorizationList = GlobalVariables.getRootPath() + File.separator + "localAuthorizationList";
            File targetFile = new File(authorizationList);
            if (targetFile.exists()) {
                JSONObject jsonObject = new JSONObject(fileManagement.getStringFromFile(authorizationList));
                JSONArray jsonArray = jsonObject.getJSONArray("localAuthorizationList");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject contDetail = jsonArray.getJSONObject(i);
                    if (Objects.equals(idTag, contDetail.getString("idTag"))) {
                        result[0] = contDetail.getString("idTag");
                        JSONObject idTagInfo = new JSONObject(contDetail.getString("idTagInfo"));
                        result[1] = idTagInfo.optString("parentIdTag", "");
                        idTagCheck = true;
                        break;
                    }
                }
            }
            //idTag 값이 없는 경우
            if (!idTagCheck) {
                result[0] = "notFound";
                result[1] = "";
            }
        } catch (Exception e) {
            logger.error(" getLocalAuthorizationListStrings error : {}", e.getMessage());
        }
        return result;
    }


    //ChargerOperate
    public void onChargerOperateSave() {
        try {
            boolean chk;
            String rootPath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";
            String fileName = "ChargerOperate";
            File file = new File(rootPath + File.separator + fileName);
            if (file.exists()) chk = file.delete();
            for (int i = 0; i < GlobalVariables.maxPlugCount; i++) {
                String statusContent = String.valueOf(GlobalVariables.ChargerOperation[i]);
                fileManagement.stringToFileSave(rootPath, fileName, statusContent, true);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean onDiagnosticsFileMake(String startTime, String stopTime, String location) {
        boolean result = false;
        try {
            File diagnosticsContext = new File(GlobalVariables.getRootPath() + File.separator + "diagnostics.dongah");
            JSONArray resultJsonArray = new JSONArray();

            if (diagnosticsContext.exists()) {
                FileReader fileReader = new FileReader(diagnosticsContext);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    try {
                        JSONObject object = new JSONObject(line);
                        JSONArray array = object.getJSONArray("diagnostics");
                        JSONObject contDetail = array.getJSONObject(0);
                        String startDate = contDetail.getString("startTime");
                        boolean inRange;
                        if (startTime != null && stopTime != null) {
                            inRange = startTime.compareTo(startDate) <= 0 && stopTime.compareTo(startDate) >= 0;
                        } else {
                            inRange = true;
                        }
                        if (inRange) {
                            resultJsonArray.put(contDetail);
                        }
                    } catch (Exception e) {
                        logger.error("diagnosticsFileMake line parse: {}", e.getMessage());
                    }
                }
                bufferedReader.close();
            }

            if (resultJsonArray.length() == 0) {
                JSONObject current = new JSONObject();
                current.put("startTime", zonedDateTimeConvert.doGetUtcDatetimeAsString());
                resultJsonArray.put(current);
            }

            File resultFile = new File(GlobalVariables.getRootPath() + File.separator + "diagnostics");
            if (resultFile.exists()) {
                boolean check = resultFile.delete();
            }
            fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "diagnostics", resultJsonArray.toString(), false);

            /** FTP */
            FtpRxJava ftpRxJava = new FtpRxJava(FileTransType.DIAGNOSTICS, location);
            ftpRxJava.downloadTask();
            result = true;

        } catch (Exception e) {
            logger.error(" diagnosticsFileMake {}", e.getMessage());
            try {
                chargerConfiguration.setDiagnosticsStatus(DiagnosticsStatus.UploadFailed);
                diagnosticsStatusNotificationRequest = new DiagnosticsStatusNotificationRequest(DiagnosticsStatus.UploadFailed);
                onSend(100, diagnosticsStatusNotificationRequest.getActionName(), diagnosticsStatusNotificationRequest);
            } catch (OccurenceConstraintException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }


    /**
     * Security log
     */
    public boolean onSecurityLogFileMake(String oldestTimestamp, String latestTimestamp, String location, int requestId) {
        boolean result = false;
        try {
//            File securityLogContext = new File(GlobalVariables.getRootPath() + File.separator + "securityLog.dongah");
//            FileReader fileReader = new FileReader(securityLogContext);
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//            int count = 0;
//            String line;
//            JSONArray resultJsonArray = new JSONArray();
//            while ((line = bufferedReader.readLine()) != null) {
//                JSONObject object = new JSONObject(line);
//                JSONArray array = object.getJSONArray("SecurityLogs");
//                JSONObject contDetail = array.getJSONObject(0);
//                String startDate = contDetail.getString("startTime");
//                startDate = zonedDateTimeConvert.getStringCurrentTimeZone();
//                String SecurityLog = contDetail.getString("securityLog");
//                if (oldestTimestamp.compareTo(startDate) < 0 && latestTimestamp.compareTo(startDate) > 0) {
//                    //json array make
//                    resultJsonArray.put(contDetail);
//                    count++;
//                }
//                if (count < 5) {
//                    resultJsonArray.put(contDetail);
//                    count++;
//                }
//            }
//            if (count > 0) {
//                //save
//                File resultFile = new File(GlobalVariables.getRootPath() + File.separator + "securityLogs");
//                if (resultFile.exists()) {
//                    boolean check = resultFile.delete();
//                }
//                fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "securityLogs", resultJsonArray.toString(), false);
//                //SFTP 연결
////                SftpRxJava sftpRxJava = new SftpRxJava(FileTransType.SECURITY, location);
////                sftpRxJava.downloadTask();
//
//                /** FTP */
//                GlobalVariables.setRequestId(requestId);
//                FtpRxJava ftpRxJava = new FtpRxJava(FileTransType.SECURITY, location);
//                ftpRxJava.downloadTask();
//
//                result = true;
//            }

            /** FTP */
            GlobalVariables.setRequestId(requestId);
            FtpRxJava ftpRxJava = new FtpRxJava(FileTransType.SECURITY, location);
            ftpRxJava.downloadTask();

            result = true;

        } catch (Exception e) {
            logger.error(" onSecurityLogFileMake {}", e.getMessage());
        }
        return result;
    }

    private int onFindConnectorId(String reservationId, boolean upDate) {
        int result = 0;
        try {
            for (int i = 0; i < 2; i++) {
                ChargingCurrentData chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(i);
                if (Objects.equals(reservationId, chargingCurrentData.getResReservationId())) {
                    result = chargingCurrentData.getResConnectorId();
                    if (upDate) {
                        chargingCurrentData.setResConnectorId(0);
                        chargingCurrentData.setResExpiryDate("");
                        chargingCurrentData.setResIdTag("");
                        chargingCurrentData.setResParentIdTag("");
                        chargingCurrentData.setResReservationId("");
                        chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(" {}", e.getMessage());
        }
        return result;
    }

    public String getChargingSchedule() {
        try {
            JSONObject chargingSchedule;
            String csChargingProfilesList = GlobalVariables.getRootPath() + File.separator + "csChargingProfiles";
            File targetFile = new File(csChargingProfilesList);
            if (targetFile.exists()) {
                JSONObject jsonObject = new JSONObject(fileManagement.getStringFromFile(csChargingProfilesList));

                JSONObject chargingProfiles = jsonObject.getJSONObject("csChargingProfiles");

                chargingSchedule = chargingProfiles.getJSONObject("chargingSchedule");

                return chargingSchedule.toString();
            }
        } catch (Exception e) {
            logger.error(" getChargingSchedule error : {}", e.getMessage());
        }
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onUpdateChargingProfile(JSONObject scProfile) {
        try {
            int connectorId = scProfile.has("connectorId") ? scProfile.getInt("connectorId") : -1;
            String idTag = scProfile.has("idTag") ? scProfile.getString("idTag") : "";
            JSONObject chargingProfiles = scProfile.has("csChargingProfiles") ? scProfile.getJSONObject("csChargingProfiles")
                    : scProfile.has("chargingProfile") ? scProfile.getJSONObject("chargingProfile") : null;
            assert chargingProfiles != null;
            int profileId = chargingProfiles.getInt("chargingProfileId");
            int level = chargingProfiles.has("stackLevel") ? chargingProfiles.getInt("stackLevel") : -1;
            // ChargePointMaxProfile, TxDefaultProfile, TxProfile
            ChargingProfilePurposeType chargingProfilePurpose = ChargingProfilePurposeType.valueOf(chargingProfiles.getString("chargingProfilePurpose"));
            ChargingProfileKindType chargingProfileKind = ChargingProfileKindType.valueOf(chargingProfiles.getString("chargingProfileKind"));

            JSONObject chargingSchedule = chargingProfiles.getJSONObject("chargingSchedule");
            int duration = chargingSchedule.has("duration") ? chargingSchedule.getInt("duration") : 0;

            boolean found = false;
            JSONArray jArray = new JSONArray();

            File file = new File(GlobalVariables.getRootPath() + File.separator + "csChargingProfiles");
            if (!file.exists()) {
                boolean check = file.createNewFile();
                //chargingProfiles
                jArray.put(scProfile);
                JSONObject sObject = new JSONObject();
                sObject.put("SetChargingProfile", jArray);

                boolean result = fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "csChargingProfiles", sObject.toString(), false);
            } else {
                String csFileString = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + "csChargingProfiles");
                JSONObject orgChargingProfiles = new JSONObject(csFileString);
                JSONArray orgSetChargingProfile = orgChargingProfiles.getJSONArray("SetChargingProfile");


                for (int i = 0; i < orgSetChargingProfile.length(); i++) {
                    JSONObject jsonobject = orgSetChargingProfile.getJSONObject(i);
                    int readConnectorId = jsonobject.getInt("connectorId");
                    JSONObject readChargingProfiles = jsonobject.getJSONObject("csChargingProfiles");
                    ChargingProfilePurposeType orgProfilePurpose = ChargingProfilePurposeType.valueOf(readChargingProfiles.getString("chargingProfilePurpose"));
                    int stackLevel = readChargingProfiles.getInt("stackLevel");
                    if (Objects.equals(chargingProfilePurpose, orgProfilePurpose) && Objects.equals(level, stackLevel)) {
                        jArray.put(scProfile);
                        found = true;
                    } else {
                        jArray.put(jsonobject);
                    }
                }
                // for문에서 없는 profile
                if (!found) {
                    jArray.put(scProfile);
                }

                JSONObject sObject = new JSONObject();
                sObject.put("SetChargingProfile", jArray);
                boolean result = fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "csChargingProfiles", sObject.toString(), false);
                if (!Objects.equals(chargingProfilePurpose, ChargingProfilePurposeType.ChargePointMaxProfile)) {
                    chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(connectorId - 1);
                }
            }
            if (chargingCurrentData.isRemoteStartSmartCharging() && duration > 0) {
                chargingCurrentData.remoteSmartChargingJsonArray = onChargingSchedulePeriodCurrentData(connectorId - 1, duration);
            }
        } catch (Exception e) {
            logger.error(" onUpdateChargingProfile error -  {} ", e.getMessage());
        }
    }


    public boolean onClearChargingProfile(int scProfileId, int scStackLevel, ChargingProfilePurposeType chargingProfilePurposeType) {
        boolean found = false;
        try {
            //csChargingProfiles all delete
            if (scProfileId == -1 && scStackLevel == -1) {
                //모든 삭제 csChargingProfiles
                File resultFile = new File(GlobalVariables.getRootPath() + File.separator + "csChargingProfiles");
                if (resultFile.exists()) {
                    return resultFile.delete();
                }
            }

            JSONArray jArray = new JSONArray();
            String csFileString = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + "csChargingProfiles");
            JSONObject orgChargingProfiles = new JSONObject(csFileString);
            JSONArray orgChargingProfilesList = orgChargingProfiles.getJSONArray("SetChargingProfile");
            //chargingProfileId 같은면 Update
            for (int i = 0; i < orgChargingProfilesList.length(); i++) {
                JSONObject jsonobject = orgChargingProfilesList.getJSONObject(i);
                int connectorId = jsonobject.getInt("connectorId");
                JSONObject csChargingProfiles = jsonobject.getJSONObject("csChargingProfiles");
                int ProfileId = csChargingProfiles.getInt("chargingProfileId");
                int stackLevel = csChargingProfiles.getInt("stackLevel");
                ChargingProfilePurposeType purposeType = ChargingProfilePurposeType.valueOf(csChargingProfiles.getString("chargingProfilePurpose"));
                if (scProfileId != -1) {
                    if (Objects.equals(ProfileId, scProfileId)) {
                        found = true;
                    } else {
                        jArray.put(jsonobject);
                    }
                } else if (scStackLevel != -1) {
                    if (Objects.equals(stackLevel, scStackLevel) && Objects.equals(chargingProfilePurposeType,purposeType)) {
                        found = true;
                    } else {
                        jArray.put(jsonobject);
                    }
                }
            }
            //save
            JSONObject sObject = new JSONObject();
            sObject.put("SetChargingProfile", jArray);
            boolean result = fileManagement.stringToFileSave(GlobalVariables.getRootPath(), "csChargingProfiles", sObject.toString(), false);
        } catch (Exception e) {
            logger.error(" onClearChargingProfile error : {}", e.getMessage());
        }
        return found;
    }


    String chargingProfilePurpose;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONArray onChargingSchedulePeriodCurrentData(int sChannel, int sDuration) {
        try {
            String startSchedule = "";
            int compositeGap = 0;
            HashMap<Integer, Integer>  durationTime = new HashMap<>();
            Date sStart = zonedDateTimeConvert.doStringDateToDate(zonedDateTimeConvert.getStringCurrentTimeZone());
            //
//            compositeTime = zonedDateTimeConvert.doStringDateToDate1("2025-08-20T02:43:38Z");
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ChargingCurrentData chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess(sChannel).getChargingCurrentData();
            String csFileString = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + "csChargingProfiles");
            JSONObject orgChargingProfiles = new JSONObject(csFileString);

            JSONArray setChargingProfiles = orgChargingProfiles.getJSONArray("SetChargingProfile");
            int chargingSchedulePeriodLength;
            int txProfileCount = 0, defaultDuration = 0;
            JSONArray jArray = new JSONArray();
            JSONArray maxProfileArray = new JSONArray();
            JSONArray defaultProfileArray = new JSONArray();
            JSONArray txProfileArray = new JSONArray();
            chargingCurrentData.maxProfileSchedulePeriod = null;
            chargingCurrentData.defaultProfileSchedulePeriod = null;
            chargingCurrentData.txProfileSchedulePeriod = null;


            for (int i = 0; i < Objects.requireNonNull(setChargingProfiles).length(); i++) {
                JSONObject setChargingProfile = setChargingProfiles.getJSONObject(i);
//                int connectorId = setChargingProfile.getInt("connectorId");
                JSONObject csChargingProfile = setChargingProfile.has("csChargingProfiles") ?
                        setChargingProfile.getJSONObject("csChargingProfiles") : setChargingProfile.getJSONObject("chargingProfile");

//                JSONObject csChargingProfile = setChargingProfiles.getJSONObject(i);
                int chargingProfileId = csChargingProfile.getInt("chargingProfileId");
                int stackLevel = csChargingProfile.getInt("stackLevel");

                chargingProfilePurpose = csChargingProfile.getString("chargingProfilePurpose");
                JSONObject csChargingSchedule = csChargingProfile.getJSONObject("chargingSchedule");
                int duration = csChargingSchedule.getInt("duration");


                JSONArray chargingSchedulePeriod = csChargingSchedule.getJSONArray("chargingSchedulePeriod");
                chargingSchedulePeriodLength = chargingSchedulePeriod.length();

                switch (chargingProfilePurpose) {
                    case "ChargePointMaxProfile":
                        chargingCurrentData.setMaxProfileDuration(duration);
                        compositeTime = zonedDateTimeConvert.doStringDateToDate1(csChargingSchedule.getString("startSchedule"));
                        chargingCurrentData.maxProfileSchedulePeriod = new String[chargingSchedulePeriodLength];
                        for (int j = 0; j < chargingSchedulePeriod.length(); j++) {
                            JSONObject obj = chargingSchedulePeriod.getJSONObject(j);
                            chargingCurrentData.setMaxProfileLimit(obj.getDouble("limit"));
                            chargingCurrentData.maxProfileSchedulePeriod[j] = obj.toString();
                            maxProfileArray.put(obj);
                        }

                        if (compositeTime != null) {
                            long diffTime = (sStart.getTime() - compositeTime.getTime()) / 1000;
                            compositeGap = (int) diffTime % 60;
                        }
                        break;
                    case "TxDefaultProfile":
                        defaultDuration = duration;
                        chargingCurrentData.setDefaultProfileDuration(duration);
                        chargingCurrentData.defaultProfileSchedulePeriod = new String[chargingSchedulePeriodLength];
                        List<JSONObject> tempDefaultProfiles = new ArrayList<>();

                        if (stackLevel == 0) {
                            compositeTime = zonedDateTimeConvert.doStringDateToDate1(csChargingSchedule.getString("startSchedule"));
                        }

                        List<JSONObject> tempList = new ArrayList<>();

                        for (int j = 0; j < chargingSchedulePeriod.length(); j++) {
                            JSONObject obj = chargingSchedulePeriod.getJSONObject(j);
                            chargingCurrentData.defaultProfileSchedulePeriod[j] = obj.toString();

                            // stackLevel을 같이 보관하기 위해 wrapper JSONObject 사용
                            JSONObject wrapper = new JSONObject();
                            wrapper.put("stackLevel", stackLevel);
                            wrapper.put("duration", duration);
                            wrapper.put("data", obj);
                            tempList.add(wrapper);
                        }

                        // 전체 TxDefaultProfile 정리용 배열에 합치기
                        for (JSONObject wrapped : tempList) {
                            defaultProfileArray.put(wrapped);
                        }
//                        for (int j = 0; j < chargingSchedulePeriod.length(); j++) {
//                            JSONObject obj = chargingSchedulePeriod.getJSONObject(j);
//                            chargingCurrentData.defaultProfileSchedulePeriod[j] = obj.toString();
//                            defaultProfileArray.put(obj);
//
//
//                        }
                        if (compositeTime != null) {
                            long diffTime = (sStart.getTime() - compositeTime.getTime()) / 1000;
                            compositeGap = (int) diffTime % 60;
                        }

                        break;
                    case "TxProfile":
                        txProfileCount++;
                        chargingCurrentData.setTxProfileDuration(duration);
                        chargingCurrentData.txProfileSchedulePeriod = new String[chargingSchedulePeriodLength];
                        for (int j = chargingSchedulePeriod.length() - 1; j >= 0; j--) {
                            JSONObject obj = chargingSchedulePeriod.getJSONObject(j);
                            chargingCurrentData.txProfileSchedulePeriod[j] = obj.toString();
                            txProfileArray.put(obj);
                        }
                        startSchedule = csChargingSchedule.getString("startSchedule");
                        if (compositeTime != null) {
                            long diffTime = (sStart.getTime() - compositeTime.getTime()) / 1000;
                            compositeGap = (int) diffTime % 60;
                        }
                        break;
                }
            }

            double limit = 0;
            int profileLen, startPeriod, orderType = 1;
            int startPeriodGap = 0;
            JSONObject orderCheckDefault;
            uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess(sChannel).getUiSeq();
            if (uiSeq == UiSeq.CHARGING) {
                //ChargePointMaxProfile
                profileLen = txProfileArray.length();
                if (profileLen > 0) {
                    orderCheckDefault = txProfileArray.getJSONObject(0);
                    orderType = orderCheckDefault.getInt("startPeriod");
                }
                JSONArray newTxProfileArray = new JSONArray();
                for (int i = 0; i < profileLen; i++) {
                    int index = (orderType != 0) ? (profileLen - 1 - i) : i;
                    newTxProfileArray.put(txProfileArray.get(index));
                }
                for (int s = 0; s < newTxProfileArray.length(); s++) {
                    JSONObject obj = newTxProfileArray.getJSONObject(s);

                    startPeriod = obj.getInt("startPeriod");
                    if (startPeriod == 0) {
                        limit = obj.getDouble("limit");
                        obj.putOpt("startPeriod", startPeriod);
                    } else {
                        double checkLimit =  Math.min(obj.getDouble("limit"), chargingCurrentData.getMaxProfileLimit());
                        if (limit ==  checkLimit) continue;
                        else limit = checkLimit;

                        obj.putOpt("startPeriod", startPeriod - compositeGap );
                    }
                    obj.putOpt("limit", limit);
                    jArray.put(obj);
                    startPeriodGap = startPeriod - compositeGap;
                }

                if (jArray.length() == 0) {
                    /////////////////// 나중에 삭제...........2025.8.27
                    //defaultProfileArray
                    List<JSONObject> defaultProfiles = new ArrayList<>();
                    for (int i = 0; i < defaultProfileArray.length(); i++) {
                        defaultProfiles.add(defaultProfileArray.getJSONObject(i));
                    }
                    defaultProfiles.sort((a, b) ->
                            {
                                try {
                                    return Integer.compare(b.getInt("stackLevel"), a.getInt("stackLevel"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );

                    // 정렬된 데이터를 다시 JSONArray 로
                    JSONArray sortedDefaultProfileArray = new JSONArray();
                    int[] duration = new int[defaultProfiles.size()];
                    int count = 0 ;
                    for (JSONObject wrapped : defaultProfiles) {
                        // wrapper 안의 실제 data만 꺼내기
                        sortedDefaultProfileArray.put(wrapped.getJSONObject("data"));

                        duration[count] = wrapped.getInt("duration");
                        count++;
                    }


                    profileLen = sortedDefaultProfileArray.length();
                    if (profileLen > 0) {
                        orderCheckDefault = sortedDefaultProfileArray.getJSONObject(0);
                        orderType = orderCheckDefault.getInt("startPeriod");
                    }
                    JSONArray newTxDefaultProfileArray = new JSONArray();
                    for (int i = 0; i < profileLen; i++) {
                        int index = (orderType != 0) ? (profileLen - 1 - i) : i;
                        newTxDefaultProfileArray.put(sortedDefaultProfileArray.get(index));
                    }

                    boolean startPeriodChk = false;
                    int durationChk =0;
                    for (int s = 0; s < newTxDefaultProfileArray.length(); s++) {
                        JSONObject obj = newTxDefaultProfileArray.getJSONObject(s);
                        startPeriod = obj.getInt("startPeriod");

                        if (startPeriod == 0 && !startPeriodChk) {
                            startPeriodChk = true;
                            limit = obj.getDouble("limit");
                            obj.putOpt("startPeriod", startPeriod);
                            obj.putOpt("limit", limit);
                            jArray.put(obj);
                        } else {
                            if (startPeriod == 0) continue;
                            double checkLimit = 0.0 ;
                            //
                            if (chargingCurrentData.getMaxProfileLimit() != 0.0) {
                                checkLimit = Math.min(obj.getDouble("limit"), chargingCurrentData.getMaxProfileLimit());
                            } else {
                                checkLimit = obj.getDouble("limit");
                            }

                            if (limit ==  checkLimit) continue;
                            else limit = checkLimit;

//                        obj.putOpt("startPeriod", startPeriod - compositeGap );
                            obj.putOpt("startPeriod", duration[s] == durationChk ? startPeriod - compositeGap : duration[s-2]  - compositeGap);

                            obj.putOpt("limit", limit);
                            jArray.put(obj);
                        }

                        durationChk = duration[s];
                    }

                    //////////////
                } else {
                    ///// DefaultTxt
                    int startPeriodGap99 = 0;
                    List<JSONObject> defaultProfiles = new ArrayList<>();
                    for (int i = 0; i < defaultProfileArray.length(); i++) {
                        defaultProfiles.add(defaultProfileArray.getJSONObject(i));
                    }
                    defaultProfiles.sort((a, b) ->
                            {
                                try {
                                    return Integer.compare(b.getInt("stackLevel"), a.getInt("stackLevel"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                    // 정렬된 데이터를 다시 JSONArray 로
                    JSONArray sortedDefaultProfileArray = new JSONArray();
                    int[] duration = new int[defaultProfiles.size()];
                    int count = 0 ;
                    for (JSONObject wrapped : defaultProfiles) {
                        // wrapper 안의 실제 data만 꺼내기
                        sortedDefaultProfileArray.put(wrapped.getJSONObject("data"));

                        duration[count] = wrapped.getInt("duration");
                        count++;
                    }
                    profileLen = sortedDefaultProfileArray.length();
                    if (profileLen > 0) {
                        orderCheckDefault = sortedDefaultProfileArray.getJSONObject(0);
                        orderType = orderCheckDefault.getInt("startPeriod");
                    }

                    JSONArray newTxDefaultProfileArray = new JSONArray();
                    for (int i = 0; i < profileLen; i++) {
                        int index = (orderType != 0) ? (profileLen - 1 - i) : i;
                        newTxDefaultProfileArray.put(sortedDefaultProfileArray.get(index));
                    }

                    for (int s = 0; s < newTxDefaultProfileArray.length(); s++) {
                        JSONObject obj = newTxDefaultProfileArray.getJSONObject(s);
                        startPeriod = obj.getInt("startPeriod");
                        limit = obj.getInt("limit");
                        if (startPeriod > startPeriodGap) {
//                            obj.putOpt("startPeriod", startPeriod - compositeGap);
                            obj.putOpt("startPeriod", chargingCurrentData.getTxProfileDuration() - compositeGap);
                            obj.putOpt("limit", limit);
                            jArray.put(obj);
                            startPeriodGap99 = startPeriod - compositeGap;
                        }
                    }

                    ///// maxProfile
                    if (maxProfileArray.length() != 0) {
                        JSONObject maxObject = maxProfileArray.getJSONObject(0);
                        startPeriod = maxObject.getInt("startPeriod");
                        limit = maxObject.getInt("limit");
                        if (startPeriodGap99 < sDuration){
                            maxObject.putOpt("startPeriod", defaultDuration - compositeGap);
                            maxObject.putOpt("limit", limit);
                            jArray.put(maxObject);
                        }
                    }
                }
            } else {
                //
                //defaultProfileArray
                List<JSONObject> defaultProfiles = new ArrayList<>();
                for (int i = 0; i < defaultProfileArray.length(); i++) {
                    defaultProfiles.add(defaultProfileArray.getJSONObject(i));
                }
                defaultProfiles.sort((a, b) ->
                        {
                            try {
                                return Integer.compare(b.getInt("stackLevel"), a.getInt("stackLevel"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
                // 정렬된 데이터를 다시 JSONArray 로
                JSONArray sortedDefaultProfileArray = new JSONArray();
                int[] duration = new int[defaultProfiles.size()];
                int count = 0 ;
                for (JSONObject wrapped : defaultProfiles) {
                    // wrapper 안의 실제 data만 꺼내기
                    sortedDefaultProfileArray.put(wrapped.getJSONObject("data"));

                    duration[count] = wrapped.getInt("duration");
                    count++;
                }
                profileLen = sortedDefaultProfileArray.length();
                if (profileLen > 0) {
                    orderCheckDefault = sortedDefaultProfileArray.getJSONObject(0);
                    orderType = orderCheckDefault.getInt("startPeriod");
                }

                JSONArray newTxDefaultProfileArray = new JSONArray();
                for (int i = 0; i < profileLen; i++) {
                    int index = (orderType != 0) ? (profileLen - 1 - i) : i;
                    newTxDefaultProfileArray.put(sortedDefaultProfileArray.get(index));
                }


                boolean startPeriodChk = false;
                int durationChk =0;

                for (int s = 0; s < newTxDefaultProfileArray.length(); s++) {
                    JSONObject obj = newTxDefaultProfileArray.getJSONObject(s);
                    startPeriod = obj.getInt("startPeriod");
                    if (startPeriod == 0 && !startPeriodChk) {
                        startPeriodChk = true;
                        limit = obj.getDouble("limit");
                        obj.putOpt("startPeriod", startPeriod);
                        obj.putOpt("limit", limit);
                        jArray.put(obj);
                    } else {
                        if (startPeriod == 0) continue;
                        double checkLimit = 0.0 ;
                        //
                        if (chargingCurrentData.getMaxProfileLimit() != 0.0) {
                            checkLimit = Math.min(obj.getDouble("limit"), chargingCurrentData.getMaxProfileLimit());
                        } else {
                            checkLimit = obj.getDouble("limit");
                        }

                        if (limit ==  checkLimit) continue;
                        else limit = checkLimit;

//                        obj.putOpt("startPeriod", startPeriod - compositeGap );
                        obj.putOpt("startPeriod", duration[s] == durationChk ? startPeriod - compositeGap : duration[s-2]  - compositeGap);

                        obj.putOpt("limit", limit);
                        jArray.put(obj);
                    }

                    durationChk = duration[s];
                }

                if (newTxDefaultProfileArray.length() == 0) {

                    ///// maxProfile
                    for (int i = 0; i < maxProfileArray.length(); i++) {
                        JSONObject mObj = maxProfileArray.getJSONObject(i);
                        startPeriod = mObj.getInt("startPeriod");
                        limit = mObj.getInt("limit");
                        mObj.putOpt("startPeriod", startPeriod == 0 ? startPeriod : startPeriod - compositeGap);
                        mObj.putOpt("limit", limit);
                        jArray.put(mObj);
//                        if (startPeriodGap99 < sDuration){
//                        }
                    }
                }
            }
            return jArray;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
//    public JSONArray onChargingSchedulePeriodCurrentData(int sChannel, int sDuration) {
//        try {
//            ChargingCurrentData chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(sChannel);
//            String csFileString = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + "csChargingProfiles");
//            JSONObject orgChargingProfiles = new JSONObject(csFileString);
//            JSONArray setChargingProfiles = orgChargingProfiles.getJSONArray("SetChargingProfile");
//
//            // 1. 프로파일 분류 및 상태 저장
//            double maxProfileCeiling = Double.MAX_VALUE;
//            ArrayList<JSONObject> defaultProfileList = new ArrayList<>(); // TxDefaultProfile 목록 (stackLevel 포함)
//            JSONObject txProfileSchedule = null;
//            int txProfileDuration = 0;
//            int txProfileMaxStack = -1;
//            // 각 프로파일의 startSchedule 기준 경과 시간 (복합 스케줄 startPeriod 오프셋 보정용)
//            long txProfileElapsed = 0;
//            long maxProfileElapsed = 0;
//            long nowMillis = System.currentTimeMillis();
//
//            chargingCurrentData.maxProfileSchedulePeriod = null;
//            chargingCurrentData.defaultProfileSchedulePeriod = null;
//            chargingCurrentData.txProfileSchedulePeriod = null;
//
//            for (int i = 0; i < setChargingProfiles.length(); i++) {
//                JSONObject entry = setChargingProfiles.getJSONObject(i);
//                JSONObject csProfile = entry.getJSONObject("csChargingProfiles");
//                String purpose = csProfile.getString("chargingProfilePurpose");
//                int stackLevel = csProfile.getInt("stackLevel");
//                JSONObject schedule = csProfile.getJSONObject("chargingSchedule");
//                int duration = schedule.has("duration") ? schedule.getInt("duration") : sDuration;
//                JSONArray periods = schedule.getJSONArray("chargingSchedulePeriod");
//
//                // scheduleStart 기준 경과 초 계산 (미래 startSchedule이면 0으로 처리)
//                long elapsed = schedule.has("startSchedule") ? parseElapsedSec(schedule.getString("startSchedule"), nowMillis) : 0;
//
//                switch (purpose) {
//                    case "ChargePointMaxProfile":
//                        maxProfileElapsed = elapsed;
//                        chargingCurrentData.setMaxProfileDuration(duration);
//                        chargingCurrentData.maxProfileSchedulePeriod = new String[periods.length()];
//                        for (int j = 0; j < periods.length(); j++) {
//                            JSONObject obj = periods.getJSONObject(j);
//                            chargingCurrentData.setMaxProfileLimit(obj.getDouble("limit"));
//                            chargingCurrentData.maxProfileSchedulePeriod[j] = obj.toString();
//                        }
//                        maxProfileCeiling = chargingCurrentData.getMaxProfileLimit();
//                        break;
//                    case "TxDefaultProfile":
//                        chargingCurrentData.setDefaultProfileDuration(duration);
//                        chargingCurrentData.defaultProfileSchedulePeriod = new String[periods.length()];
//                        for (int j = 0; j < periods.length(); j++) {
//                            chargingCurrentData.defaultProfileSchedulePeriod[j] = periods.getJSONObject(j).toString();
//                        }
//                        // elapsed는 JSONObject 변조 없이 startSchedule에서 직접 재계산
//                        defaultProfileList.add(csProfile);
//                        break;
//                    case "TxProfile":
//                        if (stackLevel > txProfileMaxStack) {
//                            txProfileMaxStack = stackLevel;
//                            txProfileSchedule = schedule;
//                            txProfileDuration = duration;
//                            txProfileElapsed = elapsed;
//                        }
//                        chargingCurrentData.setTxProfileDuration(duration);
//                        chargingCurrentData.txProfileSchedulePeriod = new String[periods.length()];
//                        for (int j = 0; j < periods.length(); j++) {
//                            chargingCurrentData.txProfileSchedulePeriod[j] = periods.getJSONObject(j).toString();
//                        }
//                        break;
//                }
//            }
//
//            // 2. TxDefaultProfile을 stackLevel 내림차순 정렬 (높을수록 우선순위 높음)
//            Collections.sort(defaultProfileList, (a, b) -> {
//                try {
//                    return Integer.compare(b.getInt("stackLevel"), a.getInt("stackLevel"));
//                } catch (JSONException ex) { return 0; }
//            });
//
//            // 3. 전체 시간 분기점(breakpoints) 수집: 쿼리 시각(scheduleStart) 기준 오프셋으로 조정
//            ArrayList<Integer> breakpoints = new ArrayList<>();
//            breakpoints.add(0);
//
//            if (txProfileSchedule != null) {
//                int txRemaining = (int) Math.max(0, txProfileDuration - txProfileElapsed);
//                if (txRemaining > 0 && txRemaining < sDuration && !breakpoints.contains(txRemaining))
//                    breakpoints.add(txRemaining);
//                JSONArray txPeriods = txProfileSchedule.getJSONArray("chargingSchedulePeriod");
//                for (int j = 0; j < txPeriods.length(); j++) {
//                    int sp = txPeriods.getJSONObject(j).getInt("startPeriod");
//                    int adj = (int)(sp - txProfileElapsed);
//                    if (adj > 0 && adj < sDuration && !breakpoints.contains(adj)) breakpoints.add(adj);
//                }
//            }
//
//            for (JSONObject csProfile : defaultProfileList) {
//                JSONObject sched = csProfile.getJSONObject("chargingSchedule");
//                long defElapsed = sched.has("startSchedule") ? parseElapsedSec(sched.getString("startSchedule"), nowMillis) : 0;
//                int dur = sched.has("duration") ? sched.getInt("duration") : sDuration;
//                int remaining = (int) Math.max(0, dur - defElapsed);
//                if (remaining > 0 && remaining < sDuration && !breakpoints.contains(remaining)) breakpoints.add(remaining);
//                JSONArray periods = sched.getJSONArray("chargingSchedulePeriod");
//                for (int j = 0; j < periods.length(); j++) {
//                    int sp = periods.getJSONObject(j).getInt("startPeriod");
//                    int adj = (int)(sp - defElapsed);
//                    if (adj > 0 && adj < sDuration && !breakpoints.contains(adj)) breakpoints.add(adj);
//                }
//            }
//
//            if (chargingCurrentData.maxProfileSchedulePeriod != null) {
//                int maxDur = chargingCurrentData.getMaxProfileDuration();
//                int maxRemaining = (int) Math.max(0, maxDur - maxProfileElapsed);
//                if (maxRemaining > 0 && maxRemaining < sDuration && !breakpoints.contains(maxRemaining))
//                    breakpoints.add(maxRemaining);
//                for (String s : chargingCurrentData.maxProfileSchedulePeriod) {
//                    int sp = new JSONObject(s).getInt("startPeriod");
//                    int adj = (int)(sp - maxProfileElapsed);
//                    if (adj > 0 && adj < sDuration && !breakpoints.contains(adj)) breakpoints.add(adj);
//                }
//            }
//
//            Collections.sort(breakpoints);
//
//            // 4. Composite Schedule 계산: TxProfile > TxDefaultProfile(stackLevel 높은 순) > MaxProfile ceiling
//            JSONArray jArray = new JSONArray();
//            double prevLimit = -1;
//            int txRemaining = txProfileSchedule != null ? (int) Math.max(0, txProfileDuration - txProfileElapsed) : 0;
//
//            for (int t : breakpoints) {
//                if (t >= sDuration) break;
//                double winningLimit = -1;
//                boolean found = false;
//
//                // TxProfile 우선 (OCPP: TxProfile > TxDefaultProfile)
//                if (txProfileSchedule != null && t < txRemaining) {
//                    JSONArray txPeriods = txProfileSchedule.getJSONArray("chargingSchedulePeriod");
//                    for (int j = txPeriods.length() - 1; j >= 0; j--) {
//                        JSONObject period = txPeriods.getJSONObject(j);
//                        if (period.getInt("startPeriod") <= t + txProfileElapsed) {
//                            double raw = period.getDouble("limit");
//                            winningLimit = maxProfileCeiling < Double.MAX_VALUE ? Math.min(raw, maxProfileCeiling) : raw;
//                            found = true;
//                            break;
//                        }
//                    }
//                }
//
//                // TxDefaultProfile: stackLevel 높은 것부터 순서대로 적용 (만료된 프로파일 건너뜀)
//                if (!found) {
//                    for (JSONObject csProfile : defaultProfileList) {
//                        JSONObject sched = csProfile.getJSONObject("chargingSchedule");
//                        long defElapsed = sched.has("startSchedule") ? parseElapsedSec(sched.getString("startSchedule"), nowMillis) : 0;
//                        int dur = sched.has("duration") ? sched.getInt("duration") : sDuration;
//                        int remaining = (int) Math.max(0, dur - defElapsed);
//                        if (t >= remaining) continue; // 이 프로파일은 시간 t에서 만료
//
//                        JSONArray periods = sched.getJSONArray("chargingSchedulePeriod");
//                        for (int j = periods.length() - 1; j >= 0; j--) {
//                            JSONObject period = periods.getJSONObject(j);
//                            if (period.getInt("startPeriod") <= t + defElapsed) {
//                                double raw = period.getDouble("limit");
//                                winningLimit = maxProfileCeiling < Double.MAX_VALUE ? Math.min(raw, maxProfileCeiling) : raw;
//                                found = true;
//                                break;
//                            }
//                        }
//                        if (found) break;
//                    }
//                }
//
//                // Fallback: TxProfile/TxDefault 없을 때 ChargePointMaxProfile이 limit 제공
//                if (!found && chargingCurrentData.maxProfileSchedulePeriod != null) {
//                    int maxDur = chargingCurrentData.getMaxProfileDuration();
//                    int maxRem = (int) Math.max(0, maxDur - maxProfileElapsed);
//                    if (t < maxRem) {
//                        for (int j = chargingCurrentData.maxProfileSchedulePeriod.length - 1; j >= 0; j--) {
//                            JSONObject p = new JSONObject(chargingCurrentData.maxProfileSchedulePeriod[j]);
//                            if (p.getInt("startPeriod") <= t + maxProfileElapsed) {
//                                winningLimit = p.getDouble("limit");
//                                found = true;
//                                break;
//                            }
//                        }
//                    }
//                }
//                if (!found) continue;
//
//                if (winningLimit != prevLimit) {
//                    JSONObject entry = new JSONObject();
//                    entry.put("startPeriod", t);
//                    entry.put("limit", winningLimit);
//                    entry.put("numberPhases", 3);
//                    jArray.put(entry);
//                    prevLimit = winningLimit;
//                }
//            }
//
//            return jArray.length() > 0 ? jArray : null;
//        } catch (Exception e) {
//            logger.error("onChargingSchedulePeriodCurrentData error: {} {}", e.getClass().getSimpleName(), e.getMessage());
//        }
//        return null;
//    }

    private long parseElapsedSec(String startScheduleStr, long nowMillis) {
        if (startScheduleStr == null || startScheduleStr.isEmpty()) return 0;
        try {
            // Normalize: strip milliseconds, normalize ±HH:MM offset to ±HHMM for SimpleDateFormat
            String s = startScheduleStr.trim().replaceAll("\\.[0-9]+", "");
            s = s.replaceAll("([+-])(\\d{2}):(\\d{2})$", "$1$2$3");
            SimpleDateFormat sdf;
            if (s.endsWith("Z")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
            }
            long profileMillis = sdf.parse(s).getTime();
            return Math.max(0, (nowMillis - profileMillis) / 1000);
        } catch (Exception e) {
            logger.warn("parseElapsedSec failed for '{}': {}", startScheduleStr, e.getMessage());
            return 0;
        }
    }

    private boolean onSupportedFeatureProfiles(String key) {
        boolean result = false;
        try {
            String[] values = getConfigurationValue("SupportedFeatureProfiles").split(",");
            for (String value : values) {
                if (Objects.equals(key, value)) {
                    result = true;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }


    private JSONArray getCertification(CertificateUse certificateUse) {
        try {
            String filename = certificateUse == CertificateUse.CentralSystemRootCertificate ? "cert.pem" :
                    certificateUse == CertificateUse.ManufacturerRootCertificate ? "dongahtest.p-e.kr.crt" : null;
            File file = new File(GlobalVariables.getRootPath() + File.separator + filename);
            if (file.exists()) {
                X509Certificate certificatePem = loadCertificateFromFile(file);
                CertificateHashDataType certificateHashDataType = new CertificateHashDataType();
                certificateHashDataType.setSerialNumber(certificatePem.getSerialNumber().toString(16).toUpperCase());
                certificateHashDataType.setHashAlgorithm(HashAlgorithm.valueOf(certificatePem.getSigAlgName().substring(0, 6)));
                certificateHashDataType.setIssuerNameHash(getIssuerNameHash(certificatePem));
                SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(certificatePem.getPublicKey().getEncoded());
                certificateHashDataType.setIssuerKeyHash(calculateSHA256(spki.getPublicKeyData().getBytes()));
                // Class ==> JsonObject
                Gson gson = new Gson();
                JSONObject obj = new JSONObject(gson.toJson(certificateHashDataType));
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(obj);
                return jsonArray;
            }
        } catch (Exception e) {
            logger.error(" getCertification {} : ", e.getMessage());
        }
        return null;
    }

    private X509Certificate loadCertificateFromFile(File file) throws CertificateException, IOException {
        FileInputStream inputStream = new FileInputStream(file);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        inputStream.close();
        return certificate;
    }

    private X509Certificate loadCertificate(String pemCert) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pemCert.getBytes())) {
            return (X509Certificate) factory.generateCertificate(inputStream);
        }
    }

    @NonNull
    private String getCertificateValidity(@NonNull X509Certificate cert) {
        String notBefore = cert.getNotBefore().toString();
        String notAfter = cert.getNotAfter().toString();
        return String.format("Validity Period: \nStart Date: %s\nEnd Date: %s", notBefore, notAfter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean getCertificateValidity(@NonNull String cert) throws CertificateException {
        boolean result = false;
        InputStream inputStream = new ByteArrayInputStream(cert.getBytes());
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        String notBefore = certificate.getNotBefore().toString();
        String notAfter = certificate.getNotAfter().toString();
        String currentTime = zonedDateTimeConvert.doGetUtcDatetimeAsString();

        // Fri Dec 20 14:02:52 GMT+09:00 2024 ==> 2024-12-20T14:02:52
        final Set<ZoneId> PREFERRED_ZONES = Set.of(ZoneId.of("UTC"));
        final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
                .appendPattern("EEE MMM dd HH:mm:ss [O][")
                .appendZoneText(TextStyle.SHORT, PREFERRED_ZONES)
                .appendPattern("] yyyy")
                .toFormatter(Locale.ENGLISH);

        ZonedDateTime zdt = ZonedDateTime.parse(notBefore, PARSER);
        notBefore = zonedDateTimeConvert.zonedString(zdt);
        zdt = ZonedDateTime.parse(notAfter, PARSER);
        notAfter = zonedDateTimeConvert.zonedString(zdt);

        if (notBefore.compareTo(currentTime) < 0 && notAfter.compareTo(currentTime) > 0) {
            result = true;
        }
        return result;
    }

    @NonNull
    private String calculateSHA1(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    @NonNull
    private String calculateSHA256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    public String getIssuerName(X509Certificate cert) throws Exception {
        byte[] issuerDN = cert.getIssuerX500Principal().getEncoded();
        return calculateSHA1(issuerDN);
    }

    public String getIssuerNameHash(X509Certificate cert) throws Exception {
        byte[] issuerDN = cert.getIssuerX500Principal().getEncoded();
        return calculateSHA256(issuerDN);
    }




    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean verifySignature(PublicKey publicKey, byte[] signature, String firmwareFilePath) {
        boolean result = false;
        try {
            File firmwareFile = new File(firmwareFilePath);
            FileInputStream firmwareInputStream = new FileInputStream(firmwareFile);

//            byte[] firmwareData = firmwareInputStream.readAllBytes();   //java 9 이상
            byte[] firmwareData = readFileToByteArray(firmwareFile);
            firmwareInputStream.close();

            // Initialize Signature instance with RSA SSA-PSS
//            Signature sig = Signature.getInstance("RSASSA-PSS");
            Security.removeProvider("BC");
            Security.addProvider(new BouncyCastleProvider());
            Signature sig = Signature.getInstance("SHA256withRSA/PSS", "BC"); // BouncyCastle 사용
            PSSParameterSpec pssSpec = new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1);
            sig.setParameter(pssSpec);
            sig.initVerify(publicKey);
            sig.update(firmwareData); // Use original data, not its hash
            result = sig.verify(signature);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    private byte[] readFileToByteArray(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        fis.close();
        return bos.toByteArray();
    }


    @Override
    public void onGetFailure(WebSocket webSocket, Throwable t) {
        this.webSocket = webSocket;
        socket.setState(SocketState.RECONNECT_ATTEMPT);
        logger.error(t.toString());
    }

    /**
     * single connector Id
     *
     * @param actionName action name
     * @param request    request
     * @throws OccurenceConstraintException
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSend(String actionName, Request request) throws OccurenceConstraintException {
        if (!request.validate()) {
            logger.error("Can't send request: not validated. Payload {}: ", request);
            throw new OccurenceConstraintException();
        }
        try {
            String id = store(request);
            Object payload = packPayload(request);
            Object call = makeCall(id, actionName, payload);
            String actionNameCompare = null;
            if (call != null) {
                try {
                    this.webSocket.send(call.toString());
                    if (Objects.equals(actionName, "DataTransfer")) {
                        // DataTransfer 종류가 많아 ACTION_NAME 대신 MESSAGE_ID 를 Key 값으로 정의
                        // message_id 별로 parsing 해야 하는 부분이 있음.
                        JSONObject jsonObject = new JSONObject(payload.toString());
                        actionNameCompare = jsonObject.getString("messageId");
                        hashMapUuid.put(id, jsonObject.getString("messageId"));
                        logDataSave.makeLogDate(jsonObject.getString("messageId"), call.toString());
                    } else {
                        actionNameCompare = actionName;
                        hashMapUuid.put(id, actionName);
                        logDataSave.makeLogDate(actionName, call.toString());
                    }
                    // debug event listener register
                    if (socketMessageDebugListener != null) {
                        socketMessageDebugListener.onMessageReceiveDebugEvent(2, call.toString(), actionName);
                    }
                    logger.trace("Send a message : {}", call.toString());
                } catch (Exception e) {
                    //dump data
                    if (actionList.contains(actionNameCompare)) {
                        logDataSaveDump.makeDump(call.toString());
                    }
                    logDataSave.makeLogDate("<<send fail>>" + actionName, call.toString());
                    logger.error("send error  : {} ", e.toString());

                }
            }
        } catch (Exception e) {
            logger.error("onSend error  : {} ", e.toString());
        }
    }

    /**
     * multi connectorId
     *
     * @param connectorId connector id
     * @param actionName  action name
     * @param request     request
     * @throws OccurenceConstraintException
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSend(int connectorId, String actionName, Request request) throws OccurenceConstraintException {
        if (!request.validate()) {
            logger.error("multi Can't send request: not validated. Payload {}: ", request);
            throw new OccurenceConstraintException();
        }
        try {
            String id = store(request);
            Object payload = packPayload(request);
            Object call = makeCall(id, actionName, payload);
            String actionNameCompare = null;
            if (call != null) {
                try {
                    this.webSocket.send(call.toString());
                    SendHashMapObject sendHashMapObject = new SendHashMapObject();
                    sendHashMapObject.setConnectorId(connectorId);
                    if (Objects.equals(actionName, "DataTransfer")) {
                        // DataTransfer 종류가 많아 ACTION_NAME 대신 MESSAGE_ID 를 Key 값으로 정의
                        // message_id 별로 parsing 해야 하는 부분이 있음.
                        JSONObject jsonObject = new JSONObject(payload.toString());
                        actionNameCompare = jsonObject.getString("messageId");
                        sendHashMapObject.setActionName(jsonObject.getString("messageId"));
                        newHashMapUuid.put(id, sendHashMapObject);
                        logDataSave.makeLogDate(jsonObject.getString("messageId"), call.toString());
                    } else {
                        actionNameCompare = actionName;
                        sendHashMapObject.setActionName(actionName);
                        newHashMapUuid.put(id, sendHashMapObject);
                        logDataSave.makeLogDate(actionName, call.toString());
                    }
                    // debug event listener register
                    if (socketMessageDebugListener != null) {
                        socketMessageDebugListener.onMessageReceiveDebugEvent(2, call.toString(), actionName);
                    }
                    logger.trace("Send a message: {}", call.toString());
                } catch (Exception e) {
                    //dump data
                    if (actionList.contains(actionNameCompare)) {
                        logDataSaveDump.makeDump(call.toString());
                    }
                    logDataSave.makeLogDate("<<send fail>>" + actionName, call.toString());
                    logger.error("send error : {} ", e.toString());
                }
            }
        } catch (Exception e) {
            logger.error("onSend error : {} ", e.toString());
        }
    }

    /**
     * Dump data send (미전송 데이터)
     *
     * @param text json string
     */
    public void onSend(String text) {
        try {
            this.webSocket.send(text);
            Message message = parse(text);
            String uuid = message.getId();
            String actionName = message.getAction();
            if (Objects.equals(actionName, "DataTransfer")) {
                JSONObject jsonObject = new JSONObject(message.getPayload().toString());
                actionName = jsonObject.getString("messageId");
                hashMapUuid.put(uuid, actionName);
            } else {
                hashMapUuid.put(uuid, actionName);
            }
            LogDataSave logDataSave = new LogDataSave("log");
            logDataSave.makeLogDate(actionName, text);
            logger.trace(" Send a message : {}", message);
        } catch (Exception e) {
            logger.error(" onSend error : {} ", e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResultSend(String actionName, String uuid, Confirmation confirmation) throws OccurenceConstraintException {
        if (!confirmation.validate()) {
            logger.error("Can't send request:  not validated. Payload {}: ", confirmation);
            throw new OccurenceConstraintException();
        }
        try {
            Object call = makeCallResult(uuid, actionName, packPayload(confirmation));
            if (call != null) {
                this.webSocket.send(call.toString());
                logDataSave.makeLogDate(actionName, call.toString());
                logger.trace(" Send a message: {}", call);
            }
        } catch (Exception e) {
            logger.error("onResultSend : {}", e.getMessage());
        }
    }

    @Override
    public void onCall(String id, String action, Object payload) {
        logger.trace("Send a message: id : {}, action : {}, payload : {}", id, action, payload.toString());
    }


    public String store(Request request) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        return UUID.randomUUID().toString();
    }

    /**
     * handler --> message send
     *
     * @param messageType message Type
     * @param connectorId connector Id ( 0 : All  1: 1채널  2:2채널)
     * @param delayTime   delay time
     * @param idTag       idTag (idTag, smsTele)
     * @param Uuid        UUID
     * @param result      RESULT (결제성공여부), remote start/stop connectorId check,
     * @return msg
     */
    public android.os.Message onMakeHandlerMessage(int messageType, int connectorId, int delayTime, String idTag, String Uuid, String alarmCode, Boolean result) {
        try {
            android.os.Message msg = new android.os.Message();
            Bundle bundle = new Bundle();
            bundle.putInt("connectorId", connectorId);
            bundle.putInt("delay", delayTime);
            bundle.putString("idTag", idTag);
            bundle.putString("uuid", Uuid);
            bundle.putString("alarmCode", alarmCode);
            bundle.putBoolean("result", result);
            msg.setData(bundle);
            msg.what = messageType;
            return msg;
        } catch (Exception e) {
            logger.error("onMakeHandlerMessage error : {}", e.getMessage());
        }
        return null;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }


    public HashMap<String, Object> getNewHashMapUuid() {
        return newHashMapUuid;
    }

    public void setNewHashMapUuid(String uuid, SendHashMapObject newHashMapUuid) {
        this.newHashMapUuid.put(uuid, newHashMapUuid);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private double onFindUnitPrice(JSONArray jsonArray) {
        try {
            ZonedDateTime now = zonedDateTimeConvert.doGetCurrentTime();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String startAtStr = obj.getString("startAt");
                String endAtArStr = obj.getString("endAt");
                ZonedDateTime startAt = ZonedDateTime.parse(startAtStr, DateTimeFormatter.ISO_DATE_TIME);
                ZonedDateTime endAt = ZonedDateTime.parse(endAtArStr, DateTimeFormatter.ISO_DATE_TIME);

                if ((now.isEqual(startAt) || now.isAfter(startAt)) && (now.isBefore(endAt) || now.isEqual(endAt))) {
                    return obj.getDouble("price");
                }
            }
        } catch (Exception e){
            logger.error(" onFindUnitPrice error : {}", e.getMessage());
        }
        return 0;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendSecurityEventNotification(String eventType, String techInfo) {
        try {
            ZonedDateTime timestamp = zonedDateTimeConvert.doZonedDateTimeToDatetime();
            SecurityEventNotificationRequest securityEventNotificationRequest = new SecurityEventNotificationRequest(eventType, timestamp);
            securityEventNotificationRequest.setTechInfo(techInfo);
            onSend(100, securityEventNotificationRequest.getActionName(), securityEventNotificationRequest);
        } catch (Exception e) {
            logger.error("Failed to send SecurityEventNotification", e);
        }
    }
}

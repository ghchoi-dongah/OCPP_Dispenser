package com.dongah.dispenser.basefunction;

import android.os.Environment;

import com.dongah.dispenser.websocket.ocpp.localauthlist.UpdateStatus;
import com.dongah.dispenser.websocket.ocpp.security.HashAlgorithm;

import java.io.File;

public class GlobalVariables {

    //storage/emulated/0/Android/media/files
//    public static String ROOT_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "Android" +
//            File.separator + "media" + File.separator + "files";

    public static String ROOT_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "Download";

    public static final String PACKAGE_NAME=  "com.dongah.autoupdates";
    public static final String PACKAGE_CLASS_NAME = "com.dongah.autoupdates.MainActivity";

    public static String VERSION = "DEVD 1.0.0";
    public static String FW_VERSION = "1.0.0";

    /**
     * Max plug count
     */
    public static int maxChannel = 2;
    public static int maxPlugCount = 3;
    public static boolean CONNECT_RETRY = false;
    public static boolean[] ChargerOperation = new boolean[maxPlugCount];

    public static final String UNIT_FILE_NAME = "unitPrice.dongah";

    /**
     * Handler message type
     */
    public static final int MESSAGE_HANDLER_BOOT_NOTIFICATION = 100;
    public static final int MESSAGE_HANDLER_HEART_BEAT = 101;
    public static final int MESSAGE_HANDLER_STATUS_NOTIFICATION_BOOT = 102;
    public static final int MESSAGE_HANDLER_STATUS_NOTIFICATION = 103;
    public static final int MESSAGE_HANDLER_GET_PRICE = 104;
    public static final int MESSAGE_HANDLER_START_TRANSACTION = 106;
    public static final int MESSAGE_HANDLER_PAY_INFO = 107;
    public static final int MESSAGE_HANDLER_PARTIAL_CANCEL = 108;
    public static final int MESSAGE_HANDLER_STOP_TRANSACTION = 109;
    public static final int MESSAGE_HANDLER_REMOTE_START_TRANSACTION = 110;
    public static final int MESSAGE_HANDLER_REMOTE_STOP_TRANSACTION = 111;
    public static final int MESSAGE_HANDLER_RESET = 112;
    public static final int MESSAGE_HANDLER_AUTHORIZE = 113;
    public static final int MESSAGE_HANDLER_RESULT_PRICE = 114;
    public static final int MESSAGE_HANDLER_SMS_MESSAGE = 115;
    public static final int MESSAGE_HANDLER_ANNOUNCE = 116;
    public static final int MESSAGE_HANDLER_TRIGGER_MESSAGE = 117;
    public static final int MESSAGE_CHANGE_CONFIGURATION = 121;
    public static final int MESSAGE_CHANGE_AVAILABILITY = 122;
    public static final int MESSAGE_GET_CONFIGURATION = 123;
    public static final int MESSAGE_CLEAR_CACHE = 124;
    public static final int MESSAGE_HANDLER_CHANGE_AVAILABILITY = 125;
    public static final int MESSAGE_SEND_LOCAL_LIST = 126;
    public static final int MESSAGE_CLEAR_CHARGING_PROFILE = 127;

    public static final int MESSAGE_HANDLER_LOCAL_LIST = 128;
    public static final int MESSAGE_HANDLER_FIRMWARE_STATUS = 129;


    //humax
    public static final int MESSAGE_CUSTOM_UNIT_PRICE = 130;
    public static final int MESSAGE_CUSTOM_STATUS_NOTIFICATION = 131;
    public static final int MESSAGE_HANDLER_STATUS_NOTIFICATION_ONCE = 132;

    /**
     * v-cat handler message
     */
    public static final int MESSAGE_PERMISSION = 600;
    public static final int MESSAGE_CHANGE_KEY = 700;
    public static final int MESSAGE_COM = 800;

    /**
     * ocpp
     */
    public static boolean reconnectCheck = false;
    public static String configurationKey = "";
    public static boolean notSupportedKey = false;
    public static int statusNotificationDelay = 180;
    public static int ConnectionTimeOut = 60;
    public static int MeterValueSampleInterval = 60;
    public static int HeartBeatInterval = 60;
    public static int MinimumStatusDuration = 60;
    public static boolean LocalAuthListEnabled = false;
    public static boolean AuthorizeRemoteTxRequests = false;
    public static boolean ReserveConnectorZeroSupported = true;
    public static boolean LocalPreAuthorize = false;
    public static boolean StopTransactionOnInvalidId = false;
    public static boolean AllowOfflineTxForUnknownId = false;
    public static boolean StopTransactionOnEVSideDisconnect = true;
    public static boolean UnlockConnectorOnEVSideDisconnect = true;
    public static UpdateStatus updateStatus;
    public static int ClockAlignedDataInterval = 0;
    public static boolean LocalAuthorizeOffline = false;
    public static String AuthorizationKey = "";
    public static String SecurityProfile = "0";


    //Humax Configuration Key add
    public static String HmChargerMode = "FEE";
    public static String HmSoundVolumn = "8";
    public static String HmConfigPasswd = "";
    public static String HmCharingLimitFee = "";
    public static String HmChargingTranTerm = "";           //완속 120, 급속 60
    public static String HmPreparingTranTerm = "";          //충전대기중 주기보고간격(초)
    public static String HmCpConfigVersion = "";
    public static String HmAuthExpiredTerm = "";
    public static String HmPreFee = "";                     // 선결제(완충기준) 설정금액

    //HumaxEv
    public static String humaxClientId;
    public static String humaxPassWd;
    public static String humaxUserType = "A";

    public static int dumpTransactionId = 0;
    public static boolean dumpPendingStop = false;
    public static boolean dumpChargingNotifSent = false;  // dump에서 StatusNoti(Charging) 전송 시 conf 핸들러 중복 방지
    public static boolean pendingBootAfterDump = false;   // dump 처리 완료 후 BootNoti/Heartbeat 시작 대기
    public static boolean dumpPendingFinishing = false;   // dump StopTx 전송 후 conf 수신 전까지 Finishing 대기
    public static int dumpFinishingConnectorId = 0;       // dumpPendingFinishing 대상 커넥터

    //Certification
    public static HashAlgorithm hashAlgorithm;
    public static String serialNumber;
    public static String issuerNameHash;
    public static String issuerKeyHash;

    public static int requestId;
    public static String signature;
    public static String signingCertificate;
    public static String firmwareFileName = "";

    public static short totalPackets;


    public static String startSchedule;

    public static boolean triggerBootNotification = false;

    public static boolean SecurityEventNotification = false;
    public static boolean InvalidCSMSCertificate = false;
    //modem tel number
    public static String IMSI = "" ;
    public static String RSRP = "";
    public static boolean CustomUnitPriceReq = false;

    public static String getRootPath() {
        return ROOT_PATH;
    }

    public static void setRootPath(String rootPath) {
        ROOT_PATH = rootPath;
    }

    public static String getVERSION() {
        return VERSION;
    }

    public static void setVERSION(String VERSION) {
        GlobalVariables.VERSION = VERSION;
    }

    public static String getFwVersion() {
        return FW_VERSION;
    }

    public static void setFwVersion(String fwVersion) {
        FW_VERSION = fwVersion;
    }

    public static int getMaxChannel() {
        return maxChannel;
    }

    public static void setMaxChannel(int maxChannel) {
        GlobalVariables.maxChannel = maxChannel;
    }

    public static int getMaxPlugCount() {
        return maxPlugCount;
    }

    public static void setMaxPlugCount(int maxPlugCount) {
        GlobalVariables.maxPlugCount = maxPlugCount;
    }

    public static boolean isConnectRetry() {
        return CONNECT_RETRY;
    }

    public static void setConnectRetry(boolean connectRetry) {
        CONNECT_RETRY = connectRetry;
    }

    public static boolean isReconnectCheck() {
        return reconnectCheck;
    }

    public static void setReconnectCheck(boolean reconnectCheck) {
        GlobalVariables.reconnectCheck = reconnectCheck;
    }

    public static String getConfigurationKey() {
        return configurationKey;
    }

    public static void setConfigurationKey(String configurationKey) {
        GlobalVariables.configurationKey = configurationKey;
    }

    public static boolean isNotSupportedKey() {
        return notSupportedKey;
    }

    public static void setNotSupportedKey(boolean notSupportedKey) {
        GlobalVariables.notSupportedKey = notSupportedKey;
    }

    public static int getStatusNotificationDelay() {
        return statusNotificationDelay;
    }

    public static void setStatusNotificationDelay(int statusNotificationDelay) {
        GlobalVariables.statusNotificationDelay = statusNotificationDelay;
    }

    public static int getConnectionTimeOut() {
        return ConnectionTimeOut;
    }

    public static void setConnectionTimeOut(int connectionTimeOut) {
        ConnectionTimeOut = connectionTimeOut;
    }

    public static int getMeterValueSampleInterval() {
        return MeterValueSampleInterval;
    }

    public static void setMeterValueSampleInterval(int meterValueSampleInterval) {
        MeterValueSampleInterval = meterValueSampleInterval;
    }

    public static int getHeartBeatInterval() {
        return HeartBeatInterval;
    }

    public static void setHeartBeatInterval(int heartBeatInterval) {
        HeartBeatInterval = heartBeatInterval;
    }

    public static int getMinimumStatusDuration() {
        return MinimumStatusDuration;
    }

    public static void setMinimumStatusDuration(int minimumStatusDuration) {
        MinimumStatusDuration = minimumStatusDuration;
    }

    public static boolean isLocalAuthListEnabled() {
        return LocalAuthListEnabled;
    }

    public static void setLocalAuthListEnabled(boolean localAuthListEnabled) {
        LocalAuthListEnabled = localAuthListEnabled;
    }


    public static boolean isAuthorizeRemoteTxRequests() {
        return AuthorizeRemoteTxRequests;
    }

    public static void setAuthorizeRemoteTxRequests(boolean authorizeRemoteTxRequests) {
        AuthorizeRemoteTxRequests = authorizeRemoteTxRequests;
    }

    public static boolean isReserveConnectorZeroSupported() {
        return ReserveConnectorZeroSupported;
    }

    public static void setReserveConnectorZeroSupported(boolean reserveConnectorZeroSupported) {
        ReserveConnectorZeroSupported = reserveConnectorZeroSupported;
    }

    public static boolean isLocalPreAuthorize() {
        return LocalPreAuthorize;
    }

    public static void setLocalPreAuthorize(boolean localPreAuthorize) {
        LocalPreAuthorize = localPreAuthorize;
    }

    public static boolean isStopTransactionOnInvalidId() {
        return StopTransactionOnInvalidId;
    }

    public static void setStopTransactionOnInvalidId(boolean stopTransactionOnInvalidId) {
        StopTransactionOnInvalidId = stopTransactionOnInvalidId;
    }

    public static boolean isAllowOfflineTxForUnknownId() {
        return AllowOfflineTxForUnknownId;
    }

    public static void setAllowOfflineTxForUnknownId(boolean allowOfflineTxForUnknownId) {
        AllowOfflineTxForUnknownId = allowOfflineTxForUnknownId;
    }

    public static boolean isStopTransactionOnEVSideDisconnect() {
        return StopTransactionOnEVSideDisconnect;
    }

    public static void setStopTransactionOnEVSideDisconnect(boolean stopTransactionOnEVSideDisconnect) {
        StopTransactionOnEVSideDisconnect = stopTransactionOnEVSideDisconnect;
    }

    public static boolean isUnlockConnectorOnEVSideDisconnect() {
        return UnlockConnectorOnEVSideDisconnect;
    }

    public static void setUnlockConnectorOnEVSideDisconnect(boolean unlockConnectorOnEVSideDisconnect) {
        UnlockConnectorOnEVSideDisconnect = unlockConnectorOnEVSideDisconnect;
    }

    public static int getClockAlignedDataInterval() {
        return ClockAlignedDataInterval;
    }

    public static void setClockAlignedDataInterval(int clockAlignedDataInterval) {
        ClockAlignedDataInterval = clockAlignedDataInterval;
    }

    public static boolean isLocalAuthorizeOffline() {
        return LocalAuthorizeOffline;
    }

    public static void setLocalAuthorizeOffline(boolean localAuthorizeOffline) {
        LocalAuthorizeOffline = localAuthorizeOffline;
    }

    public static String getAuthorizationKey() {
        return AuthorizationKey;
    }

    public static void setAuthorizationKey(String authorizationKey) {
        AuthorizationKey = authorizationKey;
    }

    public static HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    public static void setHashAlgorithm(HashAlgorithm hashAlgorithm) {
        GlobalVariables.hashAlgorithm = hashAlgorithm;
    }

    public static String getSerialNumber() {
        return serialNumber;
    }

    public static void setSerialNumber(String serialNumber) {
        GlobalVariables.serialNumber = serialNumber;
    }

    public static String getIssuerNameHash() {
        return issuerNameHash;
    }

    public static void setIssuerNameHash(String issuerNameHash) {
        GlobalVariables.issuerNameHash = issuerNameHash;
    }

    public static String getIssuerKeyHash() {
        return issuerKeyHash;
    }

    public static void setIssuerKeyHash(String issuerKeyHash) {
        GlobalVariables.issuerKeyHash = issuerKeyHash;
    }

    public static int getRequestId() {
        return requestId;
    }

    public static void setRequestId(int requestId) {
        GlobalVariables.requestId = requestId;
    }

    public static String getSignature() {

        return signature;
    }

    public static void setSignature(String signature) {
        GlobalVariables.signature = signature;
    }

    public static String getSigningCertificate() {
        return signingCertificate;
    }

    public static void setSigningCertificate(String signingCertificate) {
        GlobalVariables.signingCertificate = signingCertificate;
    }

    public static String getFirmwareFileName() {
        return firmwareFileName;
    }

    public static void setFirmwareFileName(String fileName) {
        GlobalVariables.firmwareFileName = fileName;
    }
    public static void setHumaxUserType(String humaxUserType) {
        GlobalVariables.humaxUserType = humaxUserType;
    }

    public static String getHmChargerMode() {
        return HmChargerMode;
    }

    public static void setHmChargerMode(String hmChargerMode) {
        HmChargerMode = hmChargerMode;
    }

    public static String getHmSoundVolumn() {
        return HmSoundVolumn;
    }

    public static void setHmSoundVolumn(String hmSoundVolumn) {
        HmSoundVolumn = hmSoundVolumn;
    }

    public static String getHmConfigPasswd() {
        return HmConfigPasswd;
    }

    public static void setHmConfigPasswd(String hmConfigPasswd) {
        HmConfigPasswd = hmConfigPasswd;
    }

    public static String getHmCharingLimitFee() {
        return HmCharingLimitFee;
    }

    public static void setHmCharingLimitFee(String hmCharingLimitFee) {
        HmCharingLimitFee = hmCharingLimitFee;
    }

    public static String getHmChargingTranTerm() {
        return HmChargingTranTerm;
    }

    public static void setHmChargingTranTerm(String hmChargingTranTerm) {
        HmChargingTranTerm = hmChargingTranTerm;
    }

    public static String getHmPreparingTranTerm() {
        return HmPreparingTranTerm;
    }

    public static void setHmPreparingTranTerm(String hmPreparingTranTerm) {
        HmPreparingTranTerm = hmPreparingTranTerm;
    }

    public static String getHmCpConfigVersion() {
        return HmCpConfigVersion;
    }

    public static void setHmCpConfigVersion(String hmCpConfigVersion) {
        HmCpConfigVersion = hmCpConfigVersion;
    }

    public static String getHmAuthExpiredTerm() {
        return HmAuthExpiredTerm;
    }

    public static void setHmAuthExpiredTerm(String hmAuthExpiredTerm) {
        HmAuthExpiredTerm = hmAuthExpiredTerm;
    }

    public static String getHmPreFee() {
        return HmPreFee;
    }

    public static void setHmPreFee(String hmPreFee) {
        HmPreFee = hmPreFee;
    }

    public static String getHumaxClientId() {
        return humaxClientId;
    }

    public static boolean[] getChargerOperation() {
        return ChargerOperation;
    }

    public static void setChargerOperation(boolean[] chargerOperation) {
        ChargerOperation = chargerOperation;
    }

    public static UpdateStatus getUpdateStatus() {
        return updateStatus;
    }

    public static void setUpdateStatus(UpdateStatus updateStatus) {
        GlobalVariables.updateStatus = updateStatus;
    }

    public static void setHumaxClientId(String humaxClientId) {
        GlobalVariables.humaxClientId = humaxClientId;
    }

    public static String getHumaxPassWd() {
        return humaxPassWd;
    }

    public static void setHumaxPassWd(String humaxPassWd) {
        GlobalVariables.humaxPassWd = humaxPassWd;
    }

    public static String getHumaxUserType() {
        return humaxUserType;
    }

    public static String getIMSI() {
        return IMSI;
    }

    public static void setIMSI(String IMSI) {
        GlobalVariables.IMSI = IMSI;
    }

    public static String getRSRP() {
        return RSRP;
    }

    public static void setRSRP(String RSRP) {
        GlobalVariables.RSRP = RSRP;
    }

    public static boolean isCustomUnitPriceReq() {
        return CustomUnitPriceReq;
    }

    public static void setCustomUnitPriceReq(boolean customUnitPriceReq) {
        CustomUnitPriceReq = customUnitPriceReq;
    }

    public static String getSecurityProfile() {
        return SecurityProfile;
    }

    public static void setSecurityProfile(String securityProfile) {
        SecurityProfile = securityProfile;
    }

    public static int getDumpTransactionId() {
        return dumpTransactionId;
    }

    public static void setDumpTransactionId(int dumpTransactionId) {
        GlobalVariables.dumpTransactionId = dumpTransactionId;
    }

    public static short getTotalPackets() {
        return totalPackets;
    }

    public static void setTotalPackets(short totalPackets) {
        GlobalVariables.totalPackets = totalPackets;
    }

    public static String getStartSchedule() {
        return startSchedule;
    }

    public static void setStartSchedule(String startSchedule) {
        GlobalVariables.startSchedule = startSchedule;
    }

    public static boolean isTriggerBootNotification() {
        return triggerBootNotification;
    }

    public static void setTriggerBootNotification(boolean triggerBootNotification) {
        GlobalVariables.triggerBootNotification = triggerBootNotification;
    }

    public static boolean isSecurityEventNotification() {
        return SecurityEventNotification;
    }

    public static void setSecurityEventNotification(boolean securityEventNotification) {
        SecurityEventNotification = securityEventNotification;
    }

    public static boolean isInvalidCSMSCertificate() {
        return InvalidCSMSCertificate;
    }

    public static void setInvalidCSMSCertificate(boolean invalidCSMSCertificate) {
        InvalidCSMSCertificate = invalidCSMSCertificate;
    }
}

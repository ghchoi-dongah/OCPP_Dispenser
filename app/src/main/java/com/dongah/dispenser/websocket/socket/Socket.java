package com.dongah.dispenser.websocket.socket;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.utils.FileManagement;
import com.dongah.dispenser.websocket.ocpp.utilities.Base64Util;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Handshake;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


public class Socket extends WebSocketListener {
    private static final Logger logger = LoggerFactory.getLogger(Socket.class);

    private static final String KEYSTORE_PATH = GlobalVariables.getRootPath() + File.separator  + "charging_station_keystore.bks";
    private static final String KEYSTORE_PASSWORD = "ocatool";
    private static final String TRUSTSTORE_PATH = GlobalVariables.getRootPath() + File.separator  + "charging_station_truststore.bks";
    private static final String TRUSTSTORE_PASSWORD = "ocatool";
    private static final String OCPP_SERVER_URL = "wss://ocpp-server.example.com:8443/ocpp/";


    private static final int MAX_COLLISION = 2;
    private SocketState state = SocketState.NONE;
    private int reconnectingAttempts;
    private String url;
    private WebSocket webSocket;
    private OkHttpClient client;
    private final Base64Util base64Util = new Base64Util();
    private boolean signedType;

    private static final ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();
    private static final FileManagement fileManagement = new FileManagement();
    private static final String FILE_NAME = "securityLog.dongah";
    /**
     * socket interface callback (New Class)
     */
    private static SocketInterface socketInterface = null;

    /**
     * Reconnect handler
     */
    private final Handler reconnectHandler = new Handler(Looper.getMainLooper());

    public SocketState getState() {
        return state;
    }

    public void setState(SocketState state) {
        this.state = state;
    }

    public Socket() {
        super();
    }


    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        try {
            super.onOpen(webSocket, response);
            setState(SocketState.OPEN);
            reconnectingAttempts = 0;
            socketInterface.onOpen(webSocket);
        } catch (Exception e) {
            logger.error("onOpen Error : {}", e.getMessage());
        }
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        try {
            super.onMessage(webSocket, text);
            socketInterface.onGetMessage(webSocket, text);
        } catch (Exception e) {
            logger.error("onMessage Error : {}", e.getMessage());
        }
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
        logger.info("receive byte : {}", bytes.hex());
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosing(webSocket, code, reason);
        setState(SocketState.CLOSING);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        socketInterface.onGetFailure(webSocket, t);
        setState(SocketState.CONNECT_ERROR);
        this.webSocket = null;
        saveFailureLog(webSocket, t, response);
        // TLS certificate validation failure (expired, notYetValid, unknownCA) →
        // send SecurityEventNotification on next successful reconnect
        if (signedType && isCertificateFailure(t)) {
            GlobalVariables.SecurityEventNotification = true;
        }
        scheduleReconnect();
    }

    private boolean isCertificateFailure(Throwable t) {
        Throwable cause = t;
        while (cause != null) {
            String name = cause.getClass().getName();
            String msg = cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";
            if (name.contains("SSLHandshakeException") || name.contains("CertificateException")
                    || name.contains("CertPathValidatorException")
                    || msg.contains("certificate") || msg.contains("pkix")
                    || msg.contains("expired") || msg.contains("not yet valid")
                    || msg.contains("unknown_ca") || msg.contains("handshake_failure")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveFailureLog(WebSocket webSocket,
                                Throwable t,
                                Response response) {
        try {
            JSONObject log = new JSONObject();

            log.put("time", zonedDateTimeConvert.doGetUtcDatetimeAsStringSimple());
            log.put("state", state.name());
            log.put("url", url);

            // Exception 정보
            log.put("exception", t.getClass().getSimpleName());
            log.put("message", t.getMessage());

            // HTTP / TLS 정보
            if (response != null) {
                log.put("httpCode", response.code());
                log.put("httpMessage", response.message());

                if (response.handshake() != null) {
                    log.put("tlsVersion", response.handshake().tlsVersion().javaName());
                    log.put("cipherSuite", response.handshake().cipherSuite().javaName());
                }
            }
            // JSON append 저장
            fileManagement.stringToFileSave(
                    GlobalVariables.getRootPath(),
                    FILE_NAME,
                    log.toString(),
                    true
            );
            logger.error("WebSocket Failure logged : {}", log.toString());
        } catch (Exception e) {
            logger.error("saveFailureLog error : {}", e.getMessage());
        }
    }

    private void connect(String url) {
        try {
            Request request;
            ChargerConfiguration chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            // SP1: Basic Auth + plain WS, SP2: Basic Auth + TLS, SP3: Client Cert + TLS (no Basic Auth)
            boolean needsBasicAuth = signedType && !Objects.equals(GlobalVariables.getSecurityProfile(), "3")
                    || Objects.equals(GlobalVariables.getSecurityProfile(), "1");
            if (needsBasicAuth) {
                //Basic <Based64encoded(chargerPointId:AuthorizationKey)>
                String connectionString = chargerConfiguration.getChargerId() + ":" + GlobalVariables.getAuthorizationKey();
                request = new Request.Builder()
                        .url(url)
                        .header("Accept", "application/json")
                        .addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                        .addHeader("Authorization","Basic " + base64Util.encode(connectionString))
                        .build();
            } else {
                request = new Request.Builder()
                        .url(url)
                        .header("Accept", "application/json")
                        .addHeader("Sec-WebSocket-Protocol", "ocpp1.6")
                        .build();
            }
            webSocket = client.newWebSocket(request, this);
        } catch (Exception e) {
            logger.error("connect fail {}", e.getMessage());
        }
    }

    private static final int MAX_RECONNECT_ATTEMPTS = 500;
    private static final long BASE_RECONNECT_DELAY_MS = 3000;

    private void scheduleReconnect() {
        reconnect();
    }

    private void reconnect() {
        if (reconnectingAttempts >= MAX_RECONNECT_ATTEMPTS) {
            logger.error("Reconnect max attempts reached");
            reconnectingAttempts = 0;
            return;
        }
        setState(SocketState.RECONNECT_ATTEMPT);

        long delay = BASE_RECONNECT_DELAY_MS * (reconnectingAttempts + 1);
        delay = Math.min(delay, 30000); // 최대 30초

        reconnectHandler.removeCallbacks(reconnectRunnable);
        reconnectHandler.postDelayed(reconnectRunnable, delay);
    }

    private final Runnable reconnectRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            if (state == SocketState.OPEN || state == SocketState.OPENING) return;

            setState(SocketState.RECONNECTING);
            logger.warn("WebSocket reconnect attempt : {}", reconnectingAttempts);

            try {
                reconnectingAttempts++;
                ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().onSocketInitialize();
            } catch (Exception e) {
                scheduleReconnect();
                logger.error("Reconnect error : {}", e.getMessage());
            }
        }
    };

    public synchronized void fullClose() {
        try {
            logger.warn("Socket FULL close");

            // WebSocket 종료
            if (webSocket != null) {
                webSocket.close(1000, "full-close");
                webSocket.cancel();
                webSocket = null;
            }

            // OkHttpClient 완전 폐기
            if (client != null) {
                client.dispatcher().executorService().shutdown();
                client.connectionPool().evictAll();
                client = null;
            }

            // 상태 초기화
            setState(SocketState.NONE);
            ((MainActivity) MainActivity.mContext).getProcessHandler().onHeartBeatStop();
        } catch (Exception e) {
            logger.error("fullClose error", e);
        }
    }

    public void disconnect() {
        try {
            if (webSocket != null) {
                webSocket.close(1000, "disconnect");
                webSocket = null;
            }
            closeClient();
        } catch (Exception e) {
            logger.error("disconnect error {}", e.getMessage());
        }
    }
    /**
     * blue ocpp web socket instance
     *
     * @param url server url (TLS 1.2)
     */
    public Socket(String url) {
        this.url = url;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getInstance(SocketInterface socketInterface) {
        try {
            if (webSocket == null) {
                setState(SocketState.OPENING);
                Socket.socketInterface = socketInterface;
                run(url);
            }

        } catch (Exception e) {
            logger.error("getInstance error : {}", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void run(String url) {
        try {
            String _sp = GlobalVariables.getSecurityProfile();
            signedType = Objects.equals(_sp, "2") || Objects.equals(_sp, "3");
            if (signedType) {
                // SP2: 서버 인증서만 검증 (truststore만 사용)
                // SP3: 클라이언트 인증서도 필요 (keystore + truststore)
                boolean isClientCert = Objects.equals(_sp, "3");
                File truststoreFile = new File(TRUSTSTORE_PATH);
                FileInputStream truststoreInputStream = truststoreFile.exists() ? new FileInputStream(truststoreFile) : null;
                SSLContext sslContext = isClientCert
                        ? createSSLContext(new FileInputStream(KEYSTORE_PATH), truststoreInputStream)
                        : createSSLContextTrustOnly(truststoreInputStream);

                FileInputStream truststoreInputStream2 = truststoreFile.exists() ? new FileInputStream(truststoreFile) : null;
                X509TrustManager trustManager = getTrustManager(truststoreInputStream2);

                //2025.12.09 add
                // Cipher + TLS 설정
                ConnectionSpec tlsSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .cipherSuites(
                                CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,  // 환경부 SP2 필수 cipher
                                CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384
                        )
                        .build();

                client = new OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                        .hostnameVerifier((hostname, session) -> {
                            try {
                                Certificate[] certs = session.getPeerCertificates();
                                X509Certificate cert = (X509Certificate) certs[0];
                                String dn = cert.getSubjectX500Principal().getName();
                                String cn = extractCn(dn);
                                // SAN 로깅 (이게 없으면 지금처럼 “CN은 맞는데 왜?” 상황이 계속 생김)
                                Collection<List<?>> sans = null;
                                try { sans = cert.getSubjectAlternativeNames(); } catch (Exception ignore) {}

                                String info = " "
                                        + "Host Name : "  + cert.getSubjectX500Principal().getName() + "\n"
                                        + "Subject DN : " + cert.getSubjectX500Principal().getName() + "\n"
                                        + "Issuer  DN : " + cert.getIssuerX500Principal().getName() + "\n"
                                        + "Serial No : " + cert.getSerialNumber().toString(16) + "\n"
                                        + "NotBefore: " + cert.getNotBefore() + "\n"
                                        + "NotAfter : " + cert.getNotAfter() + "\n"
                                        + "SigAlg   : " + cert.getSigAlgName();

                                doSecurityLogSave(String.format("FQDN_______ %s on %s%n%s",
                                        hostname , dn +"   cn:" + cn , "info : " + info));

                                // wildcard 차단
                                if ((cn != null && cn.contains("*")) || sanContainsWildcard(sans)) {
                                    markInvalid("Wildcard not allowed. hostname=" + hostname + ", CN=" + cn + ", SAN=" + sans);
                                    return false;
                                }

                                // SAN 우선 exact match
                                if (sans != null && !sans.isEmpty()) {
                                    if (!sanHasExactDns(sans, hostname)) {
                                        markInvalid("SAN DNS != FQDN exact-match. hostname=" + hostname + ", SAN=" + sans);
                                        return false;
                                    }
                                    return true;
                                }

                                // SAN 없으면 CN exact match
                                if (cn == null || !cn.equalsIgnoreCase(hostname)) {
                                    markInvalid("CN != FQDN exact-match. hostname=" + hostname + ", CN=" + cn);
                                    return false;
                                }

                                return true;


//                                if (cn == null || !cn.equalsIgnoreCase(hostname)) {
//                                    // xact match만 허용
//                                    GlobalVariables.SecurityEventNotification = true;
//                                    GlobalVariables.InvalidCSMSCertificate = true;
//                                    return true;
//                                }
//                                // wildcard 차단
//                                if (cn.contains("*")) {
//                                    GlobalVariables.SecurityEventNotification = true;
//                                    GlobalVariables.InvalidCSMSCertificate = true;
//                                    return true;
//                                }


//                                String normalizedDn = dn.replace(" ", "");
//                                if (!normalizedDn.contains("CN=65e499304b4143b0.octt.openchargealliance.org")) {
//                                    //dongahtest.p-e.kr
//                                    // 보안 이벤트 발생 처리
//                                    GlobalVariables.SecurityEventNotification = true;
//                                    GlobalVariables.InvalidCSMSCertificate = true;
//                                    return false; // 또는 true + 이벤트만 발생
//                                }

//                                return true;
                            } catch (Exception e) {
                                markInvalid("Verifier exception: " + e.getClass().getSimpleName() + " " + e.getMessage());
                                return false;
                            }
                        })
                        .connectionSpecs(Collections.singletonList(tlsSpec))
                        .protocols(Collections.singletonList(Protocol.HTTP_1_1))                //2025.12.09 add
                        .pingInterval(5, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(new SSLHandshakeInterceptor())
                        .addInterceptor(new LoggingInterceptor())
                        .build();
            } else {
                client = new OkHttpClient.Builder()
                        .pingInterval(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(new SSLHandshakeInterceptor())
                        .addInterceptor(new LoggingInterceptor())
                        .build();
            }
            closeClient();
            connect(url);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void closeClient() {
        try {
            if (webSocket != null) {
                webSocket.close(1000, "reconnect");
                webSocket = null;
            }
        } catch (Exception e) {
            logger.error("closeClient error : {}", e.getMessage());
        }
    }

    public static class SSLHandshakeInterceptor implements Interceptor {

        private static final String TAG = "OkHttp3-SSLHandshake";

        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            final Response response = chain.proceed(chain.request());
            printTlsAndCipherSuiteInfo(response);
            return response;
        }

        private void printTlsAndCipherSuiteInfo(Response response) {
            if (response != null) {
                Handshake handshake = response.handshake();
                if (handshake != null) {
                    final CipherSuite cipherSuite = handshake.cipherSuite();
                    final TlsVersion tlsVersion = handshake.tlsVersion();
                    logger.debug("TLS: {} , CipherSuite: {}", tlsVersion, cipherSuite);
                }
            }
        }
    }
    private static String extractCn(String dn) {
        if (dn == null) return null;
        for (String part : dn.split(",")) {
            part = part.trim();
            if (part.startsWith("CN=")) return part.substring(3);
        }
        return null;
    }
    private static boolean sanContainsWildcard(Collection<List<?>> sans) {
        if (sans == null) return false;
        for (List<?> san : sans) {
            Object value = san.get(1);
            if (value instanceof String && ((String) value).contains("*")) return true;
        }
        return false;
    }

    private static void markInvalid(String reason) {
        GlobalVariables.SecurityEventNotification = true;
        GlobalVariables.InvalidCSMSCertificate = true;
//        GlobalVariables.LastTlsFailReason = reason;
    }



    public String getConfigurationValue(String key) {
        String result = "none";
        try {
            FileManagement fileManagement = new FileManagement();
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



    static class LoggingInterceptor implements Interceptor {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.d("OkHttp", String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));
            //log save
            doSecurityLogSave(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));


            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.d("OkHttp", String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));
            //log save
            doSecurityLogSave(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));


            return response;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void doSecurityLogSave(String securityLog) {
        try {
            String startTime = zonedDateTimeConvert.doGetUtcDatetimeAsString();
            JSONArray data = insertData(startTime, securityLog);
            JSONObject obj = new JSONObject();
            try {
                obj.put("SecurityLogs", data);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
//            fileManagement.stringToFileSave(GlobalVariables.getRootPath(), FILE_NAME, obj.toString(), true);

        } catch (Exception e) {
            logger.error(" doSecurityLogSave error : {}", e.getMessage());
        }
    }

    public static JSONArray insertData(String startTime, String securityLog) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("startTime", startTime);
            jsonObject.put("securityLog", securityLog);
            return jsonArray.put(jsonObject);
        } catch (Exception e) {
            logger.error("insertData() : {}", e.getMessage());
        }
        return null;
    }

    /** SP2: truststore만 사용 (클라이언트 인증서 없이 서버 인증서만 검증)
     *  BKS 파일이 없으면 빈 KeyStore로 시작하고, cert.pem이 있으면 추가로 로드 */
    private SSLContext createSSLContextTrustOnly(InputStream truststoreInputStream) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("BKS");
        if (truststoreInputStream != null) {
            try {
                trustStore.load(truststoreInputStream, TRUSTSTORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                logger.warn("BKS truststore load failed, using empty store: {}", e.getMessage());
                trustStore.load(null, null);
            }
        } else {
            trustStore.load(null, null);
        }
        loadPemCertIntoStore(trustStore, "cert.pem", "csms-root");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    private SSLContext createSSLContext(InputStream keystoreInputStream, InputStream truststoreInputStream) throws Exception {
        // 키스토어 로드
        KeyStore keyStore = KeyStore.getInstance("BKS"); // 안드로이드에서는 BKS 형식 사용
        keyStore.load(keystoreInputStream, KEYSTORE_PASSWORD.toCharArray());

        // 트러스트스토어 로드
        KeyStore trustStore = KeyStore.getInstance("BKS");
        trustStore.load(truststoreInputStream, TRUSTSTORE_PASSWORD.toCharArray());

        // 키 매니저 설정
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        // 트러스트 매니저 설정
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // SSL 컨텍스트 설정
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }


    private X509TrustManager getTrustManager(InputStream truststoreInputStream) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("BKS");
        if (truststoreInputStream != null) {
            try {
                trustStore.load(truststoreInputStream, TRUSTSTORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                logger.warn("BKS getTrustManager load failed, using empty store: {}", e.getMessage());
                trustStore.load(null, null);
            }
        } else {
            trustStore.load(null, null);
        }
        loadPemCertIntoStore(trustStore, "cert.pem", "csms-root");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
    }

    /** cert.pem 파일이 존재하면 KeyStore에 추가 (InstallCertificate로 설치된 CA 인증서 활용) */
    private void loadPemCertIntoStore(KeyStore store, String filename, String alias) {
        try {
            File pemFile = new File(GlobalVariables.getRootPath() + File.separator + filename);
            if (!pemFile.exists()) return;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (FileInputStream fis = new FileInputStream(pemFile)) {
                X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
                store.setCertificateEntry(alias, cert);
                logger.info("Loaded cert from {} into truststore (alias={})", filename, alias);
            }
        } catch (Exception e) {
            logger.error("loadPemCertIntoStore({}) error: {}", filename, e.getMessage());
        }
    }
    private static boolean sanHasExactDns(Collection<List<?>> sans, String hostname) {
        for (List<?> san : sans) {
            Integer type = (Integer) san.get(0);
            Object value = san.get(1);
            if (type != null && type == 2 && value instanceof String) { // DNSName == 2
                if (((String) value).equalsIgnoreCase(hostname)) return true;
            }
        }
        return false;
    }


}

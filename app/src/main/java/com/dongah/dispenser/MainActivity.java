package com.dongah.dispenser;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.os.Looper;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.ConfigurationKeyRead;
import com.dongah.dispenser.basefunction.FocusChangeEnabled;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.FragmentCurrent;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.controlboard.ControlBoard;
import com.dongah.dispenser.handler.ProcessHandler;
import com.dongah.dispenser.rfcard.RfCardReaderReceive;
import com.dongah.dispenser.utils.MonitorHttpServer;
import com.dongah.dispenser.utils.SftpRxJava;
import com.dongah.dispenser.utils.ToastPositionMake;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.socket.Connector;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;
import com.dongah.dispenser.websocket.tcpsocket.ClientSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {


    public static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final String PACKAGE_NAME = "com.dongah.dispenser";

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;


    TextView textViewVersionValue, textViewTime;
    ImageView imgNetwork;
    Runnable runnable;
    Handler handler = new Handler();


    ChargerConfiguration chargerConfiguration;
    ClassUiProcess[] classUiProcess;
    ChargingCurrentData[] chargingCurrentData;
    UiSeq[] fragmentSeq;
    ToastPositionMake toastPositionMake;
    SocketReceiveMessage socketReceiveMessage;
    FragmentChange fragmentChange;
    ProcessHandler processHandler;

    ControlBoard controlBoard;
    RfCardReaderReceive rfCardReaderReceive;

    SftpRxJava sftpRxJava;
    ConfigurationKeyRead configurationKeyRead;
    /**
     * current fragment Exception check
     */
    FragmentCurrent fragmentCurrent;
    List<Connector> connectorList = new ArrayList<>();

    ClientSocket clientSocket;
    MonitorHttpServer monitorHttpServer;


    public ToastPositionMake getToastPositionMake() {
        return toastPositionMake;
    }
    public RfCardReaderReceive getRfCardReaderReceive() {
        return rfCardReaderReceive;
    }

    public UiSeq getFragmentSeq(int ch) {
        return fragmentSeq[ch];
    }

    public void setFragmentSeq(int ch, UiSeq fragmentSeq) {
        this.fragmentSeq[ch] = fragmentSeq;
    }

    public SocketReceiveMessage getSocketReceiveMessage() {
        return socketReceiveMessage;
    }

    public FragmentChange getFragmentChange() {
        return fragmentChange;
    }

    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    public ControlBoard getControlBoard() {
        return controlBoard;
    }


    public ChargerConfiguration getChargerConfiguration() {
        return chargerConfiguration;
    }

    public ChargingCurrentData getChargingCurrentData(int ch) {
        return chargingCurrentData[ch];
    }

    public void setChargingCurrentData(ChargingCurrentData[] chargingCurrentData) {
        this.chargingCurrentData = chargingCurrentData;
    }

    public ClassUiProcess[] getClassUiProcess() {
        return classUiProcess;
    }

    public ClassUiProcess getClassUiProcess(int ch) {
        return classUiProcess[ch];
    }

    public ConfigurationKeyRead getConfigurationKeyRead() {
        return configurationKeyRead;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hideNavigationBar();
        mContext = this;

        /* 슬립 모드 방지*/
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /* 세로 고정 */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        imgNetwork = findViewById(R.id.imgNetwork);
        textViewTime = findViewById(R.id.textViewTime);
        textViewVersionValue = findViewById(R.id.textViewVersionValue);

        fragmentCurrent = new FragmentCurrent();

        toastPositionMake = new ToastPositionMake(this);

        // Android 11 이상에서 외부 저장소 전체 접근 권한 확인
//        File externalFilesDir = getExternalFilesDir(null);
//        if (externalFilesDir != null) {
//            GlobalVariables.setRootPath(externalFilesDir.getAbsolutePath());
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
//            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
//                    Uri.parse("package:" + getPackageName()));
//            startActivity(intent);
//            return;
//        }

        // 1. charger configuration (config 파일 우선 로드 → SecurityProfile 기본값 설정)
        chargerConfiguration = new ChargerConfiguration();
        chargerConfiguration.onLoadConfiguration();
        textViewVersionValue.setText("VER-DEVD " + GlobalVariables.getVERSION() + " | ");

        // ConfigurationKey read (OCPP로 변경된 값이 있으면 GlobalVariables 덮어씀)
        configurationKeyRead = new ConfigurationKeyRead();
        configurationKeyRead.onRead();

        // 2. fragment change management */
        fragmentChange = new FragmentChange();
        fragmentSeq = new UiSeq[GlobalVariables.maxChannel];
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            fragmentChange.onFragmentChange(i, UiSeq.INIT, "INIT", "");
            fragmentChange.onFragmentHeaderChange(i, "Header");
        }

        // 3. Control board
        controlBoard = new ControlBoard(GlobalVariables.maxChannel, chargerConfiguration.getControlCom());
        // 4. rf card reade : MID = terminal ID */
        rfCardReaderReceive = new RfCardReaderReceive(chargerConfiguration.getRfCom());

        // Control Rx/Tx
        monitorHttpServer = new MonitorHttpServer(8080);
        monitorHttpServer.start();

        // 5. Handler */
        processHandler = new ProcessHandler(chargerConfiguration);

        // 6. ChargerOperate read
        onChargerOperate();

        // TEST 동아 서버
        /** security profile
        *  0: No Security (WS)
         * 1: Unsecured (WS)
         * 2: TLS (WSS)
         * 3: TLS + Client Certificate (WSS)
         * */
        String _sp = GlobalVariables.getSecurityProfile();
        String baseUrl = (Objects.equals(_sp, "2") || Objects.equals(_sp, "3") ? "wss://" : "ws://") +
                chargerConfiguration.getServerConnectingString() + ":" + chargerConfiguration.getServerPort() +
                "/" + chargerConfiguration.getChargerId();

        //스마트 그리드 테스트용
//        String baseUrl =  (GlobalVariables.getSecurityProfile().equals("2") ? "wss://" : "ws://") +
//                chargerConfiguration.getServerConnectingString() + ":" +
//                chargerConfiguration.getServerPort() + "/" + chargerConfiguration.getChargerId() ;

        socketReceiveMessage = new SocketReceiveMessage(baseUrl);


        // 7. classUiProcess */
        classUiProcess = new ClassUiProcess[GlobalVariables.maxChannel];
        chargingCurrentData = new ChargingCurrentData[GlobalVariables.maxChannel];
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            classUiProcess[i] = new ClassUiProcess(i);
            chargingCurrentData[i] = new ChargingCurrentData(i);
            chargingCurrentData[i].onCurrentDataClear();
        }


        //Diagnostics thread start
        processHandler.onDiagnosticsStart(120);
        // 전류 제한 설정
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
//            controlBoard.getTxData(i).setOutPowerLimit((short) chargerConfiguration.getDr());
            controlBoard.getTxData(i).setOutPowerLimit((short) 70);
        }
//        if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) sendOcppAuthInfoRequest();
    }


    @Override
    protected void onStart() {
        super.onStart();

        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                // 1초마다 실행
                handler.postDelayed(this, 1000);
                try {
                    if (socketReceiveMessage.getSocket().getState() != null) {
                        imgNetwork.setBackgroundResource(socketReceiveMessage.getSocket().getState() == SocketState.OPEN ?
                                R.drawable.network : R.drawable.nonetwork);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        };
        runnable.run();
    }

    private void updateTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String currentTime = sdf.format(new Date());
            textViewTime.setText(currentTime);
        } catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        try {
            // channel argument check
            for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                if (fragmentCurrent.getCurrentFragment(i) instanceof FocusChangeEnabled) {
                    ((FocusChangeEnabled) fragmentCurrent.getCurrentFragment(i)).onWindowFocusChanged(hasFocus);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void onRebooting(String type) {
        try {
            ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().getSocket().disconnect();
            if (Objects.equals(type, "Soft")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(GlobalVariables.PACKAGE_NAME, GlobalVariables.PACKAGE_CLASS_NAME));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent); // 새 앱 실행
                    overridePendingTransition(0, 0);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        ActivityCompat.finishAffinity(MainActivity.this); // 모든 액티비티 종료
                        System.exit(0);
                    }, 100); // 200ms 딜레이
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            } else {
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                powerManager.reboot("reboot");
            }
        } catch (Exception e) {
            logger.error("onRebooting : {}", e.getMessage());
        }
    }

    /**
     * ui version update
     */
    public void onRebooting() {
        try {
            boolean result = false;
            // TODO : check
            for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                result = chargingCurrentData[i].isReBoot() && (getClassUiProcess(i).getUiSeq() == UiSeq.INIT);
            }

            if (result) {
                for (int i = 0; i < GlobalVariables.maxChannel; i++) {
                    getClassUiProcess(i).setUiSeq(UiSeq.REBOOTING);
                    chargingCurrentData[i].setStopReason(Reason.Reboot);
                }
            }

        } catch (Exception e) {
            logger.error(" version reboot : {}", e.getMessage());
        }
    }

    private void onChargerOperate() {
        File file = new File(GlobalVariables.getRootPath() + File.separator + "ChargerOperate");
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                int count = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    GlobalVariables.ChargerOperation[count] = Objects.equals(line, "true");
                    count++;
                }
            } catch (IOException e) {
                Arrays.fill(GlobalVariables.ChargerOperation, true);
                logger.error("ChargerOperate read error : {}", e.getMessage());
            }
        } else {
            Arrays.fill(GlobalVariables.ChargerOperation, true);
        }
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Custom status notification stop
        for (int i = 0; i < GlobalVariables.maxChannel; i++) {
            processHandler.onDiagnosticsStop();
        }
        handler.removeCallbacks(runnable); // 메모리 누수 방지
        if (monitorHttpServer != null) {
            monitorHttpServer.stopServer();
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();

        if (view != null) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];

            if (ev.getAction() == MotionEvent.ACTION_UP &&
                    (x < view.getLeft() || x >= view.getRight() ||
                            y < view.getTop() || y > view.getBottom())) {

                // 키보드 내리기
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // EditText 포커스 제거
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    public void setRequestedOrientation(int screenOrientationUnspecified) {
    }
}
package com.dongah.dispenser.pages;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargerPointType;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.TariffFileUpdater;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.SharedModel;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(InitFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    Animation animBlink;
    View viewCircle;
    SharedModel sharedModel;
    TextView textViewConnector, textViewInitMessage, txtMemberUnitInput;
    ImageView imageViewCar;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    String[] requestStrings = new String[1];

    Handler unitPriceHandler;
    TariffFileUpdater tariffFileUpdater;

    public InitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InitFragment newInstance(String param1, String param2) {
        InitFragment fragment = new InitFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mChannel = getArguments().getInt(CHANNEL);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        animBlink = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);
        viewCircle = view.findViewById(R.id.viewCircle);
        viewCircle.setOnClickListener(this);
        textViewConnector = view.findViewById(R.id.textViewConnector);
        imageViewCar = view.findViewById(R.id.imageViewCar);
        textViewInitMessage = view.findViewById(R.id.textViewInitMessage);
        textViewInitMessage.startAnimation(animBlink);
        txtMemberUnitInput = view.findViewById(R.id.txtMemberUnitInput);

        try {
            if (mChannel == 0) {
                imageViewCar.setScaleX(1f);
                textViewConnector.setText(R.string.leftConnector);
            } else {
                imageViewCar.setScaleX(-1f);
                textViewConnector.setText(R.string.rightConnector);
            }
        } catch (Exception e) {
            logger.error("InitFragment onCreateView error : {}", e.getMessage());
        }

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
            tariffFileUpdater = new TariffFileUpdater();
            //사용 단가 display
            if (onUnitPrice()) {
                try {
                    String newPrice = tariffFileUpdater.getPrice("A").replace(".0","");
                    txtMemberUnitInput.setText(getString(R.string.chargeUnitFormat, String.valueOf(newPrice)));
                } catch (Exception e) {
                    logger.error(" getPrice error : {}", e.getMessage());
                }
            }
            onUnitPriceDisplay();
            // home image
            sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            requestStrings[0] = String.valueOf(0);
            sharedModel.setMutableLiveData(requestStrings);
        } catch (Exception e) {
            logger.error("InitFragment onViewCreated : {}", e.getMessage());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onClick(View v) {
        try {
            if (!Objects.equals(v.getId(), R.id.viewCircle)) return;

            // 초기 화면 으로 전환이 된 경우, current data clear
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
            if (chargingCurrentData.getReservedStatus() != ChargePointStatus.Reserved) {
                chargingCurrentData.onCurrentDataClear();
            }
            chargingCurrentData.setConnectorId(mChannel + 1);

            int id = v.getId();

            //* page change*/
            ((MainActivity) getActivity()).getChargingCurrentData(mChannel).setChargerPointType(ChargerPointType.COMBO);
            ((MainActivity) getActivity()).getChargingCurrentData(mChannel).setConnectorId(mChannel + 1);

            if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                try {
                    if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.AUTH_SELECT);
                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.AUTH_SELECT, "AUTH_SELECT", null);
                    } else if (Objects.equals(chargerConfiguration.getAuthMode(), "4")) {
                        double testPrice = Double.parseDouble(((MainActivity) MainActivity.mContext).getChargerConfiguration().getTestPrice());
                        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).getChargingCurrentData().setPowerUnitPrice(testPrice);
                        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.AUTH_SELECT);
                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.AUTH_SELECT, "AUTH_SELECT", null);
                    } else {
                        double testPrice = Double.parseDouble(((MainActivity) MainActivity.mContext).getChargerConfiguration().getTestPrice());
                        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).getChargingCurrentData().setPowerUnitPrice(testPrice);
                        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                    }

//                    SocketState socketState =  ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().getSocket().getState();
//                    if (Objects.equals(socketState, SocketState.OPEN)) {
//                        ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.AUTH_SELECT);
//                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.AUTH_SELECT, "AUTH_SELECT", null);
//                    } else {
//                        Toast.makeText(getActivity(), "서버 연결 DISCONNECT. \n충전을 할 수 없습니다.", Toast.LENGTH_SHORT).show();
//                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.mContext, "서버 socket이 생성되지 않습니다.", Toast.LENGTH_SHORT).show();
                    logger.error(e.getMessage());
                }
            } else if (Objects.equals(chargerConfiguration.getAuthMode(), "4")) {
                // local 회원 인증용
                double testPrice = Double.parseDouble(((MainActivity) MainActivity.mContext).getChargerConfiguration().getTestPrice());
                ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel).setPowerUnitPrice(testPrice);
                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.MEMBER_CARD);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
            } else {
                double testPrice = Double.parseDouble(((MainActivity) MainActivity.mContext).getChargerConfiguration().getTestPrice());
                chargingCurrentData.setPowerUnitPrice(testPrice);
                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
            }
        } catch (Exception e) {
            Toast.makeText(this.getActivity(), "서버 연결이 안됨.....", Toast.LENGTH_SHORT).show();
            logger.error(" init onClick error : {}", e.getMessage());        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (unitPriceHandler != null) {
                unitPriceHandler.removeCallbacksAndMessages(null);
                unitPriceHandler.removeMessages(0);
                unitPriceHandler = null;
            }
            animBlink.cancel();
            animBlink = null;
            // back image
            requestStrings[0] = String.valueOf(mChannel);
            sharedModel.setMutableLiveData(requestStrings);
        } catch (Exception e) {
            logger.error("init onDetach error : {}", e.getMessage());
        }
    }
    private boolean onUnitPrice() {
        boolean result = false;
        try {
            File file = new File(GlobalVariables.getRootPath() + File.separator + GlobalVariables.UNIT_FILE_NAME);
            result = file.exists() || !Objects.equals(chargerConfiguration.getAuthMode(), "0");
        } catch (Exception e){
            logger.error(e.getMessage());
        }
        return result;
    }

    private void onUnitPriceDisplay() {
        unitPriceHandler = new Handler();
        unitPriceHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void run() {
                        try {
                            //사용 단가 갖고 오기
                            String newPrice = tariffFileUpdater.getPrice("A").replace(".0","");
                            txtMemberUnitInput.setText(getString(R.string.memChargingUnit) + String.format(" %s 원", newPrice));
                            chargingCurrentData.setPowerUnitPrice(Double.parseDouble(newPrice));
                        } catch (Exception e) {
                            logger.error("unitPriceHandler  : {}", e.getMessage());
                        }
                    }
                });
                unitPriceHandler.postDelayed(this, 60000);
            }
        }, 8000);
    }

}
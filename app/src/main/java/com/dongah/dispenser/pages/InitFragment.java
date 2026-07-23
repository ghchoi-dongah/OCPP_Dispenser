package com.dongah.dispenser.pages;


import android.os.Build;
import android.os.Bundle;
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
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.PaymentType;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.SharedModel;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    TextView textViewConnector, textViewInitMessage;
    ImageView imageViewCar;


    MainActivity activity;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    SocketReceiveMessage socketReceiveMessage;
    ClassUiProcess classUiProcess;
    FragmentChange fragmentChange;
    String[] requestStrings = new String[1];


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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        activity = (MainActivity) MainActivity.mContext;
        chargingCurrentData = activity.getChargingCurrentData(mChannel);
        chargerConfiguration = activity.getChargerConfiguration();
        classUiProcess = activity.getClassUiProcess(mChannel);
        fragmentChange = activity.getFragmentChange();
        socketReceiveMessage = activity.getSocketReceiveMessage();

        animBlink = AnimationUtils.loadAnimation(getActivity(), R.anim.blink_animation);
        viewCircle = view.findViewById(R.id.viewCircle);
        viewCircle.setOnClickListener(this);
        textViewConnector = view.findViewById(R.id.textViewConnector);
        imageViewCar = view.findViewById(R.id.imageViewCar);
        textViewInitMessage = view.findViewById(R.id.textViewInitMessage);
        textViewInitMessage.startAnimation(animBlink);

        try {
            if (mChannel == 0) {
                imageViewCar.setScaleX(1f);
                textViewConnector.setText(R.string.leftConnector);
            } else {
                imageViewCar.setScaleX(-1f);
                textViewConnector.setText(R.string.rightConnector);
            }
        } catch (Exception e) {
            logger.error("onCreateView error : {}", e.getMessage());
        }

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            requestStrings[0] = String.valueOf(0);
            sharedModel.setMutableLiveData(requestStrings);
        } catch (Exception e) {
            logger.error("onViewCreated error : {}", e.getMessage());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onClick(View v) {
        try {
            if (!Objects.equals(v.getId(), R.id.viewCircle)) return;

            // !reservation → current data clear
            if (chargingCurrentData.getReservedStatus() != ChargePointStatus.Reserved) {
                chargingCurrentData.onCurrentDataClear();
            }
            chargingCurrentData.setConnectorId(mChannel + 1);
            chargingCurrentData.setChargerPointType(ChargerPointType.COMBO);

            double testPrice = Double.parseDouble(chargerConfiguration.getTestPrice());
            switch (chargerConfiguration.getAuthMode()) {
                case "0":
//                    if (socketReceiveMessage.getSocket().getState() != SocketState.OPEN) {
//                        Toast.makeText(this.getActivity(), "서버 연결 DISCONNECT.\n충전을 할 수 없습니다.",
//                                Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                    // server mode
                    if (Objects.equals(chargerConfiguration.getSelectPayment(), "0")) {
                        // credit + member
                        classUiProcess.setUiSeq(UiSeq.AUTH_SELECT);
                        fragmentChange.onFragmentChange(mChannel, UiSeq.AUTH_SELECT, "AUTH_SELECT", null);
                    } else if (Objects.equals(chargerConfiguration.getSelectPayment(), "1")) {
                        // member
                        chargingCurrentData.setPowerUnitPrice(testPrice);
                        chargingCurrentData.setPaymentType(PaymentType.MEMBER);
                        classUiProcess.setUiSeq(UiSeq.MEMBER_CARD);
                        fragmentChange.onFragmentChange(mChannel, UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
                    } else if (Objects.equals(chargerConfiguration.getSelectPayment(), "2")) {
                        // credit
                        GlobalVariables.setCustomUnitPriceReq(false);
                        chargingCurrentData.setPowerUnitPrice(testPrice);
                        chargingCurrentData.setPaymentType(PaymentType.CREDIT);
                        classUiProcess.setUiSeq(UiSeq.CREDIT_CARD);
                        fragmentChange.onFragmentChange(mChannel, UiSeq.CREDIT_CARD, "CREDIT_CARD", null);
                    }
                    break;
                case "3":
                    // powerMeterTest, local 회원 인증용
                    chargingCurrentData.setPowerUnitPrice(testPrice);
                    classUiProcess.setUiSeq(UiSeq.MEMBER_CARD);
                    fragmentChange.onFragmentChange(mChannel, UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
                    break;
                default:
                    // auto || test
                    chargingCurrentData.setPowerUnitPrice(testPrice);
                    classUiProcess.setUiSeq(UiSeq.PLUG_CHECK);
                    fragmentChange.onFragmentChange(mChannel, UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this.getActivity(), "잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            logger.error("onClick error : {}", e.getMessage(), e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            animBlink.cancel();
            animBlink = null;

            requestStrings[0] = String.valueOf(mChannel);
            sharedModel.setMutableLiveData(requestStrings);
        } catch (Exception e) {
            logger.error("onDetach error : {}", e.getMessage(), e);
        }
    }
}
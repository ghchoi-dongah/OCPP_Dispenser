package com.dongah.dispenser.pages;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.FragmentChange;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.PaymentType;
import com.dongah.dispenser.basefunction.TariffFileUpdater;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.utils.SharedModel;
import com.dongah.dispenser.websocket.ocpp.utilities.ZonedDateTimeConvert;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthSelectFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthSelectFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    ImageView imageViewQr;
    View viewMember, viewNoMember, viewQr;
//    TextView textViewMemberUnitInput, textViewNoMemberUnitInput;

    SharedModel sharedModel;
    Handler uiCheckHandler;

    MainActivity activity;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    ClassUiProcess classUiProcess;
    FragmentChange fragmentChange;
    SocketReceiveMessage socketReceiveMessage;


    public AuthSelectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthSelectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthSelectFragment newInstance(String param1, String param2) {
        AuthSelectFragment fragment = new AuthSelectFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_select, container, false);
        sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
        activity = (MainActivity) MainActivity.mContext;
        chargerConfiguration = activity.getChargerConfiguration();
        chargingCurrentData = activity.getChargingCurrentData(mChannel);
        socketReceiveMessage = activity.getSocketReceiveMessage();
        classUiProcess = activity.getClassUiProcess(mChannel);
        fragmentChange = activity.getFragmentChange();

//        textViewMemberUnitInput = view.findViewById(R.id.textViewMemberUnitInput);
//        textViewNoMemberUnitInput = view.findViewById(R.id.textViewNoMemberUnitInput);

        viewMember = view.findViewById(R.id.viewMember);
        viewMember.setOnClickListener(this);
        viewNoMember = view.findViewById(R.id.viewNoMember);
        viewNoMember.setOnClickListener(this);
        viewQr = view.findViewById(R.id.viewQr);
        viewQr.setOnClickListener(this);
        imageViewQr = view.findViewById(R.id.imageViewQr);

        return view;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.mContext, R.raw.authselect);
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            mediaPlayer.start();

            // 60초 후 HOME
            uiCheckHandler = new Handler();
            uiCheckHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    classUiProcess.onHome();
                }
            }, 60000);

        }catch (Exception e) {
            logger.error("onViewCreated error : {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onClick(View v) {
        try {
            int getId = v.getId();
            if (Objects.equals(getId, R.id.viewMember)) {
                chargingCurrentData.setPaymentType(PaymentType.MEMBER);
                classUiProcess.setUiSeq(UiSeq.MEMBER_CARD);
                fragmentChange.onFragmentChange(mChannel,UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
            } else if (Objects.equals(getId, R.id.viewNoMember)) {
                GlobalVariables.setCustomUnitPriceReq(false);
                chargingCurrentData.setPaymentType(PaymentType.CREDIT);
                classUiProcess.setUiSeq(UiSeq.CREDIT_CARD);
                fragmentChange.onFragmentChange(mChannel,UiSeq.CREDIT_CARD, "CREDIT_CARD", null);
            } else if (Objects.equals(getId, R.id.viewQr)) {
                classUiProcess.setUiSeq(UiSeq.QR_CODE);
                fragmentChange.onFragmentChange(mChannel,UiSeq.QR_CODE, "QR_CODE", null);
            }
        } catch (Exception e) {
            logger.error("onClick error : {}", e.getMessage(), e);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (uiCheckHandler != null) {
                uiCheckHandler.removeCallbacksAndMessages(null);
                uiCheckHandler.removeMessages(0);
                uiCheckHandler = null;
            }
        } catch (Exception e) {
            logger.error("onDetach error : {}", e.getMessage(), e);
        }
    }
}
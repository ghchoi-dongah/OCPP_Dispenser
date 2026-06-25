package com.dongah.dispenser.pages;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlugWaitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlugWaitFragment extends Fragment {

    private static final Logger logger = LoggerFactory.getLogger(PlugWaitFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL = "CHANNEL";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannel;

    int cnt = 0;
    TextView txtMessage;
    ImageView imageViewLoading;
    AnimationDrawable animationDrawable;

    RxData rxData;
    Handler countHandler;
    Runnable countRunnable;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;

    public PlugWaitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlugWaitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlugWaitFragment newInstance(String param1, String param2) {
        PlugWaitFragment fragment = new PlugWaitFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plug_wait, container, false);
        txtMessage = view.findViewById(R.id.txtMessage);
        imageViewLoading = view.findViewById(R.id.imageViewLoading);
        imageViewLoading.setBackgroundResource(R.drawable.ani_loading);
        animationDrawable = (AnimationDrawable) imageViewLoading.getBackground();
        chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
        chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);
        return view;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            animationDrawable.start();
            cnt = 0;
            rxData = ((MainActivity) getActivity()).getControlBoard().getRxData(mChannel);

            MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.mContext, R.raw.plugwait);
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            mediaPlayer.start();

            // connection time out
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countHandler = new Handler();
                    countRunnable = new Runnable() {
                        @Override
                        public void run() {
                            cnt++;
                            if (Objects.equals(cnt, GlobalVariables.getConnectionTimeOut()) && !rxData.isCsPilot()) {
                                ((MainActivity) getActivity()).getControlBoard().getTxData(mChannel).setStart(false);
                                ((MainActivity) getActivity()).getControlBoard().getTxData(mChannel).setStop(false);
                                //선 결제에 의한 무카드 취소 (4:무카드 취소)(5:부분 취소)
                                if (chargingCurrentData.isPrePaymentResult()) {
                                    chargingCurrentData.setPartialCancelPayment(chargingCurrentData.getPrePayment());
//                                    ((MainActivity) MainActivity.mContext).getTls3800().onTLS3800Request(mChannel, TLS3800.CMD_TX_PAYCANCEL, 4);
                                }

                                //preparing
                                if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                        Objects.equals(chargerConfiguration.getAuthMode(), "0") &&
                                        !((MainActivity) getActivity()).getControlBoard().getRxData(mChannel).isCsPilot()) {
                                    chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                                    chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.NoError);
                                    ((MainActivity) MainActivity.mContext).getProcessHandler().sendMessage(((MainActivity) MainActivity.mContext).getSocketReceiveMessage()
                                            .onMakeHandlerMessage(
                                                    GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                                    chargingCurrentData.getConnectorId(),
                                                    0,
                                                    null,
                                                    null,
                                                    null,
                                                    false));
                                }
                                ((MainActivity) getActivity()).getClassUiProcess(mChannel).onHome();
                            } else {
                                countHandler.postDelayed(countRunnable, 1000);
                            }

                            //connecting wait
                            if (rxData.isCsPilot()) {
                                Log.d("PlugWaitFragment", "ClassUiSeq: " + ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).getUiSeq());
                                if (txtMessage.getTag() == null || !(boolean) txtMessage.getTag()) {
                                    txtMessage.setText(R.string.EVCheckMessage);
                                    txtMessage.setTag(true);
                                }
                            }
                        }
                    };
                    countHandler.postDelayed(countRunnable, 1000);
                }
            });
        } catch (Exception e) {
            Log.e("PlugWaitFragment", "onViewCreated error", e);
            logger.error("PlugWaitFragment onViewCreated : {}", e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        try {
            if (animationDrawable != null) {
                animationDrawable.stop();
            }

            if (imageViewLoading != null) {
                Drawable bg = imageViewLoading.getBackground();
                if (bg instanceof AnimationDrawable) {
                    ((AnimationDrawable) bg).stop();
                }
                imageViewLoading.setBackground(null);
            }

            if (countHandler != null) {
                countHandler.removeCallbacksAndMessages(null);
                countHandler = null;
            }
            countRunnable = null;

        } catch (Exception e) {
            Log.e("PlugWaitFragment", "onDestroyView error", e);
            logger.error("PlugWaitFragment onDestroyView error : {}", e.getMessage());
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (countHandler != null) {
                countHandler.removeCallbacksAndMessages(null);
                countHandler = null;
            }
        } catch (Exception e) {
            Log.e("PlugWaitFragment", "onDetach error", e);
            logger.error("PlugWaitFragment onDetach error : {}", e.getMessage());
        }
    }
}
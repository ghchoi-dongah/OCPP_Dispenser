package com.dongah.dispenser.pages;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.basefunction.ChargerConfiguration;
import com.dongah.dispenser.basefunction.ChargingCurrentData;
import com.dongah.dispenser.basefunction.ClassUiProcess;
import com.dongah.dispenser.basefunction.GlobalVariables;
import com.dongah.dispenser.basefunction.UiSeq;
import com.dongah.dispenser.controlboard.RxData;
import com.dongah.dispenser.handler.ProcessHandler;
import com.dongah.dispenser.websocket.ocpp.core.ChargePointStatus;
import com.dongah.dispenser.websocket.ocpp.core.Reason;
import com.dongah.dispenser.websocket.socket.SocketReceiveMessage;
import com.dongah.dispenser.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberCardWaitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberCardWaitFragment extends Fragment  {

    private static final Logger logger = LoggerFactory.getLogger(MemberCardWaitFragment.class);


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
    TextView txtMemberWaiting;
    ImageView imageViewLoading;
    AnimationDrawable animationDrawable;

    ClassUiProcess classUiProcess;
    ChargingCurrentData chargingCurrentData;
    ChargerConfiguration chargerConfiguration;
    Handler countHandler;
    Runnable countRunnable;


    public MemberCardWaitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberCardWaitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberCardWaitFragment newInstance(String param1, String param2) {
        MemberCardWaitFragment fragment = new MemberCardWaitFragment();
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
        View view = inflater.inflate(R.layout.fragment_member_card_wait, container, false);
        txtMemberWaiting = view.findViewById(R.id.txtMemberWaiting);
        imageViewLoading = view.findViewById(R.id.imageViewLoading);
        imageViewLoading.setBackgroundResource(R.drawable.ani_loading);
        animationDrawable = (AnimationDrawable) imageViewLoading.getBackground();
        return view;
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            animationDrawable.start();
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            classUiProcess = ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel);
            chargingCurrentData = ((MainActivity) MainActivity.mContext).getChargingCurrentData(mChannel);

            MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.mContext, R.raw.membercardwait);
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            mediaPlayer.start();

            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countHandler = new Handler();
                    countRunnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cnt++;
                                if (Objects.equals(cnt, 20)) {
                                    ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
                                } else {
//                                txtCount.setText(String.valueOf(cnt));
                                    countHandler.postDelayed(countRunnable, 1000);
                                }
                                //authorize result check
                                if (!chargingCurrentData.isAuthorizeResult()) {
                                    animationDrawable.stop();
                                }
                            } catch (Exception e){
                                logger.error(e.getMessage());
                            }
                        }
                    };
                    countHandler.postDelayed(countRunnable, 1000);
                }
            });


            //나중에 부활 예정
            String[] idTagInfo;
            UiSeq uiSeq = classUiProcess.getUiSeq();
            SocketReceiveMessage socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
            ProcessHandler processHandler = ((MainActivity) MainActivity.mContext).getProcessHandler();

            //local mode Authorize
            if (Objects.equals(chargerConfiguration.getAuthMode(), "4")) {
                if (Objects.equals(chargingCurrentData.getIdTag(), "1019160058571654")) {
                    classUiProcess.setUiSeq(UiSeq.PLUG_CHECK);
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                }
                return;
            }

            //reservation check
            if (chargingCurrentData.getReservedStatus() == ChargePointStatus.Reserved) {
                if (!Objects.equals(chargingCurrentData.getResIdTag(), chargingCurrentData.getIdTag())) {
//                    Toast.makeText(getActivity(), "예약한 IdTag가 틀립니다. ", Toast.LENGTH_SHORT).show();
//                    classUiProcess.onHome();
                    if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
                                chargingCurrentData.getConnectorId(),
                                0,
                                Objects.equals(uiSeq, UiSeq.CHARGING) ? chargingCurrentData.getResIdTag() : chargingCurrentData.getIdTag(),
                                null,
                                null,
                                false));
                    } else {
                        if (!Objects.equals(chargingCurrentData.getResParentIdTag(), "")) {
                            processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                    GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
                                    chargingCurrentData.getConnectorId(),
                                    0,
                                    chargingCurrentData.getIdTag(),
                                    null,
                                    null,
                                    false));
                        } else {
                            classUiProcess.onHome();
                        }
                    }

                    return;
                } else {
                    //Authorization
                    if (Objects.equals(uiSeq, UiSeq.CHARGING)) {
                        idTagInfo = socketReceiveMessage.getLocalAuthorizationListStrings(uiSeq == UiSeq.CHARGING ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag());
                        if (Objects.equals(chargingCurrentData.getParentIdTag(), idTagInfo[1]) ||
                                Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop())) {
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING_STOP_MESSAGE, "CHARGING_STOP_MESSAGE", null);
                        } else {
                            classUiProcess.setUiSeq(UiSeq.CHARGING);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING, "CHARGING", null);
                        }
                    } else {
                        chargingCurrentData.setIdTag(chargingCurrentData.getResIdTag());
                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
                                chargingCurrentData.getConnectorId(),
                                0,
                                Objects.equals(uiSeq, UiSeq.CHARGING) ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag(),
                                null,
                                null,
                                false));
                        //preparing
                        if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                            chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
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
                    return;
                }
            }
            // isLocalPreAuthorize == true : local authorization list 에서 사용자 인증
            if (GlobalVariables.isLocalPreAuthorize()) {
                // local authorization enabled --> local 인증
                //LocalAuthListEnabled
                idTagInfo = socketReceiveMessage.getLocalAuthorizationListStrings(uiSeq == UiSeq.CHARGING ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag());
                if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
                    if (Objects.equals(chargingCurrentData.getParentIdTag(), idTagInfo[1]) ||
                            Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop())) {
                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING_STOP_MESSAGE, "CHARGING_STOP_MESSAGE", null);
                    } else {
                        classUiProcess.setUiSeq(UiSeq.CHARGING);
                        ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING, "CHARGING", null);
                    }
                } else {
                    if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                            Objects.equals(chargerConfiguration.getAuthMode(), "0") && !Objects.equals(idTagInfo[0], chargingCurrentData.getIdTag())) {

                        // 추가 2026.02.09 - 소켓 연결 상태일 때만 Authorize 전송 (오프라인 시 dump에 쌓이면 StartTransaction보다 먼저 전송되어 TC_037_2_CS 실패)
                        SocketState localAuthState = socketReceiveMessage.getSocket().getState();
                        if (Objects.equals(localAuthState.getValue(), 7)) {
                            processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                    GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
                                    chargingCurrentData.getConnectorId(),
                                    0,
                                    chargingCurrentData.getIdTag(),
                                    null,
                                    null,
                                    false));
                        }

                        // StatusNotification(Preparing)은 Authorize.conf(Accepted) 수신 후 전송
                        //AuthorizeRemoteTxRequests
//                    if (!GlobalVariables.isLocalAuthListEnabled()) {
//                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
//                                GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
//                                chargingCurrentData.getConnectorId(),
//                                0,
//                                Objects.equals(uiSeq, UiSeq.CHARGING) ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag(),
//                                null,
//                                null,
//                                false));
////                        if (!GlobalVariables.isAuthorizeRemoteTxRequests()) {
////                        }
                    } else {
                        if (Objects.equals(idTagInfo[0], chargingCurrentData.getIdTag())) {
                            chargingCurrentData.setAuthorizeResult(true);
                            chargingCurrentData.setParentIdTag(idTagInfo[1]);
                            //preparing
                            if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                    Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                                chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                                processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                        GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                        chargingCurrentData.getConnectorId(),
                                        0,
                                        null,
                                        null,
                                        null,
                                        false));
                            }
                            ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                        } else if (Objects.equals(idTagInfo[0], "notFound") || Objects.equals(idTagInfo[0], "")) {
                            if (!GlobalVariables.StopTransactionOnInvalidId) {
                                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                            } else {
                                processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                        GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
                                        chargingCurrentData.getConnectorId(),
                                        0,
                                        Objects.equals(uiSeq, UiSeq.CHARGING) ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag(),
                                        null,
                                        null,
                                        false));
//                                if (!GlobalVariables.isAuthorizeRemoteTxRequests()) {
//                                }
                            }
                        } else {
                            // 인증 실패
                            ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).getChargingCurrentData().setAuthorizeResult(false);
                            ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
                            RxData rxData = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData(mChannel);
                            if (!rxData.isCsPilot() && Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
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
                        }
                    }
                }
            } else {
                // central system send
                SocketState state = socketReceiveMessage.getSocket().getState();
                if (state == SocketState.OPEN) {
                    if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
                        boolean sendAuthorizeForStop =
                                !Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop()) &&
                                !TextUtils.isEmpty(chargingCurrentData.getParentIdTag());
                        if (sendAuthorizeForStop) {
                            processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                    GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
                                    chargingCurrentData.getConnectorId(),
                                    0,
                                    chargingCurrentData.getIdTagStop(),
                                    null,
                                    null,
                                    false));
                        } else if (Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop())) {
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING_STOP_MESSAGE, "CHARGING_STOP_MESSAGE", null);
                        } else {
                            ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.CHARGING);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING, "CHARGING", null);
                        }
                    } else {
                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
                                chargingCurrentData.getConnectorId(),
                                0,
                                chargingCurrentData.getIdTag(),
                                null,
                                null,
                                false));
                    }

//                    if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
//                        if ((Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop()))
//                            || Objects.equals(chargingCurrentData.getParentIdTag(), chargingCurrentData.getParentIdTagStop())) {
//                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.CHARGING_STOP_MESSAGE, "CHARGING_STOP_MESSAGE", null);
//                        } else {
//                            ((MainActivity) MainActivity.mContext).getClassUiProcess().setUiSeq(UiSeq.CHARGING);
//                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.CHARGING, "CHARGING", null);
//                        }
//                    } else {
//                        if (chargingCurrentData.getChargePointStatus() == ChargePointStatus.Reserved) {
//                            if (!Objects.equals(chargingCurrentData.getResIdTag(), chargingCurrentData.getIdTag())) {
//                                Toast.makeText(getActivity(), "예약한 IdTag가 틀립니다. ", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                        }
//                        processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
//                                GlobalVariables.MESSAGE_HANDLER_AUTHORIZE,
//                                chargingCurrentData.getConnectorId(),
//                                0,
//                                chargingCurrentData.getIdTag(),
//                                null,
//                                null,
//                                false));
//                    }
                } else {
                    //서버와 연결이 안된 경우
                    if (GlobalVariables.isLocalAuthorizeOffline()) {
                        // local authorization enabled --> local 인증
                        idTagInfo = socketReceiveMessage.getLocalAuthorizationListStrings(uiSeq == UiSeq.CHARGING ? chargingCurrentData.getIdTagStop() : chargingCurrentData.getIdTag());
                        if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
                            if (Objects.equals(chargingCurrentData.getParentIdTag(), idTagInfo[1]) ||
                                    Objects.equals(chargingCurrentData.getIdTag(), chargingCurrentData.getIdTagStop())) {
                                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING_STOP_MESSAGE, "CHARGING_STOP_MESSAGE", null);
                            } else {
                                classUiProcess.setUiSeq(UiSeq.CHARGING);
                                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING, "CHARGING", null);
                            }
                        } else {
                            if (Objects.equals(idTagInfo[0], chargingCurrentData.getIdTag()) || GlobalVariables.isAllowOfflineTxForUnknownId() ||
                                    GlobalVariables.isLocalAuthorizeOffline()) {
                                chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                                state = socketReceiveMessage.getSocket().getState();
                                if (state == SocketState.OPEN) {
                                    processHandler.sendMessage(socketReceiveMessage.onMakeHandlerMessage(
                                            GlobalVariables.MESSAGE_HANDLER_STATUS_NOTIFICATION,
                                            chargingCurrentData.getConnectorId(),
                                            0,
                                            null,
                                            null,
                                            null,
                                            false));
                                }
                                chargingCurrentData.setStopReason(!Objects.equals(idTagInfo[0], chargingCurrentData.getIdTag()) &&
                                        GlobalVariables.isStopTransactionOnInvalidId() ? Reason.DeAuthorized : chargingCurrentData.getStopReason());
                                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.PLUG_CHECK);
                                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                            } else {
                                // 인증 실패
                                Toast.makeText(getActivity(), "인증 실패. ", Toast.LENGTH_SHORT).show();
                                ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "서버와 통신 DISCONNECT!!! 인증 실패. ", Toast.LENGTH_SHORT).show();
                        if (Objects.equals(UiSeq.CHARGING, uiSeq)) {
                            ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).setUiSeq(UiSeq.CHARGING);
                            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(mChannel,UiSeq.CHARGING, "CHARGING", null);
                        } else {
                            ((MainActivity) MainActivity.mContext).getClassUiProcess(mChannel).onHome();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(" MemberCardWaitFragment error : {}", e.getMessage());
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

        } catch (Exception e) {
            logger.error("MemberCardWaitFragment onDestroyView error : {}", e.getMessage());
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (countHandler != null) {
                countHandler.removeCallbacks(countRunnable);
                countHandler.removeCallbacksAndMessages(null);
                countHandler.removeMessages(0);
            }
        } catch (Exception e) {
            logger.error("MemberCardWaitFragment onDetach error : {} ", e.getMessage());
        }
    }
}
package com.dongah.dispenser.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongah.dispenser.MainActivity;
import com.dongah.dispenser.R;
import com.dongah.dispenser.controlboard.RxData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingSequentialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingSequentialFragment extends Fragment {
    private static final Logger logger = LoggerFactory.getLogger(ChargingSequentialFragment.class);

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
    ImageView imageViewCar;
    TextView textViewConnector, textViewInitMessage, textViewInitMessageSub;

    RxData rxData;
    Runnable runnable;
    Handler handler;

    public ChargingSequentialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChargingSequentialFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChargingSequentialFragment newInstance(String param1, String param2) {
        ChargingSequentialFragment fragment = new ChargingSequentialFragment();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charging_sequential, container, false);
        rxData = ((MainActivity) MainActivity.mContext).getControlBoard().getRxData(mChannel);
        animBlink = AnimationUtils.loadAnimation(getActivity(), R.anim.blink_animation);
        textViewConnector = view.findViewById(R.id.textViewConnector);
        textViewInitMessage = view.findViewById(R.id.textViewInitMessage);
        textViewInitMessage.startAnimation(animBlink);
        textViewInitMessageSub = view.findViewById(R.id.textViewInitMessageSub);

        try {
            // ch0, ch1 구분 => 이미지 위치 조절
            if (mChannel == 0) {
                imageViewCar.setScaleX(1f);
                textViewConnector.setText("1 " + getString(R.string.connectorSeq));
            } else {
                imageViewCar.setScaleX(-1f);
                textViewConnector.setText("2 " + getString(R.string.connectorSeq));
            }
        } catch (Exception e) {
            logger.error("onCreateView error : {}", e.getMessage(), e);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            handler = new Handler(Looper.getMainLooper());
            runnable = new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    try {
                        int remainingSec = rxData.getOtherChannelRemainingTimeFullSoc();
                        int minute = remainingSec / 60;

                        textViewInitMessageSub.setText(
                                getString(R.string.seqTime, String.valueOf(minute))
                        );

                    } catch (Exception e) {
                        logger.error("ChargingSequentialFragment run error : {}", e.getMessage());
                    }
                    handler.postDelayed(this, 1000);
                }
            };
            handler.postDelayed(runnable, 1000);
        }catch (Exception e) {
            logger.error("onViewCreated error : {}", e.getMessage(), e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }

            runnable = null;
            handler = null;
        } catch (Exception e) {
            logger.error("onDestroyView error : {}", e.getMessage());
        }
    }
}
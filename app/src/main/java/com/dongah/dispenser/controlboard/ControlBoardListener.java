package com.dongah.dispenser.controlboard;

public interface ControlBoardListener {
    void onControlBoardReceive(RxData[] rxData);

    void onControlBoardSend(TxData[] txData);
}

package com.dongah.dispenser.rfcard;

public interface RfCardReaderListener {
    void onRfCardDataReceive(int ch, String cardNum, boolean value);

}

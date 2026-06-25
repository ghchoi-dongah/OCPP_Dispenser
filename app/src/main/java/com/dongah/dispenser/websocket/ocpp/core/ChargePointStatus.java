package com.dongah.dispenser.websocket.ocpp.core;

public enum ChargePointStatus {
    Available,
    Preparing,
    Charging,
    SuspendedEVSE,
    SuspendedEV,
    Finishing,
    Reserved,
    Unavailable,
    Faulted;
}

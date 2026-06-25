package com.dongah.dispenser.websocket.ocpp.security;

public enum MessageTrigger {
    BootNotification,
    LogStatusNotification,
    FirmwareStatusNotification,
    Heartbeat,
    MeterValues,
    SignChargePointCertificate,
    StatusNotification;
}

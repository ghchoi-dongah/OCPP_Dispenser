package com.dongah.dispenser.websocket.ocpp.core;

/**
 * Accepted values used with {@link DataTransferConfirmation}
 */
public enum DataTransferStatus {
    Accepted,
    Rejected,
    UnknownMessageId,
    UnknownVendorId
}

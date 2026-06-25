package com.dongah.dispenser.websocket.ocpp.security;

public enum SignedFirmwareStatus {
    Downloaded,
    DownloadFailed,
    Downloading,
    DownloadScheduled,
    DownloadPaused,
    Idle,
    InstallationFailed,
    Installing,
    Installed,
    InstallRebooting,
    InstallScheduled,
    InstallVerificationFailed,
    InvalidSignature,
    SignatureVerified;
}

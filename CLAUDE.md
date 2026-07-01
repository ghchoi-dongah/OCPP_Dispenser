# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

동아일렉콤이 개발한 **OCPP 1.6 기반 EV 충전기(디스펜서) Android 제어 앱**이다. 듀얼 채널(커넥터 2개)을 지원하며, WebSocket/TLS로 CSMS(충전소 관리 서버)와 통신하고, 시리얼 포트로 실제 충전 제어 보드 및 RF 카드 리더와 통신한다.

- **Namespace**: `com.dongah.dispenser`
- **Min SDK**: 24 (Android 7.0), **Compile/Target SDK**: 36
- **언어**: Java, 빌드 시스템: Gradle 8.5.2 (Kotlin DSL)
- **설정 파일**: `gradle/libs.versions.toml`

## 빌드 및 실행

```bash
# 디버그 APK 빌드
./gradlew assembleDebug

# 릴리즈 APK 빌드
./gradlew assembleRelease

# 전체 빌드 (lint + test + assemble)
./gradlew build

# 유닛 테스트
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.dongah.dispenser.ExampleUnitTest"
```

NDK(`26.1.10909125`)가 필요하다 — 시리얼 포트 JNI 코드(`jni/SerialPort.c`) 때문이다.

## 아키텍처

### 계층 구조

```
UI Layer      → pages/ (26개 Fragment)
Business      → basefunction/ClassUiProcess (채널별 상태 머신)
Protocol      → websocket/socket/SocketReceiveMessage (OCPP 메시지 디스패처)
Transport     → websocket/socket/Socket (WebSocket + TLS)
Hardware      → controlboard/ (시리얼 프로토콜), rfcard/ (RF 카드 리더)
Native        → android_serialport_api/ (JNI 래퍼)
```

### 상태 머신 (핵심 흐름)

`UiSeq` 열거형이 약 30개 상태를 정의하며, 각 채널은 독립적으로 상태를 유지한다:

```
INIT → AUTH_SELECT → (회원카드 / 신용카드 / QR) → PLUG_WAIT
     → CONNECT_CHECK → CHARGING → CHARGING_FINISH → INIT
```

상태 전환은 세 가지 이벤트로 발생한다:
1. 사용자 입력 (탭, 카드 스캔, 케이블 연결)
2. OCPP 서버 응답 (`SocketReceiveMessage` → `ClassUiProcess`)
3. 제어 보드 이벤트 (릴레이 상태 변화)

`FragmentChange.onFragmentChange()`가 `UiSeq` 상태에 따라 Fragment를 교체한다.

### 듀얼 채널 설계

채널 0 = 커넥터 ID 1, 채널 1 = 커넥터 ID 2. 각 채널은 독립적인 인스턴스를 가진다:
- `ChargingCurrentData[0/1]` — 트랜잭션 상태, 미터 값, 결제 정보
- `ClassUiProcess[0/1]` — UI 상태 머신
- `RxData[0/1]` / `TxData[0/1]` — 제어 보드 데이터

`ClassUiProcess`는 `onEventAction()` 타이머(2초 주기)로 상태를 주기적으로 점검한다.

### 주요 클래스별 역할

| 클래스 | 역할 |
|---|---|
| `GlobalVariables` | 100개 이상의 Handler 메시지 ID 상수 정의 |
| `ChargerConfiguration` | 파일에서 서버 주소·포트·요금·인증 방식 로드 |
| `ClassUiProcess` | 채널별 UI 이벤트 처리 및 OCPP 전송 트리거 |
| `SocketReceiveMessage` | OCPP 메시지 라우팅 (3500줄, 40+ 메시지 타입) |
| `Socket` | OkHttp3 WebSocket 래퍼, TLS/재연결 처리 |
| `ControlBoard` | 38400 baud 시리얼, Modbus형 프로토콜로 제어 보드 통신 |
| `ProcessHandler` | Android Handler 기반 메시지 처리기 |
| `JSONCommunicator` | OCPP JSON ↔ Java 객체 직렬화/역직렬화 |

### OCPP 메시지 흐름

```
User Action
  → ClassUiProcess.onXxx()
    → SocketReceiveMessage.onSend(request)
      → Socket → WebSocket → CSMS
        ← CSMS → Socket → SocketReceiveMessage.onMessage()
          → ProcessHandler.sendMessage()
            → Fragment UI 업데이트
```

OCPP 메시지 포맷:
- Call: `[2, "uniqueId", "Action", {payload}]`
- CallResult: `[3, "uniqueId", {payload}]`
- CallError: `[4, "uniqueId", "errorCode", "description", {}]`

벤더 확장은 DataTransfer로 처리하며, `websocket/ocpp/datatransfer/dongah/`에 동아일렉콤 전용 메시지(GetPrice, PayInfo, ResultPrice, PartialCancel 등)가 있다.

### 하드웨어 시리얼 통신

| 장치 | 포트 | Baud | 용도 |
|---|---|---|---|
| 제어 보드 | `/dev/ttyS4` | 38400 | 릴레이·미터·전압·전류 |
| RF 카드 리더 | `/dev/ttyS3` | 115200 | T-money·회원카드 |
| 신용카드 단말 | `/dev/ttyS2` | — | 신용카드 결제 |

제어 보드 패킷: `[channel][func][len][data(N words)][crc_lo][crc_hi]`  
CRC16으로 무결성 검증. `RxData`는 46워드(전압·전류·릴레이 상태·CSM 프로토콜 상태), `TxData`는 10워드(UI 시퀀스·릴레이 제어·출력 제한).

### TLS 보안

`keystore.bks`와 `truststore.bks` 파일 기반 상호 TLS 인증을 지원한다 (BouncyCastle). OCPP Security Profile 3(서명된 펌웨어 업데이트, 인증서 관리)을 구현한다.

### 스레딩 모델

- **메인 스레드**: UI 업데이트 (Handler.post())
- **ProcessHandler**: OCPP 메시지 처리
- **BootNotificationThread**: 주기적 BootNotification 전송
- **HeartbeatThread**: 주기적 Heartbeat 전송
- **MeterValueThread**: 미터값 샘플링/전송
- **ControlBoard 수신 스레드**: 시리얼 데이터 폴링
- **ClassUiProcess.Timer**: 2초 주기 이벤트 체크

### 설정 관리

설정은 `/storage/emulated/0/Download/` 경로의 파일에서 로드된다. `ChargerConfiguration.onLoadConfiguration()`이 초기 로드, `ConfigurationKeyRead`가 서버에서 받은 OCPP `ChangeConfiguration` 키를 반영하여 런타임에 오버라이드한다.

## 주요 의존성

| 라이브러리 | 용도 |
|---|---|
| OkHttp 3.14.9 + OkHttp-TLS | WebSocket 및 TLS |
| RxJava/RxAndroid 3.x | 비동기 처리 (특히 SFTP) |
| Gson 2.10.1 | JSON 직렬화 |
| BouncyCastle 1.70 | X.509 인증서 처리 |
| Zxing 4.3.0 | QR 코드 스캔 |
| JSch 0.1.55 | SFTP 펌웨어/로그 업로드 |
| SLF4J 1.7.36 | 로깅 |

# 차곡

함께 쓰는 차의 운행을 자동으로 기록하고, 다음 사람에게 차 상태를 넘겨주는 차량용 앱

현대자동차그룹 차량용 앱 개발 공모전(Pleos Connect) 출품작

## 주요 기능

- **탑승**: 시트 위치로 운전자 인식
- **자동 운행 기록**: P단 해제 시 시작, P단 진입 시 종료
- **인계**: 하차 시 한마디 남기기, 탑승 시 메시지와 차 상태 확인
- **AI 음성 인계**: 말로 남기면 Gemini가 받아쓰고 요약, 탑승 시 음성 안내
- **정산**: 달린 거리 비율로 월 충전비 나누기
- **기록 · 가족 설정**: 날짜별·사람별 운행 기록, 함께 쓰는 사람 관리

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | ViewModel, StateFlow, Navigation Compose |
| Database | Room |
| Vehicle | Pleos Vehicle SDK 2.3.2 |
| AI | Gemini API (음성 받아쓰기·요약), Android TTS |

## 구조

```
UI → ViewModel → Repository → Room
                     ↑
           VehicleDataSource (Vehicle SDK)
```

```
com.heiseung.chagok
├── ai          # 녹음, Gemini 호출
├── data        # Room, Repository
├── vehicle     # Vehicle SDK 구독, 운행 감지
├── session     # 현재 운전자
└── ui          # 화면
```

## 테스트 환경

| 항목 | 내용 |
| --- | --- |
| 에뮬레이터 | Pleos Connect Emulator v2.1.0 (API 34, arm64) |
| 화면 규격 | 2560 × 1440 |
| AVD 설정 | Default boot: Cold, RAM 4GB |
| 개발 도구 | Android Studio, AGP 8.13.2, compileSdk 36, minSdk 28 |

## 실행 방법

### APK로 실행

1. Pleos Connect 에뮬레이터를 실행합니다.
2. APK를 설치합니다.
   ```bash
   adb install apk/장혜승_차곡_release.apk
   ```

APK 위치: [`apk/장혜승_차곡_release.apk`](apk/장혜승_차곡_release.apk)

### 소스로 실행

1. 저장소를 받아 Android Studio에서 엽니다.
2. `local.properties`에 Gemini API 키를 추가합니다. (음성 인계에 필요)
   ```
   GEMINI_API_KEY=발급받은_키
   ```
3. Pleos Connect 에뮬레이터를 선택하고 Run 합니다.

## 테스트 설정

### Virtual Console (차량 신호 조작)

기어·주행거리·배터리·문·창문 값은 Vehicle SDK로 받으며, 에뮬레이터에서는 Virtual Console로 바꿀 수 있습니다.

```bash
adb forward tcp:8088 tcp:8088
```

브라우저에서 `http://127.0.0.1:8088/` 접속 → Emulation Controls

### 마이크 (음성 인계)

에뮬레이터 Extended Controls → Microphone → **Virtual microphone uses host audio input** 켜기

### 테스트 순서

1. 앱 실행 → 운전자 등록 또는 확인
2. Virtual
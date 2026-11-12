# 차곡

함께 쓰는 차의 운행을 자동으로 기록하고, 다음 사람에게 차 상태를 넘겨주는 차량용 앱

현대자동차그룹 차량용 앱 개발 공모전(Pleos Connect) 출품작

## 주요 기능

- **탑승**: 시트 위치로 운전자 인식
- **인계**: 이전 운전자가 남긴 메시지와 차 상태 확인
- **하차**: 운행 요약, 다음 사람에게 한마디 남기기
- **정산**: 달린 거리대로 월 충전비 나누기
- **기록**: 날짜별·사람별 운행 기록
- **가족·설정**: 함께 쓰는 사람 관리

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | ViewModel, StateFlow, Navigation Compose |
| Database | Room |
| Vehicle | Pleos Vehicle SDK |
| Environment | Pleos Connect 에뮬레이터 (API 34) |

## 구조

```
UI → ViewModel → Repository → Room
```

```
com.heiseung.chagok
├── data        # Room, Repository
├── vehicle     # 차량 데이터, 운행 감지
├── session     # 현재 운전자
└── ui          # 화면
```

## 실행

Pleos Connect 에뮬레이터에서 실행합니다. 현재는 가짜 차량 데이터로 동작하며, 상단 테스트 버튼으로 시트 위치·출발·도착을 바꿀 수 있습니다.

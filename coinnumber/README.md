# 과금계산기

흰 배경과 짙은 회색 버튼의 한국어 Android 오프라인 앱. `planning/image-generation-prompt-v4.txt`와 개발 기획서를 참고한 Kotlin + Jetpack Compose 구현입니다.

## 실행

Android Studio에서 이 폴더를 열고 Gradle 동기화 후 Run을 누르세요. Android 8.0(API 26) 이상을 지원합니다. SDK 36, JDK 17 이상이 필요합니다. 로컬 `local.properties`의 `sdk.dir`을 자신의 Android SDK 위치로 설정하세요.

```sh
./gradlew testDebugUnitTest assembleDebug
./gradlew connectedDebugAndroidTest
./gradlew bundleRelease
```

- 테스트 APK: `app/build/outputs/apk/debug/app-debug.apk`
- 서명 전 AAB: `app/build/outputs/bundle/release/app-release.aab`
- 단위 테스트 보고서: `app/build/reports/tests/testDebugUnitTest/index.html`
- 기기 테스트 보고서: `app/build/reports/androidTests/connected/debug/index.html`

## 기능

- 시작 → 1회 또는 다회차 입력 → 비용 결과 → 화면 터치 → 음식 비교. 뒤로가기·다시 계산 시 입력 유지.
- 다회차는 묶음 가격/횟수의 반올림 전 값으로 계산하며 실제 묶음 결제액과 구별.
- 목표 캐릭터 확정 천장, 천장 없이 99% 확률로 획득시 비용과 해당 횟수 기본 표시.
- 음식 버튼을 누르면 치킨·족발·냉면·순대국밥·햄버거 선택 메뉴 표시.
- 음식 단가는 요청한 2025년 기준 고정값: 치킨 25,000원, 족발 40,000원, 냉면 17,000원, 순대국밥 11,000원, 햄버거 8,000원. 앱에서 가격을 변경하지 않음.
- 최근 50개 기록 저장, 재사용, 개별/전체 삭제. 앱 회전·재생성 시 현재 화면과 입력 유지.
- 인터넷 권한, 계정, 서버, 광고, 결제, 추적 SDK 없음. SharedPreferences 백업·기기 전송 제외.

## 구조

- `Calculator.kt`: 입력 검증, 안정적인 확률 계산, 음식 수량 버림
- `MainActivity.kt`: 입력·비용 결과·음식 비교 화면과 계산 내역 보조 시트
- `Storage.kt`: 기기 내 기록 저장과 고정 음식 단가 정의
- `CalculatorTest.kt`: 계산 예시와 경계값 테스트
- `FlowTest.kt`: 에뮬레이터 화면 흐름 및 Activity 재생성 테스트
- `food_atlas.png`: 내장 imagegen으로 생성한 다섯 음식 이미지. 앱에서 선택한 영역을 표시.

계산 범위는 확률 0 또는 0.000001~100%, 가격 0~10억 원, 묶음/천장 1~100만 회입니다. 정수 원 가격을 받지만 묶음 단가에는 소수가 생길 수 있습니다. 금액 표시는 원 단위 반올림, 음식 수량은 표시 전 금액 기준 버림입니다.

## 출시 서명

현재 release AAB는 서명되지 않았습니다. Android Studio → Build → Generate Signed App Bundle / APK에서 본인의 업로드 키로 Android App Bundle을 생성하세요. 키와 비밀번호는 프로젝트 밖에서 보관하세요. 패키지명 `kr.co.coinnumber`는 최초 Play 등록 전 본인의 고유 식별자로 확정해야 합니다.

개발자 연락처, 공개 개인정보처리방침 URL, 실제 기기 테스트, Play Console 등록과 최종 제출은 아직 필요합니다. `docs/`의 초안과 검증 기록을 참고하세요. 이 프로젝트는 스토어 업로드나 결제를 수행하지 않습니다.

빌드 구성 참고: [Android Gradle Plugin 9.2 공식 릴리스 문서](https://developer.android.com/build/releases/agp-9-2-0-release-notes).

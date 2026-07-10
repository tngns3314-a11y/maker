# React Native 계산기 앱 설계

## 배경
기존 `계산기.html`은 덧셈(+)만 지원하는 단순 계산기다. 이를 React Native(Expo) 앱으로 포팅하여 안드로이드 APK로 빌드한다. 기능과 디자인은 기존 HTML 버전과 동일하게 유지한다.

## 범위
- 기능: 숫자 입력, 소수점(`.`), 덧셈(`+`)만 지원, `=`(계산), `C`(전체 지우기), `←`(백스페이스).
- 뺄셈/곱셈/나눗셈 등 사칙연산 확장은 범위 밖.
- 플랫폼: Expo 기본 구조로 iOS/Android 둘 다 구성하되, 이 기기에서는 안드로이드 APK만 실제 빌드한다.
- 디자인: 회색(#808080) 배경, 흰색 버튼, 4열 그리드, `=` 버튼은 2칸 병합 — 기존 HTML과 동일하게 재현.

## 프로젝트 구조
- 기존 `calculator-app/` 디렉토리를 초기화(이전 Capacitor 설치 흔적 제거)하고 `npx create-expo-app`으로 TypeScript 기반 Expo 프로젝트를 새로 생성한다.
- `App.tsx`: UI(버튼 그리드, 디스플레이) 렌더링.
- `src/calculator.ts`: 계산 로직을 담은 순수 함수 모듈.

## 계산 로직 (src/calculator.ts)
기존 HTML의 `expression` 문자열 기반 로직을 그대로 포팅한다.

- `inputDigit(expression, digit)`: 숫자/소수점을 이어붙인다. 소수점 입력 시 현재 항(`+` 기준 마지막 조각)에 이미 `.`이 있으면 무시하고, 현재 항이 비어있으면 `0.`으로 만든다.
- `inputPlus(expression)`: 식이 비어있거나 이미 `+`로 끝나면 무시하고, 아니면 `+`를 이어붙인다.
- `backspace(expression)`: 마지막 문자를 지운다.
- `clearAll()`: 빈 문자열을 반환한다.
- `calculate(expression)`: 식이 비어있거나 `+`로 끝나면 그대로 반환(계산 안 함). 아니면 `+`로 split 후 숫자를 합산해 문자열로 반환한다.

모두 순수 함수(입력 문자열 → 출력 문자열)로 작성해 Jest로 유닛 테스트한다.

## UI (App.tsx)
- `useState<string>`으로 `expression` 상태를 관리.
- 버튼 그리드(4열): `7 8 9 C` / `4 5 6 ←` / `1 2 3 +` / `0 . =(2칸)`.
- `StyleSheet`로 기존 HTML의 색상·여백·폰트 크기를 재현한다.
- 디스플레이는 `expression`이 비어있으면 `"0"`을 표시한다.

## 에러 처리
- 별도 예외 처리 없음. 기존 HTML과 동일하게 잘못된 입력(연속 `+`, 빈 식 계산)은 함수 내부 가드로 무시한다.

## 테스트
- `src/calculator.test.ts`: `inputDigit`, `inputPlus`, `backspace`, `clearAll`, `calculate`에 대한 Jest 유닛 테스트.
  - 예: `calculate("1+2")` → `"3"`, `inputPlus("1+")` → `"1+"`(무시), `inputDigit("", ".")` → `"0."`.
- UI 자동 테스트(스냅샷 등)는 범위 밖. 빌드 후 에뮬레이터/실기기에서 수동으로 덧셈 시나리오를 확인한다.

## 빌드 (APK)
1. `npx expo prebuild --platform android` — 네이티브 android 프로젝트 생성.
2. `JAVA_HOME`을 Android Studio 내장 JBR(`/Applications/Android Studio.app/Contents/jbr/Contents/Home`)로, `ANDROID_HOME`을 로컬 Android SDK(`~/Library/Android/sdk`)로 지정.
3. `cd android && ./gradlew assembleDebug` — 서명되지 않은 디버그 APK 생성 (`android/app/build/outputs/apk/debug/app-debug.apk`).

# 교대근무 / 간단 계산기

새 **교대근무** 앱은 `shift` 모듈입니다. 설치 파일은 `dist/shift-work.apk`이며,
자세한 기능과 설치·위젯 사용법은 [교대근무 안내](SHIFT_WORK.md)를 확인하세요.

아래는 기존 `app` 모듈의 계산기 안내입니다.

숫자 두 개를 입력하고 `+`, `−`, `×`, `÷` 버튼으로 계산하는 안드로이드 앱입니다.
`초기화`는 입력값과 결과를 모두 지웁니다. 소수와 음수, 화면 회전 시 상태 보존을 지원합니다.
빈 입력, 잘못된 숫자, 0으로 나누기를 안내합니다. 나눗셈 결과는 유효 숫자 16자리로 반올림합니다.
인터넷 연결이나 앱 권한이 필요하지 않습니다.

## 설치

- 설치 파일: `dist/simple-calculator.apk`
- Android 6.0(API 23) 이상 기기로 APK를 옮겨 열어 설치하세요.
- 설치 과정에서 요청되는 경우 APK를 여는 앱의 '이 출처 허용'을 켜세요.
- 개발 및 테스트용 서명 APK입니다.

## 프로젝트 열기

Android Studio에서 이 폴더를 열고 Gradle 동기화 후 실행하세요.
Android SDK 36, Android Gradle Plugin 9.0.1, Gradle 9.1.0, JDK 17 이상을 사용합니다.

## 오프라인 APK 빌드

Gradle 없이 로컬 Android SDK 도구로 빌드하는 스크립트도 포함되어 있습니다.
macOS의 기본 Android Studio 및 SDK 설치 경로를 사용합니다.
다른 환경에서는 `JAVA_HOME`과 `ANDROID_HOME`을 지정하세요.
SDK Platform 36 및 Build Tools 36.0.0이 필요합니다.

```sh
python3 scripts/build.py
```

이 명령은 계산 로직 검증, 리소스/Java 컴파일, DEX 생성, APK 정렬 및 테스트 서명을 수행합니다.

## 검증

- 계산 로직 13개 항목 통과
- APK 빌드 및 서명 검증 완료
- 실제 기기/에뮬레이터에서의 실행과 화면 조작은 아직 확인하지 않음

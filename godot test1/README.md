# 별빛 피하기

Godot 4.7.2로 만든 세로 화면 장애물 피하기 게임입니다.

## 플레이

- Mac: 방향키 또는 마우스 드래그
- Android: 화면을 터치하거나 좌우로 드래그
- START로 시작하고 TRY AGAIN으로 재시작합니다.

## Android 테스트 앱

`builds/star-dodge.apk`를 휴대폰에 복사한 뒤 파일 앱에서 열어 설치합니다.
설치 과정에서 해당 파일 앱의 '이 출처 허용'이 필요할 수 있습니다.
앱 이름은 '별빛 피하기'입니다.

이 APK는 개발용 디버그 키로 서명한 테스트 버전입니다.
스토어 배포는 별도의 출시용 서명과 AAB 내보내기 준비가 필요합니다.

## 다시 만들기

Godot에서 프로젝트 → 내보내기 → Android → 프로젝트 내보내기를 선택합니다.
디버그 내보내기를 켜고 `builds/star-dodge.apk`로 저장합니다.

이 Mac의 편집기 설정 → 내보내기 → Android 경로:

- Java SDK: `/Applications/Android Studio.app/Contents/jbr/Contents/Home`
- Android SDK: `/Users/soonkim/Library/Android/sdk`

설정 변경 전부터 편집기를 열어 두었다면 Java SDK 경로가 비어 있을 수 있습니다.
그 경우 위 경로를 편집기 설정에 입력하세요.

공식 안내: https://docs.godotengine.org/en/4.7/tutorials/export/exporting_for_android.html

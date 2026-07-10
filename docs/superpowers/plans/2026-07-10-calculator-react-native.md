# React Native 계산기 앱 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `계산기.html`(덧셈 전용 계산기)을 React Native(Expo) 앱으로 포팅하고, 로컬 Android SDK/JDK로 서명되지 않은 디버그 APK를 빌드한다.

**Architecture:** Expo(TypeScript) 단일 화면 앱. 계산 로직은 `src/calculator.ts`의 순수 함수로 분리해 Jest로 테스트하고, `App.tsx`는 그 함수들을 호출하는 UI(버튼 그리드)만 담당한다. Android 빌드는 `expo prebuild`로 네이티브 `android/` 프로젝트를 생성한 뒤 `./gradlew assembleDebug`로 로컬 빌드한다.

**Tech Stack:** Expo SDK 57 (React Native), TypeScript, Jest + jest-expo, Android SDK (`~/Library/Android/sdk`, platforms `android-36.1`, build-tools `36.1.0`/`37.0.0`), JDK (Android Studio 내장 JBR: `/Applications/Android Studio.app/Contents/jbr/Contents/Home`).

## Global Constraints

- 사칙연산 중 덧셈(+)만 지원한다. 뺄셈/곱셈/나눗셈은 구현하지 않는다.
- 디자인은 `계산기.html`과 동일하게: 배경 `#808080`, 버튼 흰색, 4열 그리드, `=` 버튼은 2칸 병합.
- Expo 기본 구조(iOS/Android 겸용)로 프로젝트를 만들되, 이 머신에서는 Android APK만 실제로 빌드한다.
- 계산 로직(`src/calculator.ts`)만 Jest 유닛 테스트로 검증한다. UI 자동 테스트(스냅샷 등)는 범위 밖.

---

## File Structure

- `calculator-app/` — Expo 프로젝트 루트 (기존 Capacitor 시도 흔적을 제거하고 새로 생성).
  - `App.tsx` — 계산기 화면(디스플레이 + 버튼 그리드). `src/calculator.ts`의 함수를 호출.
  - `src/calculator.ts` — 순수 함수 계산 로직 (`inputDigit`, `inputPlus`, `backspace`, `clearAll`, `calculate`).
  - `src/calculator.test.ts` — 위 함수들에 대한 Jest 유닛 테스트.
  - `package.json` — Expo가 생성 + jest 설정 추가.
  - `android/` — `expo prebuild`로 생성되는 네이티브 프로젝트 (직접 수정하지 않음, 커밋 대상 아님 — Expo 프로젝트에서는 보통 `android/`, `ios/`를 `.gitignore`에 유지하고 필요 시에만 커밋. 이 프로젝트는 로컬 빌드 산출물이므로 `.gitignore`에 유지한다).

---

### Task 1: Expo TypeScript 프로젝트 스캐폴딩

**Files:**
- Delete: `calculator-app/` (기존 Capacitor 설치 흔적 전체)
- Create: `calculator-app/` (Expo `blank-typescript` 템플릿으로 재생성 — `App.tsx`, `package.json`, `app.json`, `tsconfig.json`, `.gitignore` 등)

**Interfaces:**
- Consumes: 없음 (첫 작업)
- Produces: `calculator-app/package.json`에 `expo`, `react`, `react-native`, `typescript` 의존성이 설치된 상태. 이후 태스크는 이 디렉토리 안에서 작업한다.

- [ ] **Step 1: 기존 Capacitor 흔적 제거**

```bash
rm -rf /Users/soonkim/maker/calculator-app
```

- [ ] **Step 2: Expo TypeScript 프로젝트 생성**

```bash
cd /Users/soonkim/maker
npx create-expo-app@latest calculator-app -t blank-typescript --no-agents-md
```

Expected: `calculator-app/` 디렉토리가 생성되고 `npm install`까지 자동 실행되어 `node_modules/`가 존재한다.

- [ ] **Step 3: 생성된 파일 확인**

```bash
ls /Users/soonkim/maker/calculator-app
```

Expected: `App.tsx`, `app.json`, `package.json`, `tsconfig.json`, `.gitignore`, `node_modules`, `assets` 등이 보인다.

- [ ] **Step 4: 타입 체크로 프로젝트가 정상 동작하는지 확인**

```bash
cd /Users/soonkim/maker/calculator-app
npx tsc --noEmit
```

Expected: 에러 없이 종료 (exit code 0).

- [ ] **Step 5: Commit**

```bash
cd /Users/soonkim/maker
git add calculator-app
git commit -m "Scaffold Expo TypeScript project for calculator app"
```

---

### Task 2: Jest 테스트 환경 설정

**Files:**
- Modify: `calculator-app/package.json`

**Interfaces:**
- Consumes: Task 1에서 생성된 `calculator-app/package.json`
- Produces: `npm test` 명령으로 Jest가 실행되는 환경. Task 3의 `calculator.test.ts`가 이 설정으로 동작한다.

- [ ] **Step 1: jest-expo와 관련 패키지 설치**

```bash
cd /Users/soonkim/maker/calculator-app
npx expo install jest-expo jest @types/jest --dev
```

Expected: `package.json`의 `devDependencies`에 `jest-expo`, `jest`, `@types/jest`가 추가된다.

- [ ] **Step 2: `package.json`에 jest 설정과 test 스크립트 추가**

`package.json`의 `"scripts"` 블록에 `"test"`를 추가하고, 최상위에 `"jest"` 설정 블록을 추가한다:

```json
  "scripts": {
    "start": "expo start",
    "android": "expo start --android",
    "ios": "expo start --ios",
    "web": "expo start --web",
    "test": "jest"
  },
  "jest": {
    "preset": "jest-expo"
  },
```

- [ ] **Step 3: 테스트 명령이 동작하는지 확인 (아직 테스트 파일 없음)**

```bash
npx jest --listTests
```

Expected: 에러 없이 종료 (테스트 파일이 아직 없으므로 목록은 비어 있을 수 있음, exit code 0).

- [ ] **Step 4: Commit**

```bash
cd /Users/soonkim/maker
git add calculator-app/package.json calculator-app/package-lock.json
git commit -m "Add Jest test setup to calculator-app"
```

---

### Task 3: 계산 로직 모듈 (`src/calculator.ts`) — TDD

**Files:**
- Create: `calculator-app/src/calculator.ts`
- Test: `calculator-app/src/calculator.test.ts`

**Interfaces:**
- Consumes: 없음 (순수 함수, 외부 의존성 없음)
- Produces:
  - `export function inputDigit(expression: string, digit: string): string`
  - `export function inputPlus(expression: string): string`
  - `export function backspace(expression: string): string`
  - `export function clearAll(): string`
  - `export function calculate(expression: string): string`
  - Task 4의 `App.tsx`가 이 5개 함수를 `src/calculator`에서 import해서 사용한다.

- [ ] **Step 1: 실패하는 테스트 작성**

`calculator-app/src/calculator.test.ts`:

```ts
import { backspace, calculate, clearAll, inputDigit, inputPlus } from './calculator';

describe('inputDigit', () => {
  test('appends a digit', () => {
    expect(inputDigit('1', '2')).toBe('12');
  });

  test('appends "0." when starting a decimal on an empty expression', () => {
    expect(inputDigit('', '.')).toBe('0.');
  });

  test('appends "0." when starting a decimal after a trailing +', () => {
    expect(inputDigit('1+', '.')).toBe('1+0.');
  });

  test('ignores a second decimal point in the same term', () => {
    expect(inputDigit('1.5', '.')).toBe('1.5');
  });

  test('allows a decimal point in a new term after +', () => {
    expect(inputDigit('1.5+2', '.')).toBe('1.5+2.');
  });
});

describe('inputPlus', () => {
  test('appends + to a non-empty expression', () => {
    expect(inputPlus('1')).toBe('1+');
  });

  test('ignores + on an empty expression', () => {
    expect(inputPlus('')).toBe('');
  });

  test('ignores a second consecutive +', () => {
    expect(inputPlus('1+')).toBe('1+');
  });
});

describe('backspace', () => {
  test('removes the last character', () => {
    expect(backspace('12')).toBe('1');
  });

  test('returns empty string when expression is already empty', () => {
    expect(backspace('')).toBe('');
  });
});

describe('clearAll', () => {
  test('returns an empty string', () => {
    expect(clearAll()).toBe('');
  });
});

describe('calculate', () => {
  test('sums a two-term expression', () => {
    expect(calculate('1+2')).toBe('3');
  });

  test('sums a multi-term expression with decimals', () => {
    expect(calculate('1.5+2.5+1')).toBe('5');
  });

  test('returns a single number unchanged', () => {
    expect(calculate('7')).toBe('7');
  });

  test('ignores calculation on an empty expression', () => {
    expect(calculate('')).toBe('');
  });

  test('ignores calculation on an expression ending with +', () => {
    expect(calculate('1+')).toBe('1+');
  });
});
```

- [ ] **Step 2: 테스트가 실패하는지 확인 (모듈이 아직 없음)**

```bash
cd /Users/soonkim/maker/calculator-app
npx jest src/calculator.test.ts
```

Expected: FAIL — `Cannot find module './calculator'`.

- [ ] **Step 3: 최소 구현 작성**

`calculator-app/src/calculator.ts`:

```ts
export function inputDigit(expression: string, digit: string): string {
  if (digit === '.') {
    const parts = expression.split('+');
    const current = parts[parts.length - 1];
    if (current.includes('.')) return expression;
    if (current === '') return expression + '0' + digit;
  }
  return expression + digit;
}

export function inputPlus(expression: string): string {
  if (expression === '' || expression.endsWith('+')) return expression;
  return expression + '+';
}

export function backspace(expression: string): string {
  return expression.slice(0, -1);
}

export function clearAll(): string {
  return '';
}

export function calculate(expression: string): string {
  if (expression === '' || expression.endsWith('+')) return expression;
  const sum = expression
    .split('+')
    .reduce((total, n) => total + parseFloat(n || '0'), 0);
  return String(sum);
}
```

- [ ] **Step 4: 테스트가 통과하는지 확인**

```bash
npx jest src/calculator.test.ts
```

Expected: PASS — 모든 테스트(총 15개) 통과.

- [ ] **Step 5: Commit**

```bash
cd /Users/soonkim/maker
git add calculator-app/src/calculator.ts calculator-app/src/calculator.test.ts
git commit -m "Add addition-only calculator logic with tests"
```

---

### Task 4: 계산기 UI (`App.tsx`)

**Files:**
- Modify: `calculator-app/App.tsx` (Task 1에서 생성된 기본 템플릿 내용을 전부 교체)

**Interfaces:**
- Consumes: Task 3의 `inputDigit`, `inputPlus`, `backspace`, `clearAll`, `calculate` (from `./src/calculator`)
- Produces: 앱의 기본 화면. 이후 태스크는 이 파일을 참조하지 않는다 (마지막 UI 태스크).

- [ ] **Step 1: `App.tsx`를 계산기 UI로 교체**

`calculator-app/App.tsx`:

```tsx
import React, { useState } from 'react';
import { SafeAreaView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { backspace, calculate, clearAll, inputDigit, inputPlus } from './src/calculator';

type CalcButton = {
  label: string;
  apply: (expression: string) => string;
  span2?: boolean;
  op?: boolean;
};

const ROWS: CalcButton[][] = [
  [
    { label: '7', apply: (e) => inputDigit(e, '7') },
    { label: '8', apply: (e) => inputDigit(e, '8') },
    { label: '9', apply: (e) => inputDigit(e, '9') },
    { label: 'C', apply: () => clearAll() },
  ],
  [
    { label: '4', apply: (e) => inputDigit(e, '4') },
    { label: '5', apply: (e) => inputDigit(e, '5') },
    { label: '6', apply: (e) => inputDigit(e, '6') },
    { label: '←', apply: (e) => backspace(e) },
  ],
  [
    { label: '1', apply: (e) => inputDigit(e, '1') },
    { label: '2', apply: (e) => inputDigit(e, '2') },
    { label: '3', apply: (e) => inputDigit(e, '3') },
    { label: '+', apply: (e) => inputPlus(e), op: true },
  ],
  [
    { label: '0', apply: (e) => inputDigit(e, '0') },
    { label: '.', apply: (e) => inputDigit(e, '.') },
    { label: '=', apply: (e) => calculate(e), span2: true, op: true },
  ],
];

export default function App() {
  const [expression, setExpression] = useState('');
  const display = expression === '' ? '0' : expression;

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.calculator}>
        <View style={styles.display}>
          <Text style={styles.displayText}>{display}</Text>
        </View>
        {ROWS.map((row, rowIndex) => (
          <View key={rowIndex} style={styles.row}>
            {row.map((button, buttonIndex) => (
              <TouchableOpacity
                key={button.label}
                style={[
                  styles.button,
                  button.span2 && styles.buttonSpan2,
                  buttonIndex === row.length - 1 && styles.buttonLast,
                ]}
                onPress={() => setExpression((current) => button.apply(current))}
              >
                <Text style={[styles.buttonText, button.op && styles.opText]}>
                  {button.label}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        ))}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#808080',
    alignItems: 'center',
    justifyContent: 'center',
  },
  calculator: {
    backgroundColor: '#808080',
    borderRadius: 16,
    padding: 24,
    width: 300,
  },
  display: {
    backgroundColor: '#fff',
    borderRadius: 8,
    paddingVertical: 24,
    paddingHorizontal: 16,
    marginBottom: 16,
  },
  displayText: {
    color: '#000',
    fontSize: 40,
    fontWeight: '500',
    textAlign: 'right',
  },
  row: {
    flexDirection: 'row',
    marginBottom: 12,
  },
  button: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 8,
    paddingVertical: 16,
    marginRight: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonLast: {
    marginRight: 0,
  },
  buttonSpan2: {
    flex: 2,
  },
  buttonText: {
    fontSize: 24,
    fontWeight: '500',
    color: '#000',
  },
  opText: {
    fontWeight: '700',
  },
});
```

- [ ] **Step 2: 타입 체크**

```bash
cd /Users/soonkim/maker/calculator-app
npx tsc --noEmit
```

Expected: 에러 없이 종료.

- [ ] **Step 3: 기존 계산 로직 테스트가 여전히 통과하는지 확인**

```bash
npx jest
```

Expected: PASS — Task 3의 15개 테스트 모두 통과 (App.tsx는 UI라 별도 테스트 없음).

- [ ] **Step 4: (수동 확인, 선택) 개발 서버로 동작 확인**

```bash
npx expo start
```

Expected: QR 코드/메뉴가 뜨고, `w`를 눌러 웹 브라우저 또는 Expo Go 앱으로 열어 숫자 입력 → `+` → `=` 흐름이 기존 `계산기.html`과 동일하게 동작하는지 눈으로 확인. 확인 후 `Ctrl+C`로 종료.

- [ ] **Step 5: Commit**

```bash
cd /Users/soonkim/maker
git add calculator-app/App.tsx
git commit -m "Implement calculator UI matching 계산기.html design"
```

---

### Task 5: Android 네이티브 프로젝트 생성 (`expo prebuild`)

**Files:**
- Create: `calculator-app/android/` (Expo가 생성하는 네이티브 Android 프로젝트 전체)
- Create: `calculator-app/ios/` (Expo가 함께 생성 — 이 머신에서는 빌드하지 않음)

**Interfaces:**
- Consumes: Task 1의 `app.json` (앱 이름/패키지명 설정에 사용됨)
- Produces: `calculator-app/android/gradlew` — Task 6에서 이 스크립트로 APK를 빌드한다.

- [ ] **Step 1: `app.json`에 Android 패키지명 설정 확인/추가**

`calculator-app/app.json`을 열어 `expo.android.package`가 없으면 추가한다 (`"com.maker.calculator"`):

```json
{
  "expo": {
    "name": "계산기",
    "slug": "calculator-app",
    "android": {
      "package": "com.maker.calculator"
    }
  }
}
```

기존 `app.json`의 다른 필드(아이콘, splash 등 Expo 기본값)는 그대로 둔다.

- [ ] **Step 2: 네이티브 Android 프로젝트 생성**

```bash
cd /Users/soonkim/maker/calculator-app
npx expo prebuild --platform android
```

Expected: `android/` 디렉토리가 생성되고, `android/gradlew`, `android/app/build.gradle`, `android/settings.gradle` 등이 존재한다.

- [ ] **Step 3: 생성 결과 확인**

```bash
ls android
test -x android/gradlew && echo "gradlew is executable"
```

Expected: `gradlew is executable` 출력.

- [ ] **Step 4: Commit**

`android/`, `ios/`는 빌드 산출물 성격이 강해 커밋하지 않는다 (Expo 기본 `.gitignore`가 이미 `/android`, `/ios`를 무시하도록 되어 있는지 확인).

```bash
cd /Users/soonkim/maker/calculator-app
git check-ignore -v android || echo "NOT IGNORED — add android/ and ios/ to .gitignore"
```

Expected: `android`가 `.gitignore`에 의해 무시된다는 출력. 만약 "NOT IGNORED"가 출력되면 `.gitignore`에 다음을 추가하고 커밋한다:

```
/android
/ios
```

```bash
cd /Users/soonkim/maker
git add calculator-app/app.json calculator-app/.gitignore
git commit -m "Configure Android package name for calculator app"
```

---

### Task 6: 로컬 Android 디버그 APK 빌드

**Files:**
- 없음 (기존 `android/` 프로젝트를 빌드만 함, 소스 수정 없음)

**Interfaces:**
- Consumes: Task 5의 `calculator-app/android/gradlew`
- Produces: `calculator-app/android/app/build/outputs/apk/debug/app-debug.apk` — 최종 산출물, 이후 태스크 없음.

- [ ] **Step 1: Java/Android SDK 환경 변수 설정 후 빌드**

```bash
cd /Users/soonkim/maker/calculator-app/android
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL` 로 종료. (최초 실행 시 Gradle 의존성 다운로드로 수 분 소요될 수 있음)

- [ ] **Step 2: 빌드 실패 시 흔한 원인 확인**

- `SDK location not found` 에러가 나면 `calculator-app/android/local.properties` 파일을 만들고 다음 한 줄을 추가한다: `sdk.dir=/Users/soonkim/Library/Android/sdk`
- 라이선스 미동의 에러(`Failed to install ... licences`)가 나면 다음을 실행 후 재시도한다: `yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses` (또는 `$ANDROID_HOME/tools/bin/sdkmanager`가 있으면 그 경로 사용)
- 위 조치 후 `./gradlew assembleDebug`를 다시 실행한다.

- [ ] **Step 3: APK 생성 확인**

```bash
ls -la /Users/soonkim/maker/calculator-app/android/app/build/outputs/apk/debug/app-debug.apk
```

Expected: 파일이 존재하고 크기가 0보다 크다 (일반적으로 수십 MB).

- [ ] **Step 4: 최종 확인 보고**

커밋할 소스 변경 없음 (빌드 산출물). APK 경로를 사용자에게 안내한다: `calculator-app/android/app/build/outputs/apk/debug/app-debug.apk`

---

## Self-Review Notes

- **Spec coverage:** 덧셈 전용 로직(Task 3), 동일 디자인 UI(Task 4), Expo 기반 iOS/Android 겸용 구조(Task 1, 5), Android APK 로컬 빌드(Task 5-6), Jest 유닛 테스트(Task 2-3) — 스펙의 모든 섹션이 태스크로 커버됨.
- **Placeholder scan:** 모든 스텝에 실행 가능한 실제 커맨드/코드 포함. "TBD" 없음.
- **Type consistency:** `src/calculator.ts`의 5개 함수 시그니처가 Task 3(정의)과 Task 4(`App.tsx`에서 import)에서 동일하게 사용됨 (`inputDigit(expression, digit)`, `inputPlus(expression)`, `backspace(expression)`, `clearAll()`, `calculate(expression)`).

#!/usr/bin/env python3
"""Build a test APK offline with the Android SDK and JDK, without Gradle downloads."""
import os
from pathlib import Path
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
SDK = Path(os.environ.get('ANDROID_HOME', str(Path.home() / 'Library/Android/sdk')))
JAVA = Path(os.environ.get('JAVA_HOME', '/Applications/Android Studio.app/Contents/jbr/Contents/Home'))
BUILD = ROOT / 'shift/build/offline'
DIST = ROOT / 'dist'
BT = SDK / 'build-tools/36.0.0'
ANDROID = SDK / 'platforms/android-36/android.jar'
env = dict(os.environ, JAVA_HOME=str(JAVA))

def run(*args):
    subprocess.run([str(a) for a in args], check=True, cwd=ROOT, env=env)

for directory in [BUILD / 'classes', BUILD / 'dex', BUILD / 'tests', BUILD / 'generated', DIST]:
    if directory != DIST and directory.exists():
        shutil.rmtree(directory)
    directory.mkdir(parents=True, exist_ok=True)
source = ROOT / 'shift/src/main'
manifest = BUILD / 'AndroidManifest.xml'
manifest.write_text((source / 'AndroidManifest.xml').read_text().replace('<manifest ', '<manifest package="com.soon.shiftwork" ', 1))
run(JAVA / 'bin/javac', '-encoding', 'UTF-8', '-d', BUILD / 'tests',
    source / 'java/com/soon/shiftwork/ShiftPattern.java', source / 'java/com/soon/shiftwork/KoreanHolidays.java', *sorted((ROOT / 'shift/tests').glob('*.java')))
run(JAVA / 'bin/java', '-cp', BUILD / 'tests', 'ShiftPatternTest')
run(JAVA / 'bin/java', '-cp', BUILD / 'tests', 'KoreanHolidaysTest')
run(BT / 'aapt2', 'compile', '--dir', source / 'res', '-o', BUILD / 'resources.zip')
run(BT / 'aapt2', 'link', '-o', BUILD / 'base.apk', '-I', ANDROID,
    '--manifest', manifest, '--min-sdk-version', '26',
    '--target-sdk-version', '36', '--java', BUILD / 'generated', BUILD / 'resources.zip')
run(JAVA / 'bin/javac', '-encoding', 'UTF-8', '-source', '17', '-target', '17',
    '-classpath', ANDROID, '-d', BUILD / 'classes', *sorted((source / 'java').rglob('*.java')), *sorted((BUILD / 'generated').rglob('*.java')))
run(BT / 'd8', '--lib', ANDROID, '--min-api', '26', '--output', BUILD / 'dex',
    *sorted((BUILD / 'classes').rglob('*.class')))
with zipfile.ZipFile(BUILD / 'base.apk', 'a') as apk:
    apk.write(BUILD / 'dex/classes.dex', 'classes.dex')
run(BT / 'zipalign', '-f', '4', BUILD / 'base.apk', BUILD / 'aligned.apk')
key = BUILD / 'test.keystore'
if not key.exists():
    run(JAVA / 'bin/keytool', '-genkeypair', '-keystore', key, '-storepass', 'android',
        '-keypass', 'android', '-alias', 'androiddebugkey', '-keyalg', 'RSA', '-keysize', '2048',
        '-validity', '10000', '-dname', 'CN=Android Debug,O=Android,C=US')
run(BT / 'apksigner', 'sign', '--ks', key, '--ks-pass', 'pass:android',
    '--out', DIST / 'shift-work.apk', BUILD / 'aligned.apk')
run(BT / 'apksigner', 'verify', '--verbose', DIST / 'shift-work.apk')
print('APK:', DIST / 'shift-work.apk')

#!/usr/bin/env python3
import os
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[1]
SDK=Path(os.environ.get('ANDROID_HOME',str(Path.home()/'Library/Android/sdk')))
JAVA=Path(os.environ.get('JAVA_HOME','/Applications/Android Studio.app/Contents/jbr/Contents/Home'))
BT=SDK/'build-tools/36.0.0'; ANDROID=SDK/'platforms/android-36/android.jar'
BUILD=ROOT/'shift/build/runtime-tests'; MAIN=ROOT/'shift/build/offline'
for folder in [BUILD/'classes',BUILD/'dex']:folder.mkdir(parents=True,exist_ok=True)
def run(*args):subprocess.run([str(x) for x in args],check=True,env=dict(os.environ,JAVA_HOME=str(JAVA)))
run(JAVA/'bin/javac','-encoding','UTF-8','-source','17','-target','17','-classpath',str(ANDROID)+os.pathsep+str(MAIN/'classes'),'-d',BUILD/'classes',*sorted((ROOT/'shift/androidTest').glob('*.java')))
run(BT/'aapt2','link','-o',BUILD/'base.apk','-I',ANDROID,'--manifest',ROOT/'shift/androidTest/AndroidManifest.xml')
run(BT/'d8','--lib',ANDROID,'--classpath',MAIN/'classes','--min-api','26','--output',BUILD/'dex',*sorted((BUILD/'classes').rglob('*.class')))
import zipfile
with zipfile.ZipFile(BUILD/'base.apk','a') as apk:apk.write(BUILD/'dex/classes.dex','classes.dex')
run(BT/'zipalign','-f','4',BUILD/'base.apk',BUILD/'aligned.apk')
run(BT/'apksigner','sign','--ks',MAIN/'test.keystore','--ks-pass','pass:android','--out',BUILD/'runtime-tests.apk',BUILD/'aligned.apk')

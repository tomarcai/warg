#!/bin/sh
cd "`dirname "$0"`"
java -Ddebug=true -cp ../lib/midletrunner.jar:../lib/m3gbasic.jar:../lib/SwingME.jar:../../android/libs/LobbyCore.jar:../lib/Grasshopper.jar:../dist/Domination4ME.jar org.me4se.MIDletRunner -width 320 -height 480 net.yura.domination.mobile.simplegui.DominationMidlet


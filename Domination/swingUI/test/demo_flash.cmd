rem N70
cd %~dp0
java -cp ../lib/midletrunner.jar;../lib/m3gbasic.jar;../lib/SwingME.jar;../../sharedUI/lib/LobbyCore.jar;../../sharedUI/lib/MiniLobbyClient.jar;../lib/Grasshopper.jar;../dist/Domination4ME.jar org.me4se.MIDletRunner -width 320 -height 480 net.yura.domination.mobile.flashgui.DominationMain
pause

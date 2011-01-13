@echo off
del YuchBerrySvr.key

echo ------------Éú³ÉÃÜÔ¿¶Ô------------
keytool -genkey -alias serverkey -keystore YuchBerrySvr.key

pause
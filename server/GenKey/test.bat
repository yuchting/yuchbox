@echo off

keytool -genkey -alias serverkey -validity 365 -keyalg RSA -keysize 1024 -keypass 111111 -storepass 111111 -keystore YuchBerrySvr.key

echo ------------Éú³ÉÃÜÔ¿¶Ô------------

keytool -export -keypass 111111 -storepass 111111 -alias serverkey -keystore YuchBerrySvr.key -file client.crt 


pause
@echo off

mkdir newcerts
mkdir certs
mkdir crl

del YuchBerrySvr.key

echo ------------Éú³ÉÃÜÔ¿¶Ô------------
keytool -genkey -alias serverkey -keystore YuchBerrySvr.key

pause
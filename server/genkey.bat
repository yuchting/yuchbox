@echo off
mkdir newcerts
mkdir certs
mkdir crl

echo ------------Éú³ÉÃÜÔ¿¶Ô------------
keytool -genkey -alias serverkey -keystore YuchBerrySvr.key


pause
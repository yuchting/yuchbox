@echo off

echo ------------生成密钥对------------
keytool -keystore YuchBerrySvr.key -genkeypair -alias serverkey

echo ------------生成密钥对------------
echo keytool -genkey -v -alias YuchBerryServerKey -keyalg RSA -keystore YuchBerrySvr.key

echo ------------为客户端生成证书---------
echo keytool -genkey -keystore YuchBerryClient.p12 -alias YuchBerryServerKey -keyalg RSA -storetype PKCS12

echo ------------让服务器信任客户端证书---------
echo keytool -export -file YuchBerryClient.cer -keystore YuchBerryClient.p12 -alias YuchBerryServerKey -storetype PKCS12 -rfc

echo keytool -import -v -file YuchBerryClient.cer -keystore YuchBerrySvr.key

pause
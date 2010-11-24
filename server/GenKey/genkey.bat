@echo off
mkdir newcerts
mkdir certs
mkdir crl

echo ------------生成密钥对------------
keytool -keystore YuchBerrySvr.key -genkeypair -alias serverkey

echo ------------生成证书签名请求------------
keytool -keystore YuchBerrySvr.key -certreq -alias serverkey -file YuchBerrySvr.csr

echo ------------生成CA的自签名证书------------
openssl req -new -x509 -keyout YuchBerryClient.key -out YuchBerryClient.crt -config openssl.conf

echo ------------根证书导入证书库------------
keytool -keystore YuchBerrySvr.key -importcert -alias root -file cacert.pem

echo ------------把生成的证书导入到keystore------------
keytool -keystore YuchBerrySvr.key -importcert -alias clientkey -file YuchBerryClient.crt


pause
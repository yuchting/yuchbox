@echo off
mkdir newcerts
mkdir certs
mkdir crl

echo ------------生成密钥对------------
keytool -keystore YuchBerrySvr.key -genkeypair -alias serverkey

echo ------------生成证书签名请求------------
keytool -keystore YuchBerrySvr.key -certreq -alias serverkey -file YuchBerrySvr.csr

echo ------------生成CA的自签名证书------------
openssl req -new -x509 -keyout ca.key -out ca.crt -config openssl.conf

echo ------------用CA私钥进行签名-------------
openssl ca -in YuchBerrySvr.csr -out YuchBerryClient.crt -cert ca.crt -keyfile ca.key -notext -config openssl.conf

echo ------------导入信任的CA根证书到keystore-------------
keytool -import -v -trustcacerts  -alias my_ca_root -file ca.crt -keystore YuchBerrySvr.key

echo ------------把生成的证书导入到keystore------------
keytool -keystore YuchBerrySvr.key -importcert -alias clientkey -file YuchBerryClient.crt

pause
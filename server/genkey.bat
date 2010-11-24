@echo off
mkdir newcerts
mkdir certs
mkdir crl

echo ------------生成密钥对------------
keytool -genkey -alias serverkey -keystore YuchBerrySvr.key

echo ------------生成证书签名请求------------
keytool -certreq -alias serverkey -keystore YuchBerrySvr.key -file YuchBerrySvr.csr

echo ------------生成CA的自签名证书------------
openssl req -new -x509 -keyout YuchBerryClient.key -out YuchBerryClient.crt -config openssl.conf

echo ------------用CA私钥进行签名------------
openssl ca -in YuchBerrySvr.csr -out tmp.crt -cert YuchBerryClient.crt -keyfile YuchBerryClient.key -notext -config openssl.conf

echo ------------导入信任的CA根证书到keystore------------
keytool -import -v -alias clientkey -file YuchBerryClient.crt -keystore YuchBerrySvr.key

echo ------------把CA签名后的证书导入到keystore------------
keytool -import -v -trustcacerts -alias serverkey -file tmp.crt -keystore YuchBerrySvr.key

del tmp.crt

pause
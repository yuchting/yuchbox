@echo off
mkdir newcerts
mkdir certs
mkdir crl

echo ------------生成密钥对------------
keytool -keystore YuchBerrySvr.key -genkeypair -alias serverkey

echo ------------生成证书签名请求------------
keytool -keystore YuchBerrySvr.key -certreq -alias YuchBerrySvrKey -file YuchBerrySvr.csr

echo ------------生成CA的自签名证书------------
openssl req -new -x509 -keyout YuchBerryCA.key -out YuchBerryCA.crt -config openssl.conf

echo ------------用CA私钥进行签名-------------
openssl ca -in YuchBerrySvr.csr -out YuchBerryClient.cer -cert YuchBerryCA.crt -keyfile YuchBerryCA.key -notext -config openssl.conf

echo ------------导入信任的CA根证书到keystore-------------
keytool -import -v -trustcacerts  -alias YuchBerryCA -file YuchBerryCA.crt -keystore YuchBerrySvr.key

echo ------------把生成的证书导入到keystore------------
keytool -keystore YuchBerrySvr.key -importcert -alias YuchBerrySvrKey -file YuchBerryClient.cer


pause
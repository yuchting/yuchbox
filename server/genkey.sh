#!/bin/sh 

filename=./YuchBerrySvr.key

if   [   -e   "$filename"   ]; then 
       rm $filename
fi 

echo ------------生成密钥对------------
keytool -genkey -alias serverkey -keystore $filename -validity 3650

pause
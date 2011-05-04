@echo off
echo ------------yuchberry 提示------------
echo 请使用前仔细阅读 
echo.
echo           http://code.google.com/p/yuchberry/wiki/SSL_yuchberry 
echo. 
echo 弄清楚SSL方式的yuchberry之后，再生成密钥
echo.
echo 如果出现 keytool不是内部命令 之类的文字提示，请在JRE安装目录下面查找，例如：
echo.
echo           C:\Program Files\Java\jre6\bin
echo.
echo 将这个批处理文件拷贝到这个目录下运行，然后再吧生成的密钥 YuchBerrySvr.key 复制回来。或者设置PATH环境变量，不会设置的请自行搜索。



if exist YuchBerrySvr.key del YuchBerrySvr.key

echo ------------下面生成密钥对------------
keytool -genkey -alias serverkey -keystore YuchBerrySvr.key

pause
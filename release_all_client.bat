@echo off
@echo 发布客户端版本批处理程序
@echo. 
set /p var=请输入要发布的版本号:
rd /S /Q release_client_%var%

md release_client_%var%\yuchbox_%var%_for_4.2os\4.2.1
md release_client_%var%\yuchbox_%var%_for_4.2os\WebOTA

md release_client_%var%\yuchbox_%var%_for_4.5os\4.5.0
md release_client_%var%\yuchbox_%var%_for_4.5os\WebOTA

md release_client_%var%\yuchbox_%var%_for_4.6os\4.6.0
md release_client_%var%\yuchbox_%var%_for_4.6os\WebOTA

md release_client_%var%\yuchbox_%var%_for_4.7os\4.7.0
md release_client_%var%\yuchbox_%var%_for_4.7os\WebOTA

md release_client_%var%\yuchbox_%var%_for_5.0os\5.0.0
md release_client_%var%\yuchbox_%var%_for_5.0os\WebOTA

md release_client_%var%\yuchbox_%var%_for_6.0os\6.0.0
md release_client_%var%\yuchbox_%var%_for_6.0os\WebOTA

md release_client_%var%\yuchbox_%var%_for_7.0os\7.0.0
md release_client_%var%\yuchbox_%var%_for_7.0os\WebOTA

md release_client_%var%\yuchbox_%var%_for_7.1os\7.1.0
md release_client_%var%\yuchbox_%var%_for_7.1os\WebOTA

copy .\yuchbox42\deliverables\Standard\4.2.1\*.cod .\release_client_%var%\yuchbox_%var%_for_4.2os\4.2.1\
copy .\yuchbox42\deliverables\Standard\*.alx .\release_client_%var%\yuchbox_%var%_for_4.2os\
copy .\yuchbox42\deliverables\Web\4.2.1\*.cod .\release_client_%var%\yuchbox_%var%_for_4.2os\WebOTA\
copy .\yuchbox42\deliverables\Web\4.2.1\*.jad .\release_client_%var%\yuchbox_%var%_for_4.2os\WebOTA\

copy .\yuchbox45\deliverables\Standard\4.5.0\*.cod .\release_client_%var%\yuchbox_%var%_for_4.5os\4.5.0\
copy .\yuchbox45\deliverables\Standard\*.alx .\release_client_%var%\yuchbox_%var%_for_4.5os\
copy .\yuchbox45\deliverables\Web\4.5.0\*.cod .\release_client_%var%\yuchbox_%var%_for_4.5os\WebOTA\
copy .\yuchbox45\deliverables\Web\4.5.0\*.jad .\release_client_%var%\yuchbox_%var%_for_4.5os\WebOTA\

copy .\yuchbox46\deliverables\Standard\4.6.0\*.cod .\release_client_%var%\yuchbox_%var%_for_4.6os\4.6.0\
copy .\yuchbox46\deliverables\Standard\*.alx .\release_client_%var%\yuchbox_%var%_for_4.6os\
copy .\yuchbox46\deliverables\Web\4.6.0\*.cod .\release_client_%var%\yuchbox_%var%_for_4.6os\WebOTA\
copy .\yuchbox46\deliverables\Web\4.6.0\*.jad .\release_client_%var%\yuchbox_%var%_for_4.6os\WebOTA\

copy .\yuchbox47\deliverables\Standard\4.7.0\*.cod .\release_client_%var%\yuchbox_%var%_for_4.7os\4.7.0\
copy .\yuchbox47\deliverables\Standard\*.alx .\release_client_%var%\yuchbox_%var%_for_4.7os\
copy .\yuchbox47\deliverables\Web\4.7.0\*.cod .\release_client_%var%\yuchbox_%var%_for_4.7os\WebOTA\
copy .\yuchbox47\deliverables\Web\4.7.0\*.jad .\release_client_%var%\yuchbox_%var%_for_4.7os\WebOTA\

copy .\yuchbox50\deliverables\Standard\5.0.0\*.cod .\release_client_%var%\yuchbox_%var%_for_5.0os\5.0.0\
copy .\yuchbox50\deliverables\Standard\*.alx .\release_client_%var%\yuchbox_%var%_for_5.0os\
copy .\yuchbox50\deliverables\Web\5.0.0\*.cod .\release_client_%var%\yuchbox_%var%_for_5.0os\WebOTA\
copy .\yuchbox50\deliverables\Web\5.0.0\*.jad .\release_client_%var%\yuchbox_%var%_for_5.0os\WebOTA\

copy .\yuchbox60\deliverables\Standard\6.0.0\*.cod .\release_client_%var%\yuchbox_%var%_for_6.0os\6.0.0\
copy .\yuchbox60\deliverables\Standard\*.alx .\release_client_%var%\yuchbox_%var%_for_6.0os\
copy .\yuchbox60\deliverables\Web\6.0.0\*.cod .\release_client_%var%\yuchbox_%var%_for_6.0os\WebOTA\
copy .\yuchbox60\deliverables\Web\6.0.0\*.jad .\release_client_%var%\yuchbox_%var%_for_6.0os\WebOTA\

copy .\yuchbox70\deliverables\Standard\7.0.0\*.cod .\release_client_%var%\yuchbox_%var%_for_7.0os\7.0.0\
copy .\yuchbox70\deliverables\Standard\*.alx .\release_client_%var%\yuchbox_%var%_for_7.0os\
copy .\yuchbox70\deliverables\Web\7.0.0\*.cod .\release_client_%var%\yuchbox_%var%_for_7.0os\WebOTA\
copy .\yuchbox70\deliverables\Web\7.0.0\*.jad .\release_client_%var%\yuchbox_%var%_for_7.0os\WebOTA\

copy .\yuchbox71\deliverables\Standard\7.1.0\*.cod .\release_client_%var%\yuchbox_%var%_for_7.1os\7.1.0\
copy .\yuchbox71\deliverables\Standard\*.alx .\release_client_%var%\yuchbox_%var%_for_7.1os\
copy .\yuchbox71\deliverables\Web\7.1.0\*.cod .\release_client_%var%\yuchbox_%var%_for_7.1os\WebOTA\
copy .\yuchbox71\deliverables\Web\7.1.0\*.jad .\release_client_%var%\yuchbox_%var%_for_7.1os\WebOTA\

7z a .\release_client_%var%\yuchbox_%var%_for_4.2os.zip .\release_client_%var%\yuchbox_%var%_for_4.2os\
7z a .\release_client_%var%\yuchbox_%var%_for_4.5os.zip .\release_client_%var%\yuchbox_%var%_for_4.5os\
7z a .\release_client_%var%\yuchbox_%var%_for_4.6os.zip .\release_client_%var%\yuchbox_%var%_for_4.6os\
7z a .\release_client_%var%\yuchbox_%var%_for_4.7os.zip .\release_client_%var%\yuchbox_%var%_for_4.7os\
7z a .\release_client_%var%\yuchbox_%var%_for_5.0os.zip .\release_client_%var%\yuchbox_%var%_for_5.0os\
7z a .\release_client_%var%\yuchbox_%var%_for_6.0os.zip .\release_client_%var%\yuchbox_%var%_for_6.0os\
7z a .\release_client_%var%\yuchbox_%var%_for_7.0os.zip .\release_client_%var%\yuchbox_%var%_for_7.0os\
7z a .\release_client_%var%\yuchbox_%var%_for_7.1os.zip .\release_client_%var%\yuchbox_%var%_for_7.1os\

pause
@echo off
@echo 发布客户端版本批处理程序
@echo. 
set /p var=请输入要发布的版本号:
rd /S /Q release_client_%var%

md release_client_%var%\yuchberry_%var%_for_4.2os\4.2.1
md release_client_%var%\yuchberry_%var%_for_4.2os\WebOTA

md release_client_%var%\yuchberry_%var%_for_4.5os\4.5.0
md release_client_%var%\yuchberry_%var%_for_4.5os\WebOTA

md release_client_%var%\yuchberry_%var%_for_4.6os\4.6.0
md release_client_%var%\yuchberry_%var%_for_4.6os\WebOTA

md release_client_%var%\yuchberry_%var%_for_4.7os\4.7.0
md release_client_%var%\yuchberry_%var%_for_4.7os\WebOTA

md release_client_%var%\yuchberry_%var%_for_5.0os\5.0.0
md release_client_%var%\yuchberry_%var%_for_5.0os\WebOTA

md release_client_%var%\yuchberry_%var%_for_6.0os\6.0.0
md release_client_%var%\yuchberry_%var%_for_6.0os\WebOTA

copy .\client42\deliverables\Standard\4.2.1\*.cod .\release_client_%var%\yuchberry_%var%_for_4.2os\4.2.1\
copy .\client42\deliverables\Standard\*.alx .\release_client_%var%\yuchberry_%var%_for_4.2os\
copy .\client42\deliverables\Web\4.2.1\*.cod .\release_client_%var%\yuchberry_%var%_for_4.2os\WebOTA\
copy .\client42\deliverables\Web\4.2.1\*.jad .\release_client_%var%\yuchberry_%var%_for_4.2os\WebOTA\

copy .\client45\deliverables\Standard\4.5.0\*.cod .\release_client_%var%\yuchberry_%var%_for_4.5os\4.5.0\
copy .\client45\deliverables\Standard\*.alx .\release_client_%var%\yuchberry_%var%_for_4.5os\
copy .\client45\deliverables\Web\4.5.0\*.cod .\release_client_%var%\yuchberry_%var%_for_4.5os\WebOTA\
copy .\client45\deliverables\Web\4.5.0\*.jad .\release_client_%var%\yuchberry_%var%_for_4.5os\WebOTA\

copy .\client46\deliverables\Standard\4.6.0\*.cod .\release_client_%var%\yuchberry_%var%_for_4.6os\4.6.0\
copy .\client46\deliverables\Standard\*.alx .\release_client_%var%\yuchberry_%var%_for_4.6os\
copy .\client46\deliverables\Web\4.6.0\*.cod .\release_client_%var%\yuchberry_%var%_for_4.6os\WebOTA\
copy .\client46\deliverables\Web\4.6.0\*.jad .\release_client_%var%\yuchberry_%var%_for_4.6os\WebOTA\

copy .\client47\deliverables\Standard\4.7.0\*.cod .\release_client_%var%\yuchberry_%var%_for_4.7os\4.7.0\
copy .\client47\deliverables\Standard\*.alx .\release_client_%var%\yuchberry_%var%_for_4.7os\
copy .\client47\deliverables\Web\4.7.0\*.cod .\release_client_%var%\yuchberry_%var%_for_4.7os\WebOTA\
copy .\client47\deliverables\Web\4.7.0\*.jad .\release_client_%var%\yuchberry_%var%_for_4.7os\WebOTA\

copy .\client\deliverables\Standard\5.0.0\*.cod .\release_client_%var%\yuchberry_%var%_for_5.0os\5.0.0\
copy .\client\deliverables\Standard\*.alx .\release_client_%var%\yuchberry_%var%_for_5.0os\
copy .\client\deliverables\Web\5.0.0\*.cod .\release_client_%var%\yuchberry_%var%_for_5.0os\WebOTA\
copy .\client\deliverables\Web\5.0.0\*.jad .\release_client_%var%\yuchberry_%var%_for_5.0os\WebOTA\

copy .\client60\deliverables\Standard\6.0.0\*.cod .\release_client_%var%\yuchberry_%var%_for_6.0os\6.0.0\
copy .\client60\deliverables\Standard\*.alx .\release_client_%var%\yuchberry_%var%_for_6.0os\
copy .\client60\deliverables\Web\6.0.0\*.cod .\release_client_%var%\yuchberry_%var%_for_6.0os\WebOTA\
copy .\client60\deliverables\Web\6.0.0\*.jad .\release_client_%var%\yuchberry_%var%_for_6.0os\WebOTA\

7z a .\release_client_%var%\yuchberry_%var%_for_4.2os.zip .\release_client_%var%\yuchberry_%var%_for_4.2os\
7z a .\release_client_%var%\yuchberry_%var%_for_4.5os.zip .\release_client_%var%\yuchberry_%var%_for_4.5os\
7z a .\release_client_%var%\yuchberry_%var%_for_4.6os.zip .\release_client_%var%\yuchberry_%var%_for_4.6os\
7z a .\release_client_%var%\yuchberry_%var%_for_4.7os.zip .\release_client_%var%\yuchberry_%var%_for_4.7os\
7z a .\release_client_%var%\yuchberry_%var%_for_5.0os.zip .\release_client_%var%\yuchberry_%var%_for_5.0os\
7z a .\release_client_%var%\yuchberry_%var%_for_6.0os.zip .\release_client_%var%\yuchberry_%var%_for_6.0os\

pause
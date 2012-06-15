@echo off
@echo 发布客户端版本批处理程序
@echo. 
set /p var=请输入要发布的版本号:
rd /S /Q release_client_%var%
set bbm=BBM_skin
set girl=girl_skin

md release_client_%var%

md release_client_%var%\yuchsbox_%var%_%bbm%_for_5.0os\5.0.0
md release_client_%var%\yuchsbox_%var%_%bbm%_for_5.0os\WebOTA

md release_client_%var%\yuchsbox_%var%_%girl%_for_5.0os\5.0.0
md release_client_%var%\yuchsbox_%var%_%girl%_for_5.0os\WebOTA


copy .\client\deliverables\Standard\5.0.0\*.cod .\release_client_%var%\yuchsbox_%var%_%bbm%_for_5.0os\5.0.0\
copy .\client\deliverables\Standard\*.alx .\release_client_%var%\yuchsbox_%var%_%bbm%_for_5.0os\
copy .\client\deliverables\Web\5.0.0\*.cod .\release_client_%var%\yuchsbox_%var%_%bbm%_for_5.0os\WebOTA\
copy .\client\deliverables\Web\5.0.0\*.jad .\release_client_%var%\yuchsbox_%var%_%bbm%_for_5.0os\WebOTA\

copy .\client60\deliverables\Standard\5.0.0\*.cod .\release_client_%var%\yuchsbox_%var%_%girl%_for_5.0os\5.0.0\
copy .\client60\deliverables\Standard\*.alx .\release_client_%var%\yuchsbox_%var%_%girl%_for_5.0os\
copy .\client60\deliverables\Web\5.0.0\*.cod .\release_client_%var%\yuchsbox_%var%_%girl%_for_5.0os\WebOTA\
copy .\client60\deliverables\Web\5.0.0\*.jad .\release_client_%var%\yuchsbox_%var%_%girl%_for_5.0os\WebOTA\


7z a .\release_client_%var%\yuchsbox_%var%_%bbm%_for_5.0os.zip .\release_client_%var%\yuchsbox_%var%_%bbm%_for_5.0os\
7z a .\release_client_%var%\yuchsbox_%var%_%girl%_for_5.0os.zip .\release_client_%var%\yuchsbox_%var%_%girl%_for_5.0os\

pause
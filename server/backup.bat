set dir="116.255.186.64_%date%"

rd /s /q %dir%
del /Q /S %dir%.7z

md "%date%"

java -jar svr.jar backup %dir%
7z.exe a %dir%.7z %dir%
ncftpput -u phpwind@yuchberrybbs.com -p %%iUa2^^7jAkZ yuchberrybbs.com backupYB %dir%.7z

del /Q /S %dir%.7z
rd /s /q %dir%

exit
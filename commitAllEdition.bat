@echo off

copy /Y .\client45\src\com\yuchting\yuchberry\client\*.* .\client\src\com\yuchting\yuchberry\client\
copy /Y .\client45\src\com\yuchting\yuchberry\client\*.* .\client46\src\com\yuchting\yuchberry\client\
copy /Y .\client45\src\com\yuchting\yuchberry\client\*.* .\client47\src\com\yuchting\yuchberry\client\
copy /Y .\client45\src\com\yuchting\yuchberry\client\*.* .\client60\src\com\yuchting\yuchberry\client\

copy /Y .\client45\src\local\*.* .\client\src\local\
copy /Y .\client45\src\local\*.* .\client46\src\local\
copy /Y .\client45\src\local\*.* .\client47\src\local\
copy /Y .\client45\src\local\*.* .\client60\src\local\

copy /Y .\client45\res\*.* .\client\res\
copy /Y .\client45\res\*.* .\client46\res\
copy /Y .\client45\res\*.* .\client47\res\ 
copy /Y .\client45\res\*.* .\client60\res\ 

copy /Y .\client45\BlackBerry_App_Descriptor.xml .\client\
copy /Y .\client45\BlackBerry_App_Descriptor.xml .\client46\
copy /Y .\client45\BlackBerry_App_Descriptor.xml .\client47\
copy /Y .\client45\BlackBerry_App_Descriptor.xml .\client60\

svn add .\client\src\local\*.*
svn add .\client46\src\local\*.*
svn add .\client47\src\local\*.*
svn add .\client60\src\local\*.*

svn add .\client\res\*.* 
svn add .\client46\res\*.* 
svn add .\client47\res\*.* 
svn add .\client60\res\*.* 

svn add .\client\src\com\yuchting\yuchberry\client\*.*
svn add .\client60\src\com\yuchting\yuchberry\client\*.*
svn add .\client46\src\com\yuchting\yuchberry\client\*.*
svn add .\client47\src\com\yuchting\yuchberry\client\*.*

pause

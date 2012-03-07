@echo off

copy /Y .\client46\src\com\yuchting\yuchberry\client\*.* .\client\src\com\yuchting\yuchberry\client\
copy /Y .\client46\src\com\yuchting\yuchberry\client\*.* .\client45\src\com\yuchting\yuchberry\client\
copy /Y .\client46\src\com\yuchting\yuchberry\client\*.* .\client47\src\com\yuchting\yuchberry\client\
copy /Y .\client46\src\com\yuchting\yuchberry\client\*.* .\client60\src\com\yuchting\yuchberry\client\
copy /Y .\client46\src\com\yuchting\yuchberry\client\*.* .\client42\src\com\yuchting\yuchberry\client\


copy /Y .\client46\src\com\yuchting\yuchberry\client\weibo\*.* .\client\src\com\yuchting\yuchberry\client\weibo\
copy /Y .\client46\src\com\yuchting\yuchberry\client\weibo\*.* .\client45\src\com\yuchting\yuchberry\client\weibo\
copy /Y .\client46\src\com\yuchting\yuchberry\client\weibo\*.* .\client47\src\com\yuchting\yuchberry\client\weibo\
copy /Y .\client46\src\com\yuchting\yuchberry\client\weibo\*.* .\client60\src\com\yuchting\yuchberry\client\weibo\
copy /Y .\client46\src\com\yuchting\yuchberry\client\weibo\*.* .\client42\src\com\yuchting\yuchberry\client\weibo\

copy /Y .\client46\src\com\yuchting\yuchberry\client\ui\*.* .\client\src\com\yuchting\yuchberry\client\ui\
copy /Y .\client46\src\com\yuchting\yuchberry\client\ui\*.* .\client45\src\com\yuchting\yuchberry\client\ui\
copy /Y .\client46\src\com\yuchting\yuchberry\client\ui\*.* .\client47\src\com\yuchting\yuchberry\client\ui\
copy /Y .\client46\src\com\yuchting\yuchberry\client\ui\*.* .\client60\src\com\yuchting\yuchberry\client\ui\
copy /Y .\client46\src\com\yuchting\yuchberry\client\ui\*.* .\client42\src\com\yuchting\yuchberry\client\ui\

copy /Y .\client46\src\com\yuchting\yuchberry\client\screen\*.* .\client\src\com\yuchting\yuchberry\client\screen\
copy /Y .\client46\src\com\yuchting\yuchberry\client\screen\*.* .\client45\src\com\yuchting\yuchberry\client\screen\
copy /Y .\client46\src\com\yuchting\yuchberry\client\screen\*.* .\client47\src\com\yuchting\yuchberry\client\screen\
copy /Y .\client46\src\com\yuchting\yuchberry\client\screen\*.* .\client60\src\com\yuchting\yuchberry\client\screen\
copy /Y .\client46\src\com\yuchting\yuchberry\client\screen\*.* .\client42\src\com\yuchting\yuchberry\client\screen\

copy /Y .\client46\src\com\yuchting\yuchberry\client\im\*.* .\client\src\com\yuchting\yuchberry\client\im\
copy /Y .\client46\src\com\yuchting\yuchberry\client\im\*.* .\client45\src\com\yuchting\yuchberry\client\im\
copy /Y .\client46\src\com\yuchting\yuchberry\client\im\*.* .\client47\src\com\yuchting\yuchberry\client\im\
copy /Y .\client46\src\com\yuchting\yuchberry\client\im\*.* .\client60\src\com\yuchting\yuchberry\client\im\
copy /Y .\client46\src\com\yuchting\yuchberry\client\im\*.* .\client42\src\com\yuchting\yuchberry\client\im\


copy /Y .\client46\src\local\*.* .\client\src\local\
copy /Y .\client46\src\local\*.* .\client45\src\local\
copy /Y .\client46\src\local\*.* .\client47\src\local\
copy /Y .\client46\src\local\*.* .\client60\src\local\
copy /Y .\client46\src\local\*.* .\client42\src\local\

copy /Y .\client46\res\*.* .\client\res\
copy /Y .\client46\res\*.* .\client45\res\
copy /Y .\client46\res\*.* .\client47\res\ 
copy /Y .\client46\res\*.* .\client60\res\ 
copy /Y .\client46\res\*.* .\client42\res\

copy /Y .\client46\BlackBerry_App_Descriptor.xml .\client\
copy /Y .\client46\BlackBerry_App_Descriptor.xml .\client45\
copy /Y .\client46\BlackBerry_App_Descriptor.xml .\client47\
copy /Y .\client46\BlackBerry_App_Descriptor.xml .\client60\
copy /Y .\client46\BlackBerry_App_Descriptor.xml .\client42\

del /S /Q /AH .\client45\res\Thumbs.db
del /S /Q /AH .\client\res\Thumbs.db
del /S /Q /AH .\client46\res\Thumbs.db
del /S /Q /AH .\client47\res\Thumbs.db
del /S /Q /AH .\client60\res\Thumbs.db 
del /S /Q /AH .\client42\res\Thumbs.db

del /S /Q /AH .\client45\res\weibo\Thumbs.db
del /S /Q /AH .\client\res\weibo\Thumbs.db
del /S /Q /AH .\client46\res\weibo\Thumbs.db
del /S /Q /AH .\client47\res\weibo\Thumbs.db
del /S /Q /AH .\client60\res\weibo\Thumbs.db 
del /S /Q /AH .\client42\res\weibo\Thumbs.db

svn add .\client\src\local\*.*
svn add .\client45\src\local\*.*
svn add .\client47\src\local\*.*
svn add .\client60\src\local\*.*
svn add .\client42\src\local\*.*

svn add .\client\res\*.* 
svn add .\client45\res\*.* 
svn add .\client47\res\*.* 
svn add .\client60\res\*.* 
svn add .\client42\res\*.*

svn add .\client\src\com\yuchting\yuchberry\client\*.*
svn add .\client60\src\com\yuchting\yuchberry\client\*.*
svn add .\client45\src\com\yuchting\yuchberry\client\*.*
svn add .\client47\src\com\yuchting\yuchberry\client\*.*
svn add .\client42\src\com\yuchting\yuchberry\client\*.*

svn add .\client\src\com\yuchting\yuchberry\client\weibo\*.*
svn add .\client60\src\com\yuchting\yuchberry\client\weibo\*.*
svn add .\client45\src\com\yuchting\yuchberry\client\weibo\*.*
svn add .\client47\src\com\yuchting\yuchberry\client\weibo\*.*
svn add .\client42\src\com\yuchting\yuchberry\client\weibo\*.*

svn add .\client\src\com\yuchting\yuchberry\client\ui\*.*
svn add .\client60\src\com\yuchting\yuchberry\client\ui\*.*
svn add .\client45\src\com\yuchting\yuchberry\client\ui\*.*
svn add .\client47\src\com\yuchting\yuchberry\client\ui\*.*
svn add .\client42\src\com\yuchting\yuchberry\client\ui\*.*

svn add .\client\src\com\yuchting\yuchberry\client\screen\*.*
svn add .\client60\src\com\yuchting\yuchberry\client\screen\*.*
svn add .\client45\src\com\yuchting\yuchberry\client\screen\*.*
svn add .\client47\src\com\yuchting\yuchberry\client\screen\*.*
svn add .\client42\src\com\yuchting\yuchberry\client\screen\*.*

svn add .\client\src\com\yuchting\yuchberry\client\im\*.*
svn add .\client60\src\com\yuchting\yuchberry\client\im\*.*
svn add .\client45\src\com\yuchting\yuchberry\client\im\*.*
svn add .\client47\src\com\yuchting\yuchberry\client\im\*.*
svn add .\client42\src\com\yuchting\yuchberry\client\im\*.*

pause

@echo off

copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\*.* .\yuchbox50\src\com\yuchting\yuchberry\client\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\*.* .\yuchbox70\src\com\yuchting\yuchberry\client\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\*.* .\yuchbox60\src\com\yuchting\yuchberry\client\



copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\weibo\*.* .\yuchbox50\src\com\yuchting\yuchberry\client\weibo\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\weibo\*.* .\yuchbox70\src\com\yuchting\yuchberry\client\weibo\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\weibo\*.* .\yuchbox60\src\com\yuchting\yuchberry\client\weibo\


copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\ui\*.* .\yuchbox50\src\com\yuchting\yuchberry\client\ui\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\ui\*.* .\yuchbox70\src\com\yuchting\yuchberry\client\ui\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\ui\*.* .\yuchbox60\src\com\yuchting\yuchberry\client\ui\


copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\screen\*.* .\yuchbox50\src\com\yuchting\yuchberry\client\screen\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\screen\*.* .\yuchbox70\src\com\yuchting\yuchberry\client\screen\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\screen\*.* .\yuchbox60\src\com\yuchting\yuchberry\client\screen\


copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\im\*.* .\yuchbox50\src\com\yuchting\yuchberry\client\im\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\im\*.* .\yuchbox70\src\com\yuchting\yuchberry\client\im\
copy /Y .\yuchbox71\src\com\yuchting\yuchberry\client\im\*.* .\yuchbox60\src\com\yuchting\yuchberry\client\im\



copy /Y .\yuchbox71\src\local\*.* .\yuchbox50\src\local\
copy /Y .\yuchbox71\src\local\*.* .\yuchbox70\src\local\
copy /Y .\yuchbox71\src\local\*.* .\yuchbox60\src\local\


copy /Y .\yuchbox71\res\*.* .\yuchbox50\res\ 
copy /Y .\yuchbox71\res\*.* .\yuchbox70\res\
copy /Y .\yuchbox71\res\*.* .\yuchbox60\res\


copy /Y .\yuchbox71\BlackBerry_App_Descriptor.xml .\yuchbox50\
copy /Y .\yuchbox71\BlackBerry_App_Descriptor.xml .\yuchbox70\
copy /Y .\yuchbox71\BlackBerry_App_Descriptor.xml .\yuchbox60\

del /S /Q /AH .\yuchbox50\res\Thumbs.db
del /S /Q /AH .\yuchbox60\res\Thumbs.db 
del /S /Q /AH .\yuchbox70\res\Thumbs.db 
del /S /Q /AH .\yuchbox71\res\Thumbs.db 

del /S /Q /AH .\yuchbox50\res\weibo\Thumbs.db
del /S /Q /AH .\yuchbox60\res\weibo\Thumbs.db
del /S /Q /AH .\yuchbox70\res\weibo\Thumbs.db
del /S /Q /AH .\yuchbox71\res\weibo\Thumbs.db 


svn add --force .\yuchbox50\src\local\*.*
svn add --force .\yuchbox60\src\local\*.*
svn add --force .\yuchbox70\src\local\*.*
svn add --force .\yuchbox71\src\local\*.*

svn add --force .\yuchbox50\res\*.* 
svn add --force .\yuchbox60\res\*.*
svn add --force .\yuchbox70\res\*.*
svn add --force .\yuchbox71\res\*.*

svn revert .\yuchbox50\res\FlurryKey.txt
svn revert .\yuchbox60\res\FlurryKey.txt
svn revert .\yuchbox70\res\FlurryKey.txt
svn revert .\yuchbox71\res\FlurryKey.txt

svn add --force .\yuchbox50\src\com\yuchting\yuchberry\client\*.*
svn add --force .\yuchbox60\src\com\yuchting\yuchberry\client\*.*
svn add --force .\yuchbox70\src\com\yuchting\yuchberry\client\*.*
svn add --force .\yuchbox71\src\com\yuchting\yuchberry\client\*.*

svn add --force .\yuchbox50\src\com\yuchting\yuchberry\client\weibo\*.*
svn add --force .\yuchbox60\src\com\yuchting\yuchberry\client\weibo\*.*
svn add --force .\yuchbox70\src\com\yuchting\yuchberry\client\weibo\*.*
svn add --force .\yuchbox71\src\com\yuchting\yuchberry\client\weibo\*.*

svn add --force .\yuchbox50\src\com\yuchting\yuchberry\client\ui\*.*
svn add --force .\yuchbox60\src\com\yuchting\yuchberry\client\ui\*.*
svn add --force .\yuchbox70\src\com\yuchting\yuchberry\client\ui\*.*
svn add --force .\yuchbox71\src\com\yuchting\yuchberry\client\ui\*.*

svn add --force .\yuchbox50\src\com\yuchting\yuchberry\client\screen\*.*
svn add --force .\yuchbox60\src\com\yuchting\yuchberry\client\screen\*.*
svn add --force .\yuchbox70\src\com\yuchting\yuchberry\client\screen\*.*
svn add --force .\yuchbox71\src\com\yuchting\yuchberry\client\screen\*.*

svn add --force .\yuchbox50\src\com\yuchting\yuchberry\client\im\*.*
svn add --force .\yuchbox60\src\com\yuchting\yuchberry\client\im\*.*
svn add --force .\yuchbox70\src\com\yuchting\yuchberry\client\im\*.*
svn add --force .\yuchbox71\src\com\yuchting\yuchberry\client\im\*.*

pause

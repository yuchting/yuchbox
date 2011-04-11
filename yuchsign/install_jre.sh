#!/bin/sh
cd ~
wget http://cds.sun.com/is-bin/INTERSHOP.enfinity/WFS/CDS-CDS_Developer-Site/en_US/-/USD/VerifyItem-Start/jre-6u24-linux-i586.bin?BundledLineItemUUID=6VGJ_hCwrFcAAAEv0r0AHi5.&OrderID=NLiJ_hCwd4cAAAEvtL0AHi5.&ProductID=oCWJ_hCwF3gAAAEteY8ADqmW&FileName=/jre-6u24-linux-i586.bin jre-6u24-linux-i586.bin
chmod 777 jre-6u24-linux-i586.bin
./jre-6u24-linux-i586.bin
echo "export PATH=/root/jre1.6.0_24/bin:\$PATH" >> /etc/profile 
echo "export PATH=/root/jre1.6.0_24/bin:\$PATH" >> /etc/bashrc 
echo "" >> /etc/bashrc
rm -f jre-6u24-linux-i586.bin
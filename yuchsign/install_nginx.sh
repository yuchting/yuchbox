#!/bin/sh
cd ~
yum install -y gcc 
yum install -y g++ 
yum install -y gcc-c++
yum install -y unzip
yum install -y make
yum install -y openssl-devel
wget ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/pcre-8.12.zip
unzip pcre-8.12.zip
cd pcre-8.12
./configure 
make && make install
cd ..
wget http://nginx.org/download/nginx-0.8.54.tar.gz
tar -zxvf nginx-0.8.54.tar.gz
cd nginx-0.8.54
./configure --user=www --group=www --prefix=/usr/local/nginx/ --with-http_stub_status_module 
make && make install
echo "export PATH=/usr/local/nginx/sbin:\$PATH" >> /etc/profile 
echo "export PATH=/usr/local/nginx/sbin:\$PATH" >> /etc/bashrc 
echo "" >> /etc/bashrc
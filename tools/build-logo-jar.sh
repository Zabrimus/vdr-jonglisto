#!/bin/sh

# Clone or pull repo
if [ -d picicon-repo ]; then
    (cd picicon-repo; git pull)
else
    git clone --depth 1 https://github.com/picons/picons-source.git picicon-repo
fi;

# create logos
cp icon-settings/*.conf picicon-repo/build-input
cd picicon-repo
./2-build-picons.sh snp-full

# repack the result
mkdir -p logo
tar -xf build-output/binaries-snp-full/snp-full.*hardlink.tar.xz -C logo --strip 1
jar -cf logo.jar logo/*
mkdir -p ../../external-jar
mv logo.jar ../../external-jar


#!/bin/bash

echo "----- Script SSH -----"
apt update && apt install -y openssh-server sudo
adduser --disabled-password --gecos "" user
echo "user:root1234" | chpasswd
usermod -aG sudo user
systemctl enable --now ssh
#!/bin/bash

echo "----- Installation de Git -----"
apt update && apt install -y git
git --version

#!/bin/bash

if [ -z "$CTID" ]; then
    echo "Erreur: Vous devez entrer un CTID"
    exit 1
fi

if [ -z "$MEMORY" ]; then
    echo "Erreur: Vous devez entrer une valeur MEMORY"
    exit 1
fi

echo "CTID sélectionné: $CTID"
echo "RAM allouée: $MEMORY Mo"

HOSTNAME="LXC-$CTID"

echo "Utilisateur actuel: $(whoami)"
echo "UID: $UID"

echo $HOSTNAME

sudo pct create $CTID local:vztmpl/debian-12-standard_12.12-1_amd64.tar.zst \
  --hostname $HOSTNAME \
  --password root1234 \
  --storage local-lvm \
  --rootfs local-lvm:8 \
  --memory $MEMORY \
  --cores 1 \
  --net0 name=eth0,bridge=vmbr0,ip=dhcp \
  --onboot 1 \
  --unprivileged 1

sudo pct start $CTID

echo "TEST OK"
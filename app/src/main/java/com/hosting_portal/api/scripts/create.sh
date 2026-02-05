#!/bin/bash

read -p "Entrez l'ID du conteneur (CTID): " CTID

if [ -z "$CTID" ]; then
    echo "Erreur: Vous devez entrer un CTID"
    exit 1
fi

read -p "Entrez la quantité de RAM en Mo (ex: 1024, 2048, 3072, 4096, 5120, 6144, 7168, 8192): " MEMORY

if [ -z "$MEMORY" ]; then
    echo "Erreur: Vous devez entrer une quantité de RAM"
    exit 1
fi

if ! [[ "$MEMORY" =~ ^[0-9]+$ ]]; then
    echo "Erreur: La RAM doit être un nombre entier"
    exit 1
fi

echo "CTID sélectionné: $CTID"
echo "RAM allouée: $MEMORY Mo"

HOSTNAME="LXC-$CTID"

echo $HOSTNAME

pct create $CTID local:vztmpl/debian-12-standard_12.12-1_amd64.tar.zst \
  --hostname $HOSTNAME \
  --password root1234 \
  --storage local-lvm \
  --rootfs local-lvm:8 \
  --memory $MEMORY \
  --cores 1 \
  --net0 name=eth0,bridge=vmbr0,ip=dhcp \
  --onboot 1 \
  --unprivileged 1

pct start $CTID

echo "Attente de la disponibilité réseau..."
TIMEOUT=60
ELAPSED=0

while [ $ELAPSED -lt $TIMEOUT ]; do
    IP=$(pct exec $CTID -- hostname -I 2>/dev/null | awk '{print $1}')
    
    if [ -n "$IP" ]; then
        echo "Conteneur prêt avec l'IP: $IP"
        break
    fi
    
    sleep 1
    ((ELAPSED++))
done

pct exec $CTID -- apt-get update -y
pct exec $CTID -- apt-get upgrade -y

pct push $CTID ./ssh.sh /tmp/script.sh
pct exec $CTID -- bash /tmp/script.sh
pct exec $CTID -- rm /tmp/script.sh

pct push $CTID ./gitlab.sh /tmp/script.sh
pct exec $CTID -- bash /tmp/script.sh
pct exec $CTID -- rm /tmp/script.sh

#pct push $CTID ./docker.sh /tmp/script.sh
#pct exec $CTID -- bash /tmp/script.sh
#pct exec $CTID -- rm /tmp/script.sh
#pct exec $CTID -- usermod -aG docker user
#pct set $CTID -features nesting=1

#pct push $CTID ./cloudflare.sh /tmp/script.sh
#pct exec $CTID -- bash /tmp/script.sh
#pct exec $CTID -- rm /tmp/script.sh

pct exec $CTID -- ip a

exit
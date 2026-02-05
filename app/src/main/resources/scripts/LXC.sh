#!/bin/bash

read -p "Entrez l'ID du conteneur (CTID): " CTID

if [ -z "$CTID" ]; then
    echo "Erreur: Vous devez entrer un CTID"
    exit 1
fi

echo "CTID sélectionné: $CTID"

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

pct push $CTID ./basic.sh /tmp/basic.sh
pct exec $CTID -- bash /tmp/basic.sh
pct exec $CTID -- rm /tmp/basic.sh

exit
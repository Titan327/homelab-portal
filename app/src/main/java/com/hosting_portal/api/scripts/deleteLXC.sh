#!/bin/bash

read -p "Entrez l'ID du conteneur (CTID): " CTID

if [ -z "$CTID" ]; then
    echo "Erreur: Vous devez entrer un CTID"
    exit 1
fi

echo "CTID a supprimer: $CTID"


#!/bin/bash

# Script avec option --nom

NOM=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --nom)
            NOM="$2"
            shift 2
            ;;
        *)
            shift
            ;;
    esac
    
done

echo "Le nom est: $NOM"
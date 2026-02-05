#!/bin/bash

echo "----- Script Cloudflare -----"

export CLOUDFLARE_API_TOKEN=""
export ACCOUNT_ID=""
export HOSTNAME=$(cat /etc/hostname)

apt-get install curl -y
apt-get install jq -y

# Add cloudflare gpg key
mkdir -p --mode=0755 /usr/share/keyrings
curl -fsSL https://pkg.cloudflare.com/cloudflare-main.gpg | tee /usr/share/keyrings/cloudflare-main.gpg >/dev/null

# Add this repo to your apt repositories
# Stable
echo 'deb [signed-by=/usr/share/keyrings/cloudflare-main.gpg] https://pkg.cloudflare.com/cloudflared any main' | tee /etc/apt/sources.list.d/cloudflared.list
# Nightly
echo 'deb [signed-by=/usr/share/keyrings/cloudflare-main.gpg] https://next.pkg.cloudflare.com/cloudflared any main' | tee /etc/apt/sources.list.d/cloudflared.list

# install cloudflared
apt-get update -y

apt-get install cloudflared -y

curl -4 https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/tunnels \
    -H "Authorization: Bearer $CLOUDFLARE_API_TOKEN"

RESPONSE=$(curl -4 "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/cfd_tunnel" \
  --request POST \
  --header "Authorization: Bearer $CLOUDFLARE_API_TOKEN" \
  --json '{
    "name": "'$HOSTNAME'",
    "config_src": "cloudflare"
  }')

TUNNEL_TOKEN=$(echo $RESPONSE | jq -r '.result.token')

TUNNEL_ID=$(echo $RESPONSE | jq -r '.result.id')

echo "Token: $TUNNEL_TOKEN"
echo "Tunnel ID: $TUNNEL_ID"

cloudflared service install $TUNNEL_TOKEN


#!/bin/zsh

echo "HOST_IP=$(route get default | grep 'interface' | awk '{print $2}' | xargs ipconfig getifaddr)" >> .env

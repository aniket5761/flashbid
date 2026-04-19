#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_URL="${1:-}"
USERS_FILE="${2:-${SCRIPT_DIR}/../data/users.json}"

if [[ -z "$BASE_URL" ]]; then
  echo "Usage: $0 <base-url> [users-file]"
  exit 1
fi

if [[ ! -f "$USERS_FILE" ]]; then
  echo "Users file not found: $USERS_FILE"
  exit 1
fi

for i in $(seq -w 1 500); do
  username="bidder${i}"
  email="${username}@example.com"
  password="Pass1234!"

  payload=$(printf '{"firstName":"Bidder","lastName":"%s","username":"%s","email":"%s","password":"%s"}' "$i" "$username" "$email" "$password")

  response=$(curl -sS -o /tmp/flashbid-register-response.json -w "%{http_code}" \
    -X POST "${BASE_URL%/}/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "$payload")

  if [[ "$response" == "200" ]]; then
    echo "registered ${username}"
  elif [[ "$response" == "400" || "$response" == "409" ]]; then
    echo "skipped ${username} (already exists or invalid)"
  else
    echo "failed ${username} with status ${response}"
    cat /tmp/flashbid-register-response.json
    exit 1
  fi
done

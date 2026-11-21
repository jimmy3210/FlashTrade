#!/bin/sh
set -eu

secret_dir="$(dirname "$0")/secrets"
mkdir -p "$secret_dir"
chmod 700 "$secret_dir"

create_secret() {
    name="$1"
    file="$secret_dir/$name.txt"
    if [ ! -f "$file" ]; then
        openssl rand -base64 48 > "$file"
        chmod 600 "$file"
        printf 'Created %s\n' "$file"
    else
        printf 'Kept existing %s\n' "$file"
    fi
}

create_secret db_password
create_secret redis_password
create_secret jwt_secret

#!/bin/sh
set -eu

read_secret_file() {
    value_file="$1"
    value_name="$2"

    if [ -n "${value_file:-}" ] && [ -r "$value_file" ]; then
        value="$(cat "$value_file")"
        export "$value_name=$value"
        unset value
    fi
}

read_secret_file "${DB_PASSWORD_FILE:-}" DB_PASSWORD
read_secret_file "${REDIS_PASSWORD_FILE:-}" REDIS_PASSWORD
read_secret_file "${JWT_SECRET_FILE:-}" JWT_SECRET

exec "$@"

#!/bin/bash

set -e
set -u

function create_user() {
	echo "  Creating user"
	psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
	    CREATE USER $OPENK9_USER WITH ENCRYPTED PASSWORD '$OPENK9_PASSWORD';
EOSQL
}

function create_user_and_database() {
	local database=$1
	echo "  Creating database '$database'"
	psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
	    CREATE DATABASE $database WITH OWNER $OPENK9_PASSWORD;
	    GRANT ALL PRIVILEGES ON DATABASE $database TO $OPENK9_USER;
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
	echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
	create_user
	for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
		create_user_and_database $db
	done
	echo "Multiple databases created"
fi
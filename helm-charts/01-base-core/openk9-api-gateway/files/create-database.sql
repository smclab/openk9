-- Creates the gateway role and database when they are missing.
-- psql variables: db_user, db_name. Safe to run again.
SELECT format('CREATE ROLE %I WITH LOGIN ENCRYPTED PASSWORD %L', :'db_user', :'db_user')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = :'db_user')\gexec

SELECT format('CREATE DATABASE %I OWNER %I', :'db_name', :'db_user')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'db_name')\gexec

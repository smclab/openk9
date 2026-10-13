# Local Caddy routes

The Caddyfile imports every `*.caddy` file in this folder into the tenant
site block (`localhost, *.localhost, *.*.localhost`), so a demo or a test
setup can expose its own service under `https://<tenant>.openk9.localhost/...`
without changing the versioned Caddyfile. Git ignores the files here.

Example, `pokedex.caddy`:

```
handle_path /pokedex/* {
    reverse_proxy pokedex:8080
}
```

The service must be on the `openk9` network: add it to the stack through
`K9_EXTRA_COMPOSE` (see `./k9.sh` usage).

Apply a new or changed file without restarting the stack:

```bash
docker exec caddy caddy reload --config /etc/caddy/Caddyfile
```

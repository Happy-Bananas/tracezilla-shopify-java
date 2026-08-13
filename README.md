# tracezilla-shopify-java

Framework-neutral Java templates for integrating Shopify with the tracezilla
API. The first example implements the read-only cross-platform **Compare
Catalogs** workflow.

## Run with Docker

```bash
cp .env.example .env
# Fill in test-account credentials
docker compose build
docker compose run --rm app
```

Optional output controls:

```bash
docker compose run --rm app --limit=25
docker compose run --rm app --json
```

Preview or explicitly create missing tracezilla SKUs:

```bash
docker compose run --rm app create-tracezilla-skus --limit=10
docker compose run --rm app create-tracezilla-skus --execute --confirm --limit=1
```

The complete catalogs are compared by SKU code. The display limit defaults to
10 and does not affect comparison totals. Differences return exit code `0`;
configuration and API failures return a non-zero code. No data is written.

List all Shopify locations (read-only):

```bash
docker compose run --rm app list-shopify-locations
docker compose run --rm app list-shopify-locations --json
```

Synchronize inventory with an explicit source and target (dry run by default):

```bash
docker compose run --rm app synchronize-inventory \
  --shopify-location=gid://shopify/Location/123 --tracezilla-warehouse=2 --limit=10
```

Writes additionally require `--execute --confirm`.

## Tests

Java and Maven do not need to be installed on the host:

```bash
docker compose run --rm --entrypoint mvn app test
```

The design separates GraphQL queries, HTTP clients, pagination services,
response mappers, workflow logic, and output rendering. This is ordinary Java
without Spring or another application framework.

See the [Tracezilla Integrations documentation](https://happy-bananas.github.io/tracezilla-integrations-docs/)
for canonical setup and safety guidance. Never commit `.env` or print secrets;
this read-only workflow needs only Shopify `read_products` access.

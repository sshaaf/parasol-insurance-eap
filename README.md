# Parasol Insurance (Quarkus)

Java / Quarkus application for the **EAP 7 → Quarkus** migration demo. Serves a static UI plus REST APIs for claims and inbox, with in-process reactive messaging (no external broker required).

## Prerequisites

- Java 17+
- Maven 3.8+
- For OpenShift deploy: `oc` logged in to a project with create permissions

## Local development

```bash
mvn quarkus:dev
```

Uses the default profile (in-memory H2). UI: http://localhost:8080/ — API: http://localhost:8080/api/claims

```bash
mvn clean package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

Packaged runs use the **`%prod`** profile (see `application.properties`).

## OpenShift deployment (from local code)

Assumes `oc` is already logged in and a project is selected (`oc project`).

### 1. Provision PostgreSQL

```bash
oc new-app --template=postgresql-ephemeral \
  -p DATABASE_SERVICE_NAME=parasol-db \
  -p POSTGRESQL_USER=parasol \
  -p POSTGRESQL_PASSWORD=parasol \
  -p POSTGRESQL_DATABASE=parasol
```

Wait until the database is ready:

```bash
oc rollout status deploymentconfig/parasol-db 2>/dev/null \
  || oc rollout status deployment/parasol-db
oc get pods -l name=parasol-db
```

This creates Service/Secret `parasol-db` (`database-user`, `database-password`, `database-name`).

For a PVC-backed database instead:

```bash
oc new-app --template=postgresql-persistent \
  -p DATABASE_SERVICE_NAME=parasol-db \
  -p POSTGRESQL_USER=parasol \
  -p POSTGRESQL_PASSWORD=parasol \
  -p POSTGRESQL_DATABASE=parasol \
  -p VOLUME_CAPACITY=1Gi
```

### 2. Build the Quarkus app locally

```bash
mvn clean package -DskipTests
```

Produces `target/quarkus-app/`.

### 3. Build the container image on the cluster (binary Docker build)

```bash
# Assemble a small build context (Dockerfile + quarkus-app)
rm -rf /tmp/parasol-build
mkdir -p /tmp/parasol-build
cp src/main/docker/Dockerfile.jvm /tmp/parasol-build/Dockerfile
cp -R target/quarkus-app /tmp/parasol-build/

# Create ImageStream + BuildConfig once, then upload sources
oc new-build --name=parasol-insurance --binary --strategy=docker -l app=parasol-insurance
oc start-build parasol-insurance --from-dir=/tmp/parasol-build --follow
```

### 4. Deploy the application

Apply the manifests (image namespace is filled from the current project). The Deployment maps Secret `parasol-db` into the `POSTGRESQL_*` env vars expected by the **`%prod`** profile:

```bash
NS=$(oc project -q)
sed "s|REPLACE_NAMESPACE|${NS}|g" openshift/deployment.yaml | oc apply -f -
oc apply -f openshift/service.yaml
oc apply -f openshift/route.yaml
oc rollout status deployment/parasol-insurance
```

Alternatively, create the app from the ImageStream and set env by hand:

```bash
oc new-app parasol-insurance --name=parasol-insurance
oc set env deployment/parasol-insurance \
  POSTGRESQL_HOST=parasol-db \
  POSTGRESQL_PORT=5432 \
  POSTGRESQL_USER="$(oc get secret parasol-db -o jsonpath='{.data.database-user}' | base64 -d)" \
  POSTGRESQL_PASSWORD="$(oc get secret parasol-db -o jsonpath='{.data.database-password}' | base64 -d)" \
  POSTGRESQL_DATABASE="$(oc get secret parasol-db -o jsonpath='{.data.database-name}' | base64 -d)"
oc expose service/parasol-insurance
```
### 5. Verify

```bash
ROUTE=$(oc get route parasol-insurance -o jsonpath='{.spec.host}')
echo "https://${ROUTE}/"
curl -sk "https://${ROUTE}/api/claims"
curl -sk "https://${ROUTE}/api/inbox"
```

Open the Route URL in a browser — the dashboard should load. After ~50s the scheduled email generator should populate the inbox.

### `%prod` configuration (OpenShift)

| Property / env | Purpose |
|----------------|---------|
| `POSTGRESQL_HOST` | DB service DNS (`parasol-db`) |
| `POSTGRESQL_PORT` | `5432` |
| `POSTGRESQL_USER` / `PASSWORD` / `DATABASE` | From Secret `parasol-db` |
| Hibernate `drop-and-create` + `import.sql` | Demo schema + seed data on startup |

Dev (`quarkus:dev`) keeps H2; packaged / OpenShift runs use PostgreSQL via `%prod`.

### Tear down

```bash
oc delete route,service,deployment,imagestream,buildconfig parasol-insurance --ignore-not-found
oc delete all -l template=postgresql-ephemeral --ignore-not-found
oc delete secret,service,deploymentconfig,deployment parasol-db --ignore-not-found
```

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/claims` | List all claims |
| GET | `/api/claims/{claimNumber}` | Get claim by number |
| GET | `/api/inbox` | List routed emails |
| GET | `/api/inbox?after={id}` | Poll for new emails |

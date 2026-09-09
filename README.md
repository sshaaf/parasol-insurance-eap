# Parasol Insurance (EAP 7)

Legacy Java EE 8 application for the **EAP 7 → Quarkus** migration demo. This is the "before" application; the modernized Quarkus version lives in the sibling repo [`parasol-insurance`](../parasol-insurance).

## Architecture

- **JAX-RS** REST API for claims and inbox
- **JPA 2.2** + PostgreSQL for claims persistence
- **JMS** (embedded ActiveMQ Artemis) for email intake
- **@Stateless EJB** for business logic (`EmailRoutingService`, `ClaimService`)
- **@MessageDriven** bean for async email consumption (`EmailIntakeMDB`)
- **@Singleton EJB** with timer for sample email generation
- Rule-based email routing (no AI libraries)

## Prerequisites

- Java 11
- Maven 3.8+
- Access to the [Red Hat GA Maven repository](https://maven.repository.redhat.com/ga/)
- JBoss EAP 7.4 (for local deployment) or OpenShift 4.x

### Maven settings for Red Hat GA repository

Add the Red Hat GA repository to your `~/.m2/settings.xml` if not using the repository declared in `pom.xml`:

```xml
<settings>
  <profiles>
    <profile>
      <id>redhat-ga</id>
      <repositories>
        <repository>
          <id>jboss-ga-repository</id>
          <url>https://maven.repository.redhat.com/ga/</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>false</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>redhat-ga</activeProfile>
  </activeProfiles>
</settings>
```

## Build

```bash
mvn clean package
```

Produces `target/parasol-insurance-eap.war`.

## Local deployment (EAP 7.4)

1. Create a PostgreSQL datasource named `ParasolDS` bound to JNDI `java:jboss/datasources/ParasolDS`
2. Deploy the WAR to EAP 7.4
3. The JMS queue `java:/jms/queue/email-intake` is created automatically via `jboss-all.xml`

### Verify

```bash
curl http://localhost:8080/api/claims
curl http://localhost:8080/api/inbox
```

After ~50 seconds, the inbox should receive routed emails from the sample generator.

## OpenShift deployment

### Build image (S2I)

```bash
oc new-project parasol
oc new-app jboss-eap74-openjdk11-openshift~. \
  --name=parasol-insurance-eap \
  --context-dir=. \
  -e DB_SERVICE_PREFIX_MAPPING=parasol-db=PostgreSQL \
  -e DATASOURCES=parasol \
  -e parasol_JNDI=java:jboss/datasources/ParasolDS \
  -e parasol_DRIVER=postgresql \
  -e parasol_CONNECTION_URL=jdbc:postgresql://parasol-db:5432/parasol \
  -e parasol_USERNAME=parasol \
  -e parasol_PASSWORD=parasol
```

Or apply the manifests:

```bash
oc apply -f openshift/
```

### Verify on OpenShift

1. Open the Route URL — dashboard should load
2. `GET /api/claims` returns 8 seeded claims
3. Inbox page receives routed emails after the timer fires (~50s)

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/claims` | List all claims |
| GET | `/api/claims/{claimNumber}` | Get claim by number |
| GET | `/api/inbox` | List routed emails |
| GET | `/api/inbox?after={id}` | Poll for new emails |

## Migration comparison

| Concern | EAP 7 (this app) | Quarkus (`parasol-insurance`) |
|---------|------------------|-------------------------------|
| Packaging | WAR | fast-jar |
| APIs | `javax.*` | `jakarta.*` |
| Business logic | `@Stateless` EJB | `@ApplicationScoped` CDI |
| Messaging | JMS MDB | Kafka `@Incoming` |
| Scheduling | EJB `@Timeout` timer | Quarkus `@Scheduled` |
| Data access | JPA `EntityManager` | Hibernate Panache |
| Messaging broker | Embedded Artemis | Strimzi Kafka |

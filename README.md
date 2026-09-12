# Paravai: Final Technical Baseline

Punto inicial técnico del TFM: un único microservicio, `communities-service`, con creación, consulta por ID y actualización de reglas de comunidades.

## Stack

Perfil tecnológico: Java 21, Spring Boot **4.1.1**, Spring WebFlux/Reactor, Spring Data Reactive MongoDB, Maven, JUnit Jupiter **6.0.3** y ArchUnit **1.5.0** mediante `archunit-junit6`. MongoDB local: **8.0.30**.

Spring Boot 4.1.1 es una versión estable publicada en Maven Central. Su BOM determina las versiones de Spring Framework 7, Reactor, Spring Data, el driver MongoDB y Jackson 3. La combinación Java/Spring Boot/WebFlux/MongoDB constituye el perfil tecnológico definitivo para los siguientes sprints y para el diseño del generador. Las versiones no se actualizan de forma implícita. Véanse la [nota oficial de la versión](https://spring.io/blog/2026/08/20/spring-boot-4-1-1-available-now) y las [decisiones de la baseline](docs/technical-baseline.md).

## Arranque local

Requisitos: JDK 21, Maven 3.6.3 o posterior y Docker con Compose v2.

```bash
docker compose up -d --wait
mvn clean verify
java -jar communities-service/target/communities-service-0.1.0-SNAPSHOT.jar
```

El servicio escucha en `http://127.0.0.1:8080`. MongoDB conserva los datos en el volumen `communities-data` y publica su puerto únicamente en loopback. Para detener MongoDB conservando los datos:

```bash
docker compose down
```

Variables opcionales: `MONGODB_URI`, `SERVER_PORT`, `SERVER_ADDRESS` y `DEMO_USERS` (lista separada por comas; por defecto `alice,bob`). La URI predeterminada usa la base `communities` y limita la selección/conexión del servidor a 2 segundos.

## Identidad de demostración

Las escrituras requieren exactamente una cabecera `X-Demo-User` con uno de los usuarios configurados. El servicio toma el administrador del creador; el cuerpo no puede suministrar ni modificar `administratorId`. GET es público.

La cabecera permite representar a cualquiera de los usuarios de demostración: **no autentica a una persona**. Esta configuración está destinada a una demostración local controlada. No exponer el servicio o MongoDB a una red pública con esta configuración.

## Contrato HTTP

| Operación | Método y ruta | Resultado |
| --- | --- | --- |
| Create Community | `POST /v1/communities` | 201, cuerpo y cabecera Location |
| Get Community by ID | `GET /v1/communities/{id}` | 200 |
| Update Community Rules | `PUT /v1/communities/{id}/rules` | 200 con la nueva revisión |

El modelo contiene exclusivamente `id` (UUID), `name`, `administratorId`, `allowedExchangeTypes` y `policyRevision`. El nombre debe tener contenido y un máximo de 100 caracteres; se eliminan los espacios de los extremos. Las reglas requieren al menos un tipo entre `DONATION`, `LOAN` y `EXCHANGE`. No se aceptan tipos desconocidos, nulos ni ordinales numéricos, ni campos JSON adicionales.

La revisión inicial es 1. Cada actualización aceptada reemplaza el conjunto completo de tipos e incrementa la revisión en uno, incluso si se envía el mismo conjunto. Debe suministrarse la revisión leída en `expectedPolicyRevision`, como entero positivo. La comprobación y actualización de MongoDB son atómicas para un único documento. Ante 409, volver a leer y decidir si procede reenviar; no se reintenta automáticamente.

Crear:

```bash
curl -i http://127.0.0.1:8080/v1/communities \
  -H 'Content-Type: application/json' \
  -H 'X-Demo-User: alice' \
  -d '{"name":"Vecindario","allowedExchangeTypes":["DONATION","LOAN"]}'
```

Copiar el `id` devuelto en los siguientes comandos:

```bash
COMMUNITY_ID='sustituir-por-el-uuid-devuelto'
curl -i "http://127.0.0.1:8080/v1/communities/$COMMUNITY_ID"

curl -i -X PUT "http://127.0.0.1:8080/v1/communities/$COMMUNITY_ID/rules" \
  -H 'Content-Type: application/json' \
  -H 'X-Demo-User: alice' \
  -d '{"allowedExchangeTypes":["LOAN"],"expectedPolicyRevision":1}'
```

Errores con `application/problem+json`: 400 para entrada inválida; 401 para identidad ausente, ambigua o desconocida; 403 si el actor no es administrador; 404 si la comunidad no existe; 409 si la revisión cambió; 503 si falla el acceso a persistencia. WebFlux conserva 405/415 para método o tipo de contenido incompatibles. Las peticiones con varios defectos pueden fallar durante la validación HTTP antes de resolver la identidad.

## Pruebas

`mvn clean verify` ejecuta pruebas de dominio, aplicación, contrato HTTP y arquitectura sin necesitar MongoDB. Las pruebas HTTP de esta ejecución usan un repositorio en memoria.

Para ejecutar también las pruebas contra un servidor HTTP real y MongoDB real:

```bash
docker compose up -d --wait
MONGODB_TEST_URI='mongodb://127.0.0.1:27017' mvn -Pintegration clean verify
```

El perfil de integración exige `MONGODB_TEST_URI`; si falta o MongoDB no está disponible, falla. Cada ejecución utiliza y elimina únicamente su propia base `gate1_it_<UUID>`. No utilizar una instancia compartida de producción. GitHub Actions arranca MongoDB mediante el mismo Compose y ejecuta este perfil.

## Estructura y decisiones

Un agregador Maven y un módulo ejecutable. El dominio usa únicamente Java; la aplicación depende del dominio y de Reactor mediante puertos explícitos; los adaptadores REST y MongoDB se conectan en la configuración Spring. Las reglas ArchUnit se ejecutan en el ciclo normal de Maven.

El perfil congelado, la estructura y las evidencias de verificación se documentan en [docs/technical-baseline.md](docs/technical-baseline.md). CI exige cero fallos, errores y pruebas omitidas, y conserva los informes junto al árbol de dependencias resuelto.

# Final Technical Baseline

Esta baseline establece el punto inicial técnico del desarrollo formal del TFM. Su alcance es un único microservicio, `communities-service`, y exactamente tres operaciones. El perfil Java/Spring Boot/WebFlux/MongoDB queda fijado para los siguientes sprints y para el diseño del generador.

## Perfil tecnológico

| Componente | Decisión congelada | Fuente de versión |
| --- | --- | --- |
| Java | Java 21; compilación con `release 21` | `java.version` del POM raíz |
| Spring Boot | **4.1.1**, versión estable | Parent `spring-boot-starter-parent:4.1.1` |
| Spring Framework / WebFlux | **7.0.9** | BOM de Spring Boot 4.1.1 |
| Reactor | BOM **2025.0.7**; API reactiva `Mono`/`Flux` y servidor Reactor Netty | BOM de Spring Boot 4.1.1 |
| Spring Data | BOM **2026.0.1**, soporte MongoDB reactivo | BOM de Spring Boot 4.1.1 |
| Driver MongoDB | **5.8.1**, Reactive Streams | BOM de Spring Boot 4.1.1 |
| MongoDB local | **8.0.30** | Imagen `mongo:8.0.30` en Compose |
| JSON | Jackson **3.1.5** | BOM de Spring Boot 4.1.1 |
| JUnit | Jupiter / Platform **6.0.3** | BOM de Spring Boot 4.1.1 |
| ArchUnit | **1.5.0**, módulo `archunit-junit6` | `archunit.version` del POM raíz |
| Build | Maven, mínimo compatible **3.6.3**; Surefire/Failsafe **3.5.6** | Requisitos y parent de Spring Boot 4.1.1 |
| Infraestructura local | Docker Compose v2, sólo MongoDB | `compose.yaml` |

Se utilizan versiones concretas de Spring Boot, ArchUnit y MongoDB. Las dependencias gestionadas conservan las versiones del BOM de Boot; no se sobrescriben individualmente para formar combinaciones distintas. Java 21 define el nivel de lenguaje y bytecode; el parche del JDK y la versión efectiva de Maven de cada ejecución se registran como parte de la evidencia.

Cualquier modificación posterior del perfil requiere una decisión explícita de planificación y repetir la validación de compatibilidad. Los servicios y las plantillas que se diseñen durante los siguientes sprints tendrán este perfil como referencia.

## Arquitectura y decisiones de compatibilidad

- Un agregador Maven y un único módulo ejecutable. La separación hexagonal se expresa mediante paquetes y reglas ArchUnit.
- El dominio depende exclusivamente de Java. `Community` es inmutable y contiene `id`, `name`, `administratorId`, `allowedExchangeTypes` y `policyRevision`.
- Tres puertos de entrada y un puerto de persistencia. La aplicación depende del dominio y de Reactor; los adaptadores dependen de los puertos.
- REST mediante WebFlux, DTOs propios, Jakarta Validation y Problem Details. MongoDB utiliza `ReactiveMongoTemplate`, con un documento separado del dominio.
- Los starters de prueba son `spring-boot-starter-webflux-test` y `spring-boot-starter-data-mongodb-reactive-test`. La prueba HTTP usa `org.springframework.boot.webflux.test.autoconfigure.WebFluxTest`.
- La conexión MongoDB se configura con `spring.mongodb.uri`; la prueba de integración selecciona su base aislada mediante `spring.mongodb.database`.
- Jackson 3 rechaza propiedades adicionales mediante `spring.jackson.deserialization.fail-on-unknown-properties` y revisiones decimales mediante `accept-float-as-int: false`. El rechazo de ordinales numéricos de enumeraciones se configura en `spring.jackson.datatype.enum.fail-on-numbers-for-enums`.
- ArchUnit se integra con JUnit 6 mediante su módulo específico. Se conservan las seis reglas de arquitectura y los mismos casos de dominio, aplicación, HTTP e integración.

## Alcance y comportamiento HTTP

| Operación | Ruta | Resultado satisfactorio |
| --- | --- | --- |
| Create Community | `POST /v1/communities` | 201 con cuerpo y `Location` |
| Get Community by ID | `GET /v1/communities/{id}` | 200 |
| Update Community Rules | `PUT /v1/communities/{id}/rules` | 200 con nueva revisión |

El nombre debe tener contenido y un máximo de 100 caracteres. Las reglas contienen al menos un tipo entre `DONATION`, `LOAN` y `EXCHANGE`. La revisión inicial es 1. Cada actualización aceptada reemplaza el conjunto completo de tipos e incrementa la revisión una sola vez.

Las escrituras requieren una única cabecera `X-Demo-User` perteneciente al conjunto configurado, por defecto `alice,bob`. El creador determina el administrador. Sólo éste puede actualizar las reglas; GET es público. La cabecera es una identidad de demostración controlada, no una autenticación de producción. El servicio y MongoDB escuchan por defecto en loopback.

Cada actualización exige `expectedPolicyRevision`. La aplicación verifica la revisión leída y MongoDB condiciona atómicamente la escritura por ID y revisión, sin upsert ni reintento automático. Dos escrituras basadas en la misma revisión no pueden aceptarse ambas. No se implementan operaciones adicionales.

Errores: 400 para entrada inválida; 401 para identidad ausente, ambigua o desconocida; 403 para un actor que no sea administrador; 404 para una comunidad inexistente; 409 para conflicto de revisión; 503 para fallo de acceso a persistencia. Se conservan 405 y 415 para métodos o formatos HTTP incompatibles.

## Verificación reproducible

Desde la raíz del repositorio, con Java 21 y Maven:

```bash
mvn clean verify
docker compose up -d --wait
MONGODB_TEST_URI='mongodb://127.0.0.1:27017' mvn -Pintegration verify
```

La primera invocación no necesita MongoDB y ejecuta 55 pruebas. El perfil de integración vuelve a ejecutar esas pruebas y añade 5 contra un servidor HTTP real y MongoDB real. No se suman ambas invocaciones como pruebas distintas.

| Suite | Casos de referencia |
| --- | ---: |
| Dominio | 15 |
| Aplicación | 9 |
| HTTP con repositorio en memoria | 25 |
| ArchUnit | 6 |
| HTTP/MongoDB reales | 5 |
| **Total distinto** | **60** |

La integración comprueba el ciclo crear/consultar/actualizar, la preservación de datos ante rechazos, el 404 sin inserción y la concurrencia tanto por HTTP como directamente contra MongoDB. Cada ejecución crea y elimina únicamente su base `gate1_it_<UUID>`; el prefijo es un identificador interno de aislamiento. Si falta `MONGODB_TEST_URI` o la base no está disponible, la integración falla.

CI verifica los informes de las cinco suites y exige **cero failures, errors y skipped**. También comprueba que no falten casos de referencia, incluidas las seis reglas ArchUnit. No se desactivan pruebas ni se ignoran fallos. El artefacto `technical-baseline-evidence` contiene los informes Surefire/Failsafe, los totales, el commit comprobado, el entorno de build y el árbol completo de dependencias resueltas.

### Evidencia de aceptación

Pendiente de registrar la ejecución satisfactoria correspondiente a esta baseline. El perfil sólo se acepta cuando la suite completa y la comprobación de informes han terminado correctamente.

## Estructura

```text
pom.xml
compose.yaml
README.md
.github/workflows/verify.yml
docs/technical-baseline.md
communities-service/
  pom.xml
  src/main/java/com/paravai/communities/
    CommunitiesApplication.java
    domain/
    application/port/in/
    application/port/out/
    adapter/in/rest/
    adapter/out/mongo/
    configuration/
  src/main/resources/application.yaml
  src/test/java/com/paravai/communities/
    domain/
    application/
    architecture/
    adapter/in/rest/
    integration/
    support/
```

## Fuentes técnicas

- [Publicación estable de Spring Boot 4.1.1](https://spring.io/blog/2026/08/20/spring-boot-4-1-1-available-now).
- [POM de dependencias de Spring Boot 4.1.1 en Maven Central](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.1/spring-boot-dependencies-4.1.1.pom).
- [Requisitos de Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html).
- [ArchUnit 1.5.0 y soporte de JUnit 6](https://github.com/TNG/ArchUnit/releases/tag/v1.5.0).

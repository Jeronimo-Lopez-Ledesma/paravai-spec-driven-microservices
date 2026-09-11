# Gate 1: communities-service

## Alcance

Tres operaciones: crear una comunidad, consultarla por ID y reemplazar sus reglas. Un microservicio ejecutable y una base MongoDB. El objetivo del gate es demostrar una baseline ejecutable y verificable con límites hexagonales claros.

## Decisiones

- Agregador Maven raíz y un único módulo; separación por paquetes para evitar una estructura de módulos innecesaria.
- Dominio inmutable con UUID, nombre, administrador, conjunto de tipos y revisión de política. Los tipos del gate son DONATION, LOAN y EXCHANGE.
- Tres puertos de entrada y un puerto de persistencia reactivo. La aplicación acepta Reactor como dependencia explícita; el dominio depende sólo de Java.
- REST con DTOs separados del dominio, validación de entrada, identidad de demostración limitada a usuarios configurados y errores Problem Details.
- MongoDB mediante ReactiveMongoTemplate. La actualización usa una condición por ID y revisión, sin upsert ni bloqueo de la hebra de ejecución.
- La revisión también actúa como condición de concurrencia para las reglas. No hay transacciones entre documentos.
- Código escrito específicamente para esta baseline, sin incorporar bibliotecas internas ni módulos externos.
- No se implementan componentes ni operaciones adicionales a este gate.

## Riesgos y límites

La identidad de demostración es seleccionable por el cliente y no ofrece autenticación real. Servicio y base se publican por defecto sólo en loopback. La configuración local no es una configuración de producción.

Spring Boot 3.5.16 conserva JUnit 5, pero la rama ha terminado su soporte OSS, como indica su nota oficial enlazada en el README. Una actualización de plataforma deberá validarse como cambio independiente.

El contrato de concurrencia exige que el cliente lea y envíe la revisión. Un conflicto devuelve 409 y obliga a tomar una decisión explícita. No se garantiza que una solicitud POST repetida sea deduplicada.

## Verificación

Pendiente de completar con los resultados observados de la ejecución de Maven y de la integración HTTP/Mongo. La existencia de pruebas no equivale a haberlas superado.

## Estructura e inventario

Todos los archivos de la siguiente lista son nuevos salvo README.md, que sustituye el título inicial. No se eliminan archivos.

```text
.github/workflows/verify.yml
.gitignore
README.md
communities-service/pom.xml
communities-service/src/main/java/com/paravai/communities/CommunitiesApplication.java
communities-service/src/main/java/com/paravai/communities/adapter/in/rest/ApiExceptionHandler.java
communities-service/src/main/java/com/paravai/communities/adapter/in/rest/CommunityController.java
communities-service/src/main/java/com/paravai/communities/adapter/in/rest/CommunityResponse.java
communities-service/src/main/java/com/paravai/communities/adapter/in/rest/CreateCommunityRequest.java
communities-service/src/main/java/com/paravai/communities/adapter/in/rest/DemoAuthenticationException.java
communities-service/src/main/java/com/paravai/communities/adapter/in/rest/DemoIdentityResolver.java
communities-service/src/main/java/com/paravai/communities/adapter/in/rest/UpdateCommunityRulesRequest.java
communities-service/src/main/java/com/paravai/communities/adapter/out/mongo/CommunityDocument.java
communities-service/src/main/java/com/paravai/communities/adapter/out/mongo/MongoCommunityRepository.java
communities-service/src/main/java/com/paravai/communities/application/CommunityNotFoundException.java
communities-service/src/main/java/com/paravai/communities/application/CommunityService.java
communities-service/src/main/java/com/paravai/communities/application/CommunityStorageException.java
communities-service/src/main/java/com/paravai/communities/application/PolicyConflictException.java
communities-service/src/main/java/com/paravai/communities/application/port/in/CreateCommunityUseCase.java
communities-service/src/main/java/com/paravai/communities/application/port/in/GetCommunityUseCase.java
communities-service/src/main/java/com/paravai/communities/application/port/in/UpdateCommunityRulesUseCase.java
communities-service/src/main/java/com/paravai/communities/application/port/out/CommunityRepository.java
communities-service/src/main/java/com/paravai/communities/configuration/CommunityConfiguration.java
communities-service/src/main/java/com/paravai/communities/domain/Community.java
communities-service/src/main/java/com/paravai/communities/domain/ExchangeType.java
communities-service/src/main/java/com/paravai/communities/domain/InvalidCommunityException.java
communities-service/src/main/java/com/paravai/communities/domain/NotAdministratorException.java
communities-service/src/main/resources/application.yaml
communities-service/src/test/java/com/paravai/communities/adapter/in/rest/CommunityHttpTest.java
communities-service/src/test/java/com/paravai/communities/application/CommunityServiceTest.java
communities-service/src/test/java/com/paravai/communities/architecture/HexagonalArchitectureTest.java
communities-service/src/test/java/com/paravai/communities/domain/CommunityTest.java
communities-service/src/test/java/com/paravai/communities/integration/CommunityHttpMongoIT.java
communities-service/src/test/java/com/paravai/communities/support/InMemoryCommunityRepository.java
compose.yaml
docs/gate-1.md
pom.xml
```

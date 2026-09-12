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

**Resultado observado: GATE 1 PASS.**

La ejecución [34655443399](https://github.com/Jeronimo-Lopez-Ledesma/paravai-spec-driven-microservices/actions/runs/34655443399), activada por `push` con `head_sha` igual a `33a1b5956e14caec6b18f1794f816cce1def2a0b`, terminó con estado `completed` y conclusión `success` el 11 de septiembre de 2026. Utilizó Java 21 (Temurin 21.0.12), Spring Boot 3.5.16 y MongoDB 8.0.30 arrancado con el Compose del repositorio.

Comandos ejecutados: `mvn -B -ntp clean verify` y, tras arrancar MongoDB mediante `docker compose up -d --wait`, `mvn -B -ntp -Pintegration verify`. Ambas invocaciones de Maven terminaron con BUILD SUCCESS. Se compiló y empaquetó el JAR ejecutable del servicio.

| Grupo | Pruebas | Fallos | Errores | Omitidas |
| --- | ---: | ---: | ---: | ---: |
| Dominio | 15 | 0 | 0 | 0 |
| Aplicación | 9 | 0 | 0 | 0 |
| HTTP con repositorio en memoria | 25 | 0 | 0 | 0 |
| ArchUnit | 6 | 0 | 0 | 0 |
| HTTP/MongoDB reales | 5 | 0 | 0 | 0 |
| **Total de pruebas distintas** | **60** | **0** | **0** | **0** |

La integración comprueba:

1. Crear, leer y actualizar mediante HTTP, inspeccionando también el documento persistido.
2. Rechazar escrituras sin permiso, con revisión obsoleta o reglas inválidas, conservando el documento.
3. Responder 404 sin insertar comunidades inexistentes.
4. Resolver dos peticiones HTTP competidoras con un 200 y un 409, conservando una única nueva revisión.
5. Rechazar en MongoDB una segunda escritura construida a partir de la misma revisión, incluso sin depender de la comprobación previa de la aplicación.

Las seis reglas ArchUnit verifican dependencias del dominio y la aplicación, separación de adaptadores, uso de puertos y ausencia de dependencia de adaptadores respecto a la implementación del servicio.

El workflow verifica también de forma explícita `mvn clean verify` sin MongoDB, antes de arrancar Compose, y después `mvn -Pintegration verify`. Esta segunda invocación vuelve a ejecutar las 55 pruebas normales y añade las 5 de integración: no deben sumarse ambas invocaciones como pruebas distintas.

Los informes XML están en `communities-service/target/surefire-reports/` y `communities-service/target/failsafe-reports/`; Actions los conserva en el artefacto `gate-1-test-reports`.

## Incidencias encontradas

- Una etiqueta de MongoDB anunciada como binario todavía no estaba publicada como imagen Docker. El primer intento de CI falló antes de Maven. Se fijó la imagen publicada `mongo:8.0.30` y el siguiente arranque y verificación completos pasaron.
- El entorno local perdió la conexión durante la preparación de la verificación; la compilación inicial había pasado. La ejecución completa se realizó en GitHub Actions con Docker y MongoDB reales.
- Actions avisó de la retirada de Node 20 para acciones v4. El workflow se actualizó a las acciones v5, que usan Node 24.
- Spring Test/Mockito emite un aviso de instrumentación dinámica en Java 21. No provoca fallos ni se oculta; las pruebas de aplicación utilizan dobles escritos a mano. El log de indisponibilidad de persistencia procede de una prueba que fuerza ese error y espera 503.
- La limitación de soporte de Spring Boot 3.5 permanece expresamente documentada. No se han desactivado pruebas ni configurado opciones para ignorar sus fallos.

El pase de este gate acredita las tres operaciones y su baseline técnica; no acredita autenticación de producción, rendimiento o tolerancia a fallos distribuida.

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

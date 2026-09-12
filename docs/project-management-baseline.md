# Project Management Baseline

**Identificador:** PMB-1.1. **Estado:** propuesta de gestión preparada para revisión antes del desarrollo funcional. La aceptación de esta baseline se registra mediante la PR correspondiente; no equivale a una aprobación académica del tutor.

Este documento establece el alcance, el backlog inicial y las reglas de planificación y seguimiento del TFM. Su creación es exclusivamente documental. Ninguna historia propuesta se considera iniciada o terminada por figurar aquí.

## 1. Punto de partida y restricciones

La [Final Technical Baseline](technical-baseline.md) está integrada en `main`. La referencia de partida de esta planificación es el commit [`bf9024df36e7457a218d14c4107c912081c2dab8`](https://github.com/Jeronimo-Lopez-Ledesma/paravai-spec-driven-microservices/commit/bf9024df36e7457a218d14c4107c912081c2dab8). Existe un único servicio, `communities-service`, con tres operaciones y 60 pruebas distintas documentadas, incluidas cinco de HTTP/MongoDB real. Sus evidencias y condiciones de aceptación se conservan en la baseline técnica.

El perfil congelado es Java 21, Spring Boot 4.1.1, WebFlux 7.0.9, Reactor 3.8.7 y Spring Data Reactive MongoDB 5.1.1, con MongoDB 8.0.30. Se mantienen Maven, JUnit 6.0.3, ArchUnit 1.5.0 y Docker Compose. Esta planificación no reabre esas decisiones. El generador será Python, exclusivamente en design-time; su versión concreta y dependencias se fijarán al preparar su primera historia, sin cambiar el perfil del runtime.

No se dispone aquí de fechas oficiales de PEC, entrega o defensa, ni de una dedicación semanal confirmada. Los sprints y milestones siguientes expresan una propuesta relativa, no un calendario académico ni una garantía de finalización en un plazo determinado. MG-01 debe completar esos datos con fuentes reales antes de comprometer la carga del primer sprint.

## 2. Project Vision y Product Goal

Desarrollar y evaluar un mecanismo determinista de generación y regeneración a partir de especificaciones para automatizar tareas estructurales repetitivas de microservicios web, mantener separada la lógica de negocio manual y verificar automáticamente su conformidad arquitectónica. Paravai aporta un caso de estudio de dos contextos, Communities y Offers, con exactamente ocho operaciones funcionales comprometidas. Se evaluará el efecto sobre el esfuerzo y se medirá el coste adicional del mecanismo, sin anticipar un beneficio.

El usuario del generador es el desarrollador de servicios. Los usuarios de demostración permiten verificar el comportamiento de negocio. El lector de la memoria debe poder seguir cada afirmación hasta una decisión, un artefacto y una evidencia reproducible.

**Product Goal:** disponer de dos servicios ejecutables bajo el mismo perfil tecnológico, un generador limitado a ese perfil, regeneración segura y una evaluación trazable que permita juzgar qué se automatiza, con qué coste y con qué límites.

La pregunta que orienta el backlog es:

**“¿En qué medida un mecanismo determinista de generación y regeneración basado en especificaciones permite construir y evolucionar microservicios web manteniendo separada la lógica de negocio manual y verificando automáticamente su conformidad arquitectónica?”**

El efecto sobre el esfuerzo, el coste y las divergencias detectadas es objeto de observación; no se presupone una reducción ni una mejora de productividad. Un resultado neutro, negativo o inconcluso, medido y explicado, sigue siendo académicamente válido. El producto sí debe cumplir sus criterios técnicos y las ocho operaciones comprometidas.

No se atribuirá al generador la resolución de problemas inherentes a la distribución, ni se presentarán dos servicios como prueba de generalización a cualquier dominio o tecnología.

## 3. Scope / Out of Scope

### 3.1 Alcance funcional comprometido

| Área | Límite de esta baseline de gestión |
| --- | --- |
| Caso de estudio | Exactamente dos Bounded Contexts: Communities y Offers; un agregado raíz por contexto |
| Operaciones | Exactamente ocho operaciones Must: tres aceptadas de Communities y cinco por desarrollar de Offers. Es el compromiso funcional; una novena operación requiere necesariamente un `CR-nn` |
| Communities | Conservar modelo, contratos, identidad de demostración, invariantes y control de revisión existentes |
| Offers | Crear, consultar por ID, listar activas por comunidad, pausar y retirar; estados ACTIVE, PAUSED y WITHDRAWN |
| Distribución | Dos servicios ejecutables por separado, propiedad de datos separada y una dependencia REST de Offers hacia Communities |
| Persistencia | MongoDB reactivo, con una base lógica propia por servicio: `communities` para `communities-service` y `offers` para `offers-service`. Una única instancia MongoDB puede alojar ambas en desarrollo local; se mantiene el aislamiento temporal de las bases de prueba |
| Generación | CLI Python, un formato de especificación YAML, una versión inicial de esquema y un único perfil Java/Spring/WebFlux/MongoDB |
| Arquitectura | Dominio sólo Java, puertos explícitos y separación REST/persistencia; reglas de negocio manuales |
| Evaluación | Dos casos, pruebas automatizadas, regeneración, repetibilidad, inventario generado/manual y esfuerzo registrado; resultados y límites en la memoria |
| Entrega | Código, especificaciones, comandos de reproducción, evidencias, memoria y material de defensa conforme a los requisitos académicos que se confirmen |

Cada servicio es propietario exclusivo de su base lógica. Ningún servicio accede directamente a la base ni a las colecciones del otro; la comunicación entre contextos se realiza exclusivamente mediante sus contratos. Compartir la instancia MongoDB en desarrollo local no implica compartir una base lógica ni requiere dos servidores MongoDB o dos contenedores.

| ID | Operación | Situación al planificar |
| --- | --- | --- |
| US-C01 | `POST /v1/communities` — Create Community | Aceptada en la baseline técnica |
| US-C02 | `GET /v1/communities/{id}` — Get Community by ID | Aceptada en la baseline técnica |
| US-C03 | `PUT /v1/communities/{id}/rules` — Update Community Rules | Aceptada en la baseline técnica |
| US-O01 | `POST /v1/offers` — Create Offer | Must comprometida; pendiente de desarrollo |
| US-O02 | `GET /v1/offers/{id}` — Get Offer by ID | Must comprometida; pendiente de desarrollo |
| US-O03 | `GET /v1/communities/{communityId}/offers` — List Offers by Community | Must comprometida; pendiente de desarrollo |
| US-O04 | `POST /v1/offers/{id}/pause` — Pause Offer | Must comprometida; pendiente de desarrollo |
| US-O05 | `DELETE /v1/offers/{id}` — Withdraw Offer | Must comprometida; pendiente de desarrollo |

El modelo mínimo de Offer contiene exclusivamente `id`, `communityId`, `title`, `description` opcional, `exchangeType`, `ownerId`, `status` y `validatedPolicyRevision`. Los estados son `ACTIVE`, `PAUSED` y `WITHDRAWN`. Una creación válida deja la oferta ACTIVE.

**Lógica de negocio manual:** obtener y conservar un propietario inmutable desde la identidad de demostración; autorizar pausa y retirada sólo a ese propietario; controlar las transiciones; definir qué se publica; validar el tipo de intercambio contra Communities al crear; y conservar la revisión observada. El cuerpo no puede imponer propietario, estado inicial ni revisión validada. Estas decisiones no se expresan como reglas ejecutables del generador.

| Estado actual | Pause Offer | Withdraw Offer |
| --- | --- | --- |
| ACTIVE | PAUSED, 200 con representación | WITHDRAWN, 204 |
| PAUSED | Permanece PAUSED, 200, sin nuevo efecto | WITHDRAWN, 204 |
| WITHDRAWN | 409, sin modificación ni reactivación | Permanece WITHDRAWN, 204, sin nuevo efecto |

La tabla presupone identidad válida, propietario autorizado y oferta existente. Las peticiones ausentes de identidad válida, de otro propietario o sobre un ID inexistente producen 401, 403 o 404, respectivamente; no se elude la autorización en una repetición. La retirada es lógica: DELETE conserva los datos y el estado WITHDRAWN; Get Offer by ID puede devolver esa representación con 200. No existe operación de reactivación ni borrado físico. Pause y Withdraw preservan propietario, comunidad, tipo y revisión validada. Las escrituras condicionan el estado de origen de forma atómica sobre el documento, de modo que una pausa concurrente no pueda revertir una retirada. La decisión de transición y su semántica siguen siendo manuales.

List Offers by Community publica únicamente ofertas ACTIVE de la comunidad solicitada, con paginación simple acotada y orden estable que TS-01 concretará. Excluye PAUSED y WITHDRAWN. La ruta pertenece a `offers-service`, aunque incluya `communities`; no requiere Gateway ni una nueva operación en Communities. GET por ID y listado son públicos y leen sólo colecciones de Offers. El listado devuelve 200 con una colección vacía si no hay ofertas activas para el ID de comunidad indicado; no comprueba remotamente su existencia. TS-01 fija límites de entrada y formato de paginación antes de implementar.

**Integración de creación:** Offers consulta Communities por REST y contempla comunidad existente con tipo permitido, comunidad inexistente (404), tipo no permitido (400), y timeout o indisponibilidad (503). Si falla la validación remota, no persiste ninguna Offer. Con una respuesta válida conserva `validatedPolicyRevision`. Las demás operaciones no consultan Communities.

El contrato es una validación respecto de la revisión observada durante la creación. Un cambio posterior de reglas no revalida, invalida ni modifica automáticamente ofertas. Tampoco se promete atomicidad entre la lectura de Communities y la escritura de Offers. Esta limitación debe figurar en contrato, pruebas y memoria. No se introducen transacciones distribuidas, Kafka, sagas, reintentos complejos ni consistencia global; la llamada de creación tiene timeout acotado y no se reintenta automáticamente.

### 3.2 Frontera del generador

Se generarán estructura Maven y paquetes, puertos, DTOs y contratos estructurales, controladores que deleguen en casos de uso, documentos y mapeos Mongo, código repetitivo de acceso a datos, configuración y reglas ArchUnit parametrizadas. Cada familia debe justificarse primero mediante el inventario de TS-02. La prueba de concepto no exige automatizar todos los ficheros del servicio.

La especificación describe nombres, tipos acotados, campos, operaciones y enlaces a puertos. No es un lenguaje de reglas. Las invariantes, autorización, decisiones sobre políticas, coordinación REST, tratamiento específico de concurrencia y aserciones de negocio permanecen bajo autoría manual. En particular, no se generaliza el control de revisión de Communities como un motor de concurrencia.

Pause Offer y Withdraw Offer permiten comprobar deliberadamente esta frontera:

| El generador puede producir | Permanece manual |
| --- | --- |
| DTOs cuando el contrato los necesite; contratos estructurales y puertos | Máquina de estados de Offer y transiciones permitidas |
| Controllers/delegates que invocan los puertos | Autorización del propietario, invariantes y decisiones de negocio |
| Estructura de persistencia, mappings y configuración | Semántica de retirada lógica, repetición y concurrencia de transiciones |
| Reglas arquitectónicas parametrizadas | Comportamiento de publicación de ofertas activas y coordinación específica con Communities |

Generar persistencia no autoriza a deducir que DELETE borra físicamente ni a inferir transiciones a partir de nombres de rutas. Los comandos pausar/retirar delegan la decisión en código manual. No se añade un DTO de petición vacío si la operación no necesita cuerpo. Las pruebas manuales de autorización, estados y publicación se ejecutan antes y después de generar/regenerar; la memoria debe demostrar con esos casos y con los ficheros conservados que se automatiza estructura sin absorber lógica de dominio.

La estrategia propuesta separa ficheros propiedad del generador de ficheros manuales, unidos mediante interfaces o composición. No se editan manualmente ficheros generados. La regeneración se ejecuta sobre salidas identificadas mediante manifiesto y rechaza colisiones o modificaciones inesperadas antes de sobrescribir. No se implementa fusión de código ni regiones protegidas. El dominio puede incorporarse al proyecto resultante como código manual; no se deduce de un CRUD genérico.

### 3.3 Won't Have en este TFM

Quedan fuera: un tercer Bounded Context; Membership; Resource como agregado independiente; Exchange/Trading; Reputation; Disputes; Notifications; frontend; OAuth o identidad de producción; Kafka, gRPC, Eureka, Gateway, Redis/Valkey, CQRS y Outbox; transacciones distribuidas, sagas, reintentos complejos y consistencia global; Kubernetes o despliegue de producción; otros motores de bases de datos o persistencia políglota; otros lenguajes de runtime; generador Java alternativo, varios perfiles del generador, plugins, editor visual, LLM en generación o reglas de negocio generadas. El aislamiento efímero de las pruebas MongoDB aceptadas no añade otra base de aplicación al producto.

También quedan fuera observabilidad o pruebas de carga como líneas de trabajo independientes, búsqueda avanzada, listados distintos del listado activo por comunidad, edición de ofertas, reactivación, borrado físico y negociación. Pause, Withdraw y el listado comprometido sí forman parte del alcance.

El análisis de alternativas puede aparecer en la memoria sin construirlas. No se incorpora una plataforma compartida de abstracciones para dos servicios. Las ocho operaciones enumeradas son el compromiso funcional Must; cualquier operación adicional requiere un `CR-nn` antes de implementarse.

## 4. Enfoque de trabajo para un desarrollador

La recomendación es **Agile ligero inspirado en Scrum, con sprints de dos semanas y un límite explícito de trabajo en curso**. Se toman de Scrum el objetivo de producto, objetivos de sprint, backlog ordenado e inspección periódica; no se declara una aplicación íntegra del marco ni se simula un equipo con roles independientes. La [Scrum Guide](https://scrumguides.org/scrum-guide.html) distingue responsabilidades y eventos formales; la adaptación de este proyecto se hace explícita.

| Alternativa | Utilidad para este proyecto | Valoración |
| --- | --- | --- |
| Scrum adaptado | Cadencia para demostrar incrementos y revisar conjuntamente código, evaluación y memoria | Recomendado: favorece entregables verificables antes de los hitos académicos que se confirmen |
| Kanban | Visualización del flujo, límites de WIP y gestión de bloqueos; útil para incidencias y correcciones | Se incorporan esas prácticas al tablero, sin sustituir los Sprint Goals por una cola continua |
| Scrumban | Combinación flexible de cadencia y flujo | No se adopta como método adicional: para una persona no aporta una regla operativa necesaria que no esté ya descrita aquí |

La [Kanban Guide](https://kanbanguides.org/the-kanban-guide/2025.5/) fundamenta el uso de estados, control de WIP y métricas de flujo. Los límites, umbrales y tiempos siguientes son decisiones propias de este proyecto.

El autor prioriza el producto, desarrolla, verifica y mantiene el proceso. El tutor aporta contraste académico cuando su disponibilidad lo permita; no se presupone que actúe como Product Owner, Scrum Master o revisor de cada PR. La revisión propia se identifica como tal y no se presenta como revisión independiente. La evaluación académica corresponde a los órganos establecidos por la universidad.

| Actividad | Regla de trabajo propuesta | Registro mínimo |
| --- | --- | --- |
| Sprint Planning | Hasta 45 minutos al inicio; un Goal, capacidad disponible e historias Ready | Selección inicial y previsión de carga |
| Seguimiento individual | Cinco minutos por jornada efectiva de trabajo | Avance hacia el Goal, siguiente acción y bloqueo |
| Refinement | Hasta 30 minutos por semana de trabajo | Dividir incertidumbre y preparar el siguiente sprint |
| Sprint Review | Hasta 30 minutos al cierre; demostración contra aceptación y evidencias | Aceptado/no aceptado y efecto sobre backlog |
| Retrospective | Hasta 20 minutos después de la review | Una mejora concreta, responsable y comprobación posterior |

Un único tablero sirve para producto, técnica, investigación y documentación: **Backlog → Ready → In Progress → Review → Done**. `Blocked` es una marca que conserva el estado y la antigüedad. Máximo una historia en In Progress y dos en total entre In Progress y Review, incluyendo bloqueadas. Se documenta toda excepción; un bloqueo no habilita abrir varias tareas paralelas. La redacción necesaria para cerrar una historia forma parte de ella.

GitHub Issues y PRs serán los registros operativos; los IDs de esta baseline se conservarán aunque se creen issues posteriormente. Este documento no afirma que esos issues, un Project o automatizaciones existan ya. No se necesita una herramienta de gestión adicional.

## 5. Priorización y estimación

Se aplica [MoSCoW](https://www.agilebusiness.org/resource/what-is-moscow-prioritization/) al horizonte completo del TFM: **Must** para sostener la demostración académica o técnica; **Should** para mejoras valiosas con alternativa aceptable; **Could** para mejoras prescindibles; **Won't** para exclusiones explícitas. Prioridad no equivale a orden de ejecución: mandan también dependencias y reducción de riesgo.

Estimación relativa inicial: **1, 2, 3, 5, 8 puntos**. Un punto representa una modificación documental o verificación acotada; tres, una pieza técnica con varios casos y dependencias conocidas; cinco, una integración o generación con incertidumbre y evidencia propia. Ocho obliga a dividir antes de entrar en un sprint. Son anclas de comparación, no horas ni productividad del autor. Las estimaciones son provisionales y se revisan en refinement.

Cada historia incluye implementación, pruebas, evidencia y actualización documental pertinentes. Las historias DOC producen síntesis de capítulos y no vuelven a contar el registro técnico incluido en otras historias. No se suman puntos de épicas ni de tareas a los de sus historias. Los elementos aceptados al iniciar el proyecto no reciben puntos retrospectivos ni se cuentan en velocidad.

Antes de cada Planning se registran horas realmente disponibles, ausencias y trabajo académico necesario. Se reserva inicialmente **un 20 % de esa capacidad** para incidencias e incertidumbre; es una política revisable, no una estimación de contingencia calculada. Investigación y memoria son trabajo planificado, no parte de esa reserva. No se fija una velocidad inicial. Tras dos sprints se usan los puntos terminados y la disponibilidad observada como orientación, mostrando un rango y sin convertir puntos en horas.

## 6. Epics y Product Backlog inicial

| Epic | Resultado buscado |
| --- | --- |
| E1 — Delimitación y método | Contratos, capacidad, protocolo e inventario que permitan desarrollar y evaluar sin ampliar alcance |
| E2 — Caso de estudio distribuido | Communities y Offers con exactamente ocho operaciones Must, estados manuales de Offer y una integración REST mínima verificable |
| E3 — Generación estructural | Generar para ambos servicios y regenerar preservando el código manual |
| E4 — Evaluación reproducible | Medidas, resultados, limitaciones y evidencias que soporten la contribución |
| E5 — Memoria y entrega | Relato académico actualizado, reproducción final y defensa |

`US` identifica una User Story; `TS`, una Technical Story; `MG`, gestión; `DOC`, documentación académica. Todas las historias nuevas están en **Backlog**. El sprint indicado es una previsión, no una asignación cerrada. Las dependencias son IDs; los padres E1–E5 no añaden trabajo estimado.

### 6.1 Comportamiento ya aceptado

| ID / Epic | User Story y aceptación conservada | MoSCoW | Puntos / estado |
| --- | --- | --- | --- |
| US-C01 / E2 | Como usuario de demostración, quiero crear una comunidad para definir sus tipos permitidos. 201, administrador derivado de identidad, reglas válidas y revisión inicial 1 | Must | — / Aceptada en baseline técnica |
| US-C02 / E2 | Como consumidor, quiero consultar una comunidad por ID para conocer sus reglas. 200 con modelo actual o 404 | Must | — / Aceptada en baseline técnica |
| US-C03 / E2 | Como administrador, quiero sustituir las reglas con revisión esperada para evitar perder cambios. 200 e incremento único; 403/409 y persistencia intacta ante rechazo | Must | — / Aceptada en baseline técnica |

Se mantienen las demás validaciones y errores de la baseline técnica. Estas historias actúan como requisitos de regresión al introducir generación, sin reconstruir su esfuerzo como trabajo nuevo.

### 6.2 Gestión, contratos y funcionalidad propuesta

| ID / Epic | Historia y criterio de aceptación observable | MoSCoW | Puntos | Dependencias | Sprint |
| --- | --- | --- | ---: | --- | --- |
| MG-01 / E1 | Registrar capacidad, requisitos académicos confirmados, calendario disponible y trazabilidad operativa. Cada fecha tiene fuente; lo desconocido sigue marcado y se fija cuándo revisarlo | Must | 2 | — | S1 |
| TS-01 / E1 | Delimitar contratos de los dos contextos y vocabulario de la especificación. Exactamente ocho operaciones, modelo reducido, estados, retirada lógica, identidad, paginación acotada, errores y validación sobre revisión observada; propiedad de colecciones y frontera manual/generada explícitas antes de implementar Offers | Must | 3 | — | S1 |
| TS-02 / E1 | Fijar protocolo de evaluación e inventario de candidatos a generar. Definir denominadores, comparación neutral, coste adicional, casos y exclusiones antes de medir; identificar lógica manual que conservar y casos independientes de Pause/Withdraw para verificar la frontera | Must | 3 | TS-01 | S1 |
| TS-03 / E2 | Implementar puerto y adaptador REST de consulta de reglas desde Offers. Respuesta válida entrega tipos y revisión; comunidad inexistente 404, tipo no permitido 400, timeout o indisponibilidad 503; ningún fallo de validación permite persistir Offer, sin reintento automático | Must | 3 | TS-01, US-C02 | S2 |
| US-O01 / E2 | **Create Offer.** Como usuario de demostración, quiero publicar una oferta permitida en una comunidad para compartir un recurso. 201 con Location, estado ACTIVE, descripción opcional, propietario inmutable derivado de identidad y validatedPolicyRevision obtenida por REST; no se aceptan valores impuestos de propietario/estado/revisión. Entrada o tipo no permitido 400, identidad inválida 401, comunidad ausente 404 y fallo remoto 503; rechazos sin escritura | Must | 5 | TS-03 | S2 |
| US-O02 / E2 | **Get Offer by ID.** Como consumidor, quiero consultar una oferta para conocer sus datos y estado. 200 con el modelo completo, incluida descripción si existe y validatedPolicyRevision, o 404; también 200 para WITHDRAWN conservada. GET público y sin consultar Communities, incluso si éste no está disponible | Must | 2 | US-O01 | S2 |
| US-O03 / E2 | **List Offers by Community.** Como consumidor, quiero listar las ofertas activas de una comunidad para conocer lo publicado. 200 paginado, acotado y con orden estable; sólo communityId solicitado y status ACTIVE, sin PAUSED/WITHDRAWN; colección vacía si no hay coincidencias, entrada inválida 400. Ruta servida por Offers, pública y sin llamada remota; probar filtros y límites con datos de los tres estados | Must | 3 | US-O01 | S2 |
| US-O04 / E2 | **Pause Offer.** Como propietario, quiero pausar mi oferta para dejar de publicarla. ACTIVE pasa a PAUSED con 200; repetir sobre PAUSED conserva estado y devuelve 200; WITHDRAWN produce 409 y nunca se reactiva. Identidad inválida 401, otro propietario 403 e inexistente 404; rechazos sin modificación. Conserva el resto del modelo y desaparece del listado activo; decisión de estado y autorización manuales | Must | 3 | US-O01, US-O02, US-O03 | S3 |
| US-O05 / E2 | **Withdraw Offer.** Como propietario, quiero retirar definitivamente mi oferta para dejar de ofrecerla. DELETE lleva ACTIVE o PAUSED a WITHDRAWN y responde 204; repetir por el propietario devuelve 204 sin efecto adicional. Identidad inválida 401, otro propietario 403 e inexistente 404; conserva datos y revisión, GET refleja WITHDRAWN y el listado activo la excluye. Probar que una pausa concurrente o posterior no revierte la retirada; semántica y autorización manuales | Must | 3 | US-O04 | S3 |

### 6.3 Generación y evaluación

| ID / Epic | Technical Story y criterio de aceptación observable | MoSCoW | Puntos | Dependencias | Sprint |
| --- | --- | --- | ---: | --- | --- |
| TS-04 / E3 | Crear CLI Python y validación de especificaciones. Python y dependencias fijados; dos especificaciones válidas que describen los contratos estructurales de las ocho operaciones y entradas inválidas; errores antes de escribir; inicialmente sólo salida vacía y rutas contenidas en el destino, sin reglas de negocio en el esquema | Must | 3 | TS-01, TS-02 | S1 |
| TS-05 / E3 | Generar las familias repetitivas seleccionadas para Communities. Salida compila al combinarse con sus piezas manuales, mantiene sus tres contratos y pasa las pruebas existentes; manifiesto identifica entradas, versión y ficheros generados | Must | 5 | TS-04 | S3 |
| TS-06 / E3 | Permitir regeneración con propiedad explícita de ficheros. Misma entrada produce misma salida; cambio estructural acotado tiene diff esperado; código manual idéntico antes/después; colisión o edición inesperada falla sin sobrescritura parcial | Must | 5 | TS-05 | S4 |
| TS-07 / E3 | Aplicar las mismas plantillas a Offers. Las cinco operaciones pasan contratos, integración y arquitectura; Pause/Withdraw delegan autorización y estados en piezas manuales preservadas, sin DELETE físico deducido por el generador; variaciones estructurales en especificación y sin plantillas bifurcadas por servicio | Must | 5 | TS-05, US-O01, US-O02, US-O03, US-O04, US-O05 | S4 |
| TS-08 / E3 | Encadenar generación, compilación y prueba de los dos servicios en un entorno limpio. Las ocho operaciones, regeneración, MongoDB real y REST entre procesos pasan; Maven y pruebas del generador con cero failures/errors/skipped, sin omitir casos de estados/autorización; evidencias vinculadas al SHA comprobado | Must | 3 | TS-06, TS-07 | S4 |
| TS-09 / E4 | Ejecutar la evaluación definitiva sobre ambos casos sólo tras M3 y M4. Inventario generado/manual, determinismo, regeneración, tiempos y coste adicional con datos brutos; aceptación equivalente de las ocho operaciones en referencia y resultado generado, incluida frontera manual de Pause/Withdraw | Must | 5 | TS-02, TS-08 | S5 |
| TS-10 / E4 | Analizar resultados, coste adicional y amenazas a la validez. Distinguir tareas automatizadas, efecto observado sobre esfuerzo, conservación de lógica manual y conformidad cubierta por reglas; resultados neutros/negativos válidos, sin presuponer ahorro, menor divergencia o productividad | Must | 3 | TS-09 | S5 |
| TS-11 / E4 | Preparar paquete de evidencias reproducible. Especificaciones, fuentes/referencias de commit, informes, comandos y hashes; resultados legibles fuera de Actions y sin depender sólo de su retención temporal | Must | 2 | TS-09 | S5 |

### 6.4 Memoria, cierre y mejoras prescindibles

| ID / Epic | Historia y criterio de aceptación observable | MoSCoW | Puntos | Dependencias | Sprint |
| --- | --- | --- | ---: | --- | --- |
| DOC-01 / E5 | Redactar problema, objetivos, estado del arte y método. Fuentes verificables, alcance y protocolo coherentes; afirmaciones propuestas diferenciadas de resultados | Must | 2 | TS-01, TS-02 | S1 |
| DOC-02 / E5 | Completar el diseño del caso de estudio a partir de los registros de S1/S2. Ocho contratos, estados y retirada lógica, propiedad de datos y revisión observada enlazados con código/pruebas; autorización y publicación identificadas como lógica manual | Must | 2 | TS-03, US-O01, US-O02, US-O03, US-O04, US-O05 | S3 |
| DOC-03 / E5 | Redactar diseño del generador. Esquema, plantillas, extensión manual y regeneración explicados a partir de los artefactos existentes | Must | 2 | TS-06, TS-07, TS-08 | S4 |
| DOC-04 / E5 | Incorporar evaluación y discusión a la memoria. Cada tabla se obtiene de evidencia identificada; incluir resultados negativos y límites de generalización | Must | 2 | TS-10, TS-11 | S5 |
| TS-12 / E5 | Reproducir la entrega desde checkout limpio y revisar instrucciones. Suite completa satisfactoria, artefactos identificados y ningún paso manual oculto; defectos bloqueantes resueltos | Must | 3 | TS-11, DOC-03 | S6 |
| DOC-05 / E5 | Cerrar memoria y preparar defensa. Objetivos, resultados, conclusiones y trazabilidad consistentes; referencias revisadas, demostración ensayada y requisitos docentes confirmados comprobados | Must | 3 | DOC-01, DOC-02, DOC-03, DOC-04, TS-12 | S6 |
| TS-13 / E3 | Mejorar mensajes de diagnóstico de la CLI con ejemplos de corrección. No amplía el esquema ni añade una interfaz nueva | Should | 2 | TS-04 | Sólo con capacidad sobrante |
| TS-14 / E4 | Obtener una reproducción por otro lector, si hay disponibilidad acordada. Registrar entorno, incidencias y diferencias; en su ausencia, reconocer la limitación de autorreproducción | Should | 3 | TS-11 | Sólo con capacidad sobrante |
| DOC-06 / E5 | Preparar una grabación breve de respaldo de la demostración, si es útil para la defensa y compatible con sus instrucciones | Could | 1 | TS-12 | Sólo con capacidad sobrante |

Un resultado neutro, negativo o inconcluso del estudio no convierte TS-09 o TS-10 en fallidas si cumplen el protocolo. En cambio, perder código manual, romper contratos o aceptar pruebas omitidas impide cerrar las historias técnicas afectadas. Las mejoras Should/Could no desplazan Must ni consumen automáticamente la reserva.

**Recuento del backlog:** cinco épicas y 29 historias: tres US-C aceptadas sin puntos retrospectivos y 26 pendientes. Las pendientes son cinco US-O, catorce TS, una MG y seis DOC; 23 son Must (72 puntos), dos Should (5 puntos) y una Could (1 punto). Los puntos Must se distribuyen en la sección 9; las tres mejoras opcionales quedan sin sprint comprometido. No se crean historias técnicas adicionales porque los incrementos actuales están acotados a 2–5 puntos, pero Ready sigue exigiendo que quepan en la capacidad real. Si una US-O no cabe, se descompone en TS trazables, se distribuyen sus puntos sin doble contabilización y la US-O sólo se acepta cuando cumple todo su resultado funcional Must.

## 7. Definition of Ready

Una historia entra en Ready cuando:

1. Tiene ID, epic, propósito, MoSCoW, estimación y aceptación comprobable, incluidos errores relevantes.
2. Cabe en un sprint según la capacidad disponible; si no, se divide manteniendo un resultado verificable por parte. No basta con estimarla en cinco puntos.
3. Sus dependencias están resueltas o existe un orden viable dentro del sprint; no depende de una decisión académica o contractual desconocida para su aceptación.
4. Sus contratos, datos de prueba, propiedad generado/manual y restricciones aplicables están identificados.
5. Se sabe qué prueba o evidencia permitirá aceptarla y qué sección de la memoria afectará.
6. No introduce una ampliación de alcance pendiente de decisión. La incertidumbre que requiera exploración se convierte en una tarea acotada con pregunta y criterio de salida.

Las historias actuales son estimaciones iniciales de backlog; no se declaran Ready automáticamente. La falta de una fecha de defensa no bloquea por sí sola un contrato técnico independiente, pero sí impide prometer una fecha final.

## 8. Definition of Done

Para cualquier historia: aceptación demostrada, revisión propia registrada, decisiones actualizadas, enlaces de trazabilidad completos y defectos conocidos clasificados. Un defecto que invalide un criterio o una evidencia impide Done. Para el incremento integrado, el cambio debe estar incorporado a `main` mediante el proceso de revisión acordado; una PR abierta permanece en Review.

| Tipo de cambio | Evidencia necesaria adicional |
| --- | --- |
| Runtime o plantillas que lo producen | Compilación, dominio, aplicación, HTTP, ArchUnit e integración afectada; suite completa de aceptación del incremento con HTTP/MongoDB real y cero failures/errors/skipped. Conservar los 60 casos iniciales; añadir casos de las cinco operaciones Offers y de generación según se incorporan. M3/M4 exigen las ocho operaciones completas antes de TS-09 |
| Generador | Pruebas de validación, determinismo, rutas/propiedad de salida y regeneración según la historia; hashes del código manual conservados y salida bajo el perfil congelado. En M4, pruebas independientes de las ocho operaciones antes/después de regenerar, incluida máquina de estados, autorización y publicación de Offer |
| Evaluación | M3 y M4 alcanzados para la evaluación definitiva; ocho operaciones y regeneración segura disponibles. Protocolo y versión de artefactos identificados, datos brutos conservados, cálculos reproducibles y limitaciones declaradas |
| Documentación o gestión exclusivamente | Revisión de coherencia, enlaces, IDs, cifras, fuentes y alcance; no requiere inventar pruebas unitarias ni ejecutar infraestructura por el contenido del documento |

No se fuerza verde desactivando pruebas, cambiando aserciones para ocultar regresiones o retirando checks. El número de pruebas puede crecer; toda sustitución de un caso inicial exige justificar equivalencia de cobertura y trazabilidad. La aceptación técnica se vincula a commit completo y ejecución correspondiente, distinguiendo el SHA de código de cualquier commit sintético de una PR.

Done no significa aprobado por la universidad ni hipótesis confirmada. Una historia incompleta vuelve al backlog, sin puntos parciales ni cambio retrospectivo del compromiso inicial. Si se divide, se conserva el vínculo y se evita contar dos veces el mismo trabajo.

## 9. Propuesta de sprints y Sprint Goals

Se proponen **seis sprints de dos semanas**, sujetos a MG-01. Las doce semanas resultantes son un horizonte de trabajo candidato, no fechas académicas disponibles. Si capacidad y calendario no encajan, se replantea el horizonte antes de comprometerlo; no se comprime silenciosamente la evaluación o la memoria. Los puntos no justifican por sí solos la duración.

| Sprint | Sprint Goal | Selección candidata Must | Puntos Must | Review: resultado demostrable |
| --- | --- | --- | ---: | --- |
| S1 | Disponer de contratos y especificaciones verificables para construir y evaluar las ocho operaciones | MG-01, TS-01, TS-02, TS-04, DOC-01 | 13 | Ocho contratos delimitados, CLI que valida entradas estructurales, capacidad registrada, protocolo previo y capítulo de método revisable |
| S2 | Publicar y consultar ofertas activas con validación de comunidad y propiedad de datos definida | TS-03, US-O01, US-O02, US-O03 | 13 | Crear, consultar y listar por comunidad con MongoDB real; creación mediante REST entre procesos, rechazos sin escritura y listado sólo ACTIVE |
| S3 | Demostrar la frontera entre estructura generada y comportamiento manual con las ocho operaciones definidas | US-O04, US-O05, TS-05, DOC-02 | 13 | Ocho operaciones aceptadas en la referencia manual, transiciones y retirada verificadas; Communities generado conserva sus tres contratos y pruebas; diseño documentado |
| S4 | Demostrar estructura generada y regeneración segura conservando la lógica manual de las ocho operaciones | TS-06, TS-07, TS-08, DOC-03 | 15 | Ambos servicios pasan CI; misma entrada produce misma salida, código manual preservado, Pause/Withdraw mantienen semántica y proceso completo reproducible |
| S5 | Responder la pregunta de evaluación con datos trazables y sin anticipar resultados | TS-09, TS-10, TS-11, DOC-04 | 12 | Resultados de las ocho operaciones, determinismo, conformidad, efecto sobre esfuerzo y coste adicional; paquete de evidencia y capítulo de evaluación |
| S6 | Entregar un trabajo verificable y defendible | TS-12, DOC-05 | 6 | Reproducción limpia, cierre de defectos bloqueantes, memoria coherente y ensayo de defensa |
| **Total** | **Seis sprints candidatos** | **23 historias Must pendientes** | **72** | Las tres US-C aceptadas no añaden puntos ni velocidad retrospectiva |

La validación de especificaciones TS-04 se sitúa en S1 después de TS-01/02. Generar Communities (TS-05, S3) depende de esa CLI y de su comportamiento ya aceptado, sin esperar a generar Offers. En S2, el filtro de US-O03 se prueba también con datos preparados PAUSED/WITHDRAWN; en S3, US-O04/05 comprueban su efecto real sobre listado y consulta. La generación de Offers (TS-07, S4) depende expresamente de **US-O01–05 completas** y de TS-05. La evaluación definitiva TS-09 no comienza hasta alcanzar M3 y M4; las mediciones y registros de trabajo desde S1 se conservan, pero no se presentan como campaña definitiva.

La documentación de los contratos y decisiones se mantiene en S1/S2 dentro de Done. DOC-02 integra esos registros y el comportamiento de estados al cerrar S3; DOC-03 integra el diseño del generador en S4. No se difiere la redacción a S6. S3 registra también las decisiones de generación aunque su síntesis termine en S4. S5 conserva la evaluación y su capítulo completo. S6 permanece dedicado principalmente a cierre, reproducción, memoria y defensa, sin nuevas operaciones.

Estos puntos describen carga relativa, **no capacidad disponible ni garantía de cabida en dos semanas**. S4 concentra la mayor carga Must (15 puntos). MG-01 y las reviews de S1/S2 deben contrastar esa previsión con disponibilidad y trabajo terminado antes de seleccionar historias. Si no cabe, se aplica el orden de reducción de la sección 12 y se replantea explícitamente el horizonte mediante una nueva baseline cuando sea necesario; no se ocupan S5/S6 con funcionalidad pendiente ni se recortan evaluación o documentación para mantener seis sprints de forma aparente.

Should/Could sólo se seleccionan después de confirmar margen para todos los Must restantes y para la entrega. Ningún sprint se acepta únicamente por producir código sin sus evidencias y documentación correspondientes.

### Milestones

| Milestone | Momento relativo | Criterio de salida | Estado inicial |
| --- | --- | --- | --- |
| M0 — Technical Baseline | Antes de esta planificación | Perfil congelado y evidencia técnica aceptada | Alcanzado; referencia en sección 1 |
| M1 — Management Baseline | Antes de iniciar trabajo funcional | Documento revisado por el autor, alcance y reglas de seguimiento aceptados | Pendiente de revisión de esta propuesta |
| M2 — Contratos y protocolo | Cierre de S1 | TS-01/02 y DOC-01 Done; calendario/capacidad registrados con incertidumbres explícitas | Pendiente |
| M3 — Caso distribuido | Cierre de S3 | Exactamente ocho operaciones aceptadas; identidad, estados y retirada manuales, listado ACTIVE y escenarios REST/MongoDB demostrados | Pendiente |
| M4 — Generación validable | Cierre de S4 | Dos salidas con las ocho operaciones verificadas, regeneración segura y lógica manual conservada; habilita TS-09 junto con M3 | Pendiente |
| M5 — Evaluación cerrada | Cierre de S5 | Evaluación definitiva posterior a M3/M4, datos, análisis neutral, coste adicional y límites reproducibles | Pendiente |
| M6 — Entrega preparada | Cierre de S6 | Reproducción y documentación final conformes a requisitos confirmados | Pendiente |

Los hitos internos no sustituyen PEC, depósito ni defensa. MG-01 los relacionará con las fechas oficiales y conservará el origen de cada dato. La fecha de merge de una PR acredita integración, no una fecha académica.

## 10. Review y retrospective

La review responde cuatro preguntas: ¿se logró el Sprint Goal?, ¿qué historias cumplen Done?, ¿qué evidencia permite afirmarlo?, ¿qué cambió en riesgos, alcance o previsión? La demostración usa rutas y casos de aceptación, no sólo una lista de ficheros. Al cerrar S3 se revisan las ocho operaciones; al cerrar S4 se repiten sus criterios sobre las salidas generadas, con foco en identidad, estados, retirada, publicación y código manual conservado. Para investigación se muestran datos y reproducción; para memoria, un capítulo con referencias y coherencia verificable. Se registra también lo no aceptado y el trabajo que queda.

Se solicita contraste del tutor sólo en los puntos académicos y con la cadencia que se acuerde. Si no participa, la review sigue siendo una revisión del autor, sin atribuirle aprobación externa. Las observaciones recibidas se convierten en historias, defectos o solicitudes de cambio identificadas.

La retrospective revisa tamaño de historias, bloqueos, retrabajo, estimación, tiempo real disponible y demora de documentación. Produce **una mejora aplicable al siguiente sprint**, por ejemplo dividir antes las historias de integración. Al siguiente cierre se comprueba su efecto; no se recopilan acciones indefinidamente. Cambiar la estimación para hacer coincidir la previsión con lo ocurrido no constituye una mejora.

## 11. Riesgos iniciales

Probabilidad e impacto son valoraciones cualitativas iniciales: alta/media/baja. El autor es responsable de todos los riesgos; solicita contraste técnico o académico cuando corresponde. Los umbrales son políticas de alerta, no probabilidades medidas.

| ID | Riesgo y valoración P/I | Señal de activación | Mitigación y respuesta |
| --- | --- | --- | --- |
| R1 | Capacidad o calendario incompatibles — alta/alta | Disponibilidad desconocida o carga de S2–S4 incompatible con seis sprints, dejando las ocho operaciones sin margen antes de S5 | MG-01 y revisión de previsión en S1/S2; retirar Could, Should, simplificar familias generadas y mejoras no obligatorias, en ese orden; si no basta, registrar desviación y proponer nueva baseline sin consumir evaluación/cierre |
| R2 | Crecimiento del caso de estudio — alta/alta | Se propone novena operación, tercer contexto o más estados/transiciones que los comprometidos | Exactamente ocho operaciones Must; cualquier operación adicional exige CR-nn. Mantener sólo ACTIVE, PAUSED y WITHDRAWN y las transiciones definidas; registrar todo cambio antes de desarrollarlo |
| R3 | Generador convertido en framework o motor de negocio — alta/alta | Segundo perfil, DSL de estados, plugins, excepciones por servicio o retirada física inferida de DELETE | Un esquema estructural y perfil; inventario previo; transiciones, autorización y retirada manuales detrás de puertos. Simplificar familias automatizadas sin debilitar regeneración ni su evaluación |
| R4 | Evidencia insuficiente o conclusión académica sesgada — media/alta | Sólo se cuentan ficheros, no se verifica frontera manual/conformidad o se da por supuesta una mejora | Protocolo previo, casos Pause/Withdraw independientes, determinismo y regeneración; medir efecto sobre esfuerzo y coste adicional con denominadores explícitos. Un resultado neutro o negativo no constituye por sí mismo un riesgo materializado |
| R5 | Regeneración destructiva — media/alta | Diffs en código manual, colisiones o escritura parcial tras error | Propiedad de ficheros, manifiesto, validación previa y hashes; bloquear entrega ante pérdida; no añadir un motor de merge |
| R6 | Integración REST amplía semántica distribuida — media/alta | Se pide consistencia global, revalidar todas las ofertas o reintentos complejos | Contrato de revisión observada, tiempo de espera acotado y fallo sin escritura; explicar la limitación y mantener una sola llamada |
| R7 | Infraestructura o dependencias bloquean CI — media/alta | Falla MongoDB/runner o se propone cambiar el perfil congelado | Reproducir con Compose y versiones fijadas; distinguir fallo de entorno; si exige reparar infraestructura ajena, parar y reconstruir sólo una envolvente mínima mediante decisión explícita |
| R8 | Memoria o evidencia diferidas al final — alta/alta | Historia sin enlace a memoria o resultados sólo en logs temporales | Documentación en Done, DOC por incremento y paquete de evidencia en S5; frenar nuevas historias si la trazabilidad está incompleta |
| R9 | Sesgo por autor único y pruebas acopladas a plantillas — alta/media | Las mismas plantillas producen código y únicas aserciones; tiempos reconstruidos de memoria | Pruebas de negocio independientes, registros contemporáneos y límites de autorreproducción; TS-14 opcional, sin asumir disponibilidad externa |

Cada review actualiza exposición, acción y estado de riesgos. Un riesgo materializado se vincula al defecto o cambio que lo trata; no se elimina del registro para presentar una evolución sin incidencias.

## 12. Gestión de cambios de alcance

El backlog operativo puede refinarse; la baseline aceptada se conserva mediante Git. El compromiso funcional es de **exactamente ocho operaciones Must en dos contextos**. Una novena operación requiere necesariamente un `CR-nn`. Cada ampliación, reducción de un Must, cambio de perfil o modificación del protocolo tras comenzar a medir se registra igualmente como `CR-nn`.

El registro contiene solicitante/origen, motivo, situación actual y propuesta, historias y contratos afectados, impacto en estimación/capacidad/milestones/evaluación/memoria, elemento que se retira o capacidad que lo financia, decisión y evidencia de aceptación. El autor decide ajustes operativos dentro de los límites; los que alteren objetivos o compromisos académicos se contrastan con el tutor según el procedimiento docente. No se afirma una autorización que no conste.

La secuencia es: registrar → analizar impacto → decidir aceptar/rechazar/posponer → actualizar backlog y previsión → implementar. No se sustituye ninguna de las ocho operaciones ni se empieza una adicional sin una decisión de cambio registrada. Un cambio urgente que invalide el Sprint Goal exige registrar su replanteamiento; no se reescribe el Goal original como si siempre hubiese sido otro.

Un defecto es un incumplimiento de aceptación existente, no una vía para introducir funcionalidad. Ante riesgo de plazo, el orden de reducción es:

1. Eliminar Could.
2. Eliminar Should.
3. Simplificar las familias de artefactos que genera el generador, manteniendo un mecanismo evaluable y la separación manual/generado.
4. Reducir la profundidad de mejoras no obligatorias.

Se registran el trabajo retirado y su impacto. No se reducen silenciosamente **los dos contextos, las ocho operaciones, la regeneración segura, la evaluación ni la memoria**. Si conservarlos deja de ser viable, se registra explícitamente la desviación, se propone una nueva baseline y se decide su aceptación antes de continuar con otro compromiso. No se desplazan la evaluación o la documentación al cierre para ocultar funcionalidad incompleta, ni se reduce la calidad de las pruebas para conservar el plan aparente.

## 13. Seguimiento de desviaciones

En Planning se conserva una instantánea del Goal, historias/puntos seleccionados, capacidad disponible, milestone esperado y riesgos. En la review se registra lo terminado, incorporado, retirado, bloqueado y pendiente. Se miden por separado **alcance, capacidad, ejecución, calidad y documentación**.

| Indicador | Registro e interpretación | Umbral de intervención propuesto |
| --- | --- | --- |
| Goal y compromiso | Goal logrado/no logrado con evidencia; puntos Done frente a selección inicial, y añadidos aparte | Goal incumplido: replanificar inmediatamente; menos del 80 % inicial terminado en dos sprints: reducir carga y dividir historias |
| Capacidad | Horas disponibles previstas/reales y motivo de diferencia; no deducidas de puntos | Caída superior al 20 %: recalcular previsión del trabajo restante |
| Flujo | WIP, historias terminadas, antigüedad y tiempo entre In Progress y Done | Bloqueo durante dos jornadas previstas de trabajo o exceso de WIP: resolver, dividir o posponer antes de abrir más trabajo |
| Alcance | Must restantes, avance de las ocho operaciones y cambios CR; puntos añadidos/retirados visibles | Novena operación: CR-nn obligatorio. Un Must nuevo o M3/M4 amenazado: revisar financiación y milestone antes de iniciar evaluación definitiva |
| Calidad | Resultados por suite, fallos, errores, omitidos y defectos abiertos | Cualquier incumplimiento de la aceptación técnica: historia no Done y prioridad de corrección |
| Trazabilidad | Historias Done con enlace a evidencia y memoria aplicable | Cualquier hueco: corregir el registro antes de cerrar la review |

Se utiliza un burn-up sencillo por sprint: alcance estimado actual frente a puntos terminados, anotando cambios de estimación y alcance. Los puntos de la baseline técnica ya aceptada quedan fuera. No se suaviza el gráfico cambiando cifras históricas. Las horas de evaluación se registran separadas de esta métrica de gestión.

Después de dos sprints comparables puede calcularse un rango orientativo de sprints restantes a partir del trabajo pendiente y la capacidad de finalización observada. Si cambia la dedicación, el tipo de trabajo o no hay historias terminadas, se declara que no hay base fiable para ese cálculo. Hasta conocer el calendario oficial no se traduce la previsión en una fecha de entrega.

Plantilla de cierre: `Sprint | Goal inicial | capacidad prevista/real | IDs iniciales | IDs Done | incorporados/retirados | desviación y causa | milestone previsto | riesgos/CR | acción siguiente | enlaces de evidencia`. Se conserva el plan y el resultado, sin reemplazar uno por otro.

## 14. Backlog, implementación, evaluación y memoria

La unidad de trazabilidad es el ID de historia. Su ficha enlaza requisito/objetivo, contrato o especificación, decisión de diseño, PR y commit integrado, pruebas/casos de evaluación, ejecución y artefactos, y sección de memoria. Una afirmación cuantitativa de la memoria apunta además al conjunto de datos y al procedimiento de cálculo. No se usan enlaces a `main` mutable como única identificación de una evidencia.

| Objetivo | Historias principales | Implementación / artefacto esperado | Evidencia | Memoria |
| --- | --- | --- | --- | --- |
| Delimitar un caso viable | MG-01, TS-01, DOC-01 | Contratos, alcance y capacidad | Decisiones y fuentes de requisitos | Introducción, objetivos y planificación |
| Preservar comportamiento y demostrar distribución | US-C01–03, TS-03, US-O01–05, DOC-02 | Ocho operaciones, estados manuales y contrato REST de creación | Regresión e integración real, listado ACTIVE, autorización, pausa/retirada y rechazos sin escritura | Análisis y diseño del caso de estudio |
| Automatizar estructura con límites | TS-04–07, US-O04–05, DOC-03 | Esquema, plantillas, manifiestos y código manual, sin máquina de estados generada | Compilación, ArchUnit, hashes y semántica de Pause/Withdraw conservada | Diseño e implementación del generador |
| Evaluar efecto sobre esfuerzo, coste adicional, determinismo y conformidad | TS-02, TS-08–11, DOC-04 | Protocolo, datos e informes de las ocho operaciones | Ejecuciones exactas y cálculos reproducibles, con resultados neutros/negativos admitidos | Evaluación, discusión y amenazas |
| Entregar un resultado verificable | TS-12, DOC-05 | Checkout y documentación de entrega | Reproducción y revisión final | Conclusiones y defensa |

El protocolo mínimo de TS-02 incluirá:

- **Casos:** Communities y Offers con las ocho operaciones terminadas antes de la campaña definitiva; mismos contratos y código manual de negocio para comparar referencia y resultado generado. Pause/Withdraw aportan pruebas independientes de autorización, máquina de estados, retirada lógica y publicación; esos casos se ejecutan antes y después de regenerar. No se mantienen dos productos alternativos; se conservan instantáneas identificadas para evaluación.
- **Compilación y comportamiento:** pruebas de aceptación independientes de las plantillas, suite completa de las ocho operaciones y reglas ArchUnit. Se registran resultados de conformidad y violaciones observadas sin presuponer menor divergencia. La conformidad sólo acredita las reglas comprobadas, no toda propiedad arquitectónica posible.
- **Regeneración:** salida inicial, tres ejecuciones idénticas por caso y un cambio estructural acotado por caso en una copia de evaluación; hashes esperados, código manual intacto y ensayo de colisión. Ese cambio no introduce una operación de producto. Las repeticiones comprueban determinismo, no constituyen una muestra para inferencia estadística.
- **Proporción de artefactos:** contar por familia ficheros fuente/configuración de autoría generada y manual; publicar numerador, denominador, exclusiones y clasificación. Excluir dependencias, binarios, `target`, logs y el propio generador del denominador de salida. Un porcentaje de generación no equivale a ahorro de esfuerzo ni se fija como meta arbitraria.
- **Esfuerzo y coste adicional:** registrar desde S1 tiempo efectivo de diseño de especificación, plantillas, implementación manual, ejecución, corrección y documentación por separado. No reconstruir horas de la baseline técnica. Comparar tareas estructurales equivalentes y reconocer aprendizaje/orden; si no existe medición comparable, declarar el efecto temporal no evaluable. Medir explícitamente el coste de construir, validar y mantener el mecanismo, además de su uso; una diferencia nula o desfavorable se informa sin reformularla como beneficio.
- **Reproducibilidad:** versiones, SHA de código/generador/especificaciones, entorno, comandos, informes y hashes de evidencia. La campaña puede producir resultados favorables, neutros, negativos o inconclusos; no se ajustan métricas después para asegurar una conclusión favorable.

Al cerrar cada sprint se actualizan las fichas y la memoria afectada. Al alcanzar M5 se congela el conjunto de datos utilizado en las conclusiones; una corrección posterior produce una versión nueva con motivo y efecto identificados. La procedencia de código, bibliografía y herramientas de asistencia se registra con transparencia conforme a las instrucciones docentes que se confirmen.

## 15. Condición de activación y recomendación

Esta propuesta estará lista para activar el desarrollo funcional cuando el autor acepte M1 y las historias seleccionadas cumplan Ready. MG-01 registra la dedicación y las fechas disponibles; ninguna ausencia se sustituye por una suposición presentada como dato. La aceptación y los cambios posteriores quedan en el historial de PRs.

Se recomienda mantener **un único enfoque Agile ligero inspirado en Scrum**, con sprints de dos semanas, un Goal por sprint, tablero común y WIP limitado. La cadencia permite comprobar regularmente que avanzan juntos implementación, evaluación y memoria, mientras que el control de flujo contiene el trabajo abierto de un único desarrollador.

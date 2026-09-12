# Project Management Baseline

**Identificador:** PMB-1.0. **Estado:** propuesta de gestión preparada para revisión antes del desarrollo funcional. La aceptación de esta baseline se registra mediante la PR correspondiente; no equivale a una aprobación académica del tutor.

Este documento establece el alcance, el backlog inicial y las reglas de planificación y seguimiento del TFM. Su creación es exclusivamente documental. Ninguna historia propuesta se considera iniciada o terminada por figurar aquí.

## 1. Punto de partida y restricciones

La [Final Technical Baseline](technical-baseline.md) está integrada en `main`. La referencia de partida de esta planificación es el commit [`bf9024df36e7457a218d14c4107c912081c2dab8`](https://github.com/Jeronimo-Lopez-Ledesma/paravai-spec-driven-microservices/commit/bf9024df36e7457a218d14c4107c912081c2dab8). Existe un único servicio, `communities-service`, con tres operaciones y 60 pruebas distintas documentadas, incluidas cinco de HTTP/MongoDB real. Sus evidencias y condiciones de aceptación se conservan en la baseline técnica.

El perfil congelado es Java 21, Spring Boot 4.1.1, WebFlux 7.0.9, Reactor 3.8.7 y Spring Data Reactive MongoDB 5.1.1, con MongoDB 8.0.30. Se mantienen Maven, JUnit 6.0.3, ArchUnit 1.5.0 y Docker Compose. Esta planificación no reabre esas decisiones. El generador será Python, exclusivamente en design-time; su versión concreta y dependencias se fijarán al preparar su primera historia, sin cambiar el perfil del runtime.

No se dispone aquí de fechas oficiales de PEC, entrega o defensa, ni de una dedicación semanal confirmada. Los sprints y milestones siguientes expresan una propuesta relativa, no un calendario académico ni una garantía de finalización en un plazo determinado. MG-01 debe completar esos datos con fuentes reales antes de comprometer la carga del primer sprint.

## 2. Project Vision y Product Goal

Desarrollar y evaluar un mecanismo de generación a partir de especificaciones estructurales que permita construir microservicios web con una arquitectura hexagonal consistente, reduciendo tareas repetitivas y preservando el código de negocio implementado manualmente. Paravai aporta un caso de estudio acotado a Communities y Offers.

El usuario del generador es el desarrollador de servicios. Los usuarios de demostración permiten verificar el comportamiento de negocio. El lector de la memoria debe poder seguir cada afirmación hasta una decisión, un artefacto y una evidencia reproducible.

**Product Goal:** disponer de dos servicios ejecutables bajo el mismo perfil tecnológico, un generador limitado a ese perfil, regeneración segura y una evaluación trazable que permita juzgar qué se automatiza, con qué coste y con qué límites.

La pregunta que orienta el backlog es: ¿en qué medida la generación de estructura repetitiva reduce intervención manual y divergencias arquitectónicas en estos dos servicios, manteniendo compilabilidad, comportamiento y conservación del código de negocio? Se espera una reducción observable, pero no se presupone su magnitud. Un resultado negativo o inconcluso, medido y explicado, sigue siendo un resultado académico válido. El producto sí debe cumplir sus criterios técnicos.

No se atribuirá al generador la resolución de problemas inherentes a la distribución, ni se presentarán dos servicios como prueba de generalización a cualquier dominio o tecnología.

## 3. Scope / Out of Scope

### 3.1 Alcance comprometido propuesto

| Área | Límite de esta baseline de gestión |
| --- | --- |
| Caso de estudio | Exactamente dos Bounded Contexts: Communities y Offers; un agregado raíz por contexto |
| Operaciones | Cinco en total: tres existentes de Communities y dos propuestas de Offers. Ocho es el techo absoluto acordado, no un objetivo que haya que completar |
| Communities | Conservar modelo, contratos, identidad de demostración, invariantes y control de revisión existentes |
| Offers | Crear una oferta y consultarla por ID; sin ciclo de vida adicional |
| Distribución | Dos servicios ejecutables por separado, propiedad de datos separada y una dependencia REST de Offers hacia Communities |
| Persistencia | MongoDB reactivo; bases separadas, que pueden compartir una instancia local. Ningún servicio accede a las colecciones del otro |
| Generación | CLI Python, un formato de especificación YAML, una versión inicial de esquema y un único perfil Java/Spring/WebFlux/MongoDB |
| Arquitectura | Dominio sólo Java, puertos explícitos y separación REST/persistencia; reglas de negocio manuales |
| Evaluación | Dos casos, pruebas automatizadas, regeneración, repetibilidad, inventario generado/manual y esfuerzo registrado; resultados y límites en la memoria |
| Entrega | Código, especificaciones, comandos de reproducción, evidencias, memoria y material de defensa conforme a los requisitos académicos que se confirmen |

| ID | Operación | Situación al planificar |
| --- | --- | --- |
| US-C01 | `POST /v1/communities` — Create Community | Aceptada en la baseline técnica |
| US-C02 | `GET /v1/communities/{id}` — Get Community by ID | Aceptada en la baseline técnica |
| US-C03 | `PUT /v1/communities/{id}/rules` — Update Community Rules | Aceptada en la baseline técnica |
| US-O01 | `POST /v1/offers` — Create Offer | Propuesta, pendiente de refinamiento y desarrollo |
| US-O02 | `GET /v1/offers/{id}` — Get Offer by ID | Propuesta, pendiente de refinamiento y desarrollo |

Para Offers se propone el mínimo `id`, `communityId`, `title`, `exchangeType`, `ownerId` y `validatedPolicyRevision`. El propietario procede de la identidad de demostración; no puede imponerse desde el cuerpo. Crear requiere consultar Communities, comprobar que existe y que admite el tipo de intercambio, y registrar la revisión consultada. La consulta de una oferta lee sólo su propia base.

La comprobación remota tiene validez respecto de la revisión observada: un cambio posterior de reglas no invalida ni modifica automáticamente ofertas. Tampoco se promete atomicidad entre la lectura de Communities y la escritura de Offers. Esta limitación debe figurar en contrato, pruebas y memoria. No se introduce una transacción distribuida para ocultarla. TS-01 concreta el contrato antes de programarlo.

### 3.2 Frontera del generador

Se generarán estructura Maven y paquetes, puertos, DTOs y contratos estructurales, controladores que deleguen en casos de uso, documentos y mapeos Mongo, código repetitivo de acceso a datos, configuración y reglas ArchUnit parametrizadas. Cada familia debe justificarse primero mediante el inventario de TS-02. La prueba de concepto no exige automatizar todos los ficheros del servicio.

La especificación describe nombres, tipos acotados, campos, operaciones y enlaces a puertos. No es un lenguaje de reglas. Las invariantes, autorización, decisiones sobre políticas, coordinación REST, tratamiento específico de concurrencia y aserciones de negocio permanecen bajo autoría manual. En particular, no se generaliza el control de revisión de Communities como un motor de concurrencia.

La estrategia propuesta separa ficheros propiedad del generador de ficheros manuales, unidos mediante interfaces o composición. No se editan manualmente ficheros generados. La regeneración se ejecuta sobre salidas identificadas mediante manifiesto y rechaza colisiones o modificaciones inesperadas antes de sobrescribir. No se implementa fusión de código ni regiones protegidas. El dominio puede incorporarse al proyecto resultante como código manual; no se deduce de un CRUD genérico.

### 3.3 Won't Have en este TFM

Quedan fuera: un tercer contexto; Membership; frontend; OAuth o identidad de producción; Kafka, gRPC, Eureka, Gateway, Redis/Valkey, CQRS y outbox; transacciones distribuidas; Kubernetes o despliegue de producción; observabilidad o pruebas de carga como líneas de trabajo independientes; búsquedas, listados, cancelación o negociación de ofertas; nuevas bases de datos o lenguajes de runtime; generador Java alternativo, múltiples perfiles, plugins, editor visual, generación con LLM o reglas de negocio generadas.

El análisis de alternativas puede aparecer en la memoria sin construirlas. No se incorpora una plataforma compartida de abstracciones para dos servicios. Las operaciones sexta a octava también quedan sin compromiso: incorporarlas requiere el procedimiento de cambio, aunque no se supere el techo.

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
| E2 — Caso de estudio distribuido | Communities y Offers con cinco operaciones y una integración REST mínima verificable |
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
| TS-01 / E1 | Delimitar contratos de los dos contextos y vocabulario de la especificación. Inventario exacto de cinco operaciones, propiedad de datos, campos, errores y consistencia remota; decisiones pendientes resueltas antes de implementar Offers | Must | 3 | — | S1 |
| TS-02 / E1 | Fijar protocolo de evaluación e inventario de candidatos a generar. Definir denominadores, comparación, casos y exclusiones antes de recoger resultados; identificar ficheros manuales que deben conservarse | Must | 3 | TS-01 | S1 |
| TS-03 / E2 | Implementar puerto y adaptador REST de consulta de reglas desde Offers. 404 remoto y rechazo de tipos tienen respuesta documentada; timeout o indisponibilidad producen 503 y no permiten persistir una oferta; sin reintento automático | Must | 3 | TS-01, US-C02 | S2 |
| US-O01 / E2 | Como usuario de demostración, quiero crear una oferta de un tipo permitido en una comunidad para compartir un recurso. 201 con Location; propietario controlado, consulta remota y revisión observada persistida; entrada inválida 400, identidad inválida 401, comunidad ausente 404; rechazos sin escritura | Must | 5 | TS-03 | S2 |
| US-O02 / E2 | Como consumidor, quiero consultar una oferta por ID para conocer sus datos. 200 o 404; GET público y sin consultar Communities, incluso si éste no está disponible | Must | 2 | US-O01 | S2 |

### 6.3 Generación y evaluación

| ID / Epic | Technical Story y criterio de aceptación observable | MoSCoW | Puntos | Dependencias | Sprint |
| --- | --- | --- | ---: | --- | --- |
| TS-04 / E3 | Crear CLI Python y validación de especificaciones. Python y dependencias fijados; dos especificaciones válidas y entradas inválidas; errores antes de escribir; inicialmente sólo salida vacía y rutas contenidas en el destino | Must | 3 | TS-01, TS-02 | S3 |
| TS-05 / E3 | Generar las familias repetitivas seleccionadas para Communities. Salida compila al combinarse con sus piezas manuales, mantiene sus tres contratos y pasa las pruebas existentes; manifiesto identifica entradas, versión y ficheros generados | Must | 5 | TS-04 | S3 |
| TS-06 / E3 | Permitir regeneración con propiedad explícita de ficheros. Misma entrada produce misma salida; cambio estructural acotado tiene diff esperado; código manual idéntico antes/después; colisión o edición inesperada falla sin sobrescritura parcial | Must | 5 | TS-05 | S4 |
| TS-07 / E3 | Aplicar las mismas plantillas a Offers. Segundo servicio generado pasa sus contratos, integración y arquitectura; variaciones estructurales en especificación, sin bifurcar plantillas por nombre de servicio | Must | 5 | TS-05, US-O01, US-O02 | S4 |
| TS-08 / E3 | Encadenar generación, compilación y prueba de los dos servicios en un entorno limpio. Maven y pruebas del generador con cero failures/errors/skipped; MongoDB real y REST entre procesos; evidencias vinculadas al SHA comprobado | Must | 3 | TS-06, TS-07 | S4 |
| TS-09 / E4 | Ejecutar el protocolo sobre ambos casos. Inventario generado/manual, repeticiones y tiempos observados con datos brutos y exclusiones; pruebas de aceptación equivalentes para versión de referencia y resultado generado | Must | 5 | TS-02, TS-08 | S5 |
| TS-10 / E4 | Analizar beneficios, coste y amenazas a la validez. Separar reducción de edición, coste del generador y consistencia cubierta por reglas; no afirmar ahorro neto o significación sin evidencia | Must | 3 | TS-09 | S5 |
| TS-11 / E4 | Preparar paquete de evidencias reproducible. Especificaciones, fuentes/referencias de commit, informes, comandos y hashes; resultados legibles fuera de Actions y sin depender sólo de su retención temporal | Must | 2 | TS-09 | S5 |

### 6.4 Memoria, cierre y mejoras prescindibles

| ID / Epic | Historia y criterio de aceptación observable | MoSCoW | Puntos | Dependencias | Sprint |
| --- | --- | --- | ---: | --- | --- |
| DOC-01 / E5 | Redactar problema, objetivos, estado del arte y método. Fuentes verificables, alcance y protocolo coherentes; afirmaciones propuestas diferenciadas de resultados | Must | 2 | TS-01, TS-02 | S1 |
| DOC-02 / E5 | Redactar diseño del caso de estudio. Contextos, contratos, decisiones y limitación de consistencia enlazados con código y pruebas | Must | 2 | TS-03, US-O01, US-O02 | S2 |
| DOC-03 / E5 | Redactar diseño del generador. Esquema, plantillas, extensión manual y regeneración explicados a partir de los artefactos existentes | Must | 2 | TS-06, TS-07, TS-08 | S4 |
| DOC-04 / E5 | Incorporar evaluación y discusión a la memoria. Cada tabla se obtiene de evidencia identificada; incluir resultados negativos y límites de generalización | Must | 2 | TS-10, TS-11 | S5 |
| TS-12 / E5 | Reproducir la entrega desde checkout limpio y revisar instrucciones. Suite completa satisfactoria, artefactos identificados y ningún paso manual oculto; defectos bloqueantes resueltos | Must | 3 | TS-11, DOC-03 | S6 |
| DOC-05 / E5 | Cerrar memoria y preparar defensa. Objetivos, resultados, conclusiones y trazabilidad consistentes; referencias revisadas, demostración ensayada y requisitos docentes confirmados comprobados | Must | 3 | DOC-01, DOC-02, DOC-03, DOC-04, TS-12 | S6 |
| TS-13 / E3 | Mejorar mensajes de diagnóstico de la CLI con ejemplos de corrección. No amplía el esquema ni añade una interfaz nueva | Should | 2 | TS-04 | Sólo con capacidad sobrante |
| TS-14 / E4 | Obtener una reproducción por otro lector, si hay disponibilidad acordada. Registrar entorno, incidencias y diferencias; en su ausencia, reconocer la limitación de autorreproducción | Should | 3 | TS-11 | Sólo con capacidad sobrante |
| DOC-06 / E5 | Preparar una grabación breve de respaldo de la demostración, si es útil para la defensa y compatible con sus instrucciones | Could | 1 | TS-12 | Sólo con capacidad sobrante |

Un resultado negativo del estudio no convierte TS-09 o TS-10 en fallidas si cumplen el protocolo. En cambio, perder código manual, romper contratos o aceptar pruebas omitidas impide cerrar las historias técnicas afectadas. Las mejoras Should/Could no desplazan Must ni consumen automáticamente la reserva.

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
| Runtime o plantillas que lo producen | Compilación, dominio, aplicación, HTTP, ArchUnit e integración afectada; suite completa de aceptación con HTTP/MongoDB real y cero failures/errors/skipped. Conservar los 60 casos iniciales y añadir los necesarios para Offers y generación |
| Generador | Pruebas de validación, determinismo, rutas/propiedad de salida y regeneración; hashes del código manual conservados; salida bajo el perfil congelado y pruebas de comportamiento independientes de las plantillas |
| Evaluación | Protocolo y versión de artefactos identificados, datos brutos conservados, cálculos reproducibles y limitaciones declaradas |
| Documentación o gestión exclusivamente | Revisión de coherencia, enlaces, IDs, cifras, fuentes y alcance; no requiere inventar pruebas unitarias ni ejecutar infraestructura por el contenido del documento |

No se fuerza verde desactivando pruebas, cambiando aserciones para ocultar regresiones o retirando checks. El número de pruebas puede crecer; toda sustitución de un caso inicial exige justificar equivalencia de cobertura y trazabilidad. La aceptación técnica se vincula a commit completo y ejecución correspondiente, distinguiendo el SHA de código de cualquier commit sintético de una PR.

Done no significa aprobado por la universidad ni hipótesis confirmada. Una historia incompleta vuelve al backlog, sin puntos parciales ni cambio retrospectivo del compromiso inicial. Si se divide, se conserva el vínculo y se evita contar dos veces el mismo trabajo.

## 9. Propuesta de sprints y Sprint Goals

Se proponen **seis sprints de dos semanas**, sujetos a MG-01. Las doce semanas resultantes son un horizonte de trabajo candidato, no fechas académicas disponibles. Si capacidad y calendario no encajan, se replantea el horizonte antes de comprometerlo; no se comprime silenciosamente la evaluación o la memoria. Los puntos no justifican por sí solos la duración.

| Sprint | Sprint Goal | Selección candidata | Review: resultado demostrable |
| --- | --- | --- | --- |
| S1 | Poder implementar y medir el caso de estudio con límites explícitos | MG-01, TS-01, TS-02, DOC-01 | Cinco contratos delimitados, capacidad registrada, protocolo previo y capítulo de método revisable |
| S2 | Demostrar una oferta válida con reglas consultadas en otro servicio | TS-03, US-O01, US-O02, DOC-02 | Crear/consultar oferta entre dos procesos y MongoDB real; rechazo sin escritura cuando Communities falla |
| S3 | Producir estructura útil para Communities sin alterar su comportamiento | TS-04, TS-05 | Especificación válida genera un proyecto compilable con código manual y las pruebas de referencia; entradas inválidas no escriben |
| S4 | Demostrar reutilización de plantillas y regeneración segura en dos servicios | TS-06, TS-07, TS-08, DOC-03 | Ambos servicios pasan CI, mismo perfil, código manual preservado y proceso repetible desde cero |
| S5 | Responder la pregunta de evaluación con datos trazables | TS-09, TS-10, TS-11, DOC-04 | Resultados reproducibles, discusión de coste y límites, paquete de evidencia y capítulo de evaluación |
| S6 | Entregar un trabajo verificable y defendible | TS-12, DOC-05 | Reproducción limpia, cierre de defectos bloqueantes, memoria coherente y ensayo de defensa |

S3 registra diseño y decisiones aunque la síntesis DOC-03 termine en S4. S6 se dedica a cierre y corrección, sin nuevas funciones. Should/Could sólo se seleccionan después de confirmar margen para todos los Must restantes y para la entrega. Ningún sprint puede aprobarse únicamente por producir código sin sus evidencias y documentación correspondientes.

### Milestones

| Milestone | Momento relativo | Criterio de salida | Estado inicial |
| --- | --- | --- | --- |
| M0 — Technical Baseline | Antes de esta planificación | Perfil congelado y evidencia técnica aceptada | Alcanzado; referencia en sección 1 |
| M1 — Management Baseline | Antes de iniciar trabajo funcional | Documento revisado por el autor, alcance y reglas de seguimiento aceptados | Pendiente de revisión de esta propuesta |
| M2 — Contratos y protocolo | Cierre de S1 | TS-01/02 y DOC-01 Done; calendario/capacidad registrados con incertidumbres explícitas | Pendiente |
| M3 — Caso distribuido | Cierre de S2 | Cinco operaciones y escenarios REST/MongoDB demostrados | Pendiente |
| M4 — Generación validable | Cierre de S4 | Dos salidas verificadas y regeneración segura | Pendiente |
| M5 — Evaluación cerrada | Cierre de S5 | Datos, análisis y límites reproducibles | Pendiente |
| M6 — Entrega preparada | Cierre de S6 | Reproducción y documentación final conformes a requisitos confirmados | Pendiente |

Los hitos internos no sustituyen PEC, depósito ni defensa. MG-01 los relacionará con las fechas oficiales y conservará el origen de cada dato. La fecha de merge de una PR acredita integración, no una fecha académica.

## 10. Review y retrospective

La review responde cuatro preguntas: ¿se logró el Sprint Goal?, ¿qué historias cumplen Done?, ¿qué evidencia permite afirmarlo?, ¿qué cambió en riesgos, alcance o previsión? La demostración usa rutas y casos de aceptación, no sólo una lista de ficheros. Para investigación se muestran datos y reproducción; para memoria, un capítulo con referencias y coherencia verificable. Se registra también lo no aceptado y el trabajo que queda.

Se solicita contraste del tutor sólo en los puntos académicos y con la cadencia que se acuerde. Si no participa, la review sigue siendo una revisión del autor, sin atribuirle aprobación externa. Las observaciones recibidas se convierten en historias, defectos o solicitudes de cambio identificadas.

La retrospective revisa tamaño de historias, bloqueos, retrabajo, estimación, tiempo real disponible y demora de documentación. Produce **una mejora aplicable al siguiente sprint**, por ejemplo dividir antes las historias de integración. Al siguiente cierre se comprueba su efecto; no se recopilan acciones indefinidamente. Cambiar la estimación para hacer coincidir la previsión con lo ocurrido no constituye una mejora.

## 11. Riesgos iniciales

Probabilidad e impacto son valoraciones cualitativas iniciales: alta/media/baja. El autor es responsable de todos los riesgos; solicita contraste técnico o académico cuando corresponde. Los umbrales son políticas de alerta, no probabilidades medidas.

| ID | Riesgo y valoración P/I | Señal de activación | Mitigación y respuesta |
| --- | --- | --- | --- |
| R1 | Capacidad o calendario incompatibles — alta/alta | No se conocen horas disponibles o una fecha oficial deja el núcleo sin margen | MG-01 antes de comprometer carga; reserva explícita; retirar Could/Should y replanificar; no dar una fecha ficticia |
| R2 | Crecimiento del caso de estudio — alta/alta | Aparece tercer contexto, operación extra o ciclo de vida de Offers | Cinco operaciones como compromiso; registrar cambio antes de desarrollarlo; rechazar lo que no sostenga la tesis |
| R3 | Generador convertido en framework — alta/alta | Segundo perfil, DSL de negocio, plugins o excepciones por servicio | Un esquema y perfil; inventario previo; interfaces con código manual; reducir familias automatizadas antes que extender el lenguaje |
| R4 | Contribución reducida a scaffolding o ahorro no demostrado — media/alta | Sólo se muestran ficheros generados o porcentajes sin denominador | Protocolo previo, comparación equivalente, regeneración y coste total; resultados negativos admitidos y discusión explícita |
| R5 | Regeneración destructiva — media/alta | Diffs en código manual, colisiones o escritura parcial tras error | Propiedad de ficheros, manifiesto, validación previa y hashes; bloquear entrega ante pérdida; no añadir un motor de merge |
| R6 | Integración REST amplía semántica distribuida — media/alta | Se pide consistencia global, revalidar todas las ofertas o reintentos complejos | Contrato de revisión observada, tiempo de espera acotado y fallo sin escritura; explicar la limitación y mantener una sola llamada |
| R7 | Infraestructura o dependencias bloquean CI — media/alta | Falla MongoDB/runner o se propone cambiar el perfil congelado | Reproducir con Compose y versiones fijadas; distinguir fallo de entorno; si exige reparar infraestructura ajena, parar y reconstruir sólo una envolvente mínima mediante decisión explícita |
| R8 | Memoria o evidencia diferidas al final — alta/alta | Historia sin enlace a memoria o resultados sólo en logs temporales | Documentación en Done, DOC por incremento y paquete de evidencia en S5; frenar nuevas historias si la trazabilidad está incompleta |
| R9 | Sesgo por autor único y pruebas acopladas a plantillas — alta/media | Las mismas plantillas producen código y únicas aserciones; tiempos reconstruidos de memoria | Pruebas de negocio independientes, registros contemporáneos y límites de autorreproducción; TS-14 opcional, sin asumir disponibilidad externa |

Cada review actualiza exposición, acción y estado de riesgos. Un riesgo materializado se vincula al defecto o cambio que lo trata; no se elimina del registro para presentar una evolución sin incidencias.

## 12. Gestión de cambios de alcance

El backlog operativo puede refinarse; la baseline aceptada se conserva mediante Git. Cada ampliación, reducción de un Must, cambio de perfil o modificación del protocolo tras comenzar a medir se registra como `CR-nn`.

El registro contiene solicitante/origen, motivo, situación actual y propuesta, historias y contratos afectados, impacto en estimación/capacidad/milestones/evaluación/memoria, elemento que se retira o capacidad que lo financia, decisión y evidencia de aceptación. El autor decide ajustes operativos dentro de los límites; los que alteren objetivos o compromisos académicos se contrastan con el tutor según el procedimiento docente. No se afirma una autorización que no conste.

La secuencia es: registrar → analizar impacto → decidir aceptar/rechazar/posponer → actualizar backlog y previsión → implementar. No se empieza una ampliación porque quepa en el límite de ocho operaciones. Un cambio urgente que invalide el Sprint Goal exige registrar su replanteamiento; no se reescribe el Goal original como si siempre hubiese sido otro.

Un defecto es un incumplimiento de aceptación existente, no una vía para introducir funcionalidad. Primero se retiran Could, después Should; si los Must ya no caben, se reduce explícitamente el mecanismo o se ajusta el horizonte. Se protegen dos contextos, las cinco operaciones seleccionadas, regeneración segura, evaluación y memoria. Si tampoco caben, se declara comprometida la viabilidad y se acuerda una nueva baseline; no se reduce la calidad de las pruebas para conservar el plan aparente.

## 13. Seguimiento de desviaciones

En Planning se conserva una instantánea del Goal, historias/puntos seleccionados, capacidad disponible, milestone esperado y riesgos. En la review se registra lo terminado, incorporado, retirado, bloqueado y pendiente. Se miden por separado **alcance, capacidad, ejecución, calidad y documentación**.

| Indicador | Registro e interpretación | Umbral de intervención propuesto |
| --- | --- | --- |
| Goal y compromiso | Goal logrado/no logrado con evidencia; puntos Done frente a selección inicial, y añadidos aparte | Goal incumplido: replanificar inmediatamente; menos del 80 % inicial terminado en dos sprints: reducir carga y dividir historias |
| Capacidad | Horas disponibles previstas/reales y motivo de diferencia; no deducidas de puntos | Caída superior al 20 %: recalcular previsión del trabajo restante |
| Flujo | WIP, historias terminadas, antigüedad y tiempo entre In Progress y Done | Bloqueo durante dos jornadas previstas de trabajo o exceso de WIP: resolver, dividir o posponer antes de abrir más trabajo |
| Alcance | Must restantes y cambios CR; puntos añadidos/retirados visibles | Un Must nuevo: revisar financiación y efecto sobre milestone antes de aceptarlo |
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
| Preservar comportamiento y demostrar distribución | US-C01–03, TS-03, US-O01–02, DOC-02 | Servicios y contrato REST | Regresión e integración real, incluidos rechazos | Análisis y diseño del caso de estudio |
| Automatizar estructura con límites | TS-04–07, DOC-03 | Esquema, plantillas, manifiestos y código manual | Compilación, ArchUnit y hashes de regeneración | Diseño e implementación del generador |
| Evaluar coste, repetibilidad y consistencia | TS-02, TS-08–11, DOC-04 | Protocolo, datos e informes | Ejecuciones exactas y cálculos reproducibles | Evaluación, discusión y amenazas |
| Entregar un resultado verificable | TS-12, DOC-05 | Checkout y documentación de entrega | Reproducción y revisión final | Conclusiones y defensa |

El protocolo mínimo de TS-02 incluirá:

- **Casos:** Communities y Offers; mismos contratos y código manual de negocio para comparar la referencia y el resultado generado. No se mantienen dos productos alternativos; se conservan instantáneas identificadas para evaluación.
- **Compilación y comportamiento:** pruebas de aceptación independientes de las plantillas, suite completa y reglas ArchUnit. La conformidad sólo acredita las reglas comprobadas, no toda propiedad arquitectónica posible.
- **Regeneración:** salida inicial, tres ejecuciones idénticas por caso y un cambio estructural acotado por caso en una copia de evaluación; hashes esperados, código manual intacto y ensayo de colisión. Ese cambio no introduce una operación de producto. Las repeticiones comprueban determinismo, no constituyen una muestra para inferencia estadística.
- **Proporción de artefactos:** contar por familia ficheros fuente/configuración de autoría generada y manual; publicar numerador, denominador, exclusiones y clasificación. Excluir dependencias, binarios, `target`, logs y el propio generador del denominador de salida. Un porcentaje de generación no equivale a ahorro de esfuerzo ni se fija como meta arbitraria.
- **Esfuerzo:** registrar desde S1 tiempo efectivo de diseño de especificación, plantillas, implementación manual, ejecución, corrección y documentación por separado. No reconstruir horas de la baseline técnica. Comparar tareas estructurales equivalentes y reconocer aprendizaje/orden; si no existe medición comparable, declarar ahorro temporal no evaluable. Incluir el coste de construir y mantener el generador al discutir beneficio neto.
- **Reproducibilidad:** versiones, SHA de código/generador/especificaciones, entorno, comandos, informes y hashes de evidencia. La campaña puede refutar la expectativa de reducción; no se ajustan métricas después para asegurar una conclusión favorable.

Al cerrar cada sprint se actualizan las fichas y la memoria afectada. Al alcanzar M5 se congela el conjunto de datos utilizado en las conclusiones; una corrección posterior produce una versión nueva con motivo y efecto identificados. La procedencia de código, bibliografía y herramientas de asistencia se registra con transparencia conforme a las instrucciones docentes que se confirmen.

## 15. Condición de activación y recomendación

Esta propuesta estará lista para activar el desarrollo funcional cuando el autor acepte M1 y las historias seleccionadas cumplan Ready. MG-01 registra la dedicación y las fechas disponibles; ninguna ausencia se sustituye por una suposición presentada como dato. La aceptación y los cambios posteriores quedan en el historial de PRs.

Se recomienda mantener **un único enfoque Agile ligero inspirado en Scrum**, con sprints de dos semanas, un Goal por sprint, tablero común y WIP limitado. La cadencia permite comprobar regularmente que avanzan juntos implementación, evaluación y memoria, mientras que el control de flujo contiene el trabajo abierto de un único desarrollador.

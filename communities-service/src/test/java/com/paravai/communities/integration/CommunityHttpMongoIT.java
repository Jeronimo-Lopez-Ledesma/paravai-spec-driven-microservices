package com.paravai.communities.integration;

import com.paravai.communities.adapter.in.rest.CommunityResponse;
import com.paravai.communities.application.PolicyConflictException;
import com.paravai.communities.application.port.out.CommunityRepository;
import com.paravai.communities.domain.Community;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.paravai.communities.domain.ExchangeType.DONATION;
import static com.paravai.communities.domain.ExchangeType.EXCHANGE;
import static com.paravai.communities.domain.ExchangeType.LOAN;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityHttpMongoIT {
    private static final String DATABASE = "gate1_it_" + UUID.randomUUID().toString().replace("-", "");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    @LocalServerPort int port;
    @Autowired ReactiveMongoTemplate mongo;
    @Autowired CommunityRepository repository;
    private WebTestClient client;

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry properties) {
        String uri = System.getenv("MONGODB_TEST_URI");
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("The integration profile requires MONGODB_TEST_URI pointing to a local test MongoDB");
        }
        properties.add("spring.mongodb.uri", () -> uri);
        properties.add("spring.mongodb.database", () -> DATABASE);
        properties.add("paravai.demo.users", () -> "alice,bob");
        properties.add("server.address", () -> "127.0.0.1");
    }

    @BeforeAll
    void connectToRealHttpServerAndCheckIsolatedDatabase() {
        assertThat(mongo.getMongoDatabase().block(TIMEOUT).getName()).isEqualTo(DATABASE);
        mongo.executeCommand("{ ping: 1 }").block(TIMEOUT);
        client = WebTestClient.bindToServer().baseUrl("http://127.0.0.1:" + port).responseTimeout(TIMEOUT).build();
    }

    @AfterAll
    void removeOnlyThisTestsDatabase() {
        mongo.getMongoDatabase().flatMap(database -> {
            if (!DATABASE.equals(database.getName())) {
                return Mono.error(new IllegalStateException("Refusing to drop an unexpected database"));
            }
            return Mono.from(database.drop());
        }).block(TIMEOUT);
    }

    @Test
    void createGetAndUpdateRoundTripThroughHttpAndMongo() {
        CommunityResponse created = create();
        Document stored = mongo.findOne(Query.query(Criteria.where("_id").is(created.id().toString())),
                Document.class, "communities").block(TIMEOUT);
        assertThat(stored).isNotNull();
        assertThat(stored.getString("administratorId")).isEqualTo("alice");
        assertThat(((Number) stored.get("policyRevision")).longValue()).isEqualTo(1);
        client.get().uri("/v1/communities/{id}", created.id()).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.allowedExchangeTypes[0]").isEqualTo("DONATION");
        client.put().uri("/v1/communities/{id}/rules", created.id()).header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(updateBody("LOAN")).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.policyRevision").isEqualTo(2);
        Community persisted = repository.findById(created.id()).block(TIMEOUT);
        assertThat(persisted.allowedExchangeTypes()).containsExactly(LOAN);
        assertThat(persisted.name()).isEqualTo("Neighbours");
        assertThat(persisted.administratorId()).isEqualTo("alice");
        assertThat(persisted.policyRevision()).isEqualTo(2);
    }

    @Test
    void forbiddenStaleAndInvalidWritesLeavePersistedDataUnchanged() {
        CommunityResponse created = create();
        client.put().uri("/v1/communities/{id}/rules", created.id()).header("X-Demo-User", "bob")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(updateBody("LOAN")).exchange().expectStatus().isForbidden();
        client.put().uri("/v1/communities/{id}/rules", created.id()).header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue("{\"allowedExchangeTypes\":[],\"expectedPolicyRevision\":1}")
                .exchange().expectStatus().isBadRequest();
        client.put().uri("/v1/communities/{id}/rules", created.id()).header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue("{\"allowedExchangeTypes\":[\"LOAN\"],\"expectedPolicyRevision\":99}")
                .exchange().expectStatus().isEqualTo(409);
        Community unchanged = repository.findById(created.id()).block(TIMEOUT);
        assertThat(unchanged.policyRevision()).isEqualTo(1);
        assertThat(unchanged.allowedExchangeTypes()).containsExactly(DONATION);
    }

    @Test
    void unknownCommunityReturns404AndIsNotUpserted() {
        UUID missing = UUID.randomUUID();
        client.get().uri("/v1/communities/{id}", missing).exchange().expectStatus().isNotFound();
        client.put().uri("/v1/communities/{id}/rules", missing).header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(updateBody("LOAN")).exchange().expectStatus().isNotFound();
        assertThat(repository.findById(missing).block(TIMEOUT)).isNull();
    }

    @Test
    void competingHttpUpdatesYieldOneSuccessAndOneConflict() {
        CommunityResponse created = create();
        WebClient http = WebClient.create("http://127.0.0.1:" + port);
        List<Integer> statuses = Flux.merge(updateOverHttp(http, created.id(), "LOAN"),
                        updateOverHttp(http, created.id(), "EXCHANGE"))
                .collectList().block(TIMEOUT);
        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        Community persisted = repository.findById(created.id()).block(TIMEOUT);
        assertThat(persisted.policyRevision()).isEqualTo(2);
        assertThat(persisted.allowedExchangeTypes()).isIn(Set.of(LOAN), Set.of(EXCHANGE));
    }

    @Test
    void mongoCompareAndSetRejectsASecondWriteBasedOnTheSameSnapshot() {
        CommunityResponse created = create();
        Community snapshot = repository.findById(created.id()).block(TIMEOUT);
        List<Signal<Community>> results = Flux.merge(
                        repository.updateRules(snapshot.updateRules("alice", Set.of(LOAN)), 1).materialize(),
                        repository.updateRules(snapshot.updateRules("alice", Set.of(EXCHANGE)), 1).materialize())
                .collectList().block(TIMEOUT);
        assertThat(results.stream().filter(Signal::isOnNext)).hasSize(1);
        assertThat(results.stream().filter(Signal::isOnError).map(Signal::getThrowable))
                .singleElement().isInstanceOf(PolicyConflictException.class);
        assertThat(repository.findById(created.id()).block(TIMEOUT).policyRevision()).isEqualTo(2);
    }

    private CommunityResponse create() {
        return client.post().uri("/v1/communities").header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Neighbours\",\"allowedExchangeTypes\":[\"DONATION\"]}")
                .exchange().expectStatus().isCreated().expectBody(CommunityResponse.class).returnResult().getResponseBody();
    }

    private Mono<Integer> updateOverHttp(WebClient http, UUID id, String type) {
        return http.put().uri("/v1/communities/{id}/rules", id).header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(updateBody(type))
                .exchangeToMono(response -> response.releaseBody().thenReturn(response.statusCode().value()));
    }

    private String updateBody(String type) {
        return "{\"allowedExchangeTypes\":[\"" + type + "\"],\"expectedPolicyRevision\":1}";
    }
}

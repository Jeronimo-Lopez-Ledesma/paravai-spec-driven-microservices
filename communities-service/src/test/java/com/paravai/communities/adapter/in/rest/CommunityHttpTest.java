package com.paravai.communities.adapter.in.rest;

import com.paravai.communities.application.CommunityService;
import com.paravai.communities.application.CommunityStorageException;
import com.paravai.communities.domain.Community;
import com.paravai.communities.support.InMemoryCommunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(CommunityController.class)
@Import({DemoIdentityResolver.class, ApiExceptionHandler.class, CommunityHttpTest.Configuration.class})
class CommunityHttpTest {
    private static final String CREATE = """
            {"name":"Neighbours","allowedExchangeTypes":["DONATION"]}
            """;
    private static final String UPDATE = """
            {"allowedExchangeTypes":["LOAN"],"expectedPolicyRevision":1}
            """;
    @Autowired WebTestClient client;
    @Autowired TestRepository repository;

    @BeforeEach
    void resetRepository() {
        repository.clear();
        repository.unavailable = false;
    }

    @Test
    void createsReadsAndUpdatesWithTheAdministratorDerivedFromTheHeader() {
        CommunityResponse created = create();
        assertThat(created.administratorId()).isEqualTo("alice");
        assertThat(created.policyRevision()).isEqualTo(1);
        client.get().uri("/v1/communities/{id}", created.id()).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.name").isEqualTo("Neighbours");
        client.put().uri("/v1/communities/{id}/rules", created.id()).header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(UPDATE).exchange()
                .expectStatus().isOk().expectBody()
                .jsonPath("$.policyRevision").isEqualTo(2)
                .jsonPath("$.administratorId").isEqualTo("alice")
                .jsonPath("$.allowedExchangeTypes[0]").isEqualTo("LOAN");
    }

    @Test
    void missingOrAmbiguousIdentityIsUnauthorized() {
        client.post().uri("/v1/communities").contentType(MediaType.APPLICATION_JSON).bodyValue(CREATE).exchange()
                .expectStatus().isUnauthorized().expectHeader().exists("WWW-Authenticate")
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        client.post().uri("/v1/communities").header("X-Demo-User", "alice", "bob")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(CREATE).exchange().expectStatus().isUnauthorized();
        CommunityResponse created = create();
        client.put().uri("/v1/communities/{id}/rules", created.id())
                .contentType(MediaType.APPLICATION_JSON).bodyValue(UPDATE).exchange().expectStatus().isUnauthorized();
        assertThat(repository.size()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"mallory", "", "alice,bob"})
    void unconfiguredIdentityIsUnauthorized(String user) {
        client.post().uri("/v1/communities").header("X-Demo-User", user)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(CREATE).exchange().expectStatus().isUnauthorized();
        assertThat(repository.size()).isZero();
    }

    @Test
    void nonAdministratorIsForbiddenAndRulesRemainUnchanged() {
        CommunityResponse created = create();
        client.put().uri("/v1/communities/{id}/rules", created.id()).header("X-Demo-User", "bob")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(UPDATE).exchange()
                .expectStatus().isForbidden().expectBody().jsonPath("$.status").isEqualTo(403);
        client.get().uri("/v1/communities/{id}", created.id()).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.policyRevision").isEqualTo(1);
    }

    @Test
    void missingCommunityReturns404ForBothReadAndUpdate() {
        UUID missing = UUID.randomUUID();
        client.get().uri("/v1/communities/{id}", missing).exchange()
                .expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404);
        client.put().uri("/v1/communities/{id}/rules", missing).header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(UPDATE).exchange().expectStatus().isNotFound();
    }

    @Test
    void staleRevisionReturns409() {
        CommunityResponse created = create();
        for (int status : new int[]{200, 409}) {
            client.put().uri("/v1/communities/{id}/rules", created.id()).header("X-Demo-User", "alice")
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(UPDATE).exchange().expectStatus().isEqualTo(status);
        }
        client.get().uri("/v1/communities/{id}", created.id()).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.policyRevision").isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\"N\",\"allowedExchangeTypes\":[0]}",
            "{}", "null", "{", "{\"name\":\" \",\"allowedExchangeTypes\":[\"LOAN\"]}",
            "{\"name\":\"N\",\"allowedExchangeTypes\":[]}",
            "{\"name\":\"N\",\"allowedExchangeTypes\":[null]}",
            "{\"name\":\"N\",\"allowedExchangeTypes\":[\"UNKNOWN\"]}",
            "{\"name\":\"N\",\"allowedExchangeTypes\":[\"LOAN\"],\"administratorId\":\"bob\"}",
            "{\"name\":\"N\",\"allowedExchangeTypes\":[\"LOAN\"],\"policyRevision\":12}"
    })
    void invalidCreationIs400AndCannotInjectAdministratorOrRevision(String body) {
        client.post().uri("/v1/communities").header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(body).exchange()
                .expectStatus().isBadRequest().expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().jsonPath("$.status").isEqualTo(400);
        assertThat(repository.size()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"allowedExchangeTypes\":[\"LOAN\"],\"expectedPolicyRevision\":1.5}",
            "{\"allowedExchangeTypes\":[\"LOAN\"]}",
            "{\"allowedExchangeTypes\":[\"LOAN\"],\"expectedPolicyRevision\":0}",
            "{\"allowedExchangeTypes\":[],\"expectedPolicyRevision\":1}",
            "{\"allowedExchangeTypes\":[\"UNKNOWN\"],\"expectedPolicyRevision\":1}"
    })
    void invalidUpdateIs400AndDoesNotChangeTheCommunity(String body) {
        CommunityResponse created = create();
        client.put().uri("/v1/communities/{id}/rules", created.id()).header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(body).exchange().expectStatus().isBadRequest();
        client.get().uri("/v1/communities/{id}", created.id()).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.policyRevision").isEqualTo(1);
    }

    @Test
    void invalidIdUnsupportedMethodAndUnsupportedMediaTypeHaveClientErrorStatuses() {
        client.get().uri("/v1/communities/not-a-uuid").exchange().expectStatus().isBadRequest();
        client.delete().uri("/v1/communities/{id}", UUID.randomUUID()).exchange().expectStatus().isEqualTo(405);
        client.post().uri("/v1/communities").header("X-Demo-User", "alice")
                .contentType(MediaType.TEXT_PLAIN).bodyValue(CREATE).exchange().expectStatus().isEqualTo(415);
    }

    @Test
    void unavailableStorageReturns503WithoutLeakingInternalDetails() {
        repository.unavailable = true;
        client.get().uri("/v1/communities/{id}", UUID.randomUUID()).exchange()
                .expectStatus().isEqualTo(503).expectBody()
                .jsonPath("$.detail").isEqualTo("Community storage is temporarily unavailable")
                .jsonPath("$.stackTrace").doesNotExist();
    }

    private CommunityResponse create() {
        return client.post().uri("/v1/communities").header("X-Demo-User", "alice")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(CREATE).exchange()
                .expectStatus().isCreated().expectHeader().valueMatches("Location", "/v1/communities/[0-9a-f-]{36}")
                .expectBody(CommunityResponse.class).returnResult().getResponseBody();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Configuration {
        @Bean TestRepository repository() { return new TestRepository(); }
        @Bean CommunityService service(TestRepository repository) { return new CommunityService(repository); }
    }

    static class TestRepository extends InMemoryCommunityRepository {
        volatile boolean unavailable;

        @Override
        public Mono<Community> findById(UUID id) {
            return unavailable ? Mono.error(new CommunityStorageException(new IllegalStateException("internal endpoint")))
                    : super.findById(id);
        }
    }
}

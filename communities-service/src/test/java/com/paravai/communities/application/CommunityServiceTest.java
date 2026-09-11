package com.paravai.communities.application;

import com.paravai.communities.application.port.in.CreateCommunityUseCase;
import com.paravai.communities.application.port.in.UpdateCommunityRulesUseCase;
import com.paravai.communities.domain.Community;
import com.paravai.communities.domain.InvalidCommunityException;
import com.paravai.communities.domain.NotAdministratorException;
import com.paravai.communities.support.InMemoryCommunityRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static com.paravai.communities.domain.ExchangeType.DONATION;
import static com.paravai.communities.domain.ExchangeType.LOAN;
import static org.assertj.core.api.Assertions.assertThat;

class CommunityServiceTest {
    private final InMemoryCommunityRepository repository = new InMemoryCommunityRepository();
    private final CommunityService service = new CommunityService(repository);

    @Test
    void creationIsLazyAndCanBeReadThroughTheInputPort() {
        Mono<Community> creation = service.create(new CreateCommunityUseCase.Command("Neighbours", "alice", Set.of(DONATION)));
        assertThat(repository.size()).isZero();
        StepVerifier.create(creation.flatMap(created -> service.getById(created.id())))
                .assertNext(community -> {
                    assertThat(community.id()).isNotNull();
                    assertThat(community.administratorId()).isEqualTo("alice");
                    assertThat(community.policyRevision()).isEqualTo(1);
                }).verifyComplete();
        assertThat(repository.size()).isEqualTo(1);
    }

    @Test
    void invalidCreationSignalsAnErrorWithoutPersisting() {
        StepVerifier.create(service.create(new CreateCommunityUseCase.Command(" ", "alice", Set.of(DONATION))))
                .expectError(InvalidCommunityException.class).verify();
        assertThat(repository.size()).isZero();
    }

    @Test
    void missingCommunityIsNotFoundForReadAndUpdate() {
        UUID missing = UUID.randomUUID();
        StepVerifier.create(service.getById(missing)).expectError(CommunityNotFoundException.class).verify();
        StepVerifier.create(service.updateRules(new UpdateCommunityRulesUseCase.Command(missing, "alice", Set.of(LOAN), 1)))
                .expectError(CommunityNotFoundException.class).verify();
        assertThat(repository.size()).isZero();
    }

    @Test
    void administratorUpdatesRulesAndPersistedRevision() {
        Community community = seed();
        StepVerifier.create(service.updateRules(new UpdateCommunityRulesUseCase.Command(community.id(), "alice", Set.of(LOAN), 1)))
                .assertNext(updated -> assertThat(updated.policyRevision()).isEqualTo(2)).verifyComplete();
        StepVerifier.create(service.getById(community.id()))
                .assertNext(updated -> assertThat(updated.allowedExchangeTypes()).containsExactly(LOAN)).verifyComplete();
    }

    @Test
    void nonAdministratorCannotChangeStoredRules() {
        Community community = seed();
        StepVerifier.create(service.updateRules(new UpdateCommunityRulesUseCase.Command(community.id(), "bob", Set.of(LOAN), 1)))
                .expectError(NotAdministratorException.class).verify();
        assertUnchanged(community);
    }

    @Test
    void staleRevisionCannotOverwriteRules() {
        Community community = seed();
        StepVerifier.create(service.updateRules(new UpdateCommunityRulesUseCase.Command(community.id(), "alice", Set.of(LOAN), 2)))
                .expectError(PolicyConflictException.class).verify();
        assertUnchanged(community);
    }

    @Test
    void invalidRulesOrRevisionCannotBePersisted() {
        Community community = seed();
        StepVerifier.create(service.updateRules(new UpdateCommunityRulesUseCase.Command(community.id(), "alice", Set.of(), 1)))
                .expectError(InvalidCommunityException.class).verify();
        StepVerifier.create(service.updateRules(new UpdateCommunityRulesUseCase.Command(community.id(), "alice", Set.of(LOAN), 0)))
                .expectError(InvalidCommunityException.class).verify();
        assertUnchanged(community);
    }

    @Test
    void conflictAfterReadingIsPropagatedWithoutAnAutomaticRetry() {
        Community community = seed();
        InMemoryCommunityRepository racingRepository = new InMemoryCommunityRepository() {
            @Override
            public Mono<Community> findById(UUID id) { return Mono.just(community); }
            @Override
            public Mono<Community> updateRules(Community updated, long expectedRevision) {
                return Mono.error(new PolicyConflictException());
            }
        };
        StepVerifier.create(new CommunityService(racingRepository).updateRules(
                        new UpdateCommunityRulesUseCase.Command(community.id(), "alice", Set.of(LOAN), 1)))
                .expectError(PolicyConflictException.class).verify();
    }

    @Test
    void storageFailureIsNotConvertedIntoNotFound() {
        InMemoryCommunityRepository unavailableRepository = new InMemoryCommunityRepository() {
            @Override
            public Mono<Community> findById(UUID id) {
                return Mono.error(new CommunityStorageException(new IllegalStateException("test outage")));
            }
        };
        StepVerifier.create(new CommunityService(unavailableRepository).getById(UUID.randomUUID()))
                .expectError(CommunityStorageException.class).verify();
    }

    private Community seed() {
        return repository.insert(Community.create(UUID.randomUUID(), "Neighbours", "alice", Set.of(DONATION)))
                .block(Duration.ofSeconds(2));
    }

    private void assertUnchanged(Community original) {
        StepVerifier.create(service.getById(original.id())).expectNext(original).verifyComplete();
    }
}

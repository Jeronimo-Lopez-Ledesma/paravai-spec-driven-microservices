package com.paravai.communities.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.paravai.communities.domain.ExchangeType.DONATION;
import static com.paravai.communities.domain.ExchangeType.LOAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommunityTest {
    @Test
    void createsAnImmutableCommunityAtRevisionOne() {
        Set<ExchangeType> input = EnumSet.of(DONATION);
        Community community = Community.create(UUID.randomUUID(), "  Neighbours  ", "alice", input);
        input.add(LOAN);
        assertThat(community.name()).isEqualTo("Neighbours");
        assertThat(community.administratorId()).isEqualTo("alice");
        assertThat(community.policyRevision()).isEqualTo(1);
        assertThat(community.allowedExchangeTypes()).containsExactly(DONATION);
        assertThatThrownBy(() -> community.allowedExchangeTypes().add(LOAN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsMissingNames(String name) {
        assertThatThrownBy(() -> Community.create(UUID.randomUUID(), name, "alice", Set.of(DONATION)))
                .isInstanceOf(InvalidCommunityException.class);
    }

    @Test
    void enforcesNameLengthBoundary() {
        assertThat(Community.create(UUID.randomUUID(), "a".repeat(100), "alice", Set.of(DONATION)).name()).hasSize(100);
        assertThatThrownBy(() -> Community.create(UUID.randomUUID(), "a".repeat(101), "alice", Set.of(DONATION)))
                .isInstanceOf(InvalidCommunityException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "a,b", "alice smith"})
    void rejectsInvalidAdministratorIds(String administrator) {
        assertThatThrownBy(() -> Community.create(UUID.randomUUID(), "Neighbours", administrator, Set.of(DONATION)))
                .isInstanceOf(InvalidCommunityException.class);
    }

    @Test
    void rejectsMissingIdAndInvalidRevision() {
        assertThatThrownBy(() -> Community.create(null, "Neighbours", "alice", Set.of(DONATION)))
                .isInstanceOf(InvalidCommunityException.class);
        assertThatThrownBy(() -> new Community(UUID.randomUUID(), "Neighbours", "alice", Set.of(DONATION), 0))
                .isInstanceOf(InvalidCommunityException.class);
    }

    @Test
    void rejectsMissingEmptyOrNullExchangeTypes() {
        UUID id = UUID.randomUUID();
        Set<ExchangeType> containingNull = new HashSet<>();
        containingNull.add(null);
        assertThatThrownBy(() -> Community.create(id, "Neighbours", "alice", null))
                .isInstanceOf(InvalidCommunityException.class);
        assertThatThrownBy(() -> Community.create(id, "Neighbours", "alice", Set.of()))
                .isInstanceOf(InvalidCommunityException.class);
        assertThatThrownBy(() -> Community.create(id, "Neighbours", "alice", containingNull))
                .isInstanceOf(InvalidCommunityException.class);
    }

    @Test
    void onlyAdministratorCanReplaceRulesAndRevisionAdvances() {
        Community original = Community.create(UUID.randomUUID(), "Neighbours", "alice", Set.of(DONATION));
        Community updated = original.updateRules("alice", Set.of(LOAN));
        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.name()).isEqualTo(original.name());
        assertThat(updated.administratorId()).isEqualTo("alice");
        assertThat(updated.allowedExchangeTypes()).containsExactly(LOAN);
        assertThat(updated.policyRevision()).isEqualTo(2);
        assertThat(original.policyRevision()).isEqualTo(1);
        assertThat(original.allowedExchangeTypes()).containsExactly(DONATION);
        assertThatThrownBy(() -> original.updateRules("bob", Set.of(LOAN)))
                .isInstanceOf(NotAdministratorException.class);
    }

    @Test
    void validatesReplacementRulesAndPreventsRevisionOverflow() {
        Community community = Community.create(UUID.randomUUID(), "Neighbours", "alice", Set.of(DONATION));
        assertThatThrownBy(() -> community.updateRules("alice", Set.of()))
                .isInstanceOf(InvalidCommunityException.class);
        Community last = new Community(community.id(), community.name(), "alice", Set.of(DONATION), Long.MAX_VALUE);
        assertThatThrownBy(() -> last.updateRules("alice", Set.of(LOAN)))
                .isInstanceOf(InvalidCommunityException.class);
    }
}

package com.paravai.communities.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public record Community(UUID id, String name, String administratorId,
                        Set<ExchangeType> allowedExchangeTypes, long policyRevision) {
    public Community {
        if (id == null) {
            throw new InvalidCommunityException("Community id is required");
        }
        if (name == null || name.isBlank() || name.strip().length() > 100) {
            throw new InvalidCommunityException("Name must contain between 1 and 100 characters");
        }
        name = name.strip();
        if (administratorId == null || !administratorId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new InvalidCommunityException("Administrator id must contain 1 to 64 letters, digits, underscores or hyphens");
        }
        if (allowedExchangeTypes == null || allowedExchangeTypes.isEmpty()
                || allowedExchangeTypes.stream().anyMatch(type -> type == null)) {
            throw new InvalidCommunityException("At least one valid exchange type is required");
        }
        allowedExchangeTypes = Collections.unmodifiableSet(EnumSet.copyOf(allowedExchangeTypes));
        if (policyRevision < 1) {
            throw new InvalidCommunityException("Policy revision must be positive");
        }
    }

    public static Community create(UUID id, String name, String administratorId,
                                   Set<ExchangeType> allowedExchangeTypes) {
        return new Community(id, name, administratorId, allowedExchangeTypes, 1);
    }

    public Community updateRules(String actorId, Set<ExchangeType> exchangeTypes) {
        if (!administratorId.equals(actorId)) {
            throw new NotAdministratorException();
        }
        if (policyRevision == Long.MAX_VALUE) {
            throw new InvalidCommunityException("Policy revision limit reached");
        }
        return new Community(id, name, administratorId, exchangeTypes, policyRevision + 1);
    }
}

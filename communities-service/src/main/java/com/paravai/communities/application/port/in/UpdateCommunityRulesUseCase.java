package com.paravai.communities.application.port.in;

import com.paravai.communities.domain.Community;
import com.paravai.communities.domain.ExchangeType;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

public interface UpdateCommunityRulesUseCase {
    Mono<Community> updateRules(Command command);

    record Command(UUID id, String actorId, Set<ExchangeType> allowedExchangeTypes,
                   long expectedPolicyRevision) { }
}

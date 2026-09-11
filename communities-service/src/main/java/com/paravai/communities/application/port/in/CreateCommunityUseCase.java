package com.paravai.communities.application.port.in;

import com.paravai.communities.domain.Community;
import com.paravai.communities.domain.ExchangeType;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface CreateCommunityUseCase {
    Mono<Community> create(Command command);

    record Command(String name, String actorId, Set<ExchangeType> allowedExchangeTypes) { }
}

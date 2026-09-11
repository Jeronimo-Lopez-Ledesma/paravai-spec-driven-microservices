package com.paravai.communities.application.port.in;

import com.paravai.communities.domain.Community;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetCommunityUseCase {
    Mono<Community> getById(UUID id);
}

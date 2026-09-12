package com.paravai.communities.application.port.out;

import com.paravai.communities.domain.Community;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CommunityRepository {
    Mono<Community> insert(Community community);

    Mono<Community> findById(UUID id);

    /** Atomically updates rules only if the stored revision still matches; never inserts. */
    Mono<Community> updateRules(Community community, long expectedPolicyRevision);
}

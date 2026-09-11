package com.paravai.communities.support;

import com.paravai.communities.application.PolicyConflictException;
import com.paravai.communities.application.port.out.CommunityRepository;
import com.paravai.communities.domain.Community;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Test double; the integration suite uses real MongoDB. */
public class InMemoryCommunityRepository implements CommunityRepository {
    private final ConcurrentHashMap<UUID, Community> communities = new ConcurrentHashMap<>();

    @Override
    public Mono<Community> insert(Community community) {
        return Mono.fromSupplier(() -> {
            if (communities.putIfAbsent(community.id(), community) != null) {
                throw new IllegalStateException("Duplicate test community");
            }
            return community;
        });
    }

    @Override
    public Mono<Community> findById(UUID id) {
        return Mono.fromSupplier(() -> communities.get(id));
    }

    @Override
    public Mono<Community> updateRules(Community community, long expectedPolicyRevision) {
        return Mono.fromSupplier(() -> communities.compute(community.id(), (id, current) -> {
            if (current == null || current.policyRevision() != expectedPolicyRevision) {
                throw new PolicyConflictException();
            }
            return community;
        }));
    }

    public int size() { return communities.size(); }
    public void clear() { communities.clear(); }
}

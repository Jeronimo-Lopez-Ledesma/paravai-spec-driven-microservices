package com.paravai.communities.application;

import com.paravai.communities.application.port.in.CreateCommunityUseCase;
import com.paravai.communities.application.port.in.GetCommunityUseCase;
import com.paravai.communities.application.port.in.UpdateCommunityRulesUseCase;
import com.paravai.communities.application.port.out.CommunityRepository;
import com.paravai.communities.domain.Community;
import com.paravai.communities.domain.InvalidCommunityException;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class CommunityService implements CreateCommunityUseCase, GetCommunityUseCase,
        UpdateCommunityRulesUseCase {
    private final CommunityRepository repository;

    public CommunityService(CommunityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Community> create(CreateCommunityUseCase.Command command) {
        return Mono.defer(() -> repository.insert(Community.create(UUID.randomUUID(), command.name(),
                command.actorId(), command.allowedExchangeTypes())));
    }

    @Override
    public Mono<Community> getById(UUID id) {
        return Mono.defer(() -> repository.findById(id))
                .switchIfEmpty(Mono.error(() -> new CommunityNotFoundException(id)));
    }

    @Override
    public Mono<Community> updateRules(UpdateCommunityRulesUseCase.Command command) {
        return Mono.defer(() -> {
            if (command.expectedPolicyRevision() < 1) {
                return Mono.error(new InvalidCommunityException("Expected policy revision must be positive"));
            }
            return getById(command.id()).flatMap(current -> {
                Community updated = current.updateRules(command.actorId(), command.allowedExchangeTypes());
                if (current.policyRevision() != command.expectedPolicyRevision()) {
                    return Mono.error(new PolicyConflictException());
                }
                return repository.updateRules(updated, command.expectedPolicyRevision());
            });
        });
    }
}

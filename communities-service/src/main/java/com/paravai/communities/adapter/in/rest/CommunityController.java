package com.paravai.communities.adapter.in.rest;

import com.paravai.communities.application.port.in.CreateCommunityUseCase;
import com.paravai.communities.application.port.in.GetCommunityUseCase;
import com.paravai.communities.application.port.in.UpdateCommunityRulesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/communities")
public final class CommunityController {
    private final CreateCommunityUseCase createCommunity;
    private final GetCommunityUseCase getCommunity;
    private final UpdateCommunityRulesUseCase updateRules;
    private final DemoIdentityResolver identity;

    public CommunityController(CreateCommunityUseCase createCommunity, GetCommunityUseCase getCommunity,
                               UpdateCommunityRulesUseCase updateRules, DemoIdentityResolver identity) {
        this.createCommunity = createCommunity;
        this.getCommunity = getCommunity;
        this.updateRules = updateRules;
        this.identity = identity;
    }

    @PostMapping
    public Mono<ResponseEntity<CommunityResponse>> create(@Valid @RequestBody CreateCommunityRequest request,
                                                          @RequestHeader HttpHeaders headers) {
        String actor = identity.resolve(headers);
        return createCommunity.create(new CreateCommunityUseCase.Command(request.name(), actor,
                        request.allowedExchangeTypes()))
                .map(community -> ResponseEntity.created(URI.create("/v1/communities/" + community.id()))
                        .body(CommunityResponse.from(community)));
    }

    @GetMapping("/{id}")
    public Mono<CommunityResponse> get(@PathVariable UUID id) {
        return getCommunity.getById(id).map(CommunityResponse::from);
    }

    @PutMapping("/{id}/rules")
    public Mono<CommunityResponse> update(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateCommunityRulesRequest request,
                                         @RequestHeader HttpHeaders headers) {
        String actor = identity.resolve(headers);
        return updateRules.updateRules(new UpdateCommunityRulesUseCase.Command(id, actor,
                        request.allowedExchangeTypes(), request.expectedPolicyRevision()))
                .map(CommunityResponse::from);
    }
}

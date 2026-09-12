package com.paravai.communities.adapter.in.rest;

import com.paravai.communities.domain.Community;
import com.paravai.communities.domain.ExchangeType;

import java.util.Set;
import java.util.UUID;

public record CommunityResponse(UUID id, String name, String administratorId,
                                Set<ExchangeType> allowedExchangeTypes, long policyRevision) {
    public static CommunityResponse from(Community community) {
        return new CommunityResponse(community.id(), community.name(), community.administratorId(),
                community.allowedExchangeTypes(), community.policyRevision());
    }
}

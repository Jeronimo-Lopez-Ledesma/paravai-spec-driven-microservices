package com.paravai.communities.adapter.out.mongo;

import com.paravai.communities.domain.Community;
import com.paravai.communities.domain.ExchangeType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;
import java.util.UUID;

@Document("communities")
record CommunityDocument(@Id String id, String name, String administratorId,
                         Set<ExchangeType> allowedExchangeTypes, long policyRevision) {
    static CommunityDocument from(Community community) {
        return new CommunityDocument(community.id().toString(), community.name(), community.administratorId(),
                community.allowedExchangeTypes(), community.policyRevision());
    }

    Community toDomain() {
        return new Community(UUID.fromString(id), name, administratorId, allowedExchangeTypes, policyRevision);
    }
}

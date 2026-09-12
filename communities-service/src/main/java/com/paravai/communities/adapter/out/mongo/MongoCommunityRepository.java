package com.paravai.communities.adapter.out.mongo;

import com.paravai.communities.application.CommunityStorageException;
import com.paravai.communities.application.PolicyConflictException;
import com.paravai.communities.application.port.out.CommunityRepository;
import com.paravai.communities.domain.Community;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class MongoCommunityRepository implements CommunityRepository {
    private final ReactiveMongoTemplate template;

    public MongoCommunityRepository(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<Community> insert(Community community) {
        return template.insert(CommunityDocument.from(community))
                .map(CommunityDocument::toDomain)
                .onErrorMap(DataAccessException.class, CommunityStorageException::new);
    }

    @Override
    public Mono<Community> findById(UUID id) {
        return template.findById(id.toString(), CommunityDocument.class)
                .map(CommunityDocument::toDomain)
                .onErrorMap(DataAccessException.class, CommunityStorageException::new);
    }

    @Override
    public Mono<Community> updateRules(Community community, long expectedPolicyRevision) {
        Query query = Query.query(Criteria.where("_id").is(community.id().toString())
                .and("policyRevision").is(expectedPolicyRevision));
        Update update = new Update().set("allowedExchangeTypes", community.allowedExchangeTypes())
                .set("policyRevision", community.policyRevision());
        return template.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true),
                        CommunityDocument.class)
                .map(CommunityDocument::toDomain)
                .switchIfEmpty(Mono.error(PolicyConflictException::new))
                .onErrorMap(DataAccessException.class, CommunityStorageException::new);
    }
}

package com.paravai.communities.application;

import java.util.UUID;

public final class CommunityNotFoundException extends RuntimeException {
    public CommunityNotFoundException(UUID id) {
        super("Community not found: " + id);
    }
}

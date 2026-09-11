package com.paravai.communities.application;

public final class CommunityStorageException extends RuntimeException {
    public CommunityStorageException(Throwable cause) {
        super("Community storage is temporarily unavailable", cause);
    }
}

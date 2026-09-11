package com.paravai.communities.application;

public final class PolicyConflictException extends RuntimeException {
    public PolicyConflictException() {
        super("Policy revision has changed; read the community and retry with the current revision");
    }
}

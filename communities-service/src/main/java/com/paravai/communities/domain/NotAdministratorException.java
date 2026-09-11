package com.paravai.communities.domain;

public final class NotAdministratorException extends RuntimeException {
    public NotAdministratorException() {
        super("Only the community administrator can update its rules");
    }
}

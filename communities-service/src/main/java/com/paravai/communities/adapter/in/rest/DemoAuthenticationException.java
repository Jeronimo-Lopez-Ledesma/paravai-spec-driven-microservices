package com.paravai.communities.adapter.in.rest;

public final class DemoAuthenticationException extends RuntimeException {
    public DemoAuthenticationException() {
        super("Supply exactly one X-Demo-User header identifying a configured demo user");
    }
}

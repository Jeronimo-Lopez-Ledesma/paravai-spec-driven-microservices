package com.paravai.communities.adapter.in.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public final class DemoIdentityResolver {
    private final Set<String> users;

    public DemoIdentityResolver(@Value("${paravai.demo.users}") String configuredUsers) {
        users = Arrays.stream(configuredUsers.split(",", -1))
                .map(String::strip).collect(Collectors.toUnmodifiableSet());
        if (users.isEmpty() || users.stream().anyMatch(user -> !user.matches("[A-Za-z0-9_-]{1,64}"))) {
            throw new IllegalArgumentException("Configure non-empty demo user ids of at most 64 letters, digits, underscores or hyphens");
        }
    }

    public String resolve(HttpHeaders headers) {
        List<String> values = headers.get("X-Demo-User");
        if (values == null || values.size() != 1 || !users.contains(values.getFirst())) {
            throw new DemoAuthenticationException();
        }
        return values.getFirst();
    }
}

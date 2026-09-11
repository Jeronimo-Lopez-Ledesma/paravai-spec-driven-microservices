package com.paravai.communities.adapter.in.rest;

import com.paravai.communities.domain.ExchangeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateCommunityRequest(@NotBlank @Size(max = 100) String name,
                                     @NotEmpty Set<@NotNull ExchangeType> allowedExchangeTypes) { }

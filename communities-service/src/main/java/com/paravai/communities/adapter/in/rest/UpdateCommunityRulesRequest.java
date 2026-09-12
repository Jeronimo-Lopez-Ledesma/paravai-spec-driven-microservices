package com.paravai.communities.adapter.in.rest;

import com.paravai.communities.domain.ExchangeType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record UpdateCommunityRulesRequest(@NotEmpty Set<@NotNull ExchangeType> allowedExchangeTypes,
                                          @NotNull @Positive Long expectedPolicyRevision) { }

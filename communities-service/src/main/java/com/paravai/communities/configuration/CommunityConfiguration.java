package com.paravai.communities.configuration;

import com.paravai.communities.application.CommunityService;
import com.paravai.communities.application.port.out.CommunityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CommunityConfiguration {
    @Bean
    CommunityService communityService(CommunityRepository repository) {
        return new CommunityService(repository);
    }
}

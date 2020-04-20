package fr.auchan.nexus3.gitlabauth.plugin.config;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

@Singleton
@Named
public class GitlabAuthConfiguration {
    private static final String CONFIG_FILE = "gitlabauth.properties";

    private static final Duration DEFAULT_PRINCIPAL_CACHE_TTL = Duration.ofMinutes(1);

    private static final String DEFAULT_GITLAB_URL = "https://gitlab.com";

    private static final String GITLAB_API_URL_KEY = "gitlab.api.url";

    private static final String NEXUS_DEFAULT_ROLES_KEY = "nexus.defaultRoles";

    private static final String NEXUS_DEFAULT_ROLES_VALUE = "maven-deploy";

    private static final String NEXUS_MATCH_ROLES_KEY = "nexus.matchRoles";

    private static final String NEXUS_MATCH_ROLES_VALUE = "^gitlab-[\\S]*";

    private static final String GITLAB_PRINCIPAL_CACHE_TTL_KEY = "gitlab.principal.cache.ttl";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabAuthConfiguration.class);

    private Properties configuration;

    @PostConstruct
    public void init() {
        configuration = new Properties();

        try {
            configuration.load(Files.newInputStream(Paths.get(".", "etc", CONFIG_FILE)));
        } catch (IOException e) {
            LOGGER.warn("Error reading gitlab oauth properties, falling back to default configuration", e);
        }
    }

    public String getGitlabApiUrl() {
        return configuration.getProperty(GITLAB_API_URL_KEY, DEFAULT_GITLAB_URL);
    }

    public Set<String> getDefaultRoles() {
        String defaultRoles = configuration.getProperty(NEXUS_DEFAULT_ROLES_KEY, NEXUS_DEFAULT_ROLES_VALUE);
        String[] defaultRolesList = defaultRoles.split(",");
        Set<String> defaultRolesSet = new HashSet<>(Arrays.asList(defaultRolesList));
        return defaultRolesSet;
    }

    public Set<String> getMatchRoles() {
        String matchRoles = configuration.getProperty(NEXUS_MATCH_ROLES_KEY, NEXUS_MATCH_ROLES_VALUE);
        return new HashSet<>(Collections.singletonList(matchRoles));
    }

    public Duration getPrincipalCacheTtl() {
        return Duration.parse(configuration.getProperty(GITLAB_PRINCIPAL_CACHE_TTL_KEY, DEFAULT_PRINCIPAL_CACHE_TTL.toString()));
    }

}
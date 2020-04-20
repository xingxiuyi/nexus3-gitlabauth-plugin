package fr.auchan.nexus3.gitlabauth.plugin.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.auchan.nexus3.gitlabauth.plugin.GitlabAuthenticationException;
import fr.auchan.nexus3.gitlabauth.plugin.GitlabPrincipal;
import fr.auchan.nexus3.gitlabauth.plugin.config.GitlabAuthConfiguration;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
@Named("GitlabApiClient")
public class GitlabApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabApiClient.class);

    private GitlabAPI client;
    private GitlabAuthConfiguration configuration;
    // Cache token lookups to reduce the load on GitLab's User API to prevent hitting the rate limit.
    private Cache<String, GitlabPrincipal> tokenToPrincipalCache;

    public GitlabApiClient() {
        // no args constructor is needed
    }

    public GitlabApiClient(GitlabAPI client, GitlabAuthConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
        initPrincipalCache();
    }

    @Inject
    public GitlabApiClient(GitlabAuthConfiguration configuration) {
        configuration.init();
        this.configuration = configuration;
    }

    @PostConstruct
    public void init() {
        initPrincipalCache();
    }

    private void initPrincipalCache() {
        tokenToPrincipalCache = CacheBuilder.newBuilder()
                .expireAfterWrite(configuration.getPrincipalCacheTtl().toMillis(), TimeUnit.MILLISECONDS).build();
    }

    public GitlabPrincipal authz(String login, char[] token) throws GitlabAuthenticationException {
        // Combine the login and the token as the cache key since they are both used to generate the principal. If
        // either changes we should obtain a new
        // principal.
//        String cacheKey = login + "|" + new String(token);
//        GitlabPrincipal cached = tokenToPrincipalCache.getIfPresent(cacheKey);
//        if (cached != null) {
//            LOGGER.info("Using cached principal for login: {}", login);
//            return cached;
//        } else {
//            GitlabPrincipal principal = doAuthz(login, token);
//            tokenToPrincipalCache.put(cacheKey, principal);
//            return principal;
//
        GitlabPrincipal principal = doAuthz(login, token);
        return principal;
    }

    private GitlabPrincipal doAuthz(String loginName, char[] token) throws GitlabAuthenticationException {
        GitlabUser gitlabUser;
        try {
            GitlabAPI gitlabAPI = GitlabAPI.connect(configuration.getGitlabApiUrl(), String.valueOf(token));
            gitlabUser = gitlabAPI.getUser();
        } catch (Exception e) {
            LOGGER.error(e.toString());
            throw new GitlabAuthenticationException(e);
        }

        if (gitlabUser == null || !loginName.equals(gitlabUser.getUsername())) {
            throw new GitlabAuthenticationException("Given username not found or does not match GitLab Username!");
        }

        GitlabPrincipal principal = new GitlabPrincipal();

        principal.setUsername(gitlabUser.getUsername());

        principal.setDefaultRoles(configuration.getDefaultRoles());

        principal.setMatchRoles(configuration.getMatchRoles());

        return principal;
    }

}
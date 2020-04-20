package fr.auchan.nexus3.gitlabauth.plugin;

import fr.auchan.nexus3.gitlabauth.plugin.api.GitlabApiClient;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.user.User;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
@Named
@Description("Gitlab Authentication Realm")
public class GitlabAuthenticatingRealm extends AuthorizingRealm {
    private GitlabApiClient gitlabClient;
    private SecuritySystem securitySystem;

    public static final String NAME = GitlabAuthenticatingRealm.class.getName();
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabAuthenticatingRealm.class);

    @Inject
    public GitlabAuthenticatingRealm(final GitlabApiClient gitlabClient, SecuritySystem securitySystem) {
        this.gitlabClient = gitlabClient;
        this.securitySystem = securitySystem;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void onInit() {
        super.onInit();
        LOGGER.info("Keycloak Realm initialized...");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        GitlabPrincipal principal = (GitlabPrincipal) principals.getPrimaryPrincipal();

        Set<String> roles = new HashSet<>(principal.getDefaultRoles());
        roles.addAll(this.findUserMatchRoles(principal));

        LOGGER.info("doGetAuthorizationInfo for user {} with roles {}", principal.getUsername(), roles.toString());
        return new SimpleAuthorizationInfo(roles);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        LOGGER.info("doGetAuthenticationInfo [begin]");

        if (!(token instanceof UsernamePasswordToken)) {
            throw new UnsupportedTokenException(String.format("Token of type %s  is not supported. A %s is required.",
                    token.getClass().getName(), UsernamePasswordToken.class.getName()));
        }

        UsernamePasswordToken t = (UsernamePasswordToken) token;
        LOGGER.info("doGetAuthenticationInfo for {}", ((UsernamePasswordToken) token).getUsername());

        GitlabPrincipal authenticatedPrincipal;
        try {
            authenticatedPrincipal = gitlabClient.authz(t.getUsername(), t.getPassword());
            LOGGER.info("Successfully authenticated {}", t.getUsername());
        } catch (GitlabAuthenticationException e) {
            LOGGER.warn("Failed authentication", e);
            return null;
        }

        if (authenticatedPrincipal == null) {
            LOGGER.warn("Failed authentication for {}", t.getUsername());
        }

        LOGGER.info("doGetAuthenticationInfo [end]");

        return createSimpleAuthInfo(authenticatedPrincipal, t);
    }

    @Nonnull
    private Set<String> findUserMatchRoles(GitlabPrincipal principal) {
        Set<String> results = new HashSet<>();
        Set<String> mrs = principal.getMatchRoles();
        if (mrs == null || mrs.isEmpty()) return results;
        if (this.securitySystem == null) return results;

        try {
            User u = this.securitySystem.getUser(principal.getUsername());
            if (u == null || u.getRoles() == null) return results;

            mrs.forEach(mr -> {
                if (mr == null || mr.trim().equals("")) return;
                Pattern mp = Pattern.compile(mr);
                u.getRoles().forEach(r -> {
                    if (r == null) return;
                    String rid = r.getRoleId();
                    if (rid == null || rid.trim().equals("")) return;
                    if (mp.matcher(rid).matches()) results.add(rid);
                });
            });
        } catch (Throwable e) {
            LOGGER.error("user not found", e);
        }
        return results;
    }

    /**
     * Creates the simple auth info.
     *
     * @param token the token
     * @return the simple authentication info
     */
    private SimpleAuthenticationInfo createSimpleAuthInfo(GitlabPrincipal principal, UsernamePasswordToken token) {
        return new SimpleAuthenticationInfo(principal, token.getCredentials(), NAME);
    }

    public static void main(String[] args) {
        System.out.println(Pattern.matches("^hreeinfo", "hreeinfo-aabb-cc"));
    }
}
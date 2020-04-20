package fr.auchan.nexus3.gitlabauth.plugin;

import java.io.Serializable;
import java.util.Set;

public class GitlabPrincipal implements Serializable {
    private String username;
    private Set<String> defaultRoles;
    private Set<String> matchRoles;

    public Set<String> getDefaultRoles() {
        return defaultRoles;
    }

    public void setDefaultRoles(Set<String> defaultRoles) {
        this.defaultRoles = defaultRoles;
    }

    public Set<String> getMatchRoles() {
        return matchRoles;
    }

    public void setMatchRoles(Set<String> matchRoles) {
        this.matchRoles = matchRoles;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }
}
package fr.auchan.nexus3.gitlabauth.plugin;

import java.io.Serializable;
import java.util.Set;

public class GitlabPrincipal implements Serializable {
    private String username;
    private Set<String> defaultRoles;

    public Set<String> getDefaultRoles() {
        return defaultRoles;
    }

    public void setDefaultRoles(Set<String> defaultRoles) {
        this.defaultRoles = defaultRoles;
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
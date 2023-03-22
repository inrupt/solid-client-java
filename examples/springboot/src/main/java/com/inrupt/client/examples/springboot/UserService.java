package com.inrupt.client.examples.springboot;

import com.inrupt.client.examples.springboot.model.WebIdOwner;
import com.inrupt.client.solid.SolidSyncClient;

import java.net.URI;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final SolidSyncClient client = SolidSyncClient.getClient();

    public WebIdOwner getCurrentUser() {

        if (SecurityContextHolder.getContext() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
            if (principal instanceof OidcUser) {
                final OidcUser user = (OidcUser) principal;
                final String webidUrl = user.getClaim("webid");
                try (final WebIdOwner profile = client.read(URI.create(webidUrl), WebIdOwner.class)) {
                    profile.setToken(user.getIdToken().getTokenValue());
                    return profile;
                }
            }
        }
        return null;
    }

}

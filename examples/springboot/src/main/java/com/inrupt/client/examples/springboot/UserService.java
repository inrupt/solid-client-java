package com.inrupt.client.examples.springboot;

import com.inrupt.client.examples.springboot.model.WebIdOwner;
import com.inrupt.client.solid.SolidSyncClient;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private static SecurityContextHolderFacade securityContextHolder;

    private static final SolidSyncClient client = SolidSyncClient.getClient();

    public static WebIdOwner getCurrentUser() {

        if (securityContextHolder != null) {
            Object principal = securityContextHolder.getContext().getAuthentication().getPrincipal();
        
            if (principal instanceof OidcUser) {
                final OidcUser user = (OidcUser) principal;
                final String webidUrl = user.getClaim("webid");
                try (final WebIdOwner profile = client.read(URI.create(webidUrl), WebIdOwner.class)) {
                    return profile;
                }
            }
        }
        return null;
    }

}

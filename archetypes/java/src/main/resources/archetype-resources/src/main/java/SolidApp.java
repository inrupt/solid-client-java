/**
 * A Solid application.
 */
package ${package};

import com.inrupt.client.Resource;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidContainer;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SolidApp {

    protected Session getSession() {
        return OpenIdSession.ofClientCredentials(URI.create("https://ISSUER-URL"),
                "CLIENT_ID VALUE", "CLIENT_SECRET VALUE", "client_secret_basic");
    }

    public List<URI> readStorage() {
        final var session = getSession();
        final var client = SolidSyncClient.getClient().session(session);
        final var resources = new ArrayList<URI>();

        session.getPrincipal().ifPresent(webid -> {
            try (final var profile = client.read(webid, WebIdProfile.class)) {
                profile.getStorages().stream().findFirst().ifPresent(storage -> {
                    try (final var container = client.read(storage, SolidContainer.class)) {
                        container.getResources().forEach(resource -> resources.add(resource.getIdentifier()));
                    }
                });
            }
        });

        return resources;
    }
}

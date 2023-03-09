package com.inrupt.client.uma;

import com.inrupt.client.Client;
import com.inrupt.client.ClientProvider;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import java.net.URI;
import java.util.concurrent.CompletionStage;

public class ClientSample {

    public CompletionStage<String> useTheLowLevelClient() {
        Client client = ClientProvider.getClient();

        URI uri = URI.create("https://storage.example/resource.ttl");
        Request req = Request.newBuilder(uri)
            .header("Accept", "text/turtle")
            .GET()
            .build();

        return client.send(req, Response.BodyHandlers.ofString()).thenApply
        (res -> {
                if (res.statusCode() == 200) {
                    return res.body();
                }
                return "Error accessing resource: " + res.statusCode();
            });
    }
}

/*
 * Copyright 2023 Inrupt Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
/**
 *
 * <h2>Support for Solid specific concepts for the Inrupt Java Client Libraries.</h2>
 *
 * <h3>Solid Client</h3>
 *
 * <p>This Solid domain-specific module contains two dedicated Solid clients one can make use of:
 * {@link SolidClient} which works asynchronously and {@link SolidSyncClient} which works synchronously.
 * 
 * <p>One can instantiate a Solid client in different ways, depending on the use case:
 * 
 * <p>A simple direct way is with the following line:
 * <pre>{@code
    SolidClient client = SolidClient.getClient();
    }
 * </pre>
 *
 * <p>This will make use of the client that is currently loaded on the classpath.
 * (If you have the core module (inrupt-client-core) loaded, this will make use of the DefaultClient).
 *
 * <p>The above line is equivalent to:
 * <pre>{@code
    Client classPathClient = ClientProvider.getClient();
    SolidClient client = SolidClient.of(classPathClient);
    }
 * </pre>
 * 
 * <p>The Solid client can be used to perform CRUD operations on Solid resources.
 * 
 * <p>In this example, the client reads a Solid resource as a {@code Playlist} object.
 * <pre>{@code
    var playlist = client.read(uri, Playlist.class);
    playlist.thenAccept(p -> {
        displayTitle(p.getTitle());
        displaySongs(p.getSongs());
    }).toCompletableFuture().join(); }
 * </pre>
 *
 * <p>One may also create new resources.
 * <pre>{@code
    var playlist = new Playlist(uri);
    playlist.setTitle("Jazz Collection");
    playlist.addSong(song1);
    playlist.addSong(song2);

    client.create(playlist).toCompletableFuture().join(); }
 * </pre>
 *
 * <p>Or update existing resources.
 * <pre>{@code
    var playlist = client.read(uri, Playlist.class);

    playlist.thenCompose(p -> {
        p.setTitle("Bossa Nova");
        p.removeSong(song1);
        p.addSong(song3);
        return client.update(p);
    }).toCompletableFuture().join(); }
 * </pre>
 *
 * <p>Or delete resources.
* <pre>{@code
    client.delete(uri).toCompletableFuture().join(); }
 * </pre>
 * 
 * <h3>Solid Resource and Solid Container</h3>
 *
 * <p>This module also contains a BodyHandler which consumes the response body
 *  and converts it into a {@link SolidResource} or a {@link SolidContainer} Java object.
 *
 * <p>The following example reads a Solid Container and presents it as a {@link SolidContainer} Java object:
 *
 * <pre>{@code
        Request request = Request.newBuilder()
            .uri(URI.create("https://solid.example/storage/"))
            .header("Accept", "text/turtle")
            .GET()
            .build();

        Response<SolidContainer> response = client.send(
            request,
            SolidResourceHandlers.ofSolidContainer()
        ).toCompletableFuture().join();
        System.out.println("HTTP status: " + response.statusCode());
        System.out.println("Resource URI: " + response.body().getId());}
 * </pre>
 */
package com.inrupt.client.solid;

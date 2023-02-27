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
 * <h2>Jena RDF support for the Inrupt Java Client Libraries.</h2>
 *
 * <p>The Jena module gives two possibilities to read RDF data: through the Service {@link JenaService}
 * and through the BodyHandler {@link JenaBodyHandlers}.
 * 
 * <p>A user of the {@code JenaService} should ensure that this implementation is
 * available on the classpath by adding the following dependency:
 *
 * <pre>
 *     &lt;dependency&gt;
 *            &lt;groupId&gt;com.inrupt&lt;/groupId&gt;
 *            &lt;artifactId&gt;inrupt-client-jena&lt;/artifactId&gt;
 *            &lt;version&gt;${project.version}&lt;/version&gt;
 *     &lt;/dependency&gt;
 * </pre>
 * 
 * <h3>Example of using the Jena Service toDataset() method to read triples
 * from a trig file into a {@code Dataset}:</h3>
 *
 * <pre>{@code
    RdfService service = ServiceProvider.getRdfService();
    Dataset dataset;
    try (InputStream input = Test.class.getResourceAsStream("/tripleExamples.trig")) {
        dataset = service.toDataset(RDFSyntax.TRIG, input);
    }
    System.out.println("Number of triples in file: " + dataset.size());
 * }</pre>
 *
 * <h3>Example of using the Jena BodyHandler ofModel() method to read the triples
 * from the same trig file into a {@code Model}:</h3>
 * <pre>{@code
    Request request = Request.newBuilder()
        .uri(URI.create("https://example.example/tripleExamples.trig"))
        .GET()
        .build();

    Response<Model> response = client.send(request, JenaBodyHandlers.ofModel()).toCompletableFuture().join();

    System.out.println("HTTP status code: " + response.statusCode());
    System.out.println("Number of triples in file: " + response.body().size());
 * }</pre>
 *
 * <h3>The {@link JenaBodyPublishers} can be used to write triples. An example that uses the
 * POST method to write a {@code Model}:</h3>
 *
 * <pre>{@code
    ModelBuilder builder = new ModelBuilder();
    builder.namedGraph("https://example.example/graph")
            .subject("https://example.example/subject")
                .add("https://example.example/predicate", "object");
    Model model = builder.build();

    Request request = Request.newBuilder()
            .uri("https://example.example/postEndpoint"))
            .header("Content-Type", "text/turtle")
            .POST(JenaBodyPublishers.ofModel(model))
            .build();

    Response<Void> response = client.send(request, Response.BodyHandlers.discarding()).toCompletableFuture().join();

    System.out.println("HTTP status code: " + response.statusCode());
 * }</pre>
 */
package com.inrupt.client.jena;

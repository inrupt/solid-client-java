/*
 * Copyright 2022 Inrupt Inc.
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
 * <h2>RDF4J RDF support for the Inrupt client libraries.</h2>
 *
 * <p>The RDF4J module gives two possibilities to read RDF data: through the Processor {@link RDF4JRdfProcessor}
 * and through the BodyHandler {@link RDF4JBodyHandlers}.
 *
 * <h3>Example of using the RDF4J Processor toDataset() method to read triples
 * from a trig file into a {@code Dataset}:</h3>
 *
 * <pre>{@code
    Rdf4JRdfProcessor processor = ServiceProvider.getRdfProcessor();
    Dataset dataset;
    try (InputStream input = Test.class.getResourceAsStream("/tripleExamples.trig")) {
        dataset = processor.toDataset(Syntax.TRIG, input);
    }
    System.out.println("Number of triples in file: " + dataset.stream().count());
 * }</pre>
 *
 * <h3>Example of using the RDF4J BodyHandler ofModel() method to read the triples
 * from the same trig file into a {@code Model}:</h3>
 * <pre>{@code
    Request request = Request.newBuilder()
        .uri(URI.create("https://example.example/tripleExamples.trig"))
        .GET()
        .build();

    Response<Model> response = client.send(request, RDF4JBodyHandlers.ofModel());

    System.out.println("HTTP status code: " + response.statusCode());
    System.out.println("Number of triples in file: " + response.body().size());
 * }</pre>
 *
 * <p>The {@link RDF4JBodyPublishers} can be used to write triples. An example that uses the
 * POST method to write a {@code Model}:
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
            .POST(RDF4JBodyPublishers.ofModel(model))
            .build();

    Response<Void> response = client.send(request, Response.BodyHandlers.discarding());

    System.out.println("HTTP status code: " + response.statusCode());
 * }</pre>
 */
package com.inrupt.client.rdf4j;

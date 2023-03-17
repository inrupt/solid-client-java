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
package com.inrupt.client.examples.springboot.model;

import com.inrupt.client.util.URIBuilder;
import java.net.URI;

import org.springframework.stereotype.Component;

@Component
public final class Vocabulary {

    // TODO: -----REPLACE------
    private static final URI BASE = URI.create("podStorage");

    private static final String BOOK_LIBRARY_CONTAINER = "MyBookLibrary";
    private static final String BOOK_LIBRARY_VOCABULARY_RESOURCE = "bookLibraryVocabulary";
    private static final String VOCABULARY_RESOURCE_SEPARATOR = "#";

    private static final String BOOK_LIBRARY_VOCABULARY = URIBuilder.newBuilder(BASE)
        .path(BOOK_LIBRARY_CONTAINER)
        .path(BOOK_LIBRARY_VOCABULARY_RESOURCE)
        .build()
        .toString()
        .concat(VOCABULARY_RESOURCE_SEPARATOR);

    public static final String BOOK_LIBRARY_RESOURCE = URIBuilder.newBuilder(BASE)
        .path(BOOK_LIBRARY_CONTAINER)
        .path("bookLibResource.ttl")
        .build()
        .toString();

    //Classes
    public static final String BOOK_LIBRARY = BOOK_LIBRARY_VOCABULARY.concat("BookLibrary");
    public static final String BOOK = BOOK_LIBRARY_VOCABULARY.concat("Book");
    //Relations
    public static final String BOOK_AUTHOR = BOOK_LIBRARY_VOCABULARY.concat("author");
    public static final String BOOK_DESCRIPTION = BOOK_LIBRARY_VOCABULARY.concat("description");
    public static final String CONTAINS_BOOK = BOOK_LIBRARY_VOCABULARY.concat("containsBook");

    //DC Vocabulary
    private static final String DC_BASE = "http://purl.org/dc/elements/1.1/";
    public static final String DC_TITLE = DC_BASE.concat("title");

    private Vocabulary() {
    }
}

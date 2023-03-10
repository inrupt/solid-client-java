package com.inrupt.client.examples.springboot.model;

public class Vocabulary {

    private static final String base = "https://inrupt.github.io/solid-client-java/vocab/BookLibraryVocabulary#";

    public static final String BOOK_LIBRARY = base.concat("BookLibrary");
    public static final String BOOK = base.concat("Book");
    public static final String BOOK_AUTHOR = base.concat("author");
    public static final String BOOK_DESCRIPTION = base.concat("description");
    public static final String CONTAINS_BOOK = base.concat("containsBook");
    public static final String BOOK_IN = base.concat("bookInLibrary");

    private static final String dc_base = "http://purl.org/dc/terms/";

    public static final String DC_TITLE = dc_base.concat("title");
}

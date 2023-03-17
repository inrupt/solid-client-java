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
package com.inrupt.client.examples.springboot;

import com.inrupt.client.examples.springboot.model.Book;
import com.inrupt.client.examples.springboot.model.BookLibrary;
import com.inrupt.client.examples.springboot.model.Vocabulary;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.util.URIBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class BookLibraryService {

    private static final String BOOK_LIBRARY_RESOURCE = Vocabulary.BOOK_LIBRARY_RESOURCE;
    private final SolidSyncClient client = SolidSyncClient.getClient();
    private BookLibrary bookLib;

    private BookLibrary loadBookLibraryResource() {

        final URI bookLibraryId = URIBuilder
            .newBuilder(URI.create(BOOK_LIBRARY_RESOURCE))
            .build();
        this.bookLib = client.read(bookLibraryId, BookLibrary.class);
        return this.bookLib;
    }

    public Set<URI> getAllBooks() {
        loadBookLibraryResource();
        return this.bookLib.getAllBooks();
    }

    public Set<Book> getBookForTitle(final String bookTitle) {
        loadBookLibraryResource();

        final Set<Book> foundBooks = new HashSet<>();

        final Set<URI> allBooks = this.bookLib.getAllBooks();
        allBooks.stream().forEach(oneBookURI -> {
            final Book book = client.read(oneBookURI, Book.class);
            if (book.getTitle() != null &&  book.getTitle().equals(bookTitle)) {
                foundBooks.add(book);
            }
        });

        return foundBooks;
    }

    public Set<URI> getBookForAuthor(final String bookAuthor) {
        loadBookLibraryResource();

        final Set<URI> foundBooks = new HashSet<>();

        final Set<URI> allBooks = this.bookLib.getAllBooks();
        allBooks.stream().forEach(oneBookURI -> {
            final Book book = client.read(oneBookURI, Book.class);
            if (book.getAuthor().equals(bookAuthor)) {
                foundBooks.add(oneBookURI);
            }
        });

        return foundBooks;
    }

}

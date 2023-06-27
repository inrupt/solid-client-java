/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.examples.springboot.service;

import com.inrupt.client.examples.springboot.AuthenticationException;
import com.inrupt.client.examples.springboot.AuthorizationException;
import com.inrupt.client.examples.springboot.model.Book;
import com.inrupt.client.examples.springboot.model.BookLibrary;
import com.inrupt.client.openid.OpenIdException;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.util.URIBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class BookLibraryService implements IBookLibraryService {

    @Autowired
    private UserService userService;
    private SolidSyncClient client;
    private BookLibrary bookLib;
    private String uriOfBookLibraryResource;

    public void loadBookLibrary(final String bookLibResource) {
        client = getClient();

        this.uriOfBookLibraryResource = bookLibResource;

        final URI bookLibraryId = URIBuilder
            .newBuilder(URI.create(bookLibResource))
            .build();
        try {
            this.bookLib = client.read(bookLibraryId, BookLibrary.class);
        } catch (SolidClientException exception) {
            if (authenticationFail(exception.getStatusCode())) {
                throw new AuthenticationException("You are not authenticated! Try logging in.");
            }
            if (authorizationFail(exception.getStatusCode())) {
                throw new AuthorizationException("You do not have the corresponding access rights.");
            }
        }
    }

    public void clearBookLibrary() {
        this.bookLib = null;
    }

    public Set<URI> getAllBookURIs() {
        if (this.bookLib == null) {
            throw new AuthorizationException("Not allowed.");
        }
        return this.bookLib.getAllBooks();
    }

    public Set<Book> getAllBook() {
        if (this.bookLib == null) {
            throw new AuthorizationException("Not allowed.");
        }
        final Set<Book> allBooks = new HashSet<>();
        this.bookLib.getAllBooks().stream().forEach(oneBookURI -> {
            final Book book = client.read(oneBookURI, Book.class);
            allBooks.add(book);
        });
        return allBooks;
    }

    public Set<Book> getBookForTitle(final String bookTitle) {
        if (this.bookLib == null) {
            throw new AuthorizationException("Not allowed.");
        }
        client = getClient();

        final Set<Book> foundBooks = new HashSet<>();

        this.bookLib.getAllBooks().stream().forEach(oneBookURI -> {
            final Book book = client.read(oneBookURI, Book.class);
            if (book.getTitle() != null && book.getTitle().contains(bookTitle)) {
                foundBooks.add(book);
            }
        });

        return foundBooks;
    }

    public Set<Book> getBookForAuthor(final String bookAuthor) {
        if (this.bookLib == null) {
            throw new AuthorizationException("Not allowed.");
        }
        client = getClient();

        final Set<Book> foundBooks = new HashSet<>();

        this.bookLib.getAllBooks().stream().forEach(oneBookURI -> {
            final Book book = client.read(oneBookURI, Book.class);
            if (book.getAuthor() != null && book.getAuthor().equals(bookAuthor)) {
                foundBooks.add(book);
            }
        });

        return foundBooks;
    }

    public String getBookLibraryUri() {
        return this.uriOfBookLibraryResource;
    }

    private SolidSyncClient getClient() {
        SolidSyncClient defaultClient = SolidSyncClient.getClient();

        if (userService.getCurrentUser() != null) {
            try {
                defaultClient = defaultClient.session(OpenIdSession.ofIdToken(userService.getCurrentUser().getToken()));
            } catch (OpenIdException exception) {
                throw new AuthorizationException("Token expired, please log out and re-login.");
            }
        }

        return defaultClient;
    }

    private boolean authenticationFail(final int statusCode) {
        return statusCode == 401;
    }

    private boolean authorizationFail(final int statusCode) {
        return statusCode == 403;
    }

}

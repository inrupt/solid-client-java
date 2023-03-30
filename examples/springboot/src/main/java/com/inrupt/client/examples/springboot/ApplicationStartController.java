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
import com.inrupt.client.examples.springboot.service.IBookLibraryService;
import com.inrupt.client.examples.springboot.service.UserService;

import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplicationStartController {

    private static final String INDEX_PAGE = "index";
    private static final String FRONTEND_USERNAME = "userName";
    private static final String FRONTEND_RESOURCE = "resource";
    private static final String FRONTEND_ERROR_MESSAGE = "error";
    private static final String FRONTEND_MESSAGE = "message";

    @Autowired
    private IBookLibraryService bookLibService;

    @Autowired
    private UserService userService;

    private String userName;


    @GetMapping("/")
    public String index() {
        this.userName = null;
        this.bookLibService.clearBookLibrary();
        return INDEX_PAGE;
    }

    @PostMapping("/loadlibrary")
    public String welcome(final @RequestParam String resource, final Model model) {
        Objects.nonNull(resource);
        setUsernameFrontendState(model);
        this.bookLibService.loadBookLibrary(resource);
        model.addAttribute(FRONTEND_RESOURCE, resource);
        return INDEX_PAGE;
    }

    @GetMapping("/logmein")
    public String login(final Model model) {
        setUsernameFrontendState(model);
        model.addAttribute(FRONTEND_RESOURCE, this.bookLibService.getBookLibraryUri());
        this.bookLibService.loadBookLibrary(this.bookLibService.getBookLibraryUri());
        return INDEX_PAGE;
    }

    @GetMapping("/allbooks")
    public String books(final Model model ) {
        setUsernameFrontendState(model);
        final Set<Book> result = this.bookLibService.getAllBook();
        model.addAttribute("allBooks", result);
        if (result.isEmpty()) {
            model.addAttribute(FRONTEND_MESSAGE, "We did not find any book.");
        }
        model.addAttribute(FRONTEND_RESOURCE, this.bookLibService.getBookLibraryUri());
        return INDEX_PAGE;
    }

    @GetMapping("/booksbyauthor")
    public String bookbyauthor(@RequestParam(value = "author", defaultValue = "Bram Stoker")
        final String author, final Model model) {
        setUsernameFrontendState(model);
        final Set<Book> result = this.bookLibService.getBookForAuthor(author);
        model.addAttribute("allBooks", this.bookLibService.getBookForAuthor(author));
        if (result.isEmpty()) {
            model.addAttribute(FRONTEND_MESSAGE, "We did not find any book.");
        }
        model.addAttribute(FRONTEND_RESOURCE, this.bookLibService.getBookLibraryUri());
        return INDEX_PAGE;
    }

    @GetMapping("/booksbytitle")
    public String bookbytitle(@RequestParam(value = "title", defaultValue = "Women")
        final String title, final Model model) {
        setUsernameFrontendState(model);
        final Set<Book> result = this.bookLibService.getBookForTitle(title);
        model.addAttribute("allBooks", result);
        if (result.isEmpty()) {
            model.addAttribute(FRONTEND_MESSAGE, "We did not find any book.");
        }
        model.addAttribute(FRONTEND_RESOURCE, this.bookLibService.getBookLibraryUri());
        return INDEX_PAGE;
    }

    @ExceptionHandler(value = { AuthorizationException.class, AuthenticationException.class,
        IllegalArgumentException.class })
    public String handleAuthorizationException(final RuntimeException exception, final Model model) {
        setUsernameFrontendState(model);
        model.addAttribute(FRONTEND_ERROR_MESSAGE, exception.getMessage());
        model.addAttribute(FRONTEND_RESOURCE, this.bookLibService.getBookLibraryUri());
        this.bookLibService.clearBookLibrary();
        return INDEX_PAGE;
    }

    private void setUsernameFrontendState(final Model model) {
        if (userService.getCurrentUser() != null) {
            this.userName = userService.getCurrentUser().getIdentifier().toString();
            model.addAttribute(FRONTEND_ERROR_MESSAGE, "");
        } else {
            this.userName = null;
        }
        model.addAttribute(FRONTEND_USERNAME, this.userName);
    }

}
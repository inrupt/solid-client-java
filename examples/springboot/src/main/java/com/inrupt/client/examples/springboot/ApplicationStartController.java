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

import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplicationStartController {

    @Autowired
    private SecurityContextHolderFacade context;

    @Autowired
    private IBookLibraryService bookLibService;

    private String bookLibraryResource;
    private String userName;


    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/welcome")
    public String welcome(final @RequestParam String resource, final Model model) {
        Objects.nonNull(resource);
        this.bookLibraryResource = resource;
        this.bookLibService.loadBookLibrary(resource);
        model.addAttribute("resource", resource);
        return "index";
    }

    @GetMapping("/logmein")
    public String login(final Model model) {
        final Object principal = context.getContext().getAuthentication().getPrincipal();
        if (principal instanceof OidcUser) {
            final OidcUser user = (OidcUser) principal;
            model.addAttribute("userName", user.getClaim("webid"));
        }
        return "index";
    }

    @ExceptionHandler(AuthenticationFailException.class)
    public String handleAuthenticationFailException(final AuthenticationFailException exception, final Model model) {
        model.addAttribute("error", "You are trying to access private resources. Please authenticate first!");
        model.addAttribute("resource", this.bookLibraryResource);
        return "index";
    }

    @GetMapping("/allbooks")
    public String books(final Model model ) {
        model.addAttribute("allBooks", this.bookLibService.getAllBookURIs());
        model.addAttribute("resource", this.bookLibraryResource);
        model.addAttribute("userName", this.userName);
        return "index";
    }

    @GetMapping("/bookbytitle")
    public String bookbytitle(@RequestParam(value = "title", defaultValue = "Dracula")
        final String title, final Model model) {
        final Set<Book> result = this.bookLibService.getBookForTitle(title);
        if (!result.isEmpty()) {
            model.addAttribute("booksByTitle", result);
        } else {
            model.addAttribute("error", "Did not find the book");
        }
        model.addAttribute("resource", this.bookLibraryResource);
        model.addAttribute("userName", this.userName);
        return "index";
    }
}
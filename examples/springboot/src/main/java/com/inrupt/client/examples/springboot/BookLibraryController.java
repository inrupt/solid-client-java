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

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookLibraryController {

    @Autowired
    private BookLibraryService bookLibService;
    
    @GetMapping("/allbooks")
    public String books(final Model model ) {
        model.addAttribute("allBooks", bookLibService.getAllBooks());
        return "index";
    }

    @GetMapping("/bookbytitle")
    public String bookbytitle(@RequestParam(value = "title", defaultValue = "Dracula")
        final String title, final Model model) {
        final Set<Book> result = bookLibService.getBookForTitle(title);
        if (!result.isEmpty()) {
            model.addAttribute("booksByTitle", result);
        } else {
            model.addAttribute("error", "Did not find the book");
        }
        return "index";
    }
}

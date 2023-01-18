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
package com.inrupt.client;

import java.util.Arrays;
import java.util.List;

public class ValidationResult {

    private boolean valid;
    private List<String> result;

    /**
     * Create a ValidationResult object.
     * 
     * @param valid the result from validation
     * @param messages the messages from validation method
     */
    public ValidationResult(final boolean valid, final String... messages) {
        this.valid = valid;
        this.result = Arrays.asList(messages);
    }

    /**
     * The result of the validation.
     * 
     * @return the valid boolean value
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * The messages from validation.
     * 
     * @return the result messages
     */
    public List<String> getResults() {
        return this.result;
    }
}



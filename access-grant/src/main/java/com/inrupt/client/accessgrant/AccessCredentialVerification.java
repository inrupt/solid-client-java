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
package com.inrupt.client.accessgrant;

import java.util.Collections;
import java.util.List;

/**
 * The response from a verification operation.
 */
public class AccessCredentialVerification {

    private List<String> checks;
    private List<String> warnings;
    private List<String> errors;

    /**
     * Create an empty verification response.
     */
    public AccessCredentialVerification() {
        this.checks = Collections.emptyList();
        this.warnings = Collections.emptyList();
        this.errors = Collections.emptyList();
    }

    /**
     * Create a verification response.
     *
     * @param checks the checks that were performed
     * @param warnings any warnings from the verification operation
     * @param errors any errors from the verification operation
     */
    public AccessCredentialVerification(final List<String> checks, final List<String> warnings,
            final List<String> errors) {
        this.checks = makeImmutable(checks);
        this.warnings = makeImmutable(warnings);
        this.errors = makeImmutable(errors);
    }

    /**
     * The verification checks that were performed.
     *
     * @return an unmodifiable list of any verification checks performed, never {@code null}
     */
    public List<String> getChecks() {
        return checks;
    }

    /**
     * The verification warnings that were discovered.
     *
     * @return an unmodifiable list of any verification warnings, never {@code null}
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * The verification errors that were discovered.
     *
     * @return an unmodifiable list of any verification errors, never {@code null}
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Initialize the verification checks that were performed. This can only be called once, as the checks list is
     * made unmodifiable.
     *
     * @param checks a list of any verification checks performed, never {@code null}
     */
    public void setChecks(final List<String> checks) {
        this.checks = makeImmutable(checks);

    }

    /**
     * Initialize the verification warnings that were discovered. This can only be called once, as the warnings list is
     * made unmodifiable.
     * @param warnings a list of any verification warnings, never {@code null}
     */
    public void setWarnings(final List<String> warnings) {
        this.warnings = makeImmutable(warnings);
    }

    /**
     * Initialize the verification errors that were discovered. This can only be called once, as the errors list is
     * made unmodifiable.
     * @param errors a list of any verification errors, never {@code null}
     */
    public void setErrors(final List<String> errors) {
        this.errors = makeImmutable(errors);
    }

    static List<String> makeImmutable(final List<String> list) {
        if (list != null) {
            return Collections.unmodifiableList(list);
        }
        return Collections.emptyList();
    }
}


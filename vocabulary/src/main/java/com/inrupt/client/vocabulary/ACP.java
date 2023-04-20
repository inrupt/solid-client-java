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
package com.inrupt.client.vocabulary;

import java.net.URI;

/**
 * URI constants from the ACP vocabulary.
 *
 * @see <a href="https://www.w3.org/ns/acp">ACP Vocabulary</a>
 */
public class ACP {

    private static String namespace = "http://www.w3.org/ns/solid/acp#";

    // Properties
    /**
     * The acp:resource URI.
     */
    public static final URI resource = URI.create(namespace + "resource");
    /**
     * The acp:accessControlResource URI.
     */
    public static final URI accessControlResource = URI.create(namespace + "accessControlResource");
    /**
     * The acp:accessControl URI.
     */
    public static final URI accessControl = URI.create(namespace + "accessControl");
    /**
     * The acp:memberAccessControl URI.
     */
    public static final URI memberAccessControl = URI.create(namespace + "memberAccessControl");
    /**
     * The acp:apply URI.
     */
    public static final URI apply = URI.create(namespace + "apply");
    /**
     * The acp:allow URI.
     */
    public static final URI allow = URI.create(namespace + "allow");
    /**
     * The acp:deny URI.
     */
    public static final URI deny = URI.create(namespace + "deny");
    /**
     * The acp:allOf URI.
     */
    public static final URI allOf = URI.create(namespace + "allOf");
    /**
     * The acp:anyOf URI.
     */
    public static final URI anyOf = URI.create(namespace + "anyOf");
    /**
     * The acp:noneOf URI.
     */
    public static final URI noneOf = URI.create(namespace + "noneOf");
    /**
     * The acp:vc URI.
     */
    public static final URI vc = URI.create(namespace + "vc");

    // Classes
    /**
     * The acp:AccessControlResource URI.
     */
    public static final URI AccessControlResource = URI.create(namespace + "AccessControlResource");
    /**
     * The acp:AccessControl URI.
     */
    public static final URI AccessControl = URI.create(namespace + "AccessControl");
    /**
     * The acp:Policy URI.
     */
    public static final URI Policy = URI.create(namespace + "Policy");
    /**
     * The acp:Matcher URI.
     */
    public static final URI Matcher = URI.create(namespace + "Matcher");

    /**
     * Get the ACP namespace URI.
     *
     * @return the ACP namespace
     */
    public static URI getNamespace() {
        return URI.create(namespace);
    }

    private ACP() {
        // Prevent instantiation
    }
}

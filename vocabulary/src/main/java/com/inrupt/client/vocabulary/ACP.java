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
package com.inrupt.client.vocabulary;

import java.net.URI;

/**
 * URI constants from the ACP vocabulary.
 *
 * @see <a href="https://www.w3.org/ns/acp">ACP Vocabulary</a>
 */
public final class ACP {

    private static String namespace = "http://www.w3.org/ns/solid/acp#";

    // Named Individuals
    /** The acp:AuthenticatedAgent URI. */
    public static final URI AuthenticatedAgent = URI.create(namespace + "AuthenticatedAgent");

    /** The acp:PublicAgent URI. */
    public static final URI PublicAgent = URI.create(namespace + "PublicAgent");

    /** The acp:PublicClient URI. */
    public static final URI PublicClient = URI.create(namespace + "PublicClient");

    /** The acp:PublicIssuer URI. */
    public static final URI PublicIssuer = URI.create(namespace + "PublicIssuer");

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
    /**
     * The acp:client URI.
     */
    public static final URI client = URI.create(namespace + "client");
    /**
     * The acp:agent URI.
     */
    public static final URI agent = URI.create(namespace + "agent");
    /**
     * The acp:issuer URI.
     */
    public static final URI issuer = URI.create(namespace + "issuer");

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

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
 * URI constants from the ACL vocabulary.
 *
 * @see <a href="http://www.w3.org/ns/auth/acl">ACL Vocabulary</a>
 */
public final class ACL {

    private static String namespace = "http://www.w3.org/ns/auth/acl#";

    // Classes
    /**
     * The acl:Access URI.
     */
    public static final URI Access = URI.create(namespace + "Access");
    /**
     * The acl:Append URI.
     */
    public static final URI Append = URI.create(namespace + "Append");
    /**
     * The acl:AuthenticatedAgent URI.
     */
    public static final URI AuthenticatedAgent = URI.create(namespace + "AuthenticatedAgent");
    /**
     * The acl:Authorization URI.
     */
    public static final URI Authorization = URI.create(namespace + "Authorization");
    /**
     * The acl:Control URI.
     */
    public static final URI Control = URI.create(namespace + "Control");
    /**
     * The acl:Origin URI.
     */
    public static final URI Origin = URI.create(namespace + "Origin");
    /**
     * The acl:Read URI.
     */
    public static final URI Read = URI.create(namespace + "Read");
    /**
     * The acl:Write URI.
     */
    public static final URI Write = URI.create(namespace + "Write");

    /**
     * Get the ACL namespace URI.
     *
     * @return the ACL namespace
     */
    public static URI getNamespace() {
        return URI.create(namespace);
    }

    private ACL() {
        // Prevent instantiation
    }
}

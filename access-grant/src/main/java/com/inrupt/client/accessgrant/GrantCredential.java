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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An Generic grant credential.
 */
public class GrantCredential {

    private List<String> context;
    private Set<String> modes;
    private Set<String> purposes;
    private Set<String> forPersonalData;
    private String expirationDate;
    private String inherit;
    private Map<String, Object> credential;

    public void context(List<String> context) {
        this.context = context;
    }

    public List<String> context() {
        return this.context;
    }
    
    public void modes(Set<String> modes) {
        this.modes = modes;
    }

    public Set<String> modes() {
        return this.modes;
    }

    public void purposes(Set<String> purposes) {
        this.purposes = purposes;
    }

    public Set<String> purposes() {
        return this.purposes;
    }

    public void forPersonalData(Set<String> forPersonalData) {
        this.forPersonalData = forPersonalData;
    }

    public Set<String> forPersonalData() {
        return this.forPersonalData;
    }

    public void expirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String expirationDate() {
        return this.expirationDate;
    }

    public void inherit(String inherit) {
        this.inherit = inherit;
    }

    public String inherit() {
        return this.inherit;
    }

    public void credential(Map<String, Object> credential) {
        this.credential = credential;
    }

    public Map<String, Object> credential() {
        return this.credential;
    }

}

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
package com.inrupt.client.examples.springboot.model;

import java.util.Objects;

public class WebIdOwner {
    
    private final String name;
    private final String email;
    private final String bday;
    private final String webid;

    private WebIdOwner(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.bday = builder.bday;
        this.webid = builder.webid;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getBday() {
        return this.bday;
    }

    public String getWebid() {
        return this.webid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
            WebIdOwner that = (WebIdOwner) o;
        return name.equals(that.name) && bday.equals(that.bday) && email.equals(that.email) && webid.equals(that.webid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, email, bday, webid);
    }

    public static class Builder {

        private String name;
        private String email;
        private String bday;
        private String webid;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withBday(String bday) {
            this.bday = bday;
            return this;
        }

        public Builder withWebid(String webid) {
            this.webid = webid;
            return this;
        }

        public WebIdOwner build() {
            return new WebIdOwner(this);
        }
    }
}

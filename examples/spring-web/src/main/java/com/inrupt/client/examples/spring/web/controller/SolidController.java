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
package com.inrupt.client.examples.spring.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_TOKEN;

import com.inrupt.client.auth.Session;
import com.inrupt.client.examples.spring.web.model.*;
import com.inrupt.client.openid.OpenIdException;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidRDFSource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.spring.SessionUtils;
import com.inrupt.client.webid.WebIdProfile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class SolidController {

    private static final SolidSyncClient client = SolidSyncClient.getClient();

    @Autowired
    HttpSession httpSession;

    @GetMapping(value = "/resource", produces = "text/turtle")
    public Resource getResource(final @AuthenticationPrincipal OAuth2User user, final @RequestParam URI uri) {
        final var session = SessionUtils.asSession(user)
            .orElseThrow(() -> new OAuth2AuthenticationException(INVALID_TOKEN));
        try (final var resource = client.session(session).read(uri, SolidRDFSource.class)) {
            return new Resource(resource);
        }
    }

    @GetMapping(value = "/webid", produces = APPLICATION_JSON_VALUE)
    public WebId getUser(final @AuthenticationPrincipal OAuth2User user) {
        final var webid = SessionUtils.asSession(user).flatMap(Session::getPrincipal)
            .orElseThrow(() -> new OAuth2AuthenticationException(INVALID_TOKEN));
        return new WebId(webid);
    }

    @GetMapping(value = "/profile", produces = APPLICATION_JSON_VALUE)
    public Profile getProfile(final @AuthenticationPrincipal OAuth2User user) {
        final var session = SessionUtils.asSession(user)
            .orElseThrow(() -> new OAuth2AuthenticationException(INVALID_TOKEN));
        final var webid = session.getPrincipal().orElseThrow(() -> new OAuth2AuthenticationException(INVALID_TOKEN));
        try (final var profile = client.session(session).read(webid, WebIdProfile.class)) {
            return Profile.of(profile);
        }
    }

    @ExceptionHandler(SolidClientException.class)
    public void clientException(final HttpServletResponse response) throws IOException {
        httpSession.invalidate();
        response.sendError(400);
    }

    @ExceptionHandler({OAuth2AuthenticationException.class, OpenIdException.class})
    public void sessionException(final HttpServletResponse response) throws IOException {
        httpSession.invalidate();
        response.sendError(401);
    }
}

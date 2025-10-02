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
/**
 * <h2>Access Control Policy support for the Inrupt Java Client Libraries.</h2>
 *
 * <p>This module contains classes and methods to convert ACP resources into
 * a @{@link AccessControlResource} Java object.
 *
 * <p>The following example reads a Solid Access Control Resource and presents it as an {@link AccessControlResource}
 * Java object:
 *
 * <pre>{@code
 *      try (AccessControlResource acr = client.read(uri, AccessControlResource.class)) {
 *          // find policies that grant {@code agent} read access
 *          Set<Policy> policies = acr.expand(client).find(MatcherType.AGENT, agent, Set.of(ACL.Read));
 *
 *          // remove these policies from the ACR
 *          Set<AccessControl> accessControls = new HashSet<>();
 *          accessControls.addAll(acr.accessControl());
 *          accessControls.addAll(acr.memberAccessControl());
 *          for (Policy p : policies) {
 *              for (AccessControl accessControl = accessControls) {
 *                  for (Policy policy : accessControl.apply()) {
 *                      policy.remove(p);
 *                  }
 *              }
 *          }
 *          acr.compact();
 *          client.update(acr);
 *      }}
 *  </pre>
 */
package com.inrupt.client.acp;

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
package com.inrupt.commons.wrapping.test.jena;

import com.inrupt.commons.wrapping.test.PredicateObjectSetBase;
import com.inrupt.commons.wrapping.test.TermMappingTestBase;
import com.inrupt.commons.wrapping.test.ValueMappingTestBase;
import com.inrupt.commons.wrapping.test.WrapperBlankNodeOrIRIBase;

import org.junit.jupiter.api.DisplayName;

@DisplayName("Node Mapping (Jena)")
class TermMappingTest extends TermMappingTestBase {
}

@DisplayName("Predicate-Object Set (Jena)")
class PredicateObjectSetTest extends PredicateObjectSetBase {
}

@DisplayName("Value Mapping (Jena)")
class ValueMappingTest extends ValueMappingTestBase {
}

@DisplayName("Wrapper blank node or IRI wrapper (Jena)")
class WrapperBlankNodeOrIRITest extends WrapperBlankNodeOrIRIBase {
}

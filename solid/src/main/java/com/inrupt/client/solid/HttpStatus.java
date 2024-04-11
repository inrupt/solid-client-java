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
package com.inrupt.client.solid;

import java.util.Arrays;
import java.util.Optional;

enum HttpStatus {
    BAD_REQUEST(BadRequestException.STATUS_CODE, "Bad Request"),
    UNAUTHORIZED(UnauthorizedException.STATUS_CODE, "Unauthorized"),
    FORBIDDEN(ForbiddenException.STATUS_CODE, "Forbidden"),
    NOT_FOUND(NotFoundException.STATUS_CODE, "Not Found"),
    METHOD_NOT_ALLOWED(MethodNotAllowedException.STATUS_CODE, "Method Not Allowed"),
    NOT_ACCEPTABLE(NotAcceptableException.STATUS_CODE, "Not Acceptable"),
    CONFLICT(ConflictException.STATUS_CODE, "Conflict"),
    GONE(GoneException.STATUS_CODE, "Gone"),
    PRECONDITION_FAILED(PreconditionFailedException.STATUS_CODE, "Precondition Failed"),
    UNSUPPORTED_MEDIA_TYPE(UnsupportedMediaTypeException.STATUS_CODE, "Unsupported Media Type"),
    TOO_MANY_REQUESTS(TooManyRequestsException.STATUS_CODE, "Too Many Requests"),
    INTERNAL_SERVER_ERROR(InternalServerErrorException.STATUS_CODE, "Internal Server Error");

    final int code;
    final String message;

    HttpStatus(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    static String getStatusMessage(final int statusCode) {
        final Optional<HttpStatus> knownStatus = Arrays.stream(HttpStatus.values())
                .filter(status -> status.code == statusCode)
                .findFirst();
        if (knownStatus.isPresent()) {
            return knownStatus.get().message;
        }
        // If the status is unknown, default to 400 for client errors and 500 for server errors
        if (statusCode > 499) {
            return INTERNAL_SERVER_ERROR.message;
        }
        return BAD_REQUEST.message;
    }
}

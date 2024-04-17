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

import com.inrupt.client.ClientHttpException;
import com.inrupt.client.Headers;

import java.net.URI;

/**
 * A runtime exception for use with SolidClient HTTP operations.
 */
public class SolidClientException extends ClientHttpException {

    private static final long serialVersionUID = 2868432164225689934L;

    /**
     * Create a SolidClient exception.
     *
     * @param message the message
     * @param uri the uri
     * @param statusCode the HTTP status code
     * @param headers the response headers
     * @param body the body
     */
    public SolidClientException(final String message, final URI uri, final int statusCode,
            final Headers headers, final String body) {
        super(message, uri, statusCode, headers, body);
    }

    /**
     *
     * @param message the resulting exception message
     * @param uri the request URL
     * @param statusCode the response status code
     * @param headers the response {@link Headers}
     * @param body the response body
     * @return an appropriate exception based on the status code.
     */
    public static SolidClientException handle(
            final String message,
            final URI uri,
            final int statusCode,
            final Headers headers,
            final String body) {
        switch (statusCode) {
            case BadRequestException.STATUS_CODE:
                return new BadRequestException(message, uri, headers, body);
            case UnauthorizedException.STATUS_CODE:
                return new UnauthorizedException(message, uri, headers, body);
            case ForbiddenException.STATUS_CODE:
                return new ForbiddenException(message, uri, headers, body);
            case NotFoundException.STATUS_CODE:
                return new NotFoundException(message, uri, headers, body);
            case MethodNotAllowedException.STATUS_CODE:
                return new MethodNotAllowedException(message, uri, headers, body);
            case NotAcceptableException.STATUS_CODE:
                return new NotAcceptableException(message, uri, headers, body);
            case ConflictException.STATUS_CODE:
                return new ConflictException(message, uri, headers, body);
            case GoneException.STATUS_CODE:
                return new GoneException(message, uri, headers, body);
            case PreconditionFailedException.STATUS_CODE:
                return new PreconditionFailedException(message, uri, headers, body);
            case UnsupportedMediaTypeException.STATUS_CODE:
                return new UnsupportedMediaTypeException(message, uri, headers, body);
            case TooManyRequestsException.STATUS_CODE:
                return new TooManyRequestsException(message, uri, headers, body);
            case InternalServerErrorException.STATUS_CODE:
                return new InternalServerErrorException(message, uri, headers, body);
            default:
                return new SolidClientException(message, uri, statusCode, headers, body);
        }
    }
}


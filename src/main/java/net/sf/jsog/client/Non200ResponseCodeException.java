/* This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
package net.sf.jsog.client;

/**
 * Thrown when the server responds with a non-200 HTTP response code.
 * @author jrodriguez
 */
public class Non200ResponseCodeException extends JsogClientException {

    /**
     * The serial version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The actual response code.
     */
    private int responseCode;

    /**
     * The HTTP status line.
     */
    private String status;

    /**
     * The content that was sent with the response.
     */
    private String content;

    /**
     * Constructs a new Non200ResponseCodeException.
     * @param responseCode the actual response code.
     * @param status the HTTP status line.
     * @param content The content that was sent with the response.
     */
    public Non200ResponseCodeException(final int responseCode,
                                       final String status,
                                       final String content) {
        super("Non-200 response from server.");
        this.responseCode = responseCode;
        this.status = status;
        this.content = content;
    }

    /**
     * Gets the response code that was sent instead of a 200.
     * @return the response code that was sent instead of a 200.
     */
    public final int getResponseCode() {
        return responseCode;
    }

    /**
     * Gets the HTTP status line that was sent with the request.
     * @return the HTTP status line that was sent with the request.
     */
    public final String getStatus() {
        return status;
    }

    /**
     * The content that was sent with the response.
     * @return the content that was sent with the response.
     */
    public final String getContent() {
        return content;
    }

}

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

import java.io.IOException;

/**
 * Thrown when parsing a string as JSOG fails.
 * @author jrodriguez
 */
public class InvalidJsogException extends JsogClientException {

    /**
     * The serial version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The content that was sent instead of valid JSOG.
     */
    private String content;

    /**
     * Creates a new InvalidJsogException.
     * @param message the message.
     * @param content the content that was sent instead of valid JSOG.
     * @param cause the cause of the exception.
     */
    public InvalidJsogException(final String message,
                                final String content,
                                final IOException cause) {
        super(message, cause);
        this.content = content;
    }

    /**
     * Gets the content that failed to parse.
     * @return The content that was sent instead of valid JSOG.
     */
    public final String getContent() {
        return content;
    }

}

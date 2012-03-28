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
import net.sf.jsog.JSOG;

/**
 * The default JsogClient implementation.
 *
 * <p>
 * This class extends the functionality of {@link DefaultHttpClientImpl} by
 * adding support for JSOG-based content.
 * <p>
 *
 * <p>
 * This is ALPHA quality code. The API is unlikely to change, and there are
 * test cases covering most of the "happy path" functionality, but corner
 * cases may cause unexpected behavior. Please submit feedback to
 * <a href="mailto:jeff@jeffrodriguez.com">Jeff Rodriguez</a>.
 * </p>
 * @author jrodriguez
 */
public class DefaultJsogClientImpl
       extends DefaultHttpClientImpl
       implements JsogClient {

    /**
     * Constructs a new DefaultJsogClientImpl.
     */
    public DefaultJsogClientImpl() {
        super();
        setContentType("application/json");
    }

    @Override
    public final JSOG getJsog(final String url) {
        
        // Execute the request
        String content = get(url);

        // Parse the JSOG
        try {
            return JSOG.parse(content);
        } catch (IOException e) {
            throw new InvalidJsogException(
                    "Unable to parse content as JSOG.", content, e);
        }
    }

    @Override
    public final JSOG postJsog(final String url, final JSOG data) {

        // Execute the request
        return postJsog(url, data.toString());
    }

    @Override
    public final JSOG postJsog(final String url, final String data) {

        String content = post(url, data);

        // Parse the JSOG
        try {
            return JSOG.parse(content);
        } catch (IOException e) {
            throw new InvalidJsogException(
                    "Unable to parse content as JSOG.", content, e);
        }
    }

}

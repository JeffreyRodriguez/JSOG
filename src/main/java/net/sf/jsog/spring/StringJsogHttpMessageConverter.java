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
package net.sf.jsog.spring;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.jsog.JSOG;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * Provides converter for String&lt;-&gt;JSOG conversion.
 *
 * Requires the following maven dependencies:
 * <ul>
 *   <li>net.sf.jsog:jsog</li>
 *   <li>commons-io:commons-io</li>
 *   <li>org.springframework:spring-webmvc</li>
 * </ul>
 *
 * Usage:
 * <pre>
&lt;bean class="org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter"&gt;
    &lt;property name="messageConverters"&gt;
        &lt;bean class="net.sf.jsog.spring.StringJsogHttpMessageConverter"/&gt;
    &lt;/property&gt;
&lt;/bean&gt;
 * </pre>
 *
 * @author jrodriguez
 */
public class StringJsogHttpMessageConverter implements HttpMessageConverter<JSOG> {

    public StringJsogHttpMessageConverter() {
        supportedMediaTypes.add(outputContentType);
    }

    /**
     * The output content type to use.
     */
    private MediaType outputContentType =
            new MediaType("application", "json", Charset.forName("ISO-8859-1"));

    /**
     * The output content type to use.
     * @param outputContentType the output content type.
     */
    public void setOutputContentType(MediaType outputContentType) {
        this.outputContentType = outputContentType;
    }

    /**
     * This encoding will be used as the default when reading, and the encoding
     * when writing if the output content type's charset is null.
     * @see MediaType#getCharSet()
     */
    private Charset encoding = Charset.forName("ISO-8859-1");

    /**
     * This encoding will be used as the default when reading, and the encoding
     * when writing if the output content type's charset is null.
     * @param encoding the encoding to set.
     * @see MediaType#getCharSet()
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     * This encoding will be used as the default when reading, and the encoding
     * when writing if the output content type's charset is null.
     * @param encoding the encoding to set.
     * @see MediaType#getCharSet()
     */
    public void setEncoding(String encoding) {
        this.encoding = Charset.forName(encoding);
    }

    private List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();

    /**
     * Sets the {@link MediaType}s supported by this converter.
     *
     * Defaults to {@link MediaType#APPLICATION_JSON} only.
     * @param supportedMediaTypes the supported media types to use.
     */
    public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
        this.supportedMediaTypes = new ArrayList<MediaType>(supportedMediaTypes);
    }
    
    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.unmodifiableList(supportedMediaTypes);
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if (!JSOG.class.isAssignableFrom(clazz)) {
            return false;
        }

        for (MediaType type : supportedMediaTypes) {
            if (type == null || !type.isCompatibleWith(mediaType)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public JSOG read(Class<? extends JSOG> clazz, HttpInputMessage input)
            throws IOException, HttpMessageNotReadableException {
        HttpHeaders headers = input.getHeaders();
        MediaType contentType = headers.getContentType();
        Charset encoding = contentType.getCharSet();
        
        if (encoding == null) {
            encoding = this.encoding;
        }

        // Read in the JSON
        String json = IOUtils.toString(input.getBody(), encoding.name());

        // Parse the JSON and return a JSOG.
        try {
            return JSOG.parse(json);
        } catch (IOException e) {
            throw new HttpMessageNotReadableException("Unable to parse JSON.", e);
        }
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return JSOG.class.isAssignableFrom(clazz)
                && outputContentType.isCompatibleWith(mediaType);
    }

    @Override
    public void write(JSOG jsog, MediaType type, HttpOutputMessage output)
            throws IOException, HttpMessageNotWritableException {

        // If the outputContentType doesn't specify a charset, we need to set it
        Charset encoding = outputContentType.getCharSet();
        if (encoding != null) {
            output.getHeaders().setContentType(outputContentType);
        } else {
            encoding = this.encoding;

            output.getHeaders().setContentType(new MediaType(
                    outputContentType.getType(),
                    outputContentType.getSubtype(),
                    encoding));
        }

        // Transform the JSOG to a byte array encoded in the proper encoding
        byte[] text = jsog.toString().getBytes(encoding);

        // Set the content length
        output.getHeaders().setContentLength(text.length);
        IOUtils.write(text, output.getBody());
    }
}

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

import java.nio.charset.Charset;
import java.io.ByteArrayOutputStream;
import org.springframework.http.HttpOutputMessage;
import java.io.ByteArrayInputStream;
import net.sf.jsog.JSOG;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author jrodriguez
 */
public class StringJsogHttpMessageConverterTest {
    public static MediaType JSON_CONTENT_TYPE =
            new MediaType("application", "json", Charset.forName("ISO-8859-1"));

    StringJsogHttpMessageConverter instance;

    @Before
    public void setUp() {
        instance = new StringJsogHttpMessageConverter();
    }

    @Test
    public void testGetSupportedMediaTypes() {
        List<MediaType> expected = new ArrayList<MediaType>();
        expected.add(JSON_CONTENT_TYPE);

        List<MediaType> actual = instance.getSupportedMediaTypes();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetSupportedMediaTypesCustom() {
        List<MediaType> expected = new ArrayList<MediaType>();
        expected.add(JSON_CONTENT_TYPE);

        instance.setSupportedMediaTypes(expected);
        List<MediaType> actual = instance.getSupportedMediaTypes();
        assertEquals(expected, actual);
    }

    @Test
    public void testCanRead() {
        assertTrue(instance.canRead(JSOG.class, JSON_CONTENT_TYPE));
    }

    @Test
    public void testCanReadNotJsog() {
        assertFalse(instance.canRead(String.class, JSON_CONTENT_TYPE));
    }

    @Test
    public void testCanReadNotAppJson() {
        assertFalse(instance.canRead(Object.class, MediaType.TEXT_PLAIN));
    }

    @Test
    public void testCanReadNullType() {
        assertFalse(instance.canRead(JSOG.class, null));
    }

    @Test
    public void testRead() throws Exception {

        // Setup
        JSOG expected = new JSOG("foobar");

        MediaType contentType = JSON_CONTENT_TYPE;

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(headers.getContentType()).andReturn(contentType);

        HttpInputMessage message = createMock(HttpInputMessage.class);
        expect(message.getHeaders()).andReturn(headers);

        expect(message.getBody()).andReturn(new ByteArrayInputStream(
                expected.toString().getBytes("ISO-8859-1")));

        // Execute
        replay(headers, message);
        JSOG actual = instance.read(JSOG.class, message);

        // Verify
        verify(message);
        assertEquals(expected, actual);
    }

    @Test
    public void testReadObject() throws Exception {

        // Setup
        JSOG expected = JSOG.object("foo", "bar");

        MediaType contentType = JSON_CONTENT_TYPE;

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(headers.getContentType()).andReturn(contentType);

        HttpInputMessage message = createMock(HttpInputMessage.class);
        expect(message.getHeaders()).andReturn(headers);

        expect(message.getBody()).andReturn(new ByteArrayInputStream(
                expected.toString().getBytes("ISO-8859-1")));

        // Execute
        replay(headers, message);
        JSOG actual = instance.read(JSOG.class, message);

        // Verify
        verify(message);
        assertEquals(expected, actual);
    }

    @Test
    public void testReadArray() throws Exception {

        // Setup
        JSOG expected = JSOG.array("foo", "bar");

        MediaType contentType = JSON_CONTENT_TYPE;

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(headers.getContentType()).andReturn(contentType);

        HttpInputMessage message = createMock(HttpInputMessage.class);
        expect(message.getHeaders()).andReturn(headers);

        expect(message.getBody()).andReturn(new ByteArrayInputStream(
                expected.toString().getBytes("ISO-8859-1")));

        // Execute
        replay(headers, message);
        JSOG actual = instance.read(JSOG.class, message);

        // Verify
        verify(message);
        assertEquals(expected, actual);
    }

    @Test(expected=HttpMessageNotReadableException.class)
    public void testReadBad() throws Exception {

        // Setup
        MediaType contentType = JSON_CONTENT_TYPE;

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(headers.getContentType()).andReturn(contentType);

        HttpInputMessage message = createMock(HttpInputMessage.class);
        expect(message.getHeaders()).andReturn(headers);

        expect(message.getBody()).andReturn(new ByteArrayInputStream(
                "[".toString().getBytes("ISO-8859-1")));

        // Execute
        replay(headers, message);
        try {
            instance.read(JSOG.class, message);
        } finally {

            // Verify
            verify(message);
        }
    }

    @Test
    public void testCanWrite() {
        assertTrue(instance.canWrite(JSOG.class, JSON_CONTENT_TYPE));
    }

    @Test
    public void testCanWriteNotJsog() {
        assertFalse(instance.canWrite(String.class, JSON_CONTENT_TYPE));
    }

    @Test
    public void testCanWriteNotAppJson() {
        assertFalse(instance.canWrite(JSOG.class, MediaType.TEXT_PLAIN));
    }

    @Test
    public void testCanWriteNullType() {
        assertFalse(instance.canWrite(JSOG.class, null));
    }

    @Test
    public void testWrite() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "ISO-8859-1";
        String expected = "\"foobar\"";
        MediaType contentType = JSON_CONTENT_TYPE;

        HttpOutputMessage output = createMock(HttpOutputMessage.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expect(output.getBody()).andReturn(baos);

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(output.getHeaders()).andReturn(headers).anyTimes();

        headers.setContentType(contentType);
        expectLastCall();
        headers.setContentLength(expected.getBytes(encoding).length);
        expectLastCall();

        // Execution
        replay(headers, output);
        instance.write(JSOG.parse(expected), contentType, output);

        // Verification
        verify(headers, output);
        assertEquals(expected, baos.toString(encoding));
    }

    @Test
    public void testWriteObject() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "ISO-8859-1";
        String expected = "{\"foo\":\"bar\"}";
        MediaType contentType = JSON_CONTENT_TYPE;

        HttpOutputMessage output = createMock(HttpOutputMessage.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expect(output.getBody()).andReturn(baos);

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(output.getHeaders()).andReturn(headers).anyTimes();

        headers.setContentType(contentType);
        expectLastCall();
        headers.setContentLength(expected.getBytes(encoding).length);
        expectLastCall();

        // Execution
        replay(headers, output);
        instance.write(JSOG.parse(expected), contentType, output);

        // Verification
        verify(headers, output);
        assertEquals(expected, baos.toString(encoding));
    }

    @Test
    public void testWriteArray() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "ISO-8859-1";
        String expected = "[\"foo\",\"bar\"]";
        MediaType contentType = JSON_CONTENT_TYPE;

        HttpOutputMessage output = createMock(HttpOutputMessage.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expect(output.getBody()).andReturn(baos);

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(output.getHeaders()).andReturn(headers).anyTimes();

        headers.setContentType(contentType);
        expectLastCall();
        headers.setContentLength(expected.getBytes(encoding).length);
        expectLastCall();

        // Execution
        replay(headers, output);
        instance.write(JSOG.parse(expected), contentType, output);

        // Verification
        verify(headers, output);
        assertEquals(expected, baos.toString(encoding));
    }

    @Test
    public void testWriteCustomOutputContentType() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "US-ASCII";
        instance.setEncoding(encoding);

        String expected = "\"foobar\"";

        MediaType contentType = new MediaType("text", "plain",
                                              Charset.forName(encoding));
        instance.setOutputContentType(contentType);

        HttpOutputMessage output = createMock(HttpOutputMessage.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expect(output.getBody()).andReturn(baos);

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(output.getHeaders()).andReturn(headers).anyTimes();

        headers.setContentType(contentType);
        expectLastCall();
        headers.setContentLength(expected.getBytes(encoding).length);
        expectLastCall();

        // Execution
        replay(headers, output);
        instance.write(JSOG.parse(expected), contentType, output);

        // Verification
        verify(headers, output);
        assertEquals(expected, baos.toString(encoding));
    }

    @Test
    public void testWriteCustomEncoding() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "UTF-8";
        instance.setEncoding(encoding);

        String expected = "\"foobar\"";

        MediaType contentType = JSON_CONTENT_TYPE;

        HttpOutputMessage output = createMock(HttpOutputMessage.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expect(output.getBody()).andReturn(baos);

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(output.getHeaders()).andReturn(headers).anyTimes();

        headers.setContentType(contentType);
        expectLastCall();
        headers.setContentLength(expected.getBytes(encoding).length);
        expectLastCall();

        // Execution
        replay(headers, output);
        instance.write(JSOG.parse(expected), contentType, output);

        // Verification
        verify(headers, output);
        assertEquals(expected, baos.toString(encoding));
    }

    @Test
    public void testWriteCustomEncodingSetByCharset() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "UTF-8";
        instance.setEncoding(Charset.forName(encoding));

        String expected = "\"foobar\"";

        MediaType contentType = JSON_CONTENT_TYPE;

        HttpOutputMessage output = createMock(HttpOutputMessage.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expect(output.getBody()).andReturn(baos);

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(output.getHeaders()).andReturn(headers).anyTimes();

        headers.setContentType(contentType);
        expectLastCall();
        headers.setContentLength(expected.getBytes(encoding).length);
        expectLastCall();

        // Execution
        replay(headers, output);
        instance.write(JSOG.parse(expected), contentType, output);

        // Verification
        verify(headers, output);
        assertEquals(expected, baos.toString(encoding));
    }

    @Test
    public void testWriteNoDefaultContentTypeCharset() throws Exception {

        // Setup
        instance.setOutputContentType(MediaType.APPLICATION_JSON);

        String expected = "\"foobar\"";
        String encoding = "ISO-8859-1";

        HttpOutputMessage output = createMock(HttpOutputMessage.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expect(output.getBody()).andReturn(baos);

        HttpHeaders headers = createMock(HttpHeaders.class);
        expect(output.getHeaders()).andReturn(headers).anyTimes();

        headers.setContentType(JSON_CONTENT_TYPE);
        expectLastCall();
        headers.setContentLength(expected.getBytes(encoding).length);
        expectLastCall();

        // Execution
        replay(headers, output);
        instance.write(JSOG.parse(expected), JSON_CONTENT_TYPE, output);

        // Verification
        verify(headers, output);
        assertEquals(expected, baos.toString(encoding));
    }

}
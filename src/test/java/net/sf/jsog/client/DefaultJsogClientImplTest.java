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

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.Header;
import java.io.UnsupportedEncodingException;
import net.sf.jsog.JSOG;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.easymock.Capture;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author jrodriguez
 */
public class DefaultJsogClientImplTest {

    private StringEntity createStringEntity(JSOG jsog) throws UnsupportedEncodingException {
        return createStringEntity(jsog.toString());
    }

    private StringEntity createStringEntity(String string) throws UnsupportedEncodingException {
        return new StringEntity(string, "UTF-8");
    }
    
    private StatusLine createStatusLine(int code, String reason) {
        ProtocolVersion version = new ProtocolVersion("HTTP", 1, 1);
        return new BasicStatusLine(version, code, reason);
    }

    @Test
    public void testHeadersCopied() throws Exception {

        // Create a map of headers
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("foo", "bar");

        // Set it as the instance's headers
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl();
        instance.setHeaders(headers);

        // Make sure changes to the map don't affect the client
        headers.put("bar", "baz");
        assertFalse(instance.getHeaders().containsKey("bar"));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testHeadersReadOnly() throws Exception {

        // Get the instance's headers
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl();
        Map<String, String> headers = instance.getHeaders();

        headers.put("foo", "bar");
    }

    @Test
    public void testGetDefaultContentTypeAndCharset() throws Exception {

        String url = "http://www.example.com";
        JSOG expected = JSOG.object("foo", "bar");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expected));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        JSOG actual = instance.getJsog(url);
        assertEquals(expected, actual);

        verify(client);

        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(0, headers.length);
    }

    @Test
    public void testGetCustomContentTypeAndCharset() throws Exception {

        String url = "http://www.example.com";
        JSOG expected = JSOG.object("foo", "bar");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expected));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };
        instance.setContentType("text/plain");
        instance.setCharset("ISO-8859-1");

        // Run the test
        replay(client);

        JSOG actual = instance.getJsog(url);
        assertEquals(expected, actual);

        verify(client);

        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(0, headers.length);
    }

    @Test
    public void testGetNullCharset() throws Exception {

        String url = "http://www.example.com";
        JSOG expected = JSOG.object("foo", "bar");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expected));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };
        instance.setCharset(null);

        // Run the test
        replay(client);

        JSOG actual = instance.getJsog(url);
        assertEquals(expected, actual);

        verify(client);

        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(0, headers.length);
    }

    @Test
    public void testGet() throws Exception {

        String url = "http://www.example.com";
        JSOG expected = JSOG.object("foo", "bar");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expected));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        JSOG actual = instance.getJsog(url);
        assertEquals(expected, actual);

        verify(client);

        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(0, headers.length);
    }

    @Test(expected=JsogClientException.class)
    public void testGetIOException() throws Exception {

        String url = "http://www.example.com";

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        expect(client.execute(capture(request))).andThrow(new IOException());

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        try {
            instance.getJsog(url);
        } catch (Exception e) {
            verify(client);
            throw e;
        }

        fail("Expected an exception");
    }

    @Test(expected=Non200ResponseCodeException.class)
    public void testGetNon200Response() throws Exception {

        String url = "http://www.example.com";

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(404, "Not Found"));
        response.setEntity(createStringEntity("foo"));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        try {
            instance.getJsog(url);
        } catch (Non200ResponseCodeException e) {
            verify(client);
            assertEquals("foo", e.getContent());
            throw e;
        }

        fail("Expected an exception");
    }

    @Test
    public void testPostDefaultContentTypeAndCharset() throws Exception {

        String url = "http://www.example.com";
        JSOG expectedResult = JSOG.object("foo", "bar");
        JSOG expectedRequest = JSOG.object("baz", "qux");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpPost> request = new Capture<HttpPost>();
        
        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expectedResult));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        JSOG actual = instance.postJsog(url, expectedRequest);
        assertEquals(expectedResult, actual);

        verify(client);

        assertEquals(expectedRequest.toString(),
                EntityUtils.toString(request.getValue().getEntity()));

        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(1, headers.length);

        Header header = headers[0];
        assertEquals("application/json; charset=ISO-8859-1", header.getValue());
    }

    @Test
    public void testPostCustomContentTypeAndCharset() throws Exception {

        String url = "http://www.example.com";
        JSOG expectedResult = JSOG.object("foo", "bar");
        JSOG expectedRequest = JSOG.object("baz", "qux");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpPost> request = new Capture<HttpPost>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expectedResult));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };
        instance.setContentType("text/plain");
        instance.setCharset("UTF-8");

        // Run the test
        replay(client);

        JSOG actual = instance.postJsog(url, expectedRequest);
        assertEquals(expectedResult, actual);

        verify(client);

        assertEquals(expectedRequest.toString(),
                EntityUtils.toString(request.getValue().getEntity()));

        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(1, headers.length);

        Header header = headers[0];
        assertEquals("text/plain; charset=UTF-8", header.getValue());
    }

    @Test
    public void testPostNullCharset() throws Exception {

        String url = "http://www.example.com";
        JSOG expectedResult = JSOG.object("foo", "bar");
        JSOG expectedRequest = JSOG.object("baz", "qux");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpPost> request = new Capture<HttpPost>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expectedResult));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };
        instance.setCharset(null);

        // Run the test
        replay(client);

        JSOG actual = instance.postJsog(url, expectedRequest);
        assertEquals(expectedResult, actual);

        verify(client);

        assertEquals(expectedRequest.toString(),
                EntityUtils.toString(request.getValue().getEntity()));

        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(1, headers.length);

        Header header = headers[0];
        assertEquals("application/json", header.getValue());
    }

    @Test
    public void testPost() throws Exception {

        String url = "http://www.example.com";
        JSOG expectedResult = JSOG.object("foo", "bar");
        JSOG expectedRequest = JSOG.object("baz", "qux");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpPost> request = new Capture<HttpPost>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expectedResult));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        JSOG actual = instance.postJsog(url, expectedRequest);
        assertEquals(expectedResult, actual);

        verify(client);

        assertEquals(expectedRequest.toString(),
                EntityUtils.toString(request.getValue().getEntity()));

        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(1, headers.length);

        Header header = headers[0];
        assertEquals("application/json; charset=ISO-8859-1", header.getValue());
    }

    @Test
    public void testPostHeaders() throws Exception {

        String url = "http://www.example.com";
        JSOG expectedResult = JSOG.object("foo", "bar");
        JSOG expectedRequest = JSOG.object("baz", "qux");

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpPost> request = new Capture<HttpPost>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity(expectedResult));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };
        instance.setHeaders(new HashMap<String, String>() {{
            put("foo", "bar");
        }});

        // Run the test
        replay(client);

        JSOG actual = instance.postJsog(url, expectedRequest);
        assertEquals(expectedResult, actual);

        verify(client);

        assertEquals(expectedRequest.toString(),
                EntityUtils.toString(request.getValue().getEntity()));

        // Check the content-type header
        Header[] headers = request.getValue().getHeaders("Content-Type");
        assertEquals(1, headers.length);

        Header header = headers[0];
        assertEquals("application/json; charset=ISO-8859-1", header.getValue());

        // Check the custom header "foo"
        headers = request.getValue().getHeaders("foo");
        assertEquals(1, headers.length);

        header = headers[0];
        assertEquals("bar", header.getValue());

        // Check the instance's headers
        assertEquals("bar", instance.getHeaders().get("foo"));
    }

    @Test(expected=JsogClientException.class)
    public void testPostIOException() throws Exception {

        String url = "http://www.example.com";

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        expect(client.execute(capture(request))).andThrow(new IOException());

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        try {
            instance.postJsog(url, JSOG.object("foo", "bar"));
        } catch (Exception e) {
            verify(client);
            throw e;
        }

        fail("Expected an exception");
    }

    @Test(expected=Non200ResponseCodeException.class)
    public void testPostNon200Response() throws Exception {

        String url = "http://www.example.com";

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(404, "Not Found"));
        response.setEntity(createStringEntity("foo"));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        try {
            instance.postJsog(url, JSOG.object("foo", "bar"));
        } catch (Non200ResponseCodeException e) {
            verify(client);
            assertEquals("foo", e.getContent());
            throw e;
        }

        fail("Expected an exception");
    }

    @Test(expected=InvalidJsogException.class)
    public void testPostInvalidJSOG() throws Exception {

        String url = "http://www.example.com";

        // Create and prepare the mock
        final HttpClient client = createMock(HttpClient.class);
        Capture<HttpGet> request = new Capture<HttpGet>();

        HttpResponse response = new BasicHttpResponse(createStatusLine(200, "OK"));
        response.setEntity(createStringEntity("{"));
        expect(client.execute(capture(request))).andReturn(response);

        // Create the instance
        DefaultJsogClientImpl instance = new DefaultJsogClientImpl() {
            @Override
            protected HttpClient getClient() {
                return client;
            }
        };

        // Run the test
        replay(client);

        try {
            instance.postJsog(url, JSOG.object("foo", "bar"));
        } catch (InvalidJsogException e) {
            assertEquals("{", e.getContent());
            verify(client);
            throw e;
        }

        fail("Expected an exception");
    }

}


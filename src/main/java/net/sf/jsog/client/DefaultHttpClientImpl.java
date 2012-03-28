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
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

/**
 * The default HttpClient implementation.
 *
 * <p>
 * This class implements a thread-safe HttpClient built on the Apache Commons
 * HttpClient project. It provides methods to set connection and socket
 * timeouts, as well as the request Content-Type header and character set.
 * <p>
 *
 * <p>
 * While a general HTTP client is somewhat out of scope, JsogClient uses this
 * class, and access to the {@link #get(String)} and
 * {@link #post(String, String)} is too convenient not to include.
 * </p>
 * @author jrodriguez
 */
public class DefaultHttpClientImpl implements net.sf.jsog.client.HttpClient {

    /**
     * The global HTTP parameters for the client.
     */
    private HttpParams params = new BasicHttpParams();

    /**
     * Manages reusable connections.
     */
    private ClientConnectionManager conman;

    /**
     * The HTTP client instance.
     */
    private volatile HttpClient client;

    /**
     * The default value of the Content-Type header.
     */
    private String contentType = "application/json";

    /**
     * The value of the charset parameter.
     */
    private String charset = "ISO-8859-1";

    /**
     * Additional headers to set.
     */
    private Map<String, String> headers = new HashMap<String, String>();

    /**
     * Constructs a new DefaultJsogClientImpl.
     */
    public DefaultHttpClientImpl() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme(
                "http", PlainSocketFactory.getSocketFactory(), 80));

        schemeRegistry.register(new Scheme(
                "https", SSLSocketFactory.getSocketFactory(), 443));
        conman = new ThreadSafeClientConnManager(params, schemeRegistry);
    }

    /**
     * Sets the connection timeout on the client.
     * @param timeout the timeout in milliseconds.
     * @see HttpConnectionParams#setConnectionTimeout(HttpParams, int)
     */
    public final synchronized void setConnectionTimeout(final int timeout) {
        HttpConnectionParams.setConnectionTimeout(params, timeout);
    }

    /**
     * Sets the socket timeout on the client.
     * @param timeout the timeout in milliseconds.
     * @see HttpConnectionParams#setSoTimeout(HttpParams, int)
     */
    public final synchronized void setSocketTimeout(final int timeout) {
        HttpConnectionParams.setSoTimeout(params, timeout);
    }

    /**
     * Sets the content type header value.
     *
     * This value is only used when calling {@link #post(String, String)}, it
     * defaults to "application/json".
     * @param contentType the default value of the Content-Type header.
     */
    public final synchronized void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * The value of the charset parameter of the content type header.
     *
     * This value is only used when calling {@link #post(String, String)},
     * it defaults to "UTF-8".
     * @param charset Sets the value of the charset parameter of the content
     * type header.
     */
    public final synchronized void setCharset(final String charset) {
        HttpProtocolParams.setContentCharset(params, charset);
        this.charset = charset;
    }

    /**
     * Gets the additional headers that will be applied to all requests.
     * @return a read-only map of the headers.
     */
    public final Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Sets additional headers that should be applied to all requests.
     *
     * The values of the map will be copied. Each call of this method removes
     * the headers that were previously set.
     * @param headers the headers to apply.
     */
    public final synchronized void setHeaders(final Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
    }

    /**
     * Sets the value of the User-Agent header, if any.
     *
     * @param userAgent the user agent string.
     */
    public final synchronized void setUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(params, userAgent);
    }

    /**
     * Gets the HttpClient instance.
     * @return the HttpClient instance.
     */
    protected HttpClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new DefaultHttpClient(conman, params);
                }
            }
        }

        return client;
    }

    @Override
    public final String get(final String url) {

        // Create the request
        HttpGet request = new HttpGet(url);

        // Execute the request
        return execute(request);
    }

    @Override
    public final String post(final String url, final String data) {

        // Create the request
        HttpPost request = new HttpPost(url);

        // Set the request data
        try {
            StringEntity entity = new StringEntity(data.toString(), charset);
            request.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            throw new JsogClientException(
                    "Unsupported encoding: " + charset, e);
        }

        // Set the content-type header.
        if (charset != null) {
            request.setHeader("Content-type", contentType
                            + "; charset=" + charset);
        } else {
            request.setHeader("Content-type", contentType);
        }

        // Execute the request
        return execute(request);
    }

    /**
     * Executes a request and returns the resulting String.
     * @param request the request to execute.
     * @return the raw content string.
     * @throws JsogClientException if the request fails.
     */
    private synchronized String execute(final HttpUriRequest request) {
        request.setParams(params);

        // Set the request's headers
        for (Entry<String, String> header : headers.entrySet()) {
            request.setHeader(header.getKey(), header.getValue());
        }

        // Execute the request and get it's content
        HttpResponse response;
        String content;
        try {

            // Execute the request
            response = getClient().execute(request);

            // Get the response content
            content = EntityUtils.toString(response.getEntity(), charset);
        } catch (IOException e) {
            throw new JsogClientException("Get request failed.", e);
        }

        // Check the response code
        StatusLine sl = response.getStatusLine();
        if (sl.getStatusCode() != 200) {
            throw new Non200ResponseCodeException(
                    sl.getStatusCode(),
                    sl.getReasonPhrase(),
                    content);
        }

        return content;
    }

}

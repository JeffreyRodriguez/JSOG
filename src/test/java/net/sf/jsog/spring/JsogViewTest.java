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

import net.sf.jsog.factory.bean.BeanJsogFactory;
import java.util.HashMap;
import java.util.Map;
import org.easymock.Capture;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import javax.servlet.ServletOutputStream;
import net.sf.jsog.JSOG;
import org.springframework.http.MediaType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author jrodriguez
 */
public class JsogViewTest {
    
    public static class TestBean {
        private String foo = "foovalue";
        private String bar = "barvalue";

        public String getFoo() {
            return foo;
        }

        public String getBar() {
            return bar;
        }
    }


    JsogView instance;
    HttpServletRequest request;
    HttpServletResponse response;

    @Before
    public void setUp() {
        instance = new JsogView();
        request = createMock(HttpServletRequest.class);
        response = createMock(HttpServletResponse.class);
    }

    @Test
    public void testRenderMergedOutputModel() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "ISO-8859-1"; // Default encoding
        JSOG expected = new JSOG("foobar");
        MediaType contentType = MediaType.APPLICATION_JSON;


        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("JSOG", expected);

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter("callback")).andReturn(null);

        // Execution
        replay(request, response, sos);

        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        JSOG actual = JSOG.parse(new String(out.getValue(), encoding));
        assertEquals(actual, expected);
    }

    @Test
    public void testRenderMergedOutputModelCustomContentType() throws Exception {
        
        // Setup
        String encoding = "ISO-8859-1"; // Default encoding
        JSOG expected = new JSOG("foobar");
        MediaType contentType = MediaType.TEXT_PLAIN;
        instance.setOutputContentType(contentType);


        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("JSOG", expected);

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter("callback")).andReturn(null);

        // Execution
        replay(request, response, sos);

        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        JSOG actual = JSOG.parse(new String(out.getValue(), encoding));
        assertEquals(actual, expected);
    }

    @Test
    public void testRenderMergedOutputModelCustomEncodingString() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "UTF-8";
        JSOG expected = new JSOG("foobar");
        MediaType contentType = MediaType.APPLICATION_JSON;

        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("JSOG", expected);

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter("callback")).andReturn(null);

        // Execution
        replay(request, response, sos);
        instance.setEncoding(encoding);
        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        JSOG actual = JSOG.parse(new String(out.getValue(), encoding));
        assertEquals(actual, expected);
    }

    @Test
    public void testRenderMergedOutputModelCustomEncodingCharset() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "UTF-8";
        JSOG expected = new JSOG("foobar");
        MediaType contentType = MediaType.APPLICATION_JSON;

        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("JSOG", expected);

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter("callback")).andReturn(null);

        // Execution
        replay(request, response, sos);
        instance.setEncoding(Charset.forName(encoding));
        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        JSOG actual = JSOG.parse(new String(out.getValue(), encoding));
        assertEquals(actual, expected);
    }

    @Test
    public void testRenderMergedOutputModelJSONP() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "ISO-8859-1"; // Default encoding
        String callback = "foo";
        JSOG expectedJson = new JSOG("foobar");
        String expected = callback + "(" + expectedJson + ")";
        MediaType contentType = MediaType.APPLICATION_JSON;


        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("JSOG", expectedJson);

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter("callback")).andReturn(callback);

        // Execution
        replay(request, response, sos);

        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        String actual = new String(out.getValue(), encoding);
        assertEquals(actual, expected);
    }

    @Test
    public void testRenderMergedOutputModelJSONPCustomCallback() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "ISO-8859-1"; // Default encoding
        String callback = "foo";
        String callbackParamName = "bar";
        JSOG expectedJson = new JSOG("foobar");
        String expected = callback + "(" + expectedJson + ")";
        MediaType contentType = MediaType.APPLICATION_JSON;


        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("JSOG", expectedJson);

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter(callbackParamName)).andReturn(callback);

        // Execution
        replay(request, response, sos);

        instance.setJsonpCallbackParam(callbackParamName);
        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        String actual = new String(out.getValue(), encoding);
        assertEquals(actual, expected);
    }

    /**
     * This tests that complex models can be rendered properly.
     * A complex model is one that doesn't have "JSOG" as it's only key (excepting BindingResult values).
     * @throws Exception
     */
    @Test
    public void testRenderMergedOutputModelComplex() throws Exception {

        // TODO: Make this test more robust

        // Setup
        String encoding = "ISO-8859-1"; // Default encoding
        JSOG expected = JSOG.object("foo", "foovalue")
                            .put("bar", "barvalue")
                            .put("obj", JSOG.object());
        MediaType contentType = MediaType.APPLICATION_JSON;


        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("foo", "foovalue");
        model.put("bar", "barvalue");
        model.put("obj", JSOG.object());

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter("callback")).andReturn(null);

        // Execution
        replay(request, response, sos);

        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        JSOG actual = JSOG.parse(new String(out.getValue(), encoding));
        assertEquals(actual, expected);
    }

    @Test
    public void testRenderMergedOutputModelBean() throws Exception {

        // Setup
        String encoding = "ISO-8859-1"; // Default encoding
        JSOG expected = JSOG.object("bean", JSOG.object()
                                    .put("foo", "foovalue")
                                    .put("bar", "barvalue"));
        MediaType contentType = MediaType.APPLICATION_JSON;


        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("bean", new TestBean());

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter("callback")).andReturn(null);

        // Execution
        replay(request, response, sos);

        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        JSOG actual = JSOG.parse(new String(out.getValue(), encoding));
        assertEquals(actual, expected);
    }

    @Test
    public void testRenderMergedOutputModelBeanCustomBeanJsogFactory() throws Exception {

        // Setup
        String encoding = "ISO-8859-1"; // Default encoding
        JSOG beanJsog = JSOG.object("foo", "foovalue").put("bar", "barvalue");
        JSOG expected = JSOG.object("bean", beanJsog);
        MediaType contentType = MediaType.APPLICATION_JSON;

        BeanJsogFactory bjf = createMock(BeanJsogFactory.class);
        instance.setBeanJsogFactory(bjf);
        
        expect(bjf.create(isA(TestBean.class))).andReturn(beanJsog);

        // Setup the model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("bean", new TestBean());

        // Setup the output stream
        ServletOutputStream sos = createMock(ServletOutputStream.class);
        expect(response.getOutputStream()).andReturn(sos);

        Capture<byte[]> out = new Capture<byte[]>();
        sos.write(capture(out));
        expectLastCall();

        sos.flush();
        expectLastCall();

        sos.close();
        expectLastCall();

        response.setContentType(contentType.toString());
        expectLastCall();

        response.setCharacterEncoding(encoding);
        expectLastCall();

        response.setContentLength(expected.toString().getBytes(encoding).length);
        expectLastCall();

        expect(request.getParameter("callback")).andReturn(null);

        // Execution
        replay(request, response, sos, bjf);

        instance.renderMergedOutputModel(model, request, response);

        // Verification
        verify(request, response, sos, bjf);
        assertTrue(out.hasCaptured());

        // Parse the resulting value
        JSOG actual = JSOG.parse(new String(out.getValue(), encoding));
        assertEquals(actual, expected);
    }

}
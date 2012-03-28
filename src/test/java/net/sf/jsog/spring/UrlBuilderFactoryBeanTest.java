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

import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import net.sf.jsog.spring.UrlBuilderFactoryBean.Parameter;
import net.sf.jsog.client.UrlBuilder;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jrodriguez
 */
public class UrlBuilderFactoryBeanTest {
    private static final String URL = "http://www.example.com/";
    
    private UrlBuilderFactoryBean instance;

    @Before
    public void setUp() {
        instance = new UrlBuilderFactoryBean();
    }

    @Test
    public void testSetBaseUrl() {
        instance.setBaseUrl(URL);
        UrlBuilder bean = instance.getObject();
        assertEquals(URL, bean.getBase());
        assertEquals(URL, bean.toString());
    }

    @Test
    public void testSetParameters() {
        instance.setParameters(Arrays.asList(
                new Parameter("foo", "bar"),
                new Parameter("foo", "baz"),
                new Parameter("qux", "quux")));
        UrlBuilder bean = instance.getObject();
        
        assertEquals(bean.get("foo"), Arrays.asList("bar", "baz"));
        assertEquals(bean.get("qux"), Arrays.asList("quux"));
    }

    @Test
    public void testSetParametersEmpty() {
        instance.setParameters((List<Parameter>) Collections.EMPTY_LIST);
        instance.setBaseUrl(URL);
        UrlBuilder bean = instance.getObject();
        
        assertEquals(URL, bean.toString());
    }

    @Test
    public void testGetObject() throws Exception {
        UrlBuilder bean = instance.getObject();
        assertNotNull(bean);
    }

    @Test
    public void testGetObjectType() {
        assertTrue(UrlBuilder.class.isAssignableFrom(instance.getObjectType()));
    }

    @Test
    public void testIsSingleton() {
        assertTrue(instance.isSingleton());
    }

}
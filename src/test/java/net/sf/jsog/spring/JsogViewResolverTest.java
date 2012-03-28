/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jsog.spring;

import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.web.servlet.View;

/**
 *
 * @author jrodriguez
 */
public class JsogViewResolverTest {

    private JsogViewResolver instance;

    @Before
    public void setUp() {
        instance = new JsogViewResolver();
    }

    @Test
    public void testDefaultViewName() {
        JsogView result = instance.resolveViewName("foo", null);
        assertNotNull(result);

        result = instance.resolveViewName("bar", null);
        assertNotNull(result);
    }

    @Test
    public void testNullViewName() {
        instance.setViewName(null);

        JsogView result = instance.resolveViewName("foo", null);
        assertNotNull(result);

        result = instance.resolveViewName("bar", null);
        assertNotNull(result);
    }

    @Test
    public void testCustomViewName() {
        instance.setViewName("foo");

        JsogView result = instance.resolveViewName("foo", null);
        assertNotNull(result);

        result = instance.resolveViewName("bar", null);
        assertNull(result);
    }

    @Test
    public void testCustomView() {
        JsogView expected = new JsogView();
        instance.setView(expected);

        JsogView acutal = instance.resolveViewName("foo", null);
        assertSame(expected, acutal);
    }

}
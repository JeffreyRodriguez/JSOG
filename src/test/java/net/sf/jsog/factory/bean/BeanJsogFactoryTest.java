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
package net.sf.jsog.factory.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import net.sf.jsog.JSOG;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jrodriguez
 */
public class BeanJsogFactoryTest {

    BeanJsogFactory instance;

    @Before
    public void setUp() {
        instance = BeanJsogFactory.getSingleton();
    }

    public static class Foo {

        private String foo = "foo";

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        private boolean bar = true;

        public boolean isBar() {
            return bar;
        }

        public void setBar(boolean bar) {
            this.bar = bar;
        }

    }

    public static class Bar {
        private Foo foo = new Foo();

        public Foo getFoo() {
            return foo;
        }

        public void setFoo(Foo foo) {
            this.foo = foo;
        }

        private String bar;

        public String getBar() {
            return bar;
        }
    }

    public static class Qux {
        private Qux qux;

        public Qux getQux() {
            return qux;
        }
    }

    @Test
    public void testCreateNull() throws Exception {
        JSOG expected = new JSOG();
        JSOG actual = instance.create((Object) null);
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateBasic() throws Exception {
        JSOG expected = JSOG.object("foo", "foo").put("bar", true);
        JSOG actual = instance.create(new Foo());
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateNested() throws Exception {
        JSOG expected = JSOG.object("bar", null)
                            .put("foo", 
                                JSOG.object("foo", "foo")
                                    .put("bar", true));
        JSOG actual = instance.create(new Bar());
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateListNullValue() throws Exception {
        JSOG expected = JSOG.array((Object) null);
        JSOG actual = instance.create(Arrays.asList((Object) null));
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateListNull() throws Exception {
        JSOG expected = JSOG.array();
        JSOG actual = instance.create((List<?>) null);
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateListBasic() throws Exception {
        JSOG expected = JSOG.array(JSOG.object("foo", "foo").put("bar", true));
        JSOG actual = instance.create(Arrays.asList(new Foo()));
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateListNested() throws Exception {
        JSOG expected = JSOG.array(
                JSOG.object("bar", null)
                            .put("foo",
                                JSOG.object("foo", "foo")
                                    .put("bar", true)),
                JSOG.object("foo", "foo")
                .put("bar", true));
        JSOG actual = instance.create(Arrays.asList(new Bar(), new Foo()));
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateMapNullValue() throws Exception {
        JSOG expected = JSOG.object("foo", null);
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("foo", null);
        }};
        JSOG actual = instance.create(map);
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateMapNull() throws Exception {
        JSOG expected = JSOG.object();
        JSOG actual = instance.create((Map<String, ?>) null);
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateMapBasic() throws Exception {
        JSOG expected = JSOG.object("foo",
                                    JSOG.object("foo", "foo")
                                        .put("bar", true));
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("foo", new Foo());
        }};
        JSOG actual = instance.create(map);
        assertEquals(expected, actual);
    }

    @Test
    public void testCircularReference() throws Exception {
        try {
            Qux qux1 = new Qux();
            Qux qux2 = new Qux();
            qux1.qux = qux2;
            qux2.qux = qux1;
            instance.create(qux1);
            fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Circular reference"));
        }
    }

}
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

import net.sf.jsog.JSOG;
import java.beans.PropertyDescriptor;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author jrodriguez
 */
public class ClassNamePropertyFilterTest {

    private static class Foo {
        String foo;

        public String getFoo() {
            return foo;
        }

        Bar bar = new Bar();

        public Bar getBar() {
            return bar;
        }
    }

    private static class Bar {
        int qux = 0;

        public int getQux() {
            return qux;
        }

        Integer quux = 0;

        public Integer getQuux() {
            return quux;
        }

        String yyz = "yyz";

        public String getYyz() {
            return yyz;
        }
    }

    private ClassNamePropertyFilter instance;

    private BeanJsogFactory bjf;

    @Before
    public void setUp() {
        bjf = BeanJsogFactory.getSingleton();
    }

    @Test
    public void testPrimitivesTrue() throws Exception {
        instance = new ClassNamePropertyFilter(true);
        JSOG result = bjf.create(new Bar(), instance);
        assertEquals(JSOG.object("qux", 0), result);
    }

    @Test
    public void testDefault() throws Exception {
        instance = new ClassNamePropertyFilter(false);
        JSOG result = bjf.create(new Foo(), instance);
        assertEquals(JSOG.object(), result);
    }

    @Test
    public void testIncludeSingle() throws Exception {
        instance = new ClassNamePropertyFilter(false);
        instance.include("java.lang.Integer");
        JSOG result = bjf.create(new Bar(), instance);
        assertEquals(JSOG.object("quux", 0), result);
    }

    @Test
    public void testIncludeMultiple() throws Exception {
        instance = new ClassNamePropertyFilter(false);
        instance.include("java.lang.Integer");
        instance.include("java.lang.String");
        JSOG result = bjf.create(new Bar(), instance);
        assertEquals(JSOG.object("yyz", "yyz")
                         .put("quux", 0), result);
    }

    @Test
    public void testIncludeWildcard() throws Exception {
        instance = new ClassNamePropertyFilter(false);
        instance.include("java.lang.*");
        JSOG result = bjf.create(new Bar(), instance);
        assertEquals(JSOG.object("yyz", "yyz")
                         .put("quux", 0), result);
    }

    @Test
    public void testIncludeMultipleWithPrimitives() throws Exception {
        instance = new ClassNamePropertyFilter(true);
        instance.include("java.lang.Integer", "java.lang.String");
        JSOG result = bjf.create(new Bar(), instance);
        assertEquals(JSOG.object("yyz", "yyz")
                         .put("qux", 0)
                         .put("quux", 0), result);
    }

    @Test
    public void testMixedMultipleOrdered() throws Exception {
        instance = new ClassNamePropertyFilter(false);
        instance.exclude("foo.bar.*");
        instance.exclude("java.lang.Integer");
        instance.include("java.lang.*");
        JSOG result = bjf.create(new Bar(), instance);
        assertEquals(JSOG.object("yyz", "yyz"), result);
    }

}
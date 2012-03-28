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

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jrodriguez
 */
public class UrlBuilderTest {

    private static final String URL = "http://www.example.com/";

    @Test
    public void testAdd() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        assertEquals(URL + "?foo=bar", instance.toString());
    }

    @Test
    public void testAddTwoValues() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        instance.add("foo", "baz");
        assertEquals(URL + "?foo=bar&foo=baz", instance.toString());
    }

    @Test
    public void testAddMultiple() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        instance.add("bar", "baz");
        assertEquals(URL + "?foo=bar&bar=baz", instance.toString());
    }

    @Test
    public void testAddChaining() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar")
                .add("foo", "baz");
        assertEquals(URL + "?foo=bar&foo=baz", instance.toString());
    }

    @Test
    public void testSetCollection() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", Arrays.asList("bar", "baz"));
        assertEquals(URL + "?foo=bar&foo=baz", instance.toString());
    }

    @Test
    public void testSetCollectionReplace() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", "bar")
                .set("foo", Arrays.asList("qux", "quux"));
        assertEquals(URL + "?foo=qux&foo=quux", instance.toString());
    }

    @Test
    public void testSetCollectionChaining() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", Arrays.asList("bar", "baz"));
        instance.set("qux", Arrays.asList("quux"));
        assertEquals(URL + "?foo=bar&foo=baz&qux=quux", instance.toString());
    }

    @Test
    public void testSet() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", "bar");
        assertEquals(URL + "?foo=bar", instance.toString());
    }

    @Test
    public void testSetChaining() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", "bar")
                .set("bar", "baz");
        assertEquals(URL + "?foo=bar&bar=baz", instance.toString());
    }

    @Test
    public void testSetReplace() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", "bar");
        instance.set("foo", "baz");
        assertEquals(URL + "?foo=baz", instance.toString());
    }

    @Test
    public void testClear() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", "bar");
        instance.clear();
        assertEquals(URL, instance.toString());
    }

    @Test
    public void testRemove_String() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        instance.remove("foo");
        assertEquals(URL, instance.toString());
    }

    @Test
    public void testRemove_StringMultiple() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        instance.add("bar", "baz");
        instance.remove("foo");
        assertEquals(URL + "?bar=baz", instance.toString());
    }

    @Test
    public void testRemove_StringMultipleValues() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        instance.add("foo", "baz");
        instance.remove("foo", "bar");
        assertEquals(URL + "?foo=baz", instance.toString());
    }

    @Test
    public void testRemove_String_String() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        instance.add("foo", "baz");
        instance.add("bar", "baz");
        instance.remove("foo", "baz");
        assertEquals(URL + "?foo=bar&bar=baz", instance.toString());
    }

    @Test
    public void testGet() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", "bar");
        assertEquals("bar", instance.get("foo").get(0));
    }

    @Test
    public void testGetWithNoValues() {
        UrlBuilder instance = new UrlBuilder(URL);
        assertTrue(instance.get("foo").isEmpty());
    }

    @Test
    public void testHasFalse() {
        UrlBuilder instance = new UrlBuilder(URL);
        assertFalse(instance.has("foo"));
    }

    @Test
    public void testHasTrue() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", "bar");
        assertTrue(instance.has("foo"));
    }

    @Test
    public void testGetBase() {
        UrlBuilder instance = new UrlBuilder(URL);
        assertEquals(URL, instance.getBase());
    }

    @Test
    public void testSetBase() {
        UrlBuilder instance = new UrlBuilder(URL);
        
        String newUrl = "http://set.base/";
        instance.setBase(newUrl);
        
        assertEquals(newUrl, instance.toString());
    }

    @Test
    public void testToString_0args() {
        UrlBuilder instance = new UrlBuilder(URL);

        assertEquals(URL, instance.toString());
    }

    @Test
    public void testToString_0argsWithSingleParam() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.set("foo", "bar");

        assertEquals(URL + "?foo=bar", instance.toString());
    }

    @Test
    public void testToString_0argsWithSingleParamTwoValues() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        instance.add("foo", "baz");

        assertEquals(URL + "?foo=bar&foo=baz", instance.toString());
    }

    @Test
    public void testToString_0argsWithTwoParams() {
        UrlBuilder instance = new UrlBuilder(URL);
        instance.add("foo", "bar");
        instance.add("bar", "baz");

        assertEquals(URL + "?foo=bar&bar=baz", instance.toString());
    }

    @Test
    public void testToString_StringArr() {
        UrlBuilder instance = new UrlBuilder(URL + "{0}");

        assertEquals(URL + "foo", instance.toString("foo"));
    }

    @Test
    public void testToString_StringArrMultiple() {
        UrlBuilder instance = new UrlBuilder(URL + "{0}/{1}");

        assertEquals(URL + "foo/bar", instance.toString("foo", "bar"));
    }

    @Test
    public void testToString_StringArrWithSingleParam() {
        UrlBuilder instance = new UrlBuilder(URL + "{0}");
        instance.set("foo", "bar");

        assertEquals(URL + "foo?foo=bar", instance.toString("foo"));
    }

    @Test
    public void testToString_StringArrWithSingleParamTwoValues() {
        UrlBuilder instance = new UrlBuilder(URL + "{0}");
        instance.add("foo", "bar");
        instance.add("foo", "baz");

        assertEquals(URL + "foo?foo=bar&foo=baz", instance.toString("foo"));
    }

    @Test
    public void testToString_StringArrWithTwoParams() {
        UrlBuilder instance = new UrlBuilder(URL + "{0}");
        instance.add("foo", "bar");
        instance.add("bar", "baz");

        assertEquals(URL + "foo?foo=bar&bar=baz", instance.toString("foo"));
    }

    @Test
    public void testCloneUrl() {
        UrlBuilder original = new UrlBuilder(URL);
        UrlBuilder clone    = original.clone();

        assertEquals(original.toString(), clone.toString());
    }

    @Test
    public void testCloneParameters() {
        UrlBuilder original = new UrlBuilder(URL);
        original.add("foo", "bar");
        UrlBuilder clone    = original.clone();

        assertEquals(original.get("foo"), clone.get("foo"));
    }

    @Test
    public void testCloneParametersSeparate() {
        UrlBuilder original = new UrlBuilder(URL);
        UrlBuilder clone    = original.clone();

        clone.set("foo", "bar");
        assertFalse(original.has("foo"));

        original.set("bar", "baz");
        assertFalse(clone.has("bar"));
    }

}
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
package net.sf.jsog;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * TODO: More primitive tests.
 * @author <a href="mailto:jeff@jeffrodriguez.com">Jeff Rodriguez</a>
 */
public class JSOGTest {

    private enum TestEnum {
        test, foo, bar, baz, qux, quux;
    }

    private ObjectMapper om = new ObjectMapper();

    @Test
    public void testMergeChaining() {
        JSOG dst = new JSOG();
        dst.merge(JSOG.object("foo", "bar")).merge(JSOG.object("bar", "baz"));
        assertEquals(JSOG.object("foo", "bar").put("bar", "baz"), dst);
    }

    @Test
    public void testMergeInstanceComplex() {
        JSOG dst = JSOG.object("foo", JSOG.array("foo"));
        JSOG src = JSOG.object("foo", JSOG.array("bar", "baz"));
        dst.merge(src);

        assertEquals("foo", dst.get("foo").get(0).getValue());
        assertEquals("bar", dst.get("foo").get(1).getValue());
        assertEquals("baz", dst.get("foo").get(2).getValue());
    }

    @Test
    public void testMergePrimitive() {
        JSOG dst = new JSOG("foo");
        JSOG src = new JSOG("bar");
        JSOG.merge(src, dst);

        assertEquals("bar", dst.getValue());
    }

    @Test
    public void testMergeObjectOfPrimitives() {
        JSOG dst = JSOG.object();
        JSOG src = JSOG.object("foo", "bar").put("bar", "baz");
        JSOG.merge(src, dst);

        assertEquals("bar", dst.get("foo").getValue());
        assertEquals("baz", dst.get("bar").getValue());
    }

    @Test
    public void testMergeObjectsOfPrimitives() {
        JSOG dst = JSOG.object("foo", "bar");
        JSOG src = JSOG.object("bar", "baz");
        JSOG.merge(src, dst);

        assertEquals("bar", dst.get("foo").getValue());
        assertEquals("baz", dst.get("bar").getValue());
    }

    @Test
    public void testMergeDeepObjects() {
        JSOG dst =
                JSOG.object("foo",
                    JSOG.object("bar",
                        JSOG.object("baz",
                            "baz")));
        JSOG src =
                JSOG.object("foo",
                    JSOG.object("bar",
                        JSOG.object("qux",
                            "qux")));
        JSOG.merge(src, dst);

        assertEquals("baz", dst.get("foo").get("bar").get("baz").getValue());
        assertEquals("qux", dst.get("foo").get("bar").get("qux").getValue());
    }

    @Test
    public void testMergeArrayOfPrimitives() {
        JSOG dst = JSOG.array();
        JSOG src = JSOG.array("foo", "bar");
        JSOG.merge(src, dst);

        assertEquals("foo", dst.get(0).getValue());
        assertEquals("bar", dst.get(1).getValue());
    }

    @Test
    public void testMergeEmptyArray() {
        JSOG dst = JSOG.object();
        JSOG src = JSOG.array();
        JSOG.merge(src, dst);

        assertEquals(JSOG.array(), dst);
    }

    @Test
    public void testMergeArrayOfArrays() {
        JSOG dst = JSOG.array();
        JSOG src = JSOG.array(JSOG.array("foo", "bar"), JSOG.array("baz", "qux"));
        JSOG.merge(src, dst);

        assertEquals("foo", dst.get(0).get(0).getValue());
        assertEquals("bar", dst.get(0).get(1).getValue());
        assertEquals("baz", dst.get(1).get(0).getValue());
        assertEquals("qux", dst.get(1).get(1).getValue());
    }

    @Test
    public void testMergeArrayOfObjects() {
        JSOG dst = JSOG.array();
        JSOG src = JSOG.array(JSOG.object("foo", "bar"), JSOG.object("baz", "qux"));
        JSOG.merge(src, dst);

        assertEquals("bar", dst.get(0).get("foo").getValue());
        assertEquals("qux", dst.get(1).get("baz").getValue());
    }

    @Test
    public void testMergeObjectsOfArraysOfPrimitives() {
        JSOG dst = JSOG.object("foo", JSOG.array("foo"));
        JSOG src = JSOG.object("foo", JSOG.array("bar", "baz"));
        JSOG.merge(src, dst);

        assertEquals("foo", dst.get("foo").get(0).getValue());
        assertEquals("bar", dst.get("foo").get(1).getValue());
        assertEquals("baz", dst.get("foo").get(2).getValue());
    }

    /**
     * Test of parse method, of class JSOG.
     */
    @Test
    public void testParseBadJson() throws Exception {
        System.out.println("testParseBadJson");

        try {
            JSOG.parse("{");
            fail("Expected an exception.");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("expected"));
        }
    }

    /**
     * Test of parse method, of class JSOG.
     */
    @Test
    public void testParseNull() throws Exception {
        System.out.println("testParseNull");
        JSOG result = JSOG.parse(null);
        assertNull(result.getValue());
    }

    /**
     * Test of parse method, of class JSOG.
     */
    @Test
    public void testParseEmptyValue() throws Exception {
        System.out.println("testParseEmptyValue");
        JSOG result = JSOG.parse("");
        assertNull(result.getValue());
    }

    /**
     * Test of parse method, of class JSOG.
     */
    @Test
    public void testParseComplex() throws Exception {
        System.out.println("testParseComplex");

        // Create the test JSOG
        JSOG jsog = JSOG.createArrayNode()
                .add(null)
                .add("test")
                .add(JSOG.createObjectNode()
                    .put("foo", "bar")
                    .put("qux", true)
                    .put("quux", false))
                // TODO: Test BigDecimal and BigInteger
                .add(Byte.MAX_VALUE)
                .add(Character.MAX_VALUE)
                .add(Short.MAX_VALUE)
                .add(Integer.MAX_VALUE)
                .add(Long.MAX_VALUE)
                .add(Float.MAX_VALUE)
                .add(Double.MAX_VALUE);

        String jsonString = jsog.toString();
        JSOG result = JSOG.parse(jsonString);
        assertEquals(jsonString, result.toString());
    }

    /**
     * Test of parse method, of class JSOG.
     */
    @Test
    public void testParseEmptyArray() throws Exception {
        System.out.println("testParseEmptyArray");
        String jsonString = "[]";
        JSOG result = JSOG.parse(jsonString);
        assertNotNull(result);
        assertEquals(JSOG.createArrayNode().toString(), result.toString());
    }

    /**
     * Test of parse method, of class JSOG.
     */
    @Test
    public void testParseEmptyObject() throws Exception {
        System.out.println("testParseEmptyObject");
        String jsonString = "{}";
        JSOG result = JSOG.parse(jsonString);
        assertNotNull(result);
        assertEquals(JSOG.createObjectNode().toString(), result.toString());
    }

    /**
     * Test of parse method, of class JSOG.
     */
    @Test
    public void testParseObject() throws Exception {
        System.out.println("testParseObject");
        String jsonString = "{\"foo\":\"bar\"}";
        JSOG result = JSOG.parse(jsonString);
        assertNotNull(result);
        assertEquals(jsonString, result.toString());
    }

    /**
     * Test of createValueNode method, of class JSOG.
     */
    @Test
    public void testCreateValueNode() {
        System.out.println("testCreateValueNode");
        JSOG result = JSOG.createValueNode();
        assertEquals(null, result.getValue());
    }

    /**
     * Test of createValueNode method, of class JSOG.
     */
    @Test
    public void testCreateValueNodeWithValue() {
        System.out.println("testCreateValueNodeWithValue");
        JSOG result = JSOG.createValueNode("test");
        assertEquals("test", result.getValue());
    }

    /**
     * Test of JSOG constructor, of class JSOG.
     */
    @Test
    public void testJSOG() {
        System.out.println("testJSOG");
        JSOG result = new JSOG();
        assertEquals(null, result.getValue());
    }

    /**
     * Test of JSOG(Object) constructor, of class JSOG.
     */
    @Test
    public void testJSOG_Object() {
        System.out.println("testJSOG_Object");
        JSOG result = new JSOG("test");
        assertEquals("test", result.getValue());
    }

    /**
     * Test of createObjectNode method, of class JSOG.
     */
    @Test
    public void testCreateObjectNode() {
        System.out.println("testCreateObjectNode");
        JSOG result = JSOG.createObjectNode();
        assertEquals("{}", result.toString());
    }

    /**
     * Test of object method, of class JSOG.
     */
    @Test
    public void testObject() {
        System.out.println("testObject");
        JSOG result = JSOG.object();
        assertTrue(result.isObject());
        assertEquals(0, result.size());
    }

    /**
     * Test of object(String,Object) method, of class JSOG.
     */
    @Test
    public void testObject_String_Object() {
        System.out.println("testObject_String_Object");
        JSOG result = JSOG.object("foo", "bar");
        assertTrue(result.isObject());
        assertEquals(1, result.size());
        assertEquals("bar", result.get("foo").getStringValue());
    }

    /**
     * Test of object(Enum,Object) method, of class JSOG.
     */
    @Test
    public void testObject_Enum_Object() {
        System.out.println("testObject_Enum_Object");
        JSOG result = JSOG.object(TestEnum.foo, "bar");
        assertTrue(result.isObject());
        assertEquals(1, result.size());
        assertEquals("bar", result.get(TestEnum.foo).getStringValue());
    }

    /**
     * Test of object(Enum,Object) method, of class JSOG.
     */
    @Test
    public void testObject_Enum_Object_null() {
        System.out.println("testObject_Enum_Object_null");
        JSOG result = JSOG.object((TestEnum) null, "bar");
        assertTrue(result.isObject());
        assertEquals(1, result.size());
        assertEquals("bar", result.get((TestEnum) null).getStringValue());
    }

    /**
     * Test of createArrayNode method, of class JSOG.
     */
    @Test
    public void testCreateArrayNode() {
        System.out.println("testCreateArrayNode");
        JSOG result = JSOG.createArrayNode();
        assertEquals("[]", result.toString());
    }

    /**
     * Test of array method, of class JSOG.
     */
    @Test
    public void testArray() {
        System.out.println("testArray");
        JSOG result = JSOG.array();
        assertEquals(0, result.size());
    }

    /**
     * Test of array(Object...) method, of class JSOG.
     */
    @Test
    public void testArray_Object_single() {
        System.out.println("testArray_Object_single");
        JSOG result = JSOG.array("foo");
        assertEquals(1, result.size());
        assertEquals("foo", result.get(0).getValue());
    }

    /**
     * Test of array(Object...) method, of class JSOG.
     */
    @Test
    public void testArray_Object_multiple() {
        System.out.println("testArray_Object_multiple");
        JSOG result = JSOG.array("foo", "bar", "baz");
        assertEquals(3, result.size());
        assertEquals("foo", result.get(0).getValue());
        assertEquals("bar", result.get(1).getValue());
        assertEquals("baz", result.get(2).getValue());
    }

    /**
     * Test of isNull method, of class JSOG.
     */
    @Test
    public void testIsNull() {
        System.out.println("testIsNull");
        JSOG instance = JSOG.createValueNode(null);
        boolean result = instance.isNull();
        assertEquals(true, result);
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_null() {
        System.out.println("testIsPrimitive_null");
        JSOG instance = JSOG.createValueNode(null);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_boolean_true() {
        System.out.println("testIsPrimitive_boolean_true");
        JSOG instance = JSOG.createValueNode(true);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_boolean_false() {
        System.out.println("testIsPrimitive_boolean_false");
        JSOG instance = JSOG.createValueNode(false);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_bigDecimal() {
        System.out.println("testIsPrimitive_bigDecimal");
        JSOG instance = JSOG.createValueNode(BigDecimal.ZERO);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_bigInteger() {
        System.out.println("testIsPrimitive_bigInteger");
        JSOG instance = JSOG.createValueNode(BigInteger.ZERO);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_byte() {
        System.out.println("testIsPrimitive_byte");
        JSOG instance = JSOG.createValueNode(Byte.MAX_VALUE);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_char() {
        System.out.println("testIsPrimitive_char");
        JSOG instance = JSOG.createValueNode(Character.MAX_VALUE);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_short() {
        System.out.println("testIsPrimitive_short");
        JSOG instance = JSOG.createValueNode(Short.MAX_VALUE);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_int() {
        System.out.println("testIsPrimitive_int");
        JSOG instance = JSOG.createValueNode(Integer.MAX_VALUE);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_long() {
        System.out.println("testIsPrimitive_long");
        JSOG instance = JSOG.createValueNode(Long.MAX_VALUE);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_float() {
        System.out.println("testIsPrimitive_float");
        JSOG instance = JSOG.createValueNode(Float.MAX_VALUE);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_double() {
        System.out.println("testIsPrimitive_double");
        JSOG instance = JSOG.createValueNode(Double.MAX_VALUE);
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_string() {
        System.out.println("testIsPrimitive_string");
        JSOG instance = JSOG.createValueNode("test");
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_string_empty() {
        System.out.println("testIsPrimitive_string_empty");
        JSOG instance = JSOG.createValueNode("");
        assertTrue(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_array() {
        System.out.println("testIsPrimitive_array");
        JSOG instance = JSOG.createArrayNode();
        assertFalse(instance.isPrimitive());
    }

    /**
     * Test of isPrimitive method, of class JSOG.
     */
    @Test
    public void testIsPrimitive_object() {
        System.out.println("testIsPrimitive_object");
        JSOG instance = JSOG.createObjectNode();
        assertFalse(instance.isPrimitive());
    }

    /**
     * Test of isArray method, of class JSOG.
     */
    @Test
    public void testIsArray_true() {
        System.out.println("testIsArray_true");
        JSOG instance = JSOG.createArrayNode();
        assertTrue(instance.isArray());
    }

    /**
     * Test of isArray method, of class JSOG.
     */
    @Test
    public void testIsArray_falseNull() {
        System.out.println("testIsArray_falseNull");
        JSOG instance = JSOG.createValueNode();
        assertFalse(instance.isArray());
    }

    /**
     * Test of isArray method, of class JSOG.
     */
    @Test
    public void testIsArray_falseObject() {
        System.out.println("testIsArray_falseObject");
        JSOG instance = JSOG.createObjectNode();
        assertFalse(instance.isArray());
    }

    /**
     * Test of isObject method, of class JSOG.
     */
    @Test
    public void testIsArray_falseString() {
        System.out.println("testIsArray_falseString");
        JSOG instance = JSOG.createValueNode("");
        assertFalse(instance.isArray());
    }

    /**
     * Test of isObject method, of class JSOG.
     */
    @Test
    public void testIsObject_true() {
        System.out.println("testIsObject_true");
        JSOG instance = JSOG.createObjectNode();
        assertTrue(instance.isObject());
    }

    /**
     * Test of isObject method, of class JSOG.
     */
    @Test
    public void testIsObject_falseNull() {
        System.out.println("testIsObject_falseNull");
        JSOG instance = JSOG.createValueNode();
        assertFalse(instance.isObject());
    }

    /**
     * Test of isObject method, of class JSOG.
     */
    @Test
    public void testIsObject_falseArray() {
        System.out.println("testIsArray_falseArray");
        JSOG instance = JSOG.createArrayNode();
        assertFalse(instance.isObject());
    }

    /**
     * Test of isObject method, of class JSOG.
     */
    @Test
    public void testIsObject_falseString() {
        System.out.println("testIsObject_falseString");
        JSOG instance = JSOG.createValueNode("");
        assertFalse(instance.isObject());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAdd() {
        System.out.println("testAdd");
        JSOG instance = JSOG.createArrayNode();
        instance.add(true);
        instance.add(false);
        assertEquals(true, instance.get(0).getValue());
        assertEquals(false, instance.get(1).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAdd_chain() {
        System.out.println("testAdd_chain");
        JSOG instance = JSOG.createArrayNode();
        instance
                .add(true)
                .add(false);
        assertEquals(true, instance.get(0).getValue());
        assertEquals(false, instance.get(1).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAdd_null() {
        System.out.println("testAdd_null");
        JSOG instance = JSOG.createArrayNode();
        instance.add(null);
        assertNull(instance.get(0).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAdd_nullJsog() {
        System.out.println("testAdd_nullJsog");
        JSOG instance = new JSOG();
        instance.add(null);
        assertNull(instance.get(0).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAdd_primitiveJsog() {
        System.out.println("testAdd_valueIsPrimitive");
        JSOG instance = JSOG.createValueNode("test");
        instance.add(null);
        assertNull(instance.get(0).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAdd_objectJsog() {
        System.out.println("testAdd_objectJsog");
        JSOG instance = JSOG.createObjectNode();
        instance.add(null);
        assertNull(instance.get(0).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAdd_valueIsJsog() {
        System.out.println("testAdd_valueIsJsog");
        JSOG instance = JSOG.array();
        instance.add(JSOG.object("foo", "bar"));
        assertEquals(JSOG.array(JSOG.object("foo", "bar")), instance);
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testAdd_valueIsIllegal() {
        System.out.println("testAdd_valueIsIllegal");
        JSOG instance = JSOG.array();
        instance.add(JSOG.class);

        fail("Expected an exception");
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAddWithIndex() {
        System.out.println("testAddWithIndex");
        JSOG instance = JSOG.createArrayNode();
        instance.add(0, true);
        instance.add(1, false);
        assertEquals(true, instance.get(0).getValue());
        assertEquals(false, instance.get(1).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test(expected=IndexOutOfBoundsException.class)
    public void testAddWithIndexOutOfRange() {
        System.out.println("testAddWithIndexOutOfRange");
        JSOG instance = JSOG.createArrayNode();

        instance.add(1, null);
        fail("Expected an exception.");
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAddWithIndex_chain() {
        System.out.println("testAddWithIndex_chain");
        JSOG instance = JSOG.createArrayNode();
        instance.add(0, true)
                .add(1, false);
        assertEquals(true, instance.get(0).getValue());
        assertEquals(false, instance.get(1).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAddWithIndex_nullJsog() {
        System.out.println("testAddWithIndex_nullJsog");
        JSOG instance = new JSOG();
        instance.add(0, null);
        assertNull(instance.get(0).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAddWithIndex_objectJsog() {
        System.out.println("testAddWithIndex_objectJsog");
        JSOG instance = JSOG.createObjectNode();
        instance.add(0, null);
        assertNull(instance.get(0).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAddWithIndex_primitiveJsog() {
        System.out.println("testAddWithIndex_primitiveJsog");
        JSOG instance = JSOG.createValueNode("test");
        instance.add(0, null);
        assertNull(instance.get(0).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAddWithIndex_null() {
        System.out.println("testAddWithIndex_null");
        JSOG instance = JSOG.createArrayNode();
        instance.add(0, null);
        assertNull(instance.get(0).getValue());
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testAddWithIndex_valueIsIllegal() {
        System.out.println("testAddWithIndex_valueIsIllegal");
        JSOG instance = JSOG.array();
        instance.add(0, JSOG.class);

        fail("Expected an exception");
    }

    /**
     * Test of add method, of class JSOG.
     */
    @Test
    public void testAddWithIndex_valueIsJsog() {
        System.out.println("testAddWithIndex_valueIsJsog");
        JSOG instance = JSOG.array();
        instance.add(0, JSOG.object("foo", "bar"));
        assertEquals(JSOG.array(JSOG.object("foo", "bar")), instance);
    }

    /**
     * Test of addAll method, of class JSOG.
     */
    @Test
    public void testAddAllList() {
        JSOG instance = JSOG.array();

        Collection<Object> collection = new ArrayList<Object>();
        collection.add(true);
        collection.add(false);

        instance.addAll(collection);

        assertTrue(instance.contains(true));
        assertTrue(instance.contains(false));
    }

    /**
     * Test of addAll method, of class JSOG.
     */
    @Test
    public void testAddAllSet() {
        JSOG instance = JSOG.array();

        Collection<Object> collection = new HashSet<Object>();
        collection.add(true);
        collection.add(false);

        instance.addAll(collection);

        assertTrue(instance.contains(true));
        assertTrue(instance.contains(false));
    }

    /**
     * Test of contains method, of class JSOG.
     */
    @Test
    public void testContainsTrue() {
        JSOG instance = JSOG.array();
        instance.add(true);

        assertTrue(instance.contains(true));
    }

    /**
     * Test of contains method, of class JSOG.
     */
    @Test
    public void testContainsFalse() {
        JSOG instance = JSOG.array();
        instance.add(true);

        assertFalse(instance.contains(false));
    }

    /**
     * Test of contains method, of class JSOG.
     */
    @Test
    public void testContainsJsog() {
        JSOG instance = JSOG.array();
        instance.add(JSOG.object("foo", "bar"));

        assertTrue(instance.contains(JSOG.object("foo", "bar")));
    }

    /**
     * Test of contains method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testContainsNotArray() {
        JSOG instance = JSOG.object();

        instance.contains(true);
    }

    /**
     * Test of contains method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testContainsNotArrayNull() {
        JSOG instance = new JSOG();

        instance.contains(true);
    }

    /**
     * Test of indexOf method, of class JSOG.
     */
    @Test
    public void testIndexOf() {
        JSOG instance = JSOG.array();
        instance.add(true);
        instance.add(false);

        assertEquals(0, instance.indexOf(true));
        assertEquals(1, instance.indexOf(false));
    }

    /**
     * Test of indexOf method, of class JSOG.
     */
    @Test
    public void testIndexOfFalse() {
        JSOG instance = JSOG.array();
        instance.add(true);

        assertEquals(-1, instance.indexOf(false));
    }

    /**
     * Test of indexOf method, of class JSOG.
     */
    @Test
    public void testIndexOfJsog() {
        JSOG instance = JSOG.array();
        instance.add(JSOG.object("foo", "bar"));

        assertEquals(0, instance.indexOf(JSOG.object("foo", "bar")));
    }

    /**
     * Test of indexOf method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testIndexOfNotArray() {
        JSOG instance = JSOG.object();

        instance.indexOf(true);
    }

    /**
     * Test of indexOf method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testIndexOfNotArrayNull() {
        JSOG instance = new JSOG();

        instance.indexOf(true);
    }

    /**
     * Test of hasKey method, of class JSOG.
     */
    @Test
    public void testHasKey_Enum() {
        System.out.println("testHasKey_Enum");
        JSOG instance = JSOG.createObjectNode();
        instance.put(TestEnum.foo, null);
        assertTrue(instance.hasKey(TestEnum.foo));
    }

    /**
     * Test of hasKey method, of class JSOG.
     */
    @Test
    public void testHasKey_Enum_null() {
        System.out.println("testHasKey_Enum_null");
        JSOG instance = JSOG.createObjectNode();
        instance.put((TestEnum) null, null);
        assertTrue(instance.hasKey((TestEnum) null));
    }

    /**
     * Test of hasKey method, of class JSOG.
     */
    @Test
    public void testHasKey_null() {
        System.out.println("testHasKey_null");
        JSOG instance = new JSOG();
        assertFalse(instance.hasKey("test"));
    }

    /**
     * Test of hasKey method, of class JSOG.
     */
    @Test
    public void testHasKey_array() {
        System.out.println("testHasKey_array");
        JSOG instance = JSOG.createArrayNode();
        try {
            instance.hasKey("test");
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Not an object."));
        }
    }

    /**
     * Test of hasKey method, of class JSOG.
     */
    @Test
    public void testHasKey_value() {
        System.out.println("testHasKey_value");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.hasKey("test");
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Not an object."));
        }
    }

    /**
     * Test of hasKey method, of class JSOG.
     */
    @Test
    public void testHasKey_objectFalse() {
        System.out.println("testHasKey_objectFalse");
        JSOG instance = JSOG.createObjectNode();
        assertFalse(instance.hasKey("test"));
    }

    /**
     * Test of hasKey method, of class JSOG.
     */
    @Test
    public void testHasKey_objectTrue() {
        System.out.println("testHasKey_objectTrue");
        JSOG instance = JSOG.createObjectNode();
        instance.put("test", null);
        assertTrue(instance.hasKey("test"));
    }

    /**
     * Test of remove method, of class JSOG.
     */
    @Test
    public void testRemove_Enum() {
        System.out.println("testRemove_Enum");
        JSOG instance = JSOG.createObjectNode();
        instance.put(TestEnum.foo, "bar");
        Object result = instance.remove(TestEnum.foo);
        assertEquals("bar", result);
    }

    /**
     * Test of remove method, of class JSOG.
     */
    @Test
    public void testRemove_Enum_null() {
        System.out.println("testRemove_Enum_null");
        JSOG instance = JSOG.createObjectNode();
        instance.put((TestEnum) null, "bar");
        Object result = instance.remove((TestEnum) null);
        assertEquals("bar", result);
    }

    /**
     * Test of remove method, of class JSOG.
     */
    @Test
    public void testRemove_Object_NotAnObject() {
        System.out.println("testRemove_Object_NotAnObject");
        JSOG instance = JSOG.createArrayNode();
        try {
            instance.remove("test");
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Not an object."));
        }
    }

    /**
     * Test of remove method, of class JSOG.
     */
    @Test
    public void testRemove_Object_contains() {
        System.out.println("testRemove_Object_contains");
        JSOG instance = JSOG.createObjectNode();
        instance.put("foo", "bar");
        Object result = instance.remove("foo");
        assertEquals("bar", result);
    }

    /**
     * Test of remove method, of class JSOG.
     */
    @Test
    public void testRemove_Object_doesNotContain() {
        System.out.println("testRemove_Object_doesNotContain");
        JSOG instance = JSOG.createObjectNode();
        Object result = instance.remove("foo");
        assertNull(result);
    }

    /**
     * Test of remove method, of class JSOG.
     */
    @Test
    public void testRemove_int_NotAnArray() {
        System.out.println("testRemove_int_NotAnArray");
        JSOG instance = JSOG.createObjectNode();
        try {
            instance.remove(1);
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Not an array."));
        }
    }

    /**
     * Test of remove method, of class JSOG.
     */
    @Test
    public void testRemove_int_contains() {
        System.out.println("testRemove_int_contains");
        JSOG instance = JSOG.createArrayNode();
        String expected = "test";

        instance.add(expected);
        Object actual = instance.remove(0);
        assertSame(expected, actual);
    }

    /**
     * Test of remove method, of class JSOG.
     */
    @Test
    public void testRemove_int_doesNotContain() {
        System.out.println("testRemove_int_doesNotContain");
        JSOG instance = JSOG.createArrayNode();

        try {
            instance.remove(0);
            fail("Expected an exception.");
        } catch (IndexOutOfBoundsException e) {
            assertTrue(e.getMessage().contains("Index: 0"));
        }
    }

    /**
     * Test of clear method, of class JSOG.
     */
    @Test
    public void testClearValue() {
        System.out.println("testClearValue");
        JSOG instance = new JSOG("foo");
        instance.clear();
        assertTrue(instance.isNull());
    }

    /**
     * Test of clear method, of class JSOG.
     */
    @Test
    public void testClearArray() {
        System.out.println("testClearArray");
        JSOG instance = JSOG.array("foo");
        instance.clear();
        assertEquals(JSOG.array(), instance);
    }

    /**
     * Test of clear method, of class JSOG.
     */
    @Test
    public void testClearObject() {
        System.out.println("testClearObject");
        JSOG instance = JSOG.object("foo", "bar");
        instance.clear();
        assertEquals(JSOG.object(), instance);
    }

    /**
     * Test of put method, of class JSOG.
     */
    @Test
    public void testPut() {
        System.out.println("testPut");
        JSOG instance = JSOG.createObjectNode();
        instance.put("foo", "bar");
        instance.put("baz", "qux");
        assertEquals("bar", instance.get("foo").getValue());
        assertEquals("qux", instance.get("baz").getValue());
    }

    /**
     * Test of put method, of class JSOG.
     */
    @Test
    public void testPut_Enum() {
        System.out.println("testPut_Enum");
        JSOG instance = JSOG.createObjectNode();
        instance.put(TestEnum.foo, "bar");
        instance.put(TestEnum.baz, "qux");
        assertEquals("bar", instance.get(TestEnum.foo).getValue());
        assertEquals("qux", instance.get(TestEnum.baz).getValue());
    }

    /**
     * Test of put method, of class JSOG.
     */
    @Test
    public void testPut_Enum_null() {
        System.out.println("testPut_Enum_null");
        JSOG instance = JSOG.createObjectNode();
        instance.put((TestEnum) null, "bar");
        assertEquals("bar", instance.get((TestEnum) null).getValue());
    }

    /**
     * Test of put method, of class JSOG.
     */
    @Test
    public void testPut_notObject() {
        System.out.println("testPut_notObject");
        JSOG instance = JSOG.createValueNode();
        instance.put("foo", null);
        assertEquals(null, instance.get("foo").getValue());
    }

    public void testPut_valueIsPrimitive() {
        System.out.println("testPut_valueIsPrimitive");
        JSOG instance = JSOG.createValueNode("test");
        instance.put("foo", null);
        assertEquals(null, instance.get("foo").getValue());
    }

    /**
     * Test of put method, of class JSOG.
     */
    @Test
    public void testPut_valueIsArray() {
        System.out.println("testPut_valueIsArray");
        JSOG instance = JSOG.createArrayNode();
        instance.put("foo", null);
        assertEquals(null, instance.get("foo").getValue());
    }

    /**
     * Test of put method, of class JSOG.
     */
    @Test
    public void testPut_null() {
        System.out.println("testPut_null");
        JSOG instance = JSOG.createObjectNode();
        instance.put("foo", null);
        assertEquals(null, instance.get("foo").getValue());
    }

    /**
     * Test of put method, of class JSOG.
     */
    @Test
    public void testPut_chain() {
        System.out.println("testPut_chain");
        JSOG instance = JSOG.createObjectNode();
        instance
                .put("foo", "bar")
                .put("baz", "qux");
        assertEquals("bar", instance.get("foo").getValue());
        assertEquals("qux", instance.get("baz").getValue());
    }

    /**
     * Test of put method, of class JSOG.
     *
     * There's currently no support for other objects.
     * Exepect an error if we get one.
     */
    @Test
    public void testPut_invalid() {
        System.out.println("testSet_object");
        JSOG instance = JSOG.createObjectNode();
        try {
            instance.put("test", new Object());
            fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("primitive types"));
        }
    }

    /**
     * Test of putAll method, of class JSOG.
     */
    @Test
    public void testPutAll() {
        JSOG instance = JSOG.object();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "bar");
        map.put("bar", "baz");

        instance.putAll(map);

        assertEquals(JSOG.object("foo", "bar")
                         .put("bar", "baz"),
                     instance);
    }

    /**
     * Test of set method, of class JSOG.
     */
    @Test
    public void testSet() {
        System.out.println("testSet");
        JSOG instance = JSOG.createValueNode();
        instance.set("test");
        assertEquals("test", instance.getValue());
    }

    /**
     * Test of set method, of class JSOG.
     */
    @Test
    public void testSet_null() {
        System.out.println("testSet_null");
        JSOG instance = JSOG.createValueNode("test");
        instance.set(null);
        assertEquals(null, instance.getValue());
    }

    /**
     * Test of set method, of class JSOG.
     *
     * This method should coerce the JSOG into the specified value if it's currently an array.
     */
    @Test
    public void testSet_coerceArray() {
        System.out.println("testSet_coerceArray");
        JSOG instance = JSOG.createArrayNode();
        instance.set("test");
        assertEquals("test", instance.getValue());
    }

    /**
     * Test of set method, of class JSOG.
     *
     * This method should coerce the JSOG into the specified value if it's currently an object.
     */
    @Test
    public void testSet_coerceObject() {
        System.out.println("testSet_coerceObject");
        JSOG instance = JSOG.createObjectNode();
        instance.set("test");
        assertEquals("test", instance.getValue());
    }

    /**
     * Test of set method, of class JSOG.
     */
    @Test
    public void testSet_jsog() {
        System.out.println("testSet_jsog");
        JSOG value = JSOG.createArrayNode();
        JSOG instance = JSOG.createValueNode();
        instance.set(value);
        assertSame(value, instance.getValue());
    }

    /**
     * Test of set method, of class JSOG.
     *
     * There's currently no support for other objects.
     * Exepect an error if we get one.
     */
    @Test
    public void testSet_invalid() {
        System.out.println("testSet_invalid");
        JSOG instance = JSOG.createValueNode();
        try {
            instance.set(new Object());
            fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("primitive types"));
        }
    }

    /**
     * Test of size method, of class JSOG.
     */
    @Test
    public void testSize_objectZero() {
        System.out.println("testSize_objectZero");
        JSOG instance = JSOG.createObjectNode();
        assertEquals(0, instance.size());
    }

    /**
     * Test of size method, of class JSOG.
     */
    @Test
    public void testSize_objectOne() {
        System.out.println("testSize_objectOne");
        JSOG instance = JSOG.createObjectNode();
        instance.put("test", null);
        assertEquals(1, instance.size());
    }

    /**
     * Test of size method, of class JSOG.
     */
    @Test
    public void testSize_arrayZero() {
        System.out.println("testSize_arrayZero");
        JSOG instance = JSOG.createArrayNode();
        assertEquals(0, instance.size());
    }

    /**
     * Test of size method, of class JSOG.
     */
    @Test
    public void testSize_arrayOne() {
        System.out.println("testSize_arrayOne");
        JSOG instance = JSOG.createArrayNode();
        assertEquals(0, instance.size());
    }

    /**
     * Test of size method, of class JSOG.
     */
    @Test
    public void testSize_null() {
        System.out.println("testSize_null");
        JSOG instance = JSOG.createValueNode();
        try {
            instance.size();
            fail("Expected an exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not an array or object"));
        }
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_Enum() {
        System.out.println("testGet_Enum");
        JSOG instance = JSOG.createObjectNode();
        instance.get(TestEnum.foo).set("bar");
        assertEquals("bar", instance.get(TestEnum.foo).getValue());
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_Enum_null() {
        System.out.println("testGet_Enum_null");
        JSOG instance = JSOG.createObjectNode();
        instance.get((TestEnum) null).set("bar");
        assertEquals("bar", instance.get((TestEnum) null).getValue());
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_String() {
        System.out.println("testGet_String");
        JSOG instance = JSOG.createObjectNode();
        instance.get("foo").set("bar");
        assertEquals("bar", instance.get("foo").getValue());
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_StringFromObjectWithNullValue() {
        System.out.println("testGet_StringFromObjectWithNullValue");
        JSOG instance = JSOG.object("foo", null);
        JSOG foo = instance.get("foo").set(true);
        assertFalse(instance.get("foo").isNull());
    }

    /**
     * Test of get method, of class JSOG.
     *
     * If the JSON already has a non-null non-object value, we should throw an exception.
     * Otherwise, we would implicitly clobber data, and that would be bad.
     */
    @Test
    public void testGet_String_existsNotAnObject() {
        System.out.println("testGet_String_existsNotAnObject");
        JSOG instance = JSOG.createValueNode("test");
        try {
            instance.get("foo").set("bar");
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Not null"));
        }
    }

    /**
     * Test of get method, of class JSOG.
     *
     * Two sequential calls to get should return the same object.
     */
    @Test
    public void testGet_String_same() {
        System.out.println("testGet_String_Same");
        JSOG instance = JSOG.createObjectNode();
        assertSame(instance.get("test"), instance.get("test"));
    }

    /**
     * Test of get method, of class JSOG.
     *
     * This tests the chaining concept. In this instance, we start with a null object and use coercion.
     */
    @Test
    public void testGet_String_chain() {
        System.out.println("testGet_String_chain");
        JSOG instance = JSOG.createValueNode(null);
        instance.get("foo").get("bar").get("baz").get("qux").set("quux");
        assertEquals("quux", instance.get("foo").get("bar").get("baz").get("qux").getValue());
    }

    /**
     * Test of get method, of class JSOG.
     *
     * This tests the chaining concept. If a key already exists, it should be reused.
     */
    @Test
    public void testGet_String_chainExists() {
        System.out.println("testGet_String_chainExists");
        JSOG instance = JSOG.createObjectNode();
        instance.get("foo").add("bar");
        instance.get("foo").add("baz");
        assertEquals("bar", instance.get("foo").get(0).getValue());
        assertEquals("baz", instance.get("foo").get(1).getValue());
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_int() {
        System.out.println("testGet_int");
        JSOG instance = JSOG.createArrayNode();
        instance.add("test");
        assertEquals("test", instance.get(0).getValue());
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_int_Json() {
        System.out.println("testGet_int_Json");
        JSOG instance = JSOG.createArrayNode();
        JSOG value = JSOG.createObjectNode();
        instance.add(value);
        assertSame(value, instance.get(0));
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_int_null() {
        System.out.println("testGet_int_notAnArray");
        JSOG instance = JSOG.createValueNode();
        try {
            instance.get(0).getValue();
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("is not an array"));
        }
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_int_valueIsPrimitive() {
        System.out.println("testGet_int_valueIsPrimitive");
        JSOG instance = JSOG.createValueNode("test");
        try {
            instance.get(0).getValue();
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("is not an array"));
        }
    }

    /**
     * Test of get method, of class JSOG.
     */
    @Test
    public void testGet_int_valueIsObject() {
        System.out.println("testGet_int_valueIsPrimitive");
        JSOG instance = JSOG.createObjectNode();
        try {
            instance.get(0).getValue();
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("is not an array"));
        }
    }

    /**
     * Test of get method, of class JSOG.
     *
     * If the index is out of bounds, expect an exception.
     */
    @Test
    public void testGet_int_outOfBounds() {
        System.out.println("testGet_int_outOfBounds");
        JSOG instance = JSOG.createArrayNode();
        try {
            instance.get(0).getValue();
            fail("Expected an exception");
        } catch (IndexOutOfBoundsException e) {
            assertTrue(e.getMessage().contains("Index: 0"));
        }
    }

    /**
     * Test of getValue method, of class JSOG.
     */
    @Test
    public void testGetValue() {
        System.out.println("testGetValue");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getValue());
    }

    /**
     * Test of getValue method, of class JSOG.
     *
     * If the value is a JSON, it should remain a JSON.
     */
    @Test
    public void testGetValue_wrapped() {
        System.out.println("testGetValue_wrapped");
        JSOG value = JSOG.createValueNode("test");
        JSOG instance = JSOG.createValueNode(value);

        assertSame(value, instance.getValue());
    }

    /**
     * Test of getValue method, of class JSOG.
     *
     * Attempting to get the value of an array should result in an exception.
     */
    @Test
    public void testGetValue_array() {
        System.out.println("testGetValue_array");
        JSOG instance = JSOG.createArrayNode();

        try {
            instance.getValue();
            fail("Expected an exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not a primitive or JSOG"));
        }
    }

    /**
     * Test of getValue method, of class JSOG.
     *
     * Attempting to get the value of an object should result in an exception.
     */
    @Test
    public void testGetValue_object() {
        System.out.println("testGetValue_object");
        JSOG instance = JSOG.createObjectNode();

        try {
            instance.getValue();
            fail("Expected an exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not a primitive or JSOG"));
        }
    }

    /**
     * Test of getBigDecimalValue method, of class JSOG.
     */
    @Test
    public void testGetBigDecimalValue() {
        System.out.println("testGetBigDecimalValue");
        JSOG instance = JSOG.createValueNode(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, instance.getBigDecimalValue());
    }

    /**
     * Test of getBigDecimalValue method, of class JSOG.
     */
    @Test
    public void testGetBigDecimalValue_null() {
        System.out.println("testGetBigDecimalValue_null");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getBigDecimalValue());
    }

    /**
     * Test of getBigDecimalValue method, of class JSOG.
     */
    @Test
    public void testGetBigDecimalValue_int() {
        System.out.println("testGetBigDecimalValue_int");
        JSOG instance = JSOG.createValueNode(1);

        assertEquals(BigDecimal.valueOf(1), instance.getBigDecimalValue());
    }

    /**
     * Test of getBigDecimalValue method, of class JSOG.
     */
    @Test
    public void testGetBigDecimalValue_float() {
        System.out.println("testGetBigDecimalValue_float");
        JSOG instance = JSOG.createValueNode(new Float(1.0));

        assertEquals(BigDecimal.valueOf(1.0), instance.getBigDecimalValue());
    }

    /**
     * Test of getBigDecimalValue method, of class JSOG.
     */
    @Test
    public void testGetBigDecimalValue_double() {
        System.out.println("testGetBigDecimalValue_double");
        JSOG instance = JSOG.createValueNode(new Double(1.0));

        assertEquals(BigDecimal.valueOf(1.0), instance.getBigDecimalValue());
    }

    /**
     * Test of getBigDecimalValue method, of class JSOG.
     */
    @Test
    public void testGetBigDecimalValue_string() {
        System.out.println("testGetBigDecimalValue_string");
        JSOG instance = JSOG.createValueNode("1.0");

        assertEquals(BigDecimal.valueOf(1.0), instance.getBigDecimalValue());
    }

    /**
     * Test of getBigDecimalValue method, of class JSOG.
     */
    @Test
    public void testGetBigDecimalValue_other() {
        System.out.println("testGetBigDecimalValue_other");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.getBigDecimalValue();
            fail("Expected an exception");
        } catch (NumberFormatException e) { }
    }

    /**
     * Test of getBigIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetBigIntegerValue() {
        System.out.println("testGetBigIntegerValue");
        JSOG instance = JSOG.createValueNode(BigInteger.ZERO);

        assertEquals(BigInteger.ZERO, instance.getBigIntegerValue());
    }

    /**
     * Test of getBigIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetBigIntegerValue_null() {
        System.out.println("testGetBigIntegerValue_null");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getBigIntegerValue());
    }

    /**
     * Test of getBigIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetBigIntegerValue_float() {
        System.out.println("testGetBigIntegerValue_float");
        JSOG instance = JSOG.createValueNode(new Float(1.0));

        assertEquals(BigInteger.valueOf(1), instance.getBigIntegerValue());
    }

    /**
     * Test of getBigIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetBigIntegerValue_double() {
        System.out.println("testGetBigIntegerValue_double");
        JSOG instance = JSOG.createValueNode(new Double(1.0));

        assertEquals(BigInteger.valueOf(1), instance.getBigIntegerValue());
    }

    /**
     * Test of getBigIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetBigIntegerValue_string() {
        System.out.println("testGetBigIntegerValue_string");
        JSOG instance = JSOG.createValueNode("1");

        assertEquals(BigInteger.valueOf(1), instance.getBigIntegerValue());
    }

    /**
     * Test of getBigIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetBigIntegerValue_other() {
        System.out.println("testGetBigIntegerValue_other");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.getBigIntegerValue();
            fail("Expected an exception");
        } catch (NumberFormatException e) { }
    }

    /**
     * Test of getByteValue method, of class JSOG.
     */
    @Test
    public void testGetByteValue() {
        System.out.println("testGetByteValue");
        JSOG instance = JSOG.createValueNode(Byte.MAX_VALUE);

        assertEquals(Byte.valueOf(Byte.MAX_VALUE), instance.getByteValue());
    }

    /**
     * Test of getByteValue method, of class JSOG.
     */
    @Test
    public void testGetByteValue_null() {
        System.out.println("testGetByteValue_null");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getByteValue());
    }

    /**
     * Test of getByteValue method, of class JSOG.
     */
    @Test
    public void testGetByteValue_float() {
        System.out.println("testGetByteValue_float");
        JSOG instance = JSOG.createValueNode(new Float(1.0));

        assertEquals(Byte.valueOf((byte) 1), instance.getByteValue());
    }

    /**
     * Test of getByteValue method, of class JSOG.
     */
    @Test
    public void testGetByteValue_double() {
        System.out.println("testGetByteValue_double");
        JSOG instance = JSOG.createValueNode(new Double(1.0));

        assertEquals(Byte.valueOf((byte) 1), instance.getByteValue());
    }

    /**
     * Test of getByteValue method, of class JSOG.
     */
    @Test
    public void testGetByteValue_string() {
        System.out.println("testGetByteValue_string");
        JSOG instance = JSOG.createValueNode("1");

        assertEquals(Byte.valueOf((byte) 1), instance.getByteValue());
    }

    /**
     * Test of getByteValue method, of class JSOG.
     */
    @Test
    public void testGetByteValue_other() {
        System.out.println("testGetByteValue_other");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.getByteValue();
            fail("Expected an exception");
        } catch (NumberFormatException e) { }
    }

    /**
     * Test of getShortValue method, of class JSOG.
     */
    @Test
    public void testGetShortValue() {
        System.out.println("testGetShortValue");
        JSOG instance = JSOG.createValueNode(Short.MAX_VALUE);

        assertEquals(Short.valueOf(Short.MAX_VALUE), instance.getShortValue());
    }

    /**
     * Test of getShortValue method, of class JSOG.
     */
    @Test
    public void testGetShortValue_null() {
        System.out.println("testGetShortValue_null");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getShortValue());
    }

    /**
     * Test of getShortValue method, of class JSOG.
     */
    @Test
    public void testGetShortValue_float() {
        System.out.println("testGetShortValue_float");
        JSOG instance = JSOG.createValueNode(new Float(1.0));

        assertEquals(Short.valueOf((short) 1), instance.getShortValue());
    }

    /**
     * Test of getShortValue method, of class JSOG.
     */
    @Test
    public void testGetShortValue_double() {
        System.out.println("testGetShortValue_double");
        JSOG instance = JSOG.createValueNode(new Double(1.0));

        assertEquals(Short.valueOf((short) 1), instance.getShortValue());
    }

    /**
     * Test of getShortValue method, of class JSOG.
     */
    @Test
    public void testGetShortValue_string() {
        System.out.println("testGetShortValue_string");
        JSOG instance = JSOG.createValueNode("1");

        assertEquals(Short.valueOf((short) 1), instance.getShortValue());
    }

    /**
     * Test of getShortValue method, of class JSOG.
     */
    @Test
    public void testGetShortValue_other() {
        System.out.println("testGetShortValue_other");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.getShortValue();
            fail("Expected an exception");
        } catch (NumberFormatException e) { }
    }

    /**
     * Test of getIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetIntegerValue() {
        System.out.println("testGetIntegerValue");
        JSOG instance = JSOG.createValueNode(Integer.MAX_VALUE);

        assertEquals(Integer.valueOf(Integer.MAX_VALUE), instance.getIntegerValue());
    }

    /**
     * Test of getIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetIntegerValue_null() {
        System.out.println("testGetIntegerValue_null");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getIntegerValue());
    }

    /**
     * Test of getIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetIntegerValue_float() {
        System.out.println("testGetIntegerValue_float");
        JSOG instance = JSOG.createValueNode(new Float(1.0));

        assertEquals(Integer.valueOf(1), instance.getIntegerValue());
    }

    /**
     * Test of getIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetIntegerValue_double() {
        System.out.println("testGetIntegerValue_double");
        JSOG instance = JSOG.createValueNode(new Double(1.0));

        assertEquals(Integer.valueOf(1), instance.getIntegerValue());
    }

    /**
     * Test of getIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetIntegerValue_string() {
        System.out.println("testGetIntegerValue_string");
        JSOG instance = JSOG.createValueNode("1");

        assertEquals(Integer.valueOf(1), instance.getIntegerValue());
    }

    /**
     * Test of getIntegerValue method, of class JSOG.
     */
    @Test
    public void testGetIntegerValue_other() {
        System.out.println("testGetIntegerValue_other");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.getIntegerValue();
            fail("Expected an exception");
        } catch (NumberFormatException e) { }
    }

    /**
     * Test of getLongValue method, of class JSOG.
     */
    @Test
    public void testGetLongValue() {
        System.out.println("testGetLongValue");
        JSOG instance = JSOG.createValueNode(Long.MAX_VALUE);

        assertEquals(Long.valueOf(Long.MAX_VALUE), instance.getLongValue());
    }

    /**
     * Test of getLongValue method, of class JSOG.
     */
    @Test
    public void testGetLongValue_null() {
        System.out.println("testGetLongValue_null");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getLongValue());
    }

    /**
     * Test of getLongValue method, of class JSOG.
     */
    @Test
    public void testGetLongValue_float() {
        System.out.println("testGetLongValue_float");
        JSOG instance = JSOG.createValueNode(new Float(1.0));

        assertEquals(Long.valueOf(1), instance.getLongValue());
    }

    /**
     * Test of getLongValue method, of class JSOG.
     */
    @Test
    public void testGetLongValue_double() {
        System.out.println("testGetLongValue_double");
        JSOG instance = JSOG.createValueNode(new Double(1.0));

        assertEquals(Long.valueOf(1), instance.getLongValue());
    }

    /**
     * Test of getLongValue method, of class JSOG.
     */
    @Test
    public void testGetLongValue_string() {
        System.out.println("testGetLongValue_string");
        JSOG instance = JSOG.createValueNode("1");

        assertEquals(Long.valueOf(1), instance.getLongValue());
    }

    /**
     * Test of getLongValue method, of class JSOG.
     */
    @Test
    public void testGetLongValue_other() {
        System.out.println("testGetLongValue_other");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.getLongValue();
            fail("Expected an exception");
        } catch (NumberFormatException e) { }
    }

    /**
     * Test of getFloatValue method, of class JSOG.
     */
    @Test
    public void testGetFloatValue() {
        System.out.println("testGetFloatValue");
        JSOG instance = JSOG.createValueNode(Float.MAX_VALUE);

        assertEquals(Float.valueOf(Float.MAX_VALUE), instance.getFloatValue());
    }

    /**
     * Test of getFloatValue method, of class JSOG.
     */
    @Test
    public void testGetFloatValue_null() {
        System.out.println("testGetFloatValue_null");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getFloatValue());
    }

    /**
     * Test of getFloatValue method, of class JSOG.
     */
    @Test
    public void testGetFloatValue_float() {
        System.out.println("testGetFloatValue_float");
        JSOG instance = JSOG.createValueNode(new Float(1.0));

        assertEquals(Float.valueOf(1), instance.getFloatValue());
    }

    /**
     * Test of getFloatValue method, of class JSOG.
     */
    @Test
    public void testGetFloatValue_double() {
        System.out.println("testGetFloatValue_double");
        JSOG instance = JSOG.createValueNode(new Double(1.0));

        assertEquals(Float.valueOf(1), instance.getFloatValue());
    }

    /**
     * Test of getFloatValue method, of class JSOG.
     */
    @Test
    public void testGetFloatValue_string() {
        System.out.println("testGetFloatValue_string");
        JSOG instance = JSOG.createValueNode("1");

        assertEquals(Float.valueOf(1), instance.getFloatValue());
    }

    /**
     * Test of getFloatValue method, of class JSOG.
     */
    @Test
    public void testGetFloatValue_other() {
        System.out.println("testGetFloatValue_other");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.getFloatValue();
            fail("Expected an exception");
        } catch (NumberFormatException e) { }
    }

    /**
     * Test of getDoubleValue method, of class JSOG.
     */
    @Test
    public void testGetDoubleValue() {
        System.out.println("testGetDoubleValue");
        JSOG instance = JSOG.createValueNode(Double.MAX_VALUE);

        assertEquals(Double.valueOf(Double.MAX_VALUE), instance.getDoubleValue());
    }

    /**
     * Test of getDoubleValue method, of class JSOG.
     */
    @Test
    public void testGetDoubleValue_null() {
        System.out.println("testGetDoubleValue_null");
        JSOG instance = JSOG.createValueNode(null);
        assertNull(instance.getDoubleValue());
    }

    /**
     * Test of getDoubleValue method, of class JSOG.
     */
    @Test
    public void testGetDoubleValue_float() {
        System.out.println("testGetDoubleValue_float");
        JSOG instance = JSOG.createValueNode(new Float(1.0));

        assertEquals(Double.valueOf(1), instance.getDoubleValue());
    }

    /**
     * Test of getDoubleValue method, of class JSOG.
     */
    @Test
    public void testGetDoubleValue_double() {
        System.out.println("testGetDoubleValue_double");
        JSOG instance = JSOG.createValueNode(new Double(1.0));

        assertEquals(Double.valueOf(1), instance.getDoubleValue());
    }

    /**
     * Test of getDoubleValue method, of class JSOG.
     */
    @Test
    public void testGetDoubleValue_string() {
        System.out.println("testGetDoubleValue_string");
        JSOG instance = JSOG.createValueNode("1");

        assertEquals(Double.valueOf(1), instance.getDoubleValue());
    }

    /**
     * Test of getDoubleValue method, of class JSOG.
     */
    @Test
    public void testGetDoubleValue_other() {
        System.out.println("testGetDoubleValue_other");
        JSOG instance = JSOG.createValueNode("foo");
        try {
            instance.getDoubleValue();
            fail("Expected an exception");
        } catch (NumberFormatException e) { }
    }

    /**
     * Test of getStringValue method, of class JSOG.
     */
    @Test
    public void testGetStringValue_null() {
        System.out.println("testGetStringValue_null");
        JSOG instance = JSOG.createValueNode();

        assertEquals(null, instance.getStringValue());
    }

    /**
     * Test of getStringValue method, of class JSOG.
     */
    @Test
    public void testGetStringValue_string() {
        System.out.println("testGetStringValue_string");
        JSOG instance = JSOG.createValueNode("test");

        assertEquals("test", instance.getStringValue());
    }

    /**
     * Test of getStringValue method, of class JSOG.
     */
    @Test
    public void testGetStringValue_other() {
        System.out.println("testGetStringValue_other");
        JSOG instance = JSOG.createValueNode(1);
        assertEquals("1", instance.getStringValue());
    }

    /**
     * Test of getBooleanValue method, of class JSOG.
     */
    @Test
    public void testGetBooleanValue_null() {
        System.out.println("testGetBooleanValue_null");
        JSOG instance = JSOG.createValueNode();

        assertNull(instance.getBooleanValue());
    }

    /**
     * Test of getBooleanValue method, of class JSOG.
     */
    @Test
    public void testGetBooleanValue_stringTrue() {
        System.out.println("testGetBooleanValue_stringTrue");
        JSOG instance = JSOG.createValueNode("true");

        assertTrue(instance.getBooleanValue());
    }

    /**
     * Test of getBooleanValue method, of class JSOG.
     */
    @Test
    public void testGetBooleanValue_stringTrueCaseInsensitive() {
        System.out.println("testGetBooleanValue_stringTrueCaseInsensitive");
        JSOG instance = JSOG.createValueNode("True");

        assertTrue(instance.getBooleanValue());
    }

    /**
     * Test of getBooleanValue method, of class JSOG.
     */
    @Test
    public void testGetBooleanValue_stringFalse() {
        System.out.println("testGetBooleanValue_stringFalse");
        JSOG instance = JSOG.createValueNode("false");

        assertFalse(instance.getBooleanValue());
    }

    /**
     * Test of getBooleanValue method, of class JSOG.
     */
    @Test
    public void testGetBooleanValue_booleanTrue() {
        System.out.println("testGetBooleanValue_booleanTrue");
        JSOG instance = JSOG.createValueNode(true);
        assertTrue(instance.getBooleanValue());
    }

    /**
     * Test of getBooleanValue method, of class JSOG.
     */
    @Test
    public void testGetBooleanValue_booleanFalse() {
        System.out.println("testGetBooleanValue_booleanFalse");
        JSOG instance = JSOG.createValueNode(true);
        assertTrue(instance.getBooleanValue());
    }

    // TODO: Add all the primitives

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_primitiveNull() {
        System.out.println("testToJsonNode_primitiveNull");
        JSOG instance = JSOG.createValueNode(null);
        assertEquals(om.getNodeFactory().nullNode(), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_primitiveNumber() {
        System.out.println("testToJsonNode_primitiveNumber");
        JSOG instance = JSOG.createValueNode(123);
        assertEquals(om.getNodeFactory().numberNode(123), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_primitiveString() {
        System.out.println("testToJsonNode_primitiveString");
        JSOG instance = JSOG.createValueNode("test");
        assertEquals(om.getNodeFactory().textNode("test"), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_jsog() {
        System.out.println("testToJsonNode_jsog");
        JSOG value = JSOG.createObjectNode();
        JSOG instance = JSOG.createValueNode(value);

        assertEquals(om.getNodeFactory().objectNode(), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_null() {
        System.out.println("testToJsonNode_null");
        JSOG instance = JSOG.createValueNode(null);
        assertEquals(om.getNodeFactory().nullNode(), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_boolean_true() {
        System.out.println("testToJsonNode_boolean_true");
        JSOG instance = JSOG.createValueNode(true);
        assertEquals(om.getNodeFactory().booleanNode(true), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_boolean_false() {
        System.out.println("testToJsonNode_boolean_false");
        JSOG instance = JSOG.createValueNode(false);
        assertEquals(om.getNodeFactory().booleanNode(false), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_bigDecimal() {
        System.out.println("testToJsonNode_bigDecimal");
        JSOG instance = JSOG.createValueNode(BigDecimal.ZERO);
        assertEquals(om.getNodeFactory().numberNode(BigDecimal.ZERO), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_bigInteger() {
        System.out.println("testToJsonNode_bigInteger");
        JSOG instance = JSOG.createValueNode(BigInteger.ZERO);
        assertEquals(om.getNodeFactory().numberNode(BigInteger.ZERO), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_byte() {
        System.out.println("testToJsonNode_byte");
        JSOG instance = JSOG.createValueNode(Byte.MAX_VALUE);
        assertEquals(om.getNodeFactory().numberNode(Byte.MAX_VALUE), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_char() {
        System.out.println("testToJsonNode_char");
        JSOG instance = JSOG.createValueNode(Character.MAX_VALUE);
        assertEquals(om.getNodeFactory().numberNode(Character.MAX_VALUE), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_short() {
        System.out.println("testToJsonNode_short");
        JSOG instance = JSOG.createValueNode(Short.MAX_VALUE);
        assertEquals(om.getNodeFactory().numberNode(Short.MAX_VALUE), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_int() {
        System.out.println("testToJsonNode_int");
        JSOG instance = JSOG.createValueNode(Integer.MAX_VALUE);
        assertEquals(om.getNodeFactory().numberNode(Integer.MAX_VALUE), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_long() {
        System.out.println("testToJsonNode_long");
        JSOG instance = JSOG.createValueNode(Long.MAX_VALUE);
        assertEquals(om.getNodeFactory().numberNode(Long.MAX_VALUE), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_Double() {
        System.out.println("testToJsonNode_Double");
        JSOG instance = JSOG.createValueNode(Double.MAX_VALUE);
        assertEquals(om.getNodeFactory().numberNode(Double.MAX_VALUE), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_double() {
        System.out.println("testToJsonNode_double");
        JSOG instance = JSOG.createValueNode(Double.MAX_VALUE);
        assertEquals(om.getNodeFactory().numberNode(Double.MAX_VALUE), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_string() {
        System.out.println("testToJsonNode_string");
        JSOG instance = JSOG.createValueNode("test");
        assertEquals(om.getNodeFactory().textNode("test"), instance.toJsonNode());
    }

    /**
     * Test of toJsonNode method, of class JSOG.
     */
    @Test
    public void testToJsonNode_string_empty() {
        System.out.println("testToJsonNode_string_empty");
        JSOG instance = JSOG.createValueNode("");
        assertEquals(om.getNodeFactory().textNode(""), instance.toJsonNode());
    }

    /**
     * Test of toString method, of class JSOG.
     */
    @Test
    public void testToString() {
        System.out.println("testToString");
        JSOG instance = JSOG.createValueNode("test");
        assertEquals("\"test\"", instance.toString());
    }

    /**
     * Test of toString method, of class JSOG.
     */
    @Test
    public void testToString_complex() {
        System.out.println("testToString_complex");
        JSOG instance = JSOG.createObjectNode();
        instance
            .get("foo")
                .add(JSOG.createObjectNode().put("bar", "baz"))     // An object
                .add(JSOG.createArrayNode().add("qux").add("quux")) // An array
                .add("val");


        assertEquals("{\"foo\":[{\"bar\":\"baz\"},[\"qux\",\"quux\"],\"val\"]}", instance.toString());
    }

    /**
     * Test of the keySet method, of class JSOG.
     */
    @Test
    public void testKeySet() {
        System.out.println("testKeySet");

        JSOG instance = JSOG.createObjectNode();
        instance.put("foo", "bar");

        Set<String> expected = new HashSet();
        expected.add("foo");

        assertEquals(expected, instance.keySet());
    }

    /**
     * Test of the keySet method, of class JSOG.
     */
    @Test
    public void testKeySetOrdered() {
        System.out.println("testKeySetOrdered");

        JSOG instance = JSOG.createObjectNode();
        instance.put("foo", null);
        instance.put("bar", null);
        instance.put("baz", null);
        instance.put("qux", null);

        Iterator<String> keys = instance.keySet().iterator();

        assertEquals("foo", keys.next());
        assertEquals("bar", keys.next());
        assertEquals("baz", keys.next());
        assertEquals("qux", keys.next());
    }

    /**
     * Test of the keySet method, of class JSOG.
     */
    @Test
    public void testKeySet_empty() {
        System.out.println("testKeySet_empty");

        JSOG instance = JSOG.createObjectNode();
        assertEquals(Collections.emptySet(), instance.keySet());
    }

    /**
     * Test of the keySet method, of class JSOG.
     */
    @Test
    public void testKeySet_null() {
        System.out.println("testKeySet_null");

        JSOG instance = JSOG.createValueNode();
        assertEquals(Collections.emptySet(), instance.keySet());
    }

    /**
     * Test of the keySet method, of class JSOG.
     */
    @Test
    public void testKeySet_array() {
        System.out.println("testKeySet_array");

        JSOG instance = JSOG.createArrayNode();
        try {
            instance.keySet();
            fail("Expected an exception.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not an object"));
        }
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsPrimitivesOfDifferentTypeButSameStringValue() {
        System.out.println("testEqualsPrimitivesOfDifferentTypeButSameStringValue");

        JSOG instance = new JSOG(1);
        assertTrue(instance.equals(new JSOG("1")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsNull() {
        System.out.println("testEqualsNull");

        JSOG instance = new JSOG();
        assertFalse(instance.equals(null));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsDifferentObject() {
        System.out.println("testEqualsDifferentObject");

        JSOG instance = new JSOG();
        assertFalse(instance.equals(""));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsValuesNull() {
        System.out.println("testEqualsValueNull");

        JSOG instance = new JSOG();
        assertTrue(instance.equals(new JSOG()));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsValuesStringAndNull() {
        System.out.println("testEqualsValuesStringAndNull");

        JSOG instance = new JSOG("");
        assertFalse(instance.equals(new JSOG()));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsValuesNullAndString() {
        System.out.println("testEqualsValuesNullAndString");

        JSOG instance = new JSOG();
        assertFalse(instance.equals(new JSOG("")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsValuesStringAndString() {
        System.out.println("testEqualsValuesStringAndString");

        JSOG instance = new JSOG("");
        assertTrue(instance.equals(new JSOG("")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsObjectAndNull() {
        System.out.println("testEqualsObjectAndNull");

        JSOG instance = JSOG.object();
        assertFalse(instance.equals(null));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsObjectAndValueNull() {
        System.out.println("testEqualsObjectAndValueNull");

        JSOG instance = JSOG.object();
        assertFalse(instance.equals(new JSOG()));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsObjectAndValueString() {
        System.out.println("testEqualsObjectAndValueString");

        JSOG instance = JSOG.object();
        assertFalse(instance.equals(new JSOG("")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsObjectAndObject() {
        System.out.println("testEqualsObjectAndObject");

        JSOG instance = JSOG.object();
        assertTrue(instance.equals(JSOG.object()));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsObjectAndObjectWithKeys() {
        System.out.println("testEqualsObjectAndObjectWithKeys");

        JSOG instance = JSOG.object("foo", "bar");
        assertTrue(instance.equals(JSOG.object("foo", "bar")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsObjectAndObjectDifferentKeysAndValues() {
        System.out.println("testEqualsObjectAndObjectDifferentKeysAndValues");

        JSOG instance = JSOG.object("foo", "bar");
        assertFalse(instance.equals(JSOG.object("bar", "baz")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsObjectAndObjectDifferentSizes() {
        System.out.println("testEqualsObjectAndObjectDifferentSizes");

        JSOG instance = JSOG.object("foo", "bar");
        assertFalse(instance.equals(JSOG.object()));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsArrayAndNull() {
        System.out.println("testEqualsArrayAndNull");

        JSOG instance = JSOG.array();
        assertFalse(instance.equals(null));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsArrayAndValueNull() {
        System.out.println("testEqualsArrayAndValueNull");

        JSOG instance = JSOG.array();
        assertFalse(instance.equals(new JSOG()));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsArrayAndValueString() {
        System.out.println("testEqualsArrayAndValueString");

        JSOG instance = JSOG.array();
        assertFalse(instance.equals(new JSOG("")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsArrayOfSimilarPrimitives() {
        System.out.println("testEqualsArrayOfSimilarPrimitives");

        JSOG thisJsog = JSOG.array("true", "1");
        JSOG thatJsog = JSOG.array(true, 1);
        assertTrue(thisJsog.equals(thatJsog));
        assertTrue(thatJsog.equals(thisJsog));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsObjectsOfSimilarPrimitives() {
        System.out.println("testEqualsObjectsOfSimilarPrimitives");

        JSOG thisJsog = JSOG.object("foo", "true").put("bar", "1");
        JSOG thatJsog = JSOG.object("foo", true).put("bar", 1);
        assertTrue(thisJsog.equals(thatJsog));
        assertTrue(thatJsog.equals(thisJsog));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsArrayAndArray() {
        System.out.println("testEqualsArrayAndArray");

        JSOG instance = JSOG.array();
        assertTrue(instance.equals(JSOG.array()));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsArrayAndArrayWithValues() {
        System.out.println("testEqualsArrayAndArrayWithValues");

        JSOG instance = JSOG.array("foo", "bar");
        assertTrue(instance.equals(JSOG.array("foo", "bar")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsArrayAndArrayDifferentValues() {
        System.out.println("testEqualsArrayAndArrayDifferentValues");

        JSOG instance = JSOG.array("foo", "bar");
        assertFalse(instance.equals(JSOG.array("bar", "baz")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsArrayAndArrayDifferentSizes() {
        System.out.println("testEqualsArrayAndArrayDifferentSizes");

        JSOG instance = JSOG.array("foo", "bar");
        assertFalse(instance.equals(JSOG.array("baz")));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsNestedObjects() {
        System.out.println("testEqualsNestedObjects");

        JSOG instance = JSOG.object("foo", JSOG.object("bar", "baz"));
        assertTrue(instance.equals(JSOG.object("foo", JSOG.object("bar", "baz"))));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsCopiedObjects() {
        System.out.println("testEqualsCopiedObjects");

        JSOG instance = JSOG.object("foo", "bar").put("bar", "baz");
        JSOG copy = JSOG.object();
        copy.merge(instance);
        System.out.println(instance);
        System.out.println(copy);
        assertTrue(instance.equals(copy));
    }

    /**
     * Test of the equals method, of class JSOG.
     */
    @Test
    public void testEqualsNestedArrays() {
        System.out.println("testEqualsNestedArrays");

        JSOG instance = JSOG.array("foo", JSOG.array("bar", "baz"));
        assertTrue(instance.equals(JSOG.array("foo", JSOG.array("bar", "baz"))));
    }

    /**
     * Test of the clone method, of class JSOG.
     */
    @Test
    public void testClonePrimitive() {
        System.out.println("testClonePrimitive");

        JSOG instance = new JSOG("foo");
        JSOG clone = instance.clone();

        assertNotSame(instance, clone);
        assertEquals(instance, clone);
    }

    /**
     * Test of the clone method, of class JSOG.
     */
    @Test
    public void testCloneArray() {
        System.out.println("testCloneArray");

        JSOG instance = JSOG.array("foo", "bar");
        JSOG clone = instance.clone();

        assertNotSame(instance, clone);
        assertEquals(instance, clone);
    }

    /**
     * Test of the clone method, of class JSOG.
     */
    @Test
    public void testCloneObject() {
        System.out.println("testCloneObject");

        JSOG instance = JSOG.object("foo", "bar");
        JSOG clone = instance.clone();

        assertNotSame(instance, clone);
        assertEquals(instance, clone);
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test(expected=ConcurrentModificationException.class)
    public void testObjectIteratorCME() {
        System.out.println("testObjectIteratorCME");

        JSOG jsog = JSOG.object("foo", "bar");
        Iterator<Entry<String, JSOG>> it = jsog.objectIterator();

        jsog.put("bar", "baz");
        it.next();

        fail("Expected an exception");
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testObjectIteratorOnArray() {
        System.out.println("testObjectIteratorOnArray");

        JSOG.array().objectIterator();
        fail("Expected an exception");
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testObjectIteratorOnPrimitive() {
        System.out.println("testObjectIteratorOnPrimitive");

        new JSOG("foo").objectIterator();
        fail("Expected an exception");
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorOnNull() {
        System.out.println("testObjectIteratorOnNull");

        Iterator<Entry<String, JSOG>> it = new JSOG(null).objectIterator();

        assertFalse(it.hasNext());
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test(expected=NoSuchElementException.class)
    public void testObjectIteratorOnNullExceptionOnNext() {
        System.out.println("testObjectIteratorOnNullExceptionOnNext");

        Iterator<Entry<String, JSOG>> it = new JSOG(null).objectIterator();

        // Test next
        it.next();
        fail("Expected an exception");
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testObjectIteratorOnNullExceptionOnRemove() {
        System.out.println("testObjectIteratorOnNullExceptionOnRemove");

        Iterator<Entry<String, JSOG>> it = new JSOG(null).objectIterator();

        // Test next
        it.remove();
        fail("Expected an exception");
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorHasNext() {
        System.out.println("testObjectIteratorHasNext");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();

        // Test hasNext
        assertTrue(it.hasNext());

        // Get next and try hasNext again
        it.next();
        assertFalse(it.hasNext());
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorNext() {
        System.out.println("testObjectIteratorNext");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();

        // Test next
        Entry<String, JSOG> next = it.next();
        assertEquals("foo", next.getKey());
        assertEquals(new JSOG("bar"), next.getValue());

    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test(expected=NoSuchElementException.class)
    public void testObjectIteratorNextAfterLast() {
        System.out.println("testObjectIteratorNextAfterLast");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();

        // Test next
        it.next();
        it.next();
        fail("Expected an exception");
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorMultiple() {
        System.out.println("testObjectIteratorMultiple");

        JSOG instance = JSOG.object("foo", "bar")
                            .put("bar", "baz");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();

        // Test hasNext
        assertTrue(it.hasNext());

        // Test next
        Entry<String, JSOG> next = it.next();
        assertEquals("foo", next.getKey());
        assertEquals(new JSOG("bar"), next.getValue());

        // Test hasNext again
        assertTrue(it.hasNext());

        // Test next again
        next = it.next();
        assertEquals("bar", next.getKey());
        assertEquals(new JSOG("baz"), next.getValue());
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorMultipleRemove() {
        System.out.println("testObjectIteratorMultipleRemove");

        JSOG instance = JSOG.object("foo", "bar")
                            .put("bar", "baz");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();

        // Remove the first one
        it.next();
        it.remove();
        assertEquals(JSOG.object("bar", "baz"), instance);

        // Remove the next one
        it.next();
        it.remove();
        assertEquals(JSOG.object(), instance);
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorSetValueOldValue() {
        System.out.println("testObjectIteratorSetValueOldValue");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();
        Entry<String, JSOG> next = it.next();

        // Test setValue with a primitive
        JSOG oldValue = next.setValue(null);
        assertEquals(new JSOG("bar"), oldValue);
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorSetValueNull() {
        System.out.println("testObjectIteratorSetValueNull");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();
        Entry<String, JSOG> next = it.next();

        // Test setValue with a primitive
        next.setValue(null);
        assertTrue(next.getValue().isNull());

        // Make sure it passed through to the original JSOG
        assertEquals(JSOG.object("foo", null), instance);
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorSetValuePrimitive() {
        System.out.println("testObjectIteratorSetValuePrimitive");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();
        Entry<String, JSOG> next = it.next();

        // Test setValue with a primitive
        next.setValue(new JSOG("baz"));
        assertEquals(new JSOG("baz"), next.getValue());

        // Make sure it passed through to the original JSOG
        assertEquals(JSOG.object("foo", "baz"), instance);
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorSetValueObject() {
        System.out.println("testObjectIteratorSetValueObject");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();
        Entry<String, JSOG> next = it.next();

        // Test setValue with an object
        next.setValue(JSOG.object("bar", "baz"));
        assertEquals(JSOG.object("bar", "baz"), next.getValue());

        // Make sure it passed through to the original JSOG
        assertEquals(JSOG.object("foo", JSOG.object("bar", "baz")), instance);

    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorSetValueArray() {
        System.out.println("testObjectIteratorSetValueArray");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();
        Entry<String, JSOG> next = it.next();

        // Test setValue with an array
        next.setValue(JSOG.array("bar", "baz"));
        assertEquals(JSOG.array("bar", "baz"), next.getValue());

        // Make sure it passed through to the original JSOG
        assertEquals(JSOG.object("foo", JSOG.array("bar", "baz")), instance);
    }

    /**
     * Test of the objectIterator method, of class JSOG.
     */
    @Test
    public void testObjectIteratorRemove() {
        System.out.println("testObjectIteratorRemove");

        JSOG instance = JSOG.object("foo", "bar");

        Iterator<Entry<String, JSOG>> it = instance.objectIterator();
        Entry<String, JSOG> next = it.next();

        // Test remove
        it.remove();
        assertEquals(JSOG.object(), instance);

    }

    /**
     * Test of the objectIterable method, of class JSOG.
     */
    @Test
    public void testObjectIterable() {
        System.out.println("testObjectIterable");

        JSOG instance = JSOG.object("foo", "bar")
                            .put("bar", "baz");

        boolean firstDone = false;
        for (Entry<String, JSOG> entry : instance.objectIterable()) {
            if(firstDone == false) {
                assertEquals("foo", entry.getKey());
                assertEquals(new JSOG("bar"), entry.getValue());
                firstDone = true;
            } else {
                assertEquals("bar", entry.getKey());
                assertEquals(new JSOG("baz"), entry.getValue());
            }
        }
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test(expected=ConcurrentModificationException.class)
    public void testArrayIteratorCME() {
        System.out.println("testArrayIteratorCME");

        JSOG jsog = JSOG.array("foo");
        Iterator<JSOG> it = jsog.arrayIterator();

        jsog.add("bar");
        it.next();

        fail("Expected an exception");
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testArrayIteratorOnObject() {
        System.out.println("testArrayIteratorOnObject");

        JSOG.object().arrayIterator();
        fail("Expected an exception");
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testArrayIteratorOnPrimitive() {
        System.out.println("testArrayIteratorOnPrimitive");

        new JSOG("foo").arrayIterator();
        fail("Expected an exception");
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test
    public void testArrayIteratorOnNull() {
        System.out.println("testArrayIteratorOnNull");

        Iterator<JSOG> it = new JSOG(null).arrayIterator();

        assertFalse(it.hasNext());
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test(expected=NoSuchElementException.class)
    public void testArrayIteratorOnNullExceptionOnNext() {
        System.out.println("testArrayIteratorOnNullExceptionOnNext");

        Iterator<JSOG> it = new JSOG(null).arrayIterator();

        // Test next
        it.next();
        fail("Expected an exception");
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test(expected=IllegalStateException.class)
    public void testArrayIteratorOnNullExceptionOnRemove() {
        System.out.println("testArrayIteratorOnNullExceptionOnRemove");

        Iterator<JSOG> it = new JSOG(null).arrayIterator();

        // Test next
        it.remove();
        fail("Expected an exception");
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test
    public void testArrayIteratorHasNext() {
        System.out.println("testArrayIteratorHasNext");

        JSOG instance = JSOG.array("foo");

        Iterator<JSOG> it = instance.arrayIterator();

        // Test hasNext
        assertTrue(it.hasNext());

        // Get next and try hasNext again
        it.next();
        assertFalse(it.hasNext());
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test
    public void testArrayIteratorNext() {
        System.out.println("testArrayIteratorNext");

        JSOG instance = JSOG.array("foo");

        Iterator<JSOG> it = instance.arrayIterator();

        // Test next
        JSOG next = it.next();
        assertEquals(new JSOG("foo"), next);

    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test(expected=NoSuchElementException.class)
    public void testArrayIteratorNextAfterLast() {
        System.out.println("testArrayIteratorNextAfterLast");

        JSOG instance = JSOG.array("foo");

        Iterator<JSOG> it = instance.arrayIterator();

        // Test next
        it.next();
        it.next();
        fail("Expected an exception");
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test
    public void testArrayIteratorMultiple() {
        System.out.println("testArrayIteratorMultiple");

        JSOG instance = JSOG.array("foo", "bar");

        Iterator<JSOG> it = instance.arrayIterator();

        // Test hasNext
        assertTrue(it.hasNext());

        // Test next
        JSOG next = it.next();
        assertEquals(new JSOG("foo"), next);

        // Test hasNext again
        assertTrue(it.hasNext());

        // Test next again
        next = it.next();
        assertEquals(new JSOG("bar"), next);
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test
    public void testArrayIteratorMultipleRemove() {
        System.out.println("testArrayIteratorMultipleRemove");

        JSOG instance = JSOG.array("foo", "bar");

        Iterator<JSOG> it = instance.arrayIterator();

        // Remove the first one
        it.next();
        it.remove();
        assertEquals(JSOG.array("bar"), instance);

        // Remove the next one
        it.next();
        it.remove();
        assertEquals(JSOG.array(), instance);
    }

    /**
     * Test of the arrayIterator method, of class JSOG.
     */
    @Test
    public void testArrayIteratorRemove() {
        System.out.println("testArrayIteratorRemove");

        JSOG instance = JSOG.array("foo");

        Iterator<JSOG> it = instance.arrayIterator();
        it.next();

        // Test remove
        it.remove();
        assertEquals(JSOG.array(), instance);

    }

    /**
     * Test of the arrayIterable method, of class JSOG.
     */
    @Test
    public void testArrayIterable() {
        System.out.println("testArrayIterable");

        JSOG instance = JSOG.array("foo", "bar");

        boolean firstDone = false;
        for (JSOG value : instance.arrayIterable()) {
            if(firstDone == false) {
                assertEquals(new JSOG("foo"), value);
                firstDone = true;
            } else {
                assertEquals(new JSOG("bar"), value);
            }
        }
    }

    /**
     * Test of the hashCode method, of class JSOG.
     */
    @Test
    public void testHashCodePrimitive() {
        System.out.println("testHashCodePrimitive");

        assertEquals(new JSOG("foo").hashCode(), new JSOG("foo").hashCode());
    }

    /**
     * Test of the hashCode method, of class JSOG.
     */
    @Test
    public void testHashCodeNull() {
        System.out.println("testHashCodeNull");

        assertEquals(new JSOG(null).hashCode(), new JSOG(null).hashCode());
    }

    /**
     * Test of the hashCode method, of class JSOG.
     */
    @Test
    public void testHashCodeArray() {
        System.out.println("testHashCodeArray");

        assertEquals(JSOG.array("foo", "bar").hashCode(),
                     JSOG.array("foo", "bar").hashCode());
    }

    /**
     * Test of the hashCode method, of class JSOG.
     */
    @Test
    public void testHashCodeObject() {
        System.out.println("testHashCodeObject");

        assertEquals(JSOG.object("foo", "bar").hashCode(),
                     JSOG.object("foo", "bar").hashCode());
    }


    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsJSOG() {
        assertEquals(new JSOG(true), new JSOG(true).getValueAs(JSOG.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsString() {
        assertEquals("true", new JSOG(true).getValueAs(String.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsBoolean() {
        assertTrue(new JSOG(true).getValueAs(Boolean.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsByteWrapper() {
        assertEquals(Byte.valueOf(Byte.MAX_VALUE), new JSOG(Byte.MAX_VALUE).getValueAs(Byte.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsBytePrimitive() {
        assertEquals((byte) 0xFF, (byte) new JSOG(0xFF).getValueAs(byte.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test(expected=NullPointerException.class)
    public void testGetValueAsBytePrimitiveNull() {
        byte value = new JSOG(null).getValueAs(byte.class);
        fail("Expected an exception");
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsShortWrapper() {
        assertEquals(Short.valueOf(Short.MAX_VALUE), new JSOG(Short.MAX_VALUE).getValueAs(Short.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsShortPrimitive() {
        assertEquals((short) 0xFF, (short) new JSOG(0xFF).getValueAs(short.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test(expected=NullPointerException.class)
    public void testGetValueAsShortPrimitiveNull() {
        short value = new JSOG(null).getValueAs(short.class);
        fail("Expected an exception");
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsIntegerWrapper() {
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), new JSOG(Integer.MAX_VALUE).getValueAs(Integer.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsIntegerPrimitive() {
        assertEquals((int) 0xFF, (int) new JSOG(0xFF).getValueAs(int.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test(expected=NullPointerException.class)
    public void testGetValueAsIntegerPrimitiveNull() {
        int value = new JSOG(null).getValueAs(int.class);
        fail("Expected an exception");
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsLongWrapper() {
        assertEquals(Long.valueOf(Long.MAX_VALUE), new JSOG(Long.MAX_VALUE).getValueAs(Long.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsLongPrimitive() {
        assertEquals((long) 0xFF, (long) new JSOG(0xFF).getValueAs(long.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test(expected=NullPointerException.class)
    public void testGetValueAsLongPrimitiveNull() {
        long value = new JSOG(null).getValueAs(long.class);
        fail("Expected an exception");
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsFloatWrapper() {
        assertEquals(Float.valueOf(Float.MAX_VALUE), new JSOG(Float.MAX_VALUE).getValueAs(Float.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsFloatPrimitive() {
        assertEquals((float) 0xFF, (float) new JSOG(0xFF).getValueAs(float.class), 0);
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test(expected=NullPointerException.class)
    public void testGetValueAsFloatPrimitiveNull() {
        float value = new JSOG(null).getValueAs(float.class);
        fail("Expected an exception");
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsDoubleWrapper() {
        assertEquals(Double.valueOf(Double.MAX_VALUE), new JSOG(Double.MAX_VALUE).getValueAs(Double.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsDoublePrimitive() {
        assertEquals((double) 0xFF, (double) new JSOG(0xFF).getValueAs(double.class), 0);
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test(expected=NullPointerException.class)
    public void testGetValueAsDoublePrimitiveNull() {
        double value = new JSOG(null).getValueAs(double.class);
        fail("Expected an exception");
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsBigDecimalWrapper() {
        assertEquals(BigDecimal.ONE, new JSOG(BigDecimal.ONE).getValueAs(BigDecimal.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test
    public void testGetValueAsBigIntegerWrapper() {
        assertEquals(BigInteger.ONE, new JSOG(BigInteger.ONE).getValueAs(BigInteger.class));
    }

    /**
     * Test of the getValueAs method, of class JSOG.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testGetValueAsInvalidType() {
        new JSOG().getValueAs(URL.class);
    }

    @Test
    public void testPathString() throws Exception {
        JSOG result = JSOG.object("foo", "bar").path("$");
        assertEquals(JSOG.object("foo", "bar"), result);
    }

    @Test
    public void testPathJsogPath() throws Exception {
        JSOG result = JSOG.object("foo", "bar").path(JsogPath.compile("$"));
        assertEquals(JSOG.object("foo", "bar"), result);
    }

}
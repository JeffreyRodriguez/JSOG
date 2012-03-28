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

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * JavaScript Object Graph.
 *
 *
 * <p>JSOG instances are not thread-safe.</p>
 * @author <a href="mailto:jeff@jeffrodriguez.com">Jeff Rodriguez</a>
 */
public class JSOG implements Cloneable, Serializable {

    /**
     * An iterator which has no elements.
     * @param <T> the type of the iterator.
     */
    private static final class EmptyIterator<T> implements Iterator<T> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new IllegalStateException();
        }
    }

    /**
     * Wraps Entry<String,Object> objects so they return proper JSOGs.
     */
    private static final class ObjectEntry implements Entry<String, JSOG> {

        /**
         * The wrapped entry.
         */
        private Entry<String, Object> entry;

        /**
         * Constructs a new ObjectEntry wrapper.
         * @param entry the entry to wrap.
         */
        public ObjectEntry(final Entry<String, Object> entry) {
            this.entry = entry;
        }

        /**
         * Gets the key of the entry, this is a simple passthrough to the entry.
         * @return the value of the getKey method on the entry.
         */
        public String getKey() {
            return entry.getKey();
        }

        /**
         * Gets the wrapped value of the entry.
         * @return the wrapped value of the getValue method on the entry.
         */
        public JSOG getValue() {
            return wrap(entry.getValue());
        }

        /**
         * Unwraps the value and sets the value on the wrapped entry.
         * @param value the value to set.
         * @return the old value.
         */
        public JSOG setValue(final JSOG value) {
            Object oldValue = entry.getValue();

            if (value == null) {
                entry.setValue(null);
            } else if (isPrimitive(value.value)) {
                entry.setValue(value.value);
            } else {
                entry.setValue(value);
            }

            return wrap(oldValue);
        }

    }

    /**
     * An iterator for object JSOGs.
     */
    private final class ObjectIterator
                  implements Iterator<Entry<String, JSOG>> {

        /**
         * The map iterator backing this one.
         */
        private Iterator<Entry<String, Object>> it;

        /**
         * Used for fail-fast.
         */
        private int expectedModCount;

        /**
         * Creates a new ObjectIterator.
         *
         * Sets the expected modification count and gets the iterator.
         */
        @SuppressWarnings("unchecked")
        ObjectIterator() {
            expectedModCount = modCount;
            it = ((Map<String, Object>) value).entrySet().iterator();
        }

        /**
         * Passthrough to the backing iterator.
         * @return the value of the backing iterator's hasNext method.
         */
        public boolean hasNext() {
            return it.hasNext();
        }

        /**
         * Wraps the next value in an ObjectEntry.
         * @return the wrapped value of the next method of the backing iterator.
         */
        public Entry<String, JSOG> next() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            return new ObjectEntry(it.next());
        }

        /**
         * Passthrough to the backing iterator.
         */
        public void remove() {
            it.remove();
        }

    }

    /**
     * An iterator for array JSOGs.
     */
    private final class ArrayIterator implements Iterator<JSOG> {

        /**
         * The backing iterator.
         */
        private Iterator<Object> it;

        /**
         * For fail-fast.
         */
        private int expectedModCount;

        /**
         * Creates a new array iterator from this JSOG.
         */
        @SuppressWarnings("unchecked")
        ArrayIterator() {
            expectedModCount = modCount;
            it = ((List<Object>) value).iterator();
        }

        /**
         * Passthrough to the backing iterator.
         * @return the value of the backing iterator's hasNext method.
         */
        public boolean hasNext() {
            return it.hasNext();
        }

        /**
         * Wraps the value from the backing iterator.
         * @return the wrapped value of the iterator's next method.
         */
        public JSOG next() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            return wrap(it.next());
        }

        /**
         * Passthrough to the backing iterator.
         */
        public void remove() {
            it.remove();
        }

    }

    /**
     * The class version, for serialization purposes.
     */
    private static final long serialVersionUID = 1L;

    static {

        // A very accomodating JsonFactory
        JsonFactory jf = new JsonFactory();
        jf.configure(Feature.ALLOW_COMMENTS, true);
        jf.configure(Feature.ALLOW_SINGLE_QUOTES, true);
        jf.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        JACKSON_OBJECT_MAPPER = new ObjectMapper(jf);
    }

    /**
     * This object mapper is used to convert values into JsonNodes.
     */
    private static final ObjectMapper JACKSON_OBJECT_MAPPER;

    /**
     * Tests if a value is a primitive.
     *
     * The following types are considered primitives:
     * <ul>
     *   <li>null</li>
     *   <li>Boolean</li>
     *   <li>BigDecimal</li>
     *   <li>BigInteger</li>
     *   <li>Byte</li>
     *   <li>Character</li>
     *   <li>Short</li>
     *   <li>Integer</li>
     *   <li>Long</li>
     *   <li>Float</li>
     *   <li>Double</li>
     *   <li>String</li>
     * </ul>
     * @param value the value to test.
     * @return true if the object is a primitive.
     */
    public static boolean isPrimitive(final Object value) {
        return
               value == null
            || value.getClass().isPrimitive()
            || value instanceof Boolean
            || value instanceof BigDecimal
            || value instanceof BigInteger
            || value instanceof Byte
            || value instanceof Character
            || value instanceof Short
            || value instanceof Integer
            || value instanceof Long
            || value instanceof Float
            || value instanceof Double
            || value instanceof String;
    }

    /**
     * Tests if a value is a JavaScript array.
     * @param value the value to test.
     * @return true if the value is an array.
     */
    private static boolean isArray(final Object value) {
        return value instanceof List<?>;
    }

    /**
     * Tests if a value is a JavaScript object.
     * @param value the value to test.
     * @return true if the value is an object.
     */
    private static boolean isObject(final Object value) {
        return value instanceof Map<?, ?>;
    }

    /**
     * Converts a primitive into a JsonNode.
     * @param primitive the object to convert.
     * @return a JsonNode instance representing the primitive.
     * @throws IllegalArgumentException if the object can't be converted to a
     * primitive.
     */
    private static JsonNode getPrimitiveAsNode(final Object primitive) {

        // A handy shortcut
        JsonNodeFactory f = JACKSON_OBJECT_MAPPER.getNodeFactory();

        if (primitive == null) {
            return f.nullNode();
        } else if (primitive instanceof Boolean) {
            return f.booleanNode((Boolean) primitive);
        } else if (primitive instanceof BigDecimal) {
            return f.numberNode((BigDecimal) primitive);
        } else if (primitive instanceof BigInteger) {
            return f.numberNode((BigInteger) primitive);
        } else if (primitive instanceof Byte) {
            return f.numberNode((Byte) primitive);
        } else if (primitive instanceof Character) {
            return f.numberNode((Character) primitive);
        } else if (primitive instanceof Short) {
            return f.numberNode((Short) primitive);
        } else if (primitive instanceof Integer) {
            return f.numberNode((Integer) primitive);
        } else if (primitive instanceof Long) {
            return f.numberNode((Long) primitive);
        } else if (primitive instanceof Float) {
            return f.numberNode((Float) primitive);
        } else if (primitive instanceof Double) {
            return f.numberNode((Double) primitive);
        } else if (primitive instanceof String) {
            return f.textNode((String) primitive);
        }

        // We really shouldn't ever get here, it's protected by isPrimitive.
        throw new IllegalArgumentException(
                "Could not convert object of type "
                + primitive.getClass().getName()
                + " to primitive.");
    }

    /**
     * Converts an object to a JsonNode.
     * @param value the value to convert.
     * @return A JsonNode representing the value.
     * @throws IllegalStateException if the value could not be converted.
     */
    private static JsonNode toJsonNode(final Object value) {
        if (value instanceof JSOG) {

            // The value is another JSON, delegate to it's toJsonNode.
            return ((JSOG) value).toJsonNode();
        } else if (isArray(value)) {

            // This is an array, return an array node containing it's values.

            // Create an array node to store the values
            ArrayNode node = JACKSON_OBJECT_MAPPER.createArrayNode();

            // Add the values to the array node
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            for (Object item : list) {
                node.add(toJsonNode(item));
            }

            return node;
        } else if (isObject(value)) {

            // This is an object, return an object node containing it's values.

            // Create an object node to store the values
            ObjectNode node = JACKSON_OBJECT_MAPPER.createObjectNode();

            // Add the values to the object node
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            for (Entry<String, Object> entry : map.entrySet()) {
                node.put(entry.getKey(), toJsonNode(entry.getValue()));
            }

            return node;
        }

        // This is a primitive, convert it and return it.
        return getPrimitiveAsNode(value);
    }

    /**
     * Constructs a new JSON object from a serialized JSON string.
     * @param jsonString the serialized JSON string.
     * @return A JSOG representing the parsed string.
     * @throws IOException if unable to parse the string.
     */
    public static JSOG parse(final String jsonString) throws IOException {

        // If there's nothing to parse, create an empty JSON object.
        if (jsonString == null || jsonString.length() == 0) {
            return createValueNode();
        }

        // Otherwise, try and parse it.
        return new JSOG(JACKSON_OBJECT_MAPPER.readTree(jsonString));
    }

    /**
     * Creates a new value JSOG.
     * @return A new JSOG with the value null.
     * @deprecated use the {@link JSOG#JSOG()} constructor instead.
     */
    public static JSOG createValueNode() {
        return new JSOG();
    }

    /**
     * Creates a new value JSOG.
     * @param value the primitive value to set for the JSOG.
     * @return A new JSOG with the specified value.
     * @throws IllegalArgumentException if the value is not a primitive.
     * @deprecated use the {@link JSOG#JSOG(Object)} constructor instead.
     */
    public static JSOG createValueNode(final Object value) {
        return new JSOG().set(value);
    }

    /**
     * Creates a new object JSOG.
     * @return a new empty JSOG object.
     * @deprecated use {@link #object()} instead.
     */
    public static JSOG createObjectNode() {
        return object();
    }

    /**
     * Creates a new array JSOG.
     * @return a new empty JSOG array.
     * @deprecated use {@link #array()} instead.
     */
    public static JSOG createArrayNode() {
        return array();
    }

    /**
     * Creates a new array JSOG.
     * @return a new empty JSOG array.
     */
    public static JSOG array() {
        JSOG jsog = new JSOG();
        jsog.value = new ArrayList<Object>();
        return jsog;
    }

    /**
     * Creates a new JSOG array.
     * @param values The primitive values to be added to the array.
     * @return a JSOG array containing the specified values.
     * @throws IllegalArgumentException if the value is not a primitive.
     */
    public static JSOG array(final Object... values) {
        JSOG jsog = array();

        for (Object value : values) {
            jsog.add(value);
        }

        return jsog;
    }

    /**
     * Creates a new object JSOG.
     * @return a new empty JSOG object.
     */
    public static JSOG object() {
        JSOG jsog = new JSOG();
        jsog.value = new LinkedHashMap<String, Object>();
        return jsog;
    }

    /**
     * Creates a new object JSOG.
     *
     * This convenience method invokes {@link #put(String, Object)} on a new
     * JSOG and returns the resulting JSOG.
     *
     * @param key the key in which to store the value.
     * @param value the value to store.
     * @return a new JSOG containing the specified key and value.
     */
    public static JSOG object(final String key, final Object value) {
        return object().put(key, value);
    }

    /**
     * Creates a new object JSOG.
     *
     * This convenience method invokes {@link #put(String, Object)} on a new
     * JSOG and returns the resulting JSOG.
     *
     * Convenience wrapper for {@link #object(String,Object)}.
     * @param key the key in which to store the value.
     * @param value the value to store.
     * @return a new JSOG containing the specified key and value.
     */
    public static JSOG object(final Enum<?> key, final Object value) {
        return object(key == null ? null : key.toString(), value);
    }

    /**
     * Merge two JSOG instances.
     *
     * Merging follows these rules:
     * <ul>
     *   <li>Primitives in the source replace primitives
     *       in the destination.</li>
     *   <li>Objects and arrays are recursively merged.</li>
     *   <li>Arrays are ONLY additive.</li>
     *   <li>Object values either replace or recursively merge,
     *       depending on the value type.</li>
     * </ul>
     *
     * @param src the source JSOG.
     * @param dst the destination JSOG.
     */
    public static void merge(final JSOG src, final JSOG dst) {
        if (src.isObject()) {
            for (String key : src.getKeySet()) {

                // If the source value is a primitive, just set it
                JSOG srcKey = src.get(key);
                if (srcKey.isPrimitive()) {
                    dst.put(key, srcKey.value);
                } else {

                    // It's not a primitive, we need to recurse
                    merge(src.get(key), dst.get(key));
                }
            }
        } else if (src.isArray()) {

            // Make an array on the destination side if needed
            // This allows even empty arrays to be merged
            if (!dst.isArray()) {
                dst.value = new ArrayList<Object>(); // This is a slightly hacky
            }

            for (int i = 0; i < src.size(); i++) {

                // If the source value is a primitive, just set it
                JSOG srcKey = src.get(i);
                if (srcKey.isPrimitive()) {
                    dst.add(srcKey.value);
                } else {

                    // It's not a primitive, we need to recurse
                    JSOG value = new JSOG();
                    merge(src.get(i), value);
                    dst.add(value);
                }
            }
        } else {
            dst.set(src.getValue());
        }
    }

    /**
     * Wraps a value in a JSOG, if it is not already wrapped.
     * @param value the value to wrap
     * @return the wrapped value.
     */
    private static JSOG wrap(final Object value) {
        if (value instanceof JSOG) {
            return (JSOG) value;
        } else {
            return new JSOG(value);
        }
    }

    /**
     * The value of this  JSOG.
     */
    private Object value = null;

    /**
     * The number of times this JSOG has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the JSOG or otherwise modify its internal structure (e.g., add, put,
     * remove). This field is used to make iterators on Collection-views of
     * the JSOG fail-fast.
     * @see ConcurrentModificationException
     */
    private transient volatile int modCount = 0;

    /**
     * Constructs a new JSOG object from a JsonNode.
     * @param jsonNode the JsonNode.
     * @throws UnsupportedOperationException if the JsonNode could not be
     * deserialized.
     */
    private JSOG(final JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            ArrayNode array = (ArrayNode) jsonNode;
            Iterator<JsonNode> it = array.getElements();
            value = new ArrayList<Object>();
            while (it.hasNext()) {
                add(new JSOG(it.next()));
            }
        } else if (jsonNode.isObject()) {
            ObjectNode object = (ObjectNode) jsonNode;
            Iterator<Entry<String, JsonNode>> it = object.getFields();
            value = new LinkedHashMap<String, Object>();
            while (it.hasNext()) {
                Entry<String, JsonNode> entry = it.next();
                put(entry.getKey(), new JSOG(entry.getValue()));
            }
        } else if (jsonNode.isNull()) {
            set(null);
        } else if (jsonNode.isBoolean()) {
            set(jsonNode.getBooleanValue());
        } else if (jsonNode.isBigDecimal()) {
            set(jsonNode.getDecimalValue());
        } else if (jsonNode.isBigInteger()) {
            set(jsonNode.getBigIntegerValue());
        } else if (jsonNode.isDouble()) {
            set(jsonNode.getDoubleValue());
        } else if (jsonNode.isInt()) {
            set(jsonNode.getIntValue());
        } else if (jsonNode.isLong()) {
            set(jsonNode.getLongValue());
        } else if (jsonNode.isTextual()) {
            set(jsonNode.getTextValue());
        } else {
            throw new UnsupportedOperationException(
                    "Could not deserialize node: " + jsonNode);
        }
    }

    /**
     * Constructs a new null JSOG.
     * @see #isNull()
     */
    public JSOG() {

    }

    /**
     * Constructs a new JSOG.
     * @param value the primitive value to set for the JSOG.
     * @throws IllegalArgumentException if the value is not a primitive.
     * @see #set(Object)
     */
    public JSOG(final Object value) {
        set(value);
    }

    /**
     * Determines if the value of the JSOG is assignable from a particular type.
     *
     * Used by the get*Value() methods.
     * @param type the type to check.
     * @return true if the value is null or is assignable from the type.
     */
    private boolean isValueType(final Class<?> type) {

        // Don't do any more work than necessary
        if (value == null) {
            return true;
        }

        // Simple return?
        if (value.getClass().isAssignableFrom(type)) {
            return true;
        }

        return false;
    }

    /**
     * Tests if the value of this object is null.
     * @return true if the object is null.
     */
    public final boolean isNull() {
        return value == null;
    }

    /**
     * Tests if the value of this JSOG is a primitive.
     *
     * The following types are considered primitives:
     * <ul>
     *   <li>null</li>
     *   <li>Boolean</li>
     *   <li>BigDecimal</li>
     *   <li>BigInteger</li>
     *   <li>Byte</li>
     *   <li>Character</li>
     *   <li>Short</li>
     *   <li>Integer</li>
     *   <li>Long</li>
     *   <li>Float</li>
     *   <li>Double</li>
     *   <li>String</li>
     * </ul>
     * @return true if the object is a primitive.
     */
    public final boolean isPrimitive() {
        return isPrimitive(value);
    }

    /**
     * Tests if the value of this JSOG is an array.
     * @return true if the value is an array.
     */
    public final boolean isArray() {
        return isArray(value);
    }

    /**
     * Tests if the value of this JSOG is an object.
     * @return true if the value is an object.
     */
    public final boolean isObject() {
        return isObject(value);
    }

    /**
     * Treats this node as an array and adds the value to it.
     *
     * If this node is not an array, any previous values will be lost.
     * @param newValue the value to add.
     * @return the current JSOG object.
     */
    @SuppressWarnings("unchecked")
    public final JSOG add(final Object newValue) {

        // Make sure it's a primitive
        if (isPrimitive(newValue) || newValue instanceof JSOG) {

            // If the current value isn't a list, create one
            List<Object> list;
            try {
                list = (List<Object>) this.value;
            } catch (ClassCastException e) {
                list = new ArrayList<Object>();
                this.value = list;
            }
            if (list == null) {
                list = new ArrayList<Object>();
                this.value = list;
            }

            list.add(newValue);
            modCount++;

            return this;
        } else {
            throw new IllegalArgumentException(
                    "This method applies only to primitive"
                    + " types and other JSOG objects, not: "
                    + newValue.getClass().toString());
        }
    }

    /**
     * Treats this node as an array and adds the values to it.
     *
     * If this node is not an array, any previous values will be lost.
     *
     * This operation is not atomic, if one entry causes an exception, some of
     * the values will have been added.
     * @param values the values to add.
     * @return the current JSOG object.
     */
    @SuppressWarnings("unchecked")
    public final JSOG addAll(final Collection<Object> values) {
        for (Object newValue : values) {
            add(newValue);
        }

        return this;
    }

    /**
     * Treats this node as an array and adds the value to it at the
     * specified index.
     *
     * If this node is not an array, any previous values will be lost.
     * @param newValue the value to add.
     * @param index the index at which the specified element is to be inserted
     * @return the current JSOG object.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index < 0 || index > size())
     */
    @SuppressWarnings("unchecked")
    public final JSOG add(final int index, final Object newValue) {

        // Make sure it's a primitive
        if (isPrimitive(newValue) || newValue instanceof JSOG) {

            // If the current value isn't a list, create one
            List<Object> list;
            try {
                list = (List<Object>) this.value;
            } catch (ClassCastException e) {
                list = new ArrayList<Object>();
                this.value = list;
            }
            if (list == null) {
                list = new ArrayList<Object>();
                this.value = list;
            }

            list.add(index, newValue);
            modCount++;

            return this;
        } else {
            throw new IllegalArgumentException(
                    "This method applies only to primitive"
                    + " types and other JSOG objects, not: "
                    + newValue.getClass().toString());
        }
    }

    /**
     * Adds a value to a JSOG object.
     *
     * Implicitly converts this JSOG to an object.
     * @param key the key in which to store the value.
     * @param newValue the primitive value to store.
     * @return this JSOG.
     * @throws IllegalArgumentException if value is not a primitive or JSOG.
     * @throws NullPointerException if key is null.
     */
    @SuppressWarnings("unchecked")
    public final JSOG put(final String key,
                                       final Object newValue) {

        if (key == null) {
            throw new NullPointerException("key is null");
        }

        // Make sure it's a primitive
        if (isPrimitive(newValue) || newValue instanceof JSOG) {

            // If the current value isn't a map, create one
            Map<String, Object> map;
            try {
                map = (Map<String, Object>) this.value;
            } catch (ClassCastException e) {
                map = new LinkedHashMap<String, Object>();
                this.value = map;
            }
            if (map == null) {
                map = new LinkedHashMap<String, Object>();
                this.value = map;
            }

            map.put(key, newValue);
            modCount++;

            return this;
        } else {
            throw new IllegalArgumentException(
                    "This method applies only to primitive"
                    + " types and other JSOG objects, not: "
                    + newValue.getClass().toString());
        }
    }

    /**
     * Adds a value to a JSOG object.
     *
     * Implicitly converts this JSOG to an object.
     * @param key the key in which to store the value.
     * @param newValue the primitive value to store.
     * @return this JSOG.
     * @throws IllegalArgumentException if value is not a primitive or JSOG.
     * @throws NullPointerException if key is null.
     */
    public final JSOG put(final Enum<?> key,
                                       final Object newValue) {
        return put(key == null ? null : key.toString(), newValue);
    }

    /**
     * Adds the values of a map value to a JSOG object.
     *
     * Implicitly converts this JSOG to an object.
     *
     * This operation is not atomic, if one entry causes an exception, some of
     * the values will have been put.
     * @param values A map of keys and primitives to store.
     * @return this JSOG.
     * @throws IllegalArgumentException if a value is not a primitive or JSOG.
     * @throws NullPointerException if key is null.
     */
    public final JSOG putAll(final Map<String, Object> values) {
        for (Entry<String, Object> entry : values.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }

        return this;
    }

    /**
     * Sets the value of this JSOG object to a primitive.
     * @param newValue the primitive value to set.
     * @return this JSOG object.
     * @throws IllegalArgumentException if the value is not a primitive.
     */
    public final JSOG set(final Object newValue) {
        if (isPrimitive(newValue) || newValue instanceof JSOG) {
            this.value = newValue;
            return this;
        } else {
            throw new IllegalArgumentException(
                    "This method applies only to primitive"
                    + " types and other JSOG objects, not: "
                    + newValue.getClass().toString());
        }
    }

    /**
     * Navigates to an object field. If the field does not exist, it is created.
     *
     * Using this method, you can easily navigate the object graph of a JSOG.
     * @param key the object key.
     * @return the JSOG object identified by the key.
     */
    @SuppressWarnings("unchecked")
    public final JSOG get(final String key) {

        /* There's a lot of magic in this method, so I've documented the tar
         * out of it. I've also made it as simple to read as possible.
         * As a result, it's probably not as efficient or concise as possible.
         */

        // If this object doesn't have a value yet, coerce it to an object
        if (isNull()) {

            // We need to put a JSOG object into that key so we can navigate
            JSOG jsog = new JSOG();
            put(key, jsog);

            // Return the new JSOG object so it can be navigated.
            return jsog;
        }

        // We should be dealing with an object at this point
        Map<String, Object> map;
        try {
            map = (Map<String, Object>) value;
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    "Not null or an object,"
                    + " and will not be implicitly coerced.");
        }

        // Do we need to create this key, or does it already exist?
        Object theValue = map.get(key);
        if (theValue != null) {

            // The key already exists. Wrap it in a JSOG, if necessary, so we
            // can navigate.
            return wrap(theValue);
        } else {

            // The key doesn't exist
            // We need to put a JSOG object into that key so we can navigate
            JSOG jsog = new JSOG();
            put(key, jsog);

            // Return our new JSOG object
            return jsog;
        }
    }

    /**
     * Convenience wrapper for {@link #get(String)}.
     * @param key the object key.
     * @return the JSOG object identified by the key.
     */
    public final JSOG get(final Enum<?> key) {
        return get(key == null ? null : key.toString());
    }

    /**
     * Gets an element of an array.
     * @param index the index of the array to retrieve.
     * @return the value at the specified index.
     * @throws IndexOutOfBoundsException if the specified index is greater than
     * the size of the array.
     */
    @SuppressWarnings("unchecked")
    public final JSOG get(final int index) {
        List<Object> list;
        try {
            list = (List<Object>) value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("The JSOG is not an array.");
        }
        if (list == null) {
            throw new IllegalStateException("The JSOG is not an array.");
        }

        Object theValue = list.get(index);
        return wrap(theValue);
    }

    /**
     * Gets the index of an element.
     * @param value element to search for
     * @return the index of the first occurrence of the specified element in
     * this list, or -1 if this list does not contain the element.
     * @throws IllegalStateException if the JSOG is not an array.
     * @see List#indexOf(Object)
     */
    public final int indexOf(final Object value) {
        List<Object> list;
        try {
            list = (List<Object>) this.value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("The JSOG is not an array.");
        }
        if (list == null) {
            throw new IllegalStateException("The JSOG is not an array.");
        }

        return list.indexOf(value);
    }

    /**
     * Returns true if this JSOG is a list and contains the specified element.
     * @param value element whose presence in this list is to be tested
     * @return true if this list contains the specified element
     * @throws IllegalStateException if the JSOG is not an array.
     * @see List#contains(Object)
     */
    public final boolean contains(final Object value) {
        List<Object> list;
        try {
            list = (List<Object>) this.value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("The JSOG is not an array.");
        }
        if (list == null) {
            throw new IllegalStateException("The JSOG is not an array.");
        }

        return list.contains(value);
    }

    /**
     * Determines if an particular key exists in an object.
     * @param key the object key.
     * @return true if the key exists, false otherwise.
     * @throws IllegalStateException if the JSOG is not an object.
     */
    @SuppressWarnings("unchecked")
    public final boolean hasKey(final String key) {
        if (isNull()) {
            return false;
        }

        Map<String, Object> map;
        try {
            map = (Map<String, Object>) value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Not an object.");
        }

        return map.containsKey(key);
    }

    /**
     * Convenience wrapper for {@link #hasKey(String)}.
     * @param key the object key.
     * @return true if the key exists, false otherwise.
     * @throws IllegalStateException if the JSOG is not an object.
     */
    public final boolean hasKey(final Enum<?> key) {
        return hasKey(key == null ? null : key.toString());
    }

    /**
     * Removes an object field.
     * @param key the object key.
     * @return the value that was removed, or null if the key does not exist.
     * @throws IllegalStateException if the JSOG is not an object.
     */
    @SuppressWarnings("unchecked")
    public final Object remove(final String key) {
        Map<String, Object> map;
        try {
            map = (Map<String, Object>) value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Not an object.");
        }

        return map.remove(key);
    }

    /**
     * Convenience wrapper for {@link #remove(String)}.
     * @param key the object key.
     * @return the value that was removed, or null if the key does not exist.
     * @throws IllegalStateException if the JSOG is not an object.
     */
    public final Object remove(final Enum<?> key) {
        return remove(key == null ? null : key.toString());
    }

    /**
     * Removes an element from an array.
     * @param index the index of the array to remove.
     * @return the value that was removed.
     * @throws IndexOutOfBoundsException if the specified index is greater than
     * the size of the array.
     */
    @SuppressWarnings("unchecked")
    public final Object remove(final int index) {
        List<Object> list;
        try {
            list = (List<Object>) value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Not an array.");
        }

        return list.remove(index);
    }

    /**
     * Clears the value of the JSOG.
     *
     * If this is a value JSOG, it's set to null. If this is an array JSOG, it's
     * values are removed. If this is an object JSOG, it's entries are removed.
     * @return this JSOG.
     */
    @SuppressWarnings("unchecked")
    public final JSOG clear() {
        if (isObject()) {
            ((Map<String, Object>) value).clear();
        } else if (isArray()) {
            ((List<Object>) value).clear();
        } else {
            value = null;
        }

        return this;
    }

    /**
     * Gets the number of elements in the JSOG.
     * @return the size.
     * @throws IllegalStateException if value of the JSOG is not an array or
     * object.
     */
    @SuppressWarnings("unchecked")
    public final int size() {
        if (isArray()) {
            return ((List<Object>) value).size();
        } else if (isObject()) {
            return ((Map<String, Object>) value).size();
        }

        // Not an array or an object...
        throw new IllegalStateException(
                "The value of this JSOG is not an array or object.");
    }

    /**
     * Gets a set of keys contained by this JSOG object.
     *
     * The set has insertion-order based iteration.
     *
     * If the JSOG is null, an empty set is returned.
     * @return the set of keys contained in this JSOG object.
     * @throws IllegalStateException if the value of the JSOG is not an object.
     * @deprecated use {@link #keySet()} instead.
     */
    @SuppressWarnings("unchecked")
    public final Set<String> getKeySet() {
        if (isObject()) {
            return Collections.unmodifiableSet(
                    ((Map<String, Object>) value).keySet());
        } else if (isNull()) {
            return Collections.emptySet();
        }

        // Not an object...
        throw new IllegalStateException(
                "The value of this JSOG is not an object.");
    }

    /**
     * Gets a set of keys contained by this JSOG object.
     *
     * The set has insertion-order based iteration.
     *
     * If the JSOG is null, an empty set is returned.
     * @return the set of keys contained in this JSOG object.
     * @throws IllegalStateException if the value of the JSOG is not an object.
     */
    @SuppressWarnings("unchecked")
    public final Set<String> keySet() {
        return getKeySet();
    }

    /**
     * Gets an iterator for the entries in an object JSOG.
     * @return an entry iterator.
     */
    public final Iterator<Entry<String, JSOG>> objectIterator() {
        if (isNull()) {
            return new EmptyIterator<Entry<String, JSOG>>();
        }

        if (!isObject()) {
            // Not an object...
            throw new IllegalStateException(
                    "The value of this JSOG is not an object.");
        }

        return new ObjectIterator();
    }

    /**
     * Gets an iterator for the values in an array JSOG.
     * @return a value iterator.
     */
    public final Iterator<JSOG> arrayIterator() {
        if (isNull()) {
            return new EmptyIterator<JSOG>();
        }

        if (!isArray()) {
            // Not an arrau...
            throw new IllegalStateException(
                    "The value of this JSOG is not an array.");
        }

        return new ArrayIterator();
    }

    /**
     * An iterable for the entries in an object JSOG.
     *
     * For use in for-each loops.
     * @return an entry iterable.
     */
    public final Iterable<Entry<String, JSOG>> objectIterable() {
        return new Iterable<Entry<String, JSOG>>() {
            public Iterator<Entry<String, JSOG>> iterator() {
                return objectIterator();
            }
        };
    }

    /**
     * An iterable for the values in an array JSOG.
     *
     * For use in for-each loops.
     * @return a value iterable.
     */
    public final Iterable<JSOG> arrayIterable() {
        return new Iterable<JSOG>() {
            public Iterator<JSOG> iterator() {
                return arrayIterator();
            }
        };
    }

    /**
     * Gets the value of this JSOG.
     * @return the value of this JSOG.
     */
    public final Object getValue() {

        // We can only get the values of primitives and other JSOG values
        if (!isPrimitive() && !(value instanceof JSOG)) {
            throw new IllegalStateException(
                    "The value is not a primitive or JSOG");
        }
        return value;
    }

    /**
     * Gets the value of the JSOG as the specified type.
     *
     * This method invokes the corresponding get*Value method on the JSOG. Only
     * types with a corresponding get*Value method are supported.
     *
     * @param <T> the type of the value.
     * @param type the type of the value.
     * @return the value, as the specified type.
     * @throws IllegalArgumentException if the type is not a supported JSOG
     * type.
     * @throws NullPointerException if the type is a primitive value, and the
     * value of the JSOG is null.
     */
    public <T> T getValueAs(Class<T> type) {

        // JSOG
        if (type.isAssignableFrom(JSOG.class)) {
            return (T) this;
        }

        if (type.isAssignableFrom(String.class)) {
            return (T) this.getStringValue();
        }

        if (type.isAssignableFrom(Boolean.class)
                || type.isAssignableFrom(boolean.class)) {
            return (T) this.getBooleanValue();
        }

        if (type.isAssignableFrom(Byte.class)
                || type.isAssignableFrom(byte.class)) {
            return (T) this.getByteValue();
        }

        if (type.isAssignableFrom(Short.class)
                || type.isAssignableFrom(short.class)) {
            return (T) this.getShortValue();
        }

        if (type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(int.class)) {
            return (T) this.getIntegerValue();
        }

        if (type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(long.class)) {
            return (T) this.getLongValue();
        }

        if (type.isAssignableFrom(Float.class)
                || type.isAssignableFrom(float.class)) {
            return (T) this.getFloatValue();
        }

        if (type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(double.class)) {
            return (T) this.getDoubleValue();
        }

        if (type.isAssignableFrom(BigDecimal.class)) {
            return (T) this.getBigDecimalValue();
        }

        if (type.isAssignableFrom(BigInteger.class)) {
            return (T) this.getBigIntegerValue();
        }

        throw new IllegalArgumentException(type + " is not a JSOG type");
    }

    /**
     * Gets the value of this JSOG as a BigDecimal, if possible.
     *
     * If the value not a number, it's toString() method will be used.
     * @return the BigDecimal value of this JSOG, or null if the value is null.
     * @throws NumberFormatException if the value is not a number and parsing
     * it's toString() method fails.
     * @see Character#digit(char, int)
     */
    public final BigDecimal getBigDecimalValue() {

        // The easy way
        if (isValueType(BigDecimal.class)) {
            return (BigDecimal) value;
        }

        // Is it a float?
        if (value instanceof Float) {
            return BigDecimal.valueOf(((Float) value).doubleValue());
        }

        // Is it a double?
        if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        }

        // Another number?
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }

        // Try and make one out of the string
        return new BigDecimal(value.toString());
    }

    /**
     * Gets the value of this JSOG as a BigInteger, if possible.
     *
     * This may involve rounding.
     *
     * If the value not a number, it's toString() method will be used.
     * @return the BigInteger value of this JSOG, or null if the value is null.
     * @throws NumberFormatException if the value is not a number and parsing
     * it's toString() method fails.
     * @see Character#digit(char, int)
     */
    public final BigInteger getBigIntegerValue() {

        // The easy way
        if (isValueType(BigInteger.class)) {
            return (BigInteger) value;
        }

        // Another number?
        if (value instanceof Number) {
            return BigInteger.valueOf(((Number) value).longValue());
        }

        // Try and make one out of the string
        return new BigInteger(value.toString());
    }

    /**
     * Gets the value of this JSOG as a Byte, if possible.
     *
     * This may involve truncation and rounding.
     *
     * If the value not a number, it's toString() method will be used.
     * @return the Byte value of this JSOG, or null if the value is null.
     * @throws NumberFormatException if the value is not a number and parsing
     * it's toString() method fails.
     * @see Character#digit(char, int)
     */
    public final Byte getByteValue() {

        // The easy way
        if (isValueType(Byte.class)) {
            return (Byte) value;
        }

        // Another number?
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        // Try and make one out of the string
        return new Byte(value.toString());
    }

    /**
     * Gets the value of this JSOG as a Short, if possible.
     *
     * This may involve truncation and rounding.
     *
     * If the value not a number, it's toString() method will be used.
     * @return the Short value of this JSOG, or null if the value is null.
     * @throws NumberFormatException if the value is not a number and parsing
     * it's toString() method fails.
     * @see Character#digit(char, int)
     */
    public final Short getShortValue() {

        // The easy way
        if (isValueType(Short.class)) {
            return (Short) value;
        }

        // Another number?
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        // Try and make one out of the string
        return new Short(value.toString());
    }

    /**
     * Gets the value of this JSOG as an Integer, if possible.
     *
     * This may involve truncation and rounding.
     *
     * If the value not a number, it's toString() method will be used.
     * @return the Integer value of this JSOG, or null if the value is null.
     * @throws NumberFormatException if the value is not a number and parsing
     * it's toString() method fails.
     * @see Character#digit(char, int)
     */
    public final Integer getIntegerValue() {

        // The easy way
        if (isValueType(Integer.class)) {
            return (Integer) value;
        }

        // Another number?
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        // Try and make one out of the string
        return new Integer(value.toString());
    }

    /**
     * Gets the value of this JSOG as a Long, if possible.
     *
     * This may involve truncation and rounding.
     *
     * If the value not a number, it's toString() method will be used.
     * @return the Long value of this JSOG, or null if the value is null.
     * @throws NumberFormatException if the value is not a number and parsing
     * it's toString() method fails.
     * @see Character#digit(char, int)
     */
    public final Long getLongValue() {

        // The easy way
        if (isValueType(Long.class)) {
            return (Long) value;
        }

        // Another number?
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        // Try and make one out of the string
        return new Long(value.toString());
    }

    /**
     * Gets the value of this JSOG as a Float, if possible.
     *
     * This may involve rounding.
     *
     * If the value not a number, it's toString() method will be used.
     * @return the Float value of this JSOG, or null if the value is null.
     * @throws NumberFormatException if the value is not a number and parsing
     * it's toString() method fails.
     * @see Character#digit(char, int)
     */
    public final Float getFloatValue() {

        // The easy way
        if (isValueType(Float.class)) {
            return (Float) value;
        }

        // Another number?
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        // Try and make one out of the string
        return new Float(value.toString());
    }

    /**
     * Gets the value of this JSOG as a Double, if possible.
     *
     * This may involve rounding.
     *
     * If the value not a number, it's toString() method will be used.
     * @return the Double value of this JSOG, or null if the value is null.
     * @throws NumberFormatException if the value is not a number and parsing
     * it's toString() method fails.
     * @see Character#digit(char, int)
     */
    public final Double getDoubleValue() {

        // The easy way
        if (isValueType(Double.class)) {
            return (Double) value;
        }

        // Another number?
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        // Try and make one out of the string
        return new Double(value.toString());
    }

    /**
     * Gets the value of this JSOG as a String, if possible.
     *
     * If the value is not a string, the result of the value's toString()
     * method is returned.
     *
     * @return the String value of this JSOG, or null if the value is null.
     */
    public final String getStringValue() {

        // The easy way
        if (isValueType(String.class)) {
            return (String) value;
        }

        return value.toString();
    }

    /**
     * Gets the value of this JSOG as a Boolean, if possible.
     *
     * If the value not a boolean, it's toString() method will be used.
     * @return the Boolean value of this JSOG, or null if the value is null.
     * @see Boolean#parseBoolean(String)
     */
    public final Boolean getBooleanValue() {

        // The easy way
        if (isValueType(Boolean.class)) {
            return (Boolean) value;
        }

        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Evaluates a path expression.
     *
     * Note: This method always recompiles the path expression. If you will be
     * using the same path multiple times, consider using a compiled JsogPath.
     *
     * <p>
     * This is ALPHA quality code. The API is unlikely to change, and there are
     * test cases covering most of the "happy path" functionality, but corner
     * cases may cause unexpected behavior. Please submit feedback to
     * <a href="mailto:jeff@jeffrodriguez.com">Jeff Rodriguez</a>.
     * </p>
     * @param path the path expression to evaluate.
     * @return the result of the evaluation.
     * @see JsogPath#compile(String)
     * @see #path(JsogPath)
     */
    public JSOG path(String path) {
        return JsogPath.compile(path).evaluate(this);
    }

    /**
     * Evaluates a path expression.
     *
     * @param path the path expression to evaluate.
     * @return the result of the evaluation.
     * @see JsogPath#compile(String)
     * @see #path(JsogPath)
     */
    public JSOG path(JsogPath path) {
        return path.evaluate(this);
    }

    /**
     * Converts this JSOG to a JsonNode.
     * @return a JsonNode representing this JSOG.
     */
    public final JsonNode toJsonNode() {
        return toJsonNode(value);
    }

    /**
     * Merges the values of another JSOG into this one.
     * @param source the source JSOG.
     * @return this JSOG.
     * @see #merge(JSOG, JSOG)
     */
    public final JSOG merge(final JSOG source) {
        merge(source, this);

        return this;
    }

    @Override
    public final String toString() {
        return toJsonNode().toString();
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    /**
     * Does a loose comparison against another object.
     *
     * A "loose comparison" means that primitives are converted to strings
     * before they're compared.
     *
     * @param obj the object to compare.
     * @return true if the object is equal to this JSOG.
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JSOG)) {
            return false;
        }
        final JSOG that = (JSOG) obj;

        // Are the values identical?
        if (this.value != that.value) {

            // No. Are they both null?
            if (this.value == null && that.value != null) {

                // No, they're different
                return false;
            }

            // If they're primitives, we can directly compare them
            if (isPrimitive()) {
                return getStringValue().equals(that.getStringValue());
            }

            // If they're arrays, check each value of the array
            if (isArray()) {

                // Different types or sizes means they're non-equal
                if (!that.isArray() || size() != that.size()) {
                    return false;
                }

                // Check each value
                for (int i = 0; i < size(); i++) {
                    JSOG thisValue = this.get(i);
                    JSOG thatValue = that.get(i);

                    // Are they different?
                    if (!thisValue.equals(thatValue)) {
                        return false;
                    }
                }
            }

            // If they're objects, check each value of the object
            if (isObject()) {

                // Different types or sizes means they're non-equal
                if (!that.isObject() || size() != that.size()) {
                    return false;
                }

                // Check each value
                for (String key : this.getKeySet()) {
                    JSOG thisValue = this.get(key);
                    JSOG thatValue = that.get(key);

                    // Are they different?
                    if (!thisValue.equals(thatValue)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public final JSOG clone() {
        return new JSOG().merge(this);
    }

}

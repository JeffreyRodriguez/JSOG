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

import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A property filter that filters based on class name.
 *
 *
 * <p>
 * Features
 * <ul>
 *   <li>Supports wildcards (suffix only)</li>
 *   <li>Honors ordering</li>
 *   <li>Default exclude</li>
 * </ul>
 * 
 * Example usage:
 * <pre>
 * ClassNamePropertyFilter instance = new ClassNamePropertyFilter(true);
 * instance.include("foo.bar.*");
 * instance.exclude("java.lang.Integer");
 * instance.include("java.lang.*");
 * </pre>
 * </p>
 * @author jrodriguez
 */
public class ClassNamePropertyFilter implements PropertyFilter {

    private boolean primitives;

    /**
     * Creates a new ClassPropertyFilter.
     * @param primitives if true, primitives are included, otherwise they are
     * excluded.
     */
    public ClassNamePropertyFilter(boolean primitives) {
        this.primitives = primitives;
    }

    /**
     * A map of class names to actions.
     */
    private LinkedHashMap<String, Boolean> filterMap
            = new LinkedHashMap<String, Boolean>();

    public void include(String... classes) {
        for (String clazz : classes) {
            filterMap.put(clazz, Boolean.TRUE);
        }
    }

    public void exclude(String... classes) {
        for (String clazz : classes) {
            filterMap.put(clazz, Boolean.FALSE);
        }
    }

    @Override
    public boolean filter(PropertyDescriptor property) {
        Class<?> type = property.getPropertyType();
        if (type.isPrimitive()) {
            return primitives;
        }
        String clazz = property.getPropertyType().getName();
        for (Entry<String, Boolean> entry : filterMap.entrySet()) {
            if (entry.getKey().endsWith("*")) {
                String key = entry.getKey();
                key = key.substring(0, key.length() - 1);
                if (clazz.startsWith(key)) {
                    return entry.getValue();
                }
            } else {
                if (clazz.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return false;
    }

}

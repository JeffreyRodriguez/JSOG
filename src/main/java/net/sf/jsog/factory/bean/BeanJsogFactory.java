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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jsog.JSOG;

/**
 * Creates JSOG objects from JavaBeans.
 * @author jrodriguez
 */
public class BeanJsogFactory {

    private static final Logger logger =
            Logger.getLogger(BeanJsogFactory.class.toString());

    private static volatile BeanJsogFactory singleton;

    private PropertyFilter defaultFilter = new NoPropertyFilter();

    /**
     * Gets the BeanJsogFactory singleton instance.
     * @return the BeanJsogFactory singleton instance.
     */
    public static BeanJsogFactory getSingleton() {
        if (singleton == null) {
            synchronized (BeanJsogFactory.class) {
                if (singleton == null) {
                    singleton = new BeanJsogFactory();
                }
            }
        }

        return singleton;
    }

    /**
     * Creates a JSOG from a bean.
     * @param bean the source bean.
     * @return a JSOG representing the bean's properties.
     * @throws IntrospectionException if the bean's information could not be
     * obtained.
     * @throws InvocationTargetException if a property getter throws an
     * exception.
     */
    public JSOG create(final Object bean)
           throws IntrospectionException,
                  InvocationTargetException {
        return create(bean, defaultFilter, new Stack<Object>());
    }

    /**
     * Creates a JSOG from a bean, filtering properties.
     * @param bean the source bean.
     * @param filter the property filter to use.
     * @return a JSOG representing the bean's properties.
     * @throws IntrospectionException if the bean's information could not be
     * obtained.
     * @throws InvocationTargetException if a property getter throws an
     * exception.
     */
    public JSOG create(final Object bean, final PropertyFilter filter)
           throws IntrospectionException,
                  InvocationTargetException {
        return create(bean, filter, new Stack<Object>());
    }

    @SuppressWarnings("unchecked")
    private JSOG create(final Object bean, final PropertyFilter filter, Stack<Object> visited)
           throws IntrospectionException,
                  InvocationTargetException {
        
        // If bean is null, return a null JSOG
        if (bean == null) {
            logger.log(Level.FINE, "Creating null JSOG from null bean.");
            return new JSOG();
        }

        // The resulting object
        logger.log(Level.FINE, "Creating object JSOG from bean {0}@{1}",
                new Object[] {bean.getClass(), bean.hashCode()});
        JSOG result = JSOG.object();

        // Get bean properties
        BeanInfo info = Introspector.getBeanInfo(bean.getClass());
        PropertyDescriptor[] properties = info.getPropertyDescriptors();

        // Map them to the JSOG
        for (PropertyDescriptor property : properties) {
            String name = property.getName();

            // getClass() is returned as a property. Skip it
            if ("class".equals(name)) {
                continue;
            }

            // Run the filter on the property
            if (!filter.filter(property)) {
                continue;
            }

            // Get the value of the property
            logger.log(Level.FINEST, "Getting property {0} from bean {1}@{2}",
                    new Object[] {name, bean.getClass(), bean.hashCode()});
            Object value;
            try {
                value = property.getReadMethod().invoke(bean);
            } catch (IllegalAccessException e) {
                throw new Error("Bean property should be accessible.", e);
            } catch (IllegalArgumentException e) {
                throw new Error("Bean property should be no-arg.", e);
            }

            // Assign primitives, create JSOG from beans
            if (JSOG.isPrimitive(value)) {
                logger.log(Level.FINER,
                           "Mapping property {0} of bean {1}@{2}"
                           + " with value {3}",
                           new Object[] {name, bean.getClass(), bean.hashCode(),
                                         value});
                result.put(name, value);
            } else {
                logger.log(Level.FINER,
                           "Mapping property {0} of bean {1}@{2}"
                           + " with value {3}@{4}",
                           new Object[] {name, bean.getClass(), bean.hashCode(),
                                         value.getClass(), value.hashCode()});

                // Circular reference checking)
                visited.push(bean);
                if (visited.contains(value)) {
                    StringBuilder message = new StringBuilder();
                    for (Object object : visited) {
                        message.append(object.getClass() + "@" + object.hashCode() + "\n");
                    }
                    message.append(value.getClass() + "@" + value.hashCode());
                    
                    throw new IllegalArgumentException(
                            "Circular reference detected:\n" + message.toString());
                }

                result.put(name, create(value, filter, visited));
                visited.pop();
            }
        }

        // Return the resulting object
        return result;
    }

    /**
     * Creates an array-JSOG from a list of beans.
     * @param beans the beans.
     * @return an array-JGOG.
     * @throws IntrospectionException if a bean's information could not be
     * obtained.
     * @throws InvocationTargetException if a property getter throws an
     * exception.
     */
    public JSOG create(final List<?> beans)
           throws IntrospectionException,
                  InvocationTargetException {
        return create(beans, defaultFilter);
    }

    /**
     * Creates an array-JSOG from a list of beans.
     * @param beans the beans.
     * @param filter
     * @return an array-JGOG.
     * @throws IntrospectionException if a bean's information could not be
     * obtained.
     * @throws InvocationTargetException if a property getter throws an
     * exception.
     */
    public JSOG create(final List<?> beans, final PropertyFilter filter)
           throws IntrospectionException,
                  InvocationTargetException {
        JSOG jsog = JSOG.array();

        if (beans != null) {
            for (Object bean : beans) {
                jsog.add(create(bean, filter));
            }
        }

        return jsog;
    }

    /**
     * Creates an object-JSOG from a map of beans.
     * @param beans the beans.
     * @return an object-JGOG.
     * @throws IntrospectionException if a bean's information could not be
     * obtained.
     * @throws InvocationTargetException if a property getter throws an
     * exception.
     */
    public JSOG create(final Map<String, ?> beans)
           throws IntrospectionException,
                  InvocationTargetException {
        return create(beans, defaultFilter);
    }

    /**
     * Creates an object-JSOG from a map of beans.
     * @param beans the beans.
     * @return an object-JGOG.
     * @throws IntrospectionException if a bean's information could not be
     * obtained.
     * @throws InvocationTargetException if a property getter throws an
     * exception.
     */
    public JSOG create(final Map<String, ?> beans, final PropertyFilter filter)
           throws IntrospectionException,
                  InvocationTargetException {
        JSOG jsog = JSOG.object();

        if (beans != null) {
            for (Entry<String, ?> entry : beans.entrySet()) {
                jsog.put(entry.getKey(), create(entry.getValue(), filter));
            }
        }

        return jsog;
    }
}










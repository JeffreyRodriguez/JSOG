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
package net.sf.jsog.dynamic;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsog.JSOG;
import static net.sf.jsog.JsogPath.compile;

/**
 * Uses {@link JsogPath} annotations to support methods whose arguments are
 * dynamically assigned from a JSOG.
 * @param <T> the method's return type.
 * @author jrodriguez
 */
public class JsogMethod<T> {

    /**
     * An annotation-based-method parameter.
     */
    private static class Parameter {
        int position;
        Class<?> type;
        net.sf.jsog.JsogPath path;
        boolean required;

        public Parameter(int position,
                         Class<?> type,
                         net.sf.jsog.JsogPath path,
                         boolean required) {
            this.position = position;
            this.type = type;
            this.path = path;
            this.required = type.isPrimitive() || required;
        }
    }

    /**
     * Determine if a class is a JSOG value type.
     * @return true if the type is a JSOG value type.
     */
    private static boolean isJsogType(Class<?> type) {
        return type.isPrimitive()
            || type.isAssignableFrom(JSOG.class)
            || type.isAssignableFrom(String.class)
            || type.isAssignableFrom(Boolean.class)
            || type.isAssignableFrom(Byte.class)
            || type.isAssignableFrom(Short.class)
            || type.isAssignableFrom(Integer.class)
            || type.isAssignableFrom(Long.class)
            || type.isAssignableFrom(Float.class)
            || type.isAssignableFrom(Double.class)
            || type.isAssignableFrom(BigDecimal.class)
            || type.isAssignableFrom(BigInteger.class);
    }

    /**
     * Searches an Annotation[] a {@link JsogPath} annotation.
     * @param annotations the parameter's annotations.
     * @return the first {@link JsogPath} annotation found, or null.
     */
    private static JsogPath getAnnotation(Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (a.annotationType().isAssignableFrom(JsogPath.class)) {
                return (JsogPath) a;
            }
        }

        return null;
    }

    /**
     * The Java method backing the content method.
     */
    private Method method;

    /**
     * The method's parameters.
     */
    private List<Parameter> parameters = new ArrayList<Parameter>();

    /**
     * Constructs a new JsogMethod.
     *
     * Eagerly compiles the JsogPath expressions into JsogPath objects.
     * @param method the method which the invoker should operate upon.
     * @throws IllegalStateException if any of the method's arguments are not
     * supported JSOG types.
     */
    public JsogMethod(Method method) {
        this.method = method;

        // Build the parameter list
        buildParameters();
    }

    /**
     * Constructs a new JsogMethod.
     *
     * Obtains the first method with the specified name from the specified
     * class.
     *
     * Eagerly compiles the JsogPath expressions into JsogPath objects.
     * @param type the class from which to obtain the method.
     * @param name the name of the method which the invoker should operate upon.
     * @throws IllegalStateException if any of the method's arguments are not
     * supported JSOG types.
     */
    public JsogMethod(Class<?> type, String name) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                this.method = method;
                break;
            }
        }

        if (this.method == null) {
            throw new IllegalArgumentException(
                    "No method with name " + name + " found on " + type);
        }

        // Build the parameter list
        buildParameters();
    }

    /**
     * Creates the method's parameters.
     */
    private void buildParameters() {
        Class<?> types[] = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();

        // Create the parameters
        for (int i = 0; i < types.length; i++) {

            // Make sure the argument is JSOG-compatible
            Class<?> type = types[i];
            if (!isJsogType(type)) {
                throw new IllegalStateException(String.format(
                        "Parameter %d on %s is not a supported JSOG type.",
                        i, method));
            }

            // Get the JsogPath annotation
            JsogPath annotation = getAnnotation(annotations[i]);
            if (annotation == null) {
                throw new IllegalStateException(method
                        + " parameter " + i
                        + " is not annotated with @JsogPath");
            }

            // Create the parameter object and add it to the parameter list
            Parameter parameter = new Parameter(i, type,
                    compile(annotation.value()), annotation.required());
            
            parameters.add(parameter);
        }
    }

    /**
     * Builds an Object[] that can be passed to
     * {@link Method#invoke(Object, Object[])}.
     *
     * @param jsog the JSOG on which the JsogPath expressions are to be
     * evaluated.
     * @return an Object[] of arguments.
     * @throws IllegalArgumentException if a parameter is required but the value
     * is null.
     */
    private Object[] buildArguments(JSOG jsog) {

        // The arguments will be stored here
        Object[] args = new Object[this.parameters.size()];

        // Iterate over the parameters and build the argument array
        for (int i = 0; i < args.length; i++) {

            // Get the parameter
            Parameter parameter = this.parameters.get(i);

            // Evaluate the JsogPath on the input JSOG
            JSOG value = parameter.path.evaluate(jsog);

            // If the parameter is required, require it
            if (parameter.required && value.isNull()) {
                throw new IllegalArgumentException(
                        "The `" + parameter.path + "' parameter is required");
            }

            // Get the argument
            args[i] = value.getValueAs(parameter.type);
        }

        // Done, return the arguments
        return args;
    }

    /**
     * Invokes the method with the specified JSOG as the argument source.
     * @param obj - the object the underlying method is invoked from
     * @param jsog the JSOG on the JsogPath expressions will be evaluated.
     * @return the method's return value.
     * @throws InvocationTargetException if the method throws an exception.
     * @throws IllegalArgumentException {@link Method#invoke(Object, Object[])}
     * @see Method#invoke(Object, Object[])
     * @see Void#TYPE
     */
    public final T invoke(final Object obj, final JSOG jsog)
                 throws InvocationTargetException {

        // Build the arguments for the invocation
        Object[] arguments = buildArguments(jsog);
        try {
            // Invoke the method and return it's value
            return (T) method.invoke(obj, arguments);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                    "Could not invoke target method.", e);
        } catch (IllegalArgumentException e) {

            // We already checked for this in buildParameters()
            throw new IllegalStateException(
                    "Could not invoke target method.", e);
        }
    }
    
}

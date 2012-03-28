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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Builds a URL.
 * @author jrodriguez
 */
public class UrlBuilder implements Cloneable {

    /**
     * The base URL.
     */
    private String base;

    // Could use a MultiKeyMap if we ever use commons-lang.
    /**
     * The query parameters to the URL.
     */
    private LinkedHashMap<String, ArrayList<String>> parameters =
            new LinkedHashMap<String, ArrayList<String>>();

    /**
     * Constructs a new UrlBuilder.
     * @param base the base URL.
     */
    public UrlBuilder(final String base) {
        this.base = base;
    }

    /**
     * Adds a parameter value.
     *
     * If a parameter with the specified name already exists, this value will be
     * added as an additional parameter.
     * @param name the name of the parameter.
     * @param value the parameter value.
     * @return the UrlBuilder for chaining purposes.
     */
    public final synchronized UrlBuilder add(final String name,
                                             final String value) {
        ArrayList<String> list = parameters.get(name);
        if (list == null) {
            list = new ArrayList<String>();
            parameters.put(name, list);
        }

        list.add(value);

        return this;
    }

    /**
     * Sets a parameter's value.
     *
     * If a parameter with the specified name already exists, it's previous
     * values are cleared and the new value is added.
     * @param name the name of the parameter.
     * @param value the parameter value.
     * @return the UrlBuilder for chaining purposes.
     */
    public final synchronized UrlBuilder set(final String name,
                                             final String value) {
        ArrayList<String> list = parameters.get(name);
        if (list == null) {
            list = new ArrayList<String>();
            parameters.put(name, list);
        }

        list.clear();
        list.add(value);

        return this;
    }

    /**
     * Sets a parameter's values.
     *
     * If a parameter with the specified name already exists, it's previous
     * values are cleared and the new values are set.
     * @param name the name of the parameter.
     * @param values the parameter values.
     * @return the UrlBuilder for chaining purposes.
     */
    public final synchronized UrlBuilder set(final String name,
                                             final Collection<String> values) {
        parameters.put(name, new ArrayList<String>(values));

        return this;
    }

    /**
     * Removes all the parameters.
     */
    public final synchronized void clear() {
        parameters.clear();
    }

    /**
     * Removes all values for the named parameter.
     * @param name the parameter name.
     */
    public final synchronized void remove(final String name) {
        parameters.remove(name);
    }

    /**
     * Removes the specified value of the named parameter.
     *
     * If the specified value, or parameter doesn't exist, no changes are made.
     * @param name the parameter name.
     * @param value the value to remove.
     */
    public final synchronized void remove(final String name,
                                          final String value) {
        List<String> list = parameters.get(name);
        if (list != null) {
            list.remove(value);
        }
    }

    /**
     * Gets the values of a parameter.
     *
     * @param name the parameter name.
     * @return an unmodifiable list containing the parameter's values.
     */
    public final synchronized List<String> get(final String name) {
        if (parameters.containsKey(name)) {
            return Collections.unmodifiableList(parameters.get(name));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns true if the specified parameter is present.
     * @param name the parameter name.
     * @return true if the specified parameter is present.
     */
    public final synchronized boolean has(final String name) {
        return parameters.containsKey(name);
    }
    
    /**
     * Gets the base URL.
     * @return the base URL.
     */
    public final synchronized String getBase() {
        return base;
    }
    
    /**
     * Sets the base URL.
     * 
     * The previous base URL will be replaced.
     * @param base the base URL.
     * @return the UrlBuilder for chaining purposes.
     */
    public final synchronized UrlBuilder setBase(String base) {
        this.base = base;
        
        return this;
    }

    /**
     * Builds the URL.
     * @return the URL, with all parameters.
     */
    @Override
    public final synchronized String toString() {
        try {

            // Build the base URL
            StringBuilder url = new StringBuilder(base);

            if (!parameters.isEmpty()) {
                url.append("?");
            }

            // Iterate over the parameter names
            Iterator<String> parameterNames = parameters.keySet().iterator();
            while (parameterNames.hasNext()) {

                // Iterate over the values for the parameter
                String name = parameterNames.next();
                Iterator<String> values = parameters.get(name).iterator();
                while (values.hasNext()) {

                    // Add the value
                    url.append(URLEncoder.encode(name, "US-ASCII"));
                    url.append("=");
                    url.append(URLEncoder.encode(values.next(), "US-ASCII"));

                    if (values.hasNext()) {
                        url.append("&");
                    }
                }

                if (parameterNames.hasNext()) {
                    url.append("&");
                }
            }

            return url.toString();
        } catch (UnsupportedEncodingException e) {
            throw new Error("US-ASCII is not a supported encoding.", e);
        }
    }

    /**
     * Builds the URL.
     *
     * Arguments are NOT urlencoded.
     * @param arguments the arguments to replace.
     * @return the url, with all parameters and formatted with the arguments.
     * @see MessageFormat#format(String, Object[])
     */
    public final synchronized String toString(final String... arguments) {
        return MessageFormat.format(toString(), (Object[]) arguments);
    }

    @Override
    public UrlBuilder clone() {

        // Create the clone object
        UrlBuilder clone = new UrlBuilder(this.base);

        // Copy the parameters, cloned
        for (String name : parameters.keySet()) {
            for (String value : parameters.get(name)) {
                clone.add(name, value);
            }
        }

        return clone;
    }

}

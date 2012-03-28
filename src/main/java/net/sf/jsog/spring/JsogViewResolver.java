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
package net.sf.jsog.spring;

import java.util.Locale;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.ViewResolver;

/**
 * Directs requests to the JsogView.
 * @author jrodriguez
 */
public class JsogViewResolver implements ViewResolver, Ordered {

    private int order = Integer.MAX_VALUE;
    
    private String viewName = null;
    
    private JsogView view = new JsogView();

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Sets the view name that will trigger this view resolver.
     * If this is not set, any view name will trigger the view resolver.
     * Defaults to null.
     * @param viewName the view name.
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    /**
     * The {@link JsogView} to use for rendering the JSOG.
     * @param view a JsogView.
     */
    public void setView(JsogView view) {
        this.view = view;
    }
    
    /**
     * If the resolver's {@code viewName} is null, or equals the
     * {@code viewName} parameter, this method returns the configured
     * {@link JsogView}.
     * 
     * Otherwise, it returns null.
     * @param viewName the name of the view.
     * @param locale unused.
     * @return the JsogView, or null.
     */
    public JsogView resolveViewName(String viewName, Locale locale) {
        if (this.viewName == null || this.viewName.equals(viewName)) {
            return view;
        } else {
            return null;
        }
    }

}

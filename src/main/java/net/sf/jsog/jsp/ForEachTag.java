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
package net.sf.jsog.jsp;

import java.io.IOException;
import java.io.Writer;
import java.util.Map.Entry;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import net.sf.jsog.JSOG;

/**
 *
 * @author Jeff
 */
public class ForEachTag extends SimpleTagSupport {

    /**
     * The JSOG array or object to iterate over.
     */
    private JSOG items;

    public void setItems(JSOG items) {
        this.items = items;
    }

    /**
     * Name of the exported scoped variable for the current item of the iteration. This scoped variable has nested visibility. Its type depends on the object of the underlying collection.
     */
    private String var;

    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    @Override
    public void doTag() throws JspException {

        // Nothing to do if the JSOG is empty
        if (items == null || items.isNull()) {
            return;
        }

        // Get the body and writer
        JspFragment body = getJspBody();
        Writer out = body.getJspContext().getOut();

        // Evaluate if the JSOG is an array or object
        try {
            if (items.isArray()) {
                for (JSOG item : items.arrayIterable()) {
                    getJspContext().setAttribute(var, item);
                    body.invoke(out);
                }

                return;
            } else if (items.isObject()) {
                for (Entry<String, JSOG> entry : items.objectIterable()) {
                    getJspContext().setAttribute(var, entry);
                    body.invoke(out);
                }

                return;
            }
        } catch (IOException e) {
            throw new JspException(e.getMessage(), e);
        }

        throw new JspException("JSOG is not an array or object.");
    }
}

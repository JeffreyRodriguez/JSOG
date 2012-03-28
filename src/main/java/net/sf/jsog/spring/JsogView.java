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

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jsog.JSOG;
import net.sf.jsog.factory.bean.BeanJsogFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Renders JSON from a model.
 * @author jrodriguez
 */
public class JsogView extends AbstractView {

    private MediaType outputContentType = MediaType.APPLICATION_JSON;

    /**
     * The output content type to use.
     * @param outputContentType the output content type.
     */
    public void setOutputContentType(MediaType outputContentType) {
        this.outputContentType = outputContentType;
    }

    private Charset encoding = Charset.forName("ISO-8859-1");

    /**
     * This encoding will be used as the default when reading, and the encoding
     * when writing if the output content type's charset is null.
     * @param encoding the encoding to set.
     * @see MediaType#getCharSet()
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     * This encoding will be used as the default when reading, and the encoding
     * when writing if the output content type's charset is null.
     * @param encoding the encoding to set.
     * @see MediaType#getCharSet()
     */
    public void setEncoding(String encoding) {
        this.encoding = Charset.forName(encoding);
    }
    
    private String jsonpCallbackParam = "callback";

    /**
     * If specified, and the parameter is set in the request, the JSOG is
     * wrapped for JSONP.
     * 
     * Defaults to "callback".
     * @param jsonpCallbackParam the name of the callback parameter.
     */
    public void setJsonpCallbackParam(String jsonpCallbackParam) {
        this.jsonpCallbackParam = jsonpCallbackParam;
    }
    
    private BeanJsogFactory beanJsogFactory = new BeanJsogFactory();

    public void setBeanJsogFactory(BeanJsogFactory beanJsogFactory) {
        this.beanJsogFactory = beanJsogFactory;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model,
                                           HttpServletRequest request,
                                           HttpServletResponse response)
              throws Exception {
                  
        // This is the string that will ultimately be rendered.
        String responseString;
        
        // Build the result object
        JSOG result;
        if ((model.size() == 1 || model.size() == 2)
                && model.containsKey("JSOG")) {
            result = (JSOG) model.get("JSOG");
        } else {
            result = modelToJsog(model);
        }
        
        // If the JSONP callback parameter is specified, grab it
        String callback = request.getParameter(jsonpCallbackParam);
        if (callback != null) {
            responseString = callback + "(" + result.toString() + ")";
        } else {
            responseString = result.toString();
        }

        // Setup the response
        byte[] responseBytes = responseString.getBytes(encoding);
        response.setContentType(outputContentType.toString());
        response.setCharacterEncoding(encoding.name());
        response.setContentLength(responseBytes.length);
        
        // Write the response
        OutputStream out = response.getOutputStream();
        out.write(responseBytes);
        out.flush();
        out.close();
    }
    
    protected JSOG modelToJsog(Map<String, Object> model) throws Exception {
        
        // We'll be storing the model attributes here
        JSOG result = JSOG.object();
        
        for (Entry<String, Object> entry : model.entrySet()) {
            Object value = entry.getValue();
            
            // Spring inserts BindingResult objects, skip them.
            if (entry.getValue() instanceof BindingResult) {
                continue;
            }

            if (JSOG.isPrimitive(value) || value instanceof JSOG) {
                
                // JSOG primitives can be stored directly
                result.put(entry.getKey(), value);
            } else {
                
                // Non-primitives must go through the BeanJsogFactory
                result.put(entry.getKey(), beanJsogFactory.create(value));
            }
        }
        
        return result;
    }

}

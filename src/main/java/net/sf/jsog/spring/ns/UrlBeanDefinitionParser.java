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
package net.sf.jsog.spring.ns;

import net.sf.jsog.spring.UrlBuilderFactoryBean;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsog.spring.UrlBuilderFactoryBean.Parameter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 *
 * @author jrodriguez
 */
public class UrlBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder
                .rootBeanDefinition(UrlBuilderFactoryBean.class);
        
        factory.addPropertyValue("baseUrl", element.getAttribute("base"));
        
        parseParameters(DomUtils.getChildElements(element), factory);
        
        return factory.getBeanDefinition();
    }
    
    private void parseParameters(List<Element> elements,
                                 BeanDefinitionBuilder factory) {
        
        // We're going to store the parameters here as SimpleEntry objects
        List<Parameter> parameters = new ArrayList<Parameter>();
                
        for (Element element : elements) {
            parameters.add(parseParameter(element));
        }
        
        factory.addPropertyValue("parameters", parameters);
    }
    
    private Parameter parseParameter(Element element) {
        String name = element.getAttribute("name");
        String value = element.getAttribute("value");
        return new Parameter(name, value);
    }
    
}

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
package net.sf.jsog.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Stack;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import net.sf.jsog.JSOG;

/**
 * An XML to JSOG factory.
 * 
 * <p>Provides methods to convert an XML string into an equivalent JSOG using
 * the Badgerfish convention.</p>
 *
 * <p>Namespaces are not currently supported.</p>
 * <p>
 * Conversion Rules copied from the website are enumerated below:
 * <ul>
 *   <li>Element names become object properties</li>
 *   <li>Text content of elements goes in the $ property of an object.<br/>
 *     {@code <alice>bob</alice>}<br/>
 *     becomes<br/>
 *     {@code { "alice": { "$" : "bob" } }}</li>
 *
 *   <li>Nested elements become nested properties<br/>
 *     {@code <alice><bob>charlie</bob><david>edgar</david></alice>}<br/>
 *     becomes<br/>
 *     {@code { "alice": { "bob" : { "$": "charlie" }, "david": { "$": "edgar"} } }}</li>
 *
 *   <li>Multiple elements at the same level become array elements.<br/>
 *     {@code <alice><bob>charlie</bob><bob>david</bob></alice>}<br/>
 *     becomes<br/>
 *     {@code { "alice": { "bob" : [{"$": charlie" }, {"$": "david" }] } }}</li>
 *
 *   <li>Attributes go in properties whose names begin with @.<br/>
 *     {@code <alice charlie="david">bob</alice>}<br/>
 *     becomes<br/>
 *     {@code { "alice": { "$" : "bob", "@charlie" : "david" } }}</li>
 *
 * </p>
 * @author jrodriguez
 */
public class XmlJsogFactory {

    private static volatile XmlJsogFactory singleton;

    /**
     * Gets the XmlJsogFactory singleton instance.
     * @return the XmlJsogFactory singleton instance.
     */
    public static XmlJsogFactory getSingleton() {
        if (singleton == null) {
            synchronized (XmlJsogFactory.class) {
                if (singleton == null) {
                    XMLInputFactory in = XMLInputFactory.newInstance();
                    in.setProperty(
                            XMLInputFactory.IS_COALESCING,
                            Boolean.TRUE);

                    in.setProperty(
                            XMLInputFactory.IS_NAMESPACE_AWARE,
                            Boolean.TRUE);
                    
                    in.setProperty(
                            XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
                            Boolean.TRUE);

                    singleton = new XmlJsogFactory(in);
                }
            }
        }

        return singleton;
    }

    /**
     * We'll be using this to create parsers.
     */
    private XMLInputFactory inputFactory;

    /**
     * Constructs a new XmlJsogFactory instance.
     * @param inputFactory the XMLInputFactory to use for parsing.
     * @see #getSingleton()
     */
    public XmlJsogFactory(XMLInputFactory inputFactory) {
        this.inputFactory = inputFactory;
    }

    /**
     * Parses an XML document.
     * @param the XML document to parse.
     * @return a JSOG representing the document.
     * @throws XMLStreamException if an exception occurs while parsing.
     */
    public final JSOG read(String xmlString) throws XMLStreamException {
        return read(new ByteArrayInputStream(xmlString.getBytes()));
    }

    /**
     * Parses an XML document.
     * @param in the source for the document's contents.
     * @return a JSOG representing the document.
     * @throws XMLStreamException if an exception occurs while parsing.
     */
    public final JSOG read(InputStream in) throws XMLStreamException {
        XMLEventReader eventReader = inputFactory.createXMLEventReader(
                in, Charset.defaultCharset().name());

        // The root element JSOG.
        JSOG root = null;

        // The last element JSOG.
        Stack<JSOG> last = new Stack<JSOG>();

        // Process the event stream
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            switch (event.getEventType()) {
                case XMLEvent.START_ELEMENT: {

                    // We'll be using this a few times
                    StartElement start = (StartElement) event;

                    // Get they JSOG key for the element
                    String elementKey = getElementKey(start);

                    // Handle the root node
                    if (root == null) {
                        last.push(root = JSOG.object());
                    } else {
                        
                        // Any other node
                        JSOG next = JSOG.object();
                        setOrAddOnLast(last.lastElement(), elementKey, next);
                        last.push(next);
                    }

                    // Handle attributes
                    Iterator<Attribute> attributes = start.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        last.lastElement().put(
                                getAttributeKey(attribute),
                                attribute.getValue());
                    }
                } break;
                case XMLEvent.END_ELEMENT: {
                    last.pop();
                } break;
                case XMLEvent.CHARACTERS: {
                    String text = ((Characters) event).getData().trim();
                    if (text.length() > 0) {
                        setOrAddOnLast(last.lastElement(), "$", text);
                    }
                } break;
            }
        }

        return root;
    }

    /**
     * Gets the name of an element, to be used as a key in a JSOG object.
     *
     * Any namespace handling should happen here.
     * @param start the element start tag event.
     * @return the key.
     */
    private String getElementKey(StartElement start) {
        return start.getName().getLocalPart();
    }

    /**
     * Gets the name of an attribute, to be used as a key in a JSOG object.
     *
     * Any namespace handling should happen here.
     * @param attribute the attribute event.
     * @return the key.
     */
    private String getAttributeKey(Attribute attribute) {
        return "@" + attribute.getName().getLocalPart();
    }

    /**
     * If {@link #last} already contains {@code key}, then
     * {@link JSOG#put(String, Object)} performed.
     *
     * Otherwise, if {@link JSOG#isArray()} is false for the key, it will be
     * converted to an array. The original value will be added to the array, and
     * the new value will also be added.
     *
     * @param last the JSOG on which to perform the operation.
     * @param key the key on which to set/add.
     * @param value the value to set or add.
     */
    private static void setOrAddOnLast(JSOG last, String key, Object value) {

        // Get the jsog
        if (!last.hasKey(key)) {

            // This is the first
            last.put(key, value);
        } else {

            // This is at least the second
            JSOG jsog = last.get(key);
            if (!jsog.isArray()) {

                // This is the second. Convert the old value to an array
                last.put(key, jsog = JSOG.array(jsog));
            }

            // Push a new JSOG onto the JSOG stack and add it to the array
            jsog.add(value);
        }
    }
}

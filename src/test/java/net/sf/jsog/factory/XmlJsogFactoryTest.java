package net.sf.jsog.factory;

import net.sf.jsog.JSOG;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jrodriguez
 */
public class XmlJsogFactoryTest {

    XmlJsogFactory instance;

    @Before
    public void setUp() {
        instance = XmlJsogFactory.getSingleton();
    }

    @Test
    public void testRootEmpty() throws Exception {
        assertEquals(JSOG.object(), instance.read("<foo/>"));
    }

    @Test
    public void testRootWithAttribute() throws Exception {
        assertEquals(JSOG.object("@foo", "bar"),
                     instance.read("<foo foo=\"bar\"/>"));
    }

    @Test
    public void testRootWithAttributeAndText() throws Exception {
        assertEquals(JSOG.object("@foo", "bar").put("$", "baz"),
                     instance.read("<foo foo=\"bar\">baz</foo>"));
    }

    @Test
    public void testNestedEmpty() throws Exception {
        assertEquals(JSOG.object("bar", JSOG.object()),
                     instance.read("<foo><bar/></foo>"));
    }

    @Test
    public void testNestedWithAttribute() throws Exception {
        assertEquals(JSOG.object("@foo", "bar"),
                     instance.read("<foo foo=\"bar\"/>"));
    }

    @Test
    public void testNestedWithAttributeAndText() throws Exception {
        assertEquals(JSOG.object("@foo", "bar").put("$", "baz"),
                     instance.read("<foo foo=\"bar\">baz</foo>"));
    }

    @Test
    public void testNestedMultipleElementsWithSameName() throws Exception {
        assertEquals(JSOG.object("bar", JSOG.array(
                            JSOG.object(),
                            JSOG.object(),
                            JSOG.object())),
                     instance.read("<foo><bar/><bar/><bar/></foo>"));
        
    }

    @Test
    public void testComplex() throws Exception {
        assertEquals(JSOG.object("@bar", "baz")
                         .put("qux", JSOG.array(
                                         JSOG.object(),
                                         JSOG.object("@able", "baker")
                                             .put("quux", JSOG.object()
                                                              .put("@alpha", "bravo")
                                                              .put("$", "foobar")))),
                     instance.read("<foo bar=\"baz\"><qux/><qux able=\"baker\"><quux alpha=\"bravo\">foobar</quux></qux></foo>"));

    }

    @Test
    public void testStripWhitespace() throws Exception {
        assertEquals(JSOG.object("bar", JSOG.array(
                                            JSOG.object(),
                                            JSOG.object("$", "baz"))),
                     instance.read("<foo>\n  <bar/><bar> baz </bar></foo>"));
    }

}
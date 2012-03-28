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
package net.sf.jsog;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:jeff@jeffrodriguez.com">Jeff Rodriguez</a>
 */
public class JsogPathTest {

    @Test
    public void testEscapeTooShort() throws Exception {
        System.out.println("testEscapeTooShort");
        try {
            JsogPath.evaluate("$.\\", new JSOG());
            fail("Expected an exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("end-of-string"));
        }
    }

    @Test
    public void testEscapeUnknownCharacter() throws Exception {
        System.out.println("testEscapeUnknownCharacter");
        try {
            JsogPath.evaluate("$.\\a", new JSOG());
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("valid escape"));
        }
    }

    @Test
    public void testEscapeUnicodeTooShort() throws Exception {
        System.out.println("testEscapeUnicodeTooShort");
        try {
            JsogPath.evaluate("$.\\u00F", new JSOG());
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("end-of-string"));
        }
    }

    @Test
    public void testCompileNull() throws Exception {
        System.out.println("testCompileNull");
        try {
            JsogPath.compile(null);
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("null"));
        }
    }

    @Test
    public void testCompileEmpty() throws Exception {
        System.out.println("testCompileEmpty");
        try {
            JsogPath.compile("");
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("empty"));
        }
    }

    @Test
    public void testCompileWithNoDollarSign() throws Exception {
        System.out.println("testCompileWithNoDollarSign");
        try {
            JsogPath.compile(".foo");
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("begin with $"));
        }
    }

    @Test
    public void testCompileWithNothingAfterOpeningBracket() throws Exception {
        System.out.println("testCompileWithNothingAfterOpeningBracket");
        try {
            JsogPath.compile("$[");
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("end-of-string"));
        }
    }

    @Test
    public void testCompileWithBadCharacters() throws Exception {
        System.out.println("testCompileWithBadCharacters");
        try {
            JsogPath.compile("$-");
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unexpected character "));
        }
    }

    @Test
    public void testRoot() throws Exception {
        System.out.println("testRoot");
        JSOG result = JsogPath.evaluate("$", JSOG.object("foo", "bar"));
        assertEquals(JSOG.object("foo", "bar"), result);
    }

    @Test
    public void testDot() throws Exception {
        System.out.println("testDot");
        JSOG result = JsogPath.evaluate("$.foo", JSOG.object("foo", "bar"));
        assertEquals("bar", result.getStringValue());
    }

    @Test
    public void testDotUnicode() throws Exception {
        System.out.println("testDotUnicode");
        JSOG result = JsogPath.evaluate("$.\\u00F1", JSOG.object("\u00F1", "bar"));
        assertEquals("bar", result.getStringValue());
    }

    @Test
    public void testDotWithNumber() throws Exception {
        System.out.println("testDotWithNumber");
        JSOG result = JsogPath.evaluate("$.foo1", JSOG.object("foo1", "bar"));
        assertEquals("bar", result.getStringValue());
    }

    @Test
    public void testDotWithDash() throws Exception {
        System.out.println("testDotWithDash");
        JSOG result = JsogPath.evaluate("$.foo-1", JSOG.object("foo-1", "bar"));
        assertEquals("bar", result.getStringValue());
    }

    @Test
    public void testDotWithNestedDots() throws Exception {
        System.out.println("testDotWithNestedDots");

        JSOG value = JSOG.object();
        value.get("foo").get("bar").get("baz").set("qux");

        JSOG result = JsogPath.evaluate("$.foo.bar.baz", value);
        assertEquals("qux", result.getStringValue());
    }

    @Test
    public void testDotWithNestedBrackets() throws Exception {
        System.out.println("testDotWithNestedBrackets");

        JSOG value = JSOG.object();
        value.get("foo").get("bar").get("baz").set("qux");

        JSOG result = JsogPath.evaluate("$.foo[\"bar\"].baz", value);
        assertEquals("qux", result.getStringValue());
    }

    @Test
    public void testDotWithNestedDotsToNonExistentProperty() throws Exception {
        System.out.println("testDotWithNestedDots");

        JSOG value = JSOG.object();

        // Make sure we navigate at all
        JSOG result = JsogPath.evaluate("$.foo.bar.baz", value);
        assertTrue(result.isNull());

        // Make sure we navigate properly
        result.set("qux");
        assertEquals("qux", value.get("foo").get("bar").get("baz").getStringValue());
    }

    @Test
    public void testDotWithBadChars() throws Exception {
        System.out.println("testDotWithBadChars");

        JSOG value = JSOG.object();
        value.get("foo").get("bar").get("baz").set("qux");

        try {
            JsogPath.evaluate("$.fo$o.bar.baz", value);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Expected"));
        }
    }

    @Test
    public void testBracketStrtingWithNumberAndHasAlphas() throws Exception {
        System.out.println("testBracketStrtingWithNumberAndHasAlphas");
        try {
            JsogPath.evaluate("$[0foo]", JSOG.object("foo", "bar"));
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must be quoted"));
        }
    }

    @Test
    public void testBracketObjectWithMissingEnd() throws Exception {
        System.out.println("testBracketObjectWithMissingEnd");
        try {
            JsogPath.evaluate("$['foo", JSOG.object("foo", "bar"));
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("end-of-string"));
        }
    }

    @Test
    public void testBracketArrayWithMissingEnd() throws Exception {
        System.out.println("testBracketArrayWithMissingEnd");
        try {
            JsogPath.compile("$[123");
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("end-of-string"));
        }
    }

    @Test
    public void testBracketWithMissingQuoteAtStart() throws Exception {
        System.out.println("testBracketWithMissingQuoteAtStart");
        try {
            JsogPath.evaluate("$[foo']", JSOG.object("foo", "bar"));
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must be quoted"));
        }
    }

    @Test
    public void testBracketWithMissingQuoteAtEnd() throws Exception {
        System.out.println("testBracketWithMissingQuoteAtEnd");
        try {
            JsogPath.evaluate("$['foo]", JSOG.object("foo", "bar"));
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("end-of-string"));
        }
    }

    @Test
    public void testBracketWithMissingEndBracket() throws Exception {
        System.out.println("testBracketWithMissingEndBracket");
        try {
            JsogPath.evaluate("$['foo'", JSOG.object("foo", "bar"));
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("end-of-string"));
        }
    }

    @Test
    public void testBracketWithJunkAfterQuote() throws Exception {
        System.out.println("testBracketWithJunkAfterQuote");
        try {
            JsogPath.evaluate("$['foo'junk]", JSOG.object("foo", "bar"));
            fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("end-of-string"));
        }
    }

    @Test
    public void testBracketWithSingleQuotedKey() throws Exception {
        System.out.println("testBracketWithSingleQuotedKey");
        JSOG result = JsogPath.evaluate("$['foo']", JSOG.object("foo", "bar"));
        assertEquals("bar", result.getStringValue());
    }

    @Test
    public void testBracketWithDoubleQuotedKey() throws Exception {
        System.out.println("testBracketWithDoubleQuotedKey");
        JSOG result = JsogPath.evaluate("$[\"foo\"]", JSOG.object("foo", "bar"));
        assertEquals("bar", result.getStringValue());
    }

    @Test
    public void testBracketWithIndexZero() throws Exception {
        System.out.println("testBracketWithIndexZero");
        JSOG result = JsogPath.evaluate("$[0]", JSOG.array("foo", "bar"));
        assertEquals("foo", result.getStringValue());
    }

    @Test
    public void testBracketWithIndexOne() throws Exception {
        System.out.println("testBracketWithIndexOne");
        JSOG result = JsogPath.evaluate("$[1]", JSOG.array("foo", "bar"));
        assertEquals("bar", result.getStringValue());
    }

    @Test
    public void testBracketWithKeyAndNestedBrackets() throws Exception {
        System.out.println("testBracketWithKeyAndNestedBrackets");

        JSOG value = JSOG.object();
        value.get("foo").get("bar").get("baz").set("qux");

        JSOG result = JsogPath.evaluate("$['foo']['bar']['baz']", value);
        assertEquals("qux", result.getStringValue());
    }

    @Test
    public void testBracketWithKeyAndNestedBracketsToNonExistentProperty() throws Exception {
        System.out.println("testBracketWithKeyAndNestedBracketsToNonExistentProperty");

        JSOG value = JSOG.object();

        // Make sure we navigate at all
        JSOG result = JsogPath.evaluate("$['foo']['bar']['baz']", value);
        assertTrue(result.isNull());

        // Make sure we navigate properly
        result.set("qux");
        assertEquals("qux", value.get("foo").get("bar").get("baz").getStringValue());
    }

    @Test
    public void testBracketWithIndexAndNestedBrackets() throws Exception {
        System.out.println("testBracketWithIndexAndNestedBrackets");

        JSOG value = JSOG.array("foo", JSOG.array("bar"));

        JSOG result = JsogPath.evaluate("$[1][0]", value);
        assertEquals("bar", result.getStringValue());
    }

    @Test
    public void testBracketWithEscapeChar_singleQuote() throws Exception {
        System.out.println("testBracketWithEscapeChar_singleQuote");

        JSOG value = JSOG.object("'", "foo");

        JSOG result = JsogPath.evaluate("$['\\'']", value);
        assertEquals("foo", result.getStringValue());
    }

    @Test
    public void testBracketWithEscapeChar_doubleQuote() throws Exception {
        System.out.println("testBracketWithEscapeChar_doubleQuote");

        JSOG value = JSOG.object("\"", "foo");

        JSOG result = JsogPath.evaluate("$['\\\"']", value);
        assertEquals("foo", result.getStringValue());
    }

    @Test
    public void testBracketWithEscapeChar_backslash() throws Exception {
        System.out.println("testBracketWithEscapeChar_backslash");

        JSOG value = JSOG.object("\\", "foo");

        JSOG result = JsogPath.evaluate("$['\\\\']", value);
        assertEquals("foo", result.getStringValue());
    }

    @Test
    public void testBracketWithEscapeChar_forwardslash() throws Exception {
        System.out.println("testBracketWithEscapeChar_forwardslash");

        JSOG value = JSOG.object("/", "foo");

        JSOG result = JsogPath.evaluate("$['\\/']", value);
        assertEquals("foo", result.getStringValue());
    }

    @Test
    public void testBracketWithEscapeChar_newline() throws Exception {
        System.out.println("testBracketWithEscapeChar_newline");

        JSOG value = JSOG.object("\n", "foo");

        JSOG result = JsogPath.evaluate("$['\\n']", value);
        assertEquals("foo", result.getStringValue());
    }

    @Test
    public void testBracketWithEscapeChar_carriageReturn() throws Exception {
        System.out.println("testBracketWithEscapeChar_carriageReturn");

        JSOG value = JSOG.object("\r", "foo");

        JSOG result = JsogPath.evaluate("$['\\r']", value);
        assertEquals("foo", result.getStringValue());
    }

    @Test
    public void testBracketWithEscapeChar_tab() throws Exception {
        System.out.println("testBracketWithEscapeChar_tab");

        JSOG value = JSOG.object("\t", "foo");

        JSOG result = JsogPath.evaluate("$['\\t']", value);
        assertEquals("foo", result.getStringValue());
    }

    @Test
    public void testBracketWithEscapeChar_utf8() throws Exception {
        System.out.println("testBracketWithEscapeChar_utf8");

        JSOG value = JSOG.object("\u1234", "foo");

        JSOG result = JsogPath.evaluate("$['\\u1234']", value);
        assertEquals("foo", result.getStringValue());
    }

    @Test(expected=NumberFormatException.class)
    public void testBracketWithEscapeChar_utf8Broken() throws Exception {
        System.out.println("testBracketWithEscapeChar_utf8Broken");

        JsogPath.evaluate("$['\\u124']", JSOG.object());
    }

    @Test(expected=NumberFormatException.class)
    public void testBracketWithIllegalEscapeChar() throws Exception {
        System.out.println("testBracketWithIllegalEscapeChar");

        JsogPath.evaluate("$['\\u124']", JSOG.object());
    }

    @Test
    public void testToStringArray() {
        System.out.println("testToStringArray");
        assertEquals("$[0]", JsogPath.compile("$[0]").toString());
    }

    @Test
    public void testToStringNestedArray() {
        System.out.println("testToStringNestedArray");
        assertEquals("$[0][1]", JsogPath.compile("$[0][1]").toString());
    }

    @Test
    public void testToStringObject() {
        System.out.println("testToStringObject");
        assertEquals("$[\"foo\"]", JsogPath.compile("$.foo").toString());
    }

    @Test
    public void testToStringObjectUnicode() {
        System.out.println("testToStringUnicode");
        assertEquals("$[\"\u00F1\"]", JsogPath.compile("$.\\u00F1").toString());
    }

    @Test
    public void testToStringNestedObject() {
        System.out.println("testToStringNestedObject");
        assertEquals("$[\"foo\"][\"bar\"]", JsogPath.compile("$.foo.bar").toString());
    }

    @Test
    public void testToStringNestedMixed() {
        System.out.println("testToStringNestedMixed");
        assertEquals("$[\"foo\"][0][\"bar\"]", JsogPath.compile("$.foo[0].bar").toString());
    }

    @Test
    public void testToStringNestedMixedUnicode() {
        System.out.println("testToStringUnicode");
        assertEquals("$[\"\u00F1\"][\"foo\"][0]", JsogPath.compile("$.\\u00F1['foo'][0]").toString());
    }

    @Test
    public void testToStringRoot() {
        System.out.println("testToStringRoot");
        assertEquals("$", JsogPath.compile("$").toString());
    }

}
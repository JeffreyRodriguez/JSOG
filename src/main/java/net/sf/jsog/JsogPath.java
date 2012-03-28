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

import java.io.Serializable;

/**
 * A JSOG path expression.
 *
 * <p>A JSOG path expression allows for easy nested navigation using dot and
 * bracket syntax. For example:
 * <pre>$.foo['bar'].baz[0]</pre>
 * </p>
 * @author jrodriguez
 */
public final class JsogPath implements Serializable {

    private static enum Type {
        root,
        array,
        object;
    }

    private static class Index {
        private final int characters;
        private final int index;

        public Index(int characters, int index) {
            this.characters = characters;
            this.index = index;
        }

        public int getCharacters() {
            return characters;
        }

        public int getIndex() {
            return index;
        }
    }

    private static class Key {
        private final int characters;
        private final String key;

        public Key(int characters, String key) {
            this.characters = characters;
            this.key = key;
        }

        public int getCharacters() {
            return characters;
        }

        public String getKey() {
            return key;
        }

    }

    private static class Escape {
        static final Escape doubleQuote    = new Escape(1, "\"");
        static final Escape singleQuote    = new Escape(1, "\'");
        static final Escape tab            = new Escape(1, "\t");
        static final Escape newline        = new Escape(1, "\n");
        static final Escape carriageReturn = new Escape(1, "\r");
        static final Escape forwardSlash   = new Escape(1, "/");
        static final Escape backslash      = new Escape(1, "\\");

        private final int characters;
        private final String escape;
        
        private Escape(int characters, String escape) {
            this.characters = characters;
            this.escape = escape;
        }

        public int getCharacters() {
            return characters;
        }

        public String getEscape() {
            return escape;
        }

    }

    private static final long serialVersionUID = 1L;

    /**
     * Parses an escape character.
     *
     * The following escape characters are supported.
     * <ul>
     *   <li>" - Double quote</li>
     *   <li>' - Single quote</li>
     *   <li>t - Tab</li>
     *   <li>n - Newline</li>
     *   <li>r - Carriage Return</li>
     *   <li>/ - Forward Slash</li>
     *   <li>\\ - Backslash</li>
     *   <li>u#### - Unicode values</li>
     * </ul>
     * @param string the string that is being parsed.
     * @param offset The position of the escape character.
     * @return the number of additional characters that were consumed.
     */
    private static Escape parseEscape(String string, int offset) {

        // Read the next character
        if (offset + 1 >= string.length()) {
            throw new IllegalArgumentException(
                    "Reached end-of-string at index " + offset
                    + ". Expected an escape character: "
                    + "\", ', \\, /, n, r, t, u####");
        }
        char c = string.charAt(offset + 1);

        if (c == '"') {
            return Escape.doubleQuote;
        } else if (c == '\'') {
            return Escape.singleQuote;
        } else if (c == '/') {
            return Escape.forwardSlash;
        } else if (c == '\\') {
            return Escape.backslash;
        } else if (c == 'n') {
            return Escape.newline;
        } else if (c == 'r') {
            return Escape.carriageReturn;
        } else if (c == 't') {
            return Escape.tab;
        } else if (c == 'u') {
            String chars = parseUnicode(string, offset + 2);
            return new Escape(5, chars);
        } else {
            throw new IllegalArgumentException("Unexpected character: " + c
                    + " at index " + offset
                    + ". Expected a valid escape character.");
        }
    }

    /**
     * Parses a Unicode character, encoded as a 4-digit hex number.
     * @param string the string from which to compile.
     * @param offset the beginning index of the character (after the \\u).
     * @return the resulting character(s).
     */
    private static String parseUnicode(String string, int offset) {

        // Make sure there are 4 characters available
        if (offset + 4 > string.length()) {
            throw new IllegalArgumentException(
                    "Reached end-of-string at index " + offset
                    + ". Expected a 4-digit Unicode value.");
        }

        // Get the substring with the characters
        String substring = string.substring(offset, offset + 4);

        // Parse them as hex
        int number = Integer.parseInt(substring, 16);

        // Convert the number into a character array
        char[] chars = Character.toChars(number);
        return new String(chars);
    }

    /**
     * Parses a dot-based child navigation path segment.
     * @param path the path.
     * @param offset the index in the path string.
     * @return the resulting Key
     */
    private static Key parseDotKey(final String path, final int offset) {

        // Get the full name of the child key
        StringBuilder key = new StringBuilder();
        int characters = 0;
        for (int i = offset + 1; i < path.length(); i++) {

            // Get the next character
            char c = path.charAt(i);

            // It should be either a letter or digit
            if (Character.isLetter(c) || Character.isDigit(c) || c == '-') {
                key.append(c);
                continue;
            }

            // Handle escape characters
            if (c == '\\') {
                Escape escape = parseEscape(path, i);
                i += escape.getCharacters();
                key.append(escape.getEscape());
                characters += escape.getCharacters();
                continue;
            }

            // We need to stop if it's a child navigation character
            if (c == '.' || c == '[') {
                break;
            }

            throw new IllegalArgumentException(
                    "Expected end-of-string or a navigation operator at index "
                    + i + ".");
        }

        return new Key(key.length() + characters + 1, key.toString());
    }

    /**
     * Parses a bracket-based child navigation path segment.
     * @param path the path.
     * @param offset the index in the path string.
     * @return the resulting Key
     */
    private static Key parseBracketKey(final String path, final int offset) {

        // Get the full name/value of the child key/index
        StringBuilder key = new StringBuilder();
        Character quote = null;
        int characters = 0;
        for (int i = offset + 1; i < path.length(); i++) {

            // Get the next character
            char c = path.charAt(i);

            // If we don't have a quote yet, this should be
            if (quote == null) {
                if (c == '\'' || c == '"') {
                    quote = c;
                    continue;
                } else {
                    throw new IllegalArgumentException(
                            "Keys must be quoted at index " + i);
                }
            }

            // Handle escape characters
            if (c == '\\') {
                Escape escape = parseEscape(path, i);
                i += escape.getCharacters();
                key.append(escape.getEscape());
                characters += escape.getCharacters();
                continue;
            }

            // This should be our quote character and right bracket
            if (c == '\'' || c == '"') {

                // This is the last quote, it must be followed by a right bracket
                if (i + 1 < path.length() && path.charAt(i + 1) == ']') {
                    return new Key(key.length() + characters + 4,
                                   key.toString());
                }

                throw new IllegalArgumentException(
                        "Unexpected end-of-string. Expected a right bracket.");
            }

            key.append(c);
        }
        
        throw new IllegalArgumentException(
                "Unexpected end-of-string. Expected `" + quote + "'.");
    }

    /**
     * Parses an index within brackets.
     * @param path the path string.
     * @param offset the offset
     * @return
     */
    private static Index parseIndex(final String path, final int offset) {

        // Build the index string
        StringBuilder index = new StringBuilder();
        for (int i = offset + 1; i < path.length(); i++) {
            
            // Get the next character
            char c = path.charAt(i);

            // If we've reached the closing bracket, we're done
            if (c == ']') {
                return new Index(index.length() + 2,
                        Integer.parseInt(index.toString()));
            }

            // Add the digit to the index string
            if (Character.isDigit(c)) {
                index.append(c);
                continue;
            }

            // All the characters must be digits
            throw new IllegalArgumentException(
                    "Keys must be quoted at index " + offset);
        }
        
        throw new IllegalArgumentException(
                "Unexpected end-of-string. Expected `]'.");
    }

    /**
     * Escapes a string so it's suitable for use as a bracketed key.
     * @param string the string to escape.
     * @return the escaped string.
     */
    public static String escape(String string) {
        return string.replaceAll("\\\\", "\\\\")
                     .replaceAll("\r", "\\r")
                     .replaceAll("\n", "\\n")
                     .replaceAll("\t", "\\t")
                     .replaceAll("\'", "\\'")
                     .replaceAll("\"", "\\\"");
    }

    /**
     * Parses a JsogPath string, and returns the resulting JsogPath object.
     * 
     * <p>
     * This is ALPHA quality code. The API is unlikely to change, and there are
     * test cases covering most of the "happy path" functionality, but corner
     * cases may cause unexpected behavior. Please submit feedback to
     * <a href="mailto:jeff@jeffrodriguez.com">Jeff Rodriguez</a>.
     * </p>
     * @param path a JsogPath expression.
     * @return a reusable JsogPath object.
     */
    public static JsogPath compile(String path) {
        return compile(path, 0);
    }

    /**
     * Parses a JsogPath string, and returns the resulting JsogPath object.
     * 
     * This method is used in recursion to child nodes.
     * @param path the JsogPath string.
     * @param offset the offset from which to parse.
     * @return a reusable JsogPath object.
     */
    private static JsogPath compile(String path, int offset) {

        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException(
                    "The path may not be null or empty.");
        }

        for (int pos = offset; pos < path.length(); pos++) {

            // Get the character
            char c = path.charAt(pos);

            // The very first character may be a $
            if (pos == 0) {
                if (c == '$') {

                    // If this is it, return a root-type JsogPath
                    if (path.length() == 1) {
                        return new JsogPath();
                    }

                    // There's more
                    continue;
                } else {
                    throw new IllegalArgumentException(
                            "Path expressions must begin with $.");
                }
            }

            // Determine the type of navigation
            if (c == '.') {

                // Dot navigation
                Key key = parseDotKey(path, pos);

                // Recurse, for the kids
                return new JsogPath(key.getKey(), compile(path, pos + key.getCharacters()));
            } else if (c == '[') {

                // Bracket navigation
                if (path.length() > pos + 1) {
                    c = path.charAt(pos + 1);
                } else {
                    throw new IllegalArgumentException(
                            "Unexpected end-of-string at index "
                            + offset + ".");
                }
                
                // This can either be an index or key
                if (Character.isDigit(c)) {

                    // Index navigation
                    Index index = parseIndex(path, pos);

                    // Recurse, for the kids
                    return new JsogPath(
                            index.getIndex(), compile(path, pos + index.getCharacters()));
                } else {

                    // Key navigation
                    Key key = parseBracketKey(path, pos);

                    // Recurse, for the kids
                    return new JsogPath(
                            key.getKey(), compile(path, pos + key.getCharacters()));
                }
            } else {
                throw new IllegalArgumentException(
                        "Unexpected character `" + c + "' at index " + pos);
            }
        }

        // We've reached the end of the string, we're done.
        return null;
    }

    /**
     * Evaluates a path expression.
     *
     * Note: This method always recompiles the path expression. If you will be
     * using the same path multiple times, consider using a compiled JsogPath.
     *
     * <p>
     * This is ALPHA quality code. The API is unlikely to change, and there are
     * test cases covering most of the "happy path" functionality, but corner
     * cases may cause unexpected behavior. Please submit feedback to
     * <a href="mailto:jeff@jeffrodriguez.com">Jeff Rodriguez</a>.
     * </p>
     * @param path the path expression to evaluate.
     * @param jsog the JSOG on which the path is to be evaluated.
     * @return the result of the evaluation.
     * @see JsogPath#compile(String)
     * @see #path(JsogPath)
     */
    public static JSOG evaluate(String path, JSOG jsog) {
        return compile(path).evaluate(jsog);
    }

    /**
     * This type of this JsogPath entry.
     */
    private final Type type;

    /**
     * If this is an object entry, the key of the object.
     */
    private final String key;

    /**
     * If this is an array entry, the index of the array.
     */
    private final int index;

    /**
     * Used for child paths recursion.
     */
    private final JsogPath child;

    /**
     * Constructs a root-type instance.
     */
    private JsogPath() {
        type = Type.root;
        key = null;
        index = 0;
        this.child = null;
    }

    /**
     * Constructs a new JsogPath object-type instance.
     * @param key the object key.
     * @param child the child JsogPath.
     */
    private JsogPath(String key, JsogPath child) {
        this.type = Type.object;
        this.index = 0;
        this.key = key;
        this.child = child;
    }

    /**
     * Constructs a new JsogPath array-type instance.
     * @param index the array index.
     * @param child the child JsogPath.
     */
    private JsogPath(int index, JsogPath child) {
        this.type = Type.array;
        this.key = null;
        this.index = index;
        this.child = child;
    }

    /**
     * Evaluates the path on a JSOG.
     * @param jsog the JSOG on which to evaluate the path.
     * @return the result of the evaluated path.
     */
    public JSOG evaluate(JSOG jsog) {
        JSOG result;
        if (type == Type.root) {
            return jsog;
        }
        
        if (type == Type.array) {
            result = jsog.get(index);
        } else {
            result = jsog.get(key);
        }

        // Recurse into the child if there is one
        if (child != null) {
            return child.evaluate(result);
        }

        // No child, just return the result
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("$");

        JsogPath jp = this;
        while (jp != null) {
            if (jp.type == Type.root) {
                return sb.toString();
            }
            
            if (jp.type == Type.array) {
                sb.append('[');
                sb.append(jp.index);
                sb.append(']');
            } else {
                sb.append("[\"");
                sb.append(escape(jp.key));
                sb.append("\"]");
            }
            
            // Iterate over the next child
            jp = jp.child;
        }

        return sb.toString();
    }
    
}

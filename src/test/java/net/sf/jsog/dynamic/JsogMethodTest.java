/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jsog.dynamic;

import java.lang.reflect.Method;
import java.net.URL;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.math.BigDecimal;
import net.sf.jsog.JSOG;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jrodriguez
 */
public class JsogMethodTest {
    
    public class Foo {

        // None
        public void noArgs() {};

        // String
        public String stringArg(@JsogPath("$.foo") String foo) {
            return foo;
        }

        public String requiredStringArg(
                @JsogPath(value="$.foo", required=true) String foo) {
            return foo;
        }

        // Boolean
        public Boolean booleanArg(@JsogPath("$.foo") Boolean foo) {
            return foo;
        }

        public Boolean requiredBooleanArg(
                @JsogPath(value="$.foo", required=true) Boolean foo) {
            return foo;
        }

        public boolean primitiveBooleanArg(@JsogPath("$.foo") boolean foo) {
            return foo;
        }

        // Byte
        public Byte byteArg(@JsogPath("$.foo") Byte foo) {
            return foo;
        }

        public Byte requiredByteArg(
                @JsogPath(value="$.foo", required=true) Byte foo) {
            return foo;
        }

        public byte primitiveByteArg(@JsogPath("$.foo") byte foo) {
            return foo;
        }

        // Short
        public Short shortArg(@JsogPath("$.foo") Short foo) {
            return foo;
        }

        public Short requiredShortArg(
                @JsogPath(value="$.foo", required=true) Short foo) {
            return foo;
        }

        public short primitiveShortArg(@JsogPath("$.foo") short foo) {
            return foo;
        }

        // Integer
        public Integer integerArg(@JsogPath("$.foo") Integer foo) {
            return foo;
        }

        public Integer requiredIntegerArg(
                @JsogPath(value="$.foo", required=true) Integer foo) {
            return foo;
        }

        public int primitiveIntegerArg(@JsogPath("$.foo") int foo) {
            return foo;
        }

        // Long
        public Long longArg(@JsogPath("$.foo") Long foo) {
            return foo;
        }

        public Long requiredLongArg(
                @JsogPath(value="$.foo", required=true) Long foo) {
            return foo;
        }

        public long primitiveLongArg(@JsogPath("$.foo") long foo) {
            return foo;
        }

        // Float
        public Float floatArg(@JsogPath("$.foo") Float foo) {
            return foo;
        }

        public Float requiredFloatArg(
                @JsogPath(value="$.foo", required=true) Float foo) {
            return foo;
        }

        public float primitiveFloatArg(@JsogPath("$.foo") float foo) {
            return foo;
        }

        // Double
        public Double doubleArg(@JsogPath("$.foo") Double foo) {
            return foo;
        }

        public Double requiredDoubleArg(
                @JsogPath(value="$.foo", required=true) Double foo) {
            return foo;
        }

        public double primitiveDoubleArg(@JsogPath("$.foo") double foo) {
            return foo;
        }

        // BigDecimal
        public BigDecimal bigDecimalArg(@JsogPath("$.foo") BigDecimal foo) {
            return foo;
        }

        public BigDecimal requiredBigDecimalArg(
                @JsogPath(value="$.foo", required=true) BigDecimal foo) {
            return foo;
        }

        // BigInteger
        public BigInteger bigIntegerArg(@JsogPath("$.foo") BigInteger foo) {
            return foo;
        }

        public BigInteger requiredBigIntegerArg(
                @JsogPath(value="$.foo", required=true) BigInteger foo) {
            return foo;
        }

        // JSOG
        public JSOG jsogArg(@JsogPath("$.foo") JSOG foo) {
            return foo;
        }

        public JSOG requiredJSOGArg(
                @JsogPath(value="$.foo", required=true) JSOG foo) {
            return foo;
        }

        // Other
        public String multipleAnnotations(@Bar @JsogPath("$.foo") String foo) {
            return foo;
        }

        public void multipleParameters(@JsogPath("$.foo") String foo, @JsogPath("$.bar") String bar) {
            assertEquals("foo", foo);
            assertEquals("bar", bar);
        }

        public void badParameterType(@JsogPath("$.foo") URL foo) { }

        public void notAnnotated(String foo) {}

        public void throwsException() throws Exception {
            throw new Exception("foo");
        }

        private void notPublic() {}
        
        public void rootArg(@JsogPath("$") String foo) {
            assertEquals("foo", foo);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Bar {

    }

    // Constructors
    @Test
    public void testConstructString() throws Exception {
        Foo foo = new Foo();
        JsogMethod<Void> instance = new JsogMethod<Void>(Foo.class, "noArgs");
        instance.invoke(foo, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructStringDoesNotExist() throws Exception {
        Foo foo = new Foo();
        JsogMethod<Void> instance = new JsogMethod<Void>(Foo.class, "doesNotExist");
        instance.invoke(foo, null);
    }

    @Test
    public void testConstructMethod() throws Exception {
        Foo foo = new Foo();
        JsogMethod<Void> instance = new JsogMethod<Void>(
                Foo.class.getDeclaredMethod("noArgs"));
        instance.invoke(foo, null);
    }

    // None
    @Test
    public void testInvokeNoArgs() throws Exception {
        Foo foo = new Foo();
        JsogMethod<Void> instance = new JsogMethod<Void>(Foo.class, "noArgs");
        instance.invoke(foo, null);
    }

    // String
    @Test
    public void testInvokeStringArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<String> instance = new JsogMethod<String>(Foo.class, "stringArg");
        String expected = "foo";

        // Execute
        String actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeStringArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<String> instance = new JsogMethod<String>(Foo.class, "stringArg");
        String expected = "foo";

        // Execute
        String actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredStringArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<String> instance = new JsogMethod<String>(Foo.class, "requiredStringArg");
        String expected = "foo";

        // Execute
        String actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredStringArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<String> instance = new JsogMethod<String>(Foo.class, "requiredStringArg");
        String expected = null;

        // Execute
        instance.invoke(foo, JSOG.object("foo", expected));
    }

    // Boolean
    @Test
    public void testInvokeBooleanArgTrue() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "booleanArg");
        Boolean expected = true;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeBooleanArgFalse() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "booleanArg");
        Boolean expected = false;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeBooleanArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "booleanArg");
        Boolean expected = null;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredBooleanArgTrue() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "requiredBooleanArg");
        Boolean expected = true;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredBooleanArgFalse() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "requiredBooleanArg");
        Boolean expected = false;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredBooleanArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "requiredBooleanArg");
        Boolean expected = null;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokePrimitiveBooleanArgTrue() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "primitiveBooleanArg");
        Boolean expected = true;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokePrimitiveBooleanArgFalse() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "primitiveBooleanArg");
        Boolean expected = false;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokePrimitiveBooleanArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Boolean> instance = new JsogMethod<Boolean>(Foo.class, "primitiveBooleanArg");
        Boolean expected = null;

        // Execute
        Boolean actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // Byte
    @Test
    public void testInvokeByteArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Byte> instance = new JsogMethod<Byte>(Foo.class, "byteArg");
        Byte expected = Byte.valueOf(Byte.MAX_VALUE);

        // Execute
        Byte actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeByteArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Byte> instance = new JsogMethod<Byte>(Foo.class, "byteArg");
        Byte expected = null;

        // Execute
        Byte actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredByteArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Byte> instance = new JsogMethod<Byte>(Foo.class, "requiredByteArg");
        Byte expected = Byte.valueOf(Byte.MAX_VALUE);

        // Execute
        Byte actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredByteArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Byte> instance = new JsogMethod<Byte>(Foo.class, "requiredByteArg");
        Byte expected = null;

        // Execute
        Byte actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokePrimitiveByteArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Byte> instance = new JsogMethod<Byte>(Foo.class, "primitiveByteArg");
        Byte expected = Byte.valueOf(Byte.MAX_VALUE);

        // Execute
        Byte actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokePrimitiveByteArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Byte> instance = new JsogMethod<Byte>(Foo.class, "primitiveByteArg");
        Byte expected = null;

        // Execute
        Byte actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // Short
    @Test
    public void testInvokeShortArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Short> instance = new JsogMethod<Short>(Foo.class, "shortArg");
        Short expected = Short.valueOf(Short.MAX_VALUE);

        // Execute
        Short actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeShortArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Short> instance = new JsogMethod<Short>(Foo.class, "shortArg");
        Short expected = null;

        // Execute
        Short actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredShortArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Short> instance = new JsogMethod<Short>(Foo.class, "requiredShortArg");
        Short expected = Short.valueOf(Short.MAX_VALUE);

        // Execute
        Short actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredShortArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Short> instance = new JsogMethod<Short>(Foo.class, "requiredShortArg");
        Short expected = null;

        // Execute
        Short actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokePrimitiveShortArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Short> instance = new JsogMethod<Short>(Foo.class, "primitiveShortArg");
        Short expected = Short.valueOf(Short.MAX_VALUE);

        // Execute
        Short actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokePrimitiveShortArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Short> instance = new JsogMethod<Short>(Foo.class, "primitiveShortArg");
        Short expected = null;

        // Execute
        Short actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // Integer
    @Test
    public void testInvokeIntegerArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Integer> instance = new JsogMethod<Integer>(Foo.class, "integerArg");
        Integer expected = Integer.valueOf(Integer.MAX_VALUE);

        // Execute
        Integer actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeIntegerArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Integer> instance = new JsogMethod<Integer>(Foo.class, "integerArg");
        Integer expected = null;

        // Execute
        Integer actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredIntegerArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Integer> instance = new JsogMethod<Integer>(Foo.class, "requiredIntegerArg");
        Integer expected = Integer.valueOf(Integer.MAX_VALUE);

        // Execute
        Integer actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredIntegerArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Integer> instance = new JsogMethod<Integer>(Foo.class, "requiredIntegerArg");
        Integer expected = null;

        // Execute
        Integer actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokePrimitiveIntegerArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Integer> instance = new JsogMethod<Integer>(Foo.class, "primitiveIntegerArg");
        Integer expected = Integer.valueOf(Integer.MAX_VALUE);

        // Execute
        Integer actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokePrimitiveIntegerArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Integer> instance = new JsogMethod<Integer>(Foo.class, "primitiveIntegerArg");
        Integer expected = null;

        // Execute
        Integer actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // Long
    @Test
    public void testInvokeLongArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Long> instance = new JsogMethod<Long>(Foo.class, "longArg");
        Long expected = Long.valueOf(Long.MAX_VALUE);

        // Execute
        Long actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeLongArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Long> instance = new JsogMethod<Long>(Foo.class, "longArg");
        Long expected = null;

        // Execute
        Long actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredLongArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Long> instance = new JsogMethod<Long>(Foo.class, "requiredLongArg");
        Long expected = Long.valueOf(Long.MAX_VALUE);

        // Execute
        Long actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredLongArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Long> instance = new JsogMethod<Long>(Foo.class, "requiredLongArg");
        Long expected = null;

        // Execute
        Long actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokePrimitiveLongArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Long> instance = new JsogMethod<Long>(Foo.class, "primitiveLongArg");
        Long expected = Long.valueOf(Long.MAX_VALUE);

        // Execute
        Long actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokePrimitiveLongArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Long> instance = new JsogMethod<Long>(Foo.class, "primitiveLongArg");
        Long expected = null;

        // Execute
        Long actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // Float
    @Test
    public void testInvokeFloatArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Float> instance = new JsogMethod<Float>(Foo.class, "floatArg");
        Float expected = Float.valueOf(Float.MAX_VALUE);

        // Execute
        Float actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeFloatArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Float> instance = new JsogMethod<Float>(Foo.class, "longArg");
        Float expected = null;

        // Execute
        Float actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredFloatArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Float> instance = new JsogMethod<Float>(Foo.class, "requiredFloatArg");
        Float expected = Float.valueOf(Float.MAX_VALUE);

        // Execute
        Float actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredFloatArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Float> instance = new JsogMethod<Float>(Foo.class, "requiredFloatArg");
        Float expected = null;

        // Execute
        Float actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokePrimitiveFloatArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Float> instance = new JsogMethod<Float>(Foo.class, "primitiveFloatArg");
        Float expected = Float.valueOf(Float.MAX_VALUE);

        // Execute
        Float actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokePrimitiveFloatArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Float> instance = new JsogMethod<Float>(Foo.class, "primitiveFloatArg");
        Float expected = null;

        // Execute
        Float actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // Double
    @Test
    public void testInvokeDoubleArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Double> instance = new JsogMethod<Double>(Foo.class, "doubleArg");
        Double expected = Double.valueOf(Double.MAX_VALUE);

        // Execute
        Double actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeDoubleArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Double> instance = new JsogMethod<Double>(Foo.class, "doubleArg");
        Double expected = null;

        // Execute
        Double actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredDoubleArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Double> instance = new JsogMethod<Double>(Foo.class, "requiredDoubleArg");
        Double expected = Double.valueOf(Double.MAX_VALUE);

        // Execute
        Double actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredDoubleArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Double> instance = new JsogMethod<Double>(Foo.class, "requiredDoubleArg");
        Double expected = null;

        // Execute
        Double actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokePrimitiveDoubleArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Double> instance = new JsogMethod<Double>(Foo.class, "primitiveDoubleArg");
        Double expected = Double.valueOf(Double.MAX_VALUE);

        // Execute
        Double actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokePrimitiveDoubleArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<Double> instance = new JsogMethod<Double>(Foo.class, "primitiveDoubleArg");
        Double expected = null;

        // Execute
        Double actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // BigDecimal
    @Test
    public void testInvokeBigDecimalArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<BigDecimal> instance = new JsogMethod<BigDecimal>(Foo.class, "bigDecimalArg");
        BigDecimal expected = BigDecimal.ONE;

        // Execute
        BigDecimal actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeBigDecimalArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<BigDecimal> instance = new JsogMethod<BigDecimal>(Foo.class, "longArg");
        BigDecimal expected = null;

        // Execute
        BigDecimal actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredBigDecimalArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<BigDecimal> instance = new JsogMethod<BigDecimal>(Foo.class, "requiredBigDecimalArg");
        BigDecimal expected = BigDecimal.ONE;

        // Execute
        BigDecimal actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredBigDecimalArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<BigDecimal> instance = new JsogMethod<BigDecimal>(Foo.class, "requiredBigDecimalArg");
        BigDecimal expected = null;

        // Execute
        BigDecimal actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // BigInteger
    @Test
    public void testInvokeBigIntegerArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<BigInteger> instance = new JsogMethod<BigInteger>(Foo.class, "bigIntegerArg");
        BigInteger expected = BigInteger.ONE;

        // Execute
        BigInteger actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeBigIntegerArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<BigInteger> instance = new JsogMethod<BigInteger>(Foo.class, "longArg");
        BigInteger expected = null;

        // Execute
        BigInteger actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeRequiredBigIntegerArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<BigInteger> instance = new JsogMethod<BigInteger>(Foo.class, "requiredBigIntegerArg");
        BigInteger expected = BigInteger.ONE;

        // Execute
        BigInteger actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredBigIntegerArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<BigInteger> instance = new JsogMethod<BigInteger>(Foo.class, "requiredBigIntegerArg");
        BigInteger expected = null;

        // Execute
        BigInteger actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    // JSOG
    @Test
    public void testInvokeJSOGArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(Foo.class, "jsogArg");
        JSOG expected = new JSOG("foo");

        // Execute
        JSOG actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testInvokeJSOGArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(Foo.class, "jsogArg");
        JSOG expected = null;

        // Execute
        JSOG actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertTrue(actual.isNull());
    }

    @Test
    public void testInvokeRequiredJSOGArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(Foo.class, "requiredJSOGArg");
        JSOG expected = new JSOG("foo");

        // Execute
        JSOG actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertEquals(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvokeRequiredJSOGArgNull() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(Foo.class, "requiredJSOGArg");
        JSOG expected = null;

        // Execute
        JSOG actual = instance.invoke(foo, JSOG.object("foo", expected));

        // Verify
        assertTrue(actual.isNull());
    }

    @Test
    public void testInvokeMultipleAnnotations() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(Foo.class, "multipleAnnotations");

        // Execute
        instance.invoke(foo, JSOG.object("foo", "foo")
                                 .put("bar", "bar"));
    }

    @Test(expected=IllegalStateException.class)
    public void testInvokeBadParameterType() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(Foo.class, "badParameterType");

        // Execute
        instance.invoke(foo, null);
    }

    @Test
    public void testInvokeNotAnnotated() throws Exception {

        // Setup
        Foo foo = new Foo();

        // Execute
        try {
            new JsogMethod<JSOG>(Foo.class, "notAnnotated");
            fail("Expected an exception");
        } catch (IllegalStateException e) {

            // Verify
            assertTrue(e.getMessage().contains("not annotated"));
        }
    }

    @Test
    public void testInvokeException() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(Foo.class, "throwsException");

        // Execute
        try {
            instance.invoke(foo, null);
            fail("Expected an exception.");
        } catch (InvocationTargetException e) {

            // Verify
            assertEquals("foo", e.getTargetException().getMessage());
        }
    }

    @Test
    public void testInvokePrivate() throws Exception {

        // Setup
        Foo foo = new Foo();
        Method method = Foo.class.getDeclaredMethod("notPublic");
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(method);

        // Execute
        try {
            instance.invoke(foo, null);
        } catch (IllegalStateException e) {
            assertTrue(e.getCause() instanceof IllegalAccessException);
        }
    }

    @Test
    public void testRootArg() throws Exception {

        // Setup
        Foo foo = new Foo();
        JsogMethod<JSOG> instance = new JsogMethod<JSOG>(Foo.class, "rootArg");

        // Execute
        instance.invoke(foo, new JSOG("foo"));
    }

}
package com.sketchproject.infogue.modules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Sketch Project Studio
 * Created by Angga on 02/05/2016 15.40.
 */
public class ValidatorTest {
    Validator validator;

    @Before
    public void setUp() throws Exception {
        validator = new Validator();
    }

    @After
    public void tearDown() throws Exception {
        validator = null;
    }

    @Test
    public void testIsEmpty() throws Exception {
        assertTrue(validator.isEmpty(""));
        assertTrue(validator.isEmpty(0));
        assertTrue(validator.isEmpty(null));
        assertTrue(validator.isEmpty(false));
    }

    @Test
    public void testIsEmptyIgnoreSpace() throws Exception {
        assertTrue(validator.isEmpty("  ", true));
        assertTrue(validator.isEmpty(0, true));
        assertTrue(validator.isEmpty(null, true));
        assertTrue(validator.isEmpty(false, true));
    }

    @Test
    public void testIsValidEmail() throws Exception {
        /*
        assertTrue(validator.isValidEmail("anggadarkprince@gmail.com"));
        assertTrue(validator.isValidEmail("anggadarkprince@gmail"));
        assertFalse(validator.isValidEmail("anggadarkprince"));
        assertFalse(validator.isValidEmail("@gmail@yahoo@rocket"));
        assertFalse(validator.isValidEmail("@gmail")); */
    }

    @Test
    public void testIsValidUrl() throws Exception {
        assertFalse(validator.isValidUrl("angga-ari.com"));
        assertTrue(validator.isValidUrl("http://angga-ari.com"));
        assertTrue(validator.isValidUrl("http://infogue.stage.angga-ari.com"));
        assertTrue(validator.isValidUrl("https://angga-ari.com/public/account"));
        assertTrue(validator.isValidUrl("ftp://angga-ari.com:8000"));
        assertTrue(validator.isValidUrl("file://angga-ari.com/images/image.jpg"));
    }

    @Test
    public void testIsAlphaDash() throws Exception {
        assertTrue(validator.isAlphaDash("AnggaDarkPrince"));
        assertTrue(validator.isAlphaDash("angga-ari2"));
        assertTrue(validator.isAlphaDash("angga_ari43"));
        assertFalse(validator.isAlphaDash("angga ari 43"));
        assertFalse(validator.isAlphaDash("angga$$"));
    }

    @Test
    public void testIsAlphaNumeric() throws Exception {
        assertTrue(validator.isAlphaNumeric("Angga17"));
        assertFalse(validator.isAlphaNumeric("Angga 17"));
        assertFalse(validator.isAlphaNumeric("Angga_17"));
        assertFalse(validator.isAlphaNumeric("Angga-17"));
        assertFalse(validator.isAlphaNumeric("Angga-+&^*17"));
    }

    @Test
    public void testIsPersonName() throws Exception {
        assertTrue(validator.isPersonName("Angga Ari Wijaya"));
        assertTrue(validator.isPersonName("O'brian"));
        assertTrue(validator.isPersonName("Mr. Echo,msi"));
        assertFalse(validator.isPersonName("angga v1.0"));
    }

    @Test
    public void testIsValidDate() throws Exception {
        assertTrue(validator.isValidDate("2015-08-12"));
        assertFalse(validator.isValidDate("18-12-2015"));
        assertFalse(validator.isValidDate("2015"));
        assertFalse(validator.isValidDate("Blue Crimson"));
        assertFalse(validator.isValidDate("Laa232"));
    }

    @Test
    public void testIsNumeric() throws Exception {
        assertTrue(validator.isNumeric(32));
        assertFalse(validator.isNumeric("tes"));
    }

    @Test
    public void testIsNumericSignedOnly() throws Exception {
        assertTrue(validator.isNumeric(32, true));
        assertFalse(validator.isNumeric(false));
        assertFalse(validator.isNumeric(-34, true));
        assertTrue(validator.isNumeric(0, true));
        assertTrue(validator.isNumeric(45, true));
    }

    @Test
    public void testMinLength() throws Exception {
        assertTrue(validator.minLength("Angga", 2));
        assertFalse(validator.minLength("Angga", 10));
    }

    @Test
    public void testMinLengthIgnoringSpace() throws Exception {
        assertTrue(validator.minLength("A n g g a A r i", 8, true));
        assertTrue(validator.minLength("Angga Ar                 i", 15, true));
        assertFalse(validator.minLength("Angga", 8, true));
    }

    @Test
    public void testMaxLength() throws Exception {
        assertTrue(validator.maxLength("Angga", 5));
        assertFalse(validator.maxLength("Angga Ari", 8));
    }

    @Test
    public void testMaxLengthIgnoringSpace() throws Exception {
        assertEquals(9, String.valueOf(" Angga Ari ").trim().length());
        assertTrue(validator.maxLength("Angga", 5, true));
        assertTrue(validator.maxLength("Angga Ari", 9, true));
    }

    @Test
    public void testRangeLength() throws Exception {
        assertTrue(validator.rangeLength("Angga", 2, 10));
        assertFalse(validator.rangeLength("Angga", 6, 10));
        assertFalse(validator.rangeLength("Angga Ari", 3, 5));
    }

    @Test
    public void testRangeLengthIgnoringSpace() throws Exception {
        assertTrue(validator.rangeLength("  Angga  ", 2, 6, true));
        assertFalse(validator.rangeLength("  Angga", 6, 10, true));
        assertFalse(validator.rangeLength("Angga Ari", 3, 5, true));
    }

    @Test
    public void testMinValue() throws Exception {
        assertTrue(validator.minValue(3, 2));
        assertTrue(validator.minValue(3f, 2f));
        assertTrue(validator.minValue(3d, 2d));
        assertFalse(validator.minValue(5, 10));
        assertFalse(validator.minValue(7f, 8f));
        assertFalse(validator.minValue(2d, 5d));
    }

    @Test
    public void testMaxValue() throws Exception {
        assertTrue(validator.maxValue(3, 5));
        assertTrue(validator.maxValue(3f, 5f));
        assertTrue(validator.maxValue(3d, 5d));
        assertFalse(validator.maxValue(15, 10));
        assertFalse(validator.maxValue(17f, 8f));
        assertFalse(validator.maxValue(12d, 5d));
    }

    @Test
    public void testRangeValue() throws Exception {
        assertTrue(validator.rangeValue(3, 1, 5));
        assertTrue(validator.rangeValue(3f, 1f, 5f));
        assertTrue(validator.rangeValue(3d, 1, 5d));
        assertFalse(validator.rangeValue(5, 6, 10));
        assertFalse(validator.rangeValue(8f, 3f, 7f));
        assertFalse(validator.rangeValue(2d, 5d, 8d));
    }

    @Test
    public void testIsUnique() throws Exception {
        int[] data = {32,34,23,54};
        assertTrue(validator.isUnique(2, data));
        assertFalse(validator.isUnique(23, data));
    }

    @Test
    public void testIsUniqueString() throws Exception {
        String[] data = {"angga","ari","wijaya"};
        assertTrue(validator.isUnique("hana", data));
        assertFalse(validator.isUnique("angga", data));
    }

    @Test
    public void testIsMemberOf() throws Exception {
        int[] data = {32,34,23,54};
        assertTrue(validator.isMemberOf(32, data));
        assertFalse(validator.isMemberOf(3, data));
    }

    @Test
    public void testIsMemberOfString() throws Exception {
        String[] data = {"angga","ari","wijaya"};
        assertTrue(validator.isMemberOf("angga", data));
        assertFalse(validator.isMemberOf("rio", data));
    }

    @Test
    public void testIsValid() throws Exception {
        assertTrue(validator.isValid("angga", "[a-zA-Z]*"));
        assertFalse(validator.isValid("angga17", "[a-zA-Z]*"));
    }
}
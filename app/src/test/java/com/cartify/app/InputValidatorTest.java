package com.cartify.app;

import com.cartify.app.utils.InputValidator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for InputValidator class
 * Demonstrates validation functionality and edge cases
 */
public class InputValidatorTest {

    @Test
    public void testEmailValidation() {
        // Valid emails
        assertTrue(InputValidator.validateEmail("user@example.com").isValid());
        assertTrue(InputValidator.validateEmail("test.email+tag@domain.co.uk").isValid());
        
        // Invalid emails
        assertFalse(InputValidator.validateEmail("").isValid());
        assertFalse(InputValidator.validateEmail("invalid-email").isValid());
        assertFalse(InputValidator.validateEmail("@domain.com").isValid());
        assertFalse(InputValidator.validateEmail("user@").isValid());
    }

    @Test
    public void testPasswordValidation() {
        // Valid passwords
        assertTrue(InputValidator.validatePassword("StrongPass123").isValid());
        assertTrue(InputValidator.validatePassword("MySecure1Pass").isValid());
        
        // Invalid passwords
        assertFalse(InputValidator.validatePassword("").isValid());
        assertFalse(InputValidator.validatePassword("weak").isValid());
        assertFalse(InputValidator.validatePassword("password123").isValid()); // Too common
        assertFalse(InputValidator.validatePassword("ALLUPPERCASE123").isValid()); // No lowercase
        assertFalse(InputValidator.validatePassword("alllowercase123").isValid()); // No uppercase
        assertFalse(InputValidator.validatePassword("NoNumbers").isValid()); // No numbers
    }

    @Test
    public void testNameValidation() {
        // Valid names
        assertTrue(InputValidator.validateName("John Doe").isValid());
        assertTrue(InputValidator.validateName("Mary Jane").isValid());
        
        // Invalid names
        assertFalse(InputValidator.validateName("").isValid());
        assertFalse(InputValidator.validateName("A").isValid()); // Too short
        assertFalse(InputValidator.validateName("John123").isValid()); // Contains numbers
        assertFalse(InputValidator.validateName("John@Doe").isValid()); // Contains special chars
    }

    @Test
    public void testPhoneValidation() {
        // Valid phone numbers
        assertTrue(InputValidator.validatePhone("1234567890").isValid());
        assertTrue(InputValidator.validatePhone("+1234567890123").isValid());
        assertTrue(InputValidator.validatePhone("12345678901").isValid());
        
        // Invalid phone numbers
        assertFalse(InputValidator.validatePhone("").isValid());
        assertFalse(InputValidator.validatePhone("123").isValid()); // Too short
        assertFalse(InputValidator.validatePhone("12345678901234567890").isValid()); // Too long
        assertFalse(InputValidator.validatePhone("123-456-7890").isValid()); // Contains dashes
    }

    @Test
    public void testQuantityValidation() {
        // Valid quantities
        assertTrue(InputValidator.validateQuantity("1").isValid());
        assertTrue(InputValidator.validateQuantity("50").isValid());
        assertTrue(InputValidator.validateQuantity("999").isValid());
        
        // Invalid quantities
        assertFalse(InputValidator.validateQuantity("").isValid());
        assertFalse(InputValidator.validateQuantity("0").isValid());
        assertFalse(InputValidator.validateQuantity("-1").isValid());
        assertFalse(InputValidator.validateQuantity("1000").isValid());
        assertFalse(InputValidator.validateQuantity("abc").isValid());
    }

    @Test
    public void testPriceValidation() {
        // Valid prices
        assertTrue(InputValidator.validatePrice("10.99").isValid());
        assertTrue(InputValidator.validatePrice("0.01").isValid());
        assertTrue(InputValidator.validatePrice("999999.99").isValid());
        
        // Invalid prices
        assertFalse(InputValidator.validatePrice("").isValid());
        assertFalse(InputValidator.validatePrice("-10.99").isValid());
        assertFalse(InputValidator.validatePrice("1000000.00").isValid());
        assertFalse(InputValidator.validatePrice("10.999").isValid()); // Too many decimals
        assertFalse(InputValidator.validatePrice("abc").isValid());
    }

    @Test
    public void testSearchQueryValidation() {
        // Valid search queries
        assertTrue(InputValidator.validateSearchQuery("laptop").isValid());
        assertTrue(InputValidator.validateSearchQuery("iPhone 12").isValid());
        assertTrue(InputValidator.validateSearchQuery("women's shoes").isValid());
        
        // Invalid search queries
        assertFalse(InputValidator.validateSearchQuery("").isValid());
        assertFalse(InputValidator.validateSearchQuery("a".repeat(101)).isValid()); // Too long
        assertFalse(InputValidator.validateSearchQuery("<script>").isValid()); // Contains HTML
    }

    @Test
    public void testInputSanitization() {
        // Test HTML entity encoding
        assertEquals("&lt;script&gt;", InputValidator.sanitizeInput("<script>"));
        assertEquals("&quot;test&quot;", InputValidator.sanitizeInput("\"test\""));
        assertEquals("&#x27;test&#x27;", InputValidator.sanitizeInput("'test'"));
        assertEquals("test&#x2F;path", InputValidator.sanitizeInput("test/path"));
        
        // Test whitespace trimming
        assertEquals("test", InputValidator.sanitizeInput("  test  "));
        assertEquals("", InputValidator.sanitizeInput("   "));
    }

    @Test
    public void testPasswordConfirmation() {
        // Matching passwords
        assertTrue(InputValidator.validatePasswordConfirmation("password123", "password123").isValid());
        
        // Non-matching passwords
        assertFalse(InputValidator.validatePasswordConfirmation("password123", "different123").isValid());
        assertFalse(InputValidator.validatePasswordConfirmation("password123", "").isValid());
    }

    @Test
    public void testAddressValidation() {
        // Valid addresses
        assertTrue(InputValidator.validateAddress("123 Main St, City, State").isValid());
        assertTrue(InputValidator.validateAddress("Apt 4B, 456 Oak Avenue").isValid());
        
        // Invalid addresses
        assertFalse(InputValidator.validateAddress("").isValid());
        assertFalse(InputValidator.validateAddress("123").isValid()); // Too short
        assertFalse(InputValidator.validateAddress("a".repeat(201)).isValid()); // Too long
    }

    @Test
    public void testRatingValidation() {
        // Valid ratings
        assertTrue(InputValidator.validateRating("4.5").isValid());
        assertTrue(InputValidator.validateRating("0.0").isValid());
        assertTrue(InputValidator.validateRating("5.0").isValid());
        
        // Invalid ratings
        assertFalse(InputValidator.validateRating("").isValid());
        assertFalse(InputValidator.validateRating("-1.0").isValid());
        assertFalse(InputValidator.validateRating("5.1").isValid());
        assertFalse(InputValidator.validateRating("abc").isValid());
    }
}
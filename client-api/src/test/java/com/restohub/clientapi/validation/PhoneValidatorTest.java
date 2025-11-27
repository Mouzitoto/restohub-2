package com.restohub.clientapi.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PhoneValidatorTest {
    
    private PhoneValidator validator;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @BeforeEach
    void setUp() {
        validator = new PhoneValidator();
        validator.initialize(null);
    }
    
    @Test
    void testValidPhoneWithPlus7() {
        assertTrue(validator.isValid("+79991234567", context));
    }
    
    @Test
    void testValidPhoneWith8() {
        assertTrue(validator.isValid("89991234567", context));
    }
    
    @Test
    void testInvalidPhoneTooShort() {
        assertFalse(validator.isValid("+7999123456", context));
    }
    
    @Test
    void testInvalidPhoneWrongFormat() {
        assertFalse(validator.isValid("79991234567", context));
    }
    
    @Test
    void testNullPhone() {
        assertFalse(validator.isValid(null, context));
    }
    
    @Test
    void testEmptyPhone() {
        assertFalse(validator.isValid("", context));
    }
    
    @Test
    void testNormalizePhoneFrom8() {
        String normalized = PhoneValidator.normalizePhone("89991234567");
        assertEquals("+79991234567", normalized);
    }
    
    @Test
    void testNormalizePhoneFromPlus7() {
        String normalized = PhoneValidator.normalizePhone("+79991234567");
        assertEquals("+79991234567", normalized);
    }
    
    @Test
    void testNormalizePhoneWithSpaces() {
        String normalized = PhoneValidator.normalizePhone("8 999 123 45 67");
        assertEquals("+79991234567", normalized);
    }
}


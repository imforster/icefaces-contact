package com.phonebook.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for phone number format validation.
 * Validates that a phone number follows acceptable international formats.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface PhoneNumber {

    /**
     * Default validation error message.
     */
    String message() default "Phone number must be a valid format (e.g., +1-234-567-8900, (555) 123-4567, 555.123.4567)";

    /**
     * Validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Payload for validation metadata.
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to allow international format with country codes.
     */
    boolean allowInternational() default true;

    /**
     * Whether to allow extensions (e.g., x1234, ext. 5678).
     */
    boolean allowExtensions() default false;

    /**
     * Minimum length for the phone number (digits only).
     */
    int minLength() default 7;

    /**
     * Maximum length for the phone number (digits only).
     */
    int maxLength() default 15;
}
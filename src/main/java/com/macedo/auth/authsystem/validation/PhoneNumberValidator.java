package com.macedo.auth.authsystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private boolean allowEmpty;

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return allowEmpty;
        }

        String digitsOnly = value.replaceAll("[^0-9+]", "");
        if (digitsOnly.length() < 10 || digitsOnly.length() > 15) {
            return false;
        }

        if (digitsOnly.startsWith("+") && digitsOnly.length() < 12) {
            return false;
        }

        return digitsOnly.matches("^[+]?[0-9]{10,14}$");
    }
}

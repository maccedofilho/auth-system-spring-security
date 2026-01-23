package com.macedo.auth.authsystem.exception;

public class AccountLockedException extends RuntimeException {

    private final long lockoutTimeRemainingMinutes;

    public AccountLockedException(String message, long lockoutTimeRemainingMinutes) {
        super(message);
        this.lockoutTimeRemainingMinutes = lockoutTimeRemainingMinutes;
    }

    public long getLockoutTimeRemainingMinutes() {
        return lockoutTimeRemainingMinutes;
    }
}

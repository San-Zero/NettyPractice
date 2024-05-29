package org.example.message;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    private final boolean isSuccess;
    private final String message;

    public LoginResponse(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public LoginResponse(boolean isSuccess) {
        this.isSuccess = isSuccess;
        this.message = "";
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }
}

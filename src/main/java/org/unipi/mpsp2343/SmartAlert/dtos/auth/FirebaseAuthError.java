package org.unipi.mpsp2343.SmartAlert.dtos.auth;

import java.util.List;

//Model for capturing errors sent by firebase during authentication.
public class FirebaseAuthError {
    private int code;
    private String message;
    private List<ErrorListItem> errors;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ErrorListItem> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorListItem> errors) {
        this.errors = errors;
    }

    public static class ErrorListItem {
        private String message;
        private String domain;
        private String reason;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}



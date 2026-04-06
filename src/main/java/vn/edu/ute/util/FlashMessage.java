package vn.edu.ute.util;

import java.io.Serializable;

public class FlashMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        SUCCESS, ERROR, INFO, WARNING
    }

    private String message;
    private Type type;

    public FlashMessage(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    public static FlashMessage success(String message) {
        return new FlashMessage(message, Type.SUCCESS);
    }

    public static FlashMessage error(String message) {
        return new FlashMessage(message, Type.ERROR);
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type.name().toLowerCase();
    }
}

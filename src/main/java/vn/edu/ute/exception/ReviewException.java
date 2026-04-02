package vn.edu.ute.exception;

/**
 * Custom exception dành riêng cho nghiệp vụ review.
 */
public class ReviewException extends RuntimeException {

    public ReviewException(String message) {
        super(message);
    }
}
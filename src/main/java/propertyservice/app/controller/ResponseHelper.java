package propertyservice.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Helper utility class to reduce repetitive ResponseEntity creation in controllers.
 * 
 * Beginner Tip: Static methods can be called without creating an instance of the class.
 * Example: ResponseHelper.ok(data) instead of new ResponseEntity<>(data, HttpStatus.OK)
 */
public class ResponseHelper {
    
    /**
     * Creates a ResponseEntity with HTTP 200 OK status
     * @param body The response body
     * @return ResponseEntity with OK status
     */
    public static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }
    
    /**
     * Creates a ResponseEntity with HTTP 201 CREATED status
     * @param body The response body
     * @return ResponseEntity with CREATED status
     */
    public static <T> ResponseEntity<T> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
    
    /**
     * Creates a ResponseEntity with HTTP 204 NO CONTENT status
     * @return ResponseEntity with NO_CONTENT status
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Creates a ResponseEntity with HTTP 200 OK status and no body (Void)
     * @return ResponseEntity with OK status and Void body
     */
    public static ResponseEntity<Void> ok() {
        return ResponseEntity.ok().build();
    }
}


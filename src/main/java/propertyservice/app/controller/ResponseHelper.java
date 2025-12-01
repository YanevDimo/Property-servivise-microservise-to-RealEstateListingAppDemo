package propertyservice.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class ResponseHelper {
    
    
    // * Creates a ResponseEntity with HTTP 200 OK status
    
    public static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }
    
    
     // Creates a ResponseEntity with HTTP 201 CREATED status
     
    public static <T> ResponseEntity<T> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
    
    
     // Creates a ResponseEntity with HTTP 204 NO CONTENT status
     
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
    
    
     // Creates a ResponseEntity with HTTP 200 OK status and no body (Void)
     
    public static ResponseEntity<Void> ok() {
        return ResponseEntity.ok().build();
    }
}


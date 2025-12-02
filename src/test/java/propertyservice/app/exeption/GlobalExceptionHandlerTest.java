package propertyservice.app.exeption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handlePropertyNotFoundException_ShouldReturn404() {
        PropertyNotFoundException exception = new PropertyNotFoundException("Property not found with id: 123");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handlePropertyNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Property not found with id: 123", response.getBody().get("message"));
        assertEquals("404", response.getBody().get("status"));
    }

    @Test
    void handleValidationExceptions_ShouldReturn400() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        List<FieldError> fieldErrors = new ArrayList<>();
        FieldError fieldError1 = new FieldError("property", "title", "Title is required");
        FieldError fieldError2 = new FieldError("property", "price", "Price must be greater than 0");
        fieldErrors.add(fieldError1);
        fieldErrors.add(fieldError2);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Title is required", response.getBody().get("title"));
        assertEquals("Price must be greater than 0", response.getBody().get("price"));
        assertEquals("400", response.getBody().get("status"));
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        Exception exception = new RuntimeException("Internal server error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().get("message"));
        assertEquals("500", response.getBody().get("status"));
    }

    @Test
    void handleGenericException_WithNullMessage_ShouldReturn500() {
        Exception exception = new RuntimeException();

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("500", response.getBody().get("status"));
    }
}



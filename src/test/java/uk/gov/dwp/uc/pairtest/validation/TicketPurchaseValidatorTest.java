package uk.gov.dwp.uc.pairtest.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketPurchaseValidatorTest {

    private TicketPurchaseValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TicketPurchaseValidator();
    }

    @Test
    @DisplayName("Should successfully validate correct inputs")
    void validateInputs_Success() {
        Long accountId = 123L;
        var request = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        
        assertDoesNotThrow(() -> validator.validateInputs(accountId, request));
    }

    @Test
    @DisplayName("Should throw exception for null accountId")
    void validateInputs_ThrowsForNullAccountId() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            validator.validateInputs(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))
        );
        assertEquals("Account ID must be greater than zero.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for zero or negative accountId")
    void validateInputs_ThrowsForInvalidAccountId() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            validator.validateInputs(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))
        );
        assertEquals("Account ID must be greater than zero.", exception.getMessage());
        
        exception = assertThrows(InvalidPurchaseException.class, () -> 
            validator.validateInputs(-1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1))
        );
        assertEquals("Account ID must be greater than zero.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for empty requests")
    void validateInputs_ThrowsForEmptyRequests() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            validator.validateInputs(1L)
        );
        assertEquals("Purchase request cannot be empty.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for null request in array")
    void validateInputs_ThrowsForNullRequestInArray() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            validator.validateInputs(1L, (TicketTypeRequest) null)
        );
        assertEquals("TicketTypeRequest cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative ticket count")
    void validateInputs_ThrowsForNegativeTicketCount() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            validator.validateInputs(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -5))
        );
        assertEquals("Number of tickets cannot be negative.", exception.getMessage());
    }
}

package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketPurchaseTest {

    @Test
    @DisplayName("Should correctly calculate total amount for mixed tickets")
    void totalAmount_CalculatesCorrectly() {
        var purchase = TicketPurchase.from(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2), // 2 * 25 = 50
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)  // 1 * 15 = 15
        );
        assertEquals(65, purchase.totalAmount());
    }

    @Test
    @DisplayName("Should correctly calculate total seats excluding infants")
    void totalSeats_ExcludesInfants() {
        var purchase = TicketPurchase.from(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );
        assertEquals(2, purchase.totalSeats());
    }

    @Test
    @DisplayName("Should successfully handle exactly 25 tickets (max boundary)")
    void validate_SuccessAtMaxBoundary() {
        assertDoesNotThrow(() -> TicketPurchase.from(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 15),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10)
        ));
    }

    @Test
    @DisplayName("Should throw exception if no adult is present")
    void validate_ThrowsIfNoAdult() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            TicketPurchase.from(new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1))
        );
        assertEquals("Adult ticket required for Child/Infant purchase.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if more infants than adults")
    void validate_ThrowsIfTooManyInfants() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            TicketPurchase.from(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
            )
        );
        assertEquals("Number of infants cannot exceed the number of adults.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception if total tickets exceeds 25")
    void validate_ThrowsIfMaxExceeded() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            TicketPurchase.from(new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26))
        );
        assertEquals("Max tickets exceeded. Maximum allowed is 25", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for integer overflow attempt in ticket count aggregation")
    void validate_ThrowsForIntegerOverflow() {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            TicketPurchase.from(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, Integer.MAX_VALUE),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)
            )
        );
        assertEquals("Max tickets exceeded. Maximum allowed is 25", exception.getMessage());
    }
}

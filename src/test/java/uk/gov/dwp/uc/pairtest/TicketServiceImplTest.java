package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.validation.TicketPurchaseValidator;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private TicketServiceImpl ticketService;

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService reservationService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(paymentService, reservationService, new TicketPurchaseValidator());
    }

    @Test
    @DisplayName("Should successfully purchase multiple tickets")
    void purchaseTickets_Success() {
        long accountId = 1L;
        var adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        var childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        var infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(accountId, adultRequest, childRequest, infantRequest);

        verify(paymentService).makePayment(accountId, 65);
        verify(reservationService).reserveSeat(accountId, 3);
    }

    @Test
    @DisplayName("Should handle multiple requests of the same ticket type correctly")
    void purchaseTickets_MergesSameTypes() {
        long accountId = 123L;
        var req1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        var req2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        
        ticketService.purchaseTickets(accountId, req1, req2);
        
        verify(paymentService).makePayment(accountId, 50);
        verify(reservationService).reserveSeat(accountId, 2);
    }

    @ParameterizedTest
    @MethodSource("invalidPurchaseProvider")
    @DisplayName("Should throw InvalidPurchaseException for invalid inputs")
    void purchaseTickets_ThrowsInvalidPurchaseException(Long accountId, String expectedMessage, TicketTypeRequest... requests) {
        var exception = assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(accountId, requests)
        );
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(paymentService, reservationService);
    }

    private static Stream<Arguments> invalidPurchaseProvider() {
        return Stream.of(
            Arguments.of(null, "Account ID must be greater than zero.", new TicketTypeRequest[]{}),
            Arguments.of(0L, "Account ID must be greater than zero.", new TicketTypeRequest[]{}),
            Arguments.of(1L, "Purchase request cannot be empty.", null),
            Arguments.of(1L, "TicketTypeRequest cannot be null.", new TicketTypeRequest[]{null}),
            Arguments.of(1L, "Number of tickets cannot be negative.", new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1)
            }),
            Arguments.of(1L, "Purchase must include at least one ticket.", new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0)
            }),
            Arguments.of(1L, "Max tickets exceeded. Maximum allowed is 25", new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)
            }),
            Arguments.of(1L, "Max tickets exceeded. Maximum allowed is 25", new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 13),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 13)
            }),
            Arguments.of(1L, "Adult ticket required for Child/Infant purchase.", new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)
            }),
            Arguments.of(1L, "Number of infants cannot exceed the number of adults.", new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
            })
        );
    }

    @Test
    @DisplayName("Should throw NPE if dependencies are null in constructor")
    void constructor_ThrowsNPE() {
        assertThrows(NullPointerException.class, () -> new TicketServiceImpl(null, reservationService, new TicketPurchaseValidator()));
        assertThrows(NullPointerException.class, () -> new TicketServiceImpl(paymentService, null, new TicketPurchaseValidator()));
        assertThrows(NullPointerException.class, () -> new TicketServiceImpl(paymentService, reservationService, null));
    }
}

package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchase;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.validation.TicketPurchaseValidator;

import java.util.Objects;

public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;
    private final TicketPurchaseValidator validator;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, 
                             SeatReservationService seatReservationService,
                             TicketPurchaseValidator validator) {
        this.ticketPaymentService = Objects.requireNonNull(ticketPaymentService, "TicketPaymentService is required");
        this.seatReservationService = Objects.requireNonNull(seatReservationService, "SeatReservationService is required");
        this.validator = Objects.requireNonNull(validator, "TicketPurchaseValidator is required");
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... requests) throws InvalidPurchaseException {
        validator.validateInputs(accountId, requests);

        var purchase = TicketPurchase.from(requests);

        ticketPaymentService.makePayment(accountId, purchase.totalAmount());
        seatReservationService.reserveSeat(accountId, purchase.totalSeats());
    }

}

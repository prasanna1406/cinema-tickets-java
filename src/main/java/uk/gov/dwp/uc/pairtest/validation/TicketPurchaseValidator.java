package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Objects;

public class TicketPurchaseValidator {

    public void validateInputs(Long accountId, TicketTypeRequest... requests) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Account ID must be greater than zero.");
        }
        if (requests == null || requests.length == 0) {
            throw new InvalidPurchaseException("Purchase request cannot be empty.");
        }
        
        for (var request : requests) {
            if (Objects.isNull(request)) {
                throw new InvalidPurchaseException("TicketTypeRequest cannot be null.");
            }
            if (request.noOfTickets() < 0) {
                throw new InvalidPurchaseException("Number of tickets cannot be negative.");
            }
        }
    }
}

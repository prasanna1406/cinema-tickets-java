package uk.gov.dwp.uc.pairtest.domain;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;


public record TicketPurchase(Map<TicketTypeRequest.Type, Long> ticketCounts) {

    private static final int MAX_TICKETS = 25;
    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;

    public TicketPurchase {
        validate(ticketCounts);
    }

    public static TicketPurchase from(TicketTypeRequest... requests) {
        Map<TicketTypeRequest.Type, Long> ticketCountByType = Arrays.stream(requests)
                .collect(Collectors.groupingBy(
                        TicketTypeRequest::getTicketType,
                        () -> new EnumMap<>(TicketTypeRequest.Type.class),
                        Collectors.summingLong(TicketTypeRequest::getNoOfTickets)
                ));

        return new TicketPurchase(ticketCountByType);
    }

    public int totalAmount() {
        return (getCount(TicketTypeRequest.Type.ADULT) * ADULT_TICKET_PRICE) +
               (getCount(TicketTypeRequest.Type.CHILD) * CHILD_TICKET_PRICE);
    }

    public int totalSeats() {
        return getCount(TicketTypeRequest.Type.ADULT) + getCount(TicketTypeRequest.Type.CHILD);
    }

    public int getCount(TicketTypeRequest.Type type) {
        return ticketCounts.getOrDefault(type, 0L).intValue();
    }

    private void validate(Map<TicketTypeRequest.Type, Long> counts) {
        for (long count : counts.values()) {
            if (count > Integer.MAX_VALUE) {
                throw new InvalidPurchaseException("Ticket count exceeds maximum allowed value.");
            }
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        if (total <= 0) {
            throw new InvalidPurchaseException("Purchase must include at least one ticket.");
        }
        if (total > MAX_TICKETS) {
            throw new InvalidPurchaseException("Max tickets exceeded. Maximum allowed is " + MAX_TICKETS);
        }

        int adults = getCount(TicketTypeRequest.Type.ADULT);
        int children = getCount(TicketTypeRequest.Type.CHILD);
        int infants = getCount(TicketTypeRequest.Type.INFANT);

        if ((children > 0 || infants > 0) && adults == 0) {
            throw new InvalidPurchaseException("Adult ticket required for Child/Infant purchase.");
        }
        if (infants > adults) {
            throw new InvalidPurchaseException("Number of infants cannot exceed the number of adults.");
        }
    }
}

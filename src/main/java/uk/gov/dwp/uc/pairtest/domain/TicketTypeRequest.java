package uk.gov.dwp.uc.pairtest.domain;

/**
 * Immutable Object
 */
public record TicketTypeRequest(Type type, int noOfTickets) {

    public enum Type {
        ADULT, CHILD, INFANT
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }
}

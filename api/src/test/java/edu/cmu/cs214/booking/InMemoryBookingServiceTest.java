package edu.cmu.cs214.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** The producer's own suite. It never touches the consumer module. */
class InMemoryBookingServiceTest {

    private final BookingApi api = new InMemoryBookingService();

    @Test
    void freeRoomGivesConfirmedBooking() {
        Booking booking = api.createBooking(BookingRequest.of("R1", 540, 600));

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(540, booking.getStartMinute());
    }

    @Test
    void conflictWithoutKeyReturnsNull() {
        api.createBooking(BookingRequest.of("R1", 540, 600));

        assertNull(api.createBooking(BookingRequest.of("R1", 570, 630)));
        assertEquals(1, api.listBookings("R1").size());
    }

    @Test
    void conflictWithKeyGoesOnWaitlist() {
        api.createBooking(BookingRequest.of("R1", 540, 600));

        Booking queued = api.createBooking(
                BookingRequest.of("R1", 570, 630).withWaitlistKey("party-of-four"));

        assertEquals(BookingStatus.WAITLISTED, queued.getStatus());
        assertEquals("party-of-four", queued.getWaitlistKey());
    }

    @Test
    void touchingRangesDoNotConflict() {
        api.createBooking(BookingRequest.of("R1", 540, 600));

        Booking next = api.createBooking(BookingRequest.of("R1", 600, 660));

        assertEquals(BookingStatus.CONFIRMED, next.getStatus());
    }

    @Test
    void cancelWithNotifyPromotesTheWaitlistedBooking() {
        Booking held = api.createBooking(BookingRequest.of("R1", 540, 600));
        Booking queued = api.createBooking(
                BookingRequest.of("R1", 570, 630).withWaitlistKey("party-of-four"));

        assertTrue(api.cancelBooking(held.getId(), true));

        assertEquals(BookingStatus.CONFIRMED, queued.getStatus());
        List<Booking> schedule = api.listBookings("R1");
        assertEquals(1, schedule.size());
        assertEquals(queued.getId(), schedule.get(0).getId());
    }
}

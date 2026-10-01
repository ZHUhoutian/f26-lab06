package edu.cmu.cs214.booking;

/**
 * Everything needed to create one booking, passed to
 * {@link BookingApi#createBooking(BookingRequest)}.
 *
 * <p>Start from {@link #of(String, long, long)} with the room and the half-open
 * range {@code [startMinute, endMinute)}, then add the optional parts with
 * {@link #withWaitlistKey(String)} and {@link #withNotes(String)}. Requests are
 * immutable: each {@code with} method returns a new request.
 *
 * <p>A request is plain data. It is validated when it is passed to
 * {@code createBooking}, not when it is built.
 */
public final class BookingRequest {

    private final String roomId;
    private final long startMinute;
    private final long endMinute;
    private final String waitlistKey;
    private final String notes;

    private BookingRequest(String roomId, long startMinute, long endMinute,
                           String waitlistKey, String notes) {
        this.roomId = roomId;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.waitlistKey = waitlistKey;
        this.notes = notes;
    }

    /**
     * A request for {@code roomId} over {@code [startMinute, endMinute)}, with
     * no waitlist key (do not waitlist on conflict) and no notes.
     */
    public static BookingRequest of(String roomId, long startMinute, long endMinute) {
        return new BookingRequest(roomId, startMinute, endMinute, null, null);
    }

    /** This request with the given waitlist key; null declines waitlisting. */
    public BookingRequest withWaitlistKey(String waitlistKey) {
        return new BookingRequest(roomId, startMinute, endMinute, waitlistKey, notes);
    }

    /** This request with the given notes; null means none. */
    public BookingRequest withNotes(String notes) {
        return new BookingRequest(roomId, startMinute, endMinute, waitlistKey, notes);
    }

    public String getRoomId() {
        return roomId;
    }

    public long getStartMinute() {
        return startMinute;
    }

    public long getEndMinute() {
        return endMinute;
    }

    /** The waitlist key, or null to decline waitlisting. */
    public String getWaitlistKey() {
        return waitlistKey;
    }

    /** The notes, or null if none. */
    public String getNotes() {
        return notes;
    }
}

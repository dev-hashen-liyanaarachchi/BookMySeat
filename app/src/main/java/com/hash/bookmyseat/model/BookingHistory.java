package com.hash.bookmyseat.model;

public class BookingHistory {
    private String bookingId;
    private String movieTitle;
    private String seats;
    private double totalAmount;
    private String bookingDate;
    private boolean ticketIssued;
    private boolean attended;

    public BookingHistory(String bookingId, String movieTitle, String seats,
                          double totalAmount, String bookingDate,
                          boolean ticketIssued, boolean attended) {
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.seats = seats;
        this.totalAmount = totalAmount;
        this.bookingDate = bookingDate;
        this.ticketIssued = ticketIssued;
        this.attended = attended;
    }


    public String getBookingId() { return bookingId; }
    public String getMovieTitle() { return movieTitle; }
    public String getSeats() { return seats; }
    public double getTotalAmount() { return totalAmount; }
    public String getBookingDate() { return bookingDate; }
    public boolean isTicketIssued() { return ticketIssued; }
    public boolean isAttended() { return attended; }
}
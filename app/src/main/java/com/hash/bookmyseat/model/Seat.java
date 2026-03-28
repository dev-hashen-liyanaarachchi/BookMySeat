package com.hash.bookmyseat.model;

public class Seat {
    private String seatNumber;
    private int status; // 0=Available, 1=Selected, 2=Booked

    public Seat(String seatNumber, int status) {
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
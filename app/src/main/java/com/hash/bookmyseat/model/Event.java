package com.hash.bookmyseat.model;

import java.util.Date;

public class Event {
    private String eventId;
    private String title;
    private String description;
    private String movieTitle;
    private String posterBase64;
    private String date;
    private String time;
    private String venue;
    private String location;          // For Google Maps
    private String contactNumber;     // Cinema contact number
    private double pricePerSeat;
    private int totalSeats;
    private String createdBy;
    private Date createdAt;
    private String status; // upcoming, ongoing, completed

    public Event() {}

    // Constructor with all fields
    public Event(String eventId, String title, String description, String movieTitle,
                 String posterBase64, String date, String time, String venue,
                 String location, String contactNumber, double pricePerSeat, int totalSeats,
                 String createdBy, Date createdAt, String status) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.movieTitle = movieTitle;
        this.posterBase64 = posterBase64;
        this.date = date;
        this.time = time;
        this.venue = venue;
        this.location = location;
        this.contactNumber = contactNumber;
        this.pricePerSeat = pricePerSeat;
        this.totalSeats = totalSeats;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getPosterBase64() { return posterBase64; }
    public void setPosterBase64(String posterBase64) { this.posterBase64 = posterBase64; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public double getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(double pricePerSeat) { this.pricePerSeat = pricePerSeat; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
package com.example.kitchen.entity;

public enum TicketStatus {
    PENDING,        // just received from monolith
    ACCEPTED,       // restaurant approved
    PREPARING,      // restaurant cooking
    READY,          // food ready for pickup
    CANCELLED
}

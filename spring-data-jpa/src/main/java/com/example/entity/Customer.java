package com.example.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import lombok.Data;

@Data
@Entity
@Table(name = "customers", schema = "public")
public class Customer {

    @Id
    @Column(name = "customer_id")
    private Long id;
    @Column(name = "customer_name", unique = true, nullable = false)
    private String name;
    @Column(name = "customer_gender")
    @Enumerated(EnumType.ORDINAL)
    private CustomerGender gender;
    @Column(name = "customer_joined_date")
    @Temporal(jakarta.persistence.TemporalType.DATE)
    private Date joinedDate;
    @Column(name = "customer_profile")
    // @Lob
    // private String profile;
    // @Column(name = "customer_photo")
    // @Lob
    // private byte[] photo;
    // @Embedded
    // private Address address;
    @ElementCollection
    @CollectionTable(name = "customer_addresses", schema = "public", joinColumns = @jakarta.persistence.JoinColumn(name = "customer_id"))
    private List<Address> addresses;

}

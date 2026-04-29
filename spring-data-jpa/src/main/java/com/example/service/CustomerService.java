package com.example.service;

import java.beans.Customizer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Customer;
import com.example.entity.CustomerGender;
import com.example.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void doSomething() {
        // Business logic using customerRepository

        // Customer customer = new Customer(); // Transient state
        // customer.setId(1L);
        // customer.setGender(CustomerGender.FEMALE);
        // customer.setName("foo");
        // customerRepository.save(customer); // Persistent/Managed state
        // customer.setGender(CustomerGender.MALE);

        Customer customer = customerRepository.findById(1L); // Persistent/Managed state
        customer.setName("baz");

    }

}

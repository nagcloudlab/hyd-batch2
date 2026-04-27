package com.example.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Customer;

import jakarta.persistence.EntityManager;

@Component
@Transactional
public class CustomerRepository /* extends JpaRepository<Customer, Long> */ {

    @Autowired
    private EntityManager entityManager;

    public void save(Customer customer) {
        entityManager.persist(customer); // ORM lib ( hibernate ) generate INSERT INTO ...
    }

    public Customer findById(Long id) {
        return entityManager.find(Customer.class, id); // ORM lib ( hibernate ) generate SELECT ... FROM ...
    }

}

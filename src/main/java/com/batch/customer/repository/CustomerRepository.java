package com.batch.customer.repository;

import com.batch.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
//if our object is to leave the domain of the JVM, it’ll require serialization.
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}

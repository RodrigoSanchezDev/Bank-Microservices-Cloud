package com.duoc.bank.transaction.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento recibido cuando se crea un nuevo cliente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent {
    
    private Long customerId;
    private String rut;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createdAt;
    
    @Override
    public String toString() {
        return "CustomerCreatedEvent{" +
                "customerId=" + customerId +
                ", rut='" + rut + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

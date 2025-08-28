package org.carecode.docpay.model;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Doctor doctor;
    @ManyToOne(optional = false)
    private Patient patient;
    @Column(nullable = false)
    private Instant appointmentDateTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SCHEDULED;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @ManyToOne
    private User createdBy;

    public enum Status { SCHEDULED, COMPLETED, CANCELLED }

    public Long getId() { return id; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Instant getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(Instant appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}


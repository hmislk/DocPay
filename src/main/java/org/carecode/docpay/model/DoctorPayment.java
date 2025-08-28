package org.carecode.docpay.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class DoctorPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String receiptId;
    @ManyToOne(optional = false)
    private Doctor doctor;
    private String description;
    @Column(nullable = false)
    private BigDecimal value;
    @Column(nullable = false)
    private Instant paidAt;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @ManyToOne
    private User createdBy;
    @Column(nullable = false)
    private boolean cancelled = false;
    @ManyToOne
    private User cancelledBy;
    private Instant cancelledAt;
    private String cancelReason;
    @Column(nullable = false)
    private int printCount = 0;
    private Instant lastPrintedAt;

    public Long getId() { return id; }
    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public User getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(User cancelledBy) { this.cancelledBy = cancelledBy; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public int getPrintCount() { return printCount; }
    public void setPrintCount(int printCount) { this.printCount = printCount; }
    public Instant getLastPrintedAt() { return lastPrintedAt; }
    public void setLastPrintedAt(Instant lastPrintedAt) { this.lastPrintedAt = lastPrintedAt; }
}


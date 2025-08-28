package org.carecode.docpay.model;

import javax.persistence.*;

@Entity
public class ReceiptSequence {
    @Id
    private int year;
    @Column(nullable = false)
    private int nextNumber = 1;

    public ReceiptSequence() {}
    public ReceiptSequence(int year) { this.year = year; this.nextNumber = 1; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getNextNumber() { return nextNumber; }
    public void setNextNumber(int nextNumber) { this.nextNumber = nextNumber; }
}


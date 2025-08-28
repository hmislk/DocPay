package org.carecode.docpay.service;

import org.carecode.docpay.model.Doctor;
import org.carecode.docpay.model.DoctorPayment;
import org.carecode.docpay.model.ReceiptSequence;
import org.carecode.docpay.model.User;
import org.carecode.docpay.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class PaymentService {
    private PaymentService() {}

    public static DoctorPayment createPayment(Long doctorId, String description, BigDecimal amount, LocalDate date, User createdBy) {
        if (doctorId == null) throw new IllegalArgumentException("doctorId required");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be > 0");
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();

            Doctor doctor = em.find(Doctor.class, doctorId);
            if (doctor == null) throw new IllegalArgumentException("Doctor not found");

            int year = LocalDate.now().getYear();
            ReceiptSequence seq;
            try {
                seq = em.createQuery("select s from ReceiptSequence s where s.year = :y", ReceiptSequence.class)
                        .setParameter("y", year)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .getSingleResult();
            } catch (NoResultException e) {
                seq = new ReceiptSequence(year);
                em.persist(seq);
                em.flush();
                em.refresh(seq, LockModeType.PESSIMISTIC_WRITE);
            }

            int next = seq.getNextNumber();
            String receiptId = String.format("%d-%06d", year, next);
            seq.setNextNumber(next + 1);

            DoctorPayment p = new DoctorPayment();
            p.setReceiptId(receiptId);
            p.setDoctor(doctor);
            p.setDescription(description);
            p.setValue(amount);
            Instant paidAt = (date == null ? Instant.now() : date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            p.setPaidAt(paidAt);
            p.setCreatedBy(createdBy);
            p.setPrintCount(0);

            em.persist(p);
            em.merge(seq);
            em.getTransaction().commit();
            return p;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public static void incrementPrintCount(Long paymentId) {
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            DoctorPayment p = em.find(DoctorPayment.class, paymentId, LockModeType.PESSIMISTIC_WRITE);
            if (p != null) {
                p.setPrintCount(p.getPrintCount() + 1);
                p.setLastPrintedAt(Instant.now());
                em.merge(p);
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally { em.close(); }
    }
}


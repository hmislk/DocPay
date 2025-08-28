package org.carecode.docpay.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class JPAUtil {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("docpayPU");
    private JPAUtil() {}
    public static EntityManager em() { return emf.createEntityManager(); }
    public static void close() {
        try { emf.close(); } catch (Exception ignored) {}
    }
}

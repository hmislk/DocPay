package org.carecode.docpay.service;

import org.carecode.docpay.model.AppSettings;
import org.carecode.docpay.util.JPAUtil;

import javax.persistence.EntityManager;

public final class SettingsService {
    private SettingsService() {}

    public static AppSettings get() {
        EntityManager em = JPAUtil.em();
        try {
            AppSettings s = em.find(AppSettings.class, 1L);
            if (s == null) {
                s = new AppSettings();
                em.getTransaction().begin();
                em.persist(s);
                em.getTransaction().commit();
            }
            return s;
        } finally { em.close(); }
    }

    public static void save(AppSettings s) {
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            if (s.getId() == null) s.setId(1L);
            em.merge(s);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }
}


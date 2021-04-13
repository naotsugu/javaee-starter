package com.mammb.javaee8.starter.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Startup
@javax.ejb.Singleton
public class StartupService {

    private static final Logger log = LoggerFactory.getLogger(StartupService.class);

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void init() {
        log.error("## Initialize");
        TypedQuery<User> query = em.createQuery(
                String.format("SELECT e FROM %s e", User.NAME), User.class);
        if (query.getResultList().size() == 0) {
            em.persist(User.of("Mark", "mark@example.com"));
            em.persist(User.of("Don", "don@example.com"));
        }
    }
}

package com.mammb.javaee8.starter.app;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

@Transactional
@RequestScoped
public class UserService {

    @PersistenceContext
    private EntityManager em;

    public User getUser(Long id) {
        return em.find(User.class, id);
    }

    public User registerUser(String name, String email) {
        var user = User.of(name, email);
        em.persist(user);
        return user;
    }

    public void deleteUser(Long id) {
        em.remove(em.find(User.class, id));
    }

    public void update(User user) {
        em.merge(user);
    }

    public List<User> findUsers() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);
        query.select(user).orderBy(cb.asc(user.get(User_.name)));
        return em.createQuery(query).getResultList();
    }

}

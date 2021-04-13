package com.mammb.javaee8.starter.web;

import com.mammb.javaee8.starter.app.User;
import com.mammb.javaee8.starter.app.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class UserBacking implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(UserBacking.class);

    @Inject
    private UserService userService;

    private User selected;

    @PostConstruct
    public void init() {
    }

    public void select(User user) {
        selected = user;
    }

    public void create() {
        selected = User.of("", "");
    }

    public List<User> list() {
        return userService.findUsers();
    }

    public void register() {
        userService.registerUser(selected.getName(), selected.getEmail());
        init();
    }

    public void update() {
        userService.update(selected);
        init();
    }

    public void delete() {
        userService.deleteUser(selected.getId());
        init();
    }

    public User getSelected() {
        return selected;
    }
}

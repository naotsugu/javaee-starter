package com.mammb.javaee8.starter.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
public class HelloBacking implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(HelloBacking.class);

    private String name;

    @PostConstruct
    public void construct() {
        name = "Thom";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}


package com.mammb.javaee8.starter.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("helloResource")
public class HelloResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Greeting get() {
        return new Greeting("Hello", "Thom");
    }

    public static class Greeting {
        private String message;
        private String name;
        public Greeting(String message, String name) {
            this.message = message;
            this.name = name;
        }
        public String getMessage() {
            return message;
        }
        public String getName() {
            return name;
        }
    }
}

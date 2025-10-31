package edu.escuelaing.distributedpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class Application {

    private final SimpleChat simpleChat;

    public Application(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() throws Exception {
        simpleChat.initChannel(); // inicializa JGroups después de arrancar Spring
    }
}

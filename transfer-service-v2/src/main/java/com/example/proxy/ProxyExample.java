package com.example.proxy;

// This example demonstrates the Proxy Pattern — the concept behind Spring AOP
// Problem: cross-cutting concerns (auth, logging) get mixed into business logic
// Solution: wrap the target object with a proxy that adds behavior before/after

// Cross-cutting concern: Authentication
class Authentication {
    public void auth() {
        System.out.println("Auth..");
    }
}

// Cross-cutting concern: Logging
class Logger {
    public void doLog() {
        System.out.println("Log...");
    }
}

// Target component — contains only business logic (SRP)
class Trainer {
    public void getSpringBootTraining() {
        System.out.println("Spring Boot Training");
    }

    public void getSqlTraining() {
        System.out.println("SQL Training");
    }
}

// Manual proxy — adds auth and logging around target methods
// In Spring AOP, this proxy is created automatically by the framework
class TrainerProxy {
    private final Authentication authentication = new Authentication();
    private final Logger logger = new Logger();
    private final Trainer trainer = new Trainer();

    public void getSpringBootTraining() {
        authentication.auth();
        logger.doLog();
        trainer.getSpringBootTraining();
        logger.doLog();
    }

    public void getSqlTraining() {
        authentication.auth();
        logger.doLog();
        trainer.getSqlTraining();
        logger.doLog();
    }
}

// Run this to see the proxy pattern in action
// Then compare with AuthAspect + TransactionAspect — Spring does this
// automatically
public class ProxyExample {

    public static void main(String[] args) {
        TrainerProxy trainer = new TrainerProxy();
        trainer.getSpringBootTraining();
        trainer.getSqlTraining();
    }

}

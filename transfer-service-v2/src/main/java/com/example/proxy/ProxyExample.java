package com.example.proxy;

// AuthnticationAspect
class Authentication {
    public void auth() {
        System.out.println("Auth..");
    }
}

// LoggerAspect
class Logger {
    public void doLog() {
        System.out.println("Log...");
    }

}

// Component
class Trainer {
    public void getSpringBootTraining() {
        System.out.println("Spring Boot Training");
    }

    public void getSqlTraining() {
        System.out.println("SQL Training");
    }
}

// Proxy
class TrainerProxy {
    private Authentication authentication = new Authentication();
    private Logger logger = new Logger();
    private Trainer trainer = new Trainer();

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

public class ProxyExample {

    public static void main(String[] args) {

        TrainerProxy trainer = new TrainerProxy();
        trainer.getSpringBootTraining();
        trainer.getSqlTraining();

    }

}

// AOP

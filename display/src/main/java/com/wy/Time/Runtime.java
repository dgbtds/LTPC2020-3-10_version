package com.wy.Time;

import java.time.Duration;
import java.time.Instant;

public interface Runtime {
    default void RunT() throws Exception {
        Instant start = Instant.now();
        this.run();
        Instant end = Instant.now();
        System.out.println("\n"+"**** process useTime:" + Duration.between(start, end).toMillis() + "ms");
    };
    void run() throws Exception;

}
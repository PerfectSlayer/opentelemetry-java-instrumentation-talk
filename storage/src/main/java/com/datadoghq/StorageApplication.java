package com.datadoghq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@SpringBootApplication
public class StorageApplication {
    private static final Logger LOGGER = Logger.getLogger(StorageApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(StorageApplication.class, args);
        LOGGER.info("Server starts");
    }

    @RestController
    @RequestMapping("/storage")
    public class StorageController {
        @GetMapping(value = "/{item}", produces = MediaType.APPLICATION_JSON_VALUE)
        public Storage getCount(@PathVariable(value = "item") String item) {
            LOGGER.info("Handling request");
            return new Storage(item, getCount());
        }

        private int getCount() {
            // Simulate computation
            try {
                Thread.sleep((long) (Math.random() * 100 + 150));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return (int) (Math.random() * 150);
        }

    }

    public record Storage(String name, int count) {}
}

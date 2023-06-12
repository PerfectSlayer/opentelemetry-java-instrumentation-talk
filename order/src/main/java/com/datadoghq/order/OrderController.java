package com.datadoghq.order;

import com.datadoghq.order.StorageFeign.Storage;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Stream;

@RestController
@RequestMapping("/order")
public class OrderController {
    final StorageFeign storageFeign;

    public OrderController(StorageFeign storageFeign) {
        this.storageFeign = storageFeign;
    }

    @GetMapping(value = "/buy", produces = MediaType.APPLICATION_JSON_VALUE)
    public Order buy() {
        int total = Stream.of("hat", "bag", "sock")
                .map(this.storageFeign::getStock)
                .mapToInt(Storage::count)
                .sum();
        return new Order(total);
    }

    record Order(int count) {
    }
}

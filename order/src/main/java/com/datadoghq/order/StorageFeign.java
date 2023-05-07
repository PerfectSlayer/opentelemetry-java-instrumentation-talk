package com.datadoghq.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "storage", url = "http://storage-otel:8080")
public interface StorageFeign {
    @RequestMapping(method = RequestMethod.GET, value = "/storage/{item}", produces = "application/json")
    Storage getStock(@PathVariable("item") String item);

    record Storage(String name, int count) {
    }
}

package com.datadoghq.order;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    private static final TextMapSetter<RequestTemplate> setter = (template, key, value) -> template.header(key, value);
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> W3CTraceContextPropagator.getInstance().inject(
                Context.current(), requestTemplate, setter
        );
    }
}

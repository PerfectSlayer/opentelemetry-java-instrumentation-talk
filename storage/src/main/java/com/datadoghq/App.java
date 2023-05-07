package com.datadoghq;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws IOException {
//        initOpenTelemetry();
        autoconfigureOpenTelemetry();
        // Create simple webserver
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/storage", new StorageHandler());
        server.start();
        LOGGER.info("Server starts");
    }

    static void initOpenTelemetryFull() {
        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "storage"))
        );

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(
                        BatchSpanProcessor.builder(
//                                OtlpHttpSpanExporter.builder().build()
                                OtlpGrpcSpanExporter.builder()
                                        .setEndpoint("http://datadog-agent-otel:4317")
                                        .build()
                        ).build())
                .setResource(resource)
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
//                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();
    }

    static void initOpenTelemetry() {
        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "storage"))
        );

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(
                        BatchSpanProcessor.builder(
                                OtlpGrpcSpanExporter.builder()
                                        .setEndpoint("http://datadog-agent-otel:4317")
                                        .build()
//                                OtlpHttpSpanExporter.builder()
//                                        .setEndpoint("http://datadog-agent-otel:4318")
//                                .build()
                        ).build())
                .setResource(resource)
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .buildAndRegisterGlobal();
    }

    static void autoconfigureOpenTelemetry() {
        AutoConfiguredOpenTelemetrySdk.initialize();
//        OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize()
//                .getOpenTelemetrySdk();
    }

    static class StorageHandler implements HttpHandler {

        private final Tracer tracer = GlobalOpenTelemetry.getTracer("http-handler");
        private final TextMapGetter<Headers> getter = new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(Headers headers) {
                return headers.keySet();
            }

            @Override
            public String get(Headers headers, String key) {
                return headers.getFirst(key);
            }
        };

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Context context = extractContext(exchange);
            Span span = tracer.spanBuilder("handleRequest")
                    .setParent(context)
                    .setSpanKind(SpanKind.SERVER)
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                LOGGER.info("Handling request");
                // Compute storage answer
                String item = extractItem(exchange);
                int count = getCount(item);
                String response = "{\"name\": \""+item+"\", \"count\": " + count + "}";
                // Answer to client
                var headers = exchange.getResponseHeaders();
                headers.add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(response.getBytes(UTF_8));
                    outputStream.flush();
                }
            } finally {
                span.end();
            }
        }

        private Context extractContext(HttpExchange exchange) {
            return W3CTraceContextPropagator.getInstance().extract(
                    Context.root(),
                    exchange.getRequestHeaders(),
                    getter
            );
        }

        private String extractItem(HttpExchange exchange) {
            String path = exchange.getRequestURI().getPath();
            int index = path.lastIndexOf('/');
            if (index == -1) {
                return path;
            } else {
                return path.substring(index+1);
            }
        }

        private int getCount(String item) {
            Span span = tracer.spanBuilder("computeCount")
                    .setAttribute("item", item)
                    .startSpan();
            // Simulate computation
            try {
                Thread.sleep((long) (Math.random() * 100 + 150));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            span.end();
            return (int) (Math.random() * 150);
        }
    }
}

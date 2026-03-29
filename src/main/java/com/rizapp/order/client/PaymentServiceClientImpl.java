package com.rizapp.order.client;

import com.rizapp.order.dto.request.PaymentRequest;
import com.rizapp.order.dto.response.ApiResponse;
import com.rizapp.order.dto.response.PaymentResponse;
import com.rizapp.order.exception.PaymentServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class PaymentServiceClientImpl implements PaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceClientImpl.class);

    private final WebClient webClient;

    public PaymentServiceClientImpl(WebClient paymentServiceWebClient) {
        this.webClient = paymentServiceWebClient;
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "createPaymentFallback")
    @Retry(name = "paymentService")
    public PaymentResponse createPayment(String orderId, BigDecimal amount, String currency, String description) {
        log.info("Creating payment for order: {}", orderId);

        PaymentRequest request = new PaymentRequest(orderId, amount, currency, description);

        ApiResponse<PaymentResponse> response = webClient.post()
                .uri("/payments")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(body -> new PaymentServiceException("Payment Service client error: " + body)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(body -> new PaymentServiceException("Payment Service server error: " + body)))
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PaymentResponse>>() {})
                .block();

        if (response != null && response.getData() != null) {
            log.info("Payment created successfully for order: {}, paymentId: {}", orderId, response.getData().getPaymentId());
            return response.getData();
        }

        throw new PaymentServiceException("Empty response from Payment Service");
    }

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getPaymentStatusFallback")
    @Retry(name = "paymentService")
    public PaymentResponse getPaymentStatus(String orderId) {
        log.info("Getting payment status for order: {}", orderId);

        ApiResponse<PaymentResponse> response = webClient.get()
                .uri("/payments/{orderId}", orderId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(body -> new PaymentServiceException("Payment Service client error: " + body)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(body -> new PaymentServiceException("Payment Service server error: " + body)))
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PaymentResponse>>() {})
                .block();

        if (response != null && response.getData() != null) {
            return response.getData();
        }

        throw new PaymentServiceException("Empty response from Payment Service");
    }

    private PaymentResponse createPaymentFallback(String orderId, BigDecimal amount, String currency, String description, Throwable t) {
        log.error("Circuit breaker fallback for createPayment, order: {}. Error: {}", orderId, t.getMessage());
        throw new PaymentServiceException("Payment Service is unavailable. Order saved with status CREATED. Error: " + t.getMessage());
    }

    private PaymentResponse getPaymentStatusFallback(String orderId, Throwable t) {
        log.error("Circuit breaker fallback for getPaymentStatus, order: {}. Error: {}", orderId, t.getMessage());
        throw new PaymentServiceException("Payment Service is unavailable. Error: " + t.getMessage());
    }
}

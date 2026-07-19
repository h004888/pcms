package com.pcms.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcms.paymentservice.client.OrderClient;
import com.pcms.paymentservice.entity.OutboxEvent;
import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.entity.WebhookEvent;
import com.pcms.paymentservice.enums.PaymentMethod;
import com.pcms.paymentservice.enums.PaymentStatus;
import com.pcms.paymentservice.enums.WebhookEventStatus;
import com.pcms.paymentservice.repository.OutboxEventRepository;
import com.pcms.paymentservice.repository.PaymentRepository;
import com.pcms.paymentservice.repository.WebhookEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SepayWebhookControllerTest {

    private MockMvc mockMvc;
    private WebhookEventRepository webhookRepo;
    private PaymentRepository paymentRepo;
    private OutboxEventRepository outboxRepo;
    private OrderClient orderClient;
    private ObjectMapper objectMapper;
    private static final String TEST_API_KEY = "test-api-key-123";

    @BeforeEach
    void setUp() {
        webhookRepo = Mockito.mock(WebhookEventRepository.class);
        paymentRepo = Mockito.mock(PaymentRepository.class);
        outboxRepo = Mockito.mock(OutboxEventRepository.class);
        orderClient = Mockito.mock(OrderClient.class);
        objectMapper = new ObjectMapper();

        SepayWebhookController controller = new SepayWebhookController(
                webhookRepo, paymentRepo, outboxRepo, orderClient, TEST_API_KEY);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldProcessValidWebhookAndMarkPaymentSuccess() throws Exception {
        UUID orderId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setPaymentMethod(PaymentMethod.QR);
        payment.setAmount(new BigDecimal("500000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionRef("ORD-001");
        payment.setStaffId(UUID.randomUUID());

        when(webhookRepo.findByGatewayEventId("evt-001")).thenReturn(Optional.empty());
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepo.findByTransactionRef("ORD-001")).thenReturn(Optional.of(payment));
        when(paymentRepo.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                {"id":"evt-001","gateway":"MBBank","content":"ORD-001",
                 "transferType":"in","transferAmount":500000,
                 "accountNumber":"68061220049999"}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("processed"));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepo, atLeastOnce()).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getAllValues().get(0).getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(outboxRepo).save(any(OutboxEvent.class));
    }

    @Test
    void shouldRejectRequestWithInvalidApiKey() throws Exception {
        String body = """
                {"id":"evt-002","content":"ORD-002","transferType":"in","transferAmount":100}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void shouldRejectRequestWithMissingApiKey() throws Exception {
        String body = """
                {"id":"evt-003","content":"ORD-003","transferType":"in","transferAmount":100}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        when(webhookRepo.findByGatewayEventId("evt-004")).thenReturn(Optional.empty());
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepo.findByTransactionRef("ORD-NONEXIST")).thenReturn(Optional.empty());

        String body = """
                {"id":"evt-004","content":"ORD-NONEXIST","transferType":"in","transferAmount":100}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void shouldRejectNonInTransfer() throws Exception {
        when(webhookRepo.findByGatewayEventId("evt-005")).thenReturn(Optional.empty());
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                {"id":"evt-005","content":"ORD-001","transferType":"out","transferAmount":500000}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void shouldReturnDuplicateWhenAlreadyProcessed() throws Exception {
        WebhookEvent existingEvent = new WebhookEvent();
        existingEvent.setGatewayEventId("evt-006");
        existingEvent.setStatus(WebhookEventStatus.PROCESSED);

        when(webhookRepo.findByGatewayEventId("evt-006")).thenReturn(Optional.of(existingEvent));
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                {"id":"evt-006","content":"ORD-001","transferType":"in","transferAmount":500000}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("duplicate"));

        verifyNoInteractions(paymentRepo);
        verifyNoInteractions(outboxRepo);
    }

    @Test
    void shouldParseOrderNumberFromContentWhenExtraTextPresent() throws Exception {
        UUID orderId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setPaymentMethod(PaymentMethod.QR);
        payment.setAmount(new BigDecimal("50000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setStaffId(UUID.randomUUID());

        when(webhookRepo.findByGatewayEventId("evt-parse-1")).thenReturn(Optional.empty());
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepo.findByTransactionRef("ORD202607190008")).thenReturn(Optional.of(payment));
        when(paymentRepo.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                {"id":"evt-parse-1","content":"ORD202607190008 I2MYINKJ/169184",
                 "transferType":"in","transferAmount":50000,
                 "accountNumber":"68061220049999"}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("processed"));

        verify(paymentRepo).findByTransactionRef("ORD202607190008");
        verify(paymentRepo, never()).findByTransactionRef("ORD202607190008 I2MYINKJ/169184");
    }

    @Test
    void shouldFallbackToFindByOrderIdWhenOrderNumberIsUuid() throws Exception {
        UUID orderId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setPaymentMethod(PaymentMethod.QR);
        payment.setAmount(new BigDecimal("50000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setStaffId(UUID.randomUUID());

        when(webhookRepo.findByGatewayEventId("evt-uuid-1")).thenReturn(Optional.empty());
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepo.findByTransactionRef(orderId.toString())).thenReturn(Optional.empty());
        when(paymentRepo.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(paymentRepo.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = String.format("""
                {"id":"evt-uuid-1","content":"%s some extra text",
                 "transferType":"in","transferAmount":50000,
                 "accountNumber":"68061220049999"}
                """, orderId.toString());

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("processed"));

        verify(paymentRepo).findByTransactionRef(orderId.toString());
        verify(paymentRepo).findByOrderId(orderId);
    }

    @Test
    void shouldReturnBadRequestWhenContentIsBlank() throws Exception {
        when(webhookRepo.findByGatewayEventId("evt-blank-1")).thenReturn(Optional.empty());
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                {"id":"evt-blank-1","content":"  ","transferType":"in","transferAmount":50000}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void shouldReturnBadRequestWhenTransferAmountInsufficient() throws Exception {
        UUID orderId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setPaymentMethod(PaymentMethod.QR);
        payment.setAmount(new BigDecimal("50000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setStaffId(UUID.randomUUID());

        when(webhookRepo.findByGatewayEventId("evt-insuff-1")).thenReturn(Optional.empty());
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepo.findByTransactionRef("ORD-LOW-001")).thenReturn(Optional.of(payment));

        String body = """
                {"id":"evt-insuff-1","content":"ORD-LOW-001","transferType":"in","transferAmount":30000}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void shouldMatchPaymentWhenOrderNumberHasNoHyphens() throws Exception {
        UUID orderId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setPaymentMethod(PaymentMethod.QR);
        payment.setAmount(new BigDecimal("50000"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setStaffId(UUID.randomUUID());

        when(webhookRepo.findByGatewayEventId("evt-hyphen-1")).thenReturn(Optional.empty());
        when(webhookRepo.save(any(WebhookEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepo.findByTransactionRef("ORD202607190009")).thenReturn(Optional.empty());
        when(paymentRepo.findByTransactionRef("ORD-20260719-0009")).thenReturn(Optional.of(payment));
        when(paymentRepo.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                {"id":"evt-hyphen-1","content":"ORD202607190009 some text",
                 "transferType":"in","transferAmount":50000,
                 "accountNumber":"68061220049999"}
                """;

        mockMvc.perform(post("/webhooks/sepay")
                        .header("Authorization", "Apikey " + TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("processed"));

        verify(paymentRepo).findByTransactionRef("ORD202607190009");
        verify(paymentRepo).findByTransactionRef("ORD-20260719-0009");
        verify(outboxRepo).save(any(OutboxEvent.class));
    }
}

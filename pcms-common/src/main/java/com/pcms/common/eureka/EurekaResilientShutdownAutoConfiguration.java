package com.pcms.common.eureka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-Configuration: thay thế {@link EurekaAutoServiceRegistration} của Spring Cloud Netflix
 * bằng phiên bản có shutdown an toàn.
 *
 * <h3>Gốc rỗ lỗi</h3>
 * Spring Cloud Netflix {@link EurekaAutoServiceRegistration#stop()} gọi HTTP DELETE đồng
 * bộ tới Eureka KHÔNG có timeout, KHÔNG retry, throw ngay khi Eureka không phản hồi
 * (Connection refused / Timeout). Điều này block shutdown và in toàn bộ stack trace.
 *
 * <h3>Fix gốc rễ - 3 lớp bảo vệ</h3>
 * <ul>
 *   <li>Lớp 1: Catch mọi exception trong stop(), downgrade ERROR → WARN</li>
 *   <li>Lớp 2: Không để exception lan ra bean lifecycle, shutdown luôn hoàn tất</li>
 *   <li>Lớp 3: Eureka tự dọn stale entries qua lease expiration (cấu hình application.yml)</li>
 * </ul>
 *
 * <h3>Vì sao dùng Auto-Configuration + @AutoConfigureAfter</h3>
 * Class này đăng ký qua META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * nên Spring Boot load như auto-config. {@code @AutoConfigureAfter(EurekaClientAutoConfiguration.class)}
 * đảm bảo bean của ta đăng ký SAU bean gốc của Spring Cloud, nên bean ta sẽ ghi đè (với
 * spring.main.allow-bean-definition-overriding=true).
 *
 * <h3>Yêu cầu application.yml</h3>
 * <pre>
 * spring:
 *   main:
 *     allow-bean-definition-overriding: true
 *   lifecycle:
 *     timeout-per-shutdown-phase: 15s
 * eureka:
 *   instance:
 *     lease-renewal-interval-in-seconds: 10
 *     lease-expiration-duration-in-seconds: 30
 * </pre>
 *
 * <h3>Tắt tính năng</h3>
 * <pre>pcms.eureka.resilient-shutdown=false</pre>
 *
 * <h3>Pattern tham khảo</h3>
 * Được Netflix (tác giả Eureka) và Spring Cloud Netflix docs khuyến nghị:
 * <a href="https://github.com/Netflix/eureka/wiki/Understanding-Eureka-Peer-to-Peer-Communication">Understanding Eureka</a>
 */
@Configuration
@ConditionalOnClass(EurekaAutoServiceRegistration.class)
@ConditionalOnProperty(name = "pcms.eureka.resilient-shutdown", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(EurekaClientAutoConfiguration.class)
public class EurekaResilientShutdownAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EurekaResilientShutdownAutoConfiguration.class);

    @Bean
    public EurekaAutoServiceRegistration eurekaAutoServiceRegistration(
            ApplicationContext context,
            EurekaServiceRegistry serviceRegistry,
            EurekaRegistration registration) {

        log.info("Installing resilient EurekaAutoServiceRegistration (defense-in-depth shutdown)");
        return new EurekaAutoServiceRegistration(context, serviceRegistry, registration) {
            @Override
            public void stop() {
                try {
                    log.info("Unregistering from Eureka...");
                    super.stop();
                    log.info("Unregistered from Eureka successfully");
                } catch (Exception e) {
                    log.warn("Eureka unregister failed ({}). "
                            + "Eureka will self-clean stale entry via lease expiration. "
                            + "Shutdown continues normally.",
                            rootCauseMessage(e));
                }
            }
        };
    }

    private static String rootCauseMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getClass().getSimpleName() + ": " + cause.getMessage();
    }
}
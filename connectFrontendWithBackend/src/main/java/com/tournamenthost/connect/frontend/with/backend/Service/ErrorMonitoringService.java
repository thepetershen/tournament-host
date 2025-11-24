package com.tournamenthost.connect.frontend.with.backend.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Central error monitoring and logging service.
 * Provides structured error logging with context information.
 *
 * In production, this can be extended to integrate with external monitoring services like:
 * - Sentry (https://sentry.io)
 * - Datadog (https://www.datadoghq.com)
 * - New Relic (https://newrelic.com)
 * - ELK Stack (Elasticsearch, Logstash, Kibana)
 */
@Service
public class ErrorMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(ErrorMonitoringService.class);

    /**
     * Log an error with full context information
     */
    public void logError(Exception exception, String contextMessage) {
        Map<String, Object> errorContext = buildErrorContext(exception);

        log.error("Error occurred: {} | Context: {} | Details: {}",
                  exception.getMessage(),
                  contextMessage,
                  errorContext,
                  exception);
    }

    /**
     * Log a warning with context
     */
    public void logWarning(String message, Map<String, Object> context) {
        log.warn("Warning: {} | Context: {}", message, context);
    }

    /**
     * Track a business metric or event
     */
    public void trackEvent(String eventName, Map<String, Object> properties) {
        log.info("Event tracked: {} | Properties: {}", eventName, properties);
    }

    /**
     * Build comprehensive error context for debugging
     */
    private Map<String, Object> buildErrorContext(Exception exception) {
        Map<String, Object> context = new HashMap<>();

        context.put("timestamp", LocalDateTime.now());
        context.put("exceptionType", exception.getClass().getSimpleName());
        context.put("message", exception.getMessage());

        // Add request context if available
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            context.put("method", request.getMethod());
            context.put("uri", request.getRequestURI());
            context.put("queryString", request.getQueryString());
            context.put("userAgent", request.getHeader("User-Agent"));
            context.put("remoteAddr", request.getRemoteAddr());
        }

        // Add stack trace summary (first 3 elements)
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            int limit = Math.min(3, stackTrace.length);
            StringBuilder trace = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                trace.append(stackTrace[i].toString()).append("; ");
            }
            context.put("stackTraceSummary", trace.toString());
        }

        return context;
    }

    /**
     * Example integration point for Sentry
     * Uncomment and configure when ready to use Sentry:
     *
     * Add dependency to pom.xml:
     * <dependency>
     *     <groupId>io.sentry</groupId>
     *     <artifactId>sentry-spring-boot-starter-jakarta</artifactId>
     *     <version>7.0.0</version>
     * </dependency>
     *
     * Add to application.properties:
     * sentry.dsn=${SENTRY_DSN}
     * sentry.traces-sample-rate=1.0
     * sentry.environment=${SPRING_PROFILES_ACTIVE:dev}
     */
    // @Autowired(required = false)
    // private Hub sentryHub;
    //
    // public void reportToSentry(Exception exception) {
    //     if (sentryHub != null) {
    //         sentryHub.captureException(exception);
    //     }
    // }
}

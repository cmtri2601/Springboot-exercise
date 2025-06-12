package nc.solon.person.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MethodAuditAspect {
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        String argsJson;
        try {
            argsJson = objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            argsJson = "Failed to serialize arguments: " + e.getMessage();
        }

        log.info("AUDIT START - Action: {}, Method: {}, Args: {}", action, method, argsJson);

        Object result = null;
        try {
            result = joinPoint.proceed();
            String resultJson;
            try {
                resultJson = objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                resultJson = "Failed to serialize arguments: " + e.getMessage();
            }
            log.info("AUDIT END - Action: {}, Method: {}, Result: {}", action, method, resultJson);
            return result;
        } catch (Throwable ex) {
            log.error("AUDIT ERROR - Action: {}, Method: {}, Error: {}", action, method, ex.getMessage());
            throw ex;
        }
    }
}


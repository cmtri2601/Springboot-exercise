package nc.solon.person.audit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ControllerAuditAspect {
    private final ObjectMapper objectMapper;

    // Target all methods in classes annotated with @RestController or @Controller
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        String argsJson;
        try {
            argsJson = objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            argsJson = "Failed to serialize arguments: " + e.getMessage();
        }

        log.info("[AUDIT] Incoming call: {}.{}() with arguments: {}", className, methodName, argsJson);
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String resultJson;
        try {
            resultJson = objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            resultJson = "Failed to serialize arguments: " + e.getMessage();
        }

        log.info("[AUDIT] Completed call: {}.{}() with result: {}", className, methodName, resultJson);
    }

    @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.error("[AUDIT] Exception in: {}.{}() with error: {}", className, methodName, ex.getMessage(), ex);
    }
}

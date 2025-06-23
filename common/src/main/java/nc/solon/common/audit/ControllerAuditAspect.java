package nc.solon.common.audit;

import lombok.extern.slf4j.Slf4j;
import nc.solon.common.constant.LogMessage;
import nc.solon.common.utils.Serialize;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/** The type Controller audit aspect. */
@Aspect
@Component
@Slf4j
public class ControllerAuditAspect {

  /** Controller methods. */
  // Target all methods in classes annotated with @RestController or @Controller
  @Pointcut(
      "within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
  public void controllerMethods() {}

  /**
   * Log before.
   *
   * @param joinPoint the join point
   */
  @Before("controllerMethods()")
  public void logBefore(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String className = signature.getDeclaringType().getSimpleName();
    String methodName = signature.getName();
    Object[] args = joinPoint.getArgs();
    String argsJson = Serialize.audit(args);
    log.info(LogMessage.AUDIT_CONTROLLER_INCOMING, className, methodName, argsJson);
  }

  /**
   * Log after returning.
   *
   * @param joinPoint the join point
   * @param result the result
   */
  @AfterReturning(pointcut = "controllerMethods()", returning = "result")
  public void logAfterReturning(JoinPoint joinPoint, Object result) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String className = signature.getDeclaringType().getSimpleName();
    String methodName = signature.getName();
    String resultJson = Serialize.audit(result);
    log.info(LogMessage.AUDIT_CONTROLLER_COMPLETED, className, methodName, resultJson);
  }

  /**
   * Log after throwing.
   *
   * @param joinPoint the join point
   * @param ex the ex
   */
  @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String className = signature.getDeclaringType().getSimpleName();
    String methodName = signature.getName();
    log.error(LogMessage.AUDIT_CONTROLLER_EXCEPTION, className, methodName, ex.getMessage(), ex);
  }
}

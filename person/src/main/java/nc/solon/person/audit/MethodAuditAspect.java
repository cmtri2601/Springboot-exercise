package nc.solon.person.audit;

import lombok.extern.slf4j.Slf4j;
import nc.solon.person.constant.LogMessage;
import nc.solon.person.utils.Serialize;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/** The type Method audit aspect. */
@Aspect
@Component
@Slf4j
public class MethodAuditAspect {

  /**
   * Audit method object.
   *
   * @param joinPoint the join point
   * @param auditable the auditable
   * @return the object
   * @throws Throwable the throwable
   */
  @Around("@annotation(auditable)")
  public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
    String action = auditable.action();
    String method = joinPoint.getSignature().toShortString();
    Object[] args = joinPoint.getArgs();
    String argsJson = Serialize.audit(args);
    log.info(LogMessage.AUDIT_METHOD_START, action, method, argsJson);

    try {
      Object result = joinPoint.proceed();
      String resultJson = Serialize.audit(result);
      log.info(LogMessage.AUDIT_METHOD_END, action, method, resultJson);
      return result;
    } catch (Throwable ex) {
      log.error(LogMessage.AUDIT_METHOD_ERROR, action, method, ex.getMessage());
      throw ex;
    }
  }
}

package nc.solon.person.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class TaxIdGenerator {

  @PersistenceContext private EntityManager entityManager;

  @Transactional
  public String generateTaxId() {
    Long nextVal =
        ((Number) entityManager.createNativeQuery("SELECT nextval('tax_id_seq')").getSingleResult())
            .longValue();
    return String.format("TAX%06d", nextVal);
  }
}

package nc.solon.camunda.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

/**
 * The type Tax id generator.
 */
@Component
public class TaxIdGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Generate tax id string.
     *
     * @return the string
     */
    @Transactional
    public String generateTaxId() {
        Long nextVal =
                ((Number) entityManager.createNativeQuery("SELECT nextval('tax_id_seq')").getSingleResult())
                        .longValue();
        return String.format("TAX%06d", nextVal);
    }
}

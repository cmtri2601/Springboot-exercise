package nc.solon.camunda.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import nc.solon.camunda.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The interface Person repository.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Find by tax id optional.
     *
     * @param taxId the tax id
     * @return the optional
     */
    Optional<Person> findByTaxId(String taxId);

    /**
     * Delete person by tax id.
     *
     * @param taxId the tax id
     */
    void deletePersonByTaxId(@NotBlank(message = "Tax id is mandatory") @Size(max = 10, message = "Tax id must not exceed 10 characters") String taxId);

}

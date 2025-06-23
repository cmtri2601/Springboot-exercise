package nc.solon.camunda.repository;

import nc.solon.camunda.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** The interface Person repository. */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}

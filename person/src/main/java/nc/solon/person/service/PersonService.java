package nc.solon.person.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nc.solon.person.dto.PersonInDTO;
import nc.solon.person.dto.PersonOutDTO;
import nc.solon.person.entity.Person;
import nc.solon.person.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public PersonOutDTO createPerson(PersonInDTO data) {
        Person person = Person.builder()
                .firstName(data.getFirstName())
                .lastName(data.getLastName())
                .dateOfBirth(data.getDateOfBirth())
                .taxId(data.getTaxId())
                .taxDebt(BigDecimal.ZERO)
                .build();

        Person saved = personRepository.save(person);
        return mapToDTO(saved);
    }

    public PersonOutDTO updatePerson(Long id, PersonInDTO data) {
        Person existing = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));

        existing.setFirstName(data.getFirstName());
        existing.setLastName(data.getLastName());
        existing.setDateOfBirth(data.getDateOfBirth());
        existing.setTaxDebt(data.getTaxDebt());

        Person updated = personRepository.save(existing);
        return mapToDTO(updated);
    }

    public void deletePerson(Long id) {
        personRepository.deleteById(id);
    }

    public Optional<PersonOutDTO> getPersonById(Long id) {
        return personRepository.findById(id).map(this::mapToDTO);
    }

    public Optional<PersonOutDTO> getPersonByTaxId(String taxId) {
        return personRepository.findByTaxId(taxId).map(this::mapToDTO);
    }

    public List<PersonOutDTO> getAllPersons() {
        return personRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PersonOutDTO> findPersonsByNamePrefixAndMinAge(String prefix, int minAge) {
        LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
        return personRepository.findByNamePrefixAndOlderThan(prefix, maxBirthDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PersonOutDTO mapToDTO(Person person) {
        return PersonOutDTO.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .taxId(person.getTaxId())
                .age(Period.between(person.getDateOfBirth(), LocalDate.now()).getYears())
                .taxDebt(person.getTaxDebt())
                .build();
    }
}

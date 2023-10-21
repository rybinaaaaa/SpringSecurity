package rybina.SpringBootSecurity.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import rybina.SpringBootSecurity.model.Person;
import rybina.SpringBootSecurity.services.PersonDetailService;
import rybina.SpringBootSecurity.services.PersonService;

@Component
public class PersonValidator implements Validator {

    private final PersonService personService;

    @Autowired
    public PersonValidator(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return  Person.class.equals(clazz);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Person person = (Person)o;

        if (personService.loadUserByUsername(person.getUsername()).isEmpty()) {
            return;
        }

        errors.rejectValue("username", "", "User with this username is already exist");
    }
}

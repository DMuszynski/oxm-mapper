package pl.dmuszynski.oxmmapper.samples;

import lombok.*;

import lombok.experimental.SuperBuilder;

import pl.dmuszynski.oxmmapper.tools.annotation.NodeAdapter;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;
import pl.dmuszynski.oxmmapper.tools.annotation.Property;
import pl.dmuszynski.oxmmapper.tools.annotation.Node;

import java.time.LocalDate;

@Getter
@ToString
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode
@RootNode
public class Person {
    @Node
    private final Address address;

    @Property
    private final String name;

    @Property
    private final String surname;

    @Property
    private final String phone;

    @Property
    @NodeAdapter(classType = DateAdapter.class)
    private final LocalDate dateOfBirth;

    public Person(Person person) {
        this.address = person.getAddress();
        this.name = person.getName();
        this.surname = person.surname;
        this.phone = person.phone;
        this.dateOfBirth = person.dateOfBirth;
    }
}

package pl.dmuszynski.oxmmapper.samples;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Builder;
import lombok.Getter;

import pl.dmuszynski.oxmmapper.tools.annotation.NodeAdapter;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;
import pl.dmuszynski.oxmmapper.tools.annotation.Property;
import pl.dmuszynski.oxmmapper.tools.annotation.Node;

import java.time.LocalDate;

@Getter
@Builder
@ToString
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
}

package pl.dmuszynski.oxmmapper.samples;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;

import pl.dmuszynski.oxmmapper.tools.annotation.Node;
import pl.dmuszynski.oxmmapper.tools.annotation.Property;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@RootNode
public class Address {
    @Node
    private final int houseNumber;

    @Property
    private final String streetAddress;

    @Property
    private final String city;

    @Property
    private final String zipCode;
}

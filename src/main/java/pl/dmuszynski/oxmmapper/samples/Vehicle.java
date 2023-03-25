package pl.dmuszynski.oxmmapper.samples;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;

import pl.dmuszynski.oxmmapper.tools.annotation.Property;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@RootNode
public class Vehicle {
    @Property
    private final String model;

    @Property
    private final String brand;

    @Property
    private final String licenseNumber;
}

package pl.dmuszynski.oxmmapper.samples;

import pl.dmuszynski.oxmmapper.tools.annotation.Property;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;

@RootNode
public record Vehicle(
        @Property String model,
        @Property String brand,
        @Property String licenseNumber) {
}

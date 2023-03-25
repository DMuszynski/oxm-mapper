package pl.dmuszynski.oxmmapper.samples;

import lombok.Builder;
import pl.dmuszynski.oxmmapper.tools.annotation.Property;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;

@Builder
@RootNode
public record Address(
    @Property int houseNumber,
    @Property String streetAddress,
    @Property String city,
    @Property String zipCode) {
}

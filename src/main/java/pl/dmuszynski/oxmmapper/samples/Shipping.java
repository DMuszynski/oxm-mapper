package pl.dmuszynski.oxmmapper.samples;

import pl.dmuszynski.oxmmapper.tools.annotation.Property;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;

@RootNode
public record Shipping(
        @Property String number,
        @Property String shippingType,
        @Property String wrappingType) {
}

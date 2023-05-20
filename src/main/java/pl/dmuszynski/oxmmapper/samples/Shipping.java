package pl.dmuszynski.oxmmapper.samples;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import pl.dmuszynski.oxmmapper.tools.annotation.Property;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@RootNode
public class Shipping {
    @Property
    private final String number;
    @Property
    private final String shippingType;
    @Property
    private final String wrappingType;
}

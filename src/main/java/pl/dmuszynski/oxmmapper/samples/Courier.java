package pl.dmuszynski.oxmmapper.samples;

import lombok.Getter;
import lombok.ToString;

import pl.dmuszynski.oxmmapper.tools.annotation.Node;
import pl.dmuszynski.oxmmapper.tools.annotation.RootNode;

@Getter
@ToString(callSuper = true)
@RootNode
public class Courier extends Person {
    @Node
    private final Vehicle vehicle;

    public Courier(Person person, Vehicle vehicle) {
        super(person);
        this.vehicle = vehicle;
    }
}

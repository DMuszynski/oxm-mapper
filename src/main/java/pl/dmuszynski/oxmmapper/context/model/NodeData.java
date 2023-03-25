package pl.dmuszynski.oxmmapper.context.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString(callSuper = true)
public class NodeData extends ElementData {
    private final boolean root;
    private final boolean managedReference;
}

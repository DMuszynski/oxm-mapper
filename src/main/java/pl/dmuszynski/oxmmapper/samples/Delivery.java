package pl.dmuszynski.oxmmapper.samples;

import lombok.*;

import pl.dmuszynski.oxmmapper.tools.annotation.Node;
import pl.dmuszynski.oxmmapper.tools.annotation.NodeAdapter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public final class Delivery {
    @Node
    private final List<Shipping> shippingList;

    @Node
    private final Address receiverAddress;

    @Node
    private final Address senderAddress;

    @Node
    private final Courier courier;

    @NodeAdapter(classType = DateAdapter.class)
    private final LocalDateTime shipmentTime;

    @NodeAdapter(classType = DateAdapter.class)
    private final LocalDateTime deliveryTime;
}

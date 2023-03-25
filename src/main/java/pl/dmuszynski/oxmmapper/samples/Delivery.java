package pl.dmuszynski.oxmmapper.samples;

import lombok.Builder;

import pl.dmuszynski.oxmmapper.tools.annotation.Node;
import pl.dmuszynski.oxmmapper.tools.annotation.NodeAdapter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record Delivery(
    @Node List<Shipping> shippingList,
    @Node Address receiverAddress,
    @Node Address senderAddress,
    @Node Courier courier,
    @NodeAdapter(classType = DateAdapter.class) LocalDateTime shipmentTime,
    @NodeAdapter(classType = DateAdapter.class) LocalDateTime deliveryTime) {
}

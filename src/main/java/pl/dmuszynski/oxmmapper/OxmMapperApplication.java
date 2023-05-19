package pl.dmuszynski.oxmmapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.dmuszynski.oxmmapper.samples.*;
import pl.dmuszynski.oxmmapper.exception.InvalidXmlContextException;
import pl.dmuszynski.oxmmapper.serializer.Serializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class OxmMapperApplication {

    private final static Logger log = LogManager.getLogger(OxmMapperApplication.class);

    public static void main(String[] args) {
        final Delivery delivery = generateDelivery();
        serializeDelivery(delivery);
    }

    private static void serializeDelivery(Delivery delivery) {
        try {
            XmlConverter xmlConverter = XmlConverter.load(Delivery.class);
            Serializer xmlSerializer = xmlConverter.serializer();
            xmlSerializer.serialize(delivery, "src/main/resources/xml");
        } catch (InvalidXmlContextException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private static Delivery generateDelivery() {
        final Person person = Person.builder()
                .name("Jan")
                .surname("Kowalski")
                .dateOfBirth(LocalDate.of(1954, 8, 14))
                .phone("733254732")
                .address(new Address(9, "Warszawska", "Kielce", "11-300"))
                .build();

        final Vehicle vehicle = new Vehicle("A4", "Audi", "123TJE");
        final Courier courier = new Courier(person, vehicle);

        final List<Shipping> shippingList = new ArrayList<>();
        shippingList.add(new Shipping("121CA1", "Packet", "Packet"));
        shippingList.add(new Shipping("12313", "Packet", "Packet"));

        return Delivery.builder()
                .shippingList(shippingList)
                .courier(courier)
                .receiverAddress(new Address(11, "Główna", "Wrocław", "12-100"))
                .senderAddress(new Address(13, "Nowa", "Warszawa", "00-010"))
                .shipmentTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0,0)))
                .deliveryTime(LocalDateTime.of(LocalDate.now(), LocalTime.now()))
                .build();
    }
}

package pl.dmuszynski.oxmmapper;

import pl.dmuszynski.oxmmapper.samples.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class OxmMapperApplication {
    public static void main(String[] args) {
        Person person = Person.builder()
                .name("Jan")
                .surname("Kowalski")
                .dateOfBirth(LocalDate.of(1954, 8, 14))
                .phone("733254732")
                .address(new Address(9, "Warszawska", "Kielce", "11-300"))
                .build();
        System.out.println(person);

        Vehicle vehicle = new Vehicle("A4", "Audi", "123TJE");
        System.out.println(vehicle);

        Courier courier = new Courier(person, vehicle);
        System.out.println(courier);

        List<Shipping> shippingList = new ArrayList<>();
        shippingList.add(new Shipping("121CA1", "Packet", "Packet"));
        shippingList.add(new Shipping("12313", "Packet", "Packet"));

        Delivery delivery = Delivery.builder()
                .shippingList(shippingList)
                .courier(courier)
                .receiverAddress(new Address(11, "Główna", "Wrocław", "12-100"))
                .senderAddress(new Address(13, "Nowa", "Warszawa", "00-010"))
                .shipmentTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 0,0)))
                .deliveryTime(LocalDateTime.of(LocalDate.now(), LocalTime.now()))
                .build();

        System.out.println(delivery);

    }
}

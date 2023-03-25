package pl.dmuszynski.oxmmapper.samples;

import pl.dmuszynski.oxmmapper.tools.xmldata.XmlAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class DateAdapter implements XmlAdapter<LocalDate, String> {
    @Override
    public String convert(LocalDate object) {
        return object.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }
}

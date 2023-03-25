package pl.dmuszynski.oxmmapper.serializer;

import pl.dmuszynski.oxmmapper.exception.InvalidXmlContextException;

public interface Serializer {

    <T> String serialize(T source) throws InvalidXmlContextException;

    <T> void serialize(T source, String outputPath) throws InvalidXmlContextException;
}

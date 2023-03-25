package pl.dmuszynski.oxmmapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.dmuszynski.oxmmapper.exception.InvalidXmlContextException;
import pl.dmuszynski.oxmmapper.serializer.Serializer;
import pl.dmuszynski.oxmmapper.serializer.XmlSerializer;

public class XmlConverter {

    private final static Logger log = LogManager.getLogger(XmlConverter.class);

    private final Serializer serializer;

    private XmlConverter(Class<?> clazz) throws InvalidXmlContextException {
        serializer = new XmlSerializer(clazz);
    }

    public Serializer serializer() {
        return serializer;
    }

    public static XmlConverter load(Class<?> clazz) throws InvalidXmlContextException {
        if (log.isInfoEnabled()) {
            log.info(String.format("Create converter for class: %s", clazz));
        }
        return new XmlConverter(clazz);
    }
}

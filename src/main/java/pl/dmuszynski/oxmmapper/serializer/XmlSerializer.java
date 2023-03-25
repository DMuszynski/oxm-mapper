package pl.dmuszynski.oxmmapper.serializer;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.dmuszynski.oxmmapper.context.Context;
import pl.dmuszynski.oxmmapper.context.ElementContext;
import pl.dmuszynski.oxmmapper.context.model.PropertyData;
import pl.dmuszynski.oxmmapper.context.model.ElementData;
import pl.dmuszynski.oxmmapper.context.model.NodeData;
import pl.dmuszynski.oxmmapper.exception.InvalidXmlContextException;
import pl.dmuszynski.oxmmapper.tools.xmldata.XmlAdapter;
import pl.dmuszynski.oxmmapper.tools.xmldata.XmlProperty;
import pl.dmuszynski.oxmmapper.tools.xmldata.XmlNode;
import pl.dmuszynski.oxmmapper.tools.xmldata.XmlParentNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.Level.DEBUG;
import static org.apache.logging.log4j.Level.INFO;

public class XmlSerializer implements Serializer {

    private static final Logger log = LogManager.getLogger(XmlSerializer.class);

    private static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String OPENING_TAG = "<";
    private static final String OPENING_CLOSE_TAG = "</";
    private static final String CLOSING_TAG = ">";
    private static final String CLOSING_CLOSE_TAG = "/>";
    private static final String BLANK_SPACE = " ";

    public static final String INVALID_CONTEXT_ERROR_MESSAGE = "Context not prepared for class %s";

    private final Context<ElementData> elementContext;

    public XmlSerializer(Class<?> clazz) throws InvalidXmlContextException {
        if (log.isInfoEnabled()) {
            log.info(String.format("Create serializer for class: %s", clazz));
        }
        this.elementContext = ElementContext.newContext(clazz);
    }

    @Override
    public <T> String serialize(T object) throws InvalidXmlContextException {
        log(String.format("Serialize object: %s", object), DEBUG);
        return serializeObject(object);
    }

    @Override
    public <T> void serialize(T object, String outputPath) throws InvalidXmlContextException {
        log(String.format("Serialize object: %s to: %s", object, outputPath), DEBUG);
        String xmlElement = serializeObject(object);
        writeToFile(outputPath, object, xmlElement);
    }

    private void log(String message, Level level) {
        if (log.isEnabled(level)) {
            log.debug(message);
        }
    }

    private <T> void writeToFile(String outputPath, T object, String xmlElement) {
        try {
            log(String.format("Write xml element: %s to file", xmlElement), INFO);
            writeToFile(outputPath, xmlElement, object.getClass().getSimpleName());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private void writeToFile(String outputPath, String xmlResult, String type) throws IOException {
        Path path = prepareFilePath(outputPath, type);
        Files.write(path.toAbsolutePath(), xmlResult.getBytes(StandardCharsets.UTF_8));
    }

    private Path prepareFilePath(String outputPath, String type) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = type + "_" + timestamp + ".xml";

        return Path.of(StringUtils.isNotBlank(outputPath) ? outputPath : "output", fileName);
    }

    private <T> String serializeObject(T object) throws InvalidXmlContextException {
        String xmlElement = generateXmlElement(object, 0);
        return XML_PREFIX + xmlElement;
    }

    private <T> String generateXmlElement(T object, int depth) throws InvalidXmlContextException {
        log(String.format("Generate xml element for object: %s", object), DEBUG);
        XmlParentNode rootNode = createRootNode(object, depth);
        collectChildrenNodes(rootNode, object);
        collectAttributes(rootNode, object);

        return generateXmlElement(rootNode);
    }

    private <T> void collectChildrenNodes(XmlParentNode parentNode, T object) throws InvalidXmlContextException {
        List<XmlNode> childrenNodes = createChildrenNodes(parentNode, object);
        parentNode.getChildren().addAll(childrenNodes);
    }

    private <T> List<XmlNode> createChildrenNodes(XmlParentNode parentNode, T object) throws InvalidXmlContextException {
        return Context.find(elementContext, object.getClass(), parentNode.getDepth())
                .map(ctx -> createChildrenNodes(ctx, parentNode, object))
                .orElseThrow(() -> new InvalidXmlContextException(String.format(INVALID_CONTEXT_ERROR_MESSAGE, object.getClass())));
    }

    private <T> List<XmlNode> createChildrenNodes(Context<ElementData> elementContext, XmlParentNode parentNode, T object) {
        return elementContext.getData().stream()
                .filter(NodeData.class::isInstance)
                .map(NodeData.class::cast)
                .filter(Predicate.not(NodeData::isRoot))
                .filter(Predicate.not(NodeData::isManagedReference))
                .map(nodeData -> createChildNode(nodeData, parentNode, object))
                .collect(Collectors.toList());
    }

    private <T> XmlNode createChildNode(NodeData nodeData, XmlParentNode parentNode, T object) {
        Object value = getFieldValue(nodeData, object).orElse(null);
        return new XmlNode(nodeData.getField().getName(), value, parentNode);
    }

    private <T> Optional<Object> getFieldValue(ElementData elementData, T object) {
        Field field = elementData.getField();

        return getFieldValue(field.getDeclaringClass(), field.getName(), object)
                .map(value -> convertValueIfAdapterPresent(elementData, value));
    }

    private Object convertValueIfAdapterPresent(ElementData elementData, Object value) {
        return Optional.ofNullable(elementData.getAdapter())
                .flatMap(this::getConvertMethod)
                .flatMap(adapterEntry -> convertValueViaAdapter(adapterEntry.getKey(), adapterEntry.getValue(), value))
                .orElse(value);
    }

    private <T extends XmlAdapter<?, ?>> Optional<Object> convertValueViaAdapter(Class<T> adapterType, Method convertMethod, Object parameter) {
        return createAdapterInstance(adapterType)
                .flatMap(adapterInstance -> invokeMethod(convertMethod, adapterInstance, parameter));
    }

    private <T extends XmlAdapter<?, ?>> Optional<T> createAdapterInstance(Class<T> type) {
        try {
            return Optional.of(type.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(String.format("Could not create adapter instance for type: %s!", type), e);
        }
        return Optional.empty();
    }

    public <T extends XmlAdapter<?, ?>> Optional<Map.Entry<Class<T>, Method>> getConvertMethod(Class<T> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals("convert"))
                .filter(method -> method.getDeclaringClass().equals(type))
                .map(method -> Map.entry(type, method))
                .findAny();
    }

    private <T> void collectAttributes(XmlParentNode parentNode, T object) throws InvalidXmlContextException {
        List<XmlProperty> attributes = createAttributes(object, parentNode.getDepth());
        parentNode.getAttributes().addAll(attributes);
    }

    private <T> List<XmlProperty> createAttributes(T object, int depth) throws InvalidXmlContextException {
        return Context.find(elementContext, object.getClass(), depth)
                .map(ctx -> createAttributes(ctx, object))
                .orElseThrow(() -> new InvalidXmlContextException(String.format(INVALID_CONTEXT_ERROR_MESSAGE, object.getClass())));
    }

    private <T> List<XmlProperty> createAttributes(Context<ElementData> elementContext, T object) {
        return elementContext.getData().stream()
                .filter(PropertyData.class::isInstance)
                .map(PropertyData.class::cast)
                .map(propertyData -> createAttribute(propertyData, object))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private <T> Optional<XmlProperty> createAttribute(PropertyData propertyData, T object) {
        return getFieldValue(propertyData, object)
                .map(fieldValue -> new XmlProperty(propertyData.getField().getName(), fieldValue));
    }

    private String generateXmlElement(XmlNode node) {
        if (isCollectionOrArrayType(node.getValue())) {
            return generateXmlElementFromCollection(node);
        } else if (hasComplexType(node)) {
            return generateXmlElementFromComplex(node);
        }
        return processGeneratingXmlElement(node);
    }

    private boolean hasComplexType(XmlNode node) {
        return !(node instanceof XmlParentNode) && isComplexType(node.getValue());
    }

    private String generateXmlElementFromComplex(XmlNode node) {
        return Optional.ofNullable(node.getValue())
                .flatMap(value -> generateXmlElementFromComplex(value, node.getParent().getDepth()))
                .orElse("");
    }

    private Optional<String> generateXmlElementFromComplex(Object object, int depth) {
        try {
            return Optional.of(generateXmlElement(object, ++depth));
        } catch (InvalidXmlContextException e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private boolean isComplexType(Object object) {
        return Optional.ofNullable(object)
                .map(Object::getClass)
                .map(clazz -> isNotPrimitiveType(object, clazz))
                .orElse(false);
    }

    private boolean isNotPrimitiveType(Object object, Class<?> clazz) {
        return !ClassUtils.isPrimitiveOrWrapper(clazz) && !(object instanceof String);
    }

    private boolean isCollectionOrArrayType(Object object) {
        return Optional.ofNullable(object)
                .map(Object::getClass)
                .map(this::isCollectionOrArray)
                .orElse(false);
    }

    private String processGeneratingXmlElement(XmlNode node) {
        StringBuilder xmlElementBuilder = new StringBuilder();
        xmlElementBuilder.append(generateXmlTag(node, false));
        String elementContent = generateXmlElementContent(node);

        return toXmlElement(node, xmlElementBuilder, elementContent);
    }

    private String toXmlElement(XmlNode node, StringBuilder xmlElementBuilder, String elementContent) {
        if (StringUtils.isNotBlank(elementContent.trim())) {
            appendXmlElementContent(xmlElementBuilder, node, elementContent);
            return xmlElementBuilder.toString();
        } else {
            return toNoContentXmlElement(xmlElementBuilder.toString());
        }
    }

    private void appendXmlElementContent(StringBuilder xmlElementBuilder, XmlNode node, String elementContent) {
        xmlElementBuilder.append(elementContent);
        xmlElementBuilder.append(generateXmlTag(node, true));
    }

    private String toNoContentXmlElement(String xmlElement) {
        return xmlElement.replaceFirst(CLOSING_TAG, CLOSING_CLOSE_TAG);
    }

    private String generateXmlElementFromCollection(XmlNode node) {
        return Optional.ofNullable(node.getValue())
                .map(collection -> toXmlNodes(collection, node))
                .stream()
                .flatMap(Set::stream)
                .map(this::generateXmlElement)
                .collect(Collectors.joining(""));
    }

    public boolean isCollectionOrArray(Class<?> clazz) {
        return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
    }

    private Set<XmlNode> toXmlNodes(Object object, XmlNode node) {
        Class<?> clazz = object.getClass();
        Collection<?> collection = toCollection(object, clazz.isArray());

        return toXmlNodeCollection(collection, node);
    }

    private <T> Collection<?> toCollection(Object object, boolean isArray) {
        if (isArray) {
            return new ArrayList<>(Arrays.asList((T[]) object));
        }
        return (Collection<?>) object;
    }

    private Set<XmlNode> toXmlNodeCollection(Collection<?> collection, XmlNode node) {
        return collection.stream()
                .map(el -> new XmlNode(node.getName(), el, node.getParent()))
                .collect(Collectors.toSet());
    }

    private String generateXmlElementContent(XmlNode node) {
        if (node instanceof XmlParentNode) {
            return generateXmlTagsForChildren(((XmlParentNode) node).getChildren());
        }
        return parseNodeValue(node);
    }

    private String parseNodeValue(XmlNode node) {
        return Optional.ofNullable(node.getValue())
                .map(String::valueOf)
                .orElse("");
    }

    private <T> Optional<Object> getFieldValue(Class<?> type, String name, T object) {
        return getFieldForName(type, name)
                .flatMap(field -> getFieldValue(field, object));
    }

    public static Optional<Field> getFieldForName(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.getName().equals(name)).findFirst();
    }

    private Optional<Method> getGetterOfField(Field field, List<Method> getters) {
        return getters.stream()
                .filter(getter -> isGetterOrSetterOfField(field, getter))
                .findFirst();
    }

    private boolean isGetterOrSetterOfField(Field field, Method method) {
        return method.getName().toLowerCase().contains(field.getName().toLowerCase());
    }

    public <T> Optional<Object> getFieldValue(Field field, T object) {
        return getGetterOfField(field, getGetters(field.getDeclaringClass()))
                .flatMap(method -> invokeMethod(method, object));
    }

    public List<Method> getGetters(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(this::isMethodGetter)
                .collect(Collectors.toList());
    }

    private boolean isMethodGetter(Method method) {
        String methodName = method.getName();
        return methodName.startsWith("get") || methodName.startsWith("is");
    }

    public <T> Optional<Object> invokeMethod(Method method, T object, Object... parameters) {
        try {
            return Optional.ofNullable(method.invoke(object, parameters));
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Object> invokeMethod(Method method, Object object) {
        try {
            return Optional.ofNullable(method.invoke(object));
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    private String generateXmlTag(XmlNode node, boolean closingTag) {
        StringBuilder tagBuilder = new StringBuilder(closingTag ? OPENING_CLOSE_TAG : OPENING_TAG);
        tagBuilder.append(node.getName());

        if (!closingTag) {
            appendAttributes(tagBuilder, node);
        }
        return tagBuilder.append(CLOSING_TAG).toString();
    }

    private void appendAttributes(StringBuilder tagBuilder, XmlNode node) {
        String attributes = generateAttributes(node);

        if (StringUtils.isNotBlank(attributes.trim())) {
            tagBuilder.append(BLANK_SPACE).append(attributes);
        }
    }

    private String generateAttributes(XmlNode node) {
        return node.getAttributes().stream()
                .map(this::generateTagAttribute)
                .collect(Collectors.joining(BLANK_SPACE));
    }

    private String generateTagAttribute(XmlProperty attribute) {
        StringBuilder attributeBuilder = new StringBuilder(attribute.getName());
        attributeBuilder.append("=").append("\"").append(attribute.getValue()).append("\"");

        return attributeBuilder.toString();
    }

    private String generateXmlTagsForChildren(Set<XmlNode> childrenNodes) {
        return childrenNodes.stream()
                .map(this::generateXmlElement)
                .collect(Collectors.joining(""));
    }

    private <T> XmlParentNode createRootNode(T object, int depth) throws InvalidXmlContextException {
        return Context.find(elementContext, object.getClass(), depth)
                .flatMap(this::getParentNode)
                .map(parentNode -> new XmlParentNode(parentNode.getName(), null, null, depth))
                .orElseThrow(() -> new InvalidXmlContextException(String.format(INVALID_CONTEXT_ERROR_MESSAGE, object.getClass())));
    }

    private Optional<NodeData> getParentNode(Context<ElementData> context) {
        return context.getData().stream()
                .filter(NodeData.class::isInstance)
                .map(NodeData.class::cast)
                .filter(NodeData::isRoot)
                .findAny();
    }
}

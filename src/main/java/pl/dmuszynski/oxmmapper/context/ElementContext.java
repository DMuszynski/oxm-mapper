package pl.dmuszynski.oxmmapper.context;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.dmuszynski.oxmmapper.context.model.PropertyData;
import pl.dmuszynski.oxmmapper.context.model.ElementData;
import pl.dmuszynski.oxmmapper.context.model.NodeData;
import pl.dmuszynski.oxmmapper.exception.InvalidXmlContextException;
import pl.dmuszynski.oxmmapper.tools.annotation.*;
import pl.dmuszynski.oxmmapper.tools.xmldata.XmlAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ElementContext implements Context<ElementData> {

    private final static Logger log = LogManager.getLogger(ElementContext.class);

    public static final String DEFAULT_NAME = "defaultName";

    private final Class<?> clazz;
    private final int depth;
    private final Context<ElementData> parentContext;
    private final Set<Class<?>> ancestorClasses;
    private final String rootNode;
    private final Map<String, Field> nodes;
    private final Map<String, Field> attributes;
    private final Map<Field, Class<? extends XmlAdapter<?, ?>>> nodeAdapters;
    private final Map<Field, Class<?>> mapReferences;
    private final Map<Class<?>, Context<ElementData>> subContexts;
    private final Set<ElementData> data;

    protected ElementContext(Class<?> clazz, Context<ElementData> parentContext) {
        this.clazz = clazz;
        this.parentContext = parentContext;
        this.depth = Objects.nonNull(parentContext) ? parentContext.getDepth() + 1 : 0;
        this.ancestorClasses = loadAncestorClasses();
        this.rootNode = loadParentNode();
        this.nodes = loadNodes();
        this.attributes = loadNodeAttributes();
        this.nodeAdapters = loadNodesAdapters();
        this.mapReferences = loadMapRef();
        this.subContexts = loadSubContexts();
        this.data = buildData();
    }

    public static Context<ElementData> newContext(Class<?> clazz) throws InvalidXmlContextException {
        return newContext(clazz, null);
    }

    public static Context<ElementData> newContext(Class<?> clazz, Context<ElementData> parentContext) throws InvalidXmlContextException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Load context for class: %s", clazz));
        }
        return new ElementContext(clazz, parentContext);
    }

    @Override
    public Set<Context<ElementData>> allContexts() {
        Set<Context<ElementData>> allContexts = new HashSet<>();
        collectContexts(allContexts, this);

        return allContexts.stream()
                .sorted(Comparator.comparingInt(Context::getDepth))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void collectContexts(Set<Context<ElementData>> allContexts, ElementContext context) {
        Set<Context<ElementData>> contexts = context.subContexts();
        allContexts.addAll(contexts);
        allContexts.add(this);

        contexts.forEach(ctx -> collectContexts(allContexts, (ElementContext) ctx));
    }

    private Set<Class<?>> loadAncestorClasses() {
        Set<Class<?>> ancestors = new LinkedHashSet<>();
        Class<?> ancestor = clazz.getSuperclass();

        while (ancestor != Object.class) {
            ancestors.add(ancestor);
            ancestor = ancestor.getSuperclass();
        }
        return ancestors;
    }

    private String loadParentNode() {
        return getTypeRootNodeName(clazz);
    }

    private String getTypeRootNodeName(Class<?> clazz) {
        String rootElementName = null;
//        rootElementName = clazz.getDeclaredAnnotation(RootNode.class).name();

        if (isDefaultName(rootElementName)) {
            return clazz.getSimpleName();
        }
        return rootElementName;
    }

    private boolean isDefaultName(String name) {
        return StringUtils.isBlank(name) || name.equals(DEFAULT_NAME);
    }

    private Map<String, Field> loadNodes() {
        return getFieldsByAnnotation(Node.class).stream()
                .map(field -> Map.entry(getNodeName(field), field))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (node1, node2) -> node1, LinkedHashMap::new));
    }

    private String getNodeName(Field field) {
        String name = field.getAnnotation(Node.class).name();
        return getName(field, name);
    }

    private String getName(Field field, String name) {
        if (StringUtils.isBlank(name) || name.equals(DEFAULT_NAME)) {
            return field.getName();
        }
        return name;
    }

    private Map<String, Field> loadNodeAttributes() {
        return getFieldsByAnnotation(Property.class).stream()
                .map(field -> Map.entry(getAttributeName(field), field))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (attr1, attr2) -> attr1, LinkedHashMap::new));
    }

    private String getAttributeName(Field field) {
        String name = field.getAnnotation(Property.class).name();
        return getName(field, name);
    }

    public Set<Field> getFieldsByAnnotation(Class<?> annotation) {
        return getAllFields().stream()
                .filter(field -> hasAnnotation(field, annotation))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<Field> getAllFields() {
        List<Field> allFields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        ancestorClasses.forEach(ancestor -> {
            List<Field> fields = Arrays.asList(ancestor.getDeclaredFields());
            Collections.reverse(fields);
            allFields.addAll(fields);
        });
        Collections.reverse(allFields);

        return allFields;
    }

    private boolean hasAnnotation(Field field, Class<?> clazz) {
        return Arrays.stream(field.getDeclaredAnnotations())
                .anyMatch(clazz::isInstance);
    }

    private Map<Field, Class<? extends XmlAdapter<?, ?>>> loadNodesAdapters() {
        return getFieldsByAnnotation(NodeAdapter.class).stream()
                .map(field -> Map.entry(field, getAdapterType(field)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (type1, type2) -> type1, LinkedHashMap::new));
    }

    private Class<? extends XmlAdapter<?, ?>> getAdapterType(Field field) {
        return field.getAnnotation(NodeAdapter.class).classType();
    }

    private Map<Field, Class<?>> loadMapRef() {
        return getFieldsByAnnotation(NodeMapRef.class).stream()
                .map(field -> Map.entry(field, clazz))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (type1, type2) -> type1, LinkedHashMap::new));
    }

    private Set<ElementData> buildData() {
        Set<ElementData> elementsData = new LinkedHashSet<>();
        elementsData.addAll(buildAttributesData());
        elementsData.addAll(buildNodesData());

        return elementsData;
    }

    private Set<PropertyData> buildAttributesData() {
        return attributes.entrySet().stream()
                .map(nodeEntry -> PropertyData.builder()
                        .field(nodeEntry.getValue())
                        .name(nodeEntry.getKey())
                        .adapter(getNodeAdapter(nodeEntry.getValue()))
                        .build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<NodeData> buildNodesData() {
        Set<NodeData> nodesData = nodes.entrySet().stream()
                .map(nodeEntry -> buildNodeData(nodeEntry.getValue(), nodeEntry.getKey()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        nodesData.add(buildNodeData(null, rootNode));

        return nodesData;
    }

    private NodeData buildNodeData(Field field, String name) {
        final Class<? extends XmlAdapter<?, ?>> adapter = getNodeAdapter(field);
        boolean managedReference = isManagedReference(field);

        return NodeData.builder()
                .field(field)
                .name(name)
                .root(name.equals(rootNode))
                .adapter(adapter)
                .managedReference(managedReference)
                .build();
    }

    private Class<? extends XmlAdapter<?, ?>> getNodeAdapter(Field field) {
        return nodeAdapters.entrySet().stream()
                .filter(adapterEntry -> adapterEntry.getKey().equals(field))
                .map(Map.Entry::getValue)
                .findAny()
                .orElse(null);
    }

    private boolean isManagedReference(Field field) {
        Class<?> type = getFieldType(field);

        return getParentMapReferences().stream()
                .anyMatch(managedReference -> managedReference.equals(type));
    }

    private Class<?> getFieldType(Field field) {
        return Optional.ofNullable(field)
                .map(fld -> {
                    if (isNotPrimitiveType(fld.getType())) {
                        return getComplexType(fld);
                    }
                    return fld.getType();
                })
                .orElse(null);
    }

    private Map<Class<?>, Context<ElementData>> loadSubContexts() {
        Collection<Class<?>> parentManagedReferences = getParentMapReferences();
        Map<Class<?>, Context<ElementData>> contexts = new HashMap<>();

        findComplexTypeFields().stream()
                .filter(Predicate.not(nodeAdapters::containsKey))
                .map(this::getComplexType)
                .filter(this::isNotPrimitiveType)
                .filter(Predicate.not(contexts::containsKey))
                .filter(Predicate.not(parentManagedReferences::contains))
                .map(this::toContextEntry)
                .flatMap(Optional::stream)
                .forEach(ctxEntry -> contexts.put(ctxEntry.getKey(), ctxEntry.getValue()));

        return contexts;
    }

    private Collection<Class<?>> getParentMapReferences() {
        return Optional.ofNullable(parentContext)
                .map(ctx -> ((ElementContext) ctx).getMapReferences().values())
                .orElseGet(HashSet::new);
    }

    private Class<?> getComplexType(Field field) {
        Class<?> fieldType = field.getType();

        if (isCollection(fieldType)) {
            return retrieveGenericType(field);
        } else if (fieldType.isArray()) {
            return fieldType.getComponentType();
        }
        return fieldType;
    }

    private Class<?> retrieveGenericType(Field field) {
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        return (Class<?>) stringListType.getActualTypeArguments()[0];
    }

    public boolean isCollection(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    private Optional<Map.Entry<Class<?>, Context<ElementData>>> toContextEntry(Class<?> fieldType) {
        try {
            return Optional.of(Map.entry(fieldType, ElementContext.newContext(fieldType, this)));
        } catch (InvalidXmlContextException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return Optional.empty();
    }

    private Set<Field> findComplexTypeFields() {
        return getAllFields().stream()
                .filter(field -> isNotPrimitiveType(field.getType()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isNotPrimitiveType(Class<?> clazz) {
        return !ClassUtils.isPrimitiveOrWrapper(clazz) && !(clazz.equals(String.class));
    }

    @Override
    public Class<?> getType() {
        return clazz;
    }

    public Context<ElementData> getParentContext() {
        return parentContext;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    public String getRootNode() {
        return rootNode;
    }

    public Map<String, Field> getNodes() {
        return nodes;
    }

    public Map<String, Field> getAttributes() {
        return attributes;
    }

    public Map<Field, Class<? extends XmlAdapter<?, ?>>> getNodeAdapters() {
        return nodeAdapters;
    }

    public Map<Field, Class<?>> getMapReferences() {
        return mapReferences;
    }

    @Override
    public Set<Context<ElementData>> subContexts() {
        return new HashSet<>(subContexts.values());
    }

    @Override
    public Set<ElementData> getData() {
        return data;
    }
}

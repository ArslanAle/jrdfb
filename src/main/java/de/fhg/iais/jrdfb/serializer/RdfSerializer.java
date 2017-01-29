package de.fhg.iais.jrdfb.serializer;

import de.fhg.iais.jrdfb.annotation.RdfId;
import de.fhg.iais.jrdfb.annotation.RdfProperty;
import de.fhg.iais.jrdfb.annotation.RdfType;
import de.fhg.iais.jrdfb.resolver.Resolver;
import de.fhg.iais.jrdfb.resolver.ResolverFactory;
import de.fhg.iais.jrdfb.resolver.ResolverFactoryImpl;
import de.fhg.iais.jrdfb.util.ReflectUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * @author <a href="mailto:ali.arslan@rwth-aachen.de">AliArslan</a>
 */
public class RdfSerializer {

    private Class[] tClasses;
    private ResolverFactory resolverFactory = new ResolverFactoryImpl();
    private Model model;

    public RdfSerializer(Class... tClasses){
        //this.rootClass = rootClass;
        this.tClasses = tClasses;
        model = ModelFactory.createDefaultModel();
    }
    public String serialize(Object obj) throws ReflectiveOperationException {
        assert (obj != null);
        Class rootClass = tClasses[0];
        for(Class clazz: tClasses){
            if(clazz.isInstance(obj))
                rootClass = clazz;
        }

        this.createResource(rootClass, obj);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        model.write(out, "TURTLE");

        String result = null;
        try {
            result = out.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Resource createResource(Class clazz, Object obj) throws
            ReflectiveOperationException{
        Resource resource = null;
        Resource metaData;
        Object id = null;
        String uriTemplate = "";
        final Iterable<Field> allFields = ReflectUtils.getFieldsUpTo(clazz, Object.class);
        for(Field field: allFields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(RdfId.class)) {
                Resolver resolver = resolverFactory.createResolver(field, model);
                RDFNode resolvedNode = resolver.resolveField(obj);
                id = resolvedNode.toString();
                uriTemplate = field.getAnnotation(RdfId.class).uriTemplate();
                break;
            }
        }
        id = (id == null || id.toString().isEmpty()) ? UUID.randomUUID() : id;

        if(uriTemplate.contains("{RfdId}")){
            id = uriTemplate.replace("{RfdId}", id.toString());
        }

        resource = model.createResource(id.toString());
        metaData = model.createResource();


        if(clazz.isAnnotationPresent(RdfType.class))
            resource.addProperty(RDF.type, ((RdfType) clazz.getAnnotation(RdfType.class))
                    .value());


        for(Field field: allFields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(RdfProperty.class)) {
                RdfProperty rdfPropertyInfo = field.getAnnotation(RdfProperty.class);
                Property jenaProperty = model.createProperty(rdfPropertyInfo.value());
                Resolver resolver = resolverFactory.createResolver(field, model);
                boolean resolved = false;
                for(Class tClass: tClasses){
                    if(tClass.getName().equals(resolver.resolveFieldClassName(obj))){
                        resource.addProperty(jenaProperty,
                                this.createResource(tClass, field.get(obj)));
                        resolved = true;
                        break;
                    }
                }

                if(!resolved){
                    RDFNode resolvedNode = resolver.resolveField(obj);
                    if(resolvedNode != null) {
                        resource.addProperty(jenaProperty, resolvedNode);
                        metaData.addProperty(jenaProperty, resolver.resolveFieldClassName(obj));
                    }
                }
            }
        }

        resource.addProperty(VOID.dataDump, metaData);

        return resource;
    }


    public Object deserialize(String data) throws ReflectiveOperationException {
        assert (data != null);

        Class rootClass = null;

        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(data.getBytes()), null, "TURTLE");

        Resource resource = null;
        for(Class clazz: tClasses){
            if(clazz.isAnnotationPresent(RdfType.class)){
                String RDFType  = ((RdfType) clazz.getAnnotation(RdfType.class)).value();
                ResIterator iter = model.listResourcesWithProperty(RDF.type, RDFType);
                if(iter!=null && iter.hasNext()){
                    rootClass = clazz;
                    resource = iter.nextResource();
                    break;
                }
            }
        }

        Constructor cons = rootClass.getDeclaredConstructor();
        cons.setAccessible(true);
        Object obj = cons.newInstance();

        if(resource==null)
            throw new ReflectiveOperationException("No matching resource found in rdf");

        final Iterable<Field> allFields = ReflectUtils.getFieldsUpTo(rootClass, Object.class);
        for(Field field: allFields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(RdfProperty.class)) {

                Object propertyVal = resolverFactory.createResolver(field, model)
                        .resolveProperty(resource);

                field.set(obj, propertyVal);
            }
        }

        return obj;
    }

}

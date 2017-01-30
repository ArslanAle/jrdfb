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
        Resource resource;
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
        id = (id == null || id.toString().isEmpty()) ? ReflectUtils.getChecksum(obj) : id;

        if(uriTemplate.contains("{RfdId}")){
            id = uriTemplate.replace("{RfdId}", id.toString());
        }

        resource = model.createResource(id.toString());
        metaData = model.createResource();

        String rdfType = null;
        if(clazz.isAnnotationPresent(RdfType.class)) {
            rdfType = ((RdfType) clazz.getAnnotation(RdfType.class)).value();

            resource.addProperty(RDF.type, rdfType);
            metaData.addProperty(model.createProperty(rdfType), obj.getClass().getName());
        }else
            throw new NoSuchFieldException("RdfType for class '"+obj.getClass().getName()+"' " +
                    "Not provided");
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
                    }
                }
                metaData.addProperty(jenaProperty, resolver.resolveFieldClassName(obj));
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
                String rdfType  = ((RdfType) clazz.getAnnotation(RdfType.class)).value();
                ResIterator iter = model.listResourcesWithProperty(RDF.type, rdfType);
                if(iter!=null && iter.hasNext()){
                    resource = iter.nextResource();

                    Resource metadata = (Resource)resource.getProperty(VOID.dataDump).getObject();
                    Statement metaProperty = metadata.getProperty(model.createProperty(rdfType));
                    if(metaProperty==null)
                        throw new NoSuchFieldException("Metadata for Java Class Name Not provided" +
                                " " +
                                "in RDF Resource: "+resource.getURI());
                    if(clazz.getName().equals(metaProperty.getObject().toString()
                    )){
                        rootClass = clazz;
                        break;
                    }
                }
            }
        }

        if(rootClass ==null)
            throw new ClassNotFoundException("No matching java class found for rdf resource: " +
                    ""+data);
        return createObject( rootClass, resource);
    }

    private Object createObject(Class clazz, Resource resource)
            throws ReflectiveOperationException {
        Constructor cons = clazz.getDeclaredConstructor();
        cons.setAccessible(true);
        Object obj = cons.newInstance();

        if(resource==null)
            throw new ReflectiveOperationException("No matching resource found in rdf");

        final Iterable<Field> allFields = ReflectUtils.getFieldsUpTo(clazz, Object.class);
        for(Field field: allFields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(RdfProperty.class)) {

                Resolver resolver = resolverFactory.createResolver(field, model);
                boolean resolved = false;
                Object propertyVal = null;
                RdfProperty rdfPropertyInfo = field.getAnnotation(RdfProperty.class);
                Property jenaProperty = model.createProperty(rdfPropertyInfo.value());
                for(Class tClass: tClasses){
                    if(tClass.getName().equals(resolver.resolveFieldClassName(resource))){
                        propertyVal = this.createObject(tClass,
                                (Resource)resource.getProperty(jenaProperty).getObject());
                        resolved = true;
                        break;
                    }
                }
                if(!resolved)
                    propertyVal = resolver.resolveProperty(resource);

                field.set(obj, propertyVal);
            }
        }

        return obj;
    }
}

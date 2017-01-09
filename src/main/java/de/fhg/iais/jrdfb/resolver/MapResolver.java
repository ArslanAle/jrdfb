package de.fhg.iais.jrdfb.resolver;

import de.fhg.iais.jrdfb.annotation.RdfBag;
import de.fhg.iais.jrdfb.util.ReflectUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:ali.arslan@rwth-aachen.de">AliArslan</a>
 */
public class MapResolver extends ObjectResolver {

    public MapResolver(Field field, Model model) {
        super(field, model);
    }

    @Override
    public RDFNode resolveField(Object object) throws ReflectiveOperationException {
        Object value = extractFieldValue(object);
        if(value == null) return null;

        RDFNode rdfNode = null;
        if(field.isAnnotationPresent(RdfBag.class)){
            Map map = (Map) value;
            Bag propertiesBag = model.createBag();

            Iterator it = map.entrySet().iterator();

            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                propertiesBag.add(model.createResource()
                        .addProperty(DCTerms.identifier, pair.getKey().toString())
                        .addProperty(RDF.value, pair.getValue().toString()));
            }

            rdfNode = propertiesBag;
        }

        return rdfNode;
    }

    @Override
    public Object resolveProperty(Resource resource) throws ClassNotFoundException {
        Statement value = resource.getProperty(getJenaProperty());
        return ReflectUtils.stringToObject(resolveFieldClassName(resource),
                value.getLiteral().getString());
    }
}

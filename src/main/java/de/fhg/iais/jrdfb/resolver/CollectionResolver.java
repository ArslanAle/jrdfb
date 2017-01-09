package de.fhg.iais.jrdfb.resolver;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:ali.arslan@rwth-aachen.de">AliArslan</a>
 */
public class CollectionResolver extends ObjectResolver {


    public CollectionResolver(Field field, Model model) {
        super(field, model);
    }

    @Override
    public RDFNode resolveField(Object object) throws ReflectiveOperationException {
        Object value = extractFieldValue(object);
        RDFNode rdfNode = null;

        return rdfNode;
    }

    @Override
    public Object resolveProperty(Resource resource) throws ClassNotFoundException {
        Statement value = resource.getProperty(getJenaProperty());
        return null;
    }
}

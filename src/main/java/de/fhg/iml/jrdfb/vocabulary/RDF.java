package de.fhg.iml.jrdfb.vocabulary;

/**
 * @author <a href="mailto:ali.arslan@rwth-aachen.de">AliArslan</a>
 */
public class RDF {

    /**
     * The namespace of the vocabulary as a string
     */
    private static final String uri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** returns the URI for this schema
     * @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }

    public static final String VALUE = uri + "value";
    public static final String TYPE = uri + "type";

}


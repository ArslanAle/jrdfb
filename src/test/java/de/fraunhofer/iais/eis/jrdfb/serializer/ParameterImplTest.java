package de.fraunhofer.iais.eis.jrdfb.serializer;

import de.fraunhofer.iais.eis.jrdfb.serializer.example.Parameter;
import de.fraunhofer.iais.eis.jrdfb.serializer.example.ParameterDataType;
import de.fraunhofer.iais.eis.jrdfb.serializer.example.ParameterImpl;
import de.fraunhofer.iais.eis.jrdfb.util.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author <a href="mailto:ali.arslan@rwth-aachen.de">AliArslan</a>
 */
public class ParameterImplTest {

    RdfSerializer serializer;
    String rdf_turtle;
    Model expectedModel;


    @BeforeClass
    public void setUp() throws Exception {
        serializer = new RdfSerializer(ParameterImpl.class);
        rdf_turtle = FileUtils
                .readResource("ParameterImpl.ttl",
                        this.getClass());

        expectedModel = ModelFactory.createDefaultModel();
        expectedModel.read(new ByteArrayInputStream(rdf_turtle.getBytes()), null, "TURTLE");
    }

    @Test
    public void testSerializeParameter() throws Exception{
        Parameter parameter = new ParameterImpl(ParameterDataType.XSD_STRING);

        Model actualModel = ModelFactory.createDefaultModel();
        String serializedTurtle = serializer.serialize(parameter).trim();
        System.out.println("Serialized Turtle:");
        System.out.println(serializedTurtle);
        actualModel.read(new ByteArrayInputStream(serializedTurtle.getBytes()),
                null, "TURTLE");

        assertTrue(expectedModel.isIsomorphicWith(actualModel));
    }

    @Test
    public void testDeserialize() throws Exception{
        ParameterImpl parameter =
                (ParameterImpl)serializer.deserialize(rdf_turtle);
        assertEquals(parameter.getDataType(), ParameterDataType.XSD_STRING);
    }
}
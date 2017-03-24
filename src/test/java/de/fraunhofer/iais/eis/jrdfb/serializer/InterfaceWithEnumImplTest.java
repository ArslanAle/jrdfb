package de.fraunhofer.iais.eis.jrdfb.serializer;

import de.fraunhofer.iais.eis.jrdfb.JrdfbException;
import de.fraunhofer.iais.eis.jrdfb.serializer.example.DayEnum;
import de.fraunhofer.iais.eis.jrdfb.serializer.example.InterfaceWithEnum;
import de.fraunhofer.iais.eis.jrdfb.serializer.example.InterfaceWithEnumImpl;
import de.fraunhofer.iais.eis.jrdfb.util.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author <a href="mailto:ali.arslan@rwth-aachen.de">AliArslan</a>
 */
public class InterfaceWithEnumImplTest {
    RdfSerializer serializer;
    String rdf_turtle;
    Resource expectedRes;
    Model expectedModel;


    @BeforeMethod
    public void setUp() throws Exception {
        serializer = new RdfSerializer(InterfaceWithEnumImpl.class);
        rdf_turtle = FileUtils
                .readResource("InterfaceWithEnumImpl.ttl",
                        this.getClass());

        expectedModel = ModelFactory.createDefaultModel();
        expectedModel.read(new ByteArrayInputStream(rdf_turtle.getBytes()), null, "TURTLE");
    }

    @Test
    public void testSerialization() throws Exception{
        InterfaceWithEnum interfaceWithEnum = new InterfaceWithEnumImpl(DayEnum.FRIDAY);

        Model actualModel = ModelFactory.createDefaultModel();
        String serializedTurtle = serializer.serialize(interfaceWithEnum).trim();
        System.out.println("Serialized Turtle:");
        System.out.println(serializedTurtle);
        actualModel.read(new ByteArrayInputStream(serializedTurtle.getBytes()),
                null, "TURTLE");

        assertTrue(expectedModel.isIsomorphicWith(actualModel));
    }

    @Test
    public void testDeserialize() throws Exception{
        InterfaceWithEnumImpl interfaceWithEnum = (InterfaceWithEnumImpl)serializer.deserialize(rdf_turtle);

        assertNotNull(interfaceWithEnum.id);
        assertEquals(interfaceWithEnum.getSomeEnum(), DayEnum.FRIDAY);
    }

    @Test
    public void enumFieldUnused() throws JrdfbException {
        InterfaceWithEnum interfaceWithEnum = new InterfaceWithEnumImpl(null);
        String serializedTurtle = serializer.serialize(interfaceWithEnum).trim();
        Model serializedModel = ModelFactory.createDefaultModel();
        serializedModel.read(new ByteArrayInputStream(serializedTurtle.getBytes()), null, "TURTLE");

        InterfaceWithEnumImpl interfaceWithEnum_fromRdf =
                (InterfaceWithEnumImpl)serializer.deserialize(serializedTurtle);
        String reSerializedTurtle = serializer.serialize(interfaceWithEnum_fromRdf).trim();
        Model reSerializedModel = ModelFactory.createDefaultModel();
        reSerializedModel.read(new ByteArrayInputStream(serializedTurtle.getBytes()), null, "TURTLE");

        assertTrue(reSerializedModel.isIsomorphicWith(serializedModel));
    }

}
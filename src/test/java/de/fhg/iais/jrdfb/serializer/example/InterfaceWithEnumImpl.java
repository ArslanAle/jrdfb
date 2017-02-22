package de.fhg.iais.jrdfb.serializer.example;

import de.fhg.iais.jrdfb.annotation.RdfId;

/**
 * @author <a href="mailto:ali.arslan@rwth-aachen.de">AliArslan</a>
 */
public class InterfaceWithEnumImpl implements InterfaceWithEnum{
    @RdfId
    private String id = "http://example.com/InterfaceWithEnumImpl";

    private DayEnum someEnum;

    public InterfaceWithEnumImpl(DayEnum someEnum) {
        this.someEnum = someEnum;
    }

    @Override
    public DayEnum getSomeEnum() {
        return this.someEnum;
    }

    public void setSomeEnum(DayEnum someEnum) {
        this.someEnum = someEnum;
    }

}

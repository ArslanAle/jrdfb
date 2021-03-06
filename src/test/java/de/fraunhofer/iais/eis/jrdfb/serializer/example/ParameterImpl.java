package de.fraunhofer.iais.eis.jrdfb.serializer.example;

import de.fraunhofer.iais.eis.jrdfb.annotation.RdfId;

/**
 * @author <a href="mailto:ali.arslan@rwth-aachen.de">AliArslan</a>
 */
public class ParameterImpl implements Parameter {
    @RdfId
    private String id = "http://example.com/ParameterImpl";

    ParameterDataType dataType;

    public ParameterImpl(ParameterDataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public ParameterDataType getDataType() {
        return dataType;
    }

    public void setDataType(ParameterDataType dataType) {
        this.dataType = dataType;
    }
}

package com.beardbuddy.web.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CompactDoubleSerializer extends JsonSerializer<Double> {

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (!value.isNaN() && !value.isInfinite() && value == Math.rint(value) && Math.abs(value) < 1e15) {
            gen.writeNumber(value.longValue());
        } else {
            gen.writeNumber(value);
        }
    }
}

package uk.gov.companieshouse.pscdataapi.serdes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.pscdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.pscmerge.PscMerge;

public class PscMergeSerialiser implements Serializer<PscMerge> {

    @Override
    public byte[] serialize(String topic, PscMerge message) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<PscMerge> writer = getDatumWriter();
        try {
            writer.write(message, encoder);
        } catch (IOException ex) {
            throw new SerDesException("Error serialising PscMerge message", ex);
        }
        return outputStream.toByteArray();
    }

    public DatumWriter<PscMerge> getDatumWriter() {
        return new ReflectDatumWriter<>(PscMerge.class);
    }
}

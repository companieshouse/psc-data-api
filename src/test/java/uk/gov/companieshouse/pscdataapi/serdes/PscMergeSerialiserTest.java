package uk.gov.companieshouse.pscdataapi.serdes;

import java.io.IOException;

import org.apache.avro.io.DatumWriter;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.pscdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.pscmerge.PscMerge;

@ExtendWith(MockitoExtension.class)
class PscMergeSerialiserTest {

    @Mock
    private DatumWriter<PscMerge> writer;

    @Test
    void testSerialiseChsDelta() {
        // given
        PscMerge delta = new PscMerge("pscId", "previousPscId", "contextId");
        try (PscMergeSerialiser serialiser = new PscMergeSerialiser()) {

            // when
            byte[] actual = serialiser.serialize("topic", delta);

            // then
            assertThat(actual, is(notNullValue()));
        }
    }

    @Test
    void testThrowNonRetryableExceptionIfIOExceptionThrown() throws IOException {
        // given
        PscMerge delta = new PscMerge("pscId", "previousPscId", "contextId");
        PscMergeSerialiser serialiser = spy(new PscMergeSerialiser());
        when(serialiser.getDatumWriter()).thenReturn(writer);
        doThrow(IOException.class).when(writer).write(any(), any());

        // when
        Executable actual = () -> serialiser.serialize("topic", delta);

        // then
        SerDesException exception = assertThrows(SerDesException.class, actual);
        assertThat(exception.getCause(), is(instanceOf(IOException.class)));
    }
}

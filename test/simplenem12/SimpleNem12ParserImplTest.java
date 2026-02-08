package simplenem12;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Collection;

import org.junit.jupiter.api.Test;

public class SimpleNem12ParserImplTest {

    
    // These paths are relative to the project root when running JUnit in Eclipse.
    private static final String BASE_DIR = "resources/nem12/";

    private File csv(String fileName) {
        File f = new File(BASE_DIR + fileName);
        assertTrue(f.exists(), "Missing test CSV file: " + f.getPath());
        assertTrue(f.isFile(), "Not a file: " + f.getPath());
        return f;
    }

    @Test
    void parsesValidFile_multipleMeters_noExactMatchAssumption() {
        File f = csv("valid.csv");

        Collection<MeterRead> reads = new SimpleNem12ParserImpl().parseSimpleNem12(f);

        assertNotNull(reads);
        assertTrue(reads.size() >= 1, "Expected at least one 200 block");

        
        for (MeterRead mr : reads) {
            assertNotNull(mr.getNmi(), "NMI should not be null");
            assertEquals(10, mr.getNmi().length(), "NMI must be exactly 10 chars");

            assertNotNull(mr.getEnergyUnit(), "EnergyUnit should not be null");
            assertNotNull(mr.getVolumes(), "Volumes map should not be null");
        }
    }

    @Test
    void recordType100_mustBeFirstLine() {
        File f = csv("bad_100_not_first.csv");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> new SimpleNem12ParserImpl().parseSimpleNem12(f));

        assertTrue(ex.getMessage().contains("RecordType 100 must be the first line"),
                "Unexpected message: " + ex.getMessage());
    }

    @Test
    void recordType200_nmiMustBeExactly10Chars() {
        File f = csv("bad_nmi_length.csv");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> new SimpleNem12ParserImpl().parseSimpleNem12(f));

        assertTrue(ex.getMessage().contains("NMI must be 10 characters"),
                "Unexpected message: " + ex.getMessage());
    }

    @Test
    void recordType300_mustHavePreceding200() {
        File f = csv("bad_300_without_200.csv");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> new SimpleNem12ParserImpl().parseSimpleNem12(f));

        assertTrue(ex.getMessage().contains("300 record without preceding 200"),
                "Unexpected message: " + ex.getMessage());
    }

    @Test
    void recordType900_mustBeLastLine() {
        File f = csv("bad_900_not_last.csv");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> new SimpleNem12ParserImpl().parseSimpleNem12(f));

        assertTrue(ex.getMessage().contains("RecordType 900 must be the last line"),
                "Unexpected message: " + ex.getMessage());
    }
}

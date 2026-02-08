package simplenem12;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleNem12ParserImpl implements SimpleNem12Parser {

    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) {

        List<MeterRead> meterReads = new ArrayList<>(); 
        MeterRead currentMeterRead = null;              
        
        // Track if RecordType 100 has appeared
        boolean startSeen = false; 
        // Track if RecordType 900 has appeared
        boolean endSeen = false;   
        // Track line number for error messages    
        int lineNumber = 0; 

        try (
            BufferedReader reader = new BufferedReader(new FileReader(simpleNem12File)) 
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim(); // 
                if (line.isEmpty()) continue; 

                // If 900 has already been seen, no more records are allowed
                if (endSeen) {
                    throw new RuntimeException("RecordType 900 must be the last line (extra data at line " + lineNumber + ")");
                }

                String[] tokens = line.split(","); 
                String recordType = tokens[0].trim(); // First token determines record type

                switch (recordType) {

                    case "100":
                        // 100 record must be first line
                        if (lineNumber != 1) {
                            throw new RuntimeException("RecordType 100 must be the first line");
                        }

                        // 100 record should contain only one field
                        if (tokens.length != 1) {
                            throw new RuntimeException("Invalid 100 record at line " + lineNumber);
                        }

                        startSeen = true;
                        break;

                    case "200":
                        // Ensure file started correctly with 100
                        if (!startSeen) {
                            throw new RuntimeException("RecordType 100 must be the first line");
                        }

                        // Validate exact fields: RecordType, NMI, EnergyUnit
                        if (tokens.length != 3) {
                            throw new RuntimeException("Invalid 200 record at line " + lineNumber);
                        }

                        // Extract NMI and validate length (must be 10 chars)
                        String nmi = tokens[1].trim();
                        if (nmi.length() != 10) {
                            throw new RuntimeException("NMI must be 10 characters at line " + lineNumber);
                        }

                        // Convert energy unit string into enum; ensures only valid units
                        EnergyUnit energyUnit;
                        try {
                            energyUnit = EnergyUnit.valueOf(tokens[2].trim());
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid EnergyUnit at line " + lineNumber);
                        }

                        // Create a new MeterRead object for this 200 record
                        currentMeterRead = new MeterRead(nmi, energyUnit);

                        // Ensures returned collection is complete
                        meterReads.add(currentMeterRead);
                        break;

                    case "300":
                        // Ensure file started correctly with 100
                        if (!startSeen) {
                            throw new RuntimeException("RecordType 100 must be the first line");
                        }

                        // Record 300 must belong to a meter
                        if (currentMeterRead == null) {
                            throw new RuntimeException("300 record without preceding 200 at line " + lineNumber);
                        }

                        // Validate exact fields: RecordType, Date, Volume, Quality
                        if (tokens.length != 4) {
                            throw new RuntimeException("Invalid 300 record at line " + lineNumber);
                        }

                        // Parse date string to LocalDate
                        LocalDate date;
                        try {
                            date = LocalDate.parse(tokens[1].trim(), DATE_FORMAT);
                        } catch (DateTimeParseException e) {
                            throw new RuntimeException("Invalid date (yyyyMMdd) at line " + lineNumber);
                        }

                        
                        BigDecimal volume;
                        try {
                            volume = new BigDecimal(tokens[2].trim());
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Invalid volume at line " + lineNumber);
                        }

                        // Convert quality string ("A" or "E") to Quality enum; ensures type safety
                        Quality quality;
                        try {
                            quality = Quality.valueOf(tokens[3].trim());
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid quality at line " + lineNumber);
                        }

                        
                        MeterVolume meterVolume = new MeterVolume(volume, quality);

                        
                        // Correctly associates each date with its reading
                        currentMeterRead.appendVolume(date, meterVolume);
                        break;

                    case "900":
                        // 900 indicates end of file and must be the final non-empty line
                        if (!startSeen) {
                            throw new RuntimeException("RecordType 100 must be the first line");
                        }

                        // 900 record should contain only one field
                        if (tokens.length != 1) {
                            throw new RuntimeException("Invalid 900 record at line " + lineNumber);
                        }

                        endSeen = true;
                        break;

                    default:
                        // Unknown record type → error to prevent invalid parsing
                        throw new RuntimeException("Unknown record type '" + recordType + "' at line " + lineNumber);
                }
            }

            // Ensure file ends with RecordType 900
            if (!endSeen) {
                throw new RuntimeException("RecordType 900 must be the last line");
            }

        } catch (IOException e) {
            // Wrap IOException in RuntimeException so method signature matches interface
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }

        
        return meterReads;
    }
}

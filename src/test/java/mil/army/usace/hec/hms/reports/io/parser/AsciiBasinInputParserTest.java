package mil.army.usace.hec.hms.reports.io.parser;

import mil.army.usace.hec.hms.reports.*;
import mil.army.usace.hec.hms.reports.Process;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsciiBasinInputParserTest {

    @Test
    void getElementInput() {
        String basinPath = "src/test/resources/SimulationRun/Punx/punxsutawney.basin";
        BasinInputParser parser = BasinInputParser.builder().pathToBasinInputFile(basinPath).build();
        List<ElementInput> parsedInputs = parser.getElementInput();

        /* Checking for amount and items parsed */
        assertEquals(5, parsedInputs.size());
        assertEquals("EB Mahoning Ck", parsedInputs.get(0).getName());
        assertEquals("Stump Ck", parsedInputs.get(1).getName());
        assertEquals("Mahoning Ck", parsedInputs.get(2).getName());
        assertEquals("Punx Local", parsedInputs.get(3).getName());
        assertEquals("Sink-1", parsedInputs.get(4).getName());

        List<Process> expectedProcesses;
        Process area = Process.builder().value("42.030").build();
        Process latitude = Process.builder().value("40.97944848820452").build();
        Process longitude = Process.builder().value("-78.7858587588502").build();
        Process downstream = Process.builder().value("Mahoning Ck").build();
        Process loss = Process.builder().value("Deficit Constant").build();
        Process canopy = Process.builder().value("Simple").build();
        Process transform = Process.builder().value("Modified Clark").build();
        Process baseflow = Process.builder().value("Linear Reservoir").build();
        expectedProcesses = Arrays.asList(area, latitude, longitude, downstream, loss, canopy, transform, baseflow);

        /* Checking single processes */
        List<Process> actualInputProcesses = parsedInputs.get(0).getProcesses();
        for(int i = 0; i < actualInputProcesses.size(); i++) {
            assertEquals(expectedProcesses.get(i).getValue(), actualInputProcesses.get(i).getValue());
        }

        /* Checking complex processes, i.e Baseflow */
        Parameter actualBaseflow = actualInputProcesses.get(actualInputProcesses.size() - 1).getParameters().get(0);
        assertEquals(2, actualBaseflow.getSubParameters().size());

        List<Parameter> gw1 = actualBaseflow.getSubParameters().get(0).getSubParameters();
        assertEquals("GW-1 Baseflow Fraction", gw1.get(0).getName());
        assertEquals("0.5", gw1.get(0).getValue());
        assertEquals("GW-1 Number Reservoirs", gw1.get(1).getName());
        assertEquals("1", gw1.get(1).getValue());
        assertEquals("GW-1 Routing Coefficient", gw1.get(2).getName());
        assertEquals("30", gw1.get(2).getValue());
        assertEquals("GW-1 Initial Flow/Area Ratio", gw1.get(3).getName());
        assertEquals("0", gw1.get(3).getValue());

        List<Parameter> gw2 = actualBaseflow.getSubParameters().get(1).getSubParameters();
        assertEquals("GW-2 Baseflow Fraction", gw2.get(0).getName());
        assertEquals("0.5", gw2.get(0).getValue());
        assertEquals("GW-2 Number Reservoirs", gw2.get(1).getName());
        assertEquals("1", gw2.get(1).getValue());
        assertEquals("GW-2 Routing Coefficient", gw2.get(2).getName());
        assertEquals("150", gw2.get(2).getValue());
        assertEquals("GW-2 Initial Flow/Area Ratio", gw2.get(3).getName());
        assertEquals("1", gw2.get(3).getValue());
    }

    @Test
    void getLastModifiedTime() {
        String basinPath = "src/test/resources/SimulationRun/Punx/punxsutawney.basin";
        BasinInputParser parser = BasinInputParser.builder().pathToBasinInputFile(basinPath).build();
        ZonedDateTime parsedDate = parser.getLastModifiedTime();
        ZonedDateTime expectedDate = ZonedDateTime.of(2020, 4, 14, 21, 32, 33, 0, ZoneId.of("GMT"));
        assertEquals(expectedDate, parsedDate);
    }

    @Test
    void getHmsVersion() {
        String basinPath = "src/test/resources/SimulationRun/Punx/punxsutawney.basin";
        BasinInputParser parser = BasinInputParser.builder().pathToBasinInputFile(basinPath).build();
        String parsedVersion = parser.getHmsVersion();
        assertEquals("4.9", parsedVersion);
    }

    @Test
    void getDisplayUnitSystem() {
        String basinPath = "src/test/resources/SimulationRun/Punx/punxsutawney.basin";
        BasinInputParser parser = BasinInputParser.builder().pathToBasinInputFile(basinPath).build();
        assertTrue(parser instanceof AsciiBasinInputParser);
        AsciiBasinInputParser asciiParser = (AsciiBasinInputParser) parser;
        assertTrue(asciiParser.getDisplayUnitSystem() instanceof EnglishDisplayUnits);
    }
}
package mil.army.usace.hec.hms.reports.io.parser;

import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.util.TimeUtil;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlBasinResultsParserTest {
    @Test
    void getElementResults() {
        String projectPath = "src/test/resources/SimulationRun/Punx";
        String resultsPath = projectPath + "/RUN_Sep_2018.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.RUN)
                .build();

        Map<String, ElementResults> resultsMap = parser.getElementResults();
        System.out.println("Hello World");

        assertEquals(5, resultsMap.size());
        assertEquals(40, resultsMap.get("EB Mahoning Ck").getStatisticResultsMap().size());
        assertEquals(24, resultsMap.get("EB Mahoning Ck").getTimeSeriesResultsMap().size());
        assertEquals(2, resultsMap.get("EB Mahoning Ck").getOtherResults().size());
    }

    @Test
    void getSimulationName_RUN() {
        String projectPath = "src/test/resources/SimulationRun/Punx";
        String resultsPath = projectPath + "/RUN_Sep_2018.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.RUN)
                .build();

        assertEquals("Sep-2018", parser.getSimulationName());
    }

    @Test
    void getAvailablePlots_RUN() {
        String projectPath = "src/test/resources/SimulationRun/Punx";
        String resultsPath = projectPath + "/RUN_Sep_2018.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.RUN)
                .build();

        List<String> expected = Arrays.asList("Air Temperature", "Aquifer Recharge", "Baseflow",
                "Canopy Evapotranspiration", "Canopy Overflow", "Canopy Storage", "Combined Inflow",
                "Cumulative Excess Precipitation", "Cumulative Observed Flow", "Cumulative Outflow",
                "Cumulative Precipitation", "Cumulative Precipitation Loss", "Daily Average Temperature",
                "Daily Maximum Temperature", "Daily Minimum Temperature", "Direct Runoff", "Moisture Deficit",
                "Potential Evapotranspiration", "Precipitation Loss", "Precipitation Standard Deviation",
                "Residual Flow", "Saturation Fraction", "Soil Infiltration", "Soil Percolation");

        assertEquals(expected, parser.getAvailablePlots());
    }

    @Test
    void getStartTime_RUN() {
        String projectPath = "src/test/resources/SimulationRun/Punx";
        String resultsPath = projectPath + "/RUN_Sep_2018.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.RUN)
                .build();

        ZonedDateTime expected = ZonedDateTime.of(2018, 9, 1, 0, 0, 0, 0, ZoneId.of("Z"));
        assertEquals(expected, TimeUtil.toZonedDateTime(parser.getStartTime()));
    }

    @Test
    void getEndTime_RUN() {
        String projectPath = "src/test/resources/SimulationRun/Punx";
        String resultsPath = projectPath + "/RUN_Sep_2018.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.RUN)
                .build();

        ZonedDateTime expected = ZonedDateTime.of(2018, 9, 29, 0, 0, 0, 0, ZoneId.of("Z"));
        assertEquals(expected, TimeUtil.toZonedDateTime(parser.getEndTime()));
    }

    @Test
    void getLastComputedTime_RUN() {
        String projectPath = "src/test/resources/SimulationRun/Punx";
        String resultsPath = projectPath + "/RUN_Sep_2018.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.RUN)
                .build();

        ZonedDateTime expected = ZonedDateTime.of(2021, 3, 25, 22, 1, 10, 0, ZoneId.of("GMT"));
        assertEquals(expected, parser.getLastComputedTime());
    }
}
package mil.army.usace.hec.hms.reports.io.parser;

import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.util.TimeUtil;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonteCarloResultsParserTest {

    @Test
    void getSimulationName() {
        String projectPath = "src/test/resources/MonteCarlo";
        String resultsPath = projectPath + "/MCA_Normal.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.MONTE_CARLO)
                .build();

        assertEquals("Simple", parser.getSimulationName());
    }

    @Test
    void getAvailablePlots() {
        String projectPath = "src/test/resources/MonteCarlo";
        String resultsPath = projectPath + "/MCA_Normal.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.MONTE_CARLO)
                .build();

        assertEquals(Collections.emptyList(), parser.getAvailablePlots());
    }

    @Test
    void getStartTime() {
        String projectPath = "src/test/resources/MonteCarlo";
        String resultsPath = projectPath + "/MCA_Normal.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.MONTE_CARLO)
                .build();

        ZonedDateTime expected = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("Z"));
        assertEquals(expected, TimeUtil.toZonedDateTime(parser.getStartTime()));
    }

    @Test
    void getEndTime() {
        String projectPath = "src/test/resources/MonteCarlo";
        String resultsPath = projectPath + "/MCA_Normal.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.MONTE_CARLO)
                .build();

        ZonedDateTime expected = ZonedDateTime.of(2025, 1, 2, 12, 0, 0, 0, ZoneId.of("Z"));
        assertEquals(expected, TimeUtil.toZonedDateTime(parser.getEndTime()));
    }

    @Test
    void getLastComputedTime() {
        String projectPath = "src/test/resources/MonteCarlo";
        String resultsPath = projectPath + "/MCA_Normal.results";
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile(resultsPath)
                .pathToProjectDirectory(projectPath)
                .simulationType(SimulationType.MONTE_CARLO)
                .build();

        ZonedDateTime expectedDate = ZonedDateTime.of(2021, 4, 15, 16, 21, 37, 0, ZoneId.of("GMT"));
        assertEquals(expectedDate, parser.getLastComputedTime());
    }
}
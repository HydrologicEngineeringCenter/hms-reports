package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.util.Utilities;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HmsReportWriterTest {

    @Test
    void writeShort() {
        List<String> availablePlots = Utilities.getAvailablePlot("src/resources/Punx/RUN_Sep_2018.results");
        for(String plotName : availablePlots) {
            System.out.println(plotName);
        }

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput("src/resources/MiddleColumbia/MiddleColumbia_WY2017(mod).basin.json")
                .pathToResult("src/resources/MiddleColumbia/RUN_WY2017(mod).results")
                .pathToDestination("src/resources/output-short.html")
                .projectDirectory("C:\\Users\\q0hecntv\\Desktop\\MiddleColumbiaForNick\\MiddleColumbia")
                .build();

        reportWriter.write();
    }

    @Test
    void writeLong() {
        Map<String, List<String>> globalParameterChoices = new HashMap<>();
        globalParameterChoices.put("Subbasin", Arrays.asList("Area", "Canopy", "Loss Rate", "Transform", "Baseflow"));
        globalParameterChoices.put("Reach", Arrays.asList("Route"));

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput("src/resources/MiddleColumbia/MiddleColumbia_WY2017.basin.json")
                .pathToResult("src/resources/MiddleColumbia/RUN_WY2017.results")
                .pathToDestination("src/resources/output-long.html")
                .projectDirectory("C:\\Users\\q0hecntv\\Desktop\\MiddleColumbiaForNick\\MiddleColumbia")
                .reportSummaryChoice(Arrays.asList(ReportWriter.SummaryChoice.GLOBAL_RESULTS_SUMMARY, ReportWriter.SummaryChoice.GLOBAL_PARAMETER_SUMMARY, ReportWriter.SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .globalParameterChoices(globalParameterChoices)
                .build();

        List<Element> elementList = reportWriter.write();
    }

    @Test
    void writePunx() {
        String pathToInput = "C:\\HyperNick\\Punx\\punxsutawney.basin.json";
        String pathToResult = "C:\\HyperNick\\Punx\\results\\RUN_Sep_2018.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\hms-output.html";
        String projectDirectory = "C:\\HyperNick\\Punx";

        /* The user choosing what global parameters to hide/not print out */
        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, "global");
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, "element");

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportSummaryChoice(Arrays.asList(ReportWriter.SummaryChoice.GLOBAL_RESULTS_SUMMARY, ReportWriter.SummaryChoice.GLOBAL_PARAMETER_SUMMARY, ReportWriter.SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .elementParameterizationChoice(availableElementParameter)
                .globalParameterChoices(availableGlobalParameter)
                .build();

        List<Element> elementList = reportWriter.write();
    }
}
package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.ParameterSummary;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.Utilities;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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
        String pathToInput  = "src/resources/MiddleColumbia/MiddleColumbia_WY2017.basin.json";
        String pathToResult = "src/resources/MiddleColumbia/RUN_WY2017.results";
        String pathToOutput = "src/resources/output-long.html";
        String projectDir   = "C:\\Users\\q0hecntv\\Desktop\\MiddleColumbiaForNick\\MiddleColumbia";

        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);
        int numElements = Utilities.getNumberOfElements(pathToInput);

        List<String> availablePlots = Utilities.getAvailablePlot(pathToResult);

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToOutput)
                .projectDirectory(projectDir)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .globalParameterChoices(availableGlobalParameter)
                .elementParameterizationChoice(availableElementParameter)
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
        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);

        /* Removing Element Parameters */
//        availableElementParameter.remove("Subbasin");
//        availableElementParameter.remove("Reach");

//        availableElementParameter.get("Subbasin").remove("Loss Rate");

        /* Removing Global Parameters */
//        availableGlobalParameter.remove("Subbasin");
//        availableGlobalParameter.remove("Reach");

//        availableGlobalParameter.get("Subbasin").remove("Area");
//        availableGlobalParameter.get("Subbasin").remove("Loss Rate");
//        availableGlobalParameter.get("Subbasin").remove("Canopy");
//        availableGlobalParameter.get("Subbasin").remove("Transform");
//        availableGlobalParameter.get("Subbasin").remove("Baseflow");
//        availableGlobalParameter.get("Reach").remove("Route");

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .elementParameterizationChoice(availableElementParameter)
                .globalParameterChoices(availableGlobalParameter)
                .build();

        List<Element> elementList = reportWriter.write();
    }
}
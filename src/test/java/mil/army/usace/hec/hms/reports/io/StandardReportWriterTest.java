package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.ParameterSummary;
import mil.army.usace.hec.hms.reports.enums.ReportWriterType;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.Utilities;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class StandardReportWriterTest {

    @Test
    void writeMiddleColumbia() {
        String pathToInput  = "C:\\Users\\q0hecntv\\Desktop\\HmsReports\\MiddleColumbia_forNick\\MiddleColumbia\\MiddleColumbia_WY1997.basin";
        String pathToResult = "C:\\Users\\q0hecntv\\Desktop\\HmsReports\\MiddleColumbia_forNick\\MiddleColumbia\\results\\RUN_WY1997.results";
        String pathToOutput = "C:\\Temp\\reportTest.html";
        String projectDir   = "C:\\Users\\q0hecntv\\Desktop\\HmsReports\\MiddleColumbia_forNick\\MiddleColumbia";

        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);
        int numElements = Utilities.getNumberOfElements(pathToInput);

        List<String> availablePlots = Utilities.getAvailablePlots(pathToResult, SimulationType.RUN);

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToOutput)
                .projectDirectory(projectDir)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .globalParameterChoices(availableGlobalParameter)
                .elementParameterizationChoice(availableElementParameter)
                .reportWriterType(ReportWriterType.STANDARD_REPORT)
                .simulationType(SimulationType.RUN)
                .chosenPlots(availablePlots)
                .build();

        List<Element> elementList = reportWriter.write();
    }

    @Test
    void writePunx() {
        String pathToInput = "C:\\HyperNick\\Punx\\punxsutawney.basin";
        String pathToResult = "C:\\HyperNick\\Punx\\results\\RUN_Sep_2018.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\hms-output.html";
        String projectDirectory = "C:\\HyperNick\\Punx";

        /* The user choosing what global parameters to hide/not print out */
        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);

        List<String> availablePlots = Utilities.getAvailablePlots(pathToResult, SimulationType.RUN);

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .elementParameterizationChoice(availableElementParameter)
                .globalParameterChoices(availableGlobalParameter)
                .reportWriterType(ReportWriterType.STANDARD_REPORT)
                .simulationType(SimulationType.RUN)
                .chosenPlots(availablePlots)
                .build();

        List<Element> elementList = reportWriter.write();
    }

    @Test
    void writeForecast() {
        String pathToInput = "C:\\Projects\\hec-hms\\src\\test\\resources\\forecast\\Base.basin";
        String pathToResult = "C:\\Projects\\hec-hms\\src\\test\\resources\\forecast\\results\\FOR_05_06_Forecast.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\Report-Forecast.html";
        String projectDirectory = "C:\\Projects\\hec-hms\\src\\test\\resources\\forecast";

        /* The user choosing what global parameters to hide/not print out */
        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);

        List<String> availablePlots = Utilities.getAvailablePlots(pathToResult, SimulationType.FORECAST);

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .elementParameterizationChoice(availableElementParameter)
                .globalParameterChoices(availableGlobalParameter)
                .reportWriterType(ReportWriterType.STANDARD_REPORT)
                .simulationType(SimulationType.FORECAST)
                .chosenPlots(availablePlots)
                .build();

        List<Element> elementList = reportWriter.write();
        System.out.println("Done");
    }

    @Test
    void writeOptimization() {
        String pathToInput = "C:\\Projects\\hec-hms\\src\\test\\resources\\optimization\\Punxsutawney.basin";
        String pathToResult = "C:\\Projects\\hec-hms\\src\\test\\resources\\optimization\\results\\OPT_Opt_May_95.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\Report-Optimization.html";
        String projectDirectory = "C:\\Projects\\hec-hms\\src\\test\\resources\\optimization";

        /* The user choosing what global parameters to hide/not print out */
        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);

        List<String> availablePlots = Utilities.getAvailablePlots(pathToResult, SimulationType.OPTIMIZATION);

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .elementParameterizationChoice(availableElementParameter)
                .globalParameterChoices(availableGlobalParameter)
                .reportWriterType(ReportWriterType.STANDARD_REPORT)
                .simulationType(SimulationType.OPTIMIZATION)
                .chosenPlots(availablePlots)
                .build();

        List<Element> elementList = reportWriter.write();
        System.out.println("Done");
    }

    @Test
    void writeRun() {
        String pathToInput = "C:\\Projects\\hec-hms\\src\\test\\resources\\castro\\Castro_1.basin";
        String pathToResult = "C:\\Projects\\hec-hms\\src\\test\\resources\\castro\\results\\RUN_Current.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\Report-Run.html";
        String projectDirectory = "C:\\Projects\\hec-hms\\src\\test\\resources\\castro";

        /* The user choosing what global parameters to hide/not print out */
        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);

        List<String> availablePlots = Utilities.getAvailablePlots(pathToResult, SimulationType.RUN);

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .elementParameterizationChoice(availableElementParameter)
                .globalParameterChoices(availableGlobalParameter)
                .reportWriterType(ReportWriterType.STANDARD_REPORT)
                .simulationType(SimulationType.RUN)
                .chosenPlots(availablePlots)
                .build();

        List<Element> elementList = reportWriter.write();
        System.out.println("Done");
    }

    @Test
    void writeMonteCarlo() {
        String pathToInput = "C:\\Projects\\hec-hms\\src\\test\\resources\\montecarlo\\Simple.basin";
        String pathToResult = "C:\\Projects\\hec-hms\\src\\test\\resources\\montecarlo\\results\\MCA_Normal.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\Report-MonteCarlo.html";
        String projectDirectory = "C:\\Projects\\hec-hms\\src\\test\\resources\\montecarlo";

        /* The user choosing what global parameters to hide/not print out */
        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);

        List<String> availablePlots = Utilities.getAvailablePlots(pathToResult, SimulationType.MONTE_CARLO);

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .elementParameterizationChoice(availableElementParameter)
                .globalParameterChoices(availableGlobalParameter)
                .reportWriterType(ReportWriterType.STANDARD_REPORT)
                .simulationType(SimulationType.MONTE_CARLO)
                .chosenPlots(availablePlots)
                .build();

        List<Element> elementList = reportWriter.write();
        System.out.println("Done");
    }

    @Test
    void writeDepthArea() {
        String pathToInput = "C:\\Projects\\hec-hms\\src\\test\\resources\\deptharea\\Subset.basin";
        String pathToResult = "C:\\Projects\\hec-hms\\src\\test\\resources\\deptharea\\results\\DAA_Maricopa.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\Report-DepthArea.html";
        String projectDirectory = "C:\\Projects\\hec-hms\\src\\test\\resources\\deptharea";

        /* The user choosing what global parameters to hide/not print out */
        Map<String, List<String>> availableGlobalParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.GLOBAL_PARAMETER);
        Map<String, List<String>> availableElementParameter = Utilities.getParameterMap(pathToInput, ParameterSummary.ELEMENT_PARAMETER);

        List<String> availablePlots = Utilities.getAvailablePlots(pathToResult, SimulationType.DEPTH_AREA);

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportSummaryChoice(Arrays.asList(SummaryChoice.GLOBAL_RESULTS_SUMMARY, SummaryChoice.GLOBAL_PARAMETER_SUMMARY, SummaryChoice.ELEMENT_RESULTS_SUMMARY))
                .elementParameterizationChoice(availableElementParameter)
                .globalParameterChoices(availableGlobalParameter)
                .reportWriterType(ReportWriterType.STANDARD_REPORT)
                .simulationType(SimulationType.DEPTH_AREA)
                .chosenPlots(availablePlots)
                .build();

        List<Element> elementList = reportWriter.write();
        System.out.println("Done");
    }

}
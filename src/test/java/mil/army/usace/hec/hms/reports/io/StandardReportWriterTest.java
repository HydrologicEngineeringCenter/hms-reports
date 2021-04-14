package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.ParameterSummary;
import mil.army.usace.hec.hms.reports.enums.ReportWriterType;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.Utilities;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardReportWriterTest {

    private void deleteTempFiles(List<String> filePaths) {
        for(String path : filePaths) {
            try { Files.delete(Paths.get(path)); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    private boolean compareHtmlFiles(String expected, String actual) {
        try {
            Document expectedDoc = Jsoup.parse(new File(expected), "UTF-8");
            Document actualDoc = Jsoup.parse(new File(expected), "UTF-8");
            if(actualDoc.hasSameValue(expectedDoc))
                return true;
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    @Test
    void writeRunPunx() {
        String projectDirectory = new File(getClass().getResource("/SimulationRun/Punx").getFile()).toString();
        String pathToInput = projectDirectory + File.separator + "punxsutawney.basin";
        String pathToResult = projectDirectory + File.separator + "RUN_Sep_2018.results";
        String pathToDestination = "./actual_punx.html";

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

        reportWriter.write();

        assertTrue(compareHtmlFiles(pathToDestination, projectDirectory + File.separator + "expected_punx.html"));
//        deleteTempFiles(Arrays.asList(pathToDestination, "./styleStandard.css"));
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
    void writeRunCastro() {
        String projectDirectory = new File(getClass().getResource("/SimulationRun/Castro").getFile()).toString();
        String pathToInput = projectDirectory + File.separator + "Castro_1.basin";
        String pathToResult = projectDirectory + File.separator + "RUN_Current.results";
        String pathToDestination = "./actual_castro.html";

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

        assertTrue(compareHtmlFiles(pathToDestination, projectDirectory + File.separator + "expected_castro.html"));
        deleteTempFiles(Arrays.asList(pathToDestination, "./styleStandard.css"));
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

    @Test
    void writeUH() {
        String pathToInput = "C:\\Users\\q0hecntv\\Desktop\\user_spec_UH\\user_spec_UH\\USBR_UH.basin";
        String pathToResult = "C:\\Users\\q0hecntv\\Desktop\\user_spec_UH\\user_spec_UH\\results\\RUN_Center_Weighted_PMP_USBRUH.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\Report-UH.html";
        String projectDirectory = "C:\\Users\\q0hecntv\\Desktop\\user_spec_UH\\user_spec_UH";

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
    void writeBug() {
        String pathToInput = "C:\\Users\\Ryzen2700x\\Downloads\\CA_AVE_GDP_4469_PR_12112020\\Basins_6+7_EX.basin";
        String pathToResult = "C:\\Users\\Ryzen2700x\\Downloads\\CA_AVE_GDP_4469_PR_12112020\\results\\RUN_Basin_6_And_7.results";
        String pathToDestination = "C:\\Users\\Ryzen2700x\\Desktop\\abcde.html";
        String projectDirectory = "C:\\Users\\Ryzen2700x\\Downloads\\CA_AVE_GDP_4469_PR_12112020";

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

}
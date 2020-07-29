package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.ReportWriterType;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import org.junit.jupiter.api.Test;

import java.util.List;

class SummaryStatisticsReportWriterTest {

    @Test
    void writeShort() {
        String pathToInput = "C:\\HyperNick\\Punx\\punxsutawney.basin.json";
        String pathToResult = "C:\\HyperNick\\Punx\\results\\RUN_Sep_2018.results";
        String pathToDestination = "C:\\HyperNick\\HmsReportOutput\\statistic-report-short.html";
        String projectDirectory = "C:\\HyperNick\\Punx";

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToDestination)
                .projectDirectory(projectDirectory)
                .reportWriterType(ReportWriterType.SUMMARY_STATISTICS_REPORT)
                .build();

        List<Element> elementList = reportWriter.write();
    } // writeShort()

    @Test
    void writeLong() {
        String pathToInput  = "C:\\Users\\q0hecntv\\Desktop\\HmsReports\\MiddleColumbiaForNick\\MiddleColumbia\\MiddleColumbia_WY1997.basin";
        String pathToResult = "C:\\Users\\q0hecntv\\Desktop\\HmsReports\\MiddleColumbiaForNick\\MiddleColumbia\\results\\RUN_WY1997.results";
        String pathToOutput = "C:\\Temp\\reportTest.html";
        String projectDir   = "C:\\Users\\q0hecntv\\Desktop\\HmsReports\\MiddleColumbiaForNick\\MiddleColumbia";


        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToOutput)
                .projectDirectory(projectDir)
                .reportWriterType(ReportWriterType.SUMMARY_STATISTICS_REPORT)
                .simulationType(SimulationType.RUN)
                .build();

        List<Element> elementList = reportWriter.write();
    } // writeLong()

    @Test
    void writeForecast() {
        String pathToInput = "C:\\Projects\\hec-hms\\src\\test\\resources\\forecast\\Base.basin";
        String pathToResult = "C:\\Projects\\hec-hms\\src\\test\\resources\\forecast\\results\\FOR_05_06_Forecast.results";
        String pathToOutput = "C:\\HyperNick\\HmsReportOutput\\Statistics-Forecast.html";
        String projectDir = "C:\\Projects\\hec-hms\\src\\test\\resources\\forecast";


        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToOutput)
                .projectDirectory(projectDir)
                .reportWriterType(ReportWriterType.SUMMARY_STATISTICS_REPORT)
                .simulationType(SimulationType.FORECAST)
                .build();

        List<Element> elementList = reportWriter.write();
    } // writeLong()

} // SummaryStatisticsReportWriterTest
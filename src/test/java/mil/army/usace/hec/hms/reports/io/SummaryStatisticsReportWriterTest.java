package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.ReportWriterType;
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
        String pathToInput  = "src/resources/MiddleColumbia/MiddleColumbia_WY2017.basin.json";
        String pathToResult = "src/resources/MiddleColumbia/RUN_WY2017.results";
        String pathToOutput = "C:\\HyperNick\\HmsReportOutput\\statistic-report-long.html";
        String projectDir   = "C:\\Users\\q0hecntv\\Desktop\\MiddleColumbiaForNick\\MiddleColumbia";

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput(pathToInput)
                .pathToResult(pathToResult)
                .pathToDestination(pathToOutput)
                .projectDirectory(projectDir)
                .reportWriterType(ReportWriterType.SUMMARY_STATISTICS_REPORT)
                .build();

        List<Element> elementList = reportWriter.write();
    } // writeLong()

} // SummaryStatisticsReportWriterTest
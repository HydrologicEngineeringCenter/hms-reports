package mil.army.usace.hec.hms.reports.io;

import hec.client.Report;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.util.Utilities;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput("src/resources/MiddleColumbia_WY2017.basin.json")
                .pathToResult("src/resources/RUN_WY2017.results")
                .pathToDestination("src/resources/output-big.html")
                .build();

        reportWriter.write();
    }

    @Test
    void writePunx() {
        List<String> availablePlots = Utilities.getAvailablePlot("src/resources/Punx/RUN_Sep_2018.results");
        for(String plotName : availablePlots) {
            System.out.println(plotName);
        }

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToInput("src/resources/Punx/punxsutawney.basin.json")
                .pathToResult("src/resources/Punx/RUN_Sep_2018.results")
                .pathToDestination("src/resources/output-punx.html")
                .projectDirectory("C:\\HyperNick\\Punx")
                .chosenPlots(Arrays.asList("Air Temperature", "Aquifer Recharge", "Canopy Storage"))
                .build();

        reportWriter.write();
    }
}
package mil.army.usace.hec.hms.reports.io;

import hec.client.Report;
import mil.army.usace.hec.hms.reports.Element;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HmsReportWriterTest {

    @Test
    void write() {
        BasinParser parser = BasinParser.builder()
                .pathToBasinInputFile("src/resources/MiddleColumbia_WY2017(mod).basin.json")
                .pathToBasinResultsFile("src/resources/RUN_WY2017(mod).results")
                .build();

        List<Element> elementList = parser.getElements();
//        List<Element> elementList = new ArrayList<>();

        ReportWriter reportWriter = ReportWriter.builder()
                .pathToDestination("src/resources/output-mini.html")
                .elements(elementList)
                .build();

        reportWriter.write();
    }
}
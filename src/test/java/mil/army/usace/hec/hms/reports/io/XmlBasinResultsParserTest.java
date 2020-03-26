package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.ElementResults;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class XmlBasinResultsParserTest {

    @Test
    void getElementInput() {
        BasinResultsParser parser = BasinResultsParser.builder()
                .pathToBasinResultsFile("src/resources/RUN_WY2017.results")
                .build();

        Map<String, ElementResults> inputs = parser.getElementResults();
    }

    @Test
    void getAvailablePlot() {
        List<String> x = XmlBasinResultsParser.getAvailablePlots("C:\\Projects\\hms-reports\\src\\resources\\Punx\\RUN_Sep_2018.results");

        for(String item : x) {
            System.out.println(item);
        } // Loop
    }
}
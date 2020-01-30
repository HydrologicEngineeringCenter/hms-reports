package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementResults;
import org.junit.jupiter.api.Test;

import java.util.List;

class BasinParserTest {

    @Test
    void getElement() {
        BasinParser parser = BasinParser.builder()
                .pathToBasinInputFile("src/resources/MiddleColumbia_WY2017.basin.json")
                .pathToBasinResultsFile("src/resources/RUN_WY2017.results")
                .build();

        List<Element> elementList = parser.getElements();
        System.out.println("Done!");
    }
}
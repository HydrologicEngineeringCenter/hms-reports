package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.io.parser.BasinInputParser;
import org.junit.jupiter.api.Test;

import java.util.List;

class JsonBasinInputParserTest {

    @Test
    void getElementInput() {
        BasinInputParser parser = BasinInputParser.builder()
                .pathToBasinInputFile("src/resources/MiddleColumbia_WY2017.basin.json")
                .build();

        List<ElementInput> inputs = parser.getElementInput();
    }
}
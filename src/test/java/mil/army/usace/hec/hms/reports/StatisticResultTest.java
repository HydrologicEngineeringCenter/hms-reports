package mil.army.usace.hec.hms.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticResultTest {

    @Test
    void CreateStatisticResult(){
        StatisticResult result = StatisticResult.builder()
                .name("Outflow Minimum")
                .value("10.9546337")
                .units("CFS")
                .build();

        assertEquals("Outflow Minimum", result.getName());
        assertEquals("10.9546337", result.getValue());
        assertEquals("CFS", result.getUnits());
    }

}
package mil.army.usace.hec.hms.reports;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DisplayRangeTest {

    @Test
    void inRange() {
        DisplayRange range = new DisplayRange(0, 5, "red");
        /* GT min, LEQ to max */
        assertFalse(range.inRange(0));
        assertTrue(range.inRange(5));

        /* In Range */
        assertTrue(range.inRange(3));

        /* LEQ min, GT max */
        assertFalse(range.inRange(-1));
        assertFalse(range.inRange(6));
    }

    @Test
    void displayRangeListToString() {
        List<DisplayRange> displayRangeList = new ArrayList<>();

        DisplayRange range1 = new DisplayRange(0, 5, "red");
        DisplayRange range2 = new DisplayRange(5, 10, "green");
        DisplayRange range3 = new DisplayRange(10,15, "blue");

        displayRangeList.add(range1);
        displayRangeList.add(range2);
        displayRangeList.add(range3);

        String expected = "0.0,5.0,red|5.0,10.0,green|10.0,15.0,blue";
        String actual = DisplayRange.displayRangeListToString(displayRangeList);
        assertEquals(expected, actual);
    }

    @Test
    void stringToDisplayRangeList() {
        String rangeStr = "0.0,5.0,red|5.0,10.0,green|10.0,15.0,blue";
        List<DisplayRange> displayRangeList = DisplayRange.stringToDisplayRangeList(rangeStr);
        DisplayRange range1 = new DisplayRange(0, 5, "red");
        DisplayRange range2 = new DisplayRange(5, 10, "green");
        DisplayRange range3 = new DisplayRange(10,15, "blue");

        assertEquals(range1, displayRangeList.get(0));
        assertEquals(range2, displayRangeList.get(1));
        assertEquals(range3, displayRangeList.get(2));
    }
}
package mil.army.usace.hec.hms.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DisplayRange {
    private final double min;
    private final double max;
    private final String colorCode;
    private static final Logger logger = Logger.getLogger(DisplayRange.class.getName());

    public DisplayRange(double min, double max, String colorCode) {
        this.min = min;
        this.max = max;
        this.colorCode = colorCode;
    } // DisplayRange Constructor

    public double getMin() {
        return this.min;
    } // getMin()

    public double getMax() {
        return this.max;
    } // getMax()

    public String getColorCode() {
        return this.colorCode;
    } // getColorCode()

    public boolean inRange(double value) {
        return (value > min) && (value <= max);
    } // inRange()

    public static String displayRangeListToString(List<DisplayRange> displayRangeList) {
        StringBuilder result = new StringBuilder();
        for(DisplayRange range : displayRangeList) {
            String rangeStr = range.getMin() + "," + range.getMax() + "," + range.getColorCode();
            result.append(rangeStr);
            if(range != displayRangeList.get(displayRangeList.size() - 1))
                result.append("|");
        }
        return result.toString();
    }

    public static List<DisplayRange> stringToDisplayRangeList(String rangeStr) {
        List<DisplayRange> displayRangeList = new ArrayList<>();
        String[] displayRangeListStr = rangeStr.split("\\|");
        for (String list : displayRangeListStr) {
            String[] attributes = list.split(",");
            try {
                double min = Double.parseDouble(attributes[0]);
                double max = Double.parseDouble(attributes[1]);
                String colorCode = attributes[2];
                DisplayRange displayRange = new DisplayRange(min, max, colorCode);
                displayRangeList.add(displayRange);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }
        return displayRangeList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisplayRange that = (DisplayRange) o;
        return Double.compare(that.min, min) == 0 && Double.compare(that.max, max) == 0 && Objects.equals(colorCode, that.colorCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, colorCode);
    }
} // DisplayRange Class

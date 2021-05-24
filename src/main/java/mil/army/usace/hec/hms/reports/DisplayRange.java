package mil.army.usace.hec.hms.reports;

public class DisplayRange {
    private final double min;
    private final double max;
    private final String colorCode;

    public DisplayRange(double min, double max, String colorCode) {
        this.min = min;
        this.max = max;
        this.colorCode = colorCode;
    } // DisplayRange Constructor

    public String getColorCode() {
        return this.colorCode;
    } // getColorCode()

    public boolean inRange(double value) {
        return (value > min) && (value <= max);
    } // inRange()

} // DisplayRange Class

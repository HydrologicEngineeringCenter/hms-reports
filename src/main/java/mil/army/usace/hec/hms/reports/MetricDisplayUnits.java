package mil.army.usace.hec.hms.reports;

public class MetricDisplayUnits implements DisplayUnits {
    private String distanceUnit = "m";
    private String areaUnit = "m\u00B2";

    @Override
    public String getDistanceUnit() { return distanceUnit; }

    @Override
    public String getAreaUnit() { return areaUnit; }
}

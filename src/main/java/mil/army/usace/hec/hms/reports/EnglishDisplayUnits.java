package mil.army.usace.hec.hms.reports;

public class EnglishDisplayUnits implements DisplayUnits {
    private String distanceUnit = "ft";
    private String areaUnit = "ft\u00B2";

    @Override
    public String getDistanceUnit() { return distanceUnit; }

    @Override
    public String getAreaUnit() { return areaUnit; }
}

package mil.army.usace.hec.hms.reports;

public class MetricDisplayUnits implements DisplayUnits {

    @Override
    public String getDistanceUnit() {
        return "m"; }

    @Override
    public String getAreaUnit() {
        return "m\u00B2"; }
}

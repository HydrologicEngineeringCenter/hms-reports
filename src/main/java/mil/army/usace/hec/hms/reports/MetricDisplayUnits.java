package mil.army.usace.hec.hms.reports;

public class MetricDisplayUnits implements DisplayUnits {

    @Override
    public String getDistanceUnit() {
        return "M"; }

    @Override
    public String getAreaUnit() {
        return "KM2"; }

    @Override
    public String getSlopeUnit() {
        return "M/M";
    }
}

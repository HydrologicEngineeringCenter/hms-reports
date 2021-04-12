package mil.army.usace.hec.hms.reports;

public class EnglishDisplayUnits implements DisplayUnits {

    @Override
    public String getDistanceUnit() {
        return "ft"; }

    @Override
    public String getAreaUnit() {
        return "ft\u00B2"; }
}

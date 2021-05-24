package mil.army.usace.hec.hms.reports;

public class EnglishDisplayUnits implements DisplayUnits {

    @Override
    public String getDistanceUnit() {
        return "FT";
    }

    @Override
    public String getAreaUnit() {
        return "MI\u00B2";
    }

    @Override
    public String getSlopeUnit() {
        return "FT/FT";
    }
}

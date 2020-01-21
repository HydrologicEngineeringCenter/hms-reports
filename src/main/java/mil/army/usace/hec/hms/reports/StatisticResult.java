package mil.army.usace.hec.hms.reports;

public class StatisticResult {
    private final String name;
    private final double value;
    private final String units;

    private StatisticResult(Builder builder){
        this.name = builder.name;
        this.value = builder.value;
        this.units = builder.units;
    }


    public static class Builder{
        String name;
        double value;
        String units;

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder value(double value){
            this.value = value;
            return this;
        }

        public Builder units(String units){
            this.units = units;
            return this;
        }

        public StatisticResult build(){
            return new StatisticResult(this);
        }
    }

    public static Builder builder(){
        return new Builder();
    }


    String getName(){
        return name;
    }

    double getValue(){
        return value;
    }

    String getUnits(){
        return units;
    }
}

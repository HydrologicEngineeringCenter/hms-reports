package mil.army.usace.hec.hms.reports;

public class StatisticResult {
    /* Class Variables */
    private final String name;
    private final double value;
    private final String units;

    /* Constructors */
    private StatisticResult(Builder builder){
        this.name = builder.name;
        this.value = builder.value;
        this.units = builder.units;
    } // StatisticResult Constructor

    public static class Builder{
        String name;
        double value;
        String units;

        public Builder name(String name){
            this.name = name;
            return this;
        } // 'name' constructor

        public Builder value(double value){
            this.value = value;
            return this;
        } // 'value' constructor

        public Builder units(String units){
            this.units = units;
            return this;
        } // 'units' constructor

        public StatisticResult build(){
            return new StatisticResult(this);
        }
    } // Builder class: as StatisticResult's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Methods */
    String getName(){
        return this.name;
    }
    double getValue(){
        return this.value;
    }
    String getUnits(){
        return this.units;
    }

} // StatisticResult Class

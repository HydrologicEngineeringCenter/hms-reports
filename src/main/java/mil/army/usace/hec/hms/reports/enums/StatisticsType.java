package mil.army.usace.hec.hms.reports.enums;

public enum StatisticsType {
    NASH_SUTCLIFFE_EFFICIENCY("Nash Sutcliffe Efficiency"), RMSE_STDEV("RMSE Stdev"),
    PERCENT_BIAS("Percent Bias"), COEFFICIENT_OF_DETERMINATION("Coefficient of Determination");
    private String name;
    StatisticsType(String name) { this.name = name; } // StatisticsType's Constructor
    public String getName() { return this.name; } // getName()
} // StatisticsType enum


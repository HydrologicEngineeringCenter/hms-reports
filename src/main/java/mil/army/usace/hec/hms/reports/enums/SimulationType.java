package mil.army.usace.hec.hms.reports.enums;

public enum SimulationType {
    RUN("RunResults"),
    OPTIMIZATION("OptimizationResults"),
    FORECAST("ForecastResults"),
    MONTE_CARLO("MonteCarloAnalysisResults"),
    DEPTH_AREA("DepthAreaAnalysisResults");

    private final String name;
    SimulationType(String name) { this.name = name; } // StatisticsType's Constructor
    public String getName() { return this.name; } // getName()

} // SimulationType enum

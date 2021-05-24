package mil.army.usace.hec.hms.reports.enums;

public enum SimulationType {
    RUN("RunResults", "Simulation Run: "),
    OPTIMIZATION("OptimizationResults", "Optimization Trial: "),
    FORECAST("ForecastResults", "Forecast Alternative: "),
    MONTE_CARLO("MonteCarloAnalysisResults", "Uncertainty Analysis: "),
    DEPTH_AREA("DepthAreaAnalysisResults", "Depth Area Analysis: ");

    private final String name, title;

    SimulationType(String name, String title) {
        this.name = name;
        this.title = title;
    } // StatisticsType's Constructor

    public String getName() {
        return this.name;
    } // getName()

    public String getTitle() {
        return this.title;
    } // getTitle()
} // SimulationType enum

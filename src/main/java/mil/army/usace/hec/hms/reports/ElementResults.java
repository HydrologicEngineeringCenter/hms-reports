package mil.army.usace.hec.hms.reports;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElementResults {
    /* Class Variables */
    private final String name;
    private final List<StatisticResult> statisticResults;
    private final List<TimeSeriesResult> timeSeriesResults;
    private final Map<String, String> otherResults;

    /* Constructors */
    private ElementResults(Builder builder){
        this.name = builder.name;
        this.statisticResults = builder.statisticResults;
        this.timeSeriesResults = builder.timeSeriesResults;
        this.otherResults = builder.otherResults;
    } // ElementResults Constructor

    public static class Builder{
        String name;
        List<StatisticResult> statisticResults;
        List<TimeSeriesResult> timeSeriesResults;
        Map<String, String> otherResults;

        public Builder name(String name) {
            this.name = name;
            return this;
        } // 'name' constructor

        public Builder statisticResults(List<StatisticResult> statisticResults) {
            this.statisticResults = statisticResults;
            return this;
        } // 'statisticResults' constructor

        public Builder timeSeriesResults(List<TimeSeriesResult> timeSeriesResults) {
            this.timeSeriesResults = timeSeriesResults;
            return this;
        } // 'timeSeriesResults' constructor

        public Builder otherResults(Map<String, String> otherResults) {
            this.otherResults = otherResults;
            return this;
        } // 'otherResults' constructor

        public ElementResults build(){
            return new ElementResults(this);
        }
    } // Builder class: as ElementResults's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Methods */
    public String getName() { return this.name; }

    public List<TimeSeriesResult> getTimeSeriesResults(){ return this.timeSeriesResults; }

    public Map<String, String> getOtherResults() { return this.otherResults; }

    public Map<String, String> getStatisticResultsMap() {
        Map<String, String> statisticMap = this.statisticResults.stream().collect(Collectors.toMap(StatisticResult::getName, StatisticResult::getValue));
        return statisticMap;
    }

    public Map<String, double[]> getTimeSeriesResultsMap() {
        Map<String, double[]> timeSeriesMap = this.timeSeriesResults.stream().collect(Collectors.toMap(TimeSeriesResult::getType, TimeSeriesResult::getValues));
        return timeSeriesMap;
    }

    public StatisticResult getStatisticSResult(String statisticsName) {
        StatisticResult naStat = StatisticResult.builder().name("").value("Not specified").units("").build();
        return statisticResults.stream().filter(e -> e.getName().equalsIgnoreCase(statisticsName)).findFirst().orElse(naStat);
    }

} // ElementResults Class

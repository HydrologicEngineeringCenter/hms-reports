package mil.army.usace.hec.hms.reports;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElementResults {
    /* Class Variables */
    private final String name;
    private final List<StatisticResult> statisticResults;
    private final List<TimeSeriesResult> timeSeriesResults;
    private final Map<String, String> drainageArea;

    /* Constructors */
    private ElementResults(Builder builder){
        this.name = builder.name;
        this.statisticResults = builder.statisticResults;
        this.timeSeriesResults = builder.timeSeriesResults;
        this.drainageArea = builder.drainageArea;
    } // ElementResults Constructor

    public static class Builder{
        String name;
        List<StatisticResult> statisticResults;
        List<TimeSeriesResult> timeSeriesResults;
        Map<String, String> drainageArea;

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

        public Builder drainageArea(Map<String, String> drainageArea) {
            this.drainageArea = drainageArea;
            return this;
        } // 'drainageArea' constructor

        public ElementResults build(){
            return new ElementResults(this);
        }
    } // Builder class: as ElementResults's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Methods */
    public String getName() { return this.name; }
    public List<StatisticResult> getStatisticResults(){
        return this.statisticResults;
    }
    public List<TimeSeriesResult> getTimeSeriesResults(){
        return this.timeSeriesResults;
    }
    public Map<String, String> getDrainageArea() {
        return this.drainageArea;
    }
    public Map<String, String> getStatisticResultsMap() {
        Map<String, String> statisticMap = this.statisticResults.stream().collect(Collectors.toMap(StatisticResult::getName, StatisticResult::getValue));
        return statisticMap;
    }

} // ElementResults Class

package mil.army.usace.hec.hms.reports;

import java.util.List;

public class ElementResults {
    /* Class Variables */
    private final List<StatisticResult> statisticResults;
    private final List<TimeSeriesResult> timeSeriesResults;

    /* Constructors */
    private ElementResults(Builder builder){
        this.statisticResults = builder.statisticResults;
        this.timeSeriesResults = builder.timeSeriesResults;
    } // ElementResults Constructor

    public static class Builder{
        List<StatisticResult> statisticResults;
        List<TimeSeriesResult> timeSeriesResults;

        public Builder statisticResults(List<StatisticResult> statisticResults) {
            this.statisticResults = statisticResults;
            return this;
        } // 'statisticResults' constructor

        public Builder timeSeriesResults(List<TimeSeriesResult> timeSeriesResults) {
            this.timeSeriesResults = timeSeriesResults;
            return this;
        } // 'timeSeriesResults' constructor

        public ElementResults build(){
            return new ElementResults(this);
        }
    } // Builder class: as ElementResults's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Methods */
    public List<StatisticResult> getStatisticResults(){
        return this.statisticResults;
    }
    public List<TimeSeriesResult> getTimeSeriesResults(){
        return this.timeSeriesResults;
    }

} // ElementResults Class

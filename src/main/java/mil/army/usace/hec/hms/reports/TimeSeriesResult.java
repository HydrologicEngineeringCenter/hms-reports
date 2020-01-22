package mil.army.usace.hec.hms.reports;

import java.time.ZonedDateTime;

public class TimeSeriesResult {
    /* Class Variables */
    private final ZonedDateTime[] times;
    private final double[] values;

    /* Constructors */
    private TimeSeriesResult(Builder builder) {
        this.times = builder.times;
        this.values = builder.values;
    } // TimesSeriesResult Constructor

    public static class Builder {
        ZonedDateTime[] times;
        double[] values;

        public Builder times(ZonedDateTime[] times) {
            this.times = times;
            return this;
        } // 'times' constructor

        public Builder values(double[] values) {
            this.values = values;
            return this;
        } // 'values' constructor

        public TimeSeriesResult build(){ return new TimeSeriesResult(this); }
    } // Builder class: as TimeSeriesResult's Constructor

    public static Builder builder() {return new Builder();}

    /* Methods */
    public ZonedDateTime[] getTimes() { return times; }
    public double[] getValues() { return values; }

} // TimeSeriesResult Class

package mil.army.usace.hec.hms.reports;

import hec.heclib.dss.HecTimeSeries;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesContainer;
import mil.army.usace.hec.hms.reports.util.TimeConverter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeSeriesResult {
    private static final Logger logger = Logger.getLogger(TimeSeriesResult.class.getName());

    private String type;
    private String pathToFile;
    private String variable;
    private HecTime startTime;
    private HecTime endTime;

    private volatile double[] values;
    private volatile List<ZonedDateTime> times;
    private volatile String unit;
    private volatile String unitType;

    /* Constructors */
    private TimeSeriesResult(Builder builder) {
        this.type = builder.type;
        this.pathToFile = builder.pathToFile;
        this.variable = builder.variable;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
    } // TimesSeriesResult Constructor
    public static class Builder {
        String type;
        String pathToFile;
        String variable;
        HecTime startTime;
        HecTime endTime;

        // TimeSeries Type
        public Builder type(String type) {
            this.type = type;
            return this;
        } // 'type' constructor

        // Path to DSS file
        public Builder pathToFile(String pathToFile) {
            this.pathToFile = pathToFile;
            return this;
        } // 'pathToFile' constructor

        // DSS internal path
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        } // 'variable' constructor

        // Start Time and End Time
        public Builder startTime(HecTime startTime) {
            this.startTime = startTime;
            return this;
        } // 'startTime' constructor

        public Builder endTime(HecTime endTime) {
            this.endTime = endTime;
            return this;
        } // 'endTime' constructor

        public TimeSeriesResult build(){ return new TimeSeriesResult(this); }
    } // Builder class: as TimeSeriesResult's Constructor
    public static Builder builder() {return new Builder();}

    /* Helper Functions */
    private void readData() {
        // Initialize empty lists and arrays (Returns empty 'times' and 'values' if failed DSS read */
        times = Collections.emptyList();
        values = new double[]{};

        // Reading from DSS file
        /* Using HEC DSS to read in Time Series Container */
        TimeSeriesContainer container = new TimeSeriesContainer();
        container.setFullName(variable);
        container.setStartTime(startTime);
        container.setEndTime(endTime);
        HecTimeSeries dssTimeSeriesRead = new HecTimeSeries();
        dssTimeSeriesRead.setDSSFileName(pathToFile);

        // Reading in Data and populating this private variables (times, values)
        int operationStatus = dssTimeSeriesRead.read(container, true);
        if(operationStatus == 0) {
            values = container.getValues();
            times  = getZonedDateTimeArray(container.getTimes());
            unit = container.getUnits();
            unitType = variable.split("/")[3];
        } // If: Successful DSS Read
        else {
            logger.log(Level.WARNING, String.format("%s: Not Found", variable));
        } // Else: Failed DSS Read

        // Done with DSS TimeSeriesRead
        dssTimeSeriesRead.done();
    } // readData()
    private List<ZonedDateTime> getZonedDateTimeArray (HecTimeArray timeArray) {
        List<ZonedDateTime> zonedDateTimeArray = new ArrayList<>();
        for(int i = 0; i < timeArray.numberElements(); i++) {
            HecTime singleTime = timeArray.element(i);
            ZonedDateTime zonedTime = TimeConverter.toZonedDateTime(singleTime);
            zonedDateTimeArray.add(zonedTime);
        } // Loop: through HecTimeArray

        return zonedDateTimeArray;
    } // getZonedDateTimeArray()

    /* Public Methods */
    public String getType() { return type; }
    public List<ZonedDateTime> getTimes() {
        List<ZonedDateTime> result = times;

        // First Check: If found, then return
        if(result != null) { return result; }
        // Second Check: If not found, then readData (with Locking) and return
        synchronized(this) {
            if(times == null) { readData(); }
            return times;
        } // Synchronize to lock access while reading in data

    } // getTimes()
    public double[] getValues() {
        double[] result = values;

        // First Check: If found, then return
        if(result != null) { return result; }
        // Second Check: If not found, then readData (with Locking) and return
        synchronized(this) {
            if(values == null) { readData(); }
            return values;
        } // Synchronize to lock access while reading in data

    } // getValues()
    public String getUnitType() { return unitType; }
    public String getUnit() { return unit; }
    public TimeSeriesResult getTimeSeriesResult() { return this; }

} // TimeSeriesResult Class

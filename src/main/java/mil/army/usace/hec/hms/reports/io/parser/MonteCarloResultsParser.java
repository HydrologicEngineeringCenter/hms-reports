package mil.army.usace.hec.hms.reports.io.parser;

import hec.heclib.dss.HecTimeSeries;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesContainer;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.util.TimeConverter;
import mil.army.usace.hec.hms.reports.util.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonteCarloResultsParser extends BasinResultsParser {
    private HecTime startTime;
    private HecTime endTime;

    MonteCarloResultsParser(Builder builder) {
        super(builder);
    }

    @Override
    public Map<String,ElementResults> getElementResults() {
        Map<String, ElementResults> elementResultsList = new HashMap<>();
        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        JSONObject analysisObject = runResults.getJSONObject("Analysis");
        String startTimeString = analysisObject.optString("StartTime") + " GMT";
        String endTimeString   = analysisObject.optString("EndTime") + " GMT";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm z");
        this.startTime = TimeConverter.toHecTime(ZonedDateTime.parse(startTimeString, formatter));
        this.endTime   = TimeConverter.toHecTime(ZonedDateTime.parse(endTimeString, formatter));

        JSONArray analysisPointArray = runResults.optJSONArray("AnalysisPoint");
        JSONObject analysisPointObject = runResults.optJSONObject("AnalysisPoint");
        if(analysisPointArray == null && analysisPointObject == null) { throw new IllegalArgumentException("Analysis Point(s) Not Found"); }

        if(analysisPointArray != null) {
            for(int i = 0; i < analysisPointArray.length(); i++) {
                JSONObject elementObject = analysisPointArray.optJSONObject(i);
                if(elementObject == null) throw new IllegalArgumentException(i + ": Not an JSONObject");
                ElementResults elementResults = populateElement(elementObject);
                elementResultsList.put(elementResults.getName(), elementResults);
            } // Loop: through analysisPointArray
        } // If: More than one Analysis Points
        else {
            ElementResults elementResults = populateElement(analysisPointObject);
            elementResultsList.put(elementResults.getName(), elementResults);
        } // Else: Only one Analysis Point

        return elementResultsList;
    } // getElementResults()

    @Override
    public String getSimulationName() {
        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        JSONObject analysisObject = runResults.getJSONObject("Analysis");
        return analysisObject.opt("BasinModel").toString();
    } // getSimulationName()

    private ElementResults populateElement(JSONObject elementObject) {
        /* Name */
        String name = elementObject.opt("name").toString();

        /* Time Series */
        JSONArray timeSeriesArray = elementObject.optJSONArray("TimeSeries");
        JSONObject timeSeriesObj  = elementObject.optJSONObject("TimeSeries");
        if(timeSeriesArray == null && timeSeriesObj == null) throw new IllegalArgumentException("TimeSeries Not Found");

        /* Statistics */
        JSONArray statisticsArray = elementObject.optJSONArray("Statistic");
        JSONObject statisticsObj  = elementObject.optJSONObject("Statistic");
        if(statisticsArray == null && statisticsObj == null) throw new IllegalArgumentException("Statistic Not Found");

        List<TimeSeriesResult> timeSeriesResults = new ArrayList<>();
        if(timeSeriesArray != null) {
            for(int i = 0; i < timeSeriesArray.length(); i++) {
                JSONObject timeObject = timeSeriesArray.optJSONObject(i);
                if(timeObject == null) throw new IllegalArgumentException(name + " (TimeSeries index: " + i + " - is not object");
                TimeSeriesResult timeSeries = populateTimeSeriesResult(timeObject);
                if(timeSeries != null) { timeSeriesResults.add(timeSeries); }
            } // Loop: through timeSeriesArray
        } // If: More than one TimeSeries
        else {
            TimeSeriesResult timeSeries = populateTimeSeriesResult(timeSeriesObj);
            if(timeSeries != null) { timeSeriesResults.add(timeSeries); }
        } // Else: Only one TimeSeries

        ElementResults elementResults = ElementResults.builder()
                .name(name)
                .timeSeriesResults(timeSeriesResults)
                .statisticResults(new ArrayList<>())
                .otherResults(new HashMap<>())
                .build();

        return elementResults;
    } // populateElement()

    private TimeSeriesResult populateTimeSeriesResult(JSONObject timeObject) {
        String DssFileName = timeObject.getString("DssFileName");
        String pathToDss = Utilities.getFilePath(this.pathToProjectDirectory.toAbsolutePath().toString(), DssFileName);

        /* Read in TimeSeriesType */
        String type = timeObject.getJSONObject("TimeSeriesType").getString("displayString");

        /* Using HEC DSS to read in Time Series Container */
        TimeSeriesContainer container = new TimeSeriesContainer();
        container.setFullName(timeObject.getString("DssPathname"));
        container.setStartTime(this.startTime);
        container.setEndTime(this.endTime);
        HecTimeSeries dssTimeSeriesRead = new HecTimeSeries();
        dssTimeSeriesRead.setDSSFileName(pathToDss);

        int operationStatus = dssTimeSeriesRead.read(container, true);
        if(operationStatus != 0) {
            System.out.println(timeObject.getString("DssPathname") + " is Not Found");
            return null;
        } // If: Failed to read, return null
        dssTimeSeriesRead.done();

        double[] values = container.getValues();
        List<ZonedDateTime> times  = getZonedDateTimeArray(container.getTimes());
        String unit = container.getUnits();
        String fullName = container.getFullName();
        String unitType = fullName.split("/")[3];

        TimeSeriesResult timeSeriesResult = TimeSeriesResult.builder()
                .type(type)
                .unitType(unitType)
                .unit(unit)
                .times(times)
                .values(values)
                .build();

        return timeSeriesResult;
    } // populateTimeSeriesResult()

    private List<ZonedDateTime> getZonedDateTimeArray (HecTimeArray timeArray) {
        List<ZonedDateTime> zonedDateTimeArray = new ArrayList<>();
        for(int i = 0; i < timeArray.numberElements(); i++) {
            HecTime singleTime = timeArray.element(i);
            ZonedDateTime zonedTime = TimeConverter.toZonedDateTime(singleTime);
            zonedDateTimeArray.add(zonedTime);
        } // Loop: through HecTimeArray

        return zonedDateTimeArray;
    } // getZonedDateTimeArray()

} // MonteCarloResultsParser Class

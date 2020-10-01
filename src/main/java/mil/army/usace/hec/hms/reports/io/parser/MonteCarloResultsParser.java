package mil.army.usace.hec.hms.reports.io.parser;

import hec.heclib.util.HecTime;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.util.TimeConverter;
import mil.army.usace.hec.hms.reports.util.Utilities;
import mil.army.usace.hec.hms.reports.util.ValidCheck;
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
    private JSONObject simulationResults;
    private ZonedDateTime computedTime;

    MonteCarloResultsParser(Builder builder) {
        super(builder);

        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        JSONObject analysisObject = runResults.getJSONObject("Analysis");
        String startTimeString = analysisObject.optString("StartTime") + " GMT";
        String endTimeString   = analysisObject.optString("EndTime") + " GMT";
        String executionTime   = runResults.optString("ExecutionTime") + " GMT";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm z");
        DateTimeFormatter executionFormatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm:ss z");
        this.startTime = TimeConverter.toHecTime(ZonedDateTime.parse(startTimeString, formatter));
        this.endTime   = TimeConverter.toHecTime(ZonedDateTime.parse(endTimeString, formatter));
        this.computedTime = ZonedDateTime.parse(executionTime, executionFormatter);
        this.simulationResults = runResults;
    } // MonteCarloResultsParser Constructor

    @Override
    public Map<String,ElementResults> getElementResults() {
        Map<String, ElementResults> elementResultsList = new HashMap<>();

        JSONArray analysisPointArray = simulationResults.optJSONArray("AnalysisPoint");
        JSONObject analysisPointObject = simulationResults.optJSONObject("AnalysisPoint");
        if(analysisPointArray == null && analysisPointObject == null) { throw new IllegalArgumentException("Analysis Point(s) Not Found"); }

        if(analysisPointArray != null) {
            for(int i = 0; i < analysisPointArray.length(); i++) {
                JSONObject elementObject = analysisPointArray.optJSONObject(i);
                if(elementObject == null) throw new IllegalArgumentException(i + ": Not an JSONObject");
                ElementResults elementResults = populateElement(elementObject);
                elementResultsList.put(elementResults.getName(), elementResults);
                Double progressValue = ((double) i + 1) / analysisPointArray.length();
                support.firePropertyChange("Progress", "", progressValue);
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
        JSONObject analysisObject = simulationResults.getJSONObject("Analysis");
        return analysisObject.opt("BasinModel").toString();
    } // getSimulationName()

    @Override
    public List<String> getAvailablePlots() {
        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());

        JSONArray analysisPointArray = runResults.optJSONArray("AnalysisPoint");
        JSONObject analysisPointObject = runResults.optJSONObject("AnalysisPoint");
        if(analysisPointArray == null && analysisPointObject == null) { throw new IllegalArgumentException("Analysis Point(s) Not Found"); }

        List<String> availablePlots = new ArrayList<>();
        if(analysisPointArray != null) {
            for(int i = 0; i < analysisPointArray.length(); i++) {
                JSONObject elementObject = analysisPointArray.optJSONObject(i);
                if(elementObject == null) throw new IllegalArgumentException(i + ": Not an JSONObject");
                availablePlots = new ArrayList<>(getElementAvailablePlots(elementObject, availablePlots));
            } // Loop: through analysisPointArray
        } // If: More than one Analysis Points
        else {
            availablePlots = new ArrayList<>(getElementAvailablePlots(analysisPointObject, availablePlots));
        } // Else: Only one Analysis Point

        return availablePlots;
    } // getAvailablePlots()

    @Override
    public HecTime getStartTime() {
        return this.startTime;
    } // getStartTime()

    @Override
    public HecTime getEndTime() {
        return this.endTime;
    } // getEndTime()

    @Override
    public ZonedDateTime getLastComputedTime() {
        return this.computedTime;
    } // getLastComputedTime()

    private List<String> getElementAvailablePlots(JSONObject elementObject, List<String> availablePlots) {
        /* Name & Available Plots*/
        String name = elementObject.opt("name").toString();
        List<String> elementPlots = new ArrayList<>(availablePlots);

        /* Time Series */
        JSONArray timeSeriesArray = elementObject.optJSONArray("TimeSeries");
        JSONObject timeSeriesObj  = elementObject.optJSONObject("TimeSeries");
        if(timeSeriesArray == null && timeSeriesObj == null) throw new IllegalArgumentException("TimeSeries Not Found");

        /* Get Available Plots */
        if(timeSeriesArray != null) {
            for(int i = 0; i < timeSeriesArray.length(); i++) {
                JSONObject timeObject = timeSeriesArray.optJSONObject(i);
                if(timeObject == null) throw new IllegalArgumentException(name + " (TimeSeries index: " + i + " - is not object");
                String timeSeriesType = timeObject.getJSONObject("TimeSeriesType").getString("displayString");
                if(!elementPlots.contains(timeSeriesType) && !ValidCheck.validTimeSeriesPlot(timeSeriesType, null)) {
                    elementPlots.add(timeSeriesType);
                } // If: plot hasn't been added and plot is not a default plot; then add plot
            } // Loop: through timeSeriesArray
        } // If: More than one TimeSeries
        else {
            String timeSeriesType = timeSeriesObj.getJSONObject("TimeSeriesType").getString("displayString");
            if(!elementPlots.contains(timeSeriesType) && !ValidCheck.validTimeSeriesPlot(timeSeriesType, null)) {
                elementPlots.add(timeSeriesType);
            } // If: plot hasn't been added and plot is not a default plot; then add plot
        } // Else: Only one TimeSeries

        return elementPlots;
    } // getElementAvailablePlots()

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
        /* Getting data necessary to read in data for TimeSeriesResult */
        String DssFileName = timeObject.getString("DssFileName");
        String pathToDss = Utilities.getFilePath(this.pathToProjectDirectory.toAbsolutePath().toString(), DssFileName);
        String variable = timeObject.getString("DssPathname");
        String type = timeObject.getJSONObject("TimeSeriesType").getString("displayString");

        /* Building TimeSeriesResult */
        TimeSeriesResult timeSeriesResult = TimeSeriesResult.builder()
                .type(type)
                .pathToFile(pathToDss)
                .variable(variable)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .build();
        return timeSeriesResult;
    } // populateTimeSeriesResult()
} // MonteCarloResultsParser Class

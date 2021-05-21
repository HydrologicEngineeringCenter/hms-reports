package mil.army.usace.hec.hms.reports.io.parser;

import hec.heclib.util.HecTime;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.util.TimeUtil;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonteCarloResultsParser extends BasinResultsParser {
    private final HecTime startTime;
    private final HecTime endTime;
    private final JSONObject simulationResults;
    private final ZonedDateTime computedTime;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    MonteCarloResultsParser(Builder builder) {
        super(builder);

        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        JSONObject analysisObject = runResults.getJSONObject("Analysis");
        String startTimeString = analysisObject.optString("StartTime") + " GMT";
        String endTimeString   = analysisObject.optString("EndTime") + " GMT";
        String executionTime   = analysisObject.optString("ExecutionTime") + " GMT";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm z");
        DateTimeFormatter executionFormatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm:ss z");
        this.startTime = TimeUtil.toHecTime(ZonedDateTime.parse(startTimeString, formatter));
        this.endTime   = TimeUtil.toHecTime(ZonedDateTime.parse(endTimeString, formatter));
        this.computedTime = ZonedDateTime.parse(executionTime, executionFormatter);
        this.simulationResults = runResults;
    } // MonteCarloResultsParser Constructor

    @Override
    public Map<String,ElementResults> getElementResults() {
        Map<String, ElementResults> elementResultsList = new HashMap<>();

        JSONArray analysisPointArray = getAnalysisPointArray();
        if(analysisPointArray == null) return null;

        for(int i = 0; i < analysisPointArray.length(); i++) {
            JSONObject elementObject = analysisPointArray.optJSONObject(i);
            if(elementObject == null) {
                logger.log(Level.SEVERE, i + ": Not a JSONObject");
                return null;
            }

            ElementResults elementResults = populateElement(elementObject);
            elementResultsList.put(elementResults.getName(), elementResults);
            Double progressValue = ((double) i + 1) / analysisPointArray.length();
            support.firePropertyChange("Progress", "", progressValue);
        } // Loop: through analysisPointArray

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

        List<String> availablePlots = new ArrayList<>();
        if(analysisPointArray != null) {
            for(int i = 0; i < analysisPointArray.length(); i++) {
                JSONObject elementObject = analysisPointArray.optJSONObject(i);
                if(elementObject == null) {
                    logger.log(Level.SEVERE, i + ": Not a JSONObject");
                    return null;
                }
                availablePlots = new ArrayList<>(getElementAvailablePlots(elementObject, availablePlots));
            } // Loop: through analysisPointArray
        } // If: More than one Analysis Points
        else if(analysisPointObject != null){
            availablePlots = new ArrayList<>(getElementAvailablePlots(analysisPointObject, availablePlots));
        } // Else: Only one Analysis Point
        else {
            logger.log(Level.WARNING, "Analysis Point(s) Not Found");
            return null;
        }

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

    private JSONArray getAnalysisPointArray() {
        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());

        JSONArray analysisPointArray = runResults.optJSONArray("AnalysisPoint");
        JSONObject analysisPointObject = runResults.optJSONObject("AnalysisPoint");

        if(analysisPointArray != null)
            return analysisPointArray;
        else if(analysisPointObject != null)
            return new JSONArray(analysisPointObject);
        else
            logger.log(Level.WARNING, "No Analysis Point(s) found");

        return null;
    } // getAnalysisPointArray()

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

        return ElementResults.builder()
                .name(name)
                .timeSeriesResults(timeSeriesResults)
                .statisticResults(new ArrayList<>())
                .otherResults(new HashMap<>())
                .build();
    } // populateElement()

    private TimeSeriesResult populateTimeSeriesResult(JSONObject timeObject) {
        /* Getting data necessary to read in data for TimeSeriesResult */
        String DssFileName = timeObject.getString("DssFileName");
        String pathToDss = Utilities.getFilePath(this.pathToProjectDirectory.toAbsolutePath().toString(), DssFileName);
        String variable = timeObject.getString("DssPathname");
        String type = timeObject.getJSONObject("TimeSeriesType").getString("displayString");

        /* Building TimeSeriesResult */
        return TimeSeriesResult.builder()
                .type(type)
                .pathToFile(pathToDss)
                .variable(variable)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .build();
    } // populateTimeSeriesResult()
} // MonteCarloResultsParser Class

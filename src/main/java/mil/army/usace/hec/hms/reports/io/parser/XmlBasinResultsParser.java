package mil.army.usace.hec.hms.reports.io.parser;

import hec.heclib.util.HecTime;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.StatisticResult;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.util.StringUtil;
import mil.army.usace.hec.hms.reports.util.TimeUtil;
import mil.army.usace.hec.hms.reports.util.Utilities;
import mil.army.usace.hec.hms.reports.util.ValidCheck;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class XmlBasinResultsParser extends BasinResultsParser {
    private HecTime startTime;
    private HecTime endTime;
    private JSONObject simulationResults;
    private ZonedDateTime computedTime;

    XmlBasinResultsParser(Builder builder) {
        super(builder);

        JSONObject resultFile  = getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        String startTimeString = runResults.optString("StartTime") + " GMT";
        String endTimeString   = runResults.optString("EndTime") + " GMT";
        String executionTime   = runResults.optString("ExecutionTime") + " GMT";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dMMMyyyy, HH:mm z");
        DateTimeFormatter executionFormatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm:ss z");
        this.startTime = TimeUtil.toHecTime(ZonedDateTime.parse(startTimeString, formatter));
        this.endTime   = TimeUtil.toHecTime(ZonedDateTime.parse(endTimeString, formatter));
        this.computedTime = ZonedDateTime.parse(executionTime, executionFormatter);
        this.simulationResults = runResults;
    } // XMLBasinResultsParser Constructor

    @Override
    public Map<String,ElementResults> getElementResults() {
        Map<String, ElementResults> elementResultsList = new HashMap<>();

        JSONArray elementArray = simulationResults.optJSONArray("BasinElement");
        JSONObject elementObject = simulationResults.optJSONObject("BasinElement");
        if(elementArray == null && elementObject == null) { throw new IllegalArgumentException("No Elements Found"); }

        if(elementArray != null) {
            for(int i = 0; i < elementArray.length(); i++) {
                ElementResults elementResults = populateElement(elementArray.getJSONObject(i));
                elementResultsList.put(elementResults.getName(), elementResults);
                Double progressValue = ((double) i + 1) / elementArray.length();
                support.firePropertyChange("Progress", "", progressValue);
            } // Loop through all element's results, and populate
        } // If: has more than one element (is an ElementArray)
        else {
            ElementResults elementResults = populateElement(elementObject);
            elementResultsList.put(elementResults.getName(), elementResults);
        } // Else: has only one element (is an ElementObject)

        return elementResultsList;
    } // getElementResults()

    @Override
    public String getSimulationName() {
        String simulationName = "";

        if(simulationType == SimulationType.FORECAST) { simulationName = simulationResults.opt("ForecastName").toString(); }
        else if(simulationType == SimulationType.RUN) { simulationName = simulationResults.opt("RunName").toString(); }
        else if(simulationType == SimulationType.OPTIMIZATION) { simulationName = simulationResults.opt("AnalysisName").toString(); }

        return simulationName;
    } // getSimulationName()

    @Override
    public List<String> getAvailablePlots() {
        List<String> availablePlots = new ArrayList<>();
        JSONObject resultFile = getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());

        JSONArray elementArray = runResults.optJSONArray("BasinElement");
        JSONObject elementObject = runResults.optJSONObject("BasinElement");
        if(elementArray == null && elementObject == null) { throw new IllegalArgumentException("No Elements Found"); }

        if(elementArray != null) {
            for(int i = 0; i < elementArray.length(); i++) {
                JSONObject elementObj = elementArray.optJSONObject(i);
                availablePlots = new ArrayList<>(getElementAvailablePlots(elementObj, availablePlots));
            } // Loop through all element's results, and populate
        } // If: has more than one element (is an ElementArray)
        else {
            availablePlots = new ArrayList<>(getElementAvailablePlots(elementObject, availablePlots));
        } // Else: has only one element (is an ElementObject)

        // Sort the list Alphabetically
        Collections.sort(availablePlots);

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
        List<String> elementPlots = new ArrayList<>(availablePlots);
        if(elementObject == null) return elementPlots;

        JSONObject hydrologyObject = elementObject.optJSONObject("Hydrology");
        JSONArray timeSeriesArray = hydrologyObject.optJSONArray("TimeSeries");
        if(timeSeriesArray == null) {
            JSONObject timeSeriesObject = hydrologyObject.optJSONObject("TimeSeries");
            JSONObject timeSeriesTypeObject = timeSeriesObject.optJSONObject("TimeSeriesType");
            String timeSeriesType = timeSeriesTypeObject.optString("displayString");

            if(!elementPlots.contains(timeSeriesType) && !ValidCheck.validTimeSeriesPlot(timeSeriesType, null)) {
                elementPlots.add(timeSeriesType);
            } // If: plot hasn't been added and plot is not a default plot; then add plot
        } // If: Only one type of TimeSeries Plot
        else {
            for(int j = 0; j < timeSeriesArray.length(); j++) {
                JSONObject timeSeriesObject = timeSeriesArray.optJSONObject(j);
                JSONObject timeSeriesTypeObject = timeSeriesObject.optJSONObject("TimeSeriesType");
                String timeSeriesType = timeSeriesTypeObject.optString("displayString");

                if(!elementPlots.contains(timeSeriesType) && !ValidCheck.validTimeSeriesPlot(timeSeriesType, null)) {
                    elementPlots.add(timeSeriesType);
                } // If: plot hasn't been added and plot is not a default plot; then add plot
            } // Loop through all the timeSeriesArray
        } // Else: More than one type of TimeSeries Plots
        return elementPlots;
    } // getElementAvailablePlots()

    static JSONObject getJsonObject(String pathToJson) {
        /* Read in XML File */
        File file = new File(pathToJson);
        /* Read XML's content to 'content' */
        String content = StringUtil.readFileToString(file);
        JSONObject object = XML.toJSONObject(content);

        return object;
    } // getJsonObject()

    private ElementResults populateElement(JSONObject elementObject) {
        String name = elementObject.opt("name").toString();
        JSONObject hydrologyObject = elementObject.getJSONObject("Hydrology");
        List<TimeSeriesResult> timeSeriesResult = populateTimeSeriesResult(hydrologyObject);
        JSONObject statisticsArray = elementObject.getJSONObject("Statistics");
        List<StatisticResult> statisticResults = populateStatisticsResult(statisticsArray);
        JSONObject drainageArea = elementObject.getJSONObject("DrainageArea");
        Map<String, String> otherMap = new HashMap<>();
        otherMap.put("DrainageArea", drainageArea.opt("area").toString());
        otherMap.put("DrainageAreaUnits", drainageArea.opt("units").toString());
        JSONObject observedFlowGage = elementObject.optJSONObject("ObservedFlowGage");
        if(observedFlowGage != null) {
            otherMap.put("ObservedFlowGage", observedFlowGage.opt("name").toString());
        } // If: observedFlowGage exists
        JSONArray observedPoolGage = elementObject.optJSONArray("ObservedPoolElevationGage");
        if(observedPoolGage != null) {
            String observedPoolGageName = observedPoolGage.optJSONObject(0).optString("name");
            otherMap.put("ObservedPoolElevationGage", observedPoolGageName);
        } // If: observedFlowGage exists

        ElementResults elementResults = ElementResults.builder()
                .name(name)
                .timeSeriesResults(timeSeriesResult)
                .statisticResults(statisticResults)
                .otherResults(otherMap)
                .build();

        return elementResults;
    } // populateElement()

    private List<TimeSeriesResult> populateTimeSeriesResult(JSONObject hydrologyObject) {
        List<TimeSeriesResult> timeSeriesResultList = new ArrayList<>();
        String keyName = "TimeSeries";

        if(hydrologyObject.optJSONArray(keyName) != null) {
            JSONArray timeArray = hydrologyObject.getJSONArray(keyName);
            for(int i = 0; i < timeArray.length(); i++) {
                JSONObject timeObject = timeArray.getJSONObject(i);
                TimeSeriesResult timeSeriesResult = populateSingleTimeSeriesResult(timeObject);
                if(timeSeriesResult != null) { timeSeriesResultList.add(timeSeriesResult); }
            } // Loop: through timeArray to populate timeSeriesResultList
        } // If: is JSONArray
        else if(hydrologyObject.optJSONObject(keyName) != null) {
            JSONObject timeObject = hydrologyObject.getJSONObject(keyName);
            TimeSeriesResult timeSeriesResult = populateSingleTimeSeriesResult(timeObject);
            if(timeSeriesResult != null) { timeSeriesResultList.add(timeSeriesResult); }
        } // Else if: is JSONObject
        else {
            System.out.println("Invalid object");
        } // Else: invalid object

        return timeSeriesResultList;
    } // populateTimeSeriesResult()

    private TimeSeriesResult populateSingleTimeSeriesResult (JSONObject timeObject) {
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
    } // populateSingleTimeSeriesResult()

    private List<StatisticResult> populateStatisticsResult(JSONObject statisticsObject) {
        List<StatisticResult> statisticResultList = new ArrayList<>();
        String keyName = "StatisticMeasure";

        if(statisticsObject.optJSONArray(keyName) != null) {
            JSONArray statisticsArray = statisticsObject.getJSONArray(keyName);
            for(int i = 0; i < statisticsArray.length(); i++) {
                JSONObject singleObject = statisticsArray.getJSONObject(i);
                String name = singleObject.getString("displayString");
                String value = singleObject.opt("value").toString();
                String units = singleObject.optString("units");

                StatisticResult statisticResult = StatisticResult.builder()
                        .name(name)
                        .value(value)
                        .units(units)
                        .build();

                statisticResultList.add(statisticResult);
            } // Loop through statisticsArray
        } // If: is JSONArray
        else if(statisticsObject.optJSONObject(keyName) != null) {
            JSONObject singleObject = statisticsObject.getJSONObject(keyName);
            String name = singleObject.getString("displayString");
            String value = singleObject.opt("value").toString();
            String units = singleObject.optString("units");

            StatisticResult statisticResult = StatisticResult.builder()
                    .name(name)
                    .value(value)
                    .units(units)
                    .build();

            statisticResultList.add(statisticResult);
        } // Else if: is JSONObject
        else {
            System.out.println("Invalid object");
        } // Else: Invalid object

        return statisticResultList;
    } // populateStatisticsResult()

} // XmlBasinResultsParser class

package mil.army.usace.hec.hms.reports.io.parser;

import hec.heclib.util.HecTime;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.StatisticResult;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.util.TimeConverter;
import mil.army.usace.hec.hms.reports.util.Utilities;
import mil.army.usace.hec.hms.reports.util.ValidCheck;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DepthAreaResultsParser extends BasinResultsParser {
    private HecTime startTime;
    private HecTime endTime;
    private JSONObject simulationResults;
    private ZonedDateTime computedTime;

    DepthAreaResultsParser(Builder builder) {
        super(builder);

        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        String startTimeString = runResults.optString("StartTime") + " GMT";
        String endTimeString   = runResults.optString("EndTime") + " GMT";
        String executionTime   = runResults.optString("ExecutionTime") + " GMT";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm z");
        DateTimeFormatter executionFormatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm:ss z");
        this.startTime = TimeConverter.toHecTime(ZonedDateTime.parse(startTimeString, formatter));
        this.endTime   = TimeConverter.toHecTime(ZonedDateTime.parse(endTimeString, formatter));
        this.computedTime = ZonedDateTime.parse(executionTime, executionFormatter);
        this.simulationResults = runResults;
    } // DepthAreaResultsParser Constructor

    @Override
    public Map<String,ElementResults> getElementResults() {
        Map<String, ElementResults> elementResultsList = new HashMap<>();

        JSONArray analysisPointArray = simulationResults.optJSONArray("AnalysisPoint");
        JSONObject analysisPointObj  = simulationResults.optJSONObject("AnalysisPoint");
        if(analysisPointArray == null && analysisPointObj == null) { throw new IllegalArgumentException("Analysis Point(s) Not Found"); }

        if(analysisPointArray != null) {
            for(int i = 0; i < analysisPointArray.length(); i++) {
                ElementResults elementResults = populateElement(analysisPointArray.getJSONObject(i));
                elementResultsList.put(elementResults.getName(), elementResults);
                Double progressValue = ((double) i + 1) / analysisPointArray.length();
                support.firePropertyChange("Progress", "", progressValue);
            } // Loop: through all Analysis Points
        } // If: Has more than one Analysis Point(s)
        else {
            ElementResults elementResults = populateElement(analysisPointObj);
            elementResultsList.put(elementResults.getName(), elementResults);
        } // Else: Has only one Analysis Point

        return elementResultsList;
    } // getElementResults()

    @Override
    public String getSimulationName() {
        String simulationName = simulationResults.opt("AnalysisName").toString();
        return simulationName;
    } // getSimulationName()

    @Override
    public List<String> getAvailablePlots() {
        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());

        JSONArray analysisPointArray = runResults.optJSONArray("AnalysisPoint");
        JSONObject analysisPointObj  = runResults.optJSONObject("AnalysisPoint");
        if(analysisPointArray == null && analysisPointObj == null) { throw new IllegalArgumentException("Analysis Point(s) Not Found"); }

        List<String> availablePlots = new ArrayList<>();
        if(analysisPointArray != null) {
            for(int i = 0; i < analysisPointArray.length(); i++) {
                JSONObject elementObj = analysisPointArray.optJSONObject(i);
                availablePlots = new ArrayList<>(getElementAvailablePlots(elementObj, availablePlots));
            } // Loop: through all Analysis Points
        } // If: Has more than one Analysis Point(s)
        else {
            availablePlots = new ArrayList<>(getElementAvailablePlots(analysisPointObj, availablePlots));
        } // Else: Has only one Analysis Point

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

        Map<String, JSONObject> elementObjectMap = getElementObjects(elementObject);
        JSONObject hydrologyObject = elementObjectMap.get("Hydrology");

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

    private ElementResults populateElement(JSONObject elementObject) {
        /* ElementResults' Name */
        String name = elementObject.opt("name").toString();

        /* Element's Basin Results */
        Map<String, JSONObject> elementObjectMap = getElementObjects(elementObject);
        JSONObject analysisPointElement = elementObjectMap.get("AnalysisPoint");
        JSONObject hydrologyObject = elementObjectMap.get("Hydrology");
        JSONObject statisticObject = elementObjectMap.get("Statistics");

        List<TimeSeriesResult> timeSeriesResult = populateTimeSeriesResult(hydrologyObject);
        List<StatisticResult> statisticResult = populateStatisticsResult(statisticObject);
        Map<String, String> otherResultsMap = getOtherResultsMap(analysisPointElement);

        ElementResults elementResults = ElementResults.builder()
                .name(name)
                .timeSeriesResults(timeSeriesResult)
                .statisticResults(statisticResult)
                .otherResults(otherResultsMap)
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
            timeSeriesResultList.add(timeSeriesResult);
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

    /* Helper Functions */
    private Map<String, JSONObject> getElementObjects(JSONObject elementObject) {
        /* ElementResults' Name */
        String name = elementObject.opt("name").toString();

        /* Element's Basin Results */
        JSONArray basinElementArray   = elementObject.optJSONArray("BasinElement");
        JSONObject basinElementObject = elementObject.optJSONObject("BasinElement");
        if(basinElementArray == null && basinElementObject == null) { throw new IllegalArgumentException("DepthAreaResults: No Basin Elements Found"); }

        JSONObject analysisPointElement;
        // If: More than one Basin Elements
        if(basinElementArray != null) { analysisPointElement = findMatchingElement(basinElementArray, name); }
        // Else: Only one Basin Element
        else { analysisPointElement = findMatchingElement(new JSONArray(basinElementObject), name); }
        if(analysisPointElement == null) { throw new IllegalArgumentException("Analysis Point Element's Results Not Found"); }

        /* Get TimeSeriesResults, StatisticsResults, and OtherResults */
        JSONObject hydrologyObject = analysisPointElement.optJSONObject("Hydrology");
        if(hydrologyObject == null) { throw new IllegalArgumentException("Hydrology Object Not Found"); }
        JSONObject statisticObject = analysisPointElement.optJSONObject("Statistics");
        if(statisticObject == null) { throw new IllegalArgumentException("Statistics Object Not Found"); }

        Map<String, JSONObject> elementObjectMap = new LinkedHashMap<>();
        elementObjectMap.put("AnalysisPoint", analysisPointElement);
        elementObjectMap.put("Hydrology", hydrologyObject);
        elementObjectMap.put("Statistics", statisticObject);

        return elementObjectMap;
    } // getElementObjects

    private JSONObject findMatchingElement(JSONArray elementArray, String matchName) {
        for(int i = 0; i < elementArray.length(); i++) {
            JSONObject elementObject = elementArray.optJSONObject(i);
            if(elementObject == null) { throw new IllegalArgumentException("Element Object Not Found"); }
            String elementName = elementObject.opt("name").toString().trim();
            // Return 'elementObject' if Element's Name matches with 'matchName'
            if(elementName.equals(matchName.trim())) { return elementObject; }
        } // Loop: through elementArray

        return null;
    } // findMatchingElement()

    private Map<String, String> getOtherResultsMap(JSONObject elementObject) {
        JSONObject drainageArea = elementObject.getJSONObject("DrainageArea");
        Map<String, String> otherMap = new HashMap<>();
        otherMap.put("DrainageArea", drainageArea.opt("area").toString());
        JSONObject observedFlowGage = elementObject.optJSONObject("ObservedFlowGage");
        if(observedFlowGage != null) {
            otherMap.put("ObservedFlowGage", observedFlowGage.opt("name").toString());
        } // If: observedFlowGage exists
        JSONArray observedPoolGage = elementObject.optJSONArray("ObservedPoolElevationGage");
        if(observedPoolGage != null) {
            String observedPoolGageName = observedPoolGage.optJSONObject(0).optString("name");
            otherMap.put("ObservedPoolElevationGage", observedPoolGageName);
        } // If: observedFlowGage exists

        return otherMap;
    } // getOtherResultsMap()
} // MonteCarloResultsParser Class

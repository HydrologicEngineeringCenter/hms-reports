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
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlBasinResultsParser extends BasinResultsParser {
    private final HecTime startTime;
    private final HecTime endTime;
    private final JSONObject simulationResults;
    private final ZonedDateTime computedTime;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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

        JSONArray elementArray = getElementArray();
        if(elementArray == null) return null;

        for(int i = 0; i < elementArray.length(); i++) {
            ElementResults elementResults = populateElement(elementArray.getJSONObject(i));
            elementResultsList.put(elementResults.getName(), elementResults);
            Double progressValue = ((double) i + 1) / elementArray.length();
            support.firePropertyChange("Progress", "", progressValue);
        } // Loop through all element's results, and populate

        return elementResultsList;
    } // getElementResults()

    @Override
    public String getSimulationName() {
        String simulationName = "";

        if(simulationType == SimulationType.FORECAST)
            simulationName = simulationResults.opt("ForecastName").toString();
        else if(simulationType == SimulationType.RUN)
            simulationName = simulationResults.opt("RunName").toString();
        else if(simulationType == SimulationType.OPTIMIZATION)
            simulationName = simulationResults.opt("AnalysisName").toString();

        return simulationName;
    } // getSimulationName()

    @Override
    public List<String> getAvailablePlots() {
        List<String> availablePlots = new ArrayList<>();

        JSONArray elementArray = getElementArray();
        if(elementArray == null) return null;

        for(int i = 0; i < elementArray.length(); i++) {
            JSONObject elementObj = elementArray.optJSONObject(i);
            availablePlots = getElementAvailablePlots(elementObj, availablePlots);
        } // Loop through all element's results, and populate

        if(availablePlots == null) {
            logger.log(Level.WARNING, "No available plots");
            return null;
        }

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

    private JSONArray getElementArray() {
        JSONArray elementArray = simulationResults.optJSONArray("BasinElement");
        JSONObject elementObject = simulationResults.optJSONObject("BasinElement");

        if(elementArray != null)
            return elementArray;
        else if(elementObject != null)
            return new JSONArray(elementObject);
        else
            logger.log(Level.WARNING, "No Elements Found");

        return null;
    } // getElementArray()

    private List<String> getElementAvailablePlots(JSONObject elementObject, List<String> availablePlots) {
        List<String> elementPlots = new ArrayList<>(availablePlots);
        if(elementObject == null){
            logger.log(Level.WARNING, "getElementAvailablePlots() - elementObject is null");
            return null;
        }

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

        return XML.toJSONObject(content);
    } // getJsonObject()

    private ElementResults populateElement(JSONObject elementObject) {
        String name = elementObject.opt("name").toString();
        JSONObject hydrologyObject = elementObject.getJSONObject("Hydrology");
        List<TimeSeriesResult> timeSeriesResult = populateTimeSeriesResult(hydrologyObject);
        JSONObject statisticsArray = elementObject.getJSONObject("Statistics");
        List<StatisticResult> statisticResults = populateStatisticsResult(statisticsArray);
        Map<String, String> otherResults = populateOtherResults(elementObject);

        return ElementResults.builder()
                .name(name)
                .timeSeriesResults(timeSeriesResult)
                .statisticResults(statisticResults)
                .otherResults(otherResults)
                .build();
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
            logger.log(Level.WARNING, "populateTimeSeriesResult() - Invalid object");
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
        return TimeSeriesResult.builder()
                .type(type)
                .pathToFile(pathToDss)
                .variable(variable)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .build();
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
            logger.log(Level.WARNING, "populateStatisticsResult() - Invalid object");
        } // Else: Invalid object

        return statisticResultList;
    } // populateStatisticsResult()

    private Map<String, String> populateOtherResults(JSONObject elementObject) {
        Map<String, String> otherResults = new HashMap<>();

        /* Drainage Area & Units*/
        JSONObject drainageArea = elementObject.getJSONObject("DrainageArea");
        otherResults.put("DrainageArea", drainageArea.opt("area").toString());
        otherResults.put("DrainageAreaUnits", drainageArea.opt("units").toString());

        /* Observed Flow Gage */
        JSONObject observedFlowGage = elementObject.optJSONObject("ObservedFlowGage");
        if(observedFlowGage != null) {
            otherResults.put("ObservedFlowGage", observedFlowGage.opt("name").toString());
        }

        /* Observed Pool Elevation Gage */
        JSONArray observedPoolGage = elementObject.optJSONArray("ObservedPoolElevationGage");
        if(observedPoolGage != null) {
            String observedPoolGageName = observedPoolGage.optJSONObject(0).optString("name");
            otherResults.put("ObservedPoolElevationGage", observedPoolGageName);
        }

        return otherResults;
    }

} // XmlBasinResultsParser class

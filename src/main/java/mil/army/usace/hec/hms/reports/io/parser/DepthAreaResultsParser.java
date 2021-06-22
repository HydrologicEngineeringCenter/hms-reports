package mil.army.usace.hec.hms.reports.io.parser;

import hec.heclib.util.HecTime;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.StatisticResult;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.util.TimeUtil;
import mil.army.usace.hec.hms.reports.util.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DepthAreaResultsParser extends BasinResultsParser {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final HecTime startTime;
    private final HecTime endTime;
    private final JSONObject simulationResults;
    private final ZonedDateTime computedTime;

    DepthAreaResultsParser(Builder builder) {
        super(builder);

        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        String startTimeString = runResults.optString("StartTime") + " GMT";
        String endTimeString   = runResults.optString("EndTime") + " GMT";
        String executionTime   = runResults.optString("ExecutionTime") + " GMT";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm z");
        DateTimeFormatter executionFormatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm:ss z");
        this.startTime = TimeUtil.toHecTime(ZonedDateTime.parse(startTimeString, formatter));
        this.endTime   = TimeUtil.toHecTime(ZonedDateTime.parse(endTimeString, formatter));
        this.computedTime = ZonedDateTime.parse(executionTime, executionFormatter);
        this.simulationResults = runResults;
    } // DepthAreaResultsParser Constructor

    @Override
    /* Return List of all elements available. Analysis Point(s) followed by its individuals */
    public Map<String,ElementResults> getElementResults() {
        Map<String, ElementResults> elementResultsList = new LinkedHashMap<>();

       JSONArray analysisPointArray = getArray(simulationResults,"AnalysisPoint");

        for(int i = 0; i < analysisPointArray.length(); i++) {
            JSONObject analysisPoint = analysisPointArray.getJSONObject(i);
            analysisPoint.optString("name");
            List<ElementResults> individualList = getIndividualElementResultList(analysisPoint);
            individualList.forEach(e -> elementResultsList.putIfAbsent(e.getName(), e));
            Double progressValue = ((double) i + 1) / analysisPointArray.length();
            support.firePropertyChange("Progress", "", progressValue);
        } // Loop: through all Analysis Points

        return elementResultsList;
    } // getElementResults()

    @Override
    public String getSimulationName() {
        return simulationResults.opt("AnalysisName").toString();
    } // getSimulationName()

    @Override
    public List<String> getAvailablePlots() {
        List<String> availablePlots = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(pathToBasinResultsFile);
            lines = lines.stream().filter(l -> l.contains("<TimeSeriesType type=")).collect(Collectors.toList());
            lines.forEach(l -> {
                String beg = "displayString=\"", end = "\" />";
                String displayString = l.substring(l.indexOf(beg) + beg.length(), l.indexOf(end));
                if(!availablePlots.contains(displayString))
                    availablePlots.add(displayString);
            });
        } catch(IOException exception) {
            logger.log(Level.SEVERE, "Unable to read results file at: " + pathToBasinResultsFile);
        }

        /* Sort the list Alphabetically */
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
            System.out.println("Invalid object");
        } // Else: Invalid object

        return statisticResultList;
    } // populateStatisticsResult()

    private Map<String, String> getOtherResultsMap(JSONObject elementObject) {
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

        return otherMap;
    } // getOtherResultsMap()


    private List<ElementResults> getIndividualElementResultList(JSONObject analysisPoint) {
        List<ElementResults> resultsList = new ArrayList<>();
        String analysisPointName = analysisPoint.optString("name");

        JSONArray jsonElementArray = getArray(analysisPoint, "BasinElement");
        for(int i = 0; i < jsonElementArray.length(); i++) {
            JSONObject jsonObject = jsonElementArray.optJSONObject(i);
            if(jsonObject != null) {
                ElementResults elementResults = populateElementResults(jsonObject);
                if(elementResults != null) {
                    if(elementResults.getName().equals(analysisPointName))
                        elementResults.getOtherResults().put("isAnalysisPoint", "true");
                    else
                        elementResults.getOtherResults().put("isAnalysisPoint", "false");
                    resultsList.add(elementResults);
                }
            }
        }

        return resultsList;
    }

    private ElementResults populateElementResults(JSONObject elementObject) {
        /* ElementResults' Name */
        String name = elementObject.opt("name").toString();

        /* Time Series Results */
        JSONObject hydrologyObject = elementObject.optJSONObject("Hydrology");
        if(hydrologyObject == null) {
            logger.log(Level.SEVERE, name + ": Hydrology Object not found");
            return null;
        }
        List<TimeSeriesResult> timeSeriesResult = populateTimeSeriesResult(hydrologyObject);

        /* Statistics Results */
        JSONObject statisticObject = elementObject.optJSONObject("Statistics");
        if(statisticObject == null) {
            logger.log(Level.SEVERE, name + ": Statistics Object not found");
            return null;
        }
        List<StatisticResult> statisticResult = populateStatisticsResult(statisticObject);

        /* Other Results */
        Map<String, String> otherResultsMap = getOtherResultsMap(elementObject);

        return ElementResults.builder()
                .name(name)
                .timeSeriesResults(timeSeriesResult)
                .statisticResults(statisticResult)
                .otherResults(otherResultsMap)
                .build();
    }

    private JSONArray getArray(JSONObject jsonObject, String key) {
        JSONArray analysisPointArray = jsonObject.optJSONArray(key);
        if(analysisPointArray != null)
            return analysisPointArray;

        JSONObject analysisPointObj  = jsonObject.optJSONObject(key);
        if(analysisPointObj != null) {
            JSONArray array = new JSONArray();
            array.put(analysisPointObj);
            return array;
        }

        logger.log(Level.SEVERE, key + " Not Found");
        return new JSONArray();
    }
} // MonteCarloResultsParser Class

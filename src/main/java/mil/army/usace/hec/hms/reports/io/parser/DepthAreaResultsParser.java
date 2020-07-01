package mil.army.usace.hec.hms.reports.io.parser;

import hec.heclib.dss.HecTimeSeries;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesContainer;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.StatisticResult;
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

public class DepthAreaResultsParser extends BasinResultsParser {
    private HecTime startTime;
    private HecTime endTime;

    DepthAreaResultsParser(Builder builder) {
        super(builder);
    }

    @Override
    public Map<String,ElementResults> getElementResults() {
        Map<String, ElementResults> elementResultsList = new HashMap<>();
        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        String startTimeString = runResults.optString("StartTime") + " GMT";
        String endTimeString   = runResults.optString("EndTime") + " GMT";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy, HH:mm z");
        this.startTime = TimeConverter.toHecTime(ZonedDateTime.parse(startTimeString, formatter));
        this.endTime   = TimeConverter.toHecTime(ZonedDateTime.parse(endTimeString, formatter));

        JSONArray analysisPointArray = runResults.optJSONArray("AnalysisPoint");
        JSONObject analysisPointObj  = runResults.optJSONObject("AnalysisPoint");
        if(analysisPointArray == null && analysisPointObj == null) { throw new IllegalArgumentException("Analysis Point(s) Not Found"); }

        if(analysisPointArray != null) {
            for(int i = 0; i < analysisPointArray.length(); i++) {
                ElementResults elementResults = populateElement(analysisPointArray.getJSONObject(i));
                elementResultsList.put(elementResults.getName(), elementResults);
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
        JSONObject resultFile  = XmlBasinResultsParser.getJsonObject(this.pathToBasinResultsFile.toString());
        JSONObject runResults  = resultFile.getJSONObject(simulationType.getName());
        String simulationName = runResults.opt("AnalysisName").toString();
        return simulationName;
    } // getSimulationName()

    private ElementResults populateElement(JSONObject elementObject) {
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
                timeSeriesResultList.add(timeSeriesResult);
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
        if(operationStatus != 0) { System.out.println("Time Read not Successful"); }
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

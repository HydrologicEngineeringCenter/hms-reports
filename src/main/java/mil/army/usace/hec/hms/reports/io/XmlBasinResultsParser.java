package mil.army.usace.hec.hms.reports.io;

import hec.heclib.dss.HecDataManager;
import hec.heclib.dss.HecTimeSeries;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesContainer;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.StatisticResult;
import mil.army.usace.hec.hms.reports.util.TimeConverter;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.util.Utilities;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

public class XmlBasinResultsParser extends BasinResultsParser {

    XmlBasinResultsParser(Builder builder) {
        super(builder);
    }

    @Override
    public Map<String,ElementResults> getElementResults() {
        Map<String, ElementResults> elementResultsList = new HashMap<>();
        JSONObject resultFile = getJsonObject(this.pathToBasinResultsFile.toString());
        JSONArray elemenentArray = resultFile.getJSONObject("RunResults").getJSONArray("BasinElement");

        for(int i = 0; i < elemenentArray.length(); i++) {
            System.out.println("Element Index: " + i);
            ElementResults elementResults = populateElement(elemenentArray.getJSONObject(i));
            elementResultsList.put(elementResults.getName(), elementResults);
        } // Loop through all element's results, and populate

        return elementResultsList;
    } // getElementResults()
    private static JSONObject getJsonObject(String pathToJson) {
        /* Read in XML File */
        File file = new File(pathToJson);
        String content = null;
        /* Read XML's content to 'content' */
        try { content = FileUtils.readFileToString(file, "utf-8"); }
        catch (IOException e) { e.printStackTrace(); }
        /* Convert XML string to JSONObject */
        assert content != null;
        JSONObject object = XML.toJSONObject(content);

        return object;
    } // getJsonObject()
    private ElementResults populateElement(JSONObject elementObject) {
        String name = elementObject.getString("name");
        JSONObject hydrologyObject = elementObject.getJSONObject("Hydrology");
        List<TimeSeriesResult> timeSeriesResult = populateTimeSeriesResult(hydrologyObject);
        JSONObject statisticsArray = elementObject.getJSONObject("Statistics");
        List<StatisticResult> statisticResults = populateStatisticsResult(statisticsArray);
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

        HecDataManager.closeAllFiles(); // Closing Hec

        return timeSeriesResultList;
    } // populateTimeSeriesResult()
    private TimeSeriesResult populateSingleTimeSeriesResult (JSONObject timeObject) {
        String DssFileName = timeObject.getString("DssFileName");
        String pathToDss = Utilities.getFilePath(this.pathToProjectDirectory.toAbsolutePath().toString(), DssFileName);

        /* Read in TimeSeriesType */
        String type = timeObject.getJSONObject("TimeSeriesType").getString("displayString");

        /* Using HEC DSS to read in Time Series Container */
        TimeSeriesContainer container = new TimeSeriesContainer();
        container.fullName = (timeObject.getString("DssPathname"));
        HecTimeSeries dssTimeSeriesRead = new HecTimeSeries();
        dssTimeSeriesRead.setDSSFileName(pathToDss);

        int operationStatus = dssTimeSeriesRead.read(container, true);
        if(operationStatus != 0) { System.out.println("Time Read not Successful"); }

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
    private List<ZonedDateTime> getZonedDateTimeArray (HecTimeArray timeArray) {
        List<ZonedDateTime> zonedDateTimeArray = new ArrayList<>();
        for(int i = 0; i < timeArray.numberElements(); i++) {
            HecTime singleTime = timeArray.element(i);
            ZonedDateTime zonedTime = TimeConverter.toZonedDateTime(singleTime);
            zonedDateTimeArray.add(zonedTime);
        } // Loop: through HecTimeArray

        return zonedDateTimeArray;
    } // getZonedDateTimeArray()
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

    public static List<String> getAvailablePlots(String pathToResultFile) {
        List<String> availablePlots = new ArrayList<>();
        JSONObject resultFile = getJsonObject(pathToResultFile);
        JSONArray elemenentArray = resultFile.getJSONObject("RunResults").getJSONArray("BasinElement");

        for(int i = 0; i < elemenentArray.length(); i++) {
            JSONObject elementObject = elemenentArray.optJSONObject(i);
            JSONObject hydrologyObject = elementObject.optJSONObject("Hydrology");
            JSONArray timeSeriesArray = hydrologyObject.optJSONArray("TimeSeries");
            for(int j = 0; j < timeSeriesArray.length(); j++) {
                JSONObject timeSeriesObject = timeSeriesArray.optJSONObject(j);
                JSONObject timeSeriesTypeObject = timeSeriesObject.optJSONObject("TimeSeriesType");
                String timeSeriesType = timeSeriesTypeObject.optString("displayString");

                if(!availablePlots.contains(timeSeriesType)) {
                    availablePlots.add(timeSeriesType);
                } // If: plot hasn't been added
            } // Loop through all the timeSeriesArray
        } // Loop through all element's results, and populate

        Collections.sort(availablePlots); // Sort the list Alphabetically

        return availablePlots;
    } // getAvailablePlots()
} // XmlBasinResultsParser class

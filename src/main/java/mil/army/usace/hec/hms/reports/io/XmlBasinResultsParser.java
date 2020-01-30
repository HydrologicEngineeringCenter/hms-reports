package mil.army.usace.hec.hms.reports.io;

import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import mil.army.usace.hec.hms.reports.ElementResults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import mil.army.usace.hec.hms.reports.StatisticResult;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import hec.heclib.dss.*;
import hec.io.TimeSeriesContainer;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import wcds.hfp.shared.Zone;

public class XmlBasinResultsParser extends BasinResultsParser {

    XmlBasinResultsParser(Builder builder) {
        super(builder);
    }

    @Override
    public List<ElementResults> getElementResults() {
        List<ElementResults> elementResultsList = new ArrayList<>();
        JSONObject resultFile = getJsonObject(this.pathToBasinResultsFile.toString());
        JSONArray elemenentArray = resultFile.getJSONObject("RunResults").getJSONArray("BasinElement");

        for(int i = 0; i < elemenentArray.length(); i++) {
            System.out.println("Element Index: " + i);
            ElementResults elementResults = populateElement(elemenentArray.getJSONObject(i));
            elementResultsList.add(elementResults);
        } // Loop through all element's results, and populate

        return elementResultsList;
    } // getElementResults()
    private JSONObject getJsonObject(String pathToJson) {
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
        JSONObject hydrologyObject = elementObject.getJSONObject("Hydrology");
        List<TimeSeriesResult> timeSeriesResult = populateTimeSeriesResult(hydrologyObject);
        JSONObject statisticsArray = elementObject.getJSONObject("Statistics");
        List<StatisticResult> statisticResults = populateStatisticsResult(statisticsArray);

        ElementResults elementResults = ElementResults.builder()
                .timeSeriesResults(timeSeriesResult)
                .statisticResults(statisticResults)
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
        String pathToDss = "src/resources/" + DssFileName;

        /* Using HEC DSS to read in Time Series Container */
        TimeSeriesContainer container = new TimeSeriesContainer();
        container.fullName = (timeObject.getString("DssPathname"));
        HecTimeSeries dssTimeSeriesRead = new HecTimeSeries();
        dssTimeSeriesRead.setDSSFileName(pathToDss);

        int operationStatus = dssTimeSeriesRead.read(container, true);
        if(operationStatus != 0) { System.out.println("Time Read not Successful"); }

        double[] values = container.getValues();
        List<ZonedDateTime> times  = convertToZoneDateTime(container.getTimes());

        TimeSeriesResult timeSeriesResult = TimeSeriesResult.builder()
                .times(times)
                .values(values)
                .build();

        return timeSeriesResult;
    } // populateSingleTimeSeriesResult()

    private List<ZonedDateTime> convertToZoneDateTime (HecTimeArray timeArray) {
        List<ZonedDateTime> zonedDateTimeArray = new ArrayList<>();
        for(int i = 0; i < timeArray.numberElements(); i++) {
            HecTime singleTime = timeArray.element(i);
            int year = singleTime.year();
            int month = singleTime.month();
            int day = singleTime.day();
            int hour = singleTime.hour();
            int minute = singleTime.minute();
            int second = singleTime.second();
            int nanosecond = 0;
            ZoneId zoneId = ZoneId.of("UTC");

            if(hour == 24 && minute == 0) {
                hour = 23;
                minute = 59;
            } // Make 'hour' acceptable to ZonedDateTime's hour (capped at 23)

            ZonedDateTime zonedTime = ZonedDateTime.of(year, month, day, hour, minute, second, nanosecond, zoneId);
            zonedDateTimeArray.add(zonedTime);
        } // Loop: through HecTimeArray

        return zonedDateTimeArray;
    } // convertToZoneDateTime()

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

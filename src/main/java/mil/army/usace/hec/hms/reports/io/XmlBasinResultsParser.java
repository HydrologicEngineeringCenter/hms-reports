package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.ElementResults;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mil.army.usace.hec.hms.reports.StatisticResult;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import hec.heclib.dss.*;
import hec.heclib.util.HecTime;
import hec.heclib.util.HecTimeArray;
import hec.io.TimeSeriesContainer;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

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
        JSONArray timeSeriesArray = elementObject.getJSONObject("Hydrology").getJSONArray("TimeSeries");
        List<TimeSeriesResult> timeSeriesResult = populateTimeSeriesResult(timeSeriesArray);
        JSONArray statisticsArray = elementObject.getJSONObject("Statistics").getJSONArray("StatisticMeasure");
        List<StatisticResult> statisticResults = populateStatisticsResult(statisticsArray);

        ElementResults elementResults = ElementResults.builder()
                .timeSeriesResults(timeSeriesResult)
                .statisticResults(statisticResults)
                .build();

        return elementResults;
    } // populateElement()

    private List<TimeSeriesResult> populateTimeSeriesResult(JSONArray timeArray) {
        List<TimeSeriesResult> timeSeriesResultList = new ArrayList<>();

        for(int i = 0; i < timeArray.length(); i++) {
            JSONObject timeObject = timeArray.getJSONObject(i);
            String DssFileName = timeObject.getString("DssFileName");

        } // Loop: through timeArray

        return null;
    } // populateTimeSeriesResult()

    private List<StatisticResult> populateStatisticsResult(JSONArray statisticsArray) {
        List<StatisticResult> statisticResultList = new ArrayList<>();

        for(int i = 0; i < statisticsArray.length(); i++) {
            JSONObject statisticObject = statisticsArray.getJSONObject(i);
            String name = statisticObject.getString("displayString");
            String value = statisticObject.opt("value").toString();
            String units = statisticObject.optString("units");

            StatisticResult statisticResult = StatisticResult.builder()
                    .name(name)
                    .value(value)
                    .units(units)
                    .build();

            statisticResultList.add(statisticResult);
        } // Loop through statisticsArray

        return statisticResultList;
    } // populateStatisticsResult()
    private List<String> unnecessaryContent() {
        List<String> stringList = new ArrayList<>();
        stringList.add("Hydrology");
        stringList.add("Statistics");
        stringList.add("TimeSeries");
        stringList.add("StatisticMeasure");
        /* Add more if necessary */
        return stringList;
    } // necessaryResults()
}

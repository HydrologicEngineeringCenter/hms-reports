package mil.army.usace.hec.hms.reports.util;

import j2html.tags.DomContent;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static j2html.TagCreator.*;

public class ValidCheck {
    private ValidCheck() {}

    public static List<String> unnecessarySingleProcesses() {
        List<String> stringList = new ArrayList<>();
        stringList.add("name");
        stringList.add("elementType");
        return stringList;
    } // unnecessarySingleProcesses()

    public static List<String> validStatisticResult() {
        List<String> stringList = new ArrayList<>();

        stringList.add("Peak Discharge");
        stringList.add("Precipitation Volume");
        stringList.add("Loss Volume");
        stringList.add("Excess Volume");
        stringList.add("Date/Time of Peak Discharge");
        stringList.add("Direct Runoff Volume");
        stringList.add("Baseflow Volume");
        stringList.add("Discharge Volume");

        return stringList;
    } // validStatisticResult()

    public static Boolean validTimeSeriesPlot(String plotName) {
        if(plotName.contains("Precipitation")) {
            if(!plotName.contains("Cumulative"))
                return true;
        } // If: plotName contains Precipitation

        if(plotName.contains("Outflow")) {
            return true;
        } // If: plotName contains Outflow

        return false;
    } // validTimeSeriesPlot()


} // ValidCheck class

package mil.army.usace.hec.hms.reports.util;

import java.util.ArrayList;
import java.util.List;

public class ValidCheck {
    private ValidCheck() {}

    public static List<String> unnecessarySingleProcesses() {
        List<String> stringList = new ArrayList<>();
        stringList.add("name");
        stringList.add("elementType");
        return stringList;
    } // unnecessarySingleProcesses()

    public static List<String> unnecessaryGlobalParameterProcesses() {
        List<String> stringList = new ArrayList<>();
        stringList.add("name");
        stringList.add("elementType");
        stringList.add("longitude");
        stringList.add("latitude");
        stringList.add("downstream");
        return stringList;
    } // unnecessaryGlobalParameterProcesses()

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

    public static Boolean validTimeSeriesPlot(String plotName, List<String> chosenPlots) {
        // Default Case
        if(plotName.equals("Precipitation") || plotName.equals("Excess Precipitation") || plotName.equals("Outflow") || plotName.equals("Observed Flow"))
            return true;

        // Chosen Plots
        if(chosenPlots != null && chosenPlots.contains(plotName))
            return true;

        return false;
    } // validTimeSeriesPlot()

} // ValidCheck class

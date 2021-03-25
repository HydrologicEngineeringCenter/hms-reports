package mil.army.usace.hec.hms.reports.io.standard;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.StatisticResult;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static j2html.TagCreator.*;

public class ElementResultsWriter {
    private final List<Element> elementList;
    private final List<String> chosenPlots;
    private final List<SummaryChoice> reportSummaryChoice;
    private final PropertyChangeSupport support;

    /* Constructors */
    private ElementResultsWriter(Builder builder){
        this.elementList = builder.elementList;
        this.reportSummaryChoice = builder.reportSummaryChoice;
        this.chosenPlots = builder.chosenPlots;
        support = new PropertyChangeSupport(this);
    } // ElementResultsWriter Constructor

    public static class Builder{
        List<Element> elementList;
        List<SummaryChoice> reportSummaryChoice;
        List<String> chosenPlots;

        public Builder elementList(List<Element> elementList){
            this.elementList = elementList;
            return this;
        } // 'elementList' constructor

        public Builder reportSummaryChoice(List<SummaryChoice> reportSummaryChoice) {
            this.reportSummaryChoice = reportSummaryChoice;
            return this;
        } // 'reportSummaryChoice' constructor

        public Builder chosenPlots(List<String> chosenPlots) {
            this.chosenPlots = chosenPlots;
            return this;
        } // 'chosenPlots' constructor

        public ElementResultsWriter build(){
            return new ElementResultsWriter(this);
        }
    } // Builder class: as ElementResultsWriter's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Main Function */
    Map<String, DomContent> elementResultsMap() {
        Map<String, DomContent> elementResultsMap = new LinkedHashMap<>();
        Map<String, DomContent> summaryResultsMap = printListResultsWriter();
        if(summaryResultsMap == null) { summaryResultsMap = new HashMap<>(); }

        for(int i = 0; i < elementList.size(); i++) {
            Element element = elementList.get(i);
            String elementName = element.getName();
            ElementResults elementResults = element.getElementResults();
            DomContent elementResultsDom = printElementResults(elementResults, summaryResultsMap.get(elementName));
            elementResultsMap.put(elementName, elementResultsDom);
            Double progressValue = ((double) i + 1) / elementList.size();
            support.firePropertyChange("Progress", "", progressValue);
        } // Loop: through each element

        return elementResultsMap;
    } // elementResultsMap()

    /* Element Results */
    private DomContent printElementResults(ElementResults elementResults, DomContent summaryResults) {
        List<DomContent> elementResultsDomList = new ArrayList<>();
        if(elementResults == null) { return null; }

        /* Get Summary Results Dom */
        if(summaryResults != null) { elementResultsDomList.add(summaryResults); }
        /* Get TimeSeries Results Dom */
        TimeSeriesPlotWriter timeSeriesPlotWriter = new TimeSeriesPlotWriter(elementResults, chosenPlots);
        DomContent timeSeriesResults = timeSeriesPlotWriter.getTimeSeriesPlots();
        elementResultsDomList.add(timeSeriesResults);

        return div(attrs(".element-results"), elementResultsDomList.toArray(new DomContent[]{}));
    } // printElementResults()

    /* Element Results Tables */
    private Map<String, DomContent> printListResultsWriter() {
        if(reportSummaryChoice == null || !reportSummaryChoice.contains(SummaryChoice.ELEMENT_RESULTS_SUMMARY)) {
            return null;
        } // If: Report Summary Choice contains PARAMETER_SUMMARY

        Map<String, DomContent> elementResultsMap = new HashMap<>();

        for(Element element: this.elementList) {
            String elementType = element.getElementInput().getElementType().toUpperCase();
            DomContent tableDom = null;

            if(element.getElementResults() == null) {
                elementResultsMap.put(element.getName(), null);
                continue;
            } // If: No ElementResults, Skip Element

            switch(elementType) {
                case "SUBBASIN":
                    tableDom = printSubbasinResultsTable(element);
                    break;
                case "REACH":
                    tableDom = printReachResultsTable(element);
                    break;
                case "JUNCTION":
                    tableDom = printJunctionResultsTable(element);
                    break;
                case "SINK":
                    tableDom = printSinkResultsTable(element);
                    break;
                case "SOURCE":
                    tableDom = printSourceResultsTable(element);
                    break;
                case "RESERVOIR":
                    tableDom = printReservoirResultsTable(element);
                    break;
                default:
                    System.out.println("This element type is not supported: " + elementType);
            } // Switch case: for element's type
            elementResultsMap.put(element.getName(), tableDom);
        } // Loop: through all elements to print its element results table

        return elementResultsMap;
    } // printListResultsWriter()

    private DomContent printSubbasinResultsTable(Element element) {
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        List<DomContent> globalParameterTableDom = new ArrayList<>(printDefaultResultsData(element));

        /* Precipitation Volume */
        rowDom = printResultsTableRow(element, "Precipitation Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Loss Volume */
        rowDom = printResultsTableRow(element, "Loss Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Excess Volume */
        rowDom = printResultsTableRow(element, "Excess Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Direct Runoff Volume */
        rowDom = printResultsTableRow(element, "Direct Flow Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Baseflow Volume */
        rowDom = printResultsTableRow(element, "Baseflow Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSubbasinResultsTable()

    private DomContent printReachResultsTable(Element element) {
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        List<DomContent> globalParameterTableDom = new ArrayList<>(printDefaultResultsData(element));

        /* Maximum Inflow */
        rowDom = printResultsTableRow(element, "Maximum Inflow");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Inflow Volume */
        rowDom = printResultsTableRow(element, "Inflow Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printReachResultsTable()

    private DomContent printJunctionResultsTable(Element element) {
        String tdAttribute = ".global-parameter";

        /* Default Values */
        List<DomContent> globalParameterTableDom = new ArrayList<>(printDefaultResultsData(element));

        if(!globalParameterTableDom.isEmpty()) {
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printJunctionResultsTable()

    private DomContent printSinkResultsTable(Element element) {
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        List<DomContent> globalParameterTableDom = new ArrayList<>(printDefaultResultsData(element));

        /* Observed Flow Gage */
        rowDom = printResultsTableRow(element, "ObservedFlowGage");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Observed Flow's Volume */
        rowDom = printResultsTableRow(element, "Observed Flow Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Observed Flow's RMSE Stdev */
        rowDom = printResultsTableRow(element, "Observed Flow RMSE Stdev");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Observed Flow's Percent Bias' */
        rowDom = printResultsTableRow(element, "Observed Flow Percent Bias");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Observed Flow's Nash Sutcliffe */
        rowDom = printResultsTableRow(element, "Observed Flow Nash Sutcliffe");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSinkResultsTable()

    private DomContent printSourceResultsTable(Element element) {
        String tdAttribute = ".global-parameter";

        /* Default Values */
        List<DomContent> globalParameterTableDom = new ArrayList<>(printDefaultResultsData(element));

        if(!globalParameterTableDom.isEmpty()) {
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSourceResultsTable()

    private DomContent printReservoirResultsTable(Element element) {
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        List<DomContent> globalParameterTableDom = new ArrayList<>(printDefaultResultsData(element));

        /* Peak Inflow */
        rowDom = printResultsTableRow(element, "Maximum Inflow");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Time of Peak Inflow */
        rowDom = printResultsTableRow(element, "Time of Maximum Inflow");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Inflow Volume */
        rowDom = printResultsTableRow(element, "Inflow Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Peak Storage */
        rowDom = printResultsTableRow(element, "Maximum Storage");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Peak Elevation */
        rowDom = printResultsTableRow(element, "Maximum Pool Elevation");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Discharge Volume */
        rowDom = printResultsTableRow(element, "Outflow Volume");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Observed Pool Elevation Gage */
        rowDom = printResultsTableRow(element, "ObservedPoolElevationGage");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Observed Peak Pool Elevation */
        rowDom = printResultsTableRow(element, "Maximum Observed Pool Elevation");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* RMSE Std Dev (Observed) */
        rowDom = printResultsTableRow(element, "Observed Pool Elevation RMSE Stdev");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Percent Bias (Observed) */
        rowDom = printResultsTableRow(element, "Observed Pool Elevation Percent Bias");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Time of Peak Pool Elevation (Observed) */
        rowDom = printResultsTableRow(element, "Time of Maximum Observed Pool Elevation");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        /* Nash-Sutcliffe (Observed) */
        rowDom = printResultsTableRow(element, "Observed Pool Elevation Nash Sutcliffe");
        if(rowDom != null) globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printReservoirResultsTable()

    private List<DomContent> printDefaultResultsData(Element element) {
        List<DomContent> defaultDomList = new ArrayList<>();
        DomContent rowDom;

        /* Peak Discharge */
        rowDom = printResultsTableRow(element, "Maximum Outflow");
        if(rowDom != null) defaultDomList.add(rowDom);

        /* Time of Peak */
        rowDom = printResultsTableRow(element, "Time of Maximum Outflow");
        if(rowDom != null) defaultDomList.add(rowDom);

        /* Volume */
        rowDom = printResultsTableRow(element, "Outflow Depth");
        if(rowDom != null) defaultDomList.add(rowDom);

        return defaultDomList;
    } // printDefaultResultsData()

    private DomContent printResultsTableRow(Element element, String processName) {
        String tdAttribute = ".global-parameter";
        ElementResults elementResults = element.getElementResults();

        /* Data Name */
        StatisticResult statisticResult = elementResults.getStatisticSResult(processName);
        String processUnits = statisticResult.getUnits();
        String displayName = getProcessDisplayName(processName);
        String dataName = (processUnits.isEmpty()) ? displayName : displayName + " (" + processUnits + ")";

        /* Data Value */
        Map<String, String> otherResultsMap = elementResults.getOtherResults();
        String valueInOther = otherResultsMap.getOrDefault(processName, "Not specified");
        String dataValue = (statisticResult.getName().isEmpty()) ? valueInOther : statisticResult.getValue();

        /* Add Percentage to Data Value */
        if(processName.toLowerCase().contains("percent")) {
            dataValue = StringBeautifier.beautifyString(dataValue);
            dataValue = (dataValue.equals("Not specified")) ? dataValue : dataValue  + "%";
        }

        /* Return a row DOM */
        if(dataValue.equalsIgnoreCase("Not specified")) return null;
        List<String> rowData = Arrays.asList(dataName, dataValue);
        return HtmlModifier.printTableDataRow(rowData, tdAttribute, tdAttribute);
    }

    private String getProcessDisplayName(String processName) {
        if(processName.equalsIgnoreCase("Direct Flow Volume"))
            processName = "Direct Runoff Volume";
        else if(processName.equalsIgnoreCase("Maximum Inflow"))
            processName = "Peak Inflow";
        else if(processName.equalsIgnoreCase("Time of Maximum Inflow"))
            processName = "Time of Peak Inflow";
        else if(processName.equalsIgnoreCase("Maximum Pool Elevation"))
            processName = "Peak Elevation";
        else if(processName.equalsIgnoreCase("Outflow Volume"))
            processName = "Discharge Volume";
        else if(processName.equalsIgnoreCase("ObservedPoolElevationGage"))
            processName = "Observed Pool Elevation Gage";
        else if(processName.equalsIgnoreCase("Maximum Observed Pool Elevation"))
            processName = "Observed Peak Pool Elevation";
        else if(processName.equalsIgnoreCase("Maximum Outflow"))
            processName = "Peak Discharge";
        else if(processName.equalsIgnoreCase("Time of Maximum Outflow"))
            processName = "Time of Peak Discharge";
        else if(processName.equalsIgnoreCase("Outflow Depth"))
            processName = "Volume";

        return processName;
    }

    @Deprecated
    /* This function has been deprecated. Use the above 'printResultsTableRow' instead */
    private DomContent printResultsTableRow(Element element, String mapKey, String dataName) {
        String tdAttribute = ".global-parameter";
        Map<String, String> statisticResultsMap = element.getElementResults().getStatisticResultsMap();
        Map<String, String> otherResultsMap = element.getElementResults().getOtherResults();

        String valueInStatistics = statisticResultsMap.getOrDefault(mapKey, "Not specified");
        String valueInOther = otherResultsMap.getOrDefault(mapKey, "Not specified");
        String mapData = (statisticResultsMap.containsKey(mapKey)) ? valueInStatistics : valueInOther;

        if(mapKey.contains("Percent")) {
            mapData = StringBeautifier.beautifyString(mapData);
            mapData = (mapData.equals("Not specified")) ? mapData : mapData  + "%";
        } // If: is a percentage

        List<String> rowData = Arrays.asList(dataName, mapData);
        return HtmlModifier.printTableDataRow(rowData, tdAttribute, tdAttribute);
    } // printResultsTableRow()

    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl){
        support.removePropertyChangeListener(pcl);
    }
} // ElementResultsWriter Class

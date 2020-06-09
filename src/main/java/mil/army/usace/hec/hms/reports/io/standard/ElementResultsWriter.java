package mil.army.usace.hec.hms.reports.io.standard;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;

import java.util.*;

import static j2html.TagCreator.*;

public class ElementResultsWriter {
    private List<Element> elementList;
    private List<SummaryChoice> reportSummaryChoice;

    /* Constructors */
    private ElementResultsWriter(Builder builder){
        this.elementList = builder.elementList;
        this.reportSummaryChoice = builder.reportSummaryChoice;
    } // ElementResultsWriter Constructor
    public static class Builder{
        List<Element> elementList;
        List<SummaryChoice> reportSummaryChoice;

        public Builder elementList(List<Element> elementList){
            this.elementList = elementList;
            return this;
        } // 'elementList' constructor

        public Builder reportSummaryChoice(List<SummaryChoice> reportSummaryChoice) {
            this.reportSummaryChoice = reportSummaryChoice;
            return this;
        } // 'reportSummaryChoice' constructor

        public ElementResultsWriter build(){
            return new ElementResultsWriter(this);
        }
    } // Builder class: as ElementResultsWriter's Constructor
    public static Builder builder(){
        return new Builder();
    }

    /* Element Results Tables */
    Map<String, DomContent> printListResultsWriter() {
        if(reportSummaryChoice == null || !reportSummaryChoice.contains(SummaryChoice.ELEMENT_RESULTS_SUMMARY)) {
            return null;
        } // If: Report Summary Choice contains PARAMETER_SUMMARY

        Map<String, DomContent> elementResultsMap = new HashMap<>();
        String divAttribute = ".global-parameter";

        for(Element element: this.elementList) {
            String elementType = element.getElementInput().getElementType();
            DomContent tableDom = null;

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
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultResultsData(element));

        /* Precipitation Volume */
        rowDom = printResultsTableRow(element, "Precipitation Volume", "Precipitation Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Loss Volume */
        rowDom = printResultsTableRow(element, "Loss Volume", "Loss Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Excess Volume */
        rowDom = printResultsTableRow(element, "Excess Volume", "Excess Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Direct Runoff Volume */
        rowDom = printResultsTableRow(element, "Direct Flow Volume", "Direct Runoff Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Baseflow Volume */
        rowDom = printResultsTableRow(element, "Baseflow Volume", "Baseflow Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSubbasinResultsTable()
    private DomContent printReachResultsTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultResultsData(element));

        /* Maximum Inflow */
        rowDom = printResultsTableRow(element, "Maximum Inflow", "Peak Inflow (CFS)");
        globalParameterTableDom.add(rowDom);

        /* Inflow Volume */
        rowDom = printResultsTableRow(element, "Inflow Volume", "Inflow Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printReachResultsTable()
    private DomContent printJunctionResultsTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultResultsData(element));

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printJunctionResultsTable()
    private DomContent printSinkResultsTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultResultsData(element));

        /* Observed Flow Gage */
        rowDom = printResultsTableRow(element, "ObservedFlowGage", "Observed Flow Gage");
        globalParameterTableDom.add(rowDom);

        /* Observed Flow's Volume */
        rowDom = printResultsTableRow(element, "Observed Flow Volume", "Observed Flow Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Observed Flow's RMSE Stdev */
        rowDom = printResultsTableRow(element, "Observed Flow RMSE Stdev", "Observed Flow's RMSE Stdev");
        globalParameterTableDom.add(rowDom);

        /* Observed Flow's Percent Bias' */
        rowDom = printResultsTableRow(element, "Observed Flow Percent Bias", "Observed Flow's Percent Bias");
        globalParameterTableDom.add(rowDom);

        /* Observed Flow's Nash Sutcliffe */
        rowDom = printResultsTableRow(element, "Observed Flow Nash Sutcliffe", "Observed Flow's Nash Sutcliffe");
        globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSinkResultsTable()
    private DomContent printSourceResultsTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultResultsData(element));

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSourceResultsTable()
    private DomContent printReservoirResultsTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultResultsData(element));

        /* Peak Inflow */
        rowDom = printResultsTableRow(element, "Maximum Inflow", "Peak Inflow (CFS)");
        globalParameterTableDom.add(rowDom);

        /* Time of Peak Inflow */
        rowDom = printResultsTableRow(element, "Time of Maximum Inflow", "Time of Peak Inflow");
        globalParameterTableDom.add(rowDom);

        /* Inflow Volume */
        rowDom = printResultsTableRow(element, "Inflow Volume", "Inflow Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Peak Storage */
        rowDom = printResultsTableRow(element, "Maximum Storage", "Maximum Storage (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Peak Elevation */
        rowDom = printResultsTableRow(element, "Maximum Pool Elevation", "Peak Elevation (FT)");
        globalParameterTableDom.add(rowDom);

        /* Discharge Volume */
        rowDom = printResultsTableRow(element, "Outflow Volume", "Discharge Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Observed Pool Elevation Gage */
        rowDom = printResultsTableRow(element, "ObservedPoolElevationGage", "Observed Pool Elevation Gage");
        globalParameterTableDom.add(rowDom);

        /* Observed Peak Pool Elevation */
        rowDom = printResultsTableRow(element, "Maximum Observed Pool Elevation", "Observed Peak Pool Elevation (FT)");
        globalParameterTableDom.add(rowDom);

        /* RMSE Std Dev (Observed) */
        rowDom = printResultsTableRow(element, "Observed Pool Elevation RMSE Stdev", "Observed Pool Elevation RMSE Stdev");
        globalParameterTableDom.add(rowDom);

        /* Percent Bias (Observed) */
        rowDom = printResultsTableRow(element, "Observed Pool Elevation Percent Bias", "Observed Pool Elevation Percent Bias");
        globalParameterTableDom.add(rowDom);

        /* Time of Peak Pool Elevation (Observed) */
        rowDom = printResultsTableRow(element, "Time of Maximum Observed Pool Elevation", "Time of Maximum Observed Pool Elevation");
        globalParameterTableDom.add(rowDom);

        /* Nash-Sutcliffe (Observed) */
        rowDom = printResultsTableRow(element, "Observed Pool Elevation Nash Sutcliffe", "Observed Pool Elevation Nash Sutcliffe");
        globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = "Results" + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printReservoirResultsTable()
    private List<DomContent> printDefaultResultsData(Element element) {
        List<DomContent> defaultDomList = new ArrayList<>();
        DomContent rowDom;

        /* Peak Discharge */
        rowDom = printResultsTableRow(element, "Maximum Outflow", "Peak Discharge (CFS)");
        defaultDomList.add(rowDom);

        /* Time of Peak */
        rowDom = printResultsTableRow(element, "Time of Maximum Outflow", "Time of Peak Discharge");
        defaultDomList.add(rowDom);

        /* Volume */
        rowDom = printResultsTableRow(element, "Outflow Depth", "Volume (IN)");
        defaultDomList.add(rowDom);

        return defaultDomList;
    } // printDefaultResultsData()
    private DomContent printResultsTableRow(Element element, String mapKey, String dataName) {
        String tdAttribute = ".global-parameter";
        String mapData;
        Map<String, String> statisticResultsMap = element.getElementResults().getStatisticResultsMap();
        Map<String, String> otherResultsMap = element.getElementResults().getOtherResults();

        if(statisticResultsMap.containsKey(mapKey)) {
            mapData = statisticResultsMap.get(mapKey);
        } // If: the mapKey is in statisticResultsMap
        else if(otherResultsMap.containsKey(mapKey)) {
            mapData = otherResultsMap.get(mapKey);
        } // Else if: the mapKey is in otherResultsMap
        else {
            mapData = "Not specified";
        } // Else: mapKey is not found

        if(mapKey.contains("Percent")) {
            if(mapData.equals("Not specified")) {
                mapData = StringBeautifier.beautifyString(mapData);
            } // If: Not specified
            else {
                mapData = StringBeautifier.beautifyString(mapData) + "%";
            } // Else: Specified
        } // If: is a percentage

        List<String> rowData = Arrays.asList(dataName, mapData);
        DomContent rowDom = HtmlModifier.printTableDataRow(rowData, tdAttribute, tdAttribute);
        return rowDom;
    } // printResultsTableRow()
}

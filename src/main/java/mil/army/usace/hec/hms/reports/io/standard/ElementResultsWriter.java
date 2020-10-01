package mil.army.usace.hec.hms.reports.io.standard;

import hec.heclib.util.Heclib;
import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.FigureCreator;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;
import mil.army.usace.hec.hms.reports.util.ValidCheck;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Page;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class ElementResultsWriter {
    private List<Element> elementList;
    private List<String> chosenPlots;
    private List<SummaryChoice> reportSummaryChoice;
    private PropertyChangeSupport support;

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
        String elementName = elementResults.getName();
        DomContent timeSeriesResults = printTimeSeriesResult(elementResults.getTimeSeriesResults(), elementName);
        elementResultsDomList.add(timeSeriesResults);

        return div(attrs(".element-results"), elementResultsDomList.toArray(new DomContent[]{}));
    } // printElementResults()

    /* TimeSeries Results */
    private DomContent printTimeSeriesResult(List<TimeSeriesResult> timeSeriesResultList, String elementName) {
        List<DomContent> timeSeriesPlotDomList = new ArrayList<>();
        List<DomContent> maxPlotDom = new ArrayList<>();
        int maxPlotsPerPage = 2;
        Map<String, List<TimeSeriesResult>> sameTypeMap = new LinkedHashMap<>();

        Map<String, TimeSeriesResult> timeSeriesResultMap = timeSeriesResultList.stream()
                .filter(individual -> ValidCheck.validTimeSeriesPlot(individual.getType(), this.chosenPlots))
                .collect(Collectors.toMap(TimeSeriesResult::getType, TimeSeriesResult::getTimeSeriesResult,
                        (value1, value2) -> {
                            sameTypeMap.computeIfAbsent(value1.getType(), k -> new ArrayList<>());
                            sameTypeMap.get(value1.getType()).add(value1);
                            sameTypeMap.get(value1.getType()).add(value2);
                            return value2;
                        }));

        /* Remove Duplicate Types in timeSeriesResultMap */
        sameTypeMap.keySet().forEach(timeSeriesResultMap::remove);
        /* Remove Duplicate Unit Types in sameTypeMap */
        sameTypeMap.keySet().forEach(e -> sameTypeMap.replace(e, sameTypeMap.get(e).stream().distinct().collect(Collectors.toList())));

        Map<String, Map<String, TimeSeriesResult>> combinedPlotMap = getCombinedPlotName(timeSeriesResultMap);
        // Case: Combined Plot (Outflow and Precipitation, or others)
        if(!combinedPlotMap.isEmpty()) {
            for(String combinedPlotName : combinedPlotMap.keySet()) {
                Map<String, TimeSeriesResult> combinedTsrMap = combinedPlotMap.get(combinedPlotName);
                // Plot the corresponding Combined Plot
                DomContent tsrDom = printTimeSeriesCombinedPlot(combinedTsrMap, combinedPlotName, elementName);
                // Divide the plots by only having Max number of plots per page
                if(maxPlotDom.size() < maxPlotsPerPage) {
                    if(tsrDom != null) { maxPlotDom.add(tsrDom); }
                } // If: we haven't reached 2 plots per page
                else {
                    timeSeriesPlotDomList.add(div(attrs(".max-plot"), maxPlotDom.toArray(new DomContent[]{})));
                    maxPlotDom.clear();
                    if(tsrDom != null) { maxPlotDom.add(tsrDom); }
                } // Else: we have reached 2 plots per page
                // Remove the used plots from the timeSeriesResultMap
                for(String usedPlot : combinedTsrMap.keySet()) {
                    timeSeriesResultMap.remove(usedPlot);
                } // Loop: to remove used plots
            } // Loop: To plot each Combined Plot
        } // If: There is a combined plot

        // Case: Single Plots (one dataset for each plot)
        for(String key : timeSeriesResultMap.keySet()) {
            List<TimeSeriesResult> timeSeriesResults = Collections.singletonList(timeSeriesResultMap.get(key));
            DomContent tsrDom = printTimeSeriesPlot(timeSeriesResults, elementName);
            if(maxPlotDom.size() < maxPlotsPerPage) {
                if(tsrDom != null) { maxPlotDom.add(tsrDom); }
            } // If: we haven't reached 2 plots per page
            else {
                timeSeriesPlotDomList.add(div(attrs(".max-plot"), maxPlotDom.toArray(new DomContent[]{})));
                maxPlotDom.clear();
                if(tsrDom != null) { maxPlotDom.add(tsrDom); }
            } // Else: we have reached 2 plots per page
        } // Loop: to print each single plot

        // Case: Single Plots with Multiple Datasets for each plot
        for(String key : sameTypeMap.keySet()) {
            DomContent tsrDom = printTimeSeriesPlot(sameTypeMap.get(key), elementName);
            if(maxPlotDom.size() < maxPlotsPerPage) { if(tsrDom != null) { maxPlotDom.add(tsrDom); } }
        } // Loop: to print each single plot with multiple datasets

        if(!maxPlotDom.isEmpty()) {
            timeSeriesPlotDomList.add(div(attrs(".non-max-plot"), maxPlotDom.toArray(new DomContent[]{})));
        } // If: We haven't maxed out number of plots in a page yet

        return div(attrs(".group-plot"), timeSeriesPlotDomList.toArray(new DomContent[]{}));
    } // printTimeSeriesResult

    private DomContent printTimeSeriesPlot(List<TimeSeriesResult> timeSeriesResultList, String elementName) {
        // Configure Plot Settings
        TimeSeriesResult baseResult = timeSeriesResultList.get(0);
        String plotName = baseResult.getType();
        Table plotTable = getTimeSeriesTable(timeSeriesResultList, plotName);
        if(plotTable == null) { return null; } // No PlotTable

        String xAxisTit = "Time";
        String yAxisTit = baseResult.getUnitType() + " (" + baseResult.getUnit() + ")";

        // Create Plot
        Figure timeSeriesFigure = FigureCreator.createTimeSeriesPlot(plotName, plotTable, xAxisTit, yAxisTit);
        String plotDivName = StringBeautifier.getPlotDivName(elementName, plotName);
        Page page = Page.pageBuilder(timeSeriesFigure, plotDivName).build();

        // Extract Plot's Javascript
        String plotHtml = page.asJavascript();
        DomContent domContent = HtmlModifier.extractPlotlyJavascript(plotHtml);

        return div(attrs(".single-plot"), domContent);
    } // printTimeSeriesPlot()

    private Table getTimeSeriesTable(List<TimeSeriesResult> timeSeriesResultList, String tableName) {
        List<Column<?>> columnList = new ArrayList<>();

        /* Get TimeSeries Column */
        List<ZonedDateTime> zonedDateTimeList = timeSeriesResultList.get(0).getTimes();
        List<LocalDateTime> localDateTimeList = zonedDateTimeList.stream().map(ZonedDateTime::toLocalDateTime).collect(Collectors.toList());

        DateTimeColumn dateTimeColumn = DateTimeColumn.create("Time", localDateTimeList);
        columnList.add(dateTimeColumn);

        /* Get Value Array Columns */
        for(TimeSeriesResult timeSeriesResult : timeSeriesResultList) {
            double[] valueArray = timeSeriesResult.getValues();
            for(int i = 0; i < valueArray.length; i++) {
                if(valueArray[i] == Heclib.UNDEFINED_DOUBLE)
                    valueArray[i] = Double.NaN;
            } // Converting HEC Missing Data to NaN
            if(valueArray.length == 0) { return null; } // TimeSeries without values

            String columnName = timeSeriesResult.getUnitType();
            DoubleColumn valueColumn = DoubleColumn.create(columnName, valueArray);
            if(!columnList.contains(valueColumn)) { columnList.add(valueColumn); }
        } // Loop: through all timeSeriesResult

        /* Create a TimeSeriesTable with Columns from columnList */
        Table timeSeriesTable = Table.create(tableName, columnList);

        return timeSeriesTable;
    } // timeSeriesToCsv

    /* TimeSeries Custom Plots */
    private DomContent printTimeSeriesCombinedPlot(Map<String, TimeSeriesResult> tsrMap, String plotName, String elementName) {
        DomContent combinedPlotDom = null;

        // Get the DomContent of the Plot
        if(plotName.equals("Precipitation and Outflow"))
            combinedPlotDom = getPrecipOutflowPlot(tsrMap, plotName, elementName);
        else if(plotName.equals("Outflow and Observed Flow"))
            combinedPlotDom = getOutflowObservedFlowPlot(tsrMap, plotName, elementName);

        if(combinedPlotDom == null) { return null; }

        return div(attrs(".single-plot"), combinedPlotDom);
    } // printTimeSeriesCombinedPlot()

    private DomContent getPrecipOutflowPlot(Map<String, TimeSeriesResult> tsrMap, String plotName, String elementName) {
        List<TimeSeriesResult> topPlots = Arrays.asList(tsrMap.get("Precipitation"), tsrMap.get("Excess Precipitation"));
        List<TimeSeriesResult> bottomPlots = Collections.singletonList(tsrMap.get("Outflow"));

        // Creating Tables for both Top Plots and Bottom Plots
        List<Table> topPlotTables = new ArrayList<>(), bottomPlotTables = new ArrayList<>();
        for(TimeSeriesResult tsr : topPlots) {
            List<TimeSeriesResult> tsrList = Collections.singletonList(tsr);
            Table plotTable = getTimeSeriesTable(tsrList, tsr.getType());
            if(plotTable == null) { return null; }
            topPlotTables.add(plotTable);
        } // Loop: to create Tables for Top Plots
        for(TimeSeriesResult tsr : bottomPlots) {
            List<TimeSeriesResult> tsrList = Collections.singletonList(tsr);
            Table plotTable = getTimeSeriesTable(tsrList, tsr.getType());
            if(plotTable == null) { return null; }
            bottomPlotTables.add(plotTable);
        } // Loop: to create Tables for Bottom Plots

        // Setting Plot's configurations
        String xAxisTitle = "Time";
        String y1AxisTitle = bottomPlots.get(0).getUnitType() + " (" + bottomPlots.get(0).getUnit() + ")";
        String y2AxisTitle = topPlots.get(0).getUnitType() + " (" +  topPlots.get(0).getUnit() + ")";
        String divName = StringBeautifier.getPlotDivName(elementName, plotName);

        // Create Plot
        Figure timeSeriesFigure = FigureCreator.createPrecipOutflowPlot(plotName, topPlotTables, bottomPlotTables, xAxisTitle, y1AxisTitle, y2AxisTitle);
        Page page = Page.pageBuilder(timeSeriesFigure, divName).build();

        // Extract Plot's Javascript
        String plotHtml = page.asJavascript();
        DomContent domContent = HtmlModifier.extractPlotlyJavascript(plotHtml);

        return domContent;
    } // getPrecipOutflowPlot()

    private DomContent getOutflowObservedFlowPlot(Map<String, TimeSeriesResult> tsrMap, String plotName, String elementName) {
        TimeSeriesResult outflowPlot = tsrMap.get("Outflow"), observedFlowPlot = tsrMap.get("Observed Flow");
        Table outflowTable = getTimeSeriesTable(Collections.singletonList(outflowPlot), "Outflow");
        Table observedFlowTable = getTimeSeriesTable(Collections.singletonList(observedFlowPlot), "Observed Flow");
        List<Table> plotList = Arrays.asList(outflowTable, observedFlowTable);

        if(outflowTable == null || observedFlowTable == null) { return null; }

        // Setting Plot's configurations
        String xAxisTitle = "Time";
        String yAxisTitle = outflowPlot.getUnitType();
        String divName = StringBeautifier.getPlotDivName(elementName, plotName);

        // Create Plot
        Figure timeSeriesFigure = FigureCreator.createOutflowObservedFlowPlot(plotName, plotList, xAxisTitle, yAxisTitle);
        Page page = Page.pageBuilder(timeSeriesFigure, divName).build();

        // Extract Plot's Javascript
        String plotHtml = page.asJavascript();
        DomContent domContent = HtmlModifier.extractPlotlyJavascript(plotHtml);

        return domContent;
    } // getOutflowObservedFlowPlot()

    private Map<String, Map<String, TimeSeriesResult>> getCombinedPlotName(Map<String, TimeSeriesResult> tsrMap) {
        Map<String, Map<String, TimeSeriesResult>> combinedPlotMap = new HashMap<>();

        if(tsrMap.containsKey("Precipitation") && tsrMap.containsKey("Outflow") && tsrMap.containsKey("Excess Precipitation")) {
            Map<String, TimeSeriesResult> combinedMap = new HashMap<>();
            combinedMap.put("Precipitation", tsrMap.get("Precipitation"));
            combinedMap.put("Outflow", tsrMap.get("Outflow"));
            combinedMap.put("Excess Precipitation", tsrMap.get("Excess Precipitation"));
            String plotName = "Precipitation and Outflow";
            combinedPlotMap.put(plotName, combinedMap);
        } // If: CombinedPlot is a 'Precipitation and Outflow'

        if(tsrMap.containsKey("Outflow") && tsrMap.containsKey("Observed Flow")) {
            Map<String, TimeSeriesResult> combinedMap = new HashMap<>();
            combinedMap.put("Observed Flow", tsrMap.get("Observed Flow"));
            combinedMap.put("Outflow", tsrMap.get("Outflow"));
            String plotName = "Outflow and Observed Flow";
            combinedPlotMap.put(plotName, combinedMap);
        } // If: CombinedPlot is a 'Outflow and Observed Flow plot'

        return combinedPlotMap;
    } // getCombinedPlotName()

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

    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl){
        support.removePropertyChangeListener(pcl);
    }
} // ElementResultsWriter Class

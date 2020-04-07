package mil.army.usace.hec.hms.reports.io;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.*;
import mil.army.usace.hec.hms.reports.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Page;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class HmsReportWriter extends ReportWriter {
    HmsReportWriter(Builder builder) {
        super(builder);
    }

    @Override
    public void write() {
        /* Parse elements */
        BasinParser parser = BasinParser.builder()
                .pathToBasinInputFile(this.pathToInput.toAbsolutePath().toString())
                .pathToBasinResultsFile(this.pathToResult.toAbsolutePath().toString())
                .pathToProjectDirectory(this.projectDirectory.toAbsolutePath().toString())
                .build();

        List<Element> elementList = parser.getElements();

        /* HTML Layout */
        String htmlOutput = html(
                head(   title("Standardized Report"),
                        link().withRel("stylesheet").withHref("style.css"),
                        script().withSrc("https://cdn.plot.ly/plotly-latest.min.js")),
                body(   printGlobalSummary(elementList),
                        printListGlobalParameter(elementList),
                        printElementList(elementList))
        ).renderFormatted();
        /* Writing to HTML output file */
        HtmlModifier.writeToFile(this.pathToDestination.toString(), htmlOutput);
    } // write()

    /* Global Summary Table */
    private DomContent printGlobalSummary(List<Element> elementList) {
        if(reportSummaryChoice == null || !reportSummaryChoice.contains(SummaryChoice.GLOBAL_SUMMARY)) {
            return null;
        } // If: Report Summary Choice contains GLOBAL_SUMMARY

        List<DomContent> globalSummaryDomList = new ArrayList<>();
        String tdAttribute = ".global-summary";

        for(Element element : elementList) {
            List<String> rowData = new ArrayList<>();
            rowData.add(element.getName()); // Element Name
            rowData.add(element.getElementResults().getOtherResults().get("DrainageArea")); // Drainage Area
            rowData.add(element.getElementResults().getStatisticResultsMap().get("Maximum Outflow")); // Peak Discharge
            rowData.add(element.getElementResults().getStatisticResultsMap().get("Time of Maximum Outflow")); // Time of Peak
            rowData.add(element.getElementResults().getStatisticResultsMap().get("Outflow Depth")); // Volume

            rowData.replaceAll(t -> Objects.isNull(t) ? "N/A" : t);
            rowData.replaceAll(t -> t.equals("") ? "Not specified" : t);

            DomContent rowDom = HtmlModifier.printTableDataRow(rowData, tdAttribute, tdAttribute);
            globalSummaryDomList.add(rowDom);
        }

        /* Adding Head of the Table if there is a table */
        if(!globalSummaryDomList.isEmpty()) {
            DomContent head = HtmlModifier.printTableHeadRow(Arrays.asList("Hydrologic Element", "Drainage Area (MI2)",
                    "Peak Discharge (CFS)", "Time of Peak", "Volume (IN)"), tdAttribute, tdAttribute);
            globalSummaryDomList.add(0, head); // Add to front
            globalSummaryDomList.add(0, caption("Global Summary"));
        } // If: There is a table

        return table(attrs(tdAttribute), globalSummaryDomList.toArray(new DomContent[]{}));
    } // printGlobalSummary()
    /* ---------------------------------------------------------------------------------------------------------- */
    /* Global Parameter Tables */
    private DomContent printListGlobalParameter(List<Element> elementList) {
        if(reportSummaryChoice == null || !reportSummaryChoice.contains(SummaryChoice.PARAMETER_SUMMARY)) {
            return null;
        } // If: Report Summary Choice contains PARAMETER_SUMMARY

        List<DomContent> globalParameterDomList = new ArrayList<>();
        String divAttribute = ".global-parameter";

        for(Element element: elementList) {
            String elementType = element.getElementInput().getElementType();
            DomContent tableDom = null;

            switch(elementType) {
                case "SUBBASIN":
                    tableDom = printSubbasinParameterTable(element);
                    break;
                case "REACH":
                    tableDom = printReachParameterTable(element);
                    break;
                case "JUNCTION":
                    tableDom = printJunctionParameterTable(element);
                    break;
                case "SINK":
                    tableDom = printSinkParameterTable(element);
                    break;
                case "SOURCE":
                    tableDom = printSourceParameterTable(element);
                    break;
                case "RESERVOIR":
                    tableDom = printReservoirParameterTable(element);
                    break;
                default:
                    System.out.println("This element type is not supported: " + elementType);
            } // Switch case: for element's type

            globalParameterDomList.add(tableDom);
        } // Loop: through all elements to print its global parameter table

        return div(attrs(divAttribute), globalParameterDomList.toArray(new DomContent[]{}));
    } // printListGlobalParameter

    private DomContent printSubbasinParameterTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultParameterData(element));

        /* Precipitation Volume */
        rowDom = printParameterTableRow(element, "Precipitation Volume", "Precipitation Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Loss Volume */
        rowDom = printParameterTableRow(element, "Loss Volume", "Loss Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Excess Volume */
        rowDom = printParameterTableRow(element, "Excess Volume", "Excess Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Direct Runoff Volume */
        rowDom = printParameterTableRow(element, "Direct Flow Volume", "Direct Runoff Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Baseflow Volume */
        rowDom = printParameterTableRow(element, "Baseflow Volume", "Baseflow Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = elementType + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSubbasinParameterTable()
    private DomContent printReachParameterTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultParameterData(element));

        /* Maximum Inflow */
        rowDom = printParameterTableRow(element, "Maximum Inflow", "Peak Inflow (CFS)");
        globalParameterTableDom.add(rowDom);

        /* Inflow Volume */
        rowDom = printParameterTableRow(element, "Inflow Volume", "Inflow Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = elementType + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printReachParameterTable()
    private DomContent printJunctionParameterTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultParameterData(element));

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = elementType + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printJunctionParameterTable()
    private DomContent printSinkParameterTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultParameterData(element));

        /* Observed Flow Gage */
        rowDom = printParameterTableRow(element, "ObservedFlowGage", "Observed Flow Gage");
        globalParameterTableDom.add(rowDom);

        /* Observed Flow's Volume */
        rowDom = printParameterTableRow(element, "Observed Flow Volume", "Observed Flow Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Observed Flow's RMSE Stdev */
        rowDom = printParameterTableRow(element, "Observed Flow RMSE Stdev", "Observed Flow's RMSE Stdev");
        globalParameterTableDom.add(rowDom);

        /* Observed Flow's Percent Bias' */
        rowDom = printParameterTableRow(element, "Observed Flow Percent Bias", "Observed Flow's Percent Bias");
        globalParameterTableDom.add(rowDom);

        /* Observed Flow's Nash Sutcliffe */
        rowDom = printParameterTableRow(element, "Observed Flow Nash Sutcliffe", "Observed Flow's Nash Sutcliffe");
        globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = elementType + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSinkParameterTable()
    private DomContent printSourceParameterTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultParameterData(element));

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = elementType + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printSourceParameterTable()
    private DomContent printReservoirParameterTable(Element element) {
        List<DomContent> globalParameterTableDom = new ArrayList<>();
        String tdAttribute = ".global-parameter";
        DomContent rowDom;

        /* Default Values */
        globalParameterTableDom.addAll(printDefaultParameterData(element));

        /* Peak Inflow */
        rowDom = printParameterTableRow(element, "Maximum Inflow", "Peak Inflow (CFS)");
        globalParameterTableDom.add(rowDom);

        /* Time of Peak Inflow */
        rowDom = printParameterTableRow(element, "Time of Maximum Inflow", "Time of Peak Inflow");
        globalParameterTableDom.add(rowDom);

        /* Inflow Volume */
        rowDom = printParameterTableRow(element, "Inflow Volume", "Inflow Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Peak Storage */
        rowDom = printParameterTableRow(element, "Maximum Storage", "Maximum Storage (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Peak Elevation */
        rowDom = printParameterTableRow(element, "Maximum Pool Elevation", "Peak Elevation (FT)");
        globalParameterTableDom.add(rowDom);

        /* Discharge Volume */
        rowDom = printParameterTableRow(element, "Outflow Volume", "Discharge Volume (AC-FT)");
        globalParameterTableDom.add(rowDom);

        /* Observed Pool Elevation Gage */
        rowDom = printParameterTableRow(element, "ObservedPoolElevationGage", "Observed Pool Elevation Gage");
        globalParameterTableDom.add(rowDom);

        /* Observed Peak Pool Elevation */
        rowDom = printParameterTableRow(element, "Maximum Observed Pool Elevation", "Observed Peak Pool Elevation (FT)");
        globalParameterTableDom.add(rowDom);

        /* RMSE Std Dev (Observed) */
        rowDom = printParameterTableRow(element, "Observed Pool Elevation RMSE Stdev", "Observed Pool Elevation RMSE Stdev");
        globalParameterTableDom.add(rowDom);

        /* Percent Bias (Observed) */
        rowDom = printParameterTableRow(element, "Observed Pool Elevation Percent Bias", "Observed Pool Elevation Percent Bias");
        globalParameterTableDom.add(rowDom);

        /* Time of Peak Pool Elevation (Observed) */
        rowDom = printParameterTableRow(element, "Time of Maximum Observed Pool Elevation", "Time of Maximum Observed Pool Elevation");
        globalParameterTableDom.add(rowDom);

        /* Nash-Sutcliffe (Observed) */
        rowDom = printParameterTableRow(element, "Observed Pool Elevation Nash Sutcliffe", "Observed Pool Elevation Nash Sutcliffe");
        globalParameterTableDom.add(rowDom);

        if(!globalParameterTableDom.isEmpty()) {
            String elementType = StringBeautifier.beautifyString(element.getElementInput().getElementType());
            String captionTitle = elementType + ": " + element.getName();
            globalParameterTableDom.add(0, caption(captionTitle));
        } // If: table is not empty

        return table(attrs(tdAttribute), globalParameterTableDom.toArray(new DomContent[]{}));
    } // printReservoirParameterTable()
    private DomContent printElementList(List<Element> elementList) {
        List<DomContent> elementDomList = new ArrayList<>();

        /* For each element, print: ElementInput and ElementResults */
        for(Element element : elementList) {
            List<DomContent> elementDom = new ArrayList<>(); // Holds elementInput and elementResult
            // Getting ElementInput DomContent
            ElementInput elementInput = element.getElementInput();
            DomContent elementInputDom = printElementInput(elementInput);
            if(elementInputDom != null) { elementDom.add(elementInputDom); }
            // Getting ElementResults DomContent
            ElementResults elementResults = element.getElementResults();
            DomContent elementResultsDom = printElementResults(elementResults);
            if(elementResultsDom != null) { elementDom.add(elementResultsDom); }
            // Creating a 'div', 'class: element'
            elementDomList.add(div(attrs(".element"), elementDom.toArray(new DomContent[]{})));
        } // Loop: through elementList to print each Element

        return main(elementDomList.toArray(new DomContent[]{}));
    } // printElementList()
    private List<DomContent> printDefaultParameterData(Element element) {
        List<DomContent> defaultDomList = new ArrayList<>();
        DomContent rowDom;

        /* Peak Discharge */
        rowDom = printParameterTableRow(element, "Maximum Outflow", "Peak Discharge (CFS)");
        defaultDomList.add(rowDom);

        /* Time of Peak */
        rowDom = printParameterTableRow(element, "Time of Maximum Outflow", "Time of Peak Discharge");
        defaultDomList.add(rowDom);

        /* Volume */
        rowDom = printParameterTableRow(element, "Outflow Depth", "Volume (IN)");
        defaultDomList.add(rowDom);

        return defaultDomList;
    } // printDefaultParameterData()
    private DomContent printParameterTableRow(Element element, String mapKey, String dataName) {
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
            mapData = mapData + "%";
        } // If: is a percentage

        List<String> rowData = Arrays.asList(dataName, mapData);
        DomContent rowDom = HtmlModifier.printTableDataRow(rowData, tdAttribute, tdAttribute);
        return rowDom;
    } // printParameterTableRow()

    /* ---------------------------------------------------------------------------------------------------------- */
    /* Element Input */
    private DomContent printElementInput(ElementInput elementInput) {
        List<DomContent> elementInputDomList = new ArrayList<>();

        /* For each elementInput, print: Name, ElementType, and Processes */
        String elementName = elementInput.getName();
        String elementType = StringBeautifier.beautifyString(elementInput.getElementType());
        DomContent elementNameAndType = h2(elementType + ": " + elementName);
        elementInputDomList.add(elementNameAndType);

        List<Process> processList = elementInput.getProcesses();
        List<Process> processSingle = new ArrayList<>(); // For Processes without Parameters
        List<Process> processTable = new ArrayList<>();  // For Processes with Parameters to make a table

        for(Process process : processList) {
            if(process.getParameters().isEmpty())
                processSingle.add(process);
            else
                processTable.add(process);
        } // Loop: Separate processes between Single and Table

        DomContent processSingleDom = printSingleProcesses(processSingle);
        if(processSingleDom != null) { elementInputDomList.add(processSingleDom); }
        DomContent processTableDom = printTableProcesses(processTable);
        if(processTableDom != null) { elementInputDomList.add(processTableDom); }
        if(elementInputDomList.isEmpty()) { return null; } // If: There is no elementInput, return null

        return div(attrs(".element-input"), elementInputDomList.toArray(new DomContent[]{}));
    } // printElementInput()
    private DomContent printSingleProcesses(List<Process> singleProcesses) {
        List<DomContent> singleProcessesDomList = new ArrayList<>();

        for(Process process : singleProcesses) {
            if(ValidCheck.unnecessarySingleProcesses().contains(process.getName())) {
                continue;
            } // Skipping unnecessary processes

            String processName = StringBeautifier.beautifyString(process.getName());
            String processValue = StringBeautifier.beautifyString(process.getValue());
            DomContent singleDom = join(b(processName), ":", processValue, br());
            singleProcessesDomList.add(singleDom);
        } // Loop: through each single process

        if(singleProcessesDomList.isEmpty()) { return null; } // Return null if there is no single processes

        return p(attrs(".single-process"), singleProcessesDomList.toArray(new DomContent[]{})); // Return in the format of a 'paragraph'
    } // printSingleProcesses()
    private DomContent printTableProcesses(List<Process> tableProcesses) {
        List<DomContent> tableProcessesDomList = new ArrayList<>();

        for(Process process : tableProcesses) {
            String reformatName = StringBeautifier.beautifyString(process.getName());
            List<Parameter> parameterList = process.getParameters();
            DomContent tableDom  = printParameterTable(parameterList, reformatName); // The Table of Parameters
            tableProcessesDomList.add(tableDom);
        } // Loop: through each table process

        if(tableProcessesDomList.isEmpty()) { return null; } // Return null if there is no table processes

        return div(attrs(".table-process"), tableProcessesDomList.toArray(new DomContent[]{})); // Return a list of tables (for processes)
    } // printTableProcesses()
    private DomContent printParameterTable(List<Parameter> parameterList, String tableCaption) {
        List<DomContent> parameterDom = new ArrayList<>();
        List<Parameter> nestedParameterList = new ArrayList<>();
        String tdAttribute = ".element-nested";

        if(!tableCaption.equals("")) {
            parameterDom.add(caption(tableCaption));
            tdAttribute = ".element-non-nested";
        } // If: has tableCaption, add caption

        for(Parameter parameter : parameterList) {
            if(!parameter.getSubParameters().isEmpty()) {
                nestedParameterList.add(parameter);
            } // If: Parameter contains SubParameters
            else {
                List<String> tableRow = Arrays.asList(parameter.getName(), parameter.getValue());
                DomContent row = HtmlModifier.printTableDataRow(tableRow, tdAttribute, tdAttribute);
                parameterDom.add(row);
            } // Else: Parameter does not contain SubParameters
        } // Loop: through Parameter List

        /* Tables within a table */
        boolean nestedTable = false;
        for(Parameter nestedParameter : nestedParameterList) {
            /* Note: Nested Parameter's table should be in a row */
            List<DomContent> nestedParameterDom = new ArrayList<>();
            String reformatName = StringBeautifier.beautifyString(nestedParameter.getName());
            DomContent tableName = td(reformatName);
            List<Parameter> subParameters = nestedParameter.getSubParameters();
            DomContent subParameterTable = td(attrs(".nested-table"), printParameterTable(subParameters,""));
            nestedParameterDom.add(tableName);
            nestedParameterDom.add(subParameterTable);
            parameterDom.add(tr(nestedParameterDom.toArray(new DomContent[]{})));
            // Return table of class 'nested_process' if nested
            nestedTable = true;
        } // Loop: through nested parameter List

        if(nestedTable) {
            return table(attrs(".nested"), parameterDom.toArray(new DomContent[]{}));
        } // If: Nested Table

        return table(attrs(".single"), parameterDom.toArray(new DomContent[]{})); // Table of Parameters
    } // printParameterTable()

    /* Element Results */
    private DomContent printElementResults(ElementResults elementResults) {
        List<DomContent> elementResultsDomList = new ArrayList<>();

        /* Get Statistic Results Dom */
        DomContent statisticResults = printStatisticResult(elementResults.getStatisticResults());
        elementResultsDomList.add(statisticResults);
        /* Get TimeSeries Results Dom */
        String elementName = elementResults.getName();
        DomContent timeSeriesResults = printTimeSeriesResult(elementResults.getTimeSeriesResults(), elementName);
        elementResultsDomList.add(timeSeriesResults);

        return div(attrs(".element-results"), elementResultsDomList.toArray(new DomContent[]{}));
    } // printElementResults()
    /* Statistic Results */
    private DomContent printStatisticResult(List<StatisticResult> statisticResultList) {
        List<DomContent> statisticResultDomList = new ArrayList<>();

        /* Adding Data of the Table */
        for(StatisticResult statisticResult : statisticResultList) {
            String statisticName = statisticResult.getName();
            /* Skip unnecessary StatisticResults */
            if(!ValidCheck.validStatisticResult().contains(statisticName)) { continue; }
            /* Print out StatisticResult Table */
            List<String> rowContent = Arrays.asList(statisticName, statisticResult.getValue(), statisticResult.getUnits());
            DomContent row = HtmlModifier.printTableDataRow(rowContent, ".statistic", ".statistic");
            statisticResultDomList.add(row);
        } // Loop: to get DomContent rows for table

        /* Adding Head of the Table if there is a table */
        if(!statisticResultDomList.isEmpty()) {
            DomContent head = HtmlModifier.printTableHeadRow(Arrays.asList("Name", "Value", "Unit"), ".statistic", ".statistic");
            statisticResultDomList.add(0, head); // Add to front
            statisticResultDomList.add(0, caption("Statistics"));
        } // If: There is a table

        return table(attrs(".statistic-result"), statisticResultDomList.toArray(new DomContent[]{}));
    } // printStatisticResults()
    /* TimeSeries Results */
    private DomContent printTimeSeriesResult(List<TimeSeriesResult> timeSeriesResultList, String elementName) {
        List<DomContent> timeSeriesPlotDomList = new ArrayList<>();
        List<DomContent> maxPlotDom = new ArrayList<>();
        int maxPlotsPerPage = 2;

        Map<String, TimeSeriesResult> timeSeriesResultMap = timeSeriesResultList.stream()
                .filter(individual -> ValidCheck.validTimeSeriesPlot(individual.getType(), this.chosenPlots))
                .collect(Collectors.toMap(TimeSeriesResult::getType, TimeSeriesResult::getTimeSeriesResult));

        Map<String, Map<String, TimeSeriesResult>> combinedPlotMap = getCombinedPlotName(timeSeriesResultMap);

        // Case: Combined Plot (Outflow and Precipitation, or others)
        if(!combinedPlotMap.isEmpty()) {
            for(String combinedPlotName : combinedPlotMap.keySet()) {
                Map<String, TimeSeriesResult> combinedTsrMap = combinedPlotMap.get(combinedPlotName);
                // Plot the corresponding Combined Plot
                DomContent tsrDom = printTimeSeriesCombinedPlot(combinedTsrMap, combinedPlotName, elementName);
                // Divide the plots by only having Max number of plots per page
                if(maxPlotDom.size() < maxPlotsPerPage) {
                    maxPlotDom.add(tsrDom);
                } // If: we haven't reached 2 plots per page
                else {
                    timeSeriesPlotDomList.add(div(attrs(".max-plot"), maxPlotDom.toArray(new DomContent[]{})));
                    maxPlotDom.clear();
                    maxPlotDom.add(tsrDom);
                } // Else: we have reached 2 plots per page
                // Remove the used plots from the timeSeriesResultMap
                for(String usedPlot : combinedTsrMap.keySet()) {
                    timeSeriesResultMap.remove(usedPlot);
                } // Loop: to remove used plots
            } // Loop: To plot each Combined Plot
        } // If: There is a combined plot

        for(String key : timeSeriesResultMap.keySet()) {
            DomContent tsrDom = printTimeSeriesPlot(timeSeriesResultMap.get(key), elementName);
            if(maxPlotDom.size() < maxPlotsPerPage) {
                maxPlotDom.add(tsrDom);
            } // If: we haven't reached 2 plots per page
            else {
                timeSeriesPlotDomList.add(div(attrs(".max-plot"), maxPlotDom.toArray(new DomContent[]{})));
                maxPlotDom.clear();
                maxPlotDom.add(tsrDom);
            } // Else: we have reached 2 plots per page
        } // Loop: to print each single plot

        if(!maxPlotDom.isEmpty()) {
            timeSeriesPlotDomList.add(div(attrs(".non-max-plot"), maxPlotDom.toArray(new DomContent[]{})));
        } // If: We haven't maxed out number of plots in a page yet

        return div(attrs(".group-plot"), timeSeriesPlotDomList.toArray(new DomContent[]{}));
    } // printTimeSeriesResult
    private DomContent printTimeSeriesPlot(TimeSeriesResult timeSeriesResult, String elementName) {
        // Configure Plot settings
        String[] columnNames = {"Time", "Value"};
        Table timeSeriesTable = getTimeSeriesTable(timeSeriesResult, columnNames);
        String plotName = timeSeriesResult.getType();
        String xAxisTitle = "Time";
        String yAxisTitle = timeSeriesResult.getUnitType() + " (" + timeSeriesResult.getUnit() + ")";

        // Create Plot
        Figure timeSeriesFigure = FigureCreator.createTimeSeriesPlot(plotName, timeSeriesTable, xAxisTitle, yAxisTitle);
        String plotDivName = StringBeautifier.getPlotDivName(elementName, plotName);
        Page page = Page.pageBuilder(timeSeriesFigure, plotDivName).build();

        // Extract Plot's Javascript
        String plotHtml = page.asJavascript();
        DomContent domContent = HtmlModifier.extractPlotlyJavascript(plotHtml);

        return div(attrs(".single-plot"), domContent);
    } // printTimeSeriesPlot()
    private Table getTimeSeriesTable(TimeSeriesResult timeSeriesResult, String[] columnNames) {
        Table timeSeriesPlot = null;

        /* Get readable date format */
        List<ZonedDateTime> zonedDateTimeList = timeSeriesResult.getTimes();
        List<String> reformattedTimeList = new ArrayList<>();
        String dateFormat = "yyyy-MM-dd kk:mm:ss";
        for(ZonedDateTime zonedDateTime : zonedDateTimeList) {
            String reformattedDate = TimeConverter.toString(zonedDateTime, dateFormat);
            reformattedTimeList.add(reformattedDate);
        } // Loop: to convert ZonedDateTime to acceptable format

        /* Get readable value */
        double[] valueArray = timeSeriesResult.getValues();
        List<String> reformattedValueList = new ArrayList<>();
        for(double value : valueArray) {
            String reformattedValue = Double.toString(value);
            reformattedValueList.add(reformattedValue);
        } // Loop: to convert valueArray to String

        /* Writing time and value out to a csv file */
        try {
            File outputFile = new File("timeSeriesResult.csv");
            FileWriter writer = new FileWriter(outputFile);
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(columnNames));

            for(int i = 0; i < reformattedTimeList.size(); i++) {
                String time = reformattedTimeList.get(i);
                String value = reformattedValueList.get(i);
                printer.printRecord(time, value);
            } // Loop: to print out every time & value pair

            // Flush Printer, and Close Writer
            printer.flush();
            writer.close();

            // Read in CSV file to get a Table
            timeSeriesPlot = Table.read().csv(outputFile);
            timeSeriesPlot.setName(timeSeriesResult.getType());
            FileUtils.deleteQuietly(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return timeSeriesPlot;
    } // timeSeriesToCsv
    /* TimeSeries Custom Plots */
    private DomContent printTimeSeriesCombinedPlot(Map<String, TimeSeriesResult> tsrMap, String plotName, String elementName) {
        DomContent combinedPlotDom = null;

        // Get the DomContent of the Plot
        if(plotName.equals("Precipitation and Outflow"))
            combinedPlotDom = getPrecipOutflowPlot(tsrMap, plotName, elementName);
        else if(plotName.equals("Outflow and Observed Flow"))
            combinedPlotDom = getOutflowObservedFlowPlot(tsrMap, plotName, elementName);

        return div(attrs(".single-plot"), combinedPlotDom);
    } // printTimeSeriesCombinedPlot()
    private DomContent getPrecipOutflowPlot(Map<String, TimeSeriesResult> tsrMap, String plotName, String elementName) {
        List<TimeSeriesResult> topPlots = Arrays.asList(tsrMap.get("Precipitation"), tsrMap.get("Excess Precipitation"));
        List<TimeSeriesResult> bottomPlots = Collections.singletonList(tsrMap.get("Outflow"));

        // Creating Tables for both Top Plots and Bottom Plots
        List<Table> topPlotTables = new ArrayList<>(), bottomPlotTables = new ArrayList<>();
        int count = 0;
        for(TimeSeriesResult tsr : topPlots) {
            Table plotTable = getTimeSeriesTable(tsr, new String[] {"Time" + count, "Value" + count});
            topPlotTables.add(plotTable);
            count++;
        } // Loop: to create Tables for Top Plots
        for(TimeSeriesResult tsr : bottomPlots) {
            Table plotTable = getTimeSeriesTable(tsr, new String[] {"Time" + count, "Value" + count});
            bottomPlotTables.add(plotTable);
            count++;
        } // Loop: to create Tables for Bottom Plots

        // Setting Plot's configurations
        String xAxisTitle = "Time";
        String y1AxisTitle = bottomPlots.get(0).getUnitType() + " (" + topPlots.get(0).getUnit() + ")";
        String y2AxisTitle = topPlots.get(0).getUnitType() + " (" +  bottomPlots.get(0).getUnit() + ")";
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
        Table outflowTable = getTimeSeriesTable(outflowPlot, new String[] {"Time", "Value"});
        Table observedFlowTable = getTimeSeriesTable(observedFlowPlot, new String[] {"Time", "Value"});
        List<Table> plotList = Arrays.asList(outflowTable, observedFlowTable);

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

} // HmsReportWriter class

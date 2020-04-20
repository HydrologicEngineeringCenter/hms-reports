package mil.army.usace.hec.hms.reports.io;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.*;
import mil.army.usace.hec.hms.reports.Process;
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

public class ElementParametersWriter {
    private List<Element> elementList;
    private List<String> chosenPlots;
    private List<ReportWriter.SummaryChoice> reportSummaryChoice;
    private Map<String, List<String>> elementParameterizationChoice;

    /* Constructors */
    private ElementParametersWriter(Builder builder){
        this.elementList = builder.elementList;
        this.chosenPlots = builder.chosenPlots;
        this.reportSummaryChoice = builder.reportSummaryChoice;
        this.elementParameterizationChoice = builder.elementParameterizationChoice;
    } // ElementParametersWriter Constructor
    public static class Builder{
        List<Element> elementList;
        List<String> chosenPlots;
        List<ReportWriter.SummaryChoice> reportSummaryChoice;
        Map<String, List<String>> elementParameterizationChoice;

        public Builder elementList(List<Element> elementList){
            this.elementList = elementList;
            return this;
        } // 'elementList' constructor

        public Builder chosenPlots(List<String> chosenPlots) {
            this.chosenPlots = chosenPlots;
            return this;
        } // 'chosenPlots' constructor

        public Builder reportSummaryChoice(List<ReportWriter.SummaryChoice> reportSummaryChoice) {
            this.reportSummaryChoice = reportSummaryChoice;
            return this;
        } // 'reportSummaryChoice' constructor

        public Builder elementParameterizationChoice(Map<String, List<String>> elementParameterizationChoice) {
            this.elementParameterizationChoice = elementParameterizationChoice;
            return this;
        } // 'elementParameterizationChoice' constructor

        public ElementParametersWriter build(){
            return new ElementParametersWriter(this);
        }
    } // Builder class: as ElementParametersWriter's Constructor
    public static Builder builder(){
        return new Builder();
    }

    /* Main Function */
    DomContent printElementList() {
        List<DomContent> elementDomList = new ArrayList<>();
        ElementResultsWriter elementResultsWriter = ElementResultsWriter.builder().elementList(elementList).reportSummaryChoice(reportSummaryChoice).build();
        Map<String, DomContent> elementResultsMap = elementResultsWriter.printListResultsWriter();

        /* For each element, print: ElementInput and ElementResults */
        for(Element element : this.elementList) {
            if(!this.elementParameterizationChoice.containsKey(StringBeautifier.beautifyString(element.getElementInput().getElementType()))) {
                continue;
            } // Skip: element types that were not chosen

            List<DomContent> elementDom = new ArrayList<>(); // Holds elementInput and elementResult
            // Getting ElementInput DomContent
            ElementInput elementInput = element.getElementInput();
            DomContent elementInputDom = printElementInput(elementInput);
            if(elementInputDom != null) { elementDom.add(elementInputDom); }
            // Getting ElementResults DomContent
            ElementResults elementResults = element.getElementResults();
            DomContent elementResultsDom = printElementResults(elementResults, elementResultsMap.get(element.getName()));
            if(elementResultsDom != null) { elementDom.add(elementResultsDom); }
            // Creating a 'div', 'class: element'
            elementDomList.add(div(attrs(".element"), elementDom.toArray(new DomContent[]{})));
        } // Loop: through elementList to print each Element

        return main(elementDomList.toArray(new DomContent[]{}));
    } // printElementList()

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
            boolean skipProcess = false;
            for(String keyName : this.elementParameterizationChoice.keySet()) {
                if(!this.elementParameterizationChoice.get(keyName).contains(reformatName)) {
                    skipProcess = true;
                    break;
                } // If: Unnecessary
            } // Loop: through all of the user's options

            if(skipProcess){
                continue;
            } // Skip: Processes that were not chosen
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
    private DomContent printElementResults(ElementResults elementResults, DomContent summaryResults) {
        List<DomContent> elementResultsDomList = new ArrayList<>();

        /* Get Summary Results Dom */
        elementResultsDomList.add(summaryResults);
        /* Get Statistic Results Dom */
//        DomContent statisticResults = printStatisticResult(elementResults.getStatisticResults());
//        elementResultsDomList.add(statisticResults);
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
}

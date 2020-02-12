package mil.army.usace.hec.hms.reports.io;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.*;

import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static j2html.TagCreator.*;

public class HmsReportWriter extends ReportWriter {

    HmsReportWriter(Builder builder) {
        super(builder);
    }

    @Override
    public void write() {
        /* HTML Layout */
        String htmlOutput = html(
                head(title("Elements of Water and Fire"), link().withRel("stylesheet").withHref("style.css")),
                body(printElementList(this.elements))
        ).renderFormatted();
        /* Writing to HTML output file */
        writeToFile(this.pathToDestination.toString(), htmlOutput);
    } // write()
    private DomContent printElementList(List<Element> elementList) {
        List<DomContent> elementDomList = new ArrayList<>();

        /* For each element, print: ElementInput and ElementResults */
        for(Element element : elementList) {
            // Getting ElementInput DomContent
            ElementInput elementInput = element.getElementInput();
            DomContent elementInputDom = printElementInput(elementInput);
            // Getting ElementResults DomContent
            ElementResults elementResults = element.getElementResults();
            DomContent elementResultsDom = printElementResults(elementResults);
            // Creating a 'div', 'class: element'
            List<DomContent> elementDom = Arrays.asList(elementInputDom, elementResultsDom);
            elementDomList.add(div(attrs(".element"), elementDom.toArray(new DomContent[]{})));
        } // Loop: through elementList to print each Element

        return main(elementDomList.toArray(new DomContent[]{}));
    } // printElementList()
    private DomContent printElementInput(ElementInput elementInput) {
        List<DomContent> elementInputDomList = new ArrayList<>();

        /* For each elementInput, print: Name, ElementType, and Processes */
        String elementName = elementInput.getName();
        String elementType = StringBeautifier.beautifyString(elementInput.getElementType());
        DomContent elementNameAndType = h2(elementName + ": " + elementType);
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
        elementInputDomList.add(processSingleDom);
        DomContent processTableDom = printTableProcesses(processTable);
        elementInputDomList.add(processTableDom);

        return div(attrs(".element-input"), elementInputDomList.toArray(new DomContent[]{}));
    } // printElementInput()
    private DomContent printSingleProcesses(List<Process> singleProcesses) {
        List<DomContent> singleProcessesDomList = new ArrayList<>();

        for(Process process : singleProcesses) {
            String processName = StringBeautifier.beautifyString(process.getName());
            String processValue = StringBeautifier.beautifyString(process.getValue());
            DomContent singleDom = join(b(processName), ":", processValue, br());
            singleProcessesDomList.add(singleDom);
        } // Loop: through each single process

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

        return div(attrs(".table-process"), tableProcessesDomList.toArray(new DomContent[]{})); // Return a list of tables (for processes)
    } // printTableProcesses()
    private DomContent printParameterTable(List<Parameter> parameterList, String processName) {
        List<DomContent> parameterDom = new ArrayList<>();
        List<Parameter> nestedParameterList = new ArrayList<>();

        if(!processName.equals("")) {
            parameterDom.add(caption(processName));
        } // If: has processName, add caption

        for(Parameter parameter : parameterList) {
            if(!parameter.getSubParameters().isEmpty()) {
                nestedParameterList.add(parameter);
            } // If: Parameter contains SubParameters
            else {
                List<String> tableRow = Arrays.asList(parameter.getName(), parameter.getValue());
                DomContent row = printTableDataRow(tableRow);
                parameterDom.add(row);
            } // Else: Parameter does not contain SubParameters
        } // Loop: through Parameter List

        /* Tables within a table */
        for(Parameter nestedParameter : nestedParameterList) {
            /* Note: Nested Parameter's table should be in a row */
            List<DomContent> nestedParameterDom = new ArrayList<>();
            String reformatName = StringBeautifier.beautifyString(nestedParameter.getName());
            DomContent tableName = td(reformatName);
            List<Parameter> subParameters = nestedParameter.getSubParameters();
            DomContent subParameterTable = td(printParameterTable(subParameters,""));
            nestedParameterDom.add(tableName);
            nestedParameterDom.add(subParameterTable);
            parameterDom.add(tr(nestedParameterDom.toArray(new DomContent[]{})));
            // Return table of class 'nested_process' if nested
            return table(attrs(".nested-parameter"), parameterDom.toArray(new DomContent[]{}));
        } // Loop: through nested parameter List

        return table(attrs(".table-parameter"), parameterDom.toArray(new DomContent[]{})); // Table of Parameters
    } // printParameterTable()
    private DomContent printTableHeadRow(List<String> headRow) {
        List<DomContent> domList = new ArrayList<>();

        for(String column : headRow) {
            String reformatString = StringBeautifier.beautifyString(column);
            DomContent headDom = th(reformatString);
            domList.add(headDom);
        } // Loop: through headRow list

        return tr(domList.toArray(new DomContent[]{}));
    } // printTableHeadRow()
    private DomContent printTableDataRow(List<String> dataRow) {
        List<DomContent> domList = new ArrayList<>();

        for(String data : dataRow) {
            String reformatString = StringBeautifier.beautifyString(data);
            DomContent dataDom = td(reformatString); // Table Data type
            domList.add(dataDom);
        } // Convert 'data' to Dom

        return tr(domList.toArray(new DomContent[]{})); // Table Row type
    } // printTableDataRow()
    private DomContent printElementResults(ElementResults elementResults) {
        List<DomContent> elementResultsDomList = new ArrayList<>();

        /* Get Statistic Results Dom */
        DomContent statisticResults = printStatisticResult(elementResults.getStatisticResults());
        elementResultsDomList.add(statisticResults);
        /* Get TimeSeries Results Dom */
        DomContent timeSeriesResults = printTimeSeriesResult(elementResults.getTimeSeriesResults());
        elementResultsDomList.add(timeSeriesResults);

        return div(attrs(".element-results"), elementResultsDomList.toArray(new DomContent[]{}));
    } // printElementResults()
    /**
     * Takes a list of StatisticResults, and create a table for that list.
     * @param statisticResultList A list of Statistic Results.
     * @return A DomContent table of Statistic Results.
     */
    private DomContent printStatisticResult(List<StatisticResult> statisticResultList) {
        List<DomContent> statisticResultDomList = new ArrayList<>();

        /* Adding Data of the Table */
        for(StatisticResult statisticResult : statisticResultList) {
            String statisticName = statisticResult.getName();
            /* Skip unnecessary StatisticResults */
            if(!validStatisticResult().contains(statisticName)) { continue; }
            /* Print out StatisticResult Table */
            List<String> rowContent = Arrays.asList(statisticName, statisticResult.getValue(), statisticResult.getUnits());
            DomContent row = printTableDataRow(rowContent);
            statisticResultDomList.add(row);
        } // Loop: to get DomContent rows for table

        /* Addng Head of the Table if there is a table */
        if(!statisticResultDomList.isEmpty()) {
            DomContent head = printTableHeadRow(Arrays.asList("Name", "Value", "Unit"));
            statisticResultDomList.add(0, head); // Add to front
        } // If: There is a table

        return table(attrs(".statistic-result"), statisticResultDomList.toArray(new DomContent[]{}));
    } // printStatisticResults()
    /**
     * A List of valid Statistic Results
     * @return a List of Strings (of valid Statistic Results)
     */
    private List<String> validStatisticResult() {
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
    private DomContent printTimeSeriesResult(List<TimeSeriesResult> timeSeriesResult) {
        return null;
    } // printTimeSeriesResult
    private void writeToFile(String pathToOutput, String content) {
        try { FileUtils.writeStringToFile(new File(pathToOutput), content, StandardCharsets.UTF_8); }
        catch (IOException e) { e.printStackTrace(); }
    } // writeToFile()
} // HmsReportWriter class

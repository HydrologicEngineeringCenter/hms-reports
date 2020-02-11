package mil.army.usace.hec.hms.reports.io;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.Parameter;

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
            elementDomList.add(elementInputDom);
            // Getting ElementResults DomContent
            ElementResults elementResults = element.getElementResults();
            DomContent elementResultsDom = printElementResults(elementResults);
            elementDomList.add(elementResultsDom);
        } // Loop: through elementList to print each Element

        return main(elementDomList.toArray(new DomContent[]{}));
    } // printElementList()
    private DomContent printElementInput(ElementInput elementInput) {
        List<DomContent> elementInputDomList = new ArrayList<>();

        /* For each elementInput, print: Name, ElementType, and Processes */
        DomContent elementNameAndType = h2(elementInput.getName() + ": " + elementInput.getElementType());
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

        return div(elementInputDomList.toArray(new DomContent[]{})); // Instead of 'div', can do something else too ***
    } // printElementInput()
    private DomContent printElementResults(ElementResults elementResultsList) {
        List<DomContent> elementResultsDomList = new ArrayList<>();

        // TODO: Implement me!

        return div(elementResultsDomList.toArray(new DomContent[]{}));
    } // printElementResults()
    private DomContent printSingleProcesses(List<Process> singleProcesses) {
        List<DomContent> singleProcessesDomList = new ArrayList<>();

        for(Process process : singleProcesses) {
            DomContent singleDom = join(process.getName(), ":", process.getValue(), br());
            singleProcessesDomList.add(singleDom);
        } // Loop: through each single process

        return p(singleProcessesDomList.toArray(new DomContent[]{})); // Return in the format of a 'paragraph'
    } // printSingleProcesses()
    private DomContent printTableProcesses(List<Process> tableProcesses) {
        List<DomContent> tableProcessesDomList = new ArrayList<>();

        for(Process process : tableProcesses) {
            DomContent tableName = caption(process.getName()); // Table's name
            List<Parameter> parameterList = process.getParameters();
            DomContent tableDom  = printParameterTable(parameterList); // The Table of Parameters
            List<DomContent> wholeTableDom = Arrays.asList(tableName, tableDom);
            tableProcessesDomList.add(div(wholeTableDom.toArray(new DomContent[]{})));
        } // Loop: through each table process

        return div(tableProcessesDomList.toArray(new DomContent[]{})); // Return a list of tables (for processes)
    } // printTableProcesses()
    private DomContent printParameterTable(List<Parameter> parameterList) {
        List<DomContent> parameterDom = new ArrayList<>();
        List<Parameter> nestedParameterList = new ArrayList<>();

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
            DomContent tableName = td(nestedParameter.getName());
            List<Parameter> subParameters = nestedParameter.getSubParameters();
            DomContent subParameterTable = td(printParameterTable(subParameters));
            nestedParameterDom.add(tableName);
            nestedParameterDom.add(subParameterTable);
            parameterDom.add(tr(nestedParameterDom.toArray(new DomContent[]{})));
        } // Loop: through nested parameter List

        return table(parameterDom.toArray(new DomContent[]{})); // Table of Parameters
    } // printParameterTable()
    private DomContent printTableDataRow(List<String> dataRow) {
        List<DomContent> domList = new ArrayList<>();

        for(String data : dataRow) {
            DomContent dataDom = td(data); // Table Data type
            domList.add(dataDom);
        } // Convert 'data' to Dom

        return tr(domList.toArray(new DomContent[]{})); // Table Row type
    } // printTableDataRow()
    private void writeToFile(String pathToOutput, String content) {
        try { FileUtils.writeStringToFile(new File(pathToOutput), content, StandardCharsets.UTF_8); }
        catch (IOException e) { e.printStackTrace(); }
    } // writeToFile()

} // HmsReportWriter class

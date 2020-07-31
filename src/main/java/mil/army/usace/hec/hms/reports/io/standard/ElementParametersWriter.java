package mil.army.usace.hec.hms.reports.io.standard;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.*;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;
import mil.army.usace.hec.hms.reports.util.ValidCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static j2html.TagCreator.*;

public class ElementParametersWriter {
    private List<Element> elementList;
    private List<String> chosenPlots;
    private List<SummaryChoice> reportSummaryChoice;
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
        List<SummaryChoice> reportSummaryChoice;
        Map<String, List<String>> elementParameterizationChoice;

        public Builder elementList(List<Element> elementList){
            this.elementList = elementList;
            return this;
        } // 'elementList' constructor

        Builder chosenPlots(List<String> chosenPlots) {
            this.chosenPlots = chosenPlots;
            return this;
        } // 'chosenPlots' constructor

        Builder reportSummaryChoice(List<SummaryChoice> reportSummaryChoice) {
            this.reportSummaryChoice = reportSummaryChoice;
            return this;
        } // 'reportSummaryChoice' constructor

        Builder elementParameterizationChoice(Map<String, List<String>> elementParameterizationChoice) {
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
        ElementResultsWriter elementResultsWriter = ElementResultsWriter.builder()
                .elementList(elementList)
                .reportSummaryChoice(reportSummaryChoice)
                .chosenPlots(chosenPlots)
                .build();
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
            DomContent elementResultsDom = elementResultsWriter.printElementResults(elementResults, elementResultsMap.get(element.getName()));
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
            if(!this.elementParameterizationChoice.get(elementType).contains(StringBeautifier.beautifyString(process.getName()))) {
                continue;
            } // If: Choice doesn't contain process's name, skip

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
            String tableName = StringBeautifier.beautifyString(process.getName());
            tableName = tableName + ": " + StringBeautifier.beautifyString(process.getValue());
            List<Parameter> parameterList = process.getParameters();
            DomContent tableDom  = printParameterTable(parameterList, tableName); // The Table of Parameters
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
} // ElementParametersWriter Class

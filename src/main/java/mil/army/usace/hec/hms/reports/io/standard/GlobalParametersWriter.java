package mil.army.usace.hec.hms.reports.io.standard;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.Parameter;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;

import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class GlobalParametersWriter {
    private List<Element> elementList;
    private List<SummaryChoice> reportSummaryChoice;
    private Map<String, List<String>> globalParameterChoices; // {"Subbasin", ["Canopy", "Loss", etc...]}

    /* Constructors */
    private GlobalParametersWriter(Builder builder){
        this.elementList = builder.elementList;
        this.reportSummaryChoice = builder.reportSummaryChoice;
        this.globalParameterChoices = builder.globalParameterChoices;
    } // GlobalParametersWriter Constructor
    public static class Builder{
        List<Element> elementList;
        List<SummaryChoice> reportSummaryChoice;
        private Map<String, List<String>> globalParameterChoices;

        public Builder elementList(List<Element> elementList){
            this.elementList = elementList;
            return this;
        } // 'elementList' constructor

        Builder reportSummaryChoice(List<SummaryChoice> reportSummaryChoice) {
            this.reportSummaryChoice = reportSummaryChoice;
            return this;
        } // 'reportSummaryChoice' constructor

        Builder globalParameterChoices(Map<String, List<String>> globalParameterChoices) {
            this.globalParameterChoices = globalParameterChoices;
            return this;
        } // 'globalParameterChoices' constructor

        public GlobalParametersWriter build(){
            return new GlobalParametersWriter(this);
        }
    } // Builder class: as GlobalParametersWriter's Constructor
    public static Builder builder(){
        return new Builder();
    }

    /* Main functions */
    DomContent printListGlobalParameter() {
        if(reportSummaryChoice == null || !reportSummaryChoice.contains(SummaryChoice.GLOBAL_PARAMETER_SUMMARY)) {
            return null;
        } // If: Report Summary Choice contains PARAMETER_SUMMARY

        List<DomContent> globalParameterDomList = new ArrayList<>();
        Map<String, List<Element>> separatedElements = separateElementsByType(this.elementList);
        /* Print out the Global Parameter choices that the user chose */
        for(String chosenType : this.globalParameterChoices.keySet()) {
            switch (chosenType) {
                case "Subbasin": {
                    if(globalParameterChoices.containsKey("Subbasin")) {
                        List<String> availableList = globalParameterChoices.get("Subbasin");
                        DomContent parameterTable = printSubbasinParameterList(separatedElements.get("Subbasin"), availableList);
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Subbasin parameters

                    break;
                }
                case "Reach": {
                    if(globalParameterChoices.containsKey("Reach")) {
                        DomContent parameterTable = printReachParameterList(separatedElements.get("Reach"), globalParameterChoices.get("Reach"));
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Reach parameters

                    break;
                }
                case "Junction": {
                    if(globalParameterChoices.containsKey("Junction")) {
                        DomContent parameterTable = printJunctionParameterList(separatedElements.get("Junction"), globalParameterChoices.get("Junction"));
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Junction parameters

                    break;
                }
                case "Sink": {
                    if(globalParameterChoices.containsKey("Sink")) {
                        DomContent parameterTable = printSinkParameterList(separatedElements.get("Sink"), globalParameterChoices.get("Sink"));
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Sink parameters

                    break;
                }
                case "Source": {
                    if(globalParameterChoices.containsKey("Source")) {
                        DomContent parameterTable = printSourceParameterList(separatedElements.get("Source"), globalParameterChoices.get("Source"));
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Source parameters

                    break;
                }
                case "Reservoir": {
                    if(globalParameterChoices.containsKey("Reservoir")) {
                        DomContent parameterTable = printReservoirParameterList(separatedElements.get("Reservoir"), globalParameterChoices.get("Reservoir"));
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Reservoir parameters

                    break;
                }
                default:
                    System.out.println("Element Type is not Supported");
                    return null;
            } // Switch Case: for each element type
        } // Loop: through the user's chosen global parameters

        return div(attrs(".global-parameter"), globalParameterDomList.toArray(new DomContent[]{}));
    } // printListGlobalParameter

    /* Printing Parameter Tables (Subbasin/Reach/etc...) */
    private DomContent printSubbasinParameterList(List<Element> subbasinElements, List<String> processChoices) {
        String domAttribute = ".global-parameter";
        List<DomContent> tableDomList = getProcessTables(subbasinElements, processChoices, domAttribute);

        /* Add headings for Global Parameter Summary - Subbasin */
        if(!tableDomList.isEmpty()) {
            tableDomList.add(0, h2(attrs(".global-header"), "Global Parameter Summary - Subbasin"));
        } // Add section title if there is Subbasin global parameter

        return div(attrs(domAttribute), tableDomList.toArray(new DomContent[]{}));
    } // printSubbasinParameterList()
    private DomContent printReachParameterList(List<Element> reachElements, List<String> processChoices) {
        String domAttribute = ".global-parameter";
        List<DomContent> tableDomList = getProcessTables(reachElements, processChoices, domAttribute);

        /* Add headings for Global Parameter Summary - Reach */
        if(!tableDomList.isEmpty()) {
            tableDomList.add(0, h2(attrs(".global-header"), "Global Parameter Summary - Reach"));
        } // Add section title if there is Reach global parameter

        return div(attrs(domAttribute), tableDomList.toArray(new DomContent[]{}));
    } // printReachParameterList()
    private DomContent printJunctionParameterList(List<Element> junctionElements, List<String> processChoices) {
        String domAttribute = ".global-parameter";
        List<DomContent> tableDomList = getProcessTables(junctionElements, processChoices, domAttribute);

        /* Add headings for Global Parameter Summary - Junction */
        if(!tableDomList.isEmpty()) {
            tableDomList.add(0, h2(attrs(".global-header"), "Global Parameter Summary - Junction"));
        } // Add section title if there is Junction global parameter

        return div(attrs(domAttribute), tableDomList.toArray(new DomContent[]{}));
    } // printJunctionParameterList()
    private DomContent printSinkParameterList(List<Element> sinkElements, List<String> processChoices) {
        String domAttribute = ".global-parameter";
        List<DomContent> tableDomList = getProcessTables(sinkElements, processChoices, domAttribute);

        /* Add headings for Global Parameter Summary - Sink */
        if(!tableDomList.isEmpty()) {
            tableDomList.add(0, h2(attrs(".global-header"), "Global Parameter Summary - Sink"));
        } // Add section title if there is Sink global parameter

        return div(attrs(domAttribute), tableDomList.toArray(new DomContent[]{}));
    } // printSinkParameterList()
    private DomContent printSourceParameterList(List<Element> sourceElements, List<String> processChoices) {
        String domAttribute = ".global-parameter";
        List<DomContent> tableDomList = getProcessTables(sourceElements, processChoices, domAttribute);

        /* Add headings for Global Parameter Summary - Source */
        if(!tableDomList.isEmpty()) {
            tableDomList.add(0, h2(attrs(".global-header"), "Global Parameter Summary - Source"));
        } // Add section title if there is Source global parameter

        return div(attrs(domAttribute), tableDomList.toArray(new DomContent[]{}));
    } // printSourceParameterList()
    private DomContent printReservoirParameterList(List<Element> reservoirElements, List<String> processChoices) {
        String domAttribute = ".global-parameter";
        List<DomContent> tableDomList = getProcessTables(reservoirElements, processChoices, domAttribute);

        /* Add headings for Global Parameter Summary - Reservoir */
        if(!tableDomList.isEmpty()) {
            tableDomList.add(0, h2(attrs(".global-header"), "Global Parameter Summary - Reservoir"));
        } // Add section title if there is Reservoir global parameter

        return div(attrs(domAttribute), tableDomList.toArray(new DomContent[]{}));
    } // printReservoirParameterList()

    /* Helper functions */
    private Map<String, List<Element>> separateElementsByType(List<Element> listElement) {
        Map<String, List<Element>> separatedElementMap = new HashMap<>();

        List<Element> subbasinElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().toUpperCase().equals("SUBBASIN"))
                .collect(Collectors.toList());
        separatedElementMap.put("Subbasin", subbasinElements);

        List<Element> reachElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().toUpperCase().equals("REACH"))
                .collect(Collectors.toList());
        separatedElementMap.put("Reach", reachElements);

        List<Element> junctionElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().toUpperCase().equals("JUNCTION"))
                .collect(Collectors.toList());
        separatedElementMap.put("Junction", junctionElements);

        List<Element> sinkElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().toUpperCase().equals("SINK"))
                .collect(Collectors.toList());
        separatedElementMap.put("Sink", sinkElements);

        List<Element> sourceElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().toUpperCase().equals("SOURCE"))
                .collect(Collectors.toList());
        separatedElementMap.put("Source", sourceElements);

        List<Element> reservoirElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().toUpperCase().equals("RESERVOIR"))
                .collect(Collectors.toList());
        separatedElementMap.put("Reservoir", reservoirElements);

        return separatedElementMap;
    } // separateElementsByType()
    private DomContent printBaseflowTableDataRow(List<String> dataRow, String tdAttribute, String trAttribute) {
        List<DomContent> domList = new ArrayList<>();

        for(String data : dataRow) {
            String reformatString = StringBeautifier.beautifyString(data);
            DomContent dataDom = td(attrs(tdAttribute), b(reformatString)); // Table Data type
            domList.add(dataDom);
        } // Convert 'data' to Dom

        return tr(attrs(trAttribute), domList.toArray(new DomContent[]{})); // Table Row type
    } // printTableDataRow()
    private List<DomContent> getProcessTables(List<Element> elementList, List<String> processChoices, String domAttrs) {
        List<DomContent> tableDomList = new ArrayList<>();
        Map<String, List<DomContent>> processTablesMap = new LinkedHashMap<>(); // 'Table Name' x 'List of Rows'

        /* Loop through each Element to add Data to tables */
        for(Element element : elementList) {
            List<Process> elementProcesses = element.getElementInput().getProcesses();
            /* Filter Out Processes that weren't chosen by the user */
            List<Process> chosenProcesses = elementProcesses.stream()
                    .filter(p -> processChoices.contains(StringBeautifier.beautifyString(p.getName())))
                    .collect(Collectors.toList());
            /* Go through each Process to get Data */
            for(Process process : chosenProcesses) {
                String processName = process.getName().toUpperCase();
                String tableName = StringBeautifier.beautifyString(process.getName());

                /* If is Process with Method, also saves the Method. */
                if(processesWithMethod().contains(processName)) { tableName = tableName + ": " + process.getValue(); }

                /* Getting 'this' Element's Row Data */
                List<String> parametersValues = process.getParameters().stream().map(Parameter::getValue).collect(Collectors.toList());
                // If Process has no Parameters, add Process's value instead
                if(parametersValues.isEmpty()) { parametersValues.add(process.getValue()); }
                parametersValues.add(0, element.getName());
                List<DomContent> elementDataRow = Collections.singletonList(HtmlModifier.printTableDataRow(parametersValues, domAttrs, domAttrs));

                /* Place Data to processTablesMap. If key not there, new key */
                List<String> parametersNames = process.getParameters().stream().map(Parameter::getName).collect(Collectors.toList());
                if(parametersNames.isEmpty()) { parametersNames.add(process.getName()); }
                parametersNames.add(0, "Element Name");
                DomContent processTableHeader = HtmlModifier.printTableHeadRow(parametersNames, domAttrs, domAttrs);

                if(!processTablesMap.containsKey(tableName)) {
                    processTablesMap.put(tableName, Arrays.asList(processTableHeader, caption(tableName)));
                } // If: Table didn't exist, new key with table's header and caption

                // Adding in this Element's Row Data
                List<DomContent> tableDataRows = new ArrayList<>(processTablesMap.get(tableName));
                if(processName.equals("BASEFLOW")) { elementDataRow = new ArrayList<>(getBaseflowDataRows(element, process, domAttrs)); }
                tableDataRows.addAll(elementDataRow);
                processTablesMap.put(tableName, tableDataRows);
            } // Loop: through each chosen Processes
        } // Loop: through each Subbasin Element

        /* Generating Tables for each Process saved in the processTablesMap */
        for(String tableName : processTablesMap.keySet()) {
            List<DomContent> tableData = processTablesMap.get(tableName);
            /* Skip Tables without any Data (2 = Caption + Header) */
            if(tableData.size() <= 2) { continue; }
            DomContent tableDom = table(attrs(domAttrs), tableData.toArray(new DomContent[]{}));
            tableDomList.add(tableDom);
        } // Loop: through all tables in processTablesMap

        return tableDomList;
    } // getProcessTablesMap()
    private List<DomContent> getBaseflowDataRows(Element element, Process process, String domAttrs) {
        return new ArrayList<>();
    } // getBaseflowDataRows()
    private List<String> processesWithMethod() {
        List<String> processWithMethodList = new ArrayList<>();
        processWithMethodList.add("TRANSFORM");
        processWithMethodList.add("CANOPY");
        processWithMethodList.add("ROUTE");
        processWithMethodList.add("BASEFLOW");
        return processWithMethodList;
    } // processesWithMethod()
} // GlobalParametersWriter()

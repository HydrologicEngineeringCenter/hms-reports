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
                        DomContent sectionTitle = h2(attrs(".global-header"), "Global Parameter Summary - Subbasin");
                        DomContent parameterTable = getProcessTables(sectionTitle, separatedElements.get("Subbasin"), availableList, ".global-parameter");
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Subbasin parameters

                    break;
                }
                case "Reach": {
                    if(globalParameterChoices.containsKey("Reach")) {
                        List<String> availableList = globalParameterChoices.get("Reach");
                        DomContent sectionTitle = h2(attrs(".global-header"), "Global Parameter Summary - Reach");
                        DomContent parameterTable = getProcessTables(sectionTitle, separatedElements.get("Reach"), availableList, ".global-parameter");
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Reach parameters

                    break;
                }
                case "Junction": {
                    if(globalParameterChoices.containsKey("Junction")) {
                        List<String> availableList = globalParameterChoices.get("Junction");
                        DomContent sectionTitle = h2(attrs(".global-header"), "Global Parameter Summary - Junction");
                        DomContent parameterTable = getProcessTables(sectionTitle, separatedElements.get("Junction"), availableList, ".global-parameter");
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Junction parameters

                    break;
                }
                case "Sink": {
                    if(globalParameterChoices.containsKey("Sink")) {
                        List<String> availableList = globalParameterChoices.get("Sink");
                        DomContent sectionTitle = h2(attrs(".global-header"), "Global Parameter Summary - Sink");
                        DomContent parameterTable = getProcessTables(sectionTitle, separatedElements.get("Sink"), availableList, ".global-parameter");
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Sink parameters

                    break;
                }
                case "Source": {
                    if(globalParameterChoices.containsKey("Source")) {
                        List<String> availableList = globalParameterChoices.get("Source");
                        DomContent sectionTitle = h2(attrs(".global-header"), "Global Parameter Summary - Source");
                        DomContent parameterTable = getProcessTables(sectionTitle, separatedElements.get("Source"), availableList, ".global-parameter");
                        if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
                    } // If the user chooses to print out Source parameters

                    break;
                }
                case "Reservoir": {
                    if(globalParameterChoices.containsKey("Reservoir")) {
                        List<String> availableList = globalParameterChoices.get("Reservoir");
                        DomContent sectionTitle = h2(attrs(".global-header"), "Global Parameter Summary - Reservoir");
                        DomContent parameterTable = getProcessTables(sectionTitle, separatedElements.get("Reservoir"), availableList, ".global-parameter");
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

    private Map<String, List<String>> availableProcessAndParameters(List<Element> elementList) {
        Map<String, List<String>> availableMap = new LinkedHashMap<>();

        for(Element element : elementList) {
            List<Process> processList = element.getElementInput().getProcesses();

            for(Process process : processList) {
                List<Parameter> parameterList = process.getParameters();
                List<String> paramNames = parameterList.stream().map(Parameter::getName).collect(Collectors.toList());

                String processName = process.getName().toUpperCase();
                String tableName = StringBeautifier.beautifyString(process.getName());
                String processValue = StringBeautifier.beautifyString(process.getValue());

                /* For Processes with Method, add Method to tableName */
                if(processesWithMethod().contains(processName)) { tableName = tableName + ": " + processValue; }

                /* For Special Case: Baseflow */
                if(processName.equals("BASEFLOW")) {
                    paramNames.clear();
                    List<Parameter> baseflowLayerList = process.getParameters().get(0).getSubParameters();
                    for(Parameter layer : baseflowLayerList) {
                        List<String> layerParams = layer.getSubParameters().stream()
                                .map(e -> e.getName().substring(e.getName().indexOf(" ") + 1))
                                .collect(Collectors.toList());
                        if(layerParams.size() >= paramNames.size()) { paramNames = layerParams; }
                    } // Loop: through each layer in baseflowLayerList
                } // If: Process is Baseflow

                /* Put if the key is not present, or has less parameters */
                if(!availableMap.containsKey(tableName) || availableMap.get(tableName).size() < paramNames.size()) {
                    availableMap.put(tableName, paramNames);
                } // If: the key is not present, or has less parameters
            } // Loop: through each Process
        } // Loop: through each Element

        return availableMap;
    } // availableProcessAndParameters()

    private List<String> processesWithMethod() {
        List<String> processWithMethodList = new ArrayList<>();
        processWithMethodList.add("TRANSFORM");
        processWithMethodList.add("CANOPY");
        processWithMethodList.add("ROUTE");
        processWithMethodList.add("BASEFLOW");
        processWithMethodList.add("LOSSRATE");
        return processWithMethodList;
    } // processesWithMethod()

    private boolean isLocationProcess(Process process) {
        String finalProcessName = process.getName().toUpperCase();
        List<String> locationProcesses = Arrays.asList("LATITUDE", "LONGITUDE");
        return locationProcesses.stream().anyMatch(finalProcessName::contains);
    } // isLocationProcess()

    private DomContent getProcessTables(DomContent sectionTitle, List<Element> elementList, List<String> processChoices, String domAttrs) {
        List<DomContent> tableDomList = new ArrayList<>();
        Map<String, List<DomContent>> processTablesMap = new LinkedHashMap<>(); // 'Table Name' x 'List of Rows'
        Map<String, List<String>> availableMap = availableProcessAndParameters(elementList);

        for(Element element : elementList) {
            List<Process> processList = element.getElementInput().getProcesses();
            /* Filter Out Processes that weren't chosen by the user */
            List<Process> chosenProcesses = processList.stream()
                    .filter(p -> processChoices.contains(StringBeautifier.beautifyString(p.getName())))
                    .collect(Collectors.toList());
            /* Filter Out Location Processes and Get Location Table*/
            if(!processTablesMap.containsKey("Location")) {
                List<DomContent> locationDataRows = new ArrayList<>();
                List<String> headerList = Arrays.asList("Element Name", "Longitude Degrees", "Latitude Degrees");
                DomContent headerDom = HtmlModifier.printTableHeadRow(headerList, domAttrs, domAttrs);
                DomContent captionDom = caption("Location");
                locationDataRows.add(0, headerDom);
                locationDataRows.add(0, captionDom);
                processTablesMap.put("Location", locationDataRows);
            } // If: New Location Table, Then: Add Header and Caption
            List<DomContent> locationDataRows = new ArrayList<>(processTablesMap.get("Location"));
            locationDataRows.addAll(getLocationDataRows(element, chosenProcesses, domAttrs));
            processTablesMap.put("Location", locationDataRows);

            for(Process process : chosenProcesses) {
                /* For Location Processes, Skip */
                if(isLocationProcess(process)) { continue; }
                /* Get Process's Name and Table Name */
                if(process.getValue().equals("None")) { continue; }
                String processName = process.getName().toUpperCase();
                String tableName = StringBeautifier.beautifyString(process.getName());
                String processValue = StringBeautifier.beautifyString(process.getValue());
                /* For Processes with Method, add Method to tableName */
                if(processesWithMethod().contains(processName)) { tableName = tableName + ": " + processValue; }

                List<String> availableParameters = new ArrayList<>(availableMap.get(tableName));
                List<String> parameterValues = new ArrayList<>();
                Map<String, Parameter> parameterMap = process.getParameters().stream()
                        .collect(Collectors.toMap(Parameter::getName, e -> e));

                for(String availParam : availableParameters) {
                    String paramKey = parameterMap.keySet().stream().filter(e -> e.contains(availParam)).findAny().orElse("");
                    Parameter parameter = parameterMap.getOrDefault(paramKey, Parameter.builder().value("Not Specified").build());
                    String value = parameter.getValue();
                    parameterValues.add(value);
                } // Loop: through all availableParameters

                if(parameterValues.isEmpty()) { parameterValues.add(process.getValue()); } // For Single Processes
                parameterValues.add(0, element.getName());
                List<DomContent> dataRowDom = Collections.singletonList(HtmlModifier.printTableDataRow(parameterValues, domAttrs, domAttrs));

                /* Place Data to processTablesMap. If key not there, new key */
                List<String> parametersNames = new ArrayList<>(availableParameters);
                if(parametersNames.isEmpty()) { parametersNames.add(process.getName()); }
                parametersNames.add(0, "Element Name");
                DomContent processTableHeader = HtmlModifier.printTableHeadRow(parametersNames, domAttrs, domAttrs);

                if(!processTablesMap.containsKey(tableName)) {
                    tableName = StringBeautifier.beautifyString(process.getName());
                    if(processesWithMethod().contains(processName)) { tableName = tableName + ": " + StringBeautifier.beautifyString(process.getValue()); }
                    processTablesMap.put(tableName, Arrays.asList(processTableHeader, caption(tableName)));
                } // If: Table didn't exist, new key with table's header and caption

                // Adding in this Element's Row Data
                List<DomContent> tableDataRows = new ArrayList<>(processTablesMap.get(tableName));
                if(processName.equals("BASEFLOW")) { dataRowDom = new ArrayList<>(getBaseflowDataRows(element, process, availableParameters, domAttrs)); }
                tableDataRows.addAll(dataRowDom);
                processTablesMap.put(tableName, tableDataRows);
            } // Loop: through all Processes
        } // Loop: through all Elements

        /* Generating Tables for each Process saved in the processTablesMap */
        for(String tableName : processTablesMap.keySet()) {
            List<DomContent> tableData = processTablesMap.get(tableName);
            /* Skip Tables without any Data (2 = Caption + Header) */
            if(tableData.size() <= 2) { continue; }
            DomContent tableDom = table(attrs(domAttrs), tableData.toArray(new DomContent[]{}));
            tableDomList.add(tableDom);
        } // Loop: through all tables in processTablesMap

        /* Add headings for Global Parameter Summary - Subbasin */
        if(!tableDomList.isEmpty()) {
            tableDomList.add(0, sectionTitle);
        } // Add section title if there is Subbasin global parameter

        return div(attrs(domAttrs), tableDomList.toArray(new DomContent[]{}));
    } // getProcessTables()

    private DomContent printBaseflowTableDataRow(List<String> dataRow, String tdAttribute, String trAttribute) {
        List<DomContent> domList = new ArrayList<>();

        for(String data : dataRow) {
            String reformatString = StringBeautifier.beautifyString(data);
            DomContent dataDom = td(attrs(tdAttribute), b(reformatString)); // Table Data type
            domList.add(dataDom);
        } // Convert 'data' to Dom

        return tr(attrs(trAttribute), domList.toArray(new DomContent[]{})); // Table Row type
    } // printTableDataRow()

    private List<DomContent> getLocationDataRows(Element element, List<Process> processList, String domAttrs) {
        List<DomContent> locationDataRows = new ArrayList<>();

        /* Element Name */
        String elementName = element.getName();

        /* Longitude and Latitude */
        Process defaultProcess = Process.builder().value("Not Specified").build();

        Process longitudeProcess = processList.stream()
                .filter(p -> p.getName().toUpperCase().contains("LONGITUDE"))
                .findFirst().orElse(defaultProcess);

        Process latitudeProcess = processList.stream()
                .filter(p -> p.getName().toUpperCase().contains("LATITUDE"))
                .findFirst().orElse(defaultProcess);

        /* Return an Empty List if have neither Longitude nor Latitude */
        if(longitudeProcess == defaultProcess && latitudeProcess == defaultProcess) { return new ArrayList<>(); }

        /* Create a DomContent for Row and Add to LocationDataRows */
        List<String> rowData = Arrays.asList(elementName, longitudeProcess.getValue(), latitudeProcess.getValue());
        DomContent rowDataDom = HtmlModifier.printTableDataRow(rowData, domAttrs, domAttrs);
        locationDataRows.add(rowDataDom);

        return locationDataRows;
    } // getLocationDataRows()

    private List<DomContent> getBaseflowDataRows(Element element, Process process, List<String> availableParameters, String domAttrs) {
        List<DomContent> baseflowDataRows = new ArrayList<>();
        List<Parameter> baseflowLayerList = process.getParameters().get(0).getSubParameters();

        /* Element Name's Row */
        List<String> nameRow  = new ArrayList<>();
        nameRow.add(element.getName());
        for(int i = 0; i < availableParameters.size(); i++) { nameRow.add(""); }
        DomContent nameRowDom = printBaseflowTableDataRow(nameRow, domAttrs, domAttrs);
        baseflowDataRows.add(nameRowDom);

        /* Subsequent Layer Rows */
        int count = 1;
        for(Parameter layer : baseflowLayerList) {
            List<String> subParametersValues = new ArrayList<>();
            List<Parameter> layerSubParameters = layer.getSubParameters();
            Map<String, Parameter> subParametersMap = layerSubParameters.stream().collect(Collectors.toMap(Parameter::getName, e->e));
            for(String availParam : availableParameters) {
                String paramKey = subParametersMap.keySet().stream().filter(e -> e.contains(availParam)).findAny().orElse("");
                Parameter parameter = subParametersMap.getOrDefault(paramKey, Parameter.builder().value("Not Specified").build());
                String value = parameter.getValue();
                subParametersValues.add(value);
            } // Loop: through all subParameters

            /* Skip if too many null values */
            long numNullData = subParametersValues.stream().filter(e -> e.equals("Not Specified")).count();
            long maxNullData = subParametersValues.size() - 2;
            if(numNullData > maxNullData) { continue; }

            /* Add Layer's data to baseflowDataRows */
            subParametersValues.add(0, "Layer " + count);
            DomContent layerRow = HtmlModifier.printTableDataRow(subParametersValues, domAttrs, domAttrs);
            baseflowDataRows.add(layerRow);
            count++;
        } // Loop: through all Layers of Baseflow

        return baseflowDataRows;
    } // getBaseflowDataRows()

} // GlobalParametersWriter()

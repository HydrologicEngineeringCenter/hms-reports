package mil.army.usace.hec.hms.reports.io.standard;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.Parameter;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.HtmlUtil;
import mil.army.usace.hec.hms.reports.util.StringUtil;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class GlobalParametersWriter {
    private final String domAttrs = ".global-parameter";
    private final List<Element> elementList;
    private final List<SummaryChoice> reportSummaryChoice;
    private final Map<String, List<String>> globalParameterChoices; // {"Subbasin", ["Canopy", "Loss", etc...]}
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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

        /* Print out the Global Parameter choices that the user chose */
        List<DomContent> globalParameterDomList = new ArrayList<>();
        String parameterAtr = ".global-parameter";

        for(String chosenType : this.globalParameterChoices.keySet()) {
            List<String> chosenProcesses = globalParameterChoices.get(chosenType);
            List<Element> elementListByType = this.elementList.stream()
                    .filter(element -> element.getElementInput().getElementType().equals(chosenType))
                    .collect(Collectors.toList());
            DomContent parameterTable = getProcessTables(elementListByType, chosenProcesses);
            if(parameterTable != null) { globalParameterDomList.add(parameterTable); }
        } // Loop: through the user's chosen global parameters

        return div(attrs(parameterAtr), globalParameterDomList.toArray(new DomContent[]{}));
    } // printListGlobalParameter

    /* Helper functions */
    private Map<String, List<String>> availableProcessAndParameters(List<Element> elementList) {
        Map<String, List<String>> availableMap = new LinkedHashMap<>();

        for(Element element : elementList) {
            List<Process> processList = element.getElementInput().getProcesses();

            for(Process process : processList) {
                List<Parameter> parameterList = process.getParameters();
                List<String> paramNames = parameterList.stream().map(Parameter::getName).collect(Collectors.toList());

                String processName = process.getName().toUpperCase();
                String tableName = processTableName(process);

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

    private DomContent getProcessTables(List<Element> elementList, List<String> processChoices) {
        List<DomContent> tableDomList = new ArrayList<>();
        Map<String, List<DomContent>> processTablesMap = new LinkedHashMap<>(); // 'Table Name' x 'List of Rows'
        Map<String, List<String>> availableMap = availableProcessAndParameters(elementList);

        for(Element element : elementList) {
            List<Process> processList = element.getElementInput().getProcesses();
            /* Filter Out Processes that weren't chosen by the user */
            List<Process> chosenProcesses = processList.stream()
                    .filter(p -> processChoices.contains(StringUtil.beautifyString(p.getName())))
                    .collect(Collectors.toList());
            /* Filter Out Location Processes and Get Location Table*/
            if(!processTablesMap.containsKey("Location")) {
                List<DomContent> locationDataRows = new ArrayList<>();
                List<String> headerList = Arrays.asList("Element Name", "Longitude Degrees", "Latitude Degrees");
                DomContent headerDom = HtmlUtil.printTableHeadRow(headerList, domAttrs, domAttrs);
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
                String tableName = StringUtil.beautifyString(process.getName());
                String processValue = StringUtil.beautifyString(process.getValue());
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
                List<DomContent> dataRowDom = Collections.singletonList(HtmlUtil.printTableDataRow(parameterValues, domAttrs, domAttrs));

                /* Place Data to processTablesMap. If key not there, new key */
                List<String> parametersNames = new ArrayList<>(availableParameters);
                if(parametersNames.isEmpty()) { parametersNames.add(process.getName()); }
                parametersNames.add(0, "Element Name");
                DomContent processTableHeader = HtmlUtil.printTableHeadRow(parametersNames, domAttrs, domAttrs);

                if(!processTablesMap.containsKey(tableName)) {
                    tableName = StringUtil.beautifyString(process.getName());
                    if(processesWithMethod().contains(processName)) { tableName = tableName + ": " + StringUtil.beautifyString(process.getValue()); }
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

        /* Add headings for Global Parameter Summary */
        if(tableDomList.isEmpty()) return null;
        String headerAtr = ".global-header";
        String titleName = "Global Parameter Summary - " + elementList.get(0).getElementInput().getElementType();
        DomContent titleDom = h2(attrs(headerAtr), titleName);
        tableDomList.add(0, titleDom);

        return div(attrs(domAttrs), tableDomList.toArray(new DomContent[]{}));
    } // getProcessTables()

    private String processTableName(Process process) {
        String processName = process.getName().toUpperCase();
        String tableName = StringUtil.beautifyString(process.getName());
        String processValue = StringUtil.beautifyString(process.getValue());
        if(processesWithMethod().contains(processName)) { tableName = tableName + ": " + processValue; }
        return tableName;
    }

    private DomContent printBaseflowTableDataRow(List<String> dataRow, String tdAttribute, String trAttribute) {
        List<DomContent> domList = new ArrayList<>();

        for(String data : dataRow) {
            String reformatString = StringUtil.beautifyString(data);
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
        DomContent rowDataDom = HtmlUtil.printTableDataRow(rowData, domAttrs, domAttrs);
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
            DomContent layerRow = HtmlUtil.printTableDataRow(subParametersValues, domAttrs, domAttrs);
            baseflowDataRows.add(layerRow);
            count++;
        } // Loop: through all Layers of Baseflow

        return baseflowDataRows;
    } // getBaseflowDataRows()

} // GlobalParametersWriter()

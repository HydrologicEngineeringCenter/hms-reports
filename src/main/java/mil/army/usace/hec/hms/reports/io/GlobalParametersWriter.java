package mil.army.usace.hec.hms.reports.io;

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
        /* Subbasin's parameters (Area, Loss Rate, Canopy, Transform, Baseflow) */
        List<DomContent> subbasinParameterDomList = new ArrayList<>(); // Contains (Area table, Loss Rate table, etc...)
        String tdAttribute = ".global-parameter";
        /* For each table: Loop through subbasinElements List, get necessary data to get a DomContent table */
        List<DomContent> areaRowDomList = new ArrayList<>();      // Contains all rows needed for an Area Table
        List<DomContent> lossRowDomList = new ArrayList<>();      // Contains all rows needed for an Loss Table
        List<DomContent> canopyRowDomList = new ArrayList<>();    // Contains all rows needed for an Canopy Table
        List<DomContent> transformRowDomList = new ArrayList<>(); // Contains all rows needed for a Transform Table
        List<DomContent> baseflowRowDomList = new ArrayList<>();  // Contains all rows needed for an Baseflow Table

        for(Element element : subbasinElements) {
            /* Map of the element's list of processes */
            Map<String, Process> elementProcesses = element.getElementInput().getProcesses().stream()
                    .collect(Collectors.toMap(Process::getName, x -> x));

            /* Parameter Table: Area */
            if(processChoices.contains(StringBeautifier.beautifyString("area"))) {
                List<String> areaRowList = new ArrayList<>();
                areaRowList.add(element.getName()); // Element Name
                areaRowList.add(elementProcesses.get("area").getValue()); // Area
                areaRowDomList.add(HtmlModifier.printTableDataRow(areaRowList, tdAttribute, tdAttribute)); // To DomContent
            }

            /* Parameter Table: Loss Rate */
            if(processChoices.contains(StringBeautifier.beautifyString("lossRate"))) {
                Map<String, Parameter> lossParameters = elementProcesses.get("lossRate").getParameters().stream()
                        .collect(Collectors.toMap(Parameter::getName, x -> x));
                List<String> lossRowList = new ArrayList<>();

                lossRowList.add(element.getName()); // Element Name
                lossRowList.add(lossParameters.get("initialDeficit").getValue());  // Initial Deficit (IN)
                lossRowList.add(lossParameters.get("maximumDeficit").getValue());  // Maximum Storage (IN)
                lossRowList.add(lossParameters.get("percolationRate").getValue()); // Constant Rate (IN/HR)
                lossRowList.add(lossParameters.get("percentImperviousArea").getValue()); // Impervious (%)
                lossRowDomList.add(HtmlModifier.printTableDataRow(lossRowList, tdAttribute, tdAttribute)); // To DomContent
            }

            /* Parameter Table: Canopy */
            if(processChoices.contains(StringBeautifier.beautifyString("canopy"))) {
                Map<String, Parameter> canopyParameters = elementProcesses.get("canopy").getParameters().stream()
                        .collect(Collectors.toMap(Parameter::getName, x -> x));
                List<String> canopyRowList = new ArrayList<>();

                canopyRowList.add(element.getName()); // Element Name
                canopyRowList.add(StringBeautifier.beautifyString(canopyParameters.get("initialStorage").getValue()));  // Initial Storage (%)
                canopyRowList.add(canopyParameters.get("storageCapacity").getValue()); // Max Storage (IN)
                canopyRowList.add(canopyParameters.get("cropCoefficient").getValue()); // Crop Coefficient
                String evapotranspiration = canopyParameters.get("allowSimultaneousPrecipEt").getValue();
                if(evapotranspiration.equals("false")) { canopyRowList.add("Only Dry Periods"); }
                else { canopyRowList.add("Wet and Dry Periods"); } // Evapotranspiration
                canopyRowList.add(StringBeautifier.beautifyString(canopyParameters.get("uptakeMethod").getValue())); // Uptake Method
                canopyRowDomList.add(HtmlModifier.printTableDataRow(canopyRowList, tdAttribute, tdAttribute)); // To DomContent
            }

            /* Parameter Table: Transform */
            if(processChoices.contains(StringBeautifier.beautifyString("transform"))) {
                Map<String, Parameter> transformParameters = elementProcesses.get("transform").getParameters().stream()
                        .collect(Collectors.toMap(Parameter::getName, x -> x));
                List<String> transformRowList = new ArrayList<>();

                transformRowList.add(element.getName()); // Element Name
                transformRowList.add(transformParameters.get("timeOfConcentration").getValue()); // Time of Concentration (HR)
                transformRowList.add(transformParameters.get("storageCoefficient").getValue());  // Storage Coefficient (HR)
                transformRowDomList.add(HtmlModifier.printTableDataRow(transformRowList, tdAttribute, tdAttribute)); // To DomContent
            }

            /* Parameter Table: Baseflow --- this will contain 'n + 1' number of rows, where n = # layers */
            if(processChoices.contains(StringBeautifier.beautifyString("baseflow"))) {
                Map<String, Parameter> baseflowParameters = elementProcesses.get("baseflow").getParameters().stream()
                        .collect(Collectors.toMap(Parameter::getName, x -> x));
                List<String> firstRow = Arrays.asList(element.getName(), "", "", "", "");
                baseflowRowDomList.add(printBaseflowTableDataRow(firstRow, tdAttribute, tdAttribute)); // To DomContent
                List<Parameter> baseflowLayerList = baseflowParameters.get("baseflowLayerList").getSubParameters();

                // Rows: of each layer for this element
                for(Parameter baseflowLayer : baseflowLayerList) {
                    List<String> layerRowList = new ArrayList<>();
                    layerRowList.add("Layer " + baseflowLayer.getName()); // Ex. Name: "Layer 1"
                    Map<String, Parameter> layerInfoMap = baseflowLayer.getSubParameters().stream()
                            .collect(Collectors.toMap(Parameter::getName, x -> x));

                    layerRowList.add(layerInfoMap.get("initialRate").getValue());        // Initial (CFS)
                    layerRowList.add(layerInfoMap.get("baseflowFraction").getValue());   // Fraction
                    layerRowList.add(layerInfoMap.get("storageCoefficient").getValue()); // Coefficient (HR)
                    layerRowList.add(layerInfoMap.get("numberSteps").getValue());        // Steps
                    baseflowRowDomList.add(HtmlModifier.printTableDataRow(layerRowList, tdAttribute, tdAttribute)); // To DomContent
                } // Loop: through each layer
            }

        } // Loop: through all subbasin-type elements

        /* Adding headers to each Table, and create a table */
        if(!areaRowDomList.isEmpty()) {
            List<String> headerData = Arrays.asList("Element Name", "Area");
            DomContent headerDom = HtmlModifier.printTableHeadRow(headerData, tdAttribute, tdAttribute);
            areaRowDomList.add(0, headerDom);
            areaRowDomList.add(0, caption("Area"));
            DomContent parameterTable = table(attrs(tdAttribute), areaRowDomList.toArray(new DomContent[]{}));
            subbasinParameterDomList.add(parameterTable);
        } // Add Header for table if it's not empty

        if(!lossRowDomList.isEmpty()) {
            List<String> headerData = Arrays.asList("Element Name", "Initial Deficit (IN)", "Maximum Storage (IN)", "Constant Rate (IN/HR)", "Impervious (%)");
            DomContent headerDom = HtmlModifier.printTableHeadRow(headerData, tdAttribute, tdAttribute);
            lossRowDomList.add(0, headerDom);
            lossRowDomList.add(0, caption("Loss Rate"));
            DomContent parameterTable = table(attrs(tdAttribute), lossRowDomList.toArray(new DomContent[]{}));
            subbasinParameterDomList.add(parameterTable);
        } // Add Header for table if it's not empty

        if(!canopyRowDomList.isEmpty()) {
            List<String> headerData = Arrays.asList("Element Name", "Initial Storage (%)", "Max Storage (IN)", "Crop Coefficient", "Evapotranspiration", "Uptake Method");
            DomContent headerDom = HtmlModifier.printTableHeadRow(headerData, tdAttribute, tdAttribute);
            canopyRowDomList.add(0, headerDom);
            canopyRowDomList.add(0, caption("Canopy"));
            DomContent parameterTable = table(attrs(tdAttribute), canopyRowDomList.toArray(new DomContent[]{}));
            subbasinParameterDomList.add(parameterTable);
        } // Add Header for table if it's not empty

        if(!transformRowDomList.isEmpty()) {
            List<String> headerData = Arrays.asList("Element Name", "Time of Concentration (HR)", "Storage Coefficient (HR)");
            DomContent headerDom = HtmlModifier.printTableHeadRow(headerData, tdAttribute, tdAttribute);
            transformRowDomList.add(0, headerDom);
            transformRowDomList.add(0, caption("Transform"));
            DomContent parameterTable = table(attrs(tdAttribute), transformRowDomList.toArray(new DomContent[]{}));
            subbasinParameterDomList.add(parameterTable);
        } // Add Header for table if it's not empty

        if(!baseflowRowDomList.isEmpty()) {
            List<String> headerData = Arrays.asList("Element Name", "Initial (CFS)", "Fraction", "Coefficient (HR)", "Steps");
            DomContent headerDom = HtmlModifier.printTableHeadRow(headerData, tdAttribute, tdAttribute);
            baseflowRowDomList.add(0, headerDom);
            baseflowRowDomList.add(0, caption("Baseflow"));
            DomContent parameterTable = table(attrs(tdAttribute), baseflowRowDomList.toArray(new DomContent[]{}));
            subbasinParameterDomList.add(parameterTable);
        } // Add Header for table if it's not empty

        if(!subbasinParameterDomList.isEmpty()) {
            subbasinParameterDomList.add(0, h2(attrs(".global-header"), "Global Parameter Summary - Subbasin"));
        } // Add section title if there is subbasin global parameter

        /* Return a 'div' that contains all Subbasin's parameter tables */
        return div(attrs(tdAttribute), subbasinParameterDomList.toArray(new DomContent[]{}));
    } // printSubbasinParameterList()
    private DomContent printReachParameterList(List<Element> reachElements, List<String> processChoices) {
        /* Return 'null' if processChoices does not contain 'route' */
        if(!processChoices.contains(StringBeautifier.beautifyString("route"))) { return null; }

        /* Reach's Parameter (Route). DomList contains all tables */
        List<DomContent> reachParameterDomList = new ArrayList<>();

        /* Split the Reach-type Elements by their respective method type (Muskingnum, etc...) */
        List<Element> muskingumMethod = reachElements.stream()
                .filter(element -> getRouteMethod(element).equals("MUSKINGUM"))
                .collect(Collectors.toList());
        List<Element> muskingumCungeMethod = reachElements.stream()
                .filter(element -> getRouteMethod(element).equals("MUSKINGUM_CUNGE"))
                .collect(Collectors.toList());

        /* For each method, call their respective function to get a Table DOM */
        if(!muskingumMethod.isEmpty()) {
            DomContent muskingumTableDom = printMuskingumTable(muskingumMethod);
            reachParameterDomList.add(muskingumTableDom);
        } // If: There are Reach Elements with Muskingum Method
        if(!muskingumCungeMethod.isEmpty()) {
            DomContent muskingumCungeTableDom = printMuskingumCungeTable(muskingumCungeMethod);
            reachParameterDomList.add(muskingumCungeTableDom);
        } // If: There are Reach Elements with Muskingum-Cunge Method

        /* Add headings for Global Parameter Summary - Reach */
        if(!reachParameterDomList.isEmpty()) {
            reachParameterDomList.add(0, h2(attrs(".global-header"), "Global Parameter Summary - Reach"));
        } // Add section title if there is subbasin global parameter

        return div(attrs(".global-parameter"), reachParameterDomList.toArray(new DomContent[]{}));
    } // printReachParameterList()
    private DomContent printJunctionParameterList(List<Element> junctionElements, List<String> processChoices) {
        /* Junction-type Basins don't have Parameter Tables */

        return null;
    } // printJunctionParameterList()
    private DomContent printSinkParameterList(List<Element> sinkElements, List<String> processChoices) {
        /* Sink-type Basins don't have Parameter Tables */
        return null;
    } // printSinkParameterList()
    private DomContent printSourceParameterList(List<Element> sourceElements, List<String> processChoices) {
        /* Source-type Basins don't have Parameter Tables */

        return null;
    } // printSourceParameterList()
    private DomContent printReservoirParameterList(List<Element> reservoirElements, List<String> processChoices) {
        /* Reservoir-type Basins don't have Parameter Tables */
        return null;
    } // printReservoirParameterList()
    private DomContent printMuskingumTable(List<Element> muskingumMethod) {
        List<DomContent> tableRows = new ArrayList<>();
        String tdAttribute = ".global-parameter";

        /* Loop through all Elements with Muskingum route method */
        for(Element element : muskingumMethod) {
            /* Get all information necessary for this element's row */
            Map<String, Process> elementProcesses = element.getElementInput().getProcesses().stream()
                    .collect(Collectors.toMap(Process::getName, x -> x));
            Map<String, Parameter> routeParameters = elementProcesses.get("route").getParameters().stream()
                    .collect(Collectors.toMap(Parameter::getName, x -> x));

            List<String> routeRowList = new ArrayList<>();
            routeRowList.add(element.getName()); // Element Name
            routeRowList.add(StringBeautifier.beautifyString(routeParameters.get("initialVariable").getValue())); // Initial Type
            routeRowList.add(routeParameters.get("k").getValue()); // K value
            routeRowList.add(routeParameters.get("x").getValue()); // X value
            routeRowList.add(routeParameters.get("steps").getValue()); // Number of Subreaches

            tableRows.add(HtmlModifier.printTableDataRow(routeRowList, tdAttribute, tdAttribute));
        } // Loop: through each element

        /* Creating the table's header */
        String methodName  = StringBeautifier.beautifyString(getRouteMethod(muskingumMethod.get(0)));
        List<String> headerList = Arrays.asList("Reach", "Initial Type", methodName + " K (HR)", methodName + " X", "Number of Subreaches");
        tableRows.add(0, HtmlModifier.printTableHeadRow(headerList, tdAttribute, tdAttribute));
        tableRows.add(0, caption("Route - " + methodName + " Method"));

        return table(attrs(tdAttribute), tableRows.toArray(new DomContent[]{}));
    } // printMuskingumTable()
    private DomContent printMuskingumCungeTable(List<Element> muskingumCungeMethod) {
        List<DomContent> tableRows = new ArrayList<>();
        String tdAttribute = ".global-parameter";

        /* Loop through all Elements with Muskingum-Cunge route method */
        for(Element element : muskingumCungeMethod) {
            /* Get all information necessary for this element's row */
            Map<String, Process> elementProcesses = element.getElementInput().getProcesses().stream()
                    .collect(Collectors.toMap(Process::getName, x -> x));
            Map<String, Parameter> routeParameters = elementProcesses.get("route").getParameters().stream()
                    .collect(Collectors.toMap(Parameter::getName, x -> x));
            Map<String, Parameter> channelParameters = routeParameters.get("channel").getSubParameters().stream()
                    .collect(Collectors.toMap(Parameter::getName, x -> x));

            List<String> routeRowList = new ArrayList<>();
            routeRowList.add(element.getName()); // Element Name
            routeRowList.add(StringBeautifier.beautifyString(routeParameters.get("initialVariable").getValue())); // Initial Type
            routeRowList.add(channelParameters.get("length").getValue()); // Length (FT)
            routeRowList.add(channelParameters.get("energySlope").getValue()); // Slope (FT/FT)
            routeRowList.add(channelParameters.get("manningsN").getValue()); // Manning's n
            routeRowList.add(StringBeautifier.beautifyString(routeParameters.get("spaceTimeMethod").getValue())); // Space-Time Method
            routeRowList.add(StringBeautifier.beautifyString(routeParameters.get("indexParameterType").getValue())); // Index Method
            routeRowList.add(routeParameters.get("indexFlow").getValue()); // Index Flow (CFS)
            routeRowList.add(StringBeautifier.beautifyString(routeParameters.get("channelType").getValue())); // Shape

            tableRows.add(HtmlModifier.printTableDataRow(routeRowList, tdAttribute, tdAttribute));
        } // Loop: through each element

        /* Creating the table's header */
        String methodName  = StringBeautifier.beautifyString(getRouteMethod(muskingumCungeMethod.get(0)));
        List<String> headerList = Arrays.asList("Reach", "Initial Type", "Length (FT)", "Slope (FT/FT)", "Manning's n", "Space-Time Method", "Index Method", "Index Flow", "Shape");
        tableRows.add(0, HtmlModifier.printTableHeadRow(headerList, tdAttribute, tdAttribute));
        tableRows.add(0, caption("Route - " + methodName + " Method"));

        return table(attrs(tdAttribute), tableRows.toArray(new DomContent[]{}));
    } // printMuskingumCungeTable()

    /* Helper functions */
    private Map<String, List<Element>> separateElementsByType(List<Element> listElement) {
        Map<String, List<Element>> separatedElementMap = new HashMap<>();

        List<Element> subbasinElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().equals("SUBBASIN"))
                .collect(Collectors.toList());
        separatedElementMap.put("Subbasin", subbasinElements);

        List<Element> reachElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().equals("REACH"))
                .collect(Collectors.toList());
        separatedElementMap.put("Reach", reachElements);

        List<Element> junctionElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().equals("JUNCTION"))
                .collect(Collectors.toList());
        separatedElementMap.put("Junction", junctionElements);

        List<Element> sinkElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().equals("SINK"))
                .collect(Collectors.toList());
        separatedElementMap.put("Sink", sinkElements);

        List<Element> sourceElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().equals("SOURCE"))
                .collect(Collectors.toList());
        separatedElementMap.put("Source", sourceElements);

        List<Element> reservoirElements = listElement.stream()
                .filter(element -> element.getElementInput().getElementType().equals("RESERVOIR"))
                .collect(Collectors.toList());
        separatedElementMap.put("Reservoir", reservoirElements);

        return separatedElementMap;
    } // separateElementsByType()
    private String getRouteMethod(Element element) {
        Map<String, Process> elementProcesses = element.getElementInput().getProcesses().stream()
                .collect(Collectors.toMap(Process::getName, x -> x));
        Map<String, Parameter> routeParameters = elementProcesses.get("route").getParameters().stream()
                .collect(Collectors.toMap(Parameter::getName, x -> x));

        return routeParameters.get("method").getValue();
    } // getRouteMethod()
    private DomContent printBaseflowTableDataRow(List<String> dataRow, String tdAttribute, String trAttribute) {
        List<DomContent> domList = new ArrayList<>();

        for(String data : dataRow) {
            String reformatString = StringBeautifier.beautifyString(data);
            DomContent dataDom = td(attrs(tdAttribute), b(reformatString)); // Table Data type
            domList.add(dataDom);
        } // Convert 'data' to Dom

        return tr(attrs(trAttribute), domList.toArray(new DomContent[]{})); // Table Row type
    } // printTableDataRow()
} // GlobalParametersWriter()

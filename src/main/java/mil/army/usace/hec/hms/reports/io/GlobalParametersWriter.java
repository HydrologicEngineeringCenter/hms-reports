package mil.army.usace.hec.hms.reports.io;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.Parameter;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;
import mil.army.usace.hec.hms.reports.util.ValidCheck;

import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;
import static j2html.TagCreator.attrs;

public class GlobalParametersWriter {
    private List<Element> elementList;
    private List<ReportWriter.SummaryChoice> reportSummaryChoice;
    private Map<String, List<String>> globalParameterChoices; // {"Subbasin", ["Canopy", "Loss", etc...]}

    /* Constructors */
    private GlobalParametersWriter(Builder builder){
        this.elementList = builder.elementList;
        this.reportSummaryChoice = builder.reportSummaryChoice;
        this.globalParameterChoices = builder.globalParameterChoices;
    } // GlobalParametersWriter Constructor
    public static class Builder{
        List<Element> elementList;
        List<ReportWriter.SummaryChoice> reportSummaryChoice;
        private Map<String, List<String>> globalParameterChoices;

        public Builder elementList(List<Element> elementList){
            this.elementList = elementList;
            return this;
        } // 'elementList' constructor

        public Builder reportSummaryChoice(List<ReportWriter.SummaryChoice> reportSummaryChoice) {
            this.reportSummaryChoice = reportSummaryChoice;
            return this;
        } // 'reportSummaryChoice' constructor

        public Builder globalParameterChoices(Map<String, List<String>> globalParameterChoices) {
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
        if(reportSummaryChoice == null || !reportSummaryChoice.contains(ReportWriter.SummaryChoice.GLOBAL_PARAMETER_SUMMARY)) {
            return null;
        } // If: Report Summary Choice contains PARAMETER_SUMMARY

        List<DomContent> globalParameterDomList = new ArrayList<>();
        Map<String, List<Element>> separatedElements = separateElementsByType(this.elementList);
        /* Print out the Global Parameter choices that the user chose */
        for(String chosenType : this.globalParameterChoices.keySet()) {
            switch (chosenType) {
                case "Subbasin": {
                    DomContent parameterTable = printSubbasinParameterList(separatedElements.get("Subbasin"), globalParameterChoices.get("Subbasin"));
                    globalParameterDomList.add(parameterTable);
                    break;
                }
                case "Reach": {
                    DomContent parameterTable = printReachParameterList(separatedElements.get("Reach"), globalParameterChoices.get("Reach"));
                    globalParameterDomList.add(parameterTable);
                    break;
                }
                case "Junction": {
                    DomContent parameterTable = printJunctionParameterList(separatedElements.get("Junction"), globalParameterChoices.get("Junction"));
                    globalParameterDomList.add(parameterTable);
                    break;
                }
                case "Sink": {
                    DomContent parameterTable = printSinkParameterList(separatedElements.get("Sink"), globalParameterChoices.get("Sink"));
                    globalParameterDomList.add(parameterTable);
                    break;
                }
                case "Source": {
                    DomContent parameterTable = printSourceParameterList(separatedElements.get("Source"), globalParameterChoices.get("Source"));
                    globalParameterDomList.add(parameterTable);
                    break;
                }
                case "Reservoir": {
                    DomContent parameterTable = printReservoirParameterList(separatedElements.get("Reservoir"), globalParameterChoices.get("Reservoir"));
                    globalParameterDomList.add(parameterTable);
                    break;
                }
                default:
                    System.out.println("Element Type is not Supported");
                    return null;
            } // Switch Case: for each element type
        } // Loop: through the user's chosen global parameters

        return div(attrs(".global-parameter"), globalParameterDomList.toArray(new DomContent[]{}));
    } // printListGlobalParameter

    /* Printing Parameter Table (Subbasin/Reach/etc...) */
    private DomContent printSubbasinParameterList(List<Element> subbasinElements, List<String> processChoices) {
        /* TODO: Subbasin's parameters (Area, Loss Rate, Canopy, Transform, Baseflow) */
        List<DomContent> subbasinParameterDomList = new ArrayList<>(); // Contains (Area table, Loss Rate table, etc...)
        /* For each table: Loop through subbasinElements List, get necessary data to get a DomContent table */
        List<String> areaRowList = new ArrayList<>();
        List<String> lossRowList = new ArrayList<>();
        List<String> canopyRowList = new ArrayList<>();
        List<String> transformRowList = new ArrayList<>();
        Map<String, List<String>> baseflowRowList = new HashMap<>();

        for(Element element : subbasinElements) {
            /* Map of the element's list of processes */
            Map<String, Process> elementProcesses = element.getElementInput().getProcesses().stream()
                    .collect(Collectors.toMap(Process::getName, x -> x));

            /* Parameter Table: Area */
            areaRowList.add(element.getName());
            areaRowList.add(elementProcesses.get("area").getValue()); // Area

            /* Parameter Table: Loss Rate */
            Map<String, Parameter> lossParameters = elementProcesses.get("lossRate").getParameters().stream()
                    .collect(Collectors.toMap(Parameter::getName, x -> x));

            lossRowList.add(element.getName());
            lossRowList.add(lossParameters.get("initialDeficit").getValue());  // Initial Deficit (IN)
            lossRowList.add(lossParameters.get("maximumDeficit").getValue());  // Maximum Storage (IN)
            lossRowList.add(lossParameters.get("percolationRate").getValue()); // Constant Rate (IN/HR)
            lossRowList.add(lossParameters.get("percentImperviousArea").getValue()); // Impervious (%)

            /* Parameter Table: Canopy */
            Map<String, Parameter> canopyParameters = elementProcesses.get("canopy").getParameters().stream()
                    .collect(Collectors.toMap(Parameter::getName, x -> x));

            canopyRowList.add(element.getName());
            canopyRowList.add(canopyParameters.get("initialStorage").getValue() + "%");  // Initial Storage (%)
            canopyRowList.add(canopyParameters.get("storageCapacity").getValue()); // Max Storage (IN)
            canopyRowList.add(canopyParameters.get("cropCoefficient").getValue()); // Crop Coefficient
            String evapotranspiration = canopyParameters.get("allowSimultaneousPrecipEt").getValue();
            if(evapotranspiration.equals("false")) { canopyRowList.add("Only Dry Periods"); }
            else { canopyRowList.add("Positive Periods ----"); } // Evapotranspiration
            canopyRowList.add(StringBeautifier.beautifyString(canopyParameters.get("uptakeMethod").getValue())); // Uptake Method

            /* Parameter Table: Transform */
            Map<String, Parameter> transformParameters = elementProcesses.get("transform").getParameters().stream()
                    .collect(Collectors.toMap(Parameter::getName, x -> x));

            transformRowList.add(element.getName());
            transformRowList.add(transformParameters.get("timeOfConcentration").getValue()); // Time of Concentration (HR)
            transformRowList.add(transformParameters.get("storageCoefficient").getValue());  // Storage Coefficient (HR)

            /* Parameter Table: Baseflow */
            Map<String, Parameter> baseflowParameters = elementProcesses.get("baseflow").getParameters().stream()
                    .collect(Collectors.toMap(Parameter::getName, x -> x));

        } // Loop: through all subbasin-type elements

        /* Call function to get a DomContent table. (Baseflow needs a unique table) */

        return null;
    } // printSubbasinParameterList()
    private DomContent printReachParameterList(List<Element> reachElements, List<String> processChoices) {

        return null;
    } // printReachParameterList()
    private DomContent printJunctionParameterList(List<Element> junctionElements, List<String> processChoices) {

        return null;
    } // printJunctionParameterList()
    private DomContent printSinkParameterList(List<Element> sinkElements, List<String> processChoices) {

        return null;
    } // printSinkParameterList()
    private DomContent printSourceParameterList(List<Element> sourceElements, List<String> processChoices) {

        return null;
    } // printSourceParameterList()
    private DomContent printReservoirParameterList(List<Element> reservoirElements, List<String> processChoices) {

        return null;
    } // printReservoirParameterList()

    /* Getting the necessary data (Can probably get rid of these functions) */
    private DomContent printSubbasinParameterTable(Element element, List<String> processChoices) {
        return null;
    } // printSubbasinParameterTable()
    private DomContent printReachParameterTable(Element element) {
        return null;
    } // printReachParameterTable()
    private DomContent printJunctionParameterTable(Element element) {
        return null;
    } // printJunctionParameterTable()
    private DomContent printSinkParameterTable(Element element) {
        return null;
    } // printSinkParameterTable()
    private DomContent printSourceParameterTable(Element element) {
        return null;
    } // printSourceParameterTable()
    private DomContent printReservoirParameterTable(Element element) {
        return null;
    } // printReservoirParameterTable()

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

}

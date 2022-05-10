package mil.army.usace.hec.hms.reports.io.standard;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.StatisticResult;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.util.HtmlUtil;
import mil.army.usace.hec.hms.reports.util.StringUtil;

import java.util.*;

import static j2html.TagCreator.*;

public class GlobalResultsWriter {
    private final SimulationType simulationType;
    private final List<Element> elementList;
    private final List<SummaryChoice> reportSummaryChoice;

    /* Constructors */
    private GlobalResultsWriter(Builder builder){
        this.simulationType = builder.simulationType;
        this.elementList = builder.elementList;
        this.reportSummaryChoice = builder.reportSummaryChoice;
    } // GlobalResultsWriter Constructor

    public static class Builder{
        SimulationType simulationType;
        List<Element> elementList;
        List<SummaryChoice> reportSummaryChoice;

        public Builder simulationType(SimulationType simulationType){
            this.simulationType = simulationType;
            return this;
        } // 'simulationType' constructor

        public Builder elementList(List<Element> elementList){
            this.elementList = elementList;
            return this;
        } // 'elementList' constructor

        public Builder reportSummaryChoice(List<SummaryChoice> reportSummaryChoice) {
            this.reportSummaryChoice = reportSummaryChoice;
            return this;
        } // 'reportSummaryChoice' constructor

        public GlobalResultsWriter build(){
            return new GlobalResultsWriter(this);
        }
    } // Builder class: as GlobalResultsWriter's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Global Summary Table */
    DomContent printGlobalSummary() {
        if(reportSummaryChoice == null || !reportSummaryChoice.contains(SummaryChoice.GLOBAL_RESULTS_SUMMARY)) {
            return null;
        } // If: Report Summary Choice contains GLOBAL_SUMMARY

        List<DomContent> globalSummaryDomList = new ArrayList<>();
        String tdAttribute = ".global-summary";

        List<String> drainageAreaUnits  = new ArrayList<>();
        List<String> peakDischargeUnits = new ArrayList<>();
        List<String> volumeUnits        = new ArrayList<>();

        for(Element element : this.elementList) {
            ElementResults elementResults = element.getElementResults();
            if(elementResults == null) continue;

            /* Skip Elements that are not Analysis Points for Depth Area Simulations */
            if(simulationType == SimulationType.DEPTH_AREA) {
                String isPoint = elementResults.getOtherResults().getOrDefault("isAnalysisPoint", "false");
                if(!Boolean.parseBoolean(isPoint)) {
                    continue;
                }
            }

            /* Skip Elements without ElementResults */
            if(elementResults == null)
                continue;

            List<String> rowData = new ArrayList<>();

            Map<String, String> otherResultsMap = element.getElementResults().getOtherResults();

            StatisticResult peakDischarge = elementResults.getStatisticSResult("Maximum Outflow");
            StatisticResult timeOfPeak    = elementResults.getStatisticSResult("Time of Maximum Outflow");
            StatisticResult volume        = elementResults.getStatisticSResult("Outflow Depth");

            rowData.add(element.getName()); // Element Name
            rowData.add(otherResultsMap.get("DrainageArea")); // Drainage Area
            rowData.add(peakDischarge.getValue()); // Peak Discharge
            rowData.add(timeOfPeak.getValue()); // Time of Peak
            rowData.add(volume.getValue()); // Volume

            drainageAreaUnits.add(otherResultsMap.get("DrainageAreaUnits"));
            peakDischargeUnits.add(peakDischarge.getUnits());
            volumeUnits.add(volume.getUnits());

            rowData.replaceAll(t -> Objects.isNull(t) ? "N/A" : t);
            rowData.replaceAll(t -> t.equals("") ? "Not specified" : t);

            DomContent rowDom = HtmlUtil.printTableDataRow(rowData, tdAttribute, tdAttribute);
            globalSummaryDomList.add(rowDom);
        } // Loop: through each element

        /* Adding Head of the Table if there is a table */
        if(!globalSummaryDomList.isEmpty()) {
            String drainageAreaUnit  = StringUtil.mostOccurredString(drainageAreaUnits);
            String peakDischargeUnit = StringUtil.mostOccurredString(peakDischargeUnits);
            String volumeUnit        = StringUtil.mostOccurredString(volumeUnits);

            DomContent head = HtmlUtil.printTableHeadRow(Arrays.asList("Hydrologic Element", "Drainage Area (" + drainageAreaUnit + ")",
                    "Peak Discharge (" + peakDischargeUnit + ")", "Time of Peak", "Volume (" + volumeUnit + ")"), tdAttribute, tdAttribute);
            globalSummaryDomList.add(0, head); // Add to front
        } // If: There is a table

        DomContent globalSummaryTable = table(attrs(tdAttribute), globalSummaryDomList.toArray(new DomContent[]{}));
        globalSummaryDomList.clear();
        globalSummaryDomList.add(h2(attrs(tdAttribute), "Global Results Summary"));
        globalSummaryDomList.add(globalSummaryTable);

        return div(attrs(tdAttribute), globalSummaryDomList.toArray(new DomContent[]{}));
    } // printGlobalSummary()
}

package mil.army.usace.hec.hms.reports.io;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static j2html.TagCreator.*;

public class GlobalResultsWriter {
    private List<Element> elementList;
    private List<ReportWriter.SummaryChoice> reportSummaryChoice;

    /* Constructors */
    private GlobalResultsWriter(Builder builder){
        this.elementList = builder.elementList;
        this.reportSummaryChoice = builder.reportSummaryChoice;
    } // GlobalResultsWriter Constructor
    public static class Builder{
        List<Element> elementList;
        List<ReportWriter.SummaryChoice> reportSummaryChoice;

        public Builder elementList(List<Element> elementList){
            this.elementList = elementList;
            return this;
        } // 'elementList' constructor

        public Builder reportSummaryChoice(List<ReportWriter.SummaryChoice> reportSummaryChoice) {
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
        if(reportSummaryChoice == null || !reportSummaryChoice.contains(ReportWriter.SummaryChoice.GLOBAL_RESULTS_SUMMARY)) {
            return null;
        } // If: Report Summary Choice contains GLOBAL_SUMMARY

        List<DomContent> globalSummaryDomList = new ArrayList<>();
        String tdAttribute = ".global-summary";

        for(Element element : this.elementList) {
            List<String> rowData = new ArrayList<>();
            rowData.add(element.getName()); // Element Name
            rowData.add(element.getElementResults().getOtherResults().get("DrainageArea")); // Drainage Area
            rowData.add(element.getElementResults().getStatisticResultsMap().get("Maximum Outflow")); // Peak Discharge
            rowData.add(element.getElementResults().getStatisticResultsMap().get("Time of Maximum Outflow")); // Time of Peak
            rowData.add(element.getElementResults().getStatisticResultsMap().get("Outflow Depth")); // Volume

            rowData.replaceAll(t -> Objects.isNull(t) ? "N/A" : t);
            rowData.replaceAll(t -> t.equals("") ? "Not specified" : t);

            DomContent rowDom = HtmlModifier.printTableDataRow(rowData, tdAttribute, tdAttribute);
            globalSummaryDomList.add(rowDom);
        }

        /* Adding Head of the Table if there is a table */
        if(!globalSummaryDomList.isEmpty()) {
            DomContent head = HtmlModifier.printTableHeadRow(Arrays.asList("Hydrologic Element", "Drainage Area (MI2)",
                    "Peak Discharge (CFS)", "Time of Peak", "Volume (IN)"), tdAttribute, tdAttribute);
            globalSummaryDomList.add(0, head); // Add to front
        } // If: There is a table

        DomContent globalSummaryTable = table(attrs(tdAttribute), globalSummaryDomList.toArray(new DomContent[]{}));
        globalSummaryDomList.clear();
        globalSummaryDomList.add(h2(attrs(tdAttribute), "Global Summary"));
        globalSummaryDomList.add(globalSummaryTable);

        return div(attrs(tdAttribute), globalSummaryDomList.toArray(new DomContent[]{}));
    } // printGlobalSummary()
}

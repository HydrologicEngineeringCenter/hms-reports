package mil.army.usace.hec.hms.reports.io.standard;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.io.BasinParser;
import mil.army.usace.hec.hms.reports.io.ReportWriter;
import mil.army.usace.hec.hms.reports.util.HtmlUtil;
import mil.army.usace.hec.hms.reports.util.StringUtil;
import mil.army.usace.hec.hms.reports.util.Utilities;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static j2html.TagCreator.*;

public class StandardReportWriter extends ReportWriter {
    private final Double basinParserPercent = 40.0;
    private final Double otherPercent = 60.0;

    public StandardReportWriter(Builder builder) {
        super(builder);
        support = new PropertyChangeSupport(this);
    }

    @Override
    public List<Element> write() {
        /* Parse elements */
        BasinParser parser = BasinParser.builder()
                .pathToBasinInputFile(this.pathToInput.toAbsolutePath().toString())
                .pathToBasinResultsFile(this.pathToResult.toAbsolutePath().toString())
                .pathToProjectDirectory(this.projectDirectory.toAbsolutePath().toString())
                .simulationType(simulationType)
                .build();
        parser.addPropertyChangeListener(evt -> {
            if((evt.getSource() instanceof BasinParser) && (evt.getPropertyName().equals("Progress"))) {
                if(evt.getNewValue() instanceof Double) {
                    Double progressValue = (Double) evt.getNewValue() * basinParserPercent;
                    support.firePropertyChange("Progress", "", progressValue);
                }
            }
        });

        /* Check whether the simulation results was computed after the basin file or not */
        if(parser.outdatedSimulation()) {
            support.firePropertyChange("Error", "", "Data Changed, Recompute");
            return new ArrayList<>();
        } // If: User need to recompute

        /* Writing the Standard Report */
        List<Element> elementList = parser.getElements();

        GlobalResultsWriter globalResultsWriter = GlobalResultsWriter.builder()
                .elementList(elementList)
                .reportSummaryChoice(this.reportSummaryChoice)
                .build();

        GlobalParametersWriter globalParametersWriter = GlobalParametersWriter.builder()
                .elementList(elementList)
                .reportSummaryChoice(this.reportSummaryChoice)
                .globalParameterChoices(this.globalParameterChoices)
                .build();

        /* HTML Layout */
        String htmlOutput = html(
                head(   title("Standard Report"),
                        link().withRel("stylesheet").withHref("styleStandard.css"),
                        script().withSrc("https://cdn.plot.ly/plotly-latest.min.js")),
                body(   printReportTitle(parser),
                        globalParametersWriter.printListGlobalParameter(),
                        globalResultsWriter.printGlobalSummary(),
                        printElementList(elementList))
        ).renderFormatted();

        /* Writing to HTML output file */
        HtmlUtil.writeStandardReportToFile(this.pathToDestination.toString(), htmlOutput);

        /* Notify HMS that the report has been successfully generated */
        support.firePropertyChange("Message", "", "Success");

        return elementList;
    } // write()

    private DomContent printElementList(List<Element> elementList) {
        ElementParametersWriter elementParametersWriter = ElementParametersWriter.builder()
                .elementList(elementList)
                .elementParameterizationChoice(this.elementParameterizationChoice)
                .build();

        ElementResultsWriter elementResultsWriter = ElementResultsWriter.builder()
                .elementList(elementList)
                .reportSummaryChoice(reportSummaryChoice)
                .chosenPlots(chosenPlots)
                .build();

        elementResultsWriter.addPropertyChangeListener(evt -> {
            if((evt.getSource() instanceof ElementResultsWriter) && (evt.getPropertyName().equals("Progress"))) {
                if(evt.getNewValue() instanceof Double) {
                    Double progressValue = (Double) evt.getNewValue() * otherPercent + basinParserPercent;
                    support.firePropertyChange("Progress", "", progressValue);
                }
            } // If: Progress from ElementResultsWriter
        }); // For Progress Bar

        List<DomContent> elementListDom = new ArrayList<>();
        Map<String, DomContent> elementInputMap = elementParametersWriter.elementInputMap();
        Map<String, DomContent> elementResultsMap = elementResultsWriter.elementResultsMap();

        for(String elementName : elementInputMap.keySet()) {
            List<DomContent> elementDom = new ArrayList<>();
            DomContent elementInputDom = elementInputMap.get(elementName);
            DomContent elementResultsDom = elementResultsMap.get(elementName);

            if(elementInputDom != null) { elementDom.add(elementInputDom); }
            if(elementResultsDom != null) { elementDom.add(elementResultsDom); }

            elementListDom.add(div(attrs(".element"), elementDom.toArray(new DomContent[]{})));
        } // Loop: to get each Element's DomContent

        return main(elementListDom.toArray(new DomContent[]{}));
    } // printElementList()

    private DomContent printReportTitle(BasinParser basinParser) {
        List<DomContent> reportTitleDom = new ArrayList<>();
        List<DomContent> firstSectionDom = new ArrayList<>();
        List<DomContent> secondSectionDom = new ArrayList<>();

        /* Project Name */
        String projectName = Utilities.getFilePath(projectDirectory.toAbsolutePath().toString(), ".hms");
        projectName = projectName.substring(projectName.lastIndexOf(File.separator) + 1, projectName.indexOf(".hms"));
        DomContent projectTitle = h2(join(b("Project: "), StringUtil.beautifyString(projectName.trim())));

        /* Simulation Name */
        String simulation = simulationType.getTitle();
        Map<String, String> simulationData = basinParser.getSimulationData();
        DomContent simulationTitle = h2(join(b(simulation), simulationData.get("name").trim()));

        /* HMS Version Computed With */
        String hmsVersionNumber = basinParser.getHmsVersion();
        DomContent hmsVersion = h2(join(b("HMS Version: "), hmsVersionNumber));

        /* Start, End, and Execution Times */
        DomContent startTime = h2(join(b("Simulation Start: "), simulationData.get("start").trim()));
        DomContent endTime   = h2(join(b("Simulation End: "), simulationData.get("end").trim()));
        DomContent executionTime = h2(join(b("Executed: "), simulationData.get("execution")));

        /* First Section Dom */
        firstSectionDom.add(projectTitle);
        firstSectionDom.add(simulationTitle);
        firstSectionDom.add(startTime);
        firstSectionDom.add(endTime);
        DomContent firstDom = div(attrs("#first-section.report-title "), firstSectionDom.toArray(new DomContent[]{}));

        /* White Space Dom */
        DomContent whiteSpaceDom = div(attrs("#title-white-space"));

        /* Second Section Dom */
        secondSectionDom.add(hmsVersion);
        secondSectionDom.add(executionTime);
        DomContent secondDom = div(attrs("#second-section.report-title"), secondSectionDom.toArray(new DomContent[]{}));

        /* Adding All Together */
        reportTitleDom.add(firstDom);
        reportTitleDom.add(whiteSpaceDom);
        reportTitleDom.add(secondDom);

        return div(attrs(".report-header"), reportTitleDom.toArray(new DomContent[]{}));
    } // printReportTitle()

} // StandardReportWriter class

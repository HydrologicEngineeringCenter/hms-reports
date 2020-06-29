package mil.army.usace.hec.hms.reports.io.standard;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.io.BasinParser;
import mil.army.usace.hec.hms.reports.io.ReportWriter;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;
import mil.army.usace.hec.hms.reports.util.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static j2html.TagCreator.*;

public class StandardReportWriter extends ReportWriter {
    public StandardReportWriter(Builder builder) {
        super(builder);
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

        ElementParametersWriter elementParametersWriter = ElementParametersWriter.builder()
                .elementList(elementList)
                .chosenPlots(this.chosenPlots)
                .reportSummaryChoice(this.reportSummaryChoice)
                .elementParameterizationChoice(this.elementParameterizationChoice)
                .build();

        /* HTML Layout */
        String htmlOutput = html(
                head(   title("Standard Report"),
                        link().withRel("stylesheet").withHref("styleStandard.css"),
                        script().withSrc("https://cdn.plot.ly/plotly-latest.min.js")),
                body(   printReportTitle(parser),
                        globalParametersWriter.printListGlobalParameter(),
                        globalResultsWriter.printGlobalSummary(),
                        elementParametersWriter.printElementList())
        ).renderFormatted();
        /* Writing to HTML output file */
        HtmlModifier.writeStandardReportToFile(this.pathToDestination.toString(), htmlOutput);

        return elementList;
    } // write()

    private DomContent printReportTitle(BasinParser basinParser) {
        List<DomContent> reportTitleDom = new ArrayList<>();

        String projectName = Utilities.getFilePath(projectDirectory.toAbsolutePath().toString(), ".hms");
        projectName = projectName.substring(projectName.lastIndexOf(File.separator) + 1, projectName.indexOf(".hms"));
        DomContent projectTitle = h3(join(b("Project: "), StringBeautifier.beautifyString(projectName.trim())));
        reportTitleDom.add(projectTitle);

        String simulation = getSimulationTitle();
        String simulationName = basinParser.getSimulationName();
        DomContent simulationTitle = h3(join(b(simulation), simulationName.trim()));
        reportTitleDom.add(simulationTitle);

        return div(attrs(".report-title"), reportTitleDom.toArray(new DomContent[]{}));
    } // printReportTitle()

    private String getSimulationTitle() {
        String simulationName;
        switch(simulationType) {
            case RUN:
                simulationName = "Simulation Run: ";
                break;
            case FORECAST:
                simulationName = "Forecast Trial: ";
                break;
            case OPTIMIZATION:
                simulationName = "Optimization Trial: ";
                break;
            case DEPTH_AREA:
                simulationName = "Depth Area Trial: ";
                break;
            case MONTE_CARLO:
                simulationName = "Monte Carlo Trial: ";
                break;
            default:
                simulationName = "<Unknown Simulation Type>: ";
                break;
        } // Switch Case

        return simulationName;
    } // getSimulationTitle()

} // StandardReportWriter class

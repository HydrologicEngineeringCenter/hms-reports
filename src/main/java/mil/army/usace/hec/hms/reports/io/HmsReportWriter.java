package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.*;
import mil.army.usace.hec.hms.reports.util.*;

import java.util.*;

import static j2html.TagCreator.*;

public class HmsReportWriter extends ReportWriter {
    HmsReportWriter(Builder builder) {
        super(builder);
    }

    @Override
    public void write() {
        /* Parse elements */
        BasinParser parser = BasinParser.builder()
                .pathToBasinInputFile(this.pathToInput.toAbsolutePath().toString())
                .pathToBasinResultsFile(this.pathToResult.toAbsolutePath().toString())
                .pathToProjectDirectory(this.projectDirectory.toAbsolutePath().toString())
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
                .build();

        ElementResultsWriter elementResultsWriter = ElementResultsWriter.builder()
                .elementList(elementList)
                .reportSummaryChoice(this.reportSummaryChoice)
                .build();

        /* HTML Layout */
        String htmlOutput = html(
                head(   title("Standardized Report"),
                        link().withRel("stylesheet").withHref("style.css"),
                        script().withSrc("https://cdn.plot.ly/plotly-latest.min.js")),
                body(   globalResultsWriter.printGlobalSummary(),
                        globalParametersWriter.printListGlobalParameter(),
                        elementResultsWriter.printListResultsWriter(),
                        elementParametersWriter.printElementList())
        ).renderFormatted();
        /* Writing to HTML output file */
        HtmlModifier.writeToFile(this.pathToDestination.toString(), htmlOutput);
    } // write()

} // HmsReportWriter class

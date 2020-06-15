package mil.army.usace.hec.hms.reports.io.statistics;

import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.DisplayRange;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.StatisticsType;
import mil.army.usace.hec.hms.reports.io.BasinParser;
import mil.army.usace.hec.hms.reports.io.ReportWriter;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;

import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class SummaryStatisticsReportWriter extends ReportWriter {
    private String rmseStdev = "Observed Flow RMSE Stdev";
    private String nashSutcliffe = "Observed Flow Nash Sutcliffe";
    private String percentBias = "Observed Flow Percent Bias";

    public SummaryStatisticsReportWriter(Builder builder) {
        super(builder);
    }

    @Override
    public List<Element> write() {
        /* Parse elements */
        BasinParser parser = BasinParser.builder()
                .pathToBasinInputFile(this.pathToInput.toAbsolutePath().toString())
                .pathToBasinResultsFile(this.pathToResult.toAbsolutePath().toString())
                .pathToProjectDirectory(this.projectDirectory.toAbsolutePath().toString())
                .build();

        List<Element> elementList = parser.getElements();
        List<Element> statisticsElementList = elementList.stream().filter(this::isSummaryStatistics).collect(Collectors.toList());
        if(statisticsElementList.isEmpty()) { return statisticsElementList; }

        /* HTML Layout */
        String htmlOutput = html(
                head(title("Summary Statistics Report"), link().withRel("stylesheet").withHref("styleStatistics.css")),
                body(printSummaryStatisticsTable(statisticsElementList))
        ).renderFormatted();
        /* Writing to HTML output file */
        HtmlModifier.writeStatisticsReportToFile(this.pathToDestination.toString(), htmlOutput);

        return statisticsElementList;
    } // write()

    private boolean isSummaryStatistics(Element element) {
        Map<String, String> statisticResults = element.getElementResults().getStatisticResultsMap();
        return statisticResults.containsKey(rmseStdev) && statisticResults.containsKey(nashSutcliffe) && statisticResults.containsKey(percentBias);
    } // getStatisticsElement()

    private DomContent printSummaryStatisticsTable(List<Element> statisticsElementList) {
        List<DomContent> rowDomList = new ArrayList<>();
        String tdAttribute = ".summary-statistics";

        for(Element element : statisticsElementList) {
            Map<String, String> statisticsMap = element.getElementResults().getStatisticResultsMap();
            Map<String, String> colorMap = classifyStatisticsColor(element);
            List<DomContent> dataDomList = new ArrayList<>();
            dataDomList.add(td(element.getName()));

            for(String statisticsType : colorMap.keySet()) {
                String statisticsData = statisticsMap.get(statisticsType);
                String reformatData = StringBeautifier.beautifyString(statisticsData);
                DomContent dataButton = button(reformatData).withStyle("background-color:" + colorMap.get(statisticsType));
                DomContent dataDom = td(dataButton);
                dataDomList.add(dataDom);

                /* FIXME: Add to test CSS design with five columns */
                if(statisticsType.equals(percentBias)) { dataDomList.add(dataDom); }
            } // Loop: through Summary-Statistics data

            rowDomList.add(tr(dataDomList.toArray(new DomContent[]{})));
        } // Loop: through all Summary-Statistics Elements

        /* Creating the table's header */
        List<String> headerList = Arrays.asList("Computation Point", "RMSE Stdev", "Nash Sutcliffe", "Percent Bias", "R2");
        rowDomList.add(0, HtmlModifier.printTableHeadRow(headerList, tdAttribute, tdAttribute));
        rowDomList.add(0, caption("Summary Statistics Table"));

        return table(attrs(tdAttribute), rowDomList.toArray(new DomContent[]{}));
    } // printSummaryStatisticsTable()

    private Map<String, String> classifyStatisticsColor(Element element) {
        Map<String, String> colorMap = new HashMap<>();
        Map<String, String> statisticsMap = element.getElementResults().getStatisticResultsMap();
        Map<StatisticsType, List<DisplayRange>> statisticsDisplayRangeMap;

        if(this.displayRangeMap == null || this.displayRangeMap.isEmpty()) { statisticsDisplayRangeMap = getDefaultDisplayMap(); }
        else { statisticsDisplayRangeMap = this.displayRangeMap; }

        String nashColor = getColorCode(Double.parseDouble(statisticsMap.get(nashSutcliffe)), statisticsDisplayRangeMap.get(StatisticsType.NASH_SUTCLIFFE_EFFICIENCY));
        String rmseColor = getColorCode(Double.parseDouble(statisticsMap.get(rmseStdev)), statisticsDisplayRangeMap.get(StatisticsType.RMSE_STDEV));
        String biasColor = getColorCode(Double.parseDouble(statisticsMap.get(percentBias)), statisticsDisplayRangeMap.get(StatisticsType.PERCENT_BIAS));

        colorMap.put(nashSutcliffe, nashColor);
        colorMap.put(rmseStdev, rmseColor);
        colorMap.put(percentBias, biasColor);

        return colorMap;
    } // classifyStatisticsColor()

    private static Map<StatisticsType, List<DisplayRange>> getDefaultDisplayMap() {
        Map<StatisticsType, List<DisplayRange>> defaultDisplayMap = new HashMap<>();

        defaultDisplayMap.put(StatisticsType.NASH_SUTCLIFFE_EFFICIENCY, getDefaultDisplayRanges(StatisticsType.NASH_SUTCLIFFE_EFFICIENCY));
        defaultDisplayMap.put(StatisticsType.RMSE_STDEV, getDefaultDisplayRanges(StatisticsType.RMSE_STDEV));
        defaultDisplayMap.put(StatisticsType.PERCENT_BIAS, getDefaultDisplayRanges(StatisticsType.PERCENT_BIAS));

        return defaultDisplayMap;
    } // getDefaultDisplayMap()

    private static List<DisplayRange> getDefaultDisplayRanges(StatisticsType statisticsType) {
        DisplayRange ratingVeryGood = null, ratingGood = null, ratingSatisfactory = null, ratingUnsatisfactory = null;

        if(statisticsType == StatisticsType.NASH_SUTCLIFFE_EFFICIENCY) {
            ratingVeryGood = new DisplayRange(0.75,1, "#7dca5c");
            ratingGood = new DisplayRange(0.65, 0.75, "#FFD700");
            ratingSatisfactory = new DisplayRange(0.50, 0.75, "#fab144");
            ratingUnsatisfactory = new DisplayRange(Double.NEGATIVE_INFINITY, 0.50, "#f24430");
        } // If: Nash Sutcliffe Efficiency
        else if(statisticsType == StatisticsType.RMSE_STDEV) {
            ratingVeryGood = new DisplayRange(Double.NEGATIVE_INFINITY,0.50, "#7dca5c");
            ratingGood = new DisplayRange(0.50, 0.60, "#FFD700");
            ratingSatisfactory = new DisplayRange(0.60, 0.70, "#fab144");
            ratingUnsatisfactory = new DisplayRange(0.70, Double.POSITIVE_INFINITY, "#f24430");
        } // Else if: RMSE Standard Deviation
        else if(statisticsType == StatisticsType.PERCENT_BIAS) {
            ratingVeryGood = new DisplayRange(-10,10, "#7dca5c");
            ratingGood = new DisplayRange(-15, 15, "#FFD700");
            ratingSatisfactory = new DisplayRange(-25, 25, "#fab144");
            ratingUnsatisfactory = new DisplayRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, "#f24430");
        } // Else If: Percent Bias

        List<DisplayRange> defaultDisplayRanges = Arrays.asList(ratingVeryGood, ratingGood, ratingSatisfactory, ratingUnsatisfactory);

        return defaultDisplayRanges;
    } // getDefaultDisplayRanges()

    private static String getColorCode(double value, List<DisplayRange> displayRanges) {
        /* Find the first displayRange that the value matched with. If none matched, return null. */
        Optional<DisplayRange> matchedDisplayRange = displayRanges.stream().filter(e->e.inRange(value)).findFirst();
        return matchedDisplayRange.map(DisplayRange::getColorCode).orElse(null);
    } // getColorCode()

} // StandardReportWriter class

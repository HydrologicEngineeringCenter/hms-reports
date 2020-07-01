package mil.army.usace.hec.hms.reports.io.statistics;

import hec.heclib.util.Heclib;
import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.DisplayRange;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.StatisticsType;
import mil.army.usace.hec.hms.reports.io.BasinParser;
import mil.army.usace.hec.hms.reports.io.ReportWriter;
import mil.army.usace.hec.hms.reports.util.HtmlModifier;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class SummaryStatisticsReportWriter extends ReportWriter {
    private String rmseStdev = "Observed Flow RMSE Stdev";
    private String nashSutcliffe = "Observed Flow Nash Sutcliffe";
    private String percentBias = "Observed Flow Percent Bias";
    private String r2Coefficient = "Coefficient of Determination";

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
                .simulationType(this.simulationType)
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
            Map<String, String> statisticsMap = summaryStatisticsMap(element);
            Map<String, String> colorMap = classifyStatisticsColor(element);
            List<DomContent> dataDomList = new ArrayList<>();
            dataDomList.add(td(element.getName()));

            for(String statisticsType : statisticsMap.keySet()) {
                String statisticsData = statisticsMap.get(statisticsType);
                String reformatData = StringBeautifier.beautifyString(statisticsData);
                DomContent dataButton = button(reformatData).withStyle("background-color:" + colorMap.get(statisticsType));
                DomContent dataDom = td(dataButton);
                dataDomList.add(dataDom);
            } // Loop: through Summary-Statistics data

            rowDomList.add(tr(dataDomList.toArray(new DomContent[]{})));
        } // Loop: through all Summary-Statistics Elements

        /* Creating the table's header */
        List<String> headerList = Arrays.asList("Computation Point", "RMSE Stdev", "Nash Sutcliffe", "Percent Bias", "R2");
        rowDomList.add(0, HtmlModifier.printTableHeadRow(headerList, tdAttribute, tdAttribute));
        rowDomList.add(0, caption("Summary Statistics Table"));

        return table(attrs(tdAttribute), rowDomList.toArray(new DomContent[]{}));
    } // printSummaryStatisticsTable()

    private Map<String, String> summaryStatisticsMap(Element statisticsElement) {
        Map<String, String> summaryMap = new LinkedHashMap<>();
        Map<String, String> statisticsMap = statisticsElement.getElementResults().getStatisticResultsMap();

        summaryMap.put(rmseStdev, statisticsMap.get(rmseStdev));
        summaryMap.put(nashSutcliffe, statisticsMap.get(nashSutcliffe));
        summaryMap.put(percentBias, statisticsMap.get(percentBias));
        summaryMap.put(r2Coefficient, calculateCoefficientOfDetermination(statisticsElement));

        return summaryMap;
    } // summaryStatisticsMap()

    private String calculateCoefficientOfDetermination(Element statisticsElement) {
        Map<String, double[]> timeSeriesMap = statisticsElement.getElementResults().getTimeSeriesResultsMap();
        double[] simulatedFlow = timeSeriesMap.get("Outflow");
        double[] observedFlow  = timeSeriesMap.get("Observed Flow");

        /* Compute maximum absolute residual */
        SummaryStatistics simStats = new SummaryStatistics();
        SummaryStatistics obsStats = new SummaryStatistics();
        for(int i = 0; i < Math.min(simulatedFlow.length, observedFlow.length); i++) {
            if(observedFlow[i] != Heclib.UNDEFINED_DOUBLE) {
                obsStats.addValue(observedFlow[i]);
                simStats.addValue(simulatedFlow[i]);
            } // If: Not Undefined
        } // Loop: through each value in simulatedFlow

        /* Return 'Undefined' if not valid */
        if (obsStats.getN() < 1) {
            return "Undefined";
        } // If: obsStats.get() < 1

        double meanObserved = obsStats.getMean();
        double meanSimulated = simStats.getMean();
        SummaryStatistics crossResidual = new SummaryStatistics();
        SummaryStatistics simResidual = new SummaryStatistics();
        SummaryStatistics obsResidual = new SummaryStatistics();
        for(int i = 0; i < Math.min(simulatedFlow.length, observedFlow.length); i++) {
            if(observedFlow[i] != Heclib.UNDEFINED_DOUBLE) {
                crossResidual.addValue((observedFlow[i] - meanObserved) * (simulatedFlow[i] - meanSimulated));
                simResidual.addValue((simulatedFlow[i] - meanSimulated) * (simulatedFlow[i] - meanSimulated));
                obsResidual.addValue((observedFlow[i] - meanObserved) * (observedFlow[i] - meanObserved));
            } // If: Not Undefined
        } // Loop: through each value in simulatedFlow

        double r = crossResidual.getSum() / (Math.sqrt(simResidual.getSum()) * Math.sqrt(obsResidual.getSum()));
        double r2Coefficient = r * r;

        return String.valueOf(r2Coefficient);
    } // calculateCoefficientOfDetermination()

    private Map<String, String> classifyStatisticsColor(Element element) {
        Map<String, String> colorMap = new LinkedHashMap<>();
        Map<String, String> statisticsMap = summaryStatisticsMap(element);
        Map<StatisticsType, List<DisplayRange>> statisticsDisplayRangeMap;

        if(this.displayRangeMap == null || this.displayRangeMap.isEmpty()) { statisticsDisplayRangeMap = getDefaultDisplayMap(); }
        else { statisticsDisplayRangeMap = this.displayRangeMap; }

        String nashColor = getMatchedBinColor(Double.parseDouble(statisticsMap.get(nashSutcliffe)), statisticsDisplayRangeMap.get(StatisticsType.NASH_SUTCLIFFE_EFFICIENCY));
        String rmseColor = getMatchedBinColor(Double.parseDouble(statisticsMap.get(rmseStdev)), statisticsDisplayRangeMap.get(StatisticsType.RMSE_STDEV));
        String biasColor = getMatchedBinColor(Double.parseDouble(statisticsMap.get(percentBias)), statisticsDisplayRangeMap.get(StatisticsType.PERCENT_BIAS));
        String r2Color   = getMatchedBinColor(Double.parseDouble(statisticsMap.get(r2Coefficient)), statisticsDisplayRangeMap.get(StatisticsType.COEFFICIENT_OF_DETERMINATION));

        colorMap.put(nashSutcliffe, nashColor);
        colorMap.put(rmseStdev, rmseColor);
        colorMap.put(percentBias, biasColor);
        colorMap.put(r2Coefficient, r2Color);

        return colorMap;
    } // classifyStatisticsColor()

    private static Map<StatisticsType, List<DisplayRange>> getDefaultDisplayMap() {
        Map<StatisticsType, List<DisplayRange>> defaultDisplayMap = new LinkedHashMap<>();

        defaultDisplayMap.put(StatisticsType.NASH_SUTCLIFFE_EFFICIENCY, getDefaultDisplayRanges(StatisticsType.NASH_SUTCLIFFE_EFFICIENCY));
        defaultDisplayMap.put(StatisticsType.RMSE_STDEV, getDefaultDisplayRanges(StatisticsType.RMSE_STDEV));
        defaultDisplayMap.put(StatisticsType.PERCENT_BIAS, getDefaultDisplayRanges(StatisticsType.PERCENT_BIAS));
        defaultDisplayMap.put(StatisticsType.COEFFICIENT_OF_DETERMINATION, getDefaultDisplayRanges(StatisticsType.COEFFICIENT_OF_DETERMINATION));

        return defaultDisplayMap;
    } // getDefaultDisplayMap()

    private static List<DisplayRange> getDefaultDisplayRanges(StatisticsType statisticsType) {
        DisplayRange ratingVeryGood = null, ratingGood = null, ratingSatisfactory = null, ratingUnsatisfactory = null;
        String darkGreen = "#3C8A3B";
        String lightGreen = "#7DCA5C";
        String yellow = "#FFD700";
        String red = "#F24430";

        if(statisticsType == StatisticsType.NASH_SUTCLIFFE_EFFICIENCY) {
            ratingVeryGood = new DisplayRange(0.75,1, darkGreen);
            ratingGood = new DisplayRange(0.65, 0.75, lightGreen);
            ratingSatisfactory = new DisplayRange(0.50, 0.75, yellow);
            ratingUnsatisfactory = new DisplayRange(Double.NEGATIVE_INFINITY, 0.50, red);
        } // If: Nash Sutcliffe Efficiency
        else if(statisticsType == StatisticsType.RMSE_STDEV) {
            ratingVeryGood = new DisplayRange(Double.NEGATIVE_INFINITY,0.50, darkGreen);
            ratingGood = new DisplayRange(0.50, 0.60, lightGreen);
            ratingSatisfactory = new DisplayRange(0.60, 0.70, yellow);
            ratingUnsatisfactory = new DisplayRange(0.70, Double.POSITIVE_INFINITY, red);
        } // Else if: RMSE Standard Deviation
        else if(statisticsType == StatisticsType.PERCENT_BIAS) {
            ratingVeryGood = new DisplayRange(-10,10, darkGreen);
            ratingGood = new DisplayRange(-15, 15, lightGreen);
            ratingSatisfactory = new DisplayRange(-25, 25, yellow);
            ratingUnsatisfactory = new DisplayRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, red);
        } // Else If: Percent Bias
        else if(statisticsType == StatisticsType.COEFFICIENT_OF_DETERMINATION) {
            ratingVeryGood = new DisplayRange(0.75,1, darkGreen);
            ratingGood = new DisplayRange(0.65, 0.75, lightGreen);
            ratingSatisfactory = new DisplayRange(0.50, 0.65, yellow);
            ratingUnsatisfactory = new DisplayRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, red);
        } // Else If: Coefficient of Determination

        List<DisplayRange> defaultDisplayRanges = Arrays.asList(ratingVeryGood, ratingGood, ratingSatisfactory, ratingUnsatisfactory);

        return defaultDisplayRanges;
    } // getDefaultDisplayRanges()

    private static String getMatchedBinColor(double value, List<DisplayRange> displayRanges) {
        /* Find the first displayRange that the value matched with. If none matched, return null. */
        Optional<DisplayRange> matchedDisplayRange = displayRanges.stream().filter(e->e.inRange(value)).findFirst();
        return matchedDisplayRange.map(DisplayRange::getColorCode).orElse(null);
    } // getMatchedBinColor()

} // StandardReportWriter class

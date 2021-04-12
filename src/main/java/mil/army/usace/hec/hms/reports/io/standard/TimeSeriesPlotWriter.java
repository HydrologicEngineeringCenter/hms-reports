package mil.army.usace.hec.hms.reports.io.standard;

import hec.heclib.util.Heclib;
import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.TimeSeriesResult;
import mil.army.usace.hec.hms.reports.util.PlotUtil;
import mil.army.usace.hec.hms.reports.util.HtmlUtil;
import mil.army.usace.hec.hms.reports.util.StringUtil;
import mil.army.usace.hec.hms.reports.util.ValidCheck;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Page;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class TimeSeriesPlotWriter {
    private final ElementResults elementResults;
    private final Map<String, TimeSeriesResult> singleDatasetMap;
    private final Map<String, List<TimeSeriesResult>> multipleDatasetsMap;

    public TimeSeriesPlotWriter(ElementResults elementResults, List<String> chosenPlots) {
        this.elementResults = elementResults;

        /* Initialize singleDatasetMap and multipleDatasetsMap */
        multipleDatasetsMap = new LinkedHashMap<>();
        singleDatasetMap = elementResults.getTimeSeriesResults().stream()
                .filter(individual -> ValidCheck.validTimeSeriesPlot(individual.getType(), chosenPlots))
                .collect(Collectors.toMap(TimeSeriesResult::getType, TimeSeriesResult::getTimeSeriesResult,
                        (value1, value2) -> {
                            multipleDatasetsMap.computeIfAbsent(value1.getType(), k -> new ArrayList<>());
                            multipleDatasetsMap.get(value1.getType()).add(value1);
                            multipleDatasetsMap.get(value1.getType()).add(value2);
                            return value2;
                        }));

        /* Remove Duplicate Types in singleDatasetMap */
        multipleDatasetsMap.keySet().forEach(singleDatasetMap::remove);

        /* Remove Duplicate Unit Types in multipleDatasetsMap */
        multipleDatasetsMap.keySet().forEach(e -> multipleDatasetsMap.replace(e, multipleDatasetsMap.get(e).stream().distinct().collect(Collectors.toList())));
    }

    public DomContent getTimeSeriesPlots() {
        List<DomContent> timeSeriesPlotDomList = new ArrayList<>();

        /* Case: Combined Plot (Outflow and Precipitation, or others) */
        List<DomContent> combinedPlots = getCombinedPlots();
        if(!combinedPlots.isEmpty()) timeSeriesPlotDomList.addAll(combinedPlots);

        /* Case: Single Plots (One dataset for each plot) */
        List<DomContent> singleDatasetPlots = getSingleDatasetPlots();
        if(!singleDatasetPlots.isEmpty()) timeSeriesPlotDomList.addAll(singleDatasetPlots);

        /* Case: Single Plots (Multiple Datasets for each plot) */
        List<DomContent> multipleDatasetsPlots = getMultipleDatasetsPlots();
        if(!multipleDatasetsPlots.isEmpty()) timeSeriesPlotDomList.addAll(multipleDatasetsPlots);

        /* Maximum two plots per page */
        int maxPlotSize = 2;
        timeSeriesPlotDomList = limitPlotsPerPage(timeSeriesPlotDomList, maxPlotSize);

        return div(attrs(".group-plot"), timeSeriesPlotDomList.toArray(new DomContent[]{}));
    }

    private List<DomContent> limitPlotsPerPage(List<DomContent> plotList, int limit) {
        if(plotList.isEmpty() || limit <= 0) return plotList;
        List<DomContent> modifiedPlotList = new ArrayList<>();

        final AtomicInteger counter = new AtomicInteger(0);
        Collection<List<DomContent>> partitionedList = plotList.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / limit))
                .values();

        partitionedList.forEach(list -> {
            if(list.size() == limit)
                modifiedPlotList.add(div(attrs(".max-plot"), list.toArray(new DomContent[]{})));
            else
                modifiedPlotList.add(div(attrs(".non-max-plot"), list.toArray(new DomContent[]{})));
        });

        return modifiedPlotList;
    }

    private List<DomContent> getCombinedPlots() {
        List<DomContent> combinedPlots = new ArrayList<>();

        Map<String, Map<String, TimeSeriesResult>> combinedPlotMap = getCombinedPlotName(singleDatasetMap);

        if(!combinedPlotMap.isEmpty()) {
            for(String combinedPlotName : combinedPlotMap.keySet()) {
                Map<String, TimeSeriesResult> combinedTsrMap = combinedPlotMap.get(combinedPlotName);
                DomContent tsrDom = printTimeSeriesCombinedPlot(combinedTsrMap, combinedPlotName, elementResults.getName());
                if(tsrDom != null) { combinedPlots.add(tsrDom); }
                /* Remove the used plots from the singleDatasetMap */
                combinedTsrMap.keySet().forEach(singleDatasetMap::remove);
            }
        }

        return combinedPlots;
    }

    private List<DomContent> getSingleDatasetPlots() {
        List<DomContent> singleDatasetPlots = new ArrayList<>();

        for(String key : singleDatasetMap.keySet()) {
            List<TimeSeriesResult> timeSeriesResults = Collections.singletonList(singleDatasetMap.get(key));
            DomContent tsrDom = printTimeSeriesPlot(timeSeriesResults, elementResults.getName());
            if(tsrDom != null) { singleDatasetPlots.add(tsrDom); }
        }

        return singleDatasetPlots;
    }

    private List<DomContent> getMultipleDatasetsPlots() {
        List<DomContent> multipleDatasetsPlots = new ArrayList<>();

        for(String key : multipleDatasetsMap.keySet()) {
            DomContent tsrDom = printTimeSeriesPlot(multipleDatasetsMap.get(key), elementResults.getName());
            if(tsrDom != null) { multipleDatasetsPlots.add(tsrDom); }
        }

        return multipleDatasetsPlots;
    }

    /* Helper Functions */
    private Map<String, Map<String, TimeSeriesResult>> getCombinedPlotName(Map<String, TimeSeriesResult> tsrMap) {
        Map<String, Map<String, TimeSeriesResult>> combinedPlotMap = new HashMap<>();

        if(tsrMap.containsKey("Precipitation") && tsrMap.containsKey("Outflow") && tsrMap.containsKey("Excess Precipitation")) {
            Map<String, TimeSeriesResult> combinedMap = new HashMap<>();
            combinedMap.put("Precipitation", tsrMap.get("Precipitation"));
            combinedMap.put("Outflow", tsrMap.get("Outflow"));
            combinedMap.put("Excess Precipitation", tsrMap.get("Excess Precipitation"));
            String plotName = "Precipitation and Outflow";
            combinedPlotMap.put(plotName, combinedMap);
        } // If: CombinedPlot is a 'Precipitation and Outflow'

        if(tsrMap.containsKey("Outflow") && tsrMap.containsKey("Observed Flow")) {
            Map<String, TimeSeriesResult> combinedMap = new HashMap<>();
            combinedMap.put("Observed Flow", tsrMap.get("Observed Flow"));
            combinedMap.put("Outflow", tsrMap.get("Outflow"));
            String plotName = "Outflow and Observed Flow";
            combinedPlotMap.put(plotName, combinedMap);
        } // If: CombinedPlot is a 'Outflow and Observed Flow plot'

        return combinedPlotMap;
    } // getCombinedPlotName()

    private DomContent printTimeSeriesCombinedPlot(Map<String, TimeSeriesResult> tsrMap, String plotName, String elementName) {
        DomContent combinedPlotDom = null;

        // Get the DomContent of the Plot
        if(plotName.equals("Precipitation and Outflow"))
            combinedPlotDom = getPrecipOutflowPlot(tsrMap, plotName, elementName);
        else if(plotName.equals("Outflow and Observed Flow"))
            combinedPlotDom = getOutflowObservedFlowPlot(tsrMap, plotName, elementName);

        if(combinedPlotDom == null) { return null; }

        return div(attrs(".single-plot"), combinedPlotDom);
    } // printTimeSeriesCombinedPlot()

    private DomContent getPrecipOutflowPlot(Map<String, TimeSeriesResult> tsrMap, String plotName, String elementName) {
        List<TimeSeriesResult> topPlots = Arrays.asList(tsrMap.get("Precipitation"), tsrMap.get("Excess Precipitation"));
        List<TimeSeriesResult> bottomPlots = Collections.singletonList(tsrMap.get("Outflow"));

        // Creating Tables for both Top Plots and Bottom Plots
        List<Table> topPlotTables = new ArrayList<>(), bottomPlotTables = new ArrayList<>();
        for(TimeSeriesResult tsr : topPlots) {
            List<TimeSeriesResult> tsrList = Collections.singletonList(tsr);
            Table plotTable = getTimeSeriesTable(tsrList, tsr.getType());
            if(plotTable != null)
                topPlotTables.add(plotTable);
        } // Loop: to create Tables for Top Plots

        for(TimeSeriesResult tsr : bottomPlots) {
            List<TimeSeriesResult> tsrList = Collections.singletonList(tsr);
            Table plotTable = getTimeSeriesTable(tsrList, tsr.getType());
            if(plotTable != null)
                bottomPlotTables.add(plotTable);
        } // Loop: to create Tables for Bottom Plots

        // Setting Plot's configurations
        String xAxisTitle = "Time";
        String y1AxisTitle = bottomPlots.get(0).getUnitType() + " (" + bottomPlots.get(0).getUnit() + ")";
        String y2AxisTitle = topPlots.get(0).getUnitType() + " (" +  topPlots.get(0).getUnit() + ")";
        String divName = StringUtil.getPlotDivName(elementName, plotName);

        // Create Plot
        Figure timeSeriesFigure = PlotUtil.createPrecipOutflowPlot(plotName, topPlotTables, bottomPlotTables, xAxisTitle, y1AxisTitle, y2AxisTitle);
        Page page = Page.pageBuilder(timeSeriesFigure, divName).build();

        // Extract Plot's Javascript
        String plotHtml = page.asJavascript();

        return HtmlUtil.extractPlotlyJavascript(plotHtml);
    } // getPrecipOutflowPlot()

    private DomContent getOutflowObservedFlowPlot(Map<String, TimeSeriesResult> tsrMap, String plotName, String elementName) {
        TimeSeriesResult outflowPlot = tsrMap.get("Outflow"), observedFlowPlot = tsrMap.get("Observed Flow");
        Table outflowTable = getTimeSeriesTable(Collections.singletonList(outflowPlot), "Outflow");
        Table observedFlowTable = getTimeSeriesTable(Collections.singletonList(observedFlowPlot), "Observed Flow");
        List<Table> plotList = Arrays.asList(outflowTable, observedFlowTable);

        if(outflowTable == null || observedFlowTable == null) { return null; }

        // Setting Plot's configurations
        String xAxisTitle = "Time";
        String yAxisTitle = outflowPlot.getUnitType();
        String divName = StringUtil.getPlotDivName(elementName, plotName);

        // Create Plot
        Figure timeSeriesFigure = PlotUtil.createOutflowObservedFlowPlot(plotName, plotList, xAxisTitle, yAxisTitle);
        Page page = Page.pageBuilder(timeSeriesFigure, divName).build();

        // Extract Plot's Javascript
        String plotHtml = page.asJavascript();

        return HtmlUtil.extractPlotlyJavascript(plotHtml);
    } // getOutflowObservedFlowPlot()

    private Table getTimeSeriesTable(List<TimeSeriesResult> timeSeriesResultList, String tableName) {
        List<Column<?>> columnList = new ArrayList<>();

        /* Get TimeSeries Column */
        List<ZonedDateTime> zonedDateTimeList = timeSeriesResultList.get(0).getTimes();
        List<LocalDateTime> localDateTimeList = zonedDateTimeList.stream().map(ZonedDateTime::toLocalDateTime).collect(Collectors.toList());

        DateTimeColumn dateTimeColumn = DateTimeColumn.create("Time", localDateTimeList);
        columnList.add(dateTimeColumn);

        /* Get Value Array Columns */
        for(TimeSeriesResult timeSeriesResult : timeSeriesResultList) {
            double[] valueArray = timeSeriesResult.getValues();
            for(int i = 0; i < valueArray.length; i++) {
                if(valueArray[i] == Heclib.UNDEFINED_DOUBLE)
                    valueArray[i] = Double.NaN;
            } // Converting HEC Missing Data to NaN
            if(valueArray.length == 0) { return null; } // TimeSeries without values

            String columnName = timeSeriesResult.getUnitType();
            DoubleColumn valueColumn = DoubleColumn.create(columnName, valueArray);
            if(!columnList.contains(valueColumn)) { columnList.add(valueColumn); }
        } // Loop: through all timeSeriesResult

        /* Create a TimeSeriesTable with Columns from columnList */
        return Table.create(tableName, columnList);
    } // timeSeriesToCsv

    private DomContent printTimeSeriesPlot(List<TimeSeriesResult> timeSeriesResultList, String elementName) {
        // Configure Plot Settings
        TimeSeriesResult baseResult = timeSeriesResultList.get(0);
        String plotName = baseResult.getType();
        Table plotTable = getTimeSeriesTable(timeSeriesResultList, plotName);
        if(plotTable == null) { return null; } // No PlotTable

        String xAxisTit = "Time";
        String yAxisTit = baseResult.getUnitType() + " (" + baseResult.getUnit() + ")";

        // Create Plot
        Figure timeSeriesFigure = PlotUtil.createTimeSeriesPlot(plotName, plotTable, xAxisTit, yAxisTit);
        String plotDivName = StringUtil.getPlotDivName(elementName, plotName);
        Page page = Page.pageBuilder(timeSeriesFigure, plotDivName).build();

        // Extract Plot's Javascript
        String plotHtml = page.asJavascript();
        DomContent domContent = HtmlUtil.extractPlotlyJavascript(plotHtml);

        return div(attrs(".single-plot"), domContent);
    } // printTimeSeriesPlot()
}

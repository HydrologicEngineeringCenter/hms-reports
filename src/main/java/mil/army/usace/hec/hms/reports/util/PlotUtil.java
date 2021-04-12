package mil.army.usace.hec.hms.reports.util;

import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.plotly.components.*;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlotUtil {
    private PlotUtil() {}

    public static Figure createTimeSeriesPlot(String plotTitle, Table plotData, String xAxisTitle, String yAxisTitle) {
        /* Create Layout for Plot */
        Layout plotLayout = Layout.builder()
                .title(plotTitle)
                .height(500)
                .width(800)
                .xAxis(Axis.builder().title(xAxisTitle).build())
                .yAxis(Axis.builder().title(yAxisTitle).build())
                .build();

        /* Filter Columns */
        Column<?> dateTimeColumn = Arrays.stream(plotData.columnArray()).filter(e->e instanceof DateTimeColumn).findFirst().orElse(null);
        List<Column<?>> doubleColumnList = Arrays.stream(plotData.columnArray()).filter(e->e instanceof DoubleColumn).collect(Collectors.toList());
        if(dateTimeColumn == null) throw new IllegalArgumentException("Time Column Not Found");
        if(doubleColumnList.isEmpty()) throw new IllegalArgumentException("Value Column(s) Not Found");

        /* Create a List of Traces */
        List<Trace> traceList = new ArrayList<>();

        for(Column valueColumn : doubleColumnList) {
            ScatterTrace plotTrace = ScatterTrace.builder(dateTimeColumn, valueColumn)
                    .mode(ScatterTrace.Mode.LINE)
                    .name(valueColumn.name())
                    .build();
            traceList.add(plotTrace);
        } // Loop: through all valueColumnList

        return new Figure(plotLayout, traceList.toArray(new Trace[]{}));
    } // createTimeSeriesPlot() -- Single

    public static Figure createPrecipOutflowPlot(String plotTitle, List<Table> topPlots, List<Table> bottomPlots, String xAxisTitle, String y1AxisTitle, String y2AxisTitle) {

        List<Trace> traceList = new ArrayList<>();

        Grid grid = Grid.builder()
                .rows(2) // Two Subplots: Top and Bottom
                .columns(1)
                .pattern(Grid.Pattern.INDEPENDENT)
                .rowOrder(Grid.RowOrder.BOTTOM_TO_TOP)
                .build();

        Layout plotLayout = Layout.builder()
                .title(plotTitle)
                .height(500)
                .width(850)
                .xAxis(Axis.builder().title(xAxisTitle).visible(false).build())
                .yAxis(Axis.builder().title(y1AxisTitle).build())
                .yAxis2(Axis.builder().title(y2AxisTitle).autoRange(Axis.AutoRange.REVERSED).build())
                .grid(grid)
                .build();

        // Color order: Precipitation, Excess Precipitation, Outflow
        List<String> colorWay = Arrays.asList("#5CB3FF", "#ff2800", "#1f77b4");
        int count = 0;

        for(Table plot : topPlots) {
            List<String> plotColumns = plot.columnNames();
            ScatterTrace plotTrace = ScatterTrace.builder(plot.column(plotColumns.get(0)), plot.column(plotColumns.get(1)))
                    .mode(ScatterTrace.Mode.LINE)
                    .name(plot.name())
                    .marker(Marker.builder().color(colorWay.get(count)).build())
                    .yAxis("y2")
                    .xAxis("x2")
                    .build();
            count++;
            traceList.add(plotTrace);
        } // Loop: to get Traces for top plots

        for(Table plot : bottomPlots) {
            List<String> plotColumns = plot.columnNames();
            ScatterTrace plotTrace = ScatterTrace.builder(plot.column(plotColumns.get(0)), plot.column(plotColumns.get(1)))
                    .mode(ScatterTrace.Mode.LINE)
                    .name(plot.name())
                    .marker(Marker.builder().color(colorWay.get(count)).build())
                    .yAxis("y1")
                    .xAxis("x1")
                    .build();
            count++;
            traceList.add(plotTrace);
        } // Loop: to get Traces for bottom plots

        return new Figure(plotLayout, traceList.toArray(new Trace[]{}));
    } // createCombinedTimeSeriesPlot()

    public static Figure createOutflowObservedFlowPlot(String plotTitle, List<Table> plotList, String xAxisTitle, String yAxisTitle) {
        List<Trace> traceList = new ArrayList<>();
        Layout plotLayout = Layout.builder()
                .title(plotTitle)
                .height(500)
                .width(850)
                .xAxis(Axis.builder().title(xAxisTitle).build())
                .yAxis(Axis.builder().title(yAxisTitle).build())
                .build();

        // Color order: Outflow, Observed Flow
        List<String> colorWay = Arrays.asList("#0492c2", "#000000");
        int count = 0;

        for(Table plot : plotList) {
            List<String> plotColumns = plot.columnNames();
            ScatterTrace plotTrace = ScatterTrace.builder(plot.column(plotColumns.get(0)), plot.column(plotColumns.get(1)))
                    .mode(ScatterTrace.Mode.LINE)
                    .name(plot.name())
                    .marker(Marker.builder().color(colorWay.get(count)).build())
                    .yAxis("y1")
                    .xAxis("x1")
                    .build();
            count++;
            traceList.add(plotTrace);
        } // Get traces for Outflow plot and Observed Flow plot

        return new Figure(plotLayout, traceList.toArray(new Trace[]{}));
    } // createOutflowObservedFlowPlot()
} // FigureCreator class

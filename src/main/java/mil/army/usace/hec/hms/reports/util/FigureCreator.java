package mil.army.usace.hec.hms.reports.util;

import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.*;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FigureCreator {
    private FigureCreator() {}

    public static Figure createTimeSeriesPlot(String plotTitle, Table plotData, String xAxisTitle, String yAxisTitle) {
        List<String> columnNames = plotData.columnNames(); // (x | y) or (x | y1 | y2 | ... | y__)

        Layout plotLayout = Layout.builder()
                .title(plotTitle)
                .height(500)
                .width(800)
                .xAxis(Axis.builder().title(xAxisTitle).build())
                .yAxis(Axis.builder().title(yAxisTitle).build())
                .build();

        ScatterTrace plotTrace = ScatterTrace.builder(plotData.column(columnNames.get(0)), plotData.column(columnNames.get(1)))
                .mode(ScatterTrace.Mode.LINE)
                .build();

        return new Figure(plotLayout, plotTrace);
    } // createTimeSeriesPlot() -- Single

    public static Figure createPairTimeSeriesPlot(String plotTitle, Table topPlot, Table bottomPlot,
                                                  String xAxisTitle, String y1AxisTitle, String y2AxisTitle) {

        List<String> topColumnNames = topPlot.columnNames();
        List<String> bottomColumnNames = bottomPlot.columnNames();
        List<Trace> traceList = new ArrayList<>();

        Grid grid = Grid.builder()
                .rows(2)
                .columns(1)
                .pattern(Grid.Pattern.INDEPENDENT)
                .rowOrder(Grid.RowOrder.BOTTOM_TO_TOP)
                .build();

        Layout plotLayout = Layout.builder()
                .title(plotTitle)
                .height(500)
                .width(850)
                .xAxis(Axis.builder().title(xAxisTitle).build())
                .yAxis(Axis.builder().title(y1AxisTitle).build())
                .yAxis2(Axis.builder().title(y2AxisTitle).autoRange(Axis.AutoRange.REVERSED).build())
                .grid(grid)
                .build();

        ScatterTrace topPlotTrace = ScatterTrace.builder(topPlot.column(topColumnNames.get(0)), topPlot.column(topColumnNames.get(1)))
                .mode(ScatterTrace.Mode.LINE)
                .name(topPlot.name())
                .yAxis("y1")
                .xAxis("x1")
                .build();
        traceList.add(topPlotTrace);

        ScatterTrace bottomPlotTrace = ScatterTrace.builder(bottomPlot.column(bottomColumnNames.get(0)), bottomPlot.column(bottomColumnNames.get(1)))
                .mode(ScatterTrace.Mode.LINE)
                .name(bottomPlot.name())
                .marker(Marker.builder().color("#5CB3FF").build())
                .yAxis("y2")
                .xAxis("x2")
                .build();
        traceList.add(bottomPlotTrace);

        return new Figure(plotLayout, traceList.toArray(new Trace[]{}));
    } // createPairTimeSeriesPlot()

    public static Figure createCombinedTimeSeriesPlot(String plotTitle, List<Table> topPlots, List<Table> bottomPlots, String xAxisTitle, String y1AxisTitle, String y2AxisTitle) {

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
                .xAxis(Axis.builder().title(xAxisTitle).build())
                .yAxis(Axis.builder().title(y1AxisTitle).build())
                .yAxis2(Axis.builder().title(y2AxisTitle).autoRange(Axis.AutoRange.REVERSED).build())
                .grid(grid)
                .build();

        // Precipitation, Excess Precipitation, Outflow
        List<String> colorWay = Arrays.asList("#5CB3FF", "#0492c2", "#1f77b4");
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

} // FigureCreator class

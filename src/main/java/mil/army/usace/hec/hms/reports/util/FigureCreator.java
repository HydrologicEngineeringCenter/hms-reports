package mil.army.usace.hec.hms.reports.util;

import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Grid;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;

public class FigureCreator {
    private FigureCreator() {}

    public static Figure createTimeSeriesPlot(String plotTitle, Table plotData,
                                              String xColumnName, String yColumnName,
                                              String unitType, String unit) {

        String yAxisTitle = unitType + " (" + unit + ")";

        Grid grid = Grid.builder()
                .rows(1)
                .columns(1)
                .pattern(Grid.Pattern.INDEPENDENT)
                .rowOrder(Grid.RowOrder.BOTTOM_TO_TOP)
                .build();

        Layout plotLayout = Layout.builder()
                .title(plotTitle)
                .height(500)
                .width(800)
                .xAxis(Axis.builder().title(xColumnName).build())
                .yAxis(Axis.builder().title(yAxisTitle).build())
                .grid(grid)
                .build();

        ScatterTrace plotTrace = ScatterTrace.builder(plotData.column(xColumnName), plotData.column(yColumnName))
                .mode(ScatterTrace.Mode.LINE)
                .build();

        return new Figure(plotLayout, plotTrace);
    }


} // FigureCreator class

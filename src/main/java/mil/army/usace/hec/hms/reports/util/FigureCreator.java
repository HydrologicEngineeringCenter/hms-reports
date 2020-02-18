package mil.army.usace.hec.hms.reports.util;

import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;

public class FigureCreator {
    private FigureCreator() {}

    public static Figure createTimeSeriesPlot(String plotTitle, Table plotData, String xColumnName, String yColumnName) {

        Layout plotLayout = Layout.builder()
                .title(plotTitle)
                .height(400)
                .width(530)
                .xAxis(Axis.builder().title(xColumnName).build())
                .yAxis(Axis.builder().title(yColumnName).build())
                .build();

        ScatterTrace plotTrace = ScatterTrace.builder(plotData.column(xColumnName), plotData.column(yColumnName))
                .mode(ScatterTrace.Mode.LINE)
                .build();

        return new Figure(plotLayout, plotTrace);
    }


} // FigureCreator class

package mil.army.usace.hec.hms.reports.io;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import j2html.tags.DomContent;
import mil.army.usace.hec.hms.reports.*;

import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.util.FigureCreator;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;
import mil.army.usace.hec.hms.reports.util.TimeConverter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Page;

import javax.script.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;

import static j2html.TagCreator.*;

public class HmsReportWriter extends ReportWriter {
    HmsReportWriter(Builder builder) {
        super(builder);
    }

    @Override
    public void write() {
        /* HTML Layout */
        String htmlOutput = html(
                head(   title("Elements of Water and Fire"),
                        link().withRel("stylesheet").withHref("style.css"),
                        script().withSrc("https://cdn.plot.ly/plotly-latest.min.js")),
                body(printElementList(this.elements))
        ).renderFormatted();
        /* Writing to HTML output file */
        writeToFile(this.pathToDestination.toString(), htmlOutput);
    } // write()
    private DomContent printElementList(List<Element> elementList) {
        List<DomContent> elementDomList = new ArrayList<>();

        /* For each element, print: ElementInput and ElementResults */
        for(Element element : elementList) {
            // Getting ElementInput DomContent
            ElementInput elementInput = element.getElementInput();
            DomContent elementInputDom = printElementInput(elementInput);
            // Getting ElementResults DomContent
            ElementResults elementResults = element.getElementResults();
            DomContent elementResultsDom = printElementResults(elementResults);
            // Creating a 'div', 'class: element'
            List<DomContent> elementDom = Arrays.asList(elementInputDom, elementResultsDom);
            elementDomList.add(div(attrs(".element"), elementDom.toArray(new DomContent[]{})));
        } // Loop: through elementList to print each Element

        return main(elementDomList.toArray(new DomContent[]{}));
    } // printElementList()
    private DomContent printElementInput(ElementInput elementInput) {
        List<DomContent> elementInputDomList = new ArrayList<>();

        /* For each elementInput, print: Name, ElementType, and Processes */
        String elementName = elementInput.getName();
        String elementType = StringBeautifier.beautifyString(elementInput.getElementType());
        DomContent elementNameAndType = h2(elementType + ": " + elementName);
        elementInputDomList.add(elementNameAndType);

        List<Process> processList = elementInput.getProcesses();
        List<Process> processSingle = new ArrayList<>(); // For Processes without Parameters
        List<Process> processTable = new ArrayList<>();  // For Processes with Parameters to make a table

        for(Process process : processList) {
            if(process.getParameters().isEmpty())
                processSingle.add(process);
            else
                processTable.add(process);
        } // Loop: Separate processes between Single and Table

        DomContent processSingleDom = printSingleProcesses(processSingle);
        elementInputDomList.add(processSingleDom);
        DomContent processTableDom = printTableProcesses(processTable);
        elementInputDomList.add(processTableDom);

        return div(attrs(".element-input"), elementInputDomList.toArray(new DomContent[]{}));
    } // printElementInput()
    private DomContent printSingleProcesses(List<Process> singleProcesses) {
        List<DomContent> singleProcessesDomList = new ArrayList<>();

        for(Process process : singleProcesses) {
            if(unnecessarySingleProcesses().contains(process.getName())) {
                continue;
            } // Skipping unnecessary processes

            String processName = StringBeautifier.beautifyString(process.getName());
            String processValue = StringBeautifier.beautifyString(process.getValue());
            DomContent singleDom = join(b(processName), ":", processValue, br());
            singleProcessesDomList.add(singleDom);
        } // Loop: through each single process

        return p(attrs(".single-process"), singleProcessesDomList.toArray(new DomContent[]{})); // Return in the format of a 'paragraph'
    } // printSingleProcesses()
    private List<String> unnecessarySingleProcesses() {
        List<String> stringList = new ArrayList<>();
        stringList.add("name");
        stringList.add("elementType");
        return stringList;
    } // unnecessarySingleProcesses()
    private DomContent printTableProcesses(List<Process> tableProcesses) {
        List<DomContent> tableProcessesDomList = new ArrayList<>();

        for(Process process : tableProcesses) {
            String reformatName = StringBeautifier.beautifyString(process.getName());
            List<Parameter> parameterList = process.getParameters();
            DomContent tableDom  = printParameterTable(parameterList, reformatName); // The Table of Parameters
            tableProcessesDomList.add(tableDom);
        } // Loop: through each table process

        return div(attrs(".table-process"), tableProcessesDomList.toArray(new DomContent[]{})); // Return a list of tables (for processes)
    } // printTableProcesses()
    private DomContent printParameterTable(List<Parameter> parameterList, String processName) {
        List<DomContent> parameterDom = new ArrayList<>();
        List<Parameter> nestedParameterList = new ArrayList<>();

        if(!processName.equals("")) {
            parameterDom.add(caption(processName));
        } // If: has processName, add caption

        for(Parameter parameter : parameterList) {
            if(!parameter.getSubParameters().isEmpty()) {
                nestedParameterList.add(parameter);
            } // If: Parameter contains SubParameters
            else {
                List<String> tableRow = Arrays.asList(parameter.getName(), parameter.getValue());
                DomContent row = printTableDataRow(tableRow);
                parameterDom.add(row);
            } // Else: Parameter does not contain SubParameters
        } // Loop: through Parameter List

        /* Tables within a table */
        boolean nestedTable = false;
        for(Parameter nestedParameter : nestedParameterList) {
            /* Note: Nested Parameter's table should be in a row */
            List<DomContent> nestedParameterDom = new ArrayList<>();
            String reformatName = StringBeautifier.beautifyString(nestedParameter.getName());
            DomContent tableName = td(reformatName);
            List<Parameter> subParameters = nestedParameter.getSubParameters();
            DomContent subParameterTable = td(attrs(".nested-table"), printParameterTable(subParameters,""));
            nestedParameterDom.add(tableName);
            nestedParameterDom.add(subParameterTable);
            parameterDom.add(tr(nestedParameterDom.toArray(new DomContent[]{})));
            // Return table of class 'nested_process' if nested
            nestedTable = true;
        } // Loop: through nested parameter List

        if(nestedTable) {
            return table(attrs(".nested-parameter"), parameterDom.toArray(new DomContent[]{}));
        } // If: Nested Table

        return table(attrs(".table-parameter"), parameterDom.toArray(new DomContent[]{})); // Table of Parameters
    } // printParameterTable()
    private DomContent printTableHeadRow(List<String> headRow) {
        List<DomContent> domList = new ArrayList<>();

        for(String column : headRow) {
            String reformatString = StringBeautifier.beautifyString(column);
            DomContent headDom = th(reformatString);
            domList.add(headDom);
        } // Loop: through headRow list

        return tr(domList.toArray(new DomContent[]{}));
    } // printTableHeadRow()
    private DomContent printTableDataRow(List<String> dataRow) {
        List<DomContent> domList = new ArrayList<>();

        for(String data : dataRow) {
            String reformatString = StringBeautifier.beautifyString(data);
            DomContent dataDom = td(reformatString); // Table Data type
            domList.add(dataDom);
        } // Convert 'data' to Dom

        return tr(domList.toArray(new DomContent[]{})); // Table Row type
    } // printTableDataRow()
    private DomContent printElementResults(ElementResults elementResults) {
        List<DomContent> elementResultsDomList = new ArrayList<>();

        /* Get Statistic Results Dom */
        DomContent statisticResults = printStatisticResult(elementResults.getStatisticResults());
        elementResultsDomList.add(statisticResults);
        /* Get TimeSeries Results Dom */
        String elementName = elementResults.getName();
        DomContent timeSeriesResults = printTimeSeriesResult(elementResults.getTimeSeriesResults(), elementName);
        elementResultsDomList.add(timeSeriesResults);

        return div(attrs(".element-results"), elementResultsDomList.toArray(new DomContent[]{}));
    } // printElementResults()
    /**
     * Takes a list of StatisticResults, and create a table for that list.
     * @param statisticResultList A list of Statistic Results.
     * @return A DomContent table of Statistic Results.
     */
    private DomContent printStatisticResult(List<StatisticResult> statisticResultList) {
        List<DomContent> statisticResultDomList = new ArrayList<>();

        /* Adding Data of the Table */
        for(StatisticResult statisticResult : statisticResultList) {
            String statisticName = statisticResult.getName();
            /* Skip unnecessary StatisticResults */
            if(!validStatisticResult().contains(statisticName)) { continue; }
            /* Print out StatisticResult Table */
            List<String> rowContent = Arrays.asList(statisticName, statisticResult.getValue(), statisticResult.getUnits());
            DomContent row = printTableDataRow(rowContent);
            statisticResultDomList.add(row);
        } // Loop: to get DomContent rows for table

        /* Addng Head of the Table if there is a table */
        if(!statisticResultDomList.isEmpty()) {
            DomContent head = printTableHeadRow(Arrays.asList("Name", "Value", "Unit"));
            statisticResultDomList.add(0, head); // Add to front
            statisticResultDomList.add(0, caption("Statistics"));
        } // If: There is a table

        return table(attrs(".statistic-result"), statisticResultDomList.toArray(new DomContent[]{}));
    } // printStatisticResults()
    /**
     * A List of valid Statistic Results
     * @return a List of Strings (of valid Statistic Results)
     */
    private List<String> validStatisticResult() {
        List<String> stringList = new ArrayList<>();

        stringList.add("Peak Discharge");
        stringList.add("Precipitation Volume");
        stringList.add("Loss Volume");
        stringList.add("Excess Volume");
        stringList.add("Date/Time of Peak Discharge");
        stringList.add("Direct Runoff Volume");
        stringList.add("Baseflow Volume");
        stringList.add("Discharge Volume");

        return stringList;
    } // validStatisticResult()
    private DomContent printTimeSeriesResult(List<TimeSeriesResult> timeSeriesResultList, String elementName) {
        List<DomContent> timeSeriesPlotDomList = new ArrayList<>();
        List<DomContent> maxPlotDom = new ArrayList<>();
        int maxPlotsPerPage = 2;

        for(TimeSeriesResult data : timeSeriesResultList) {
            if(!validTimeSeriesPlot(data.getType())) {
//                System.out.println("Unnecessary plot: " + data.getType());
                continue;
            } // If: is an unnecessary plot
            DomContent timeSeriesPlotDom = printTimeSeriesPlot(data, elementName);

            if(maxPlotDom.size() < maxPlotsPerPage) {
                maxPlotDom.add(timeSeriesPlotDom);
           } // If: we haven't reached 6 plots per page
            else {
                timeSeriesPlotDomList.add(div(attrs(".max-plot"), maxPlotDom.toArray(new DomContent[]{})));
                maxPlotDom.clear();
                maxPlotDom.add(timeSeriesPlotDom);
            } // Else: we have reached 6 plots per page
        } // Loop: to print each TimeSeriesResult's plot

        if(!maxPlotDom.isEmpty()) {
            timeSeriesPlotDomList.add(div(attrs(".non-max-plot"), maxPlotDom.toArray(new DomContent[]{})));
        }

        return div(attrs(".group-plot"), timeSeriesPlotDomList.toArray(new DomContent[]{}));
    } // printTimeSeriesResult
    private DomContent printTimeSeriesPlot(TimeSeriesResult timeSeriesResult, String elementName) {
        Table timeSeriesTable = getTimeSeriesTable(timeSeriesResult);
        String plotName = timeSeriesResult.getType();
        Figure timeSeriesFigure = FigureCreator.createTimeSeriesPlot(plotName, timeSeriesTable,
                "Time", "Value", timeSeriesResult.getUnitType(), timeSeriesResult.getUnit());
        String plotDivName = StringBeautifier.getPlotDivName(elementName, plotName);

        Page page = Page.pageBuilder(timeSeriesFigure, plotDivName).build();
        String plotHtml = page.asJavascript();

        Document doc = Jsoup.parse(plotHtml);
        Elements elements = doc.select("body").first().children();
        String content = elements.outerHtml();
        DomContent domContent = join(content);

        return div(attrs(".single-plot"), domContent);
    } // printTimeSeriesPlot()
    private Boolean validTimeSeriesPlot(String plotName) {
        if(plotName.contains("Precipitation")) {
            return true;
        } // If: plotName contains Precipitation

        if(plotName.contains("Outflow")) {
            return true;
        } // If: plotName contains Outflow

        return false;
    } // validTimeSeriesPlot()
    private Table getTimeSeriesTable(TimeSeriesResult timeSeriesResult) {
        Table timeSeriesPlot = null;

        /* Get readable date format */
        List<ZonedDateTime> zonedDateTimeList = timeSeriesResult.getTimes();
        List<String> reformattedTimeList = new ArrayList<>();
        String dateFormat = "yyyy-MM-dd kk:mm:ss";
        for(ZonedDateTime zonedDateTime : zonedDateTimeList) {
            String reformattedDate = TimeConverter.toString(zonedDateTime, dateFormat);
            reformattedTimeList.add(reformattedDate);
        } // Loop: to convert ZonedDateTime to acceptable format

        /* Get readable value */
        double[] valueArray = timeSeriesResult.getValues();
        List<String> reformattedValueList = new ArrayList<>();
        for(double value : valueArray) {
            String reformattedValue = Double.toString(value);
            reformattedValueList.add(reformattedValue);
        } // Loop: to convert valueArray to String

        /* Writing time and value out to a csv file */
        try {
            File outputFile = new File("src/resources/timeSeriesResult.csv");
            FileWriter writer = new FileWriter(outputFile);
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Time", "Value"));

            for(int i = 0; i < reformattedTimeList.size(); i++) {
                String time = reformattedTimeList.get(i);
                String value = reformattedValueList.get(i);
                printer.printRecord(time, value);
            } // Loop: to print out every time & value pair

            // Flush Printer, and Close Writer
            printer.flush();
            writer.close();

            // Read in CSV file to get a Table
            timeSeriesPlot = Table.read().csv(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return timeSeriesPlot;
    } // timeSeriesToCsv

    private void convertPlotlyToStatic(String pathToHtml) {
        try {
            String htmlContent = FileUtils.readFileToString(new File(pathToHtml), StandardCharsets.UTF_8);
            htmlContent = htmlContent.replace("layout);", "layout, {staticPlot: true});");
            String pathToStaticPlotHtml = pathToHtml.replace(".html", "-static.html");
            File staticPlotHtml = new File(pathToStaticPlotHtml);
            FileUtils.writeStringToFile(staticPlotHtml, htmlContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // convertPlotlyToStatic()

    private void setPlotlyFont(String pathToHtml, String fontFamily, String fontSize) {
        try {
            String htmlContent = FileUtils.readFileToString(new File(pathToHtml), StandardCharsets.UTF_8);
            htmlContent = htmlContent.replace("var layout = {",
                    "var layout = { font: { family: '" + fontFamily + "', size: " + fontSize + "},");
            File staticPlotHtml = new File(pathToHtml);
            FileUtils.writeStringToFile(staticPlotHtml, htmlContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // setPlotlyFont()

    private void writeToFile(String pathToHtml, String content) {
        /* Writing to HTML file */
        String fullPathToHtml = Paths.get(pathToHtml).toAbsolutePath().toString();
        String fullPathToPdf = fullPathToHtml.replaceAll("html", "pdf");
        try { FileUtils.writeStringToFile(new File(pathToHtml), content, StandardCharsets.UTF_8); }
        catch (IOException e) { e.printStackTrace(); }

        setPlotlyFont(fullPathToHtml, "Times New Roman, serif", "12");
        convertPlotlyToStatic(fullPathToHtml);


    } // writeToFile()
} // HmsReportWriter class

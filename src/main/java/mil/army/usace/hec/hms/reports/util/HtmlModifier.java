package mil.army.usace.hec.hms.reports.util;

import j2html.tags.DomContent;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.*;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static j2html.TagCreator.*;
import static j2html.TagCreator.tr;

public class HtmlModifier {
    private HtmlModifier() {}

    public static DomContent printTableHeadRow(List<String> headRow, String thAttribute, String trAttribute) {
        List<DomContent> domList = new ArrayList<>();

        for(String column : headRow) {
            String reformatString = StringBeautifier.beautifyString(column);
            DomContent headDom = th(attrs(thAttribute), reformatString);
            domList.add(headDom);
        } // Loop: through headRow list

        return tr(attrs(trAttribute), domList.toArray(new DomContent[]{}));
    } // printTableHeadRow()

    public static DomContent printTableDataRow(List<String> dataRow, String tdAttribute, String trAttribute) {
        List<DomContent> domList = new ArrayList<>();

        for(String data : dataRow) {
            String reformatString = StringBeautifier.beautifyString(data);
            DomContent dataDom = td(attrs(tdAttribute), reformatString); // Table Data type
            domList.add(dataDom);
        } // Convert 'data' to Dom

        return tr(attrs(trAttribute), domList.toArray(new DomContent[]{})); // Table Row type
    } // printTableDataRow()

    public static DomContent extractPlotlyJavascript(String plotHtml) {
        Document doc = Jsoup.parse(plotHtml);
        Elements elements = doc.select("body").first().children();
        String content = elements.outerHtml();
        DomContent domContent = join(content);
        return domContent;
    } // extractPlotlyJavascript()

    public static void writeToFile(String pathToHtml, String content) {
        /* Writing to HTML file */
        String fullPathToHtml = Paths.get(pathToHtml).toAbsolutePath().toString();
        content = content.replace("layout);", "layout, {staticPlot: true});");
        String fullPathToCss = Paths.get(pathToHtml).getParent().toAbsolutePath().toString() + File.separator + "style.css";
        try {
            FileUtils.writeStringToFile(new File(pathToHtml), content, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(new File(fullPathToCss), getStyleCss(), StandardCharsets.UTF_8);
        }
        catch (IOException e) { e.printStackTrace(); }
        setPlotlyFont(fullPathToHtml, "Vollkorn, serif", "12");
//        convertPlotlyToStatic(fullPathToHtml);
    } // writeToFile()

    private static void convertPlotlyToStatic(String pathToHtml) {
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

    private static void setPlotlyFont(String pathToHtml, String fontFamily, String fontSize) {
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

    private static String getStyleCss() {
        String content = "@import url('https://fonts.googleapis.com/css?family=Vollkorn:400,700&display=swap');\n" +
                "\n" +
                "body{\n" +
                "\tbackground-color: white;\n" +
                "\tfont-family: 'Vollkorn', serif;\n" +
                "    width: 8.5in;\n" +
                "    height: 11in;\n" +
                "}\n" +
                "\n" +
                "/* CSS for Global tables's div */\n" +
                "div.global-parameter{\n" +
                "    page-break-after: always;\n" +
                "}\n" +
                "\n" +
                "/* CSS for each Element */\n" +
                "div.element{\n" +
                "\t/* Page break after each element */\n" +
                "    page-break-after: always;\n" +
                "}\n" +
                "\n" +
                "/* CSS for each ElementInput's All Tables of Processes */\n" +
                "div.table-process{\n" +
                "\t/* display: grid;\n" +
                "\tgrid-template-columns: repeat(auto-fill, minmax(4in, 1fr)); */\n" +
                "\t/*grid-template-columns: auto auto;*/\n" +
                "}\n" +
                "\n" +
                "/* CSS for ElementResult's with max amount of plots per page */\n" +
                "div.max-plot{\n" +
                "\tpage-break-after: always;\n" +
                "\tpage-break-inside: avoid;\n" +
                "\t/* display: grid;\n" +
                "\tgrid-template-columns: repeat(auto-fill, minmax(4in, 1fr)); */\n" +
                "}\n" +
                "\n" +
                "/* CSS for ElementResult's with non-max amount of plots per page */\n" +
                "div.non-max-plot{\n" +
                "\tpage-break-before: auto;\n" +
                "\tpage-break-inside: avoid;\n" +
                "}\n" +
                "\n" +
                "/* CSS for the Caption of all Tables */\n" +
                "caption{\n" +
                "\tcaption-side: top;\n" +
                "\tfont-weight: bold;\n" +
                "}\n" +
                "\n" +
                "/* CSS for Non-nested Tables under ElementInput */\n" +
                "table.single{\n" +
                "\twidth: 100%;\n" +
                "\tpadding-bottom: 0.3in;\n" +
                "}\n" +
                "\n" +
                "/* CSS for Nested Tables under ElementInput */\n" +
                "table.nested{\n" +
                "\tmargin: auto;\n" +
                "\tpage-break-inside: avoid;\n" +
                "\twidth: 100%;\n" +
                "}\n" +
                "\n" +
                "/* CSS for StatisticResult's table */\n" +
                "table.statistic-result{\n" +
                "\tpage-break-inside: avoid;\n" +
                "\twidth: 100%;\n" +
                "\t/* padding-bottom: 0.3in; */\n" +
                "}\n" +
                "\n" +
                "/* CSS for GlobalSummary's table */\n" +
                "table.global-summary{\n" +
                "\twidth: 100%;\n" +
                "\tpadding-bottom: 0.3in;\n" +
                "}\n" +
                "\n" +
                "/* CSS for GlobalParameter's tables */\n" +
                "table.global-parameter{\n" +
                "    page-break-inside: avoid;\n" +
                "    width: 100%;\n" +
                "    padding-bottom: 0.3in;\n" +
                "}\n" +
                "\n" +
                "/* CSS for all Data Columns */\n" +
                "td{\n" +
                "\ttext-align: center;\n" +
                "\tpadding-left: 0.2in;\n" +
                "\tpadding-right: 0.2in;\n" +
                "}\n" +
                "\n" +
                "/* CSS for all Header Columns */\n" +
                "th{\n" +
                "\ttext-align: center;\n" +
                "\tborder-bottom: 1px solid black;\n" +
                "}\n" +
                "\n" +
                "/* CSS for first Data Column */\n" +
                "td:nth-child(1){\n" +
                "\ttext-align: left;\n" +
                "\tpadding-left: 0.1in;\n" +
                "\tpadding-right: 0.1in;\n" +
                "}\n" +
                "\n" +
                "/* CSS for first Data Column of Non-Nested Tables under ElementInput */\n" +
                "td.element-non-nested:nth-child(1){\n" +
                "\ttext-align: left;\n" +
                "\tpadding-left: 0.1in;\n" +
                "\tpadding-right: 0.1in;\n" +
                "\twidth: 32%;\n" +
                "}\n" +
                "\n" +
                "/* CSS for first Data Column of Global Parameter Tables*/\n" +
                "td.global-parameter:nth-child(1){\n" +
                "\ttext-align: left;\n" +
                "\tpadding-left: 0.1in;\n" +
                "\tpadding-right: 0.1in;\n" +
                "\twidth: 32%;\n" +
                "}\n" +
                "\n" +
                "/* CSS for each Cell in a nested Table under ElementInput */\n" +
                "td.nested-table{\n" +
                "\tbackground-color: white;\n" +
                "\tpadding-top: 0.2in;\n" +
                "}\n" +
                "\n" +
                "/* CSS for odd Table Rows */\n" +
                "tr:nth-child(odd){\n" +
                "  background: #eee;\n" +
                "  -webkit-print-color-adjust: exact;\n" +
                "}\n" +
                "\n" +
                "/* CSS for each of ElementInput's single processes */\n" +
                "p.single-process{\n" +
                "\tpadding-bottom: 0.3in;\n" +
                "}\n" +
                "\n" +
                "/*(@media print {\n" +
                "  @page { margin: 0; }\n" +
                "  body { margin: 1.6cm; }\n" +
                "} */\n";

        return content;
    } // getStyleCss()

} // FigureCreator class

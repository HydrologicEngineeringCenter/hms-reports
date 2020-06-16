package mil.army.usace.hec.hms.reports.util;

import j2html.tags.DomContent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static j2html.TagCreator.*;

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
    public static void writeStandardReportToFile(String pathToHtml, String content) {
        /* Writing to HTML file */
        String fullPathToHtml = Paths.get(pathToHtml).toAbsolutePath().toString();
        content = content.replace("layout);", "layout, {staticPlot: true});");
        String fullPathToCss = Paths.get(pathToHtml).getParent().toAbsolutePath().toString() + File.separator + "styleStandard.css";
        StringBeautifier.writeStringToFile(new File(pathToHtml), content);
        StringBeautifier.writeStringToFile(new File(fullPathToCss), getStandardReportCSS());
        setPlotlyFont(fullPathToHtml, "Vollkorn, serif", "12");
    } // writeStandardReportToFile()
    public static void writeStatisticsReportToFile(String pathToHtml, String content) {
        /* Writing to HTML file */
        String fullPathToHtml = Paths.get(pathToHtml).toAbsolutePath().toString();
        String fullPathToCss = Paths.get(pathToHtml).getParent().toAbsolutePath().toString() + File.separator + "styleStatistics.css";
        StringBeautifier.writeStringToFile(new File(fullPathToHtml), content);
        StringBeautifier.writeStringToFile(new File(fullPathToCss), getStatisticsCSS());
    } // writeStatisticsReportToFile()
    private static void setPlotlyFont(String pathToHtml, String fontFamily, String fontSize) {
        String htmlContent = StringBeautifier.readFileToString(new File(pathToHtml));
        htmlContent = htmlContent.replace("var layout = {",
                "var layout = { font: { family: '" + fontFamily + "', size: " + fontSize + "},");
        File staticPlotHtml = new File(pathToHtml);
        StringBeautifier.writeStringToFile(staticPlotHtml, htmlContent);
    } // setPlotlyFont()
    private static String getStandardReportCSS() {
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
                "    page-break-before: always;\n" +
                "}\n" +
                "\n" +
                "/* CSS for each Element */\n" +
                "div.element{\n" +
                "\t/* Page break after each element */\n" +
                "    page-break-after: always;\n" +
                "}\n" +
                "\n" +
                "/* CSS for each ElementInput */\n" +
                "div.element-input{\n" +
                "\tpage-break-before: always;\n" +
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
                "table.global-parameter:nth-child(2) {\n" +
                "\tpage-break-inside: auto;\n" +
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
    } // getStandardReportCSS()
    private static String getStatisticsCSS() {
        String content = "@import url('https://fonts.googleapis.com/css?family=Vollkorn:400,700&display=swap');\n" +
                "\n" +
                "body{\n" +
                "\tbackground-color: white;\n" +
                "\tfont-family: 'Vollkorn', serif;\n" +
                "\twidth: 8.5in;\n" +
                "\theight: 11in;\n" +
                "\t-webkit-print-color-adjust: exact;\n" +
                "}\n" +
                "\n" +
                "table {\n" +
                "\tborder: solid 1px;\n" +
                "\tborder-radius: 15px;\n" +
                "\tpadding: 2px;\n" +
                "\twidth: 100%;\n" +
                "}\n" +
                "\n" +
                "/* CSS for the Caption of all Tables */\n" +
                "caption{\n" +
                "\tcaption-side: top;\n" +
                "\tfont-weight: bold;\n" +
                "\tmargin-bottom: 15px;\n" +
                "}\n" +
                "\n" +
                "/* CSS for all Data Columns */\n" +
                "td{\n" +
                "\ttext-align: center;\n" +
                "\tpadding-left: 0.2in;\n" +
                "\tpadding-right: 0.2in;\n" +
                "\twidth: 100px;\n" +
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
                "/* CSS for odd Table Rows */\n" +
                "tr:nth-child(even){\n" +
                "\tbackground-color: #eee;\n" +
                "}\n" +
                "\n" +
                "/* CSS for button-styled data */\n" +
                "button {\n" +
                "\tborder: none;\n" +
                "\tborder-radius: 15px;\n" +
                "\twidth: 70%;\n" +
                "\tcolor: white;\n" +
                "}";
        return content;
    } // getStatisticsCSS()

} // FigureCreator class

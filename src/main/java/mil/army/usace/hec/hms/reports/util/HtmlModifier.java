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
        String fullPathToPdf = fullPathToHtml.replaceAll("html", "pdf");
        try { FileUtils.writeStringToFile(new File(pathToHtml), content, StandardCharsets.UTF_8); }
        catch (IOException e) { e.printStackTrace(); }
        setPlotlyFont(fullPathToHtml, "Vollkorn, serif", "12");
        convertPlotlyToStatic(fullPathToHtml);
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


} // FigureCreator class

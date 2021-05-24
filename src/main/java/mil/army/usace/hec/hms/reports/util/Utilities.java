package mil.army.usace.hec.hms.reports.util;

import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.enums.ParameterSummary;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.io.parser.BasinInputParser;
import mil.army.usace.hec.hms.reports.io.parser.BasinResultsParser;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utilities {
    private Utilities() {}

    /* FIXME: getAvailablePlot() is deprecated */
    public static List<String> getAvailablePlot(String pathToResult) {
        return new ArrayList<>();
    }

    public static List<String> getAvailablePlots(String pathToResult, SimulationType simulationType) {
        BasinResultsParser resultsParser = BasinResultsParser.builder()
                .pathToBasinResultsFile(pathToResult)
                .simulationType(simulationType)
                .build();
        return resultsParser.getAvailablePlots();
    }

    public static String getFilePath(String directoryToSearch, String fileName) {
        final String[] x = new String[1];

        try {
            Files.walk(Paths.get(directoryToSearch))
                    .filter(Files::isRegularFile)
                    .forEach((f)->{
                        String file = f.toString();
                        if(file.endsWith(fileName)) {
                            x[0] = file;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return x[0];
    } // getFilePath()

    public static Map<String, List<String>> getParameterMap(String pathToJsonInput, ParameterSummary parameterChoice) {
        Map<String, List<String>> parameterMap = new HashMap<>();
        BasinInputParser basinInputParser = BasinInputParser.builder().pathToBasinInputFile(pathToJsonInput).build();
        List<ElementInput> elementList = basinInputParser.getElementInput();

        for(ElementInput element : elementList) {
            String elementType = element.getElementType();
            List<String> typeProcess = new ArrayList<>();

            if(!parameterMap.containsKey(StringUtil.beautifyString(elementType))) {
                List<Process> processList = element.getProcesses();

                for(Process process : processList) {
                    if(parameterChoice == ParameterSummary.GLOBAL_PARAMETER) {
                        if(ValidCheck.unnecessaryGlobalParameterProcesses().contains(process.getName())) {
                            continue;
                        } // Skip: unnecessary processes
                    } // If: Global Parameters
                    else if(parameterChoice == ParameterSummary.ELEMENT_PARAMETER) {
                        if(ValidCheck.unnecessarySingleProcesses().contains(process.getName())) {
                            continue;
                        } // Skip: unnecessary element processes
                    } // Else if: Element Parameters

                    typeProcess.add(StringUtil.beautifyString(process.getName()));
                } // Get: all types for each process

                parameterMap.put(StringUtil.beautifyString(elementType), typeProcess);
            } // If: parameterMap doesn't already have this element type
        } // Loop: through all elements

        /* These four basin types do not contain Global Parameters */
        if(parameterChoice == ParameterSummary.GLOBAL_PARAMETER) {
            parameterMap.remove("Sink");
            parameterMap.remove("Source");
            parameterMap.remove("Reservoir");
            parameterMap.remove("Junction");
        } // If: Global Parameter

        return parameterMap;
    } // getParameterMap()

    public static JSONObject getJsonObject(String pathToJson) {
        /* Read in Json File */
        File file = new File(pathToJson);
        String content = StringUtil.readFileToString(file);
        /* Convert JSON string to JSONObject */
        return new JSONObject(content);
    } // getJsonObject()

    public static List<String> getAvailableBasinType(String pathToJsonInput) {
        List<String> availableBasinType = new ArrayList<>();
        BasinInputParser basinInputParser = BasinInputParser.builder().pathToBasinInputFile(pathToJsonInput).build();
        List<ElementInput> elementList = basinInputParser.getElementInput();

        for(ElementInput element : elementList) {
            String elementType = element.getElementType();
            if(!availableBasinType.contains(StringUtil.beautifyString(elementType))) {
                availableBasinType.add(StringUtil.beautifyString(elementType));
            } // availableBasinType()
        } // Loop: through all the elementList

        return availableBasinType;
    } // getAvailableBasinType()

    public static int getNumberOfElements(String pathToBasinInput) {
        BasinInputParser basinInputParser = BasinInputParser.builder().pathToBasinInputFile(pathToBasinInput).build();
        List<ElementInput> elementList = basinInputParser.getElementInput();
        return elementList.size();
    } // getNumberOfElements()

} // Utilities class

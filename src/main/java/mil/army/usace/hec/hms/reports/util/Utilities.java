package mil.army.usace.hec.hms.reports.util;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.io.BasinInputParser;
import mil.army.usace.hec.hms.reports.io.XmlBasinResultsParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utilities {
    private Utilities() {}

    public static List<String> getAvailablePlot(String pathToResult) {
        return XmlBasinResultsParser.getAvailablePlots(pathToResult);
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

    public static Map<String, List<String>> getParameterMap(String pathToJsonInput, String parameterChoice) {
        Map<String, List<String>> parameterMap = new HashMap<>();
        BasinInputParser basinInputParser = BasinInputParser.builder().pathToBasinInputFile(pathToJsonInput).build();
        List<ElementInput> elementList = basinInputParser.getElementInput();

        for(ElementInput element : elementList) {
            String elementType = element.getElementType();
            List<String> typeProcess = new ArrayList<>();

            if(!parameterMap.containsKey(StringBeautifier.beautifyString(elementType))) {
                List<Process> processList = element.getProcesses();

                for(Process process : processList) {
                    if(parameterChoice.equals("global")) {
                        if(ValidCheck.unnecessaryGlobalParameterProcesses().contains(process.getName())) {
                            continue;
                        } // Skip: unnecessary processes
                    } // If: Global Parameters
                    else if(parameterChoice.equals("element")) {
                        if(ValidCheck.unnecessarySingleProcesses().contains(process.getName())) {
                            continue;
                        } // Skip: unnecessary element processes
                    } // Else if: Element Parameters

                    typeProcess.add(StringBeautifier.beautifyString(process.getName()));
                } // Get: all types for each process

                if(!typeProcess.isEmpty())
                    parameterMap.put(StringBeautifier.beautifyString(elementType), typeProcess);
            } // If: parameterMap doesn't already have this element type
        } // Loop: through all elements

        /* These four basin types do not contain Global Parameters */
        if(parameterChoice.equals("global")) {
            parameterMap.remove("Sink");
            parameterMap.remove("Source");
            parameterMap.remove("Reservoir");
            parameterMap.remove("Junction");
        } // If: Global Parameter

        return parameterMap;
    } // getParameterMap()

    public static List<String> getAvailableBasinType(String pathToJsonInput) {
        List<String> availableBasinType = new ArrayList<>();
        BasinInputParser basinInputParser = BasinInputParser.builder().pathToBasinInputFile(pathToJsonInput).build();
        List<ElementInput> elementList = basinInputParser.getElementInput();

        for(ElementInput element : elementList) {
            String elementType = element.getElementType();
            if(!availableBasinType.contains(StringBeautifier.beautifyString(elementType))) {
                availableBasinType.add(StringBeautifier.beautifyString(elementType));
            } // availableBasinType()
        } // Loop: through all the elementList

        return availableBasinType;
    } // getAvailableBasinType()


} // Utilities class

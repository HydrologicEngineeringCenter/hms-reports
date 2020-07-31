package mil.army.usace.hec.hms.reports.io.parser;

import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.Parameter;
import mil.army.usace.hec.hms.reports.Process;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AsciiBasinInputParser extends BasinInputParser {

    AsciiBasinInputParser(Builder builder) {
        super(builder);
    }
    private List<String> basinTypes = Arrays.asList("Subbasin:","Sink:","Source:","Junction:","Reach:","Reservoir:");
    private List<String> basinFileLines = new ArrayList<>();
    private List<Integer> endLineList = new ArrayList<>();

    @Override
    public List<ElementInput> getElementInput() {
        List<ElementInput> elementInputList = new ArrayList<>(); // List of ElementInputs
        basinFileLines = getBasinFileLines();
        endLineList = getIndicesOfEndLines(basinFileLines);

        List<String> elementLineList = basinFileLines.stream().filter(line -> basinTypes.stream().anyMatch(line::startsWith)).collect(Collectors.toList());
        for(String elementLine : elementLineList) {
            ElementInput elementInput = populateElement(elementLine);
            elementInputList.add(elementInput);
        } // Loop: through each elementInput Lines

        return elementInputList;
    } // getElementInput()

    @Override
    public ZonedDateTime getLastModifiedTime() {
        /* Getting all lines */
        List<String> fileLines = getBasinFileLines();

        /* Filter out lines of Last Modified Date */
        List<String> lastModifiedDateList = fileLines.stream()
                .filter(line -> line.trim().startsWith("Last Modified Date:"))
                .collect(Collectors.toList());

        /* Convert to LocalDate for Comparison */
        Map<LocalDate, String> latestDateMap = new LinkedHashMap<>();
        for(String lastModifiedDate : lastModifiedDateList) {
            String date = lastModifiedDate.split(":", 2)[1].trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);
            latestDateMap.put(localDate, lastModifiedDate.trim());
        } // Loop: to convert to LocalDate for comparison

        /* Getting the latest date */
        LocalDate latestDate = latestDateMap.keySet().stream().max(LocalDate::compareTo).orElse(null);
        ZonedDateTime defaultTime = ZonedDateTime.of(1500, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT"));
        if(latestDate == null) { return defaultTime; }

        /* Getting the latest time */
        List<LocalTime> timeList = new ArrayList<>();
        for(int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            if(line.contains(latestDateMap.get(latestDate))) {
                LocalTime localTime = firstTimeAfterIndex(fileLines, i);
                if(localTime == null) { return defaultTime; }
                timeList.add(localTime);
            } // If: is the latest date
        } // Loop: through each line in file
        LocalTime latestTime = timeList.stream().max(LocalTime::compareTo).orElse(null);
        if(latestTime == null) { return defaultTime; }

        /* Returning ZonedDateTime */
        ZonedDateTime latestDateTime = ZonedDateTime.of(latestDate, latestTime, ZoneId.of("GMT"));

        return latestDateTime;
    } // getLastModifiedTime()

    @Override
    public String getHmsVersion() {
        List<String> fileLines = getBasinFileLines();

        String hmsVersion = fileLines.stream().filter(line -> line.trim().startsWith("Version:"))
                .findFirst().orElse("").split(":", 2)[1].trim();

        return hmsVersion;
    } // getHmsVersion()

    private ElementInput populateElement(String elementLine) {
        /* ElementInput's name and type */
        String basinName = elementLine.substring(elementLine.indexOf(":") + 1).trim();
        String basinType = elementLine.substring(0, elementLine.indexOf(":")).trim();
        int lineStartIndex = basinFileLines.indexOf(elementLine);
        int endLineIndex = getFirstAfterIndex(endLineList, lineStartIndex);
        int[] basinLineRange = new int[]{lineStartIndex, endLineIndex};
        /* ElementInput's processes */
        List<Process> processList = getProcessList(basinType, basinLineRange);

        /* Building ElementInput object */
        ElementInput elementInput = ElementInput.builder()
                .name(basinName)
                .elementType(basinType)
                .processes(processList)
                .build();

        return elementInput;
    } // populateElement()

    private List<Process> getProcessList(String basinType, int[] basinLineRange) {
        List<Process> processList = new ArrayList<>();
        List<String> singleProcesses = basinTypeAndSingleProcesses().get(basinType);
        List<String> parameterProcesses = basinTypeAndParameterProcesses().get(basinType);
        int startIndex = basinLineRange[0], endIndex = basinLineRange[1];

        for(String singleProcess : singleProcesses) {
            Map<String, Integer> matchedMap = findMatchedString(singleProcess, startIndex, endIndex);
            String matchedLine = matchedMap.keySet().stream().findFirst().map(Object::toString).orElse("");
            if(matchedLine.isEmpty()) {continue;}
            String processName = matchedLine.substring(0, matchedLine.indexOf(":")).trim();
            String processValue = matchedLine.substring(matchedLine.indexOf(":") + 1).trim();
            List<Parameter> parameters = new ArrayList<>();
            Process process = Process.builder().name(processName).value(processValue).parameters(parameters).build();
            processList.add(process);
        } // Loop: through singleProcesses

        for(String paramProcess : parameterProcesses) {
            Map<String, Integer> matchedMap = findMatchedString(paramProcess, startIndex, endIndex);
            String matchedLine =  matchedMap.keySet().stream().findFirst().map(Object::toString).orElse("");
            if(matchedLine.isEmpty()) {continue;}
            String processName = matchedLine.substring(0, matchedLine.indexOf(":")).trim();
            String processValue = matchedLine.substring(matchedLine.indexOf(":") + 1).trim();
            if(processValue.equals("None")) { continue; } // Skip Processes that have None Method
            int processStartIndex = matchedMap.get(matchedLine);
            List<Parameter> parameters;
            if(processName.equals("Baseflow")) { parameters = getBaseflowParameter(processStartIndex + 1, endIndex); }
            else if(processName.equals("Route")) { parameters = getRouteParameter(processStartIndex, endIndex); }
            else { parameters = getParameters(processName, processStartIndex, endIndex); }
            Process process = Process.builder().name(processName).value(processValue).parameters(parameters).build();
            processList.add(process);
        } // Loop: through parameterProcesses

        return processList;
    } // getProcessList()

    private List<Parameter> getParameters(String processName, int processStartIndex, int basinEndIndex) {
        List<Parameter> parameterList = new ArrayList<>();
        String endProcessLine = "";
        if(processName.equals("Canopy")) { endProcessLine = "End Canopy:"; }

        for(int i = processStartIndex + 1; i < basinEndIndex; i++) {
            String line = basinFileLines.get(i).trim();
            if(line.isEmpty() || (!endProcessLine.isEmpty() && line.startsWith(endProcessLine))) { break; }
            String parameterName = line.substring(0, line.indexOf(":")).trim();
            String parameterValue = line.substring(line.indexOf(":") + 1).trim();
            List<Parameter> subParameters = new ArrayList<>();
            Parameter parameter = Parameter.builder().name(parameterName).value(parameterValue).subParameters(subParameters).build();
            parameterList.add(parameter);
        } // Loop: from processStartIndex until finished with process

        return parameterList;
    } // getParameters()

    private List<Parameter> getBaseflowParameter(int processStartIndex, int basinEndIndex) {
        List<Parameter> parameterList = new ArrayList<>();
        boolean hasLayers = false;

        for(int i = processStartIndex; i < basinEndIndex; i++) {
            String line = basinFileLines.get(i).trim();
            if(line.isEmpty()) { break; }
            String parameterName = line.substring(0, line.indexOf(":")).trim();
            String parameterValue = line.substring(line.indexOf(":") + 1).trim();
            List<Parameter> subParameterList = new ArrayList<>();
            if(parameterName.equals("Groundwater Layer")) {
                hasLayers = true;
                parameterName = line.substring(line.indexOf(":") + 1).trim();
                parameterValue = "";
                for(int j = i + 1; j < basinEndIndex + 1; j++) {
                    String subParamLine = basinFileLines.get(j).trim();
                    if(subParamLine.startsWith("Groundwater Layer") || j == basinEndIndex) {
                        i = j - 1;
                        break;
                    } // If: Next Layer
                    String subParamName = subParamLine.substring(0, subParamLine.indexOf(":")).trim();
                    String subParamvalue = subParamLine.substring(subParamLine.indexOf(":") + 1).trim();
                    Parameter subParameter = Parameter.builder().name(subParamName).value(subParamvalue).subParameters(new ArrayList<>()).build();
                    subParameterList.add(subParameter);
                } // Loop: through subParameter lines
            } // If: Groundwater Layer

            Parameter parameter = Parameter.builder().name(parameterName).value(parameterValue).subParameters(subParameterList).build();
            parameterList.add(parameter);
        } // Loop: from processStartIndex until finished with process

        if(!hasLayers) {
            Parameter layerParameter = Parameter.builder().name("1").value("").subParameters(parameterList).build();
            parameterList = Collections.singletonList(layerParameter);
        } // If: No Layers, turn into 1 Layer

        String name = "baseflowLayerList";
        String value = "";
        List<Parameter> subParameters = parameterList;
        Parameter parameter = Parameter.builder().name(name).value(value).subParameters(subParameters).build();

        return Collections.singletonList(parameter);
    } // getBaseflowParameter()

    private List<Parameter> getRouteParameter(int processStartIndex, int basinEndIndex) {
        List<Parameter> parameterList = new ArrayList<>();

        String processLine = basinFileLines.get(processStartIndex).trim();
        String routeMethod = processLine.substring(processLine.indexOf(":") + 1).trim();

        Parameter methodParameter = Parameter.builder().name("method").value(routeMethod).subParameters(new ArrayList<>()).build();
        parameterList.add(methodParameter);

        for(int i = processStartIndex + 1; i < basinEndIndex; i++) {
            String paramLine = basinFileLines.get(i).trim();
            String name = paramLine.substring(0, paramLine.indexOf(":")).trim();
            String value = paramLine.substring(paramLine.indexOf(":") + 1).trim();
            if(value.equals("None")) { continue; } // Skip Parameters with 'None' Value
            Parameter parameter = Parameter.builder().name(name).value(value).subParameters(new ArrayList<>()).build();
            parameterList.add(parameter);
        } // Loop: until the end of Basin

        return parameterList;
    } // getRouteParameter()

    /* Helper Functions */
    private List<String> getBasinFileLines() {
        List<String> fileLines = new ArrayList<>();
        Scanner fileScanner = null;

        /* Read in Scanner */
        try { fileScanner = new Scanner(pathToBasinInputFile); }
        catch (IOException e) { e.printStackTrace(); }
        assert fileScanner != null;
        while(fileScanner.hasNext()) { fileLines.add(fileScanner.nextLine()); }

        return fileLines;
    } // getBasinFileScanner()

    private List<Integer> getIndicesOfEndLines(List<String> fileLines) {
        List<Integer> indexList = new ArrayList<>();
        for(int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            if(line.startsWith("End:")) indexList.add(i);
        } // Loop: to get Indices of End Lines
        return indexList;
    } // getIndicesOfEndLines()

    private int getFirstAfterIndex(List<Integer> indexList, int index) {
        for(int i : indexList) { if(i > index) return i; }
        return -1;
    } // getFirstAfterIndex()

    private int[] getProcessRange(int startIndex, int endIndex) {
        int[] processRange = new int[2];
        int processStart = startIndex, processEnd = endIndex;

        for(int i = startIndex; i < endIndex; i++) {
            String line = basinFileLines.get(i);
            if(line.isEmpty()) {
                processEnd = i;
                break;
            } // If: Line is empty
        } // Loop: to find empty line

        processRange[0] = processStart;
        processRange[1] = processEnd;

        return processRange;
    } // getProcessRange()

    private Map<String, List<String>> basinTypeAndParameterProcesses() {
        Map<String, List<String>> typeProcessMap = new LinkedHashMap<>();

        List<String> subbasinProcessList = Arrays.asList("Loss", "Canopy", "Transform", "Baseflow");
        typeProcessMap.put("Subbasin", subbasinProcessList);

        List<String> reachProcessList = Arrays.asList("Route");
        typeProcessMap.put("Reach", reachProcessList);

        List<String> reservoirProcessList = Arrays.asList();
        typeProcessMap.put("Reservoir", reservoirProcessList);

        List<String> junctionProcessList = Arrays.asList();
        typeProcessMap.put("Junction", junctionProcessList);

        List<String> sourceProcessList = Arrays.asList();
        typeProcessMap.put("Source", sourceProcessList);

        List<String> sinkProcessList = Arrays.asList();
        typeProcessMap.put("Sink", sinkProcessList);

        return typeProcessMap;
    } // basinTypeAndParameterProcesses()

    private Map<String, List<String>> basinTypeAndSingleProcesses() {
        Map<String, List<String>> typeProcessMap = new LinkedHashMap<>();

        List<String> subbasinProcessList = Arrays.asList("Area", "Latitude", "Longitude", "Downstream");
        typeProcessMap.put("Subbasin", subbasinProcessList);

        List<String> reachProcessList = Arrays.asList("Downstream");
        typeProcessMap.put("Reach", reachProcessList);

        List<String> reservoirProcessList = Arrays.asList("Downstream");
        typeProcessMap.put("Reservoir", reservoirProcessList);

        List<String> junctionProcessList = Arrays.asList("Downstream");
        typeProcessMap.put("Junction", junctionProcessList);

        List<String> sourceProcessList = Arrays.asList("Area", "Downstream", "Flow Method", "Flow Gage");
        typeProcessMap.put("Source", sourceProcessList);

        List<String> sinkProcessList = Arrays.asList();
        typeProcessMap.put("Sink", sinkProcessList);

        return typeProcessMap;
    } // basinTypeAndSingleProcesses()

    private Map<String, Integer> findMatchedString(String match, int startIndex, int endIndex) {
        Map<String, Integer> matchedMap = new HashMap<>();
        for(int i = startIndex; i < endIndex; i++) {
            String line = basinFileLines.get(i).trim();
            if(line.startsWith(match)) { matchedMap.put(line, i); }
        } // Loop: through specified indices
        return matchedMap;
    } // findMatchedString()

    private LocalTime firstTimeAfterIndex(List<String> fileLines, int index) {
        for(int i = index + 1; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            if(line.trim().startsWith("Last Modified Time:")) {
                String timeString = line.split(":", 2)[1].trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                return LocalTime.parse(timeString, formatter);
            } // If: is Last Modified Time
        } // Loop: through file lines
        return null;
    } // firstTimeAfterIndex()

} // AsciiBasinInputParser class

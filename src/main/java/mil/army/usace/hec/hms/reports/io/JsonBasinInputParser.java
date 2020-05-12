package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.Parameter;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.util.StringBeautifier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonBasinInputParser extends BasinInputParser {

    JsonBasinInputParser(Builder builder) {
        super(builder);
    }

    @Override
    public List<ElementInput> getElementInput() {
        List<ElementInput> elementInputList = new ArrayList<>(); // List of ElementInputs
        JSONObject jsonFile = getJsonObject(this.pathToBasinInputFile.toString()); // Get Json Object
        JSONArray elementArray = jsonFile.getJSONObject("elementList").getJSONArray("elements");

        for(int i = 0; i < elementArray.length(); i++) {
            ElementInput elementInput = populateElement(elementArray.getJSONObject(i));
            elementInputList.add(elementInput);
        } // Loop over all elements and populate them

        return elementInputList;
    } // getElementInput()
    private JSONObject getJsonObject(String pathToJson) {
        /* Read in Json File */
        File file = new File(pathToJson);
        String content = StringBeautifier.readFileToString(file);
        /* Convert JSON string to JSONObject */
        return new JSONObject(content);
    } // getJsonObject()
    private ElementInput populateElement(JSONObject object) {
        /* Parsing each ElementInput for its: name, elementType, and Processes */
        String name = object.getString("name");
        String elementType = object.getString("elementType");

        /* Loop through objectArray to populate 'processes' */
        List<Process> processes = new ArrayList<>();
        for(String key : object.keySet()) {
            // Skipping unnecessary contents
            List<String> unusedVariables = unnecessaryContent();
            if(unusedVariables.contains(key)) { continue; }
            Process objectProcess = populateProcess(object, key);

            if(objectProcess.getParameters().isEmpty() && objectProcess.getValue().equals("")) {
                continue;
            } // If: Process doesn't contain any content

            processes.add(objectProcess);
        } // Loop through all keys in object's keySet

        // Sorting processes in order
        Collections.sort(processes);

        /* Building ElementInput object */
        ElementInput elementInput = ElementInput.builder()
                .name(name)
                .elementType(elementType)
                .processes(processes)
                .build();

        return elementInput;
    } // populateElement()
    private List<String> unnecessaryContent() {
        List<String> stringList = new ArrayList<>();
        stringList.add("schematicProperties");
        stringList.add("lastModifiedTime");
        /* Add more if necessary */
        return stringList;
    } // unnecessaryContent()
    private Process populateProcess(JSONObject elementObject, String keyName) {
        String name = keyName;
        String value = "";
        List<Parameter> parameters = new ArrayList<>();
        /* Check for Processes that does not have table-like parameters */
        if(elementObject.optJSONObject(keyName) == null) {
            if(!elementObject.opt(keyName).toString().equals("NONE"))
                value = elementObject.opt(keyName).toString();
        } // If: 'Process' is not type JSONObject. Ex: Reach's Name
        else { /* Populating Process with parameters */
            JSONObject processObject = elementObject.getJSONObject(keyName);
            for(String paramKey : processObject.keySet()) {
                Parameter param = populateParameter(processObject, paramKey, paramKey);
                if(param.getSubParameters().isEmpty() && param.getValue().equals("")) {
                    continue; // Move on to next Parameter
                } // If: paramater has no value, nor sub-parameters

                parameters.add(param);
            } // Loop: Populate Process with its parameters
        } // Else: 'Process' is type JSONObject. Ex: Reach's Route

        /* Building 'Process" object */
        Process process = Process.builder()
                .name(name)
                .value(value)
                .parameters(parameters)
                .build();

        return process;
    } // populateProcess()
    private Parameter populateParameter(JSONObject processObject, String keyName, String paramName) {
        String name = paramName;
        String value = "";
        List<Parameter> subParameters = new ArrayList<>();

        if(processObject.optJSONArray(keyName) != null) {
            JSONArray paramArray = processObject.getJSONArray(keyName);
            for(int i = 0; i < paramArray.length(); i++) {
                Parameter subParam = populateArrayParameter(paramArray, i);
                subParameters.add(subParam);
            } // Loop: through JSONArray
        } // If: 'Parameter' is a JSONArray. Ex: BaseflowLayerList
        else if(processObject.optJSONObject(keyName) != null) {
            JSONObject paramObject = processObject.getJSONObject(keyName);
            for(String subParamKey : paramObject.keySet()) {
                Parameter subParam = populateParameter(paramObject, subParamKey, subParamKey);
                subParameters.add(subParam);
            } // Loop: through all SubParameters inside Parameter
        } // If: 'Parameter' is a JSONObject. Ex: Route
        else {
            String paramValue = processObject.opt(keyName).toString();
            if(!paramValue.equals("NONE") && !paramValue.equals("UNKNOWN")) {
                value = paramValue;
            } // If: processValue is not None -> saves value
        } // Else: 'Parameter' is not a JSONObject. Ex: Method

        Parameter parameter = Parameter.builder()
                .name(name)
                .value(value)
                .subParameters(subParameters)
                .build();

        return parameter;
    } // populateParameter()
    private Parameter populateArrayParameter(JSONArray processArray, int index) {
        JSONObject paramObject = processArray.getJSONObject(index);
        String name = Integer.toString(index + 1);
        List<Parameter> subParameters = new ArrayList<>();

        for(String subParamKey : paramObject.keySet()) {
            Parameter subParam = populateParameter(paramObject, subParamKey, subParamKey);
            subParameters.add(subParam);
        }

        Parameter parameter = Parameter.builder()
                .name(name)
                .value("")
                .subParameters(subParameters)
                .build();

        return parameter;
    } // populateArrayParameter()

} // JsonBasinInputParser class

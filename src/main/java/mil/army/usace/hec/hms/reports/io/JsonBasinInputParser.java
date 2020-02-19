package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.Parameter;
import mil.army.usace.hec.hms.reports.Process;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
        String content = null;

        try {
            content = FileUtils.readFileToString(file, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } // Read Json's content to 'content'

        /* Convert JSON string to JSONObject */
        assert content != null;
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
                System.out.println("Eliminating Process: " + objectProcess.getName());
                continue;
            } // If: Process doesn't contain any content

            processes.add(objectProcess);
        } // Loop through all keys in object's keySet

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
            value = elementObject.opt(keyName).toString();
        } // If: 'Process' is not type JSONObject. Ex: Reach's Name
        else { /* Populating Process with parameters */
            JSONObject processObject = elementObject.getJSONObject(keyName);
            for(String paramKey : processObject.keySet()) {
                Parameter param = populateParameter(processObject, paramKey, paramKey);
                if(param.getSubParameters().isEmpty() && param.getValue().equals("")) {
                    System.out.println("Eliminating: " + param.getName());
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

        if(specialParameters().contains(keyName)) {
            subParameters = populateSpecialParameter(processObject, keyName);
        } // If: Special cases that need to be flattened (Ex: Base flow)
        else if(processObject.optJSONObject(keyName) != null) {
            JSONObject paramObject = processObject.getJSONObject(keyName);
            for(String subParamKey : paramObject.keySet()) {
                Parameter subParam = populateParameter(paramObject, subParamKey, subParamKey);
                subParameters.add(subParam);
            } // Loop: through all SubParameters inside Parameter
        } // If: 'Parameter' is a JSONObject. Ex: Route
        else {
            String paramValue = processObject.opt(keyName).toString();
            if(!paramValue.equals("NONE")) {
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
    private List<String> specialParameters() {
        List<String> stringList = new ArrayList<>();
        stringList.add("baseflowLayerList");
        /* Add more if necessary */
        return stringList;
    } // specialParameters()
    private List<Parameter> populateSpecialParameter(JSONObject processObject, String keyName) {
        List<Parameter> subParameters = new ArrayList<>();

        if(keyName.equals("baseflowLayerList")) {
            JSONArray baseflowLayerList = processObject.getJSONArray(keyName);
            for(int i = 0; i < baseflowLayerList.length(); i++) {
                String layerNum = Integer.toString(i + 1); // Starting with Layer 1 instead of 0
                JSONObject layer = baseflowLayerList.getJSONObject(i);
                for(String key : layer.keySet()) {
                    Parameter subParameter = populateParameter(layer, key, key + layerNum);
                    subParameters.add(subParameter);
                } // Loop: through each layer
            } // Loop: through baseflowLayerList
        } // Case: baseflowLayerList

        return subParameters;
    } // populateSpecialParameter()

} // JsonBasinInputParser class

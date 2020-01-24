package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.Parameter;
import mil.army.usace.hec.hms.reports.Process;
import mil.army.usace.hec.hms.reports.SubParameter;
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
                Parameter param = populateParameter(processObject, paramKey);
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

    private Parameter populateParameter(JSONObject processObject, String keyName) {
        String name = "";
        String value = "";
        List<SubParameter> subParameters = new ArrayList<>();

        if(processObject.optJSONObject(keyName) == null) {
            name = keyName;
            value = processObject.opt(keyName).toString();
        } // If: 'Parameter' is not type JSONObject. Ex. Route's Method
        else {
            JSONObject paramObject = processObject.getJSONObject(keyName);

            if(specialParameters().contains(keyName)) {
                subParameters = populateSpecialParameter(processObject, keyName);
            } // If: is a special SubParameter. Ex: baseflowLayerList 0 & 1
            else {
                for(String subKey : paramObject.keySet()) {
                    SubParameter subParam = populateSubParameter(paramObject, subKey);
                    subParameters.add(subParam);
                } // Loop: Populate Parameter with its SubParameters
            } // Else: Not a special parameter. Ex: Channel
        } // Else: 'Parameter' is type JSONObject. Ex: Route's Channel

        Parameter parameter = Parameter.builder()
                .name(name)
                .value(value)
                .subParameters(subParameters)
                .build();

        return parameter;
    } // populateParameter()

    private SubParameter populateSubParameter(JSONObject paramObject, String keyName) {
        String name = keyName;
        String value = paramObject.opt(keyName).toString();

        SubParameter subParameter = SubParameter.builder()
                .name(name)
                .value(value)
                .build();

        return subParameter;
    } // populateSubParameter()

    private List<String> specialParameters() {
        List<String> stringList = new ArrayList<>();
        stringList.add("baseflowLayerList");
        /* Add more if necessary */
        return stringList;
    } // unnecessaryContent()

    private List<SubParameter> populateSpecialParameter(JSONObject processObject, String keyName) {
        List<SubParameter> subParameters = new ArrayList<>();

        if(keyName.equals("baseflowLayerList")) {
            // JSONArray: baseflowLayerList
            JSONArray baseflowLayerList = processObject.getJSONArray(keyName);
            for(int i = 0; i < baseflowLayerList.length(); i++) {
                int layerNum = i + 1; // Starting with Layer 1 instead of 0
                JSONObject layer = baseflowLayerList.getJSONObject(i);
                for(String key : layer.keySet()) {
                    String name = key + layerNum;
                    String value = layer.getString(key);
                    SubParameter subParameter = SubParameter.builder()
                            .name(name)
                            .value(value)
                            .build();
                    subParameters.add(subParameter);
                } // Loop: through each layer
            } // Loop: through baseflowLayerList
        } // Case: baseflowLayerList

        return subParameters;
    } // populateSpecialParameter()

} // JsonBasinInputParser class

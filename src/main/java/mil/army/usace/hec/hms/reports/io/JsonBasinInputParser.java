package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.Parameter;
import mil.army.usace.hec.hms.reports.Process;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FileReader;
import org.apache.commons.io.FileUtils;
import java.io.File;

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
        } // If: 'Process' is not type JSONObject -> Doesn't hold table-like parameters
        else { /* Populating Process with parameters */
            JSONObject processObject = elementObject.getJSONObject(keyName);
            for(String paramKey : processObject.keySet()) {
                Parameter param = populateParameter(processObject, paramKey);
                parameters.add(param);
            } // Loop: Populate Process with its parameters
        } // Else: 'Process' is type JSONObject

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
        /* Check for parameters that does not contain a table */
        if(processObject.optJSONObject(keyName) == null) {
            name = keyName;
            value = processObject.opt(keyName).toString();
        } // If: 'Parameter' is not type JSONObject
        else { /* Special: Parameters that contain table-structure */
            // TODO: Flattening tables inside those parameters for now

        } // Else: 'Parameter' is type JSONObject

        Parameter parameter = Parameter.builder()
                .name(name)
                .value(value)
                .build();

        return parameter;
    } // populateParameter()

} // JsonBasinInputParser class

package mil.army.usace.hec.hms.reports;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Process implements Comparable<Process> {
    /* Class Variables */
    private final String name;
    private final String value;
    private final List<Parameter> parameters;

    /* Constructors */
    private Process(Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
        this.parameters = builder.parameters;
    } // Process Constructor

    @Override
    public int compareTo(@NonNull Process process) {
        List<String> orderedProcess = new ArrayList<>();
        orderedProcess.add("area");
        orderedProcess.add("latitude");
        orderedProcess.add("longitude");
        orderedProcess.add("downstream");

        int thisProcessIndex = 0, compareProcessIndex = 0;

        if(orderedProcess.contains(this.getName())) {
            thisProcessIndex = orderedProcess.indexOf(this.getName());
        } // If: this process is in the orderedProcess List

        if(orderedProcess.contains(process.getName())) {
            compareProcessIndex = orderedProcess.indexOf(process.getName());
        } // If: compare process is in the orderedProcess List

        // Ascending Order
        return thisProcessIndex - compareProcessIndex;
    } // compareTo()

    public static class Builder{
        String name;
        String value;
        List<Parameter> parameters;

        public Builder name(String name) {
            this.name = name;
            return this;
        } // 'name' constructor

        public Builder value(String value) {
            this.value = value;
            return this;
        } // 'value' constructor

        public Builder parameters(List<Parameter> parameters){
            this.parameters = parameters;
            return this;
        } // 'parameters' constructor

        public Process build(){
            return new Process(this);
        }
    } // Builder class: as Process's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Methods */
    public String getName() { return this.name; }
    public String getValue() { return this.value; }
    public List<Parameter> getParameters() { return this.parameters; }

} // Process Class

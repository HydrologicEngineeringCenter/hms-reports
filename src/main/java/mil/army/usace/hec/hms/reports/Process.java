package mil.army.usace.hec.hms.reports;

import java.util.List;

public class Process {
    /* Class Variables */
    private final String name;
    private final List<Parameter> parameters;

    /* Constructors */
    private Process(Builder builder) {
        this.name = builder.name;
        this.parameters = builder.parameters;
    } // Process Constructor

    public static class Builder{
        String name;
        List<Parameter> parameters;

        public Builder name(String name) {
            this.name = name;
            return this;
        } // 'name' constructor

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
    public List<Parameter> getParameters() { return this.parameters; }

} // Process Class

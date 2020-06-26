package mil.army.usace.hec.hms.reports;

import java.util.List;

public class Parameter {
    /* Class Variables */
    private final String name;
    private final String value;
    private final List<Parameter> subParameters;

    /* Constructors */
    private Parameter(Builder builder){
        this.name = builder.name;
        this.value = builder.value;
        this.subParameters = builder.subParameters;
    } // Parameter Constructor

    public static class Builder{
        String name;
        String value;
        List<Parameter> subParameters;

        public Builder name(String name){
            this.name = name;
            return this;
        } // 'name' constructor

        public Builder value(String value){
            this.value = value;
            return this;
        } // 'value' constructor

        public Builder subParameters(List<Parameter> subParameters){
            this.subParameters = subParameters;
            return this;
        } // 'subParameters' constructor

        public Parameter build(){
            return new Parameter(this);
        }
    } // Builder class: as Parameter's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Methods */
    public String getName(){
        return this.name;
    }
    public String getValue(){
        return this.value;
    }
    public List<Parameter> getSubParameters() { return this.subParameters; }

} // Parameter Class

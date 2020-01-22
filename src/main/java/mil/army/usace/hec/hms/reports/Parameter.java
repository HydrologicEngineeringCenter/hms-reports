package mil.army.usace.hec.hms.reports;

public class Parameter {
    /* Class Variables */
    private final String name;
    private final String value;

    /* Constructors */
    private Parameter(Builder builder){
        this.name = builder.name;
        this.value = builder.value;
    } // Parameter Constructor

    public static class Builder{
        String name;
        String value;

        public Builder name(String name){
            this.name = name;
            return this;
        } // 'name' constructor

        public Builder value(String value){
            this.value = value;
            return this;
        } // 'value' constructor

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

} // Parameter Class

package mil.army.usace.hec.hms.reports;

public class SubParameter {
    /* Class Variables */
    private final String name;
    private final String value;

    /* Constructors */
    private SubParameter(Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
    } // SubParameter Constructor

    public static class Builder{
        String name;
        String value;

        public Builder name(String name) {
            this.name = name;
            return this;
        } // 'name' constructor

        public Builder value(String value) {
            this.value = value;
            return this;
        } // 'value' constructor

        public SubParameter build(){
            return new SubParameter(this);
        }
    } // Builder class: as SubParameter's Constructor

    public static Builder builder(){
        return new Builder();
    }

    /* Methods */
    public String getName() { return this.name; }
    public String getValue() { return this.value; }

} // SubParameter Class

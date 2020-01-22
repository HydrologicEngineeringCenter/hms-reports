package mil.army.usace.hec.hms.reports;

public class Element {
    /* Class Variables */
    private final String name;
    private final ElementInput elementInput;
    private final ElementResults elementResults;

    /* Constructors */
    private Element(Builder builder) {
        this.name = builder.name;
        this.elementInput = builder.elementInput;
        this.elementResults = builder.elementResults;
    } // Element Constructor

    public static class Builder {
        String name;
        private ElementInput elementInput;
        private ElementResults elementResults;

        public Builder name(String name) {
            this.name = name;
            return this;
        } // 'name' constructor

        public Builder elementInput(ElementInput elementInput) {
            this.elementInput = elementInput;
            return this;
        } // 'elementInput' constructor

        public Builder elementResults(ElementResults elementResults) {
            this.elementResults = elementResults;
            return this;
        } // 'elementResults' constructor

        public Element build(){
            return new Element(this);
        }
    } // Builder class() as Element's Constructor

    public static Builder builder() { return new Builder(); }

    /* Methods */
    String getName() { return this.name; }
    ElementInput getElementInput() { return this.elementInput; }
    ElementResults getElementResults() { return this.elementResults; }

} // Element Class

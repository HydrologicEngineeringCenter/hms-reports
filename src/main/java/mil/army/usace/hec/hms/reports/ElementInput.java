package mil.army.usace.hec.hms.reports;

import java.util.List;

public class ElementInput {
    /* Class Variables */
    private final String name; // Ex: KettleRv_S040
    private final String elementType; // Ex: Subbasin, Reach, Sink, etc...
    private final List<Process> processes; // Ex: Canopy, Loss Rate, etc...

    /* Constructors */
    private ElementInput(Builder builder){
        this.name = builder.name;
        this.elementType = builder.elementType;
        this.processes = builder.processes;
    } // ElementInput Constructor

    public static class Builder{
        String name;
        String elementType;
        List<Process> processes;

        public Builder name(String name){
            this.name = name;
            return this;
        } // 'name' constructor

        public Builder elementType(String elementType) {
            this.elementType = elementType;
            return this;
        } // 'elementType' constructor

        public Builder processes(List<Process> processes) {
            this.processes = processes;
            return this;
        } // 'processes' constructor

        public ElementInput build(){ return new ElementInput(this); }
    } // Builder class: as ElementInput's Constructor

    public static Builder builder(){ return new Builder(); }

    /* Methods */
    public String getName() { return this.name; }
    public String getElementType() { return this.elementType; }
    public List<Process> getProcesses(){ return this.processes; }

} // ElementInput Class

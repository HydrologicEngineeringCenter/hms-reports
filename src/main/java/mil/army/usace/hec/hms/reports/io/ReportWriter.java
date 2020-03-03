package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class ReportWriter {
    Path pathToDestination;
    List<Element> elements;
    List<String> chosenPlots;

    ReportWriter(Builder builder){
        this.pathToDestination = builder.pathToDestination;
        this.elements = builder.elements;
        this.chosenPlots = builder.chosenPlots;
    }

    public static class Builder{
        private Path pathToDestination;
        private List<Element> elements;
        private List<String> chosenPlots;

        public Builder pathToDestination(final String pathToDestinaton){
            this.pathToDestination = Paths.get(pathToDestinaton);
            return this;
        }

        public Builder elements(final List<Element> elements){
            this.elements = elements;
            return this;
        }

        public Builder chosenPlots(final List<String> chosenPlots){
            this.chosenPlots = chosenPlots;
            return this;
        }

        public ReportWriter build(){
            if (pathToDestination.toString().matches(".*.html")){
                return new HmsReportWriter(this);
            } else {
                throw new IllegalArgumentException("File type not supported");
            }
        }
    }

    public static Builder builder() { return new Builder(); }

    public abstract void write();
}

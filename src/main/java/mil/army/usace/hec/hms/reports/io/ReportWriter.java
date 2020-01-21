package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

abstract class ReportWriter {
    private Path pathToDestination;
    private List<Element> elements;

    ReportWriter(Builder builder){
        pathToDestination = builder.pathToDestination;
        elements = builder.elements;
    }

    public static class Builder{
        private Path pathToDestination;
        private List<Element> elements;

        public Builder pathToDestination(final String pathToDestinaton){
            this.pathToDestination = Paths.get(pathToDestinaton);
            return this;
        }

        public Builder elements(final List<Element> elements){
            this.elements = elements;
            return this;
        }

        public ReportWriter build(){
            if (pathToDestination.toString().matches(".*//.html")){
                return new HmsReportWriter(this);
            } else {
                throw new IllegalArgumentException("File type not supported");
            }
        }
    }


    public abstract void write();
}

package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class ReportWriter {
    Path pathToInput;
    Path pathToResult;
    Path pathToDestination;
    List<String> chosenPlots;

    ReportWriter(Builder builder){
        this.pathToInput = builder.pathToInput;
        this.pathToResult = builder.pathToResult;
        this.pathToDestination = builder.pathToDestination;
        this.chosenPlots = builder.chosenPlots;
    }

    public static class Builder{
        private Path pathToInput;
        private Path pathToResult;
        private Path pathToDestination;
        private List<String> chosenPlots;

        public Builder pathToInput(final String pathToInput) {
            this.pathToInput = Paths.get(pathToInput);
            return this;
        }

        public Builder pathToResult(final String pathToResult) {
            this.pathToResult = Paths.get(pathToResult);
            return this;
        }

        public Builder pathToDestination(final String pathToDestinaton){
            this.pathToDestination = Paths.get(pathToDestinaton);
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

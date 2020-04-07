package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class ReportWriter {
    Path pathToInput;
    Path pathToResult;
    Path pathToDestination;
    Path projectDirectory;
    List<SummaryChoice> reportSummaryChoice;
    List<String> chosenPlots;

    public enum SummaryChoice {GLOBAL_SUMMARY, PARAMETER_SUMMARY}

    ReportWriter(Builder builder){
        this.pathToInput = builder.pathToInput;
        this.pathToResult = builder.pathToResult;
        this.pathToDestination = builder.pathToDestination;
        this.projectDirectory = builder.projectDirectory;
        this.reportSummaryChoice = builder.reportSummaryChoice;
        this.chosenPlots = builder.chosenPlots;
    }

    public static class Builder{
        private Path pathToInput;
        private Path pathToResult;
        private Path pathToDestination;
        private Path projectDirectory;
        private List<SummaryChoice> reportSummaryChoice;
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

        public Builder projectDirectory(final String projectDirectory) {
            this.projectDirectory = Paths.get(projectDirectory);
            return this;
        }

        public Builder reportSummaryChoice(final List<SummaryChoice> reportSummaryChoice) {
            this.reportSummaryChoice  = reportSummaryChoice;
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

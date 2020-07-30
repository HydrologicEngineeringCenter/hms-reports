package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.DisplayRange;
import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.enums.ReportWriterType;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.enums.StatisticsType;
import mil.army.usace.hec.hms.reports.enums.SummaryChoice;
import mil.army.usace.hec.hms.reports.io.standard.StandardReportWriter;
import mil.army.usace.hec.hms.reports.io.statistics.SummaryStatisticsReportWriter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public abstract class ReportWriter {
    protected Path pathToInput;
    protected Path pathToResult;
    protected Path pathToDestination;
    protected Path projectDirectory;
    protected List<SummaryChoice> reportSummaryChoice;
    protected Map<String, List<String>> globalParameterChoices;
    protected List<String> chosenPlots;
    protected Map<String, List<String>> elementParameterizationChoice;
    protected Map<StatisticsType, List<DisplayRange>> displayRangeMap;
    protected SimulationType simulationType;

    protected PropertyChangeSupport support;

    protected ReportWriter(Builder builder){
        this.pathToInput = builder.pathToInput;
        this.pathToResult = builder.pathToResult;
        this.pathToDestination = builder.pathToDestination;
        this.projectDirectory = builder.projectDirectory;
        this.reportSummaryChoice = builder.reportSummaryChoice;
        this.chosenPlots = builder.chosenPlots;
        this.globalParameterChoices = builder.globalParameterChoices;
        this.elementParameterizationChoice = builder.elementParameterizationChoice;
        this.displayRangeMap = builder.displayRangeMap;
        this.simulationType = builder.simulationType;
    }

    public static class Builder{
        private Path pathToInput;
        private Path pathToResult;
        private Path pathToDestination;
        private Path projectDirectory;
        private List<SummaryChoice> reportSummaryChoice;
        private List<String> chosenPlots;
        private Map<String, List<String>> globalParameterChoices;
        private Map<String, List<String>> elementParameterizationChoice;
        private ReportWriterType reportWriterType;
        private Map<StatisticsType, List<DisplayRange>> displayRangeMap;
        private SimulationType simulationType;

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

        public Builder globalParameterChoices(final Map<String, List<String>> globalParameterChoices) {
            this.globalParameterChoices = globalParameterChoices;
            return this;
        } // globalParameterChoices()

        public Builder elementParameterizationChoice(final Map<String, List<String>> elementParameterizationChoice) {
            this.elementParameterizationChoice = elementParameterizationChoice;
            return this;
        } // elementParameterizationChoice()

        public Builder reportWriterType(final ReportWriterType reportWriterType) {
            this.reportWriterType = reportWriterType;
            return this;
        } // reportWriterType()

        public Builder displayRangeMap(final  Map<StatisticsType, List<DisplayRange>> displayRangeMap) {
            this.displayRangeMap = displayRangeMap;
            return this;
        } // displayRangeMap()

        public Builder simulationType(final SimulationType simulationType) {
            this.simulationType = simulationType;
            return this;
        } // simulationType()

        public ReportWriter build(){
            if(reportWriterType == ReportWriterType.STANDARD_REPORT) {
                return new StandardReportWriter(this);
            } // If: Standard Report
            else if(reportWriterType == ReportWriterType.SUMMARY_STATISTICS_REPORT) {
                return new SummaryStatisticsReportWriter(this);
            } // Else If: Summary Statistics Report
            else {
                throw new IllegalArgumentException("File type not supported");
            } // Else: Non-supported report
        } // build()

    } // Builder Class

    public static Builder builder() { return new Builder(); }

    public abstract List<Element> write();

    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl){
        support.removePropertyChangeListener(pcl);
    }

} // Report Writer Class

package mil.army.usace.hec.hms.reports.io.parser;

import hec.heclib.util.HecTime;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.enums.SimulationType;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public abstract class BasinResultsParser {
    Path pathToBasinResultsFile;
    Path pathToProjectDirectory;
    SimulationType simulationType;
    PropertyChangeSupport support;

    BasinResultsParser(Builder builder){
        this.pathToBasinResultsFile = builder.pathToBasinResultsFile;
        this.pathToProjectDirectory = builder.pathToProjectDirectory;
        this.simulationType = builder.simulationType;
        this.support = new PropertyChangeSupport(this);
    }

    public static class Builder {
        private Path pathToBasinResultsFile;
        private Path pathToProjectDirectory;
        private SimulationType simulationType;

        public Builder pathToBasinResultsFile(final String pathToBasinFile){
            this.pathToBasinResultsFile = Paths.get(pathToBasinFile);
            return this;
        }

        public Builder pathToProjectDirectory(final String pathToProjectDirectory){
            this.pathToProjectDirectory = Paths.get(pathToProjectDirectory);
            return this;
        }

        public Builder simulationType(final SimulationType simulationType) {
            this.simulationType = simulationType;
            return this;
        }

        public BasinResultsParser build(){
            if (pathToBasinResultsFile.toString().matches(".*.results")){
                if(simulationType == SimulationType.MONTE_CARLO) {
                    return new MonteCarloResultsParser(this);
                } // If: MonteCarlo Type
                else if(simulationType == SimulationType.DEPTH_AREA) {
                    return new DepthAreaResultsParser(this);
                } // Else if: DepthArea Type
                else {
                    return new XmlBasinResultsParser(this);
                } // Else: Run/Forecast/Optimization Types
            } // If: is '.results' file
            else {
                throw new IllegalArgumentException("Not a valid file type.");
            } // Else: throw error
        }
    }

    public static BasinResultsParser.Builder builder(){
        return new BasinResultsParser.Builder();
    }

    public abstract Map<String, ElementResults> getElementResults();

    public abstract String getSimulationName();

    public abstract List<String> getAvailablePlots();

    public abstract HecTime getStartTime();

    public abstract HecTime getEndTime();

    public abstract ZonedDateTime getLastComputedTime();

    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl){
        support.removePropertyChangeListener(pcl);
    }
}



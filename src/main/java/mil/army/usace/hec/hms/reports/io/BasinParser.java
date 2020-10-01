package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.io.parser.BasinInputParser;
import mil.army.usace.hec.hms.reports.io.parser.BasinResultsParser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BasinParser {
    private final Path pathToBasinInputFile;
    private final Path pathToBasinResultsFile;
    private final Path pathToProjectDirectory;
    private final SimulationType simulationType;
    private PropertyChangeSupport support;

    private BasinParser(Builder builder){
        this.pathToBasinInputFile = builder.pathToBasinInputFile;
        this.pathToBasinResultsFile = builder.pathToBasinResultsFile;
        this.pathToProjectDirectory = builder.pathToProjectDirectory;
        this.simulationType = builder.simulationType;
        this.support = new PropertyChangeSupport(this);
    }

    public static class Builder {
        Path pathToBasinInputFile;
        Path pathToBasinResultsFile;
        Path pathToProjectDirectory;
        SimulationType simulationType;

        public Builder pathToBasinInputFile(final String pathToBasinFile){
            this.pathToBasinInputFile = Paths.get(pathToBasinFile);
            return this;
        }

        public Builder pathToBasinResultsFile(final String pathToBasinResultsFile){
            this.pathToBasinResultsFile = Paths.get(pathToBasinResultsFile);
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

        public BasinParser build(){
            return new BasinParser(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean outdatedSimulation() {
        BasinInputParser inputParser = BasinInputParser.builder()
                .pathToBasinInputFile(this.pathToBasinInputFile.toString())
                .build();
        BasinResultsParser resultsParser = BasinResultsParser.builder()
                .pathToBasinResultsFile(this.pathToBasinResultsFile.toAbsolutePath().toString())
                .pathToProjectDirectory(this.pathToProjectDirectory.toAbsolutePath().toString())
                .simulationType(this.simulationType)
                .build();

        ZonedDateTime basinModifiedTime = inputParser.getLastModifiedTime();
        ZonedDateTime executionTime = resultsParser.getLastComputedTime();
        int compareValue = basinModifiedTime.toInstant().compareTo(executionTime.toInstant());

        return compareValue >= 0;
    } // outdatedSimulation()

    public List<Element> getElements() {
        List<Element> elementList = new ArrayList<>();
        Double inputParserPercent = 0.15;
        Double resultsParserPercent = 0.85;

        BasinInputParser inputParser = BasinInputParser.builder()
                .pathToBasinInputFile(this.pathToBasinInputFile.toString())
                .build();
        inputParser.addPropertyChangeListener(evt -> {
            if((evt.getSource() instanceof BasinInputParser) && (evt.getPropertyName().equals("Progress"))) {
                if(evt.getNewValue() instanceof Double) {
                    Double progressValue = (Double) evt.getNewValue() * inputParserPercent;
                    support.firePropertyChange("Progress", "", progressValue);
                }
            }
        });

        BasinResultsParser resultsParser = BasinResultsParser.builder()
                .pathToBasinResultsFile(this.pathToBasinResultsFile.toAbsolutePath().toString())
                .pathToProjectDirectory(this.pathToProjectDirectory.toAbsolutePath().toString())
                .simulationType(this.simulationType)
                .build();
        resultsParser.addPropertyChangeListener(evt -> {
            if((evt.getSource() instanceof BasinResultsParser) && (evt.getPropertyName().equals("Progress"))) {
                if(evt.getNewValue() instanceof Double) {
                    Double progressValue = inputParserPercent + (Double) evt.getNewValue() * resultsParserPercent;
                    support.firePropertyChange("Progress", "", progressValue);
                }
            }
        });

        List<ElementInput> inputs = inputParser.getElementInput();
        Map<String, ElementResults> results = resultsParser.getElementResults();

        for(ElementInput input : inputs) {
            String elementName = input.getName();
            Element element = Element.builder()
                    .name(elementName)
                    .elementInput(input)
                    .elementResults(results.get(elementName))
                    .build();
            elementList.add(element);
        } // Loop: to get a List of Elements

        return elementList;
    } // getElements()

    public Map<String, String> getSimulationData() {
        Map<String, String> simulationDataMap = new LinkedHashMap<>();

        BasinResultsParser resultsParser = BasinResultsParser.builder()
                .pathToBasinResultsFile(this.pathToBasinResultsFile.toAbsolutePath().toString())
                .pathToProjectDirectory(this.pathToProjectDirectory.toAbsolutePath().toString())
                .simulationType(this.simulationType)
                .build();

        String simulationName = resultsParser.getSimulationName();
        String startTime = resultsParser.getStartTime().toString();
        String endTime = resultsParser.getEndTime().toString();
        DateTimeFormatter executionFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
        String executionTime = resultsParser.getLastComputedTime().format(executionFormatter);

        simulationDataMap.put("name", simulationName);
        simulationDataMap.put("start", startTime);
        simulationDataMap.put("end", endTime);
        simulationDataMap.put("execution", executionTime);

        return simulationDataMap;
    } // getSimulationName()

    public String getHmsVersion() {
        BasinInputParser inputParser = BasinInputParser.builder()
                .pathToBasinInputFile(this.pathToBasinInputFile.toString())
                .build();

        return inputParser.getHmsVersion();
    } // getHmsVersion()

    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl){
        support.removePropertyChangeListener(pcl);
    }
} // BasinParser class



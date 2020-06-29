package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.ElementResults;
import mil.army.usace.hec.hms.reports.enums.SimulationType;
import mil.army.usace.hec.hms.reports.io.parser.BasinInputParser;
import mil.army.usace.hec.hms.reports.io.parser.BasinResultsParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasinParser {
    private final Path pathToBasinInputFile;
    private final Path pathToBasinResultsFile;
    private final Path pathToProjectDirectory;
    private final SimulationType simulationType;

    private BasinParser(Builder builder){
        this.pathToBasinInputFile = builder.pathToBasinInputFile;
        this.pathToBasinResultsFile = builder.pathToBasinResultsFile;
        this.pathToProjectDirectory = builder.pathToProjectDirectory;
        this.simulationType = builder.simulationType;
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

    public List<Element> getElements() {
        List<Element> elementList = new ArrayList<>();

        BasinInputParser inputParser = BasinInputParser.builder()
                .pathToBasinInputFile(this.pathToBasinInputFile.toString())
                .build();
        BasinResultsParser resultsParser = BasinResultsParser.builder()
                .pathToBasinResultsFile(this.pathToBasinResultsFile.toAbsolutePath().toString())
                .pathToProjectDirectory(this.pathToProjectDirectory.toAbsolutePath().toString())
                .simulationType(this.simulationType)
                .build();

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

} // BasinParser class



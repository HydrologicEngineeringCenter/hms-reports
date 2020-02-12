package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;
import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.ElementResults;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasinParser {
    private final Path pathToBasinInputFile;
    private final Path pathToBasinResultsFile;

    private BasinParser(Builder builder){
        this.pathToBasinInputFile = builder.pathToBasinInputFile;
        this.pathToBasinResultsFile = builder.pathToBasinResultsFile;
    }

    public static class Builder {
        Path pathToBasinInputFile;
        Path pathToBasinResultsFile;

        public Builder pathToBasinInputFile(final String pathToBasinFile){
            this.pathToBasinInputFile = Paths.get(pathToBasinFile);
            return this;
        }

        public Builder pathToBasinResultsFile(final String pathToBasinResultsFile){
            this.pathToBasinResultsFile = Paths.get(pathToBasinResultsFile);
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
                .pathToBasinResultsFile(this.pathToBasinResultsFile.toString())
                .build();

        List<ElementInput> inputs = inputParser.getElementInput();
        Map<String, ElementResults> results = resultsParser.getElementResults();

        if(inputs.size() != results.size()) {
            System.out.println("Error: Number of inputs and results do not match!");
            return null;
        } // Check if # of inputs and results matches

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



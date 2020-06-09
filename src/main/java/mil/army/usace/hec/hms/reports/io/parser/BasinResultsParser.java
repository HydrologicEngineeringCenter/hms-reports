package mil.army.usace.hec.hms.reports.io.parser;

import mil.army.usace.hec.hms.reports.ElementResults;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public abstract class BasinResultsParser {
    Path pathToBasinResultsFile;
    Path pathToProjectDirectory;

    BasinResultsParser(Builder builder){
        this.pathToBasinResultsFile = builder.pathToBasinResultsFile;
        this.pathToProjectDirectory = builder.pathToProjectDirectory;
    }

    public static class Builder {
        private Path pathToBasinResultsFile;
        private Path pathToProjectDirectory;

        public Builder pathToBasinResultsFile(final String pathToBasinFile){
            this.pathToBasinResultsFile = Paths.get(pathToBasinFile);
            return this;
        }

        public Builder pathToProjectDirectory(final String pathToProjectDirectory){
            this.pathToProjectDirectory = Paths.get(pathToProjectDirectory);
            return this;
        }

        public BasinResultsParser build(){
            if (pathToBasinResultsFile.toString().matches(".*.results")){
                return new XmlBasinResultsParser(this);
            } else {
                throw new IllegalArgumentException("Not a valid file type.");
            }

        }
    }

    public static BasinResultsParser.Builder builder(){
        return new BasinResultsParser.Builder();
    }

    public abstract Map<String, ElementResults> getElementResults();
}



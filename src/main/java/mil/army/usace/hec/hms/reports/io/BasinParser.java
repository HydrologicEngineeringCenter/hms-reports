package mil.army.usace.hec.hms.reports.io;

import mil.army.usace.hec.hms.reports.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BasinParser {
    final Path pathToBasinInputFile;
    final Path pathToBasinResultsFile;

    BasinParser(Builder builder){
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
            this.pathToBasinInputFile = Paths.get(pathToBasinResultsFile);
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
        //TODO
        return null;
    }
}



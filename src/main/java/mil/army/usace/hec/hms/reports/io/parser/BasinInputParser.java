package mil.army.usace.hec.hms.reports.io.parser;

import mil.army.usace.hec.hms.reports.ElementInput;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class BasinInputParser {
    Path pathToBasinInputFile;

    BasinInputParser(Builder builder){
        this.pathToBasinInputFile = builder.pathToBasinInputFile;
    }

    public static class Builder {
        private Path pathToBasinInputFile;

        public Builder pathToBasinInputFile(final String pathToBasinFile){
            this.pathToBasinInputFile = Paths.get(pathToBasinFile);
            return this;
        }

        public BasinInputParser build(){
            if (pathToBasinInputFile.toString().matches(".*basin.json")){
                return new JsonBasinInputParser(this);
            } else {
                throw new IllegalArgumentException("Not a valid file type.");
            }

        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public abstract List<ElementInput> getElementInput();
}

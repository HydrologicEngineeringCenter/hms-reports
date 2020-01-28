package mil.army.usace.hec.hms.reports.io;

  import mil.army.usace.hec.hms.reports.ElementInput;
import mil.army.usace.hec.hms.reports.ElementResults;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class BasinResultsParser {
    Path pathToBasinResultsFile;

    BasinResultsParser(Builder builder){
        this.pathToBasinResultsFile = builder.pathToBasinResultsFile;
    }

    public static class Builder {
        private Path pathToBasinResultsFile;

        public Builder pathToBasinResultsFile(final String pathToBasinFile){
            this.pathToBasinResultsFile = Paths.get(pathToBasinFile);
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

    public abstract List<ElementResults> getElementResults();
}



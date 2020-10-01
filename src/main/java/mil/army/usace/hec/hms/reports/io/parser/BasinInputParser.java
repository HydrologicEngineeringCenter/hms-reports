package mil.army.usace.hec.hms.reports.io.parser;

import mil.army.usace.hec.hms.reports.ElementInput;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;

public abstract class BasinInputParser {
    Path pathToBasinInputFile;
    PropertyChangeSupport support;

    BasinInputParser(Builder builder){
        this.pathToBasinInputFile = builder.pathToBasinInputFile;
        support = new PropertyChangeSupport(this);
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
            }
            else if (pathToBasinInputFile.toString().matches(".*basin")){
                return new AsciiBasinInputParser(this);
            }
            else {
                throw new IllegalArgumentException("Not a valid file type.");
            }

        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public abstract List<ElementInput> getElementInput();
    public abstract ZonedDateTime getLastModifiedTime();
    public abstract String getHmsVersion();

    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl){
        support.removePropertyChangeListener(pcl);
    }
}

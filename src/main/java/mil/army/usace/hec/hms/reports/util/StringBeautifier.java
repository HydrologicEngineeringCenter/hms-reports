package mil.army.usace.hec.hms.reports.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StringBeautifier {
    private StringBeautifier() {}

    public static String beautifyString (String name) {
        String result = "";

        String camelCasePattern = "[a-z]([A-Z0-9]*[a-z][a-z0-9]*[A-Z]|[a-z0-9]*[A-Z][A-Z0-9]*[a-z])[A-Za-z0-9]*"; // Ex: camelCase, camelCaseATest3
        String pascalCasePattern = "[A-Z]([A-Z0-9]*[a-z][a-z0-9]*[A-Z]|[a-z0-9]*[A-Z][A-Z0-9]*[a-z])[A-Za-z0-9]*"; // Ex: PascalCase, PascalCaseATest
        String upperUnderscorePattern = "([A-Z]+[_])+[A-Z]+"; // Ex: UNDER_SCORE, UNDER_SCORE_TEST

        if(isNumeric(name)) {
            result =  beautifyNumber(name);
        } // If: Numeric
        else if(name.matches(camelCasePattern) || name.toLowerCase().equals(name)) {
            result = beautifyCamelCase(name);
        } // If: camelCase or lowerall
        else if(name.matches(upperUnderscorePattern) || name.toUpperCase().equals(name)) {
            result = beautifyUpperUnderscore(name);
        } // Else if: UPPER_UNDERSCORE or UPPERALL
        else if(name.matches(pascalCasePattern)) {
            result = beautifyPascalCase(name);
        } // Else if: PascalCase
        else {
            result = name;
        } // Else: Doesn't match any special

        return result;
    } // beautifyString
    private static String beautifyCamelCase (String name) {
        // Capitalizing the first letter. Turn into PascalCase
        name = name.substring(0,1).toUpperCase() + name.substring(1);
        return beautifyPascalCase(name);
    } // beautifyCamelCase()
    private static String beautifyPascalCase (String name) {
        StringBuilder result = new StringBuilder();
        char[] charArray = name.toCharArray();
        List<Integer> capitalIndices = new ArrayList<>();

        int prevIsDigit = 0; // 1 if previous char is a digit
        for(int i = 0; i < charArray.length; i++) {
            if(Character.isUpperCase(charArray[i])) {
                capitalIndices.add(i);
                prevIsDigit = 0;
            } // If: character is an uppercase
            else if(Character.isDigit(charArray[i]) && prevIsDigit == 0) {
                capitalIndices.add(i);
                prevIsDigit = 1;
            } // Else if: character is a digit, and previous char is not a digit
        }  // Getting indices of capital characters and Numbers

        for(int i = 0; i < capitalIndices.size() - 1; i++){
            String subString = name.substring(capitalIndices.get(i), capitalIndices.get(i + 1));
            result.append(subString);
            result.append(" ");
        } // Appending substrings to result

        String lastSubString = name.substring(capitalIndices.get(capitalIndices.size() - 1));
        result.append(lastSubString);

        return result.toString();
    } // beautifyPascalCase()
    private static String beautifyUpperUnderscore(String name) {
        StringBuilder result = new StringBuilder();
        name = name.toLowerCase();

        String[] splitString = name.split("_");
        for(String token : splitString) {
            token = token.substring(0, 1).toUpperCase() + token.substring(1) + " ";
            result.append(token);
        } // Capitalizing first characters

        return result.toString();
    } // beautifyUpperUnderscore()
    private static String beautifyNumber(String name) {
        Double number = Double.parseDouble(name);
        String roundedNumber = new DecimalFormat("#.##").format(number);
        return roundedNumber;
    } // beautifyNumber()
    private static boolean isNumeric(String name) {
        boolean isNumeric = true;
        try { Double num = Double.parseDouble(name); }
        catch (NumberFormatException e) { isNumeric = false; }
        return isNumeric;
    } // isDouble()
    public static String getPlotDivName(String elementName, String plotName) {

        String plotDivName = elementName.toLowerCase() + "_";
        String reformatName = plotName.toLowerCase();

        return (plotDivName + reformatName).replace(' ', '_');
    } // getPlotDivName

} // StringBeautifier class

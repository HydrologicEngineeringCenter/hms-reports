package mil.army.usace.hec.hms.reports.util;

import java.util.ArrayList;
import java.util.List;

public class StringBeautifier {
    private StringBeautifier() {}

    public static String beautifyString (String name) {
        String result = "";

        String camelCasePattern = "[a-z]+[A-Z]+[a-zA-Z]+"; // Ex: camelCase, camelCaseATest
        String pascalCasePattern = "[A-Z]+[a-zA-Z]+"; // Ex: PascalCase, PascalCaseATest
        String upperUnderscorePattern = "([A-Z]+[_])+[A-Z]+"; // Ex: UNDER_SCORE, UNDER_SCORE_TEST

        if(name.matches(camelCasePattern) || name.toLowerCase().equals(name)) {
            result = beautifyCamelCase(name);
        } // If: camelCase or lowerall
        else if(name.matches(upperUnderscorePattern) || name.toUpperCase().equals(name)) {
            result = beautifyUpperUnderscore(name);
        } // Else if: UPPER_UNDERSCORE or UPPERALL
        else if(name.matches(pascalCasePattern)) {
            result = beautifyPascalCase(name);
        } // Else if: PascalCase

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

        System.out.println(result.toString());

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

        System.out.println(result.toString());

        return result.toString();
    } // beautifyUpperUnderscore()

} // StringBeautifier class

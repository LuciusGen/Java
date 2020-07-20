package com.company;




public class Rle {
    private static void AddToRleCode(StringBuilder sb, final int currentCharCount, final char currentChar){
        sb.append(currentChar);

        if (currentCharCount > 1) {
            sb.append(currentCharCount);
        }
    }

    private static String getRLE(final String str) {
        final String emptyStr = "";

        if (str == null || str.equals(emptyStr)) {
            return str;
        }


        char currentChar = str.charAt(0);
        int currentCharCount = 1;

        final int maxNumber = 9;

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c != currentChar || currentCharCount == maxNumber) {
                AddToRleCode(sb, currentCharCount, currentChar);

                currentCharCount = 1;
                currentChar = c;
            } else {
                currentCharCount++;
            }
        }

        AddToRleCode(sb, currentCharCount, currentChar);

        return sb.toString();
    }

    private static String Decode(final String str) {
        final String emptyStr = "";

        if (str == null || str.equals(emptyStr)) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        char CurrentChar = 0;

        for(int i = 0; i < str.length(); i++) {
            if(Character.isDigit(str.charAt(i))) {
                for(int j = 0; j < Character.getNumericValue(str.charAt(i)); j++) {
                    sb.append(CurrentChar);
                }
            } else {
                CurrentChar = str.charAt(i);
                sb.append(CurrentChar);
            }
        }

        return sb.toString();
    }

    public static String RleSolve(final String Str0, final String Mode){
        if(Mode.equals(Config.Modes.Code.toString())){
            return getRLE(Str0);
        } else {
            return Decode(Str0);
        }
    }
}

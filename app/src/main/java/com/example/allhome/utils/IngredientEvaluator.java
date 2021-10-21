package com.example.allhome.utils;

import android.util.Log;

import com.example.allhome.data.entities.IngredientEntity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngredientEvaluator {
    final static String[] UNITS ={
            "teaspoon","teaspoons","t","tsp","tablespoon","tablespoons","tbl","tbsp","oz","gill","cup","cups","pint","p","pt","quart","q","qt",
            "gallon","g","gal","ml","milliliter","millilitre","cc","l","liter","litre","dl","deciliter","decilitre",
            "pound","pounds","lb","ounce","ounces","oz","mg","milligram","milligramme","g","gram","grams","gramme","kg","kilo","kilogram","kilogramme",
            "mm","millimeter","millimetre","cm","centimeter","centimetre","m","meter","metre","inch","in",
            "pcs","pc","piece","pieces"
    };
    static String TAG = "IngredientEvaluator";
    public static String getQuantity(String ingredient) {
        /**
         * match whole number,decimal, whole number with fraction and fraction
         */
        final String regex = "^(\\d+? \\d+\\/\\d+)|\\d+\\/\\d+ |^([0-9]+\\.?[0-9]*\\.[0-9]+)|^([0-9]+)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(ingredient);

        if (matcher.find()) {
            return matcher.group(0).trim();

        }
        return "";
    }
    public static String getUnit(String quantity,String ingredient){

        String ingrendientWithoutQuantity = ingredient.replaceFirst(quantity,"").trim();
        final String regex = "^\\w*";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(ingrendientWithoutQuantity);

        if (matcher.find()) {
            List<String> units = new ArrayList<>(Arrays.asList(UNITS));
            String unit = matcher.group(0).trim().toLowerCase();

            if(units.contains(unit)){
                return matcher.group(0);
            }


        }

        return "";

    }
    public static IngredientEntity evaluate(String ingredientString){

        IngredientEntity ingredientEntity = new IngredientEntity("","","",0,0,"","");




        String[] splitedIngredient = ingredientString.trim().split("\\s+");

        if(splitedIngredient.length <= 1){

            ingredientEntity.setName(ingredientString);
            return ingredientEntity;
        }

        if(splitedIngredient.length == 2){
            /**
             * check first value
             */
            String possibleQuantityString = splitedIngredient[0];
            if(isValidNumberForRecipeQuantity(possibleQuantityString)){

                ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));
            }else if(isValidFractionForRecipeQuantity(possibleQuantityString)){

                ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));
            }else{
                ingredientEntity.setName(ingredientString);
            }

        }else if(splitedIngredient.length > 2){

            String possibleQuantityString = splitedIngredient[0];
            String possibleOtherQuantityOrUnitString = splitedIngredient[1];
            String possibleUnit = splitedIngredient[2];
            boolean isFirstValueValidQuantity = isValidFractionForRecipeQuantity(possibleQuantityString) || isValidNumberForRecipeQuantity(possibleQuantityString);
            boolean isFirstValueValidNumber =  isValidNumberForRecipeQuantity(possibleQuantityString);
            boolean doSecondValueContainString = doValueContainString(possibleOtherQuantityOrUnitString);

            if(isFirstValueValidQuantity){

                if(isFirstValueValidNumber){

                }else if(isValidFractionForRecipeQuantity(possibleQuantityString)){

                }


                Log.e(TAG,"FIRST VALUE IS QUANTITY (Quantity)");
            }else{

                ingredientEntity.setName(ingredientString);

                Log.e(TAG,ingredientEntity.toString());

                return ingredientEntity;
            }

            if(doSecondValueContainString){
                //possible unit or not
                if(Arrays.asList(UNITS).contains(possibleOtherQuantityOrUnitString)){
                    // found unit

                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(2,splitedIngredient.length)));
                }else{
                    // no unit found set remaining value as ingredient name
                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));

                }

                Log.e(TAG,"Contain string. It unit");
            }else{

                Log.e(TAG,"===============");
                boolean possibleOtherQuantityOrUnitStringIsFraction = isValidFractionForRecipeQuantity(possibleOtherQuantityOrUnitString);
                boolean possibleOtherQuantityOrUnitStringIsUnit = Arrays.asList(UNITS).contains(possibleOtherQuantityOrUnitString);
                boolean possibleUnitIsUnit =  Arrays.asList(UNITS).contains(possibleUnit);

                if(possibleOtherQuantityOrUnitStringIsFraction){
                    double additionalQuantity = convertFractionToDecimal(possibleOtherQuantityOrUnitString);

                }
                if(!possibleOtherQuantityOrUnitStringIsFraction && possibleOtherQuantityOrUnitStringIsUnit){

                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));
                }
                if(!possibleOtherQuantityOrUnitStringIsFraction && !possibleOtherQuantityOrUnitStringIsUnit){

                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));
                }
                if(possibleOtherQuantityOrUnitStringIsFraction && possibleUnitIsUnit){

                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(3,splitedIngredient.length)));
                }

                if(!possibleOtherQuantityOrUnitStringIsFraction && !possibleOtherQuantityOrUnitStringIsUnit && !possibleUnitIsUnit){
                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));
                }

                if(possibleOtherQuantityOrUnitStringIsFraction && !possibleOtherQuantityOrUnitStringIsUnit && !possibleUnitIsUnit){
                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(2,splitedIngredient.length)));
                }


            }

        }

        return ingredientEntity;
    }

    /**
     * it accept whole and decimal number
     * @param stringToCheck
     * @return
     */
    public static boolean isValidNumberForRecipeQuantity(String stringToCheck){
        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]*");
        Matcher matcher = pattern.matcher(stringToCheck);
        return matcher.matches();
    }
    public static boolean isValidFractionForRecipeQuantity(String stringToCheck){
        Pattern pattern = Pattern.compile("[1-9]{1,2}/[1-9]{1,2}");
        Matcher matcher = pattern.matcher(stringToCheck);
        return matcher.matches();
    }
    public static boolean doValueContainString(String stringToCheck){
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        Matcher matcher = pattern.matcher(stringToCheck);
        return matcher.find();
    }
    public static String listToString(List<String> listString){

        Log.e(TAG," listSTring "+listString.toString());
        String newString = "";
        for (String string: listString) {
            Log.e(TAG,string);
            newString += string+" ";
        }
        return newString.trim();

    }
    public static double convertFractionToDecimal(String fractionString){

        String[] splitteString = fractionString.split("/");

        return Double.parseDouble(splitteString[0]) / Double.parseDouble(splitteString[1]);

    }
    public static void regexCheckher(String theRegex,String stringToCheck){

        Pattern pattern = Pattern.compile(theRegex);
        Matcher matcher = pattern.matcher(stringToCheck);
        while(matcher.find()){
            if(matcher.group().length() != 0 ){

                Log.e(TAG,matcher.group().trim());
            }

            Log.e(TAG,"start index : "+matcher.start());
            Log.e(TAG,"end  index : "+matcher.end());
        }

    }
}

package com.example.allhome.utils;

import android.util.Log;

import com.example.allhome.data.entities.IngredientEntity;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngredientEvaluator {
    static String TAG = "IngredientEvaluator";
    public static IngredientEntity evaluate(String ingredientString){

        IngredientEntity ingredientEntity = new IngredientEntity("","",0.0,"","",0,0,"","");

        String[] units ={
             "teaspoon","t","tsp","tablespoon","tbl","tbsp","oz","gill","cup","pint","p","pt","quart","q","qt",
             "gallon","g","gal","ml","milliliter","millilitre","cc","l","liter","litre","dl","deciliter","decilitre",
              "pound","lb","ounce","oz","mg","milligram","milligramme","g","gram","gramme","kg","kilo","kilogram","kilogramme",
              "mm","millimeter","millimetre","cm","centimeter","centimetre","m","meter","metre","inch","in",
               "pcs","pc","piece","pieces"
        };


        String[] splitedIngredient = ingredientString.split("\\s+");

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
                ingredientEntity.setQuantity(Double.parseDouble(possibleQuantityString));
                ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));
            }else if(isValidFractionForRecipeQuantity(possibleQuantityString)){
                ingredientEntity.setQuantity(convertFractionToDecimal(possibleQuantityString));
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
                    ingredientEntity.setQuantity(Double.parseDouble(possibleQuantityString));
                }else if(isValidFractionForRecipeQuantity(possibleQuantityString)){
                    ingredientEntity.setQuantity(convertFractionToDecimal(possibleQuantityString));
                }


                Log.e(TAG,"FIRST VALUE IS QUANTITY (Quantity)");
            }else{

                ingredientEntity.setName(ingredientString);

                Log.e(TAG,ingredientEntity.toString());

                return ingredientEntity;
            }

            if(doSecondValueContainString){
                //possible unit or not
                if(Arrays.asList(units).contains(possibleOtherQuantityOrUnitString)){
                    // found unit
                    ingredientEntity.setUnit(possibleOtherQuantityOrUnitString);
                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(2,splitedIngredient.length)));
                }else{
                    // no unit found set remaining value as ingredient name
                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));

                }

                Log.e(TAG,"Contain string. It unit");
            }else{

                boolean possibleOtherQuantityOrUnitStringIsFraction = isValidFractionForRecipeQuantity(possibleOtherQuantityOrUnitString);
                boolean possibleOtherQuantityOrUnitStringIsUnit = Arrays.asList(units).contains(possibleOtherQuantityOrUnitString);
                boolean possibleUnitIsUnit =  Arrays.asList(units).contains(possibleUnit);

                if(possibleOtherQuantityOrUnitStringIsFraction){
                    double additionalQuantity = convertFractionToDecimal(possibleOtherQuantityOrUnitString);
                    ingredientEntity.setQuantity(ingredientEntity.getQuantity() + additionalQuantity);
                }
                if(!possibleOtherQuantityOrUnitStringIsFraction && possibleOtherQuantityOrUnitStringIsUnit){
                    ingredientEntity.setUnit(possibleOtherQuantityOrUnitString);
                    ingredientEntity.setName(listToString(Arrays.asList(splitedIngredient).subList(1,splitedIngredient.length)));
                }
                if(possibleOtherQuantityOrUnitStringIsFraction && possibleUnitIsUnit){
                    ingredientEntity.setUnit(possibleUnit);
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

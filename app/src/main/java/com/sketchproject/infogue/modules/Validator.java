package com.sketchproject.infogue.modules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Sketch Project Studio
 * Created by Angga on 16/04/2016 13.29.
 */
public class Validator {

    public boolean isEmpty(Object value){
        return isEmpty(value, false);
    }

    public boolean isEmpty(Object value, boolean isIgnoreSpace){
        if(value == null){
            return true;
        }
        else if(value instanceof String){
            if(isIgnoreSpace){
                return String.valueOf(value).trim().isEmpty();
            }
            return String.valueOf(value).isEmpty();
        }
        else{
            try
            {
                int result = Integer.parseInt(value.toString());
                return result == 0;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
    }

    public boolean isValidEmail(String email){
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidUrl(String url){
        String urlPattern = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        return url.matches(urlPattern);
    }

    public boolean isAlphaDash(String value){
        return value.matches("^[a-zA-Z0-9-_]*$");
    }

    public boolean isAlphaNumeric(String value){
        return value.matches("^[a-zA-Z0-9]*$");
    }

    public boolean isPersonName(String value){
        return value.matches("^[a-zA-Z '.,]*$");
    }

    public boolean isValidDate(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean isNumeric(Object value){
        return isNumeric(value, false);
    }

    public boolean isNumeric(Object value, boolean isSignedOnly){
        try {
            int result = Integer.parseInt(value.toString());
            if(isSignedOnly && result >= 0){
                return true;
            }
            else{
                return true;
            }
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public boolean minLength(String value, int minValue){
        return minLength(value, minValue, false);
    }

    public boolean minLength(String value, int minValue, boolean ignoreSpace){
        if(ignoreSpace){
            return String.valueOf(value).trim().length() >= minValue;
        }
        return String.valueOf(value).length() >= minValue;
    }

    public boolean maxLength(String value, int maxValue){
        return maxLength(value, maxValue, false);
    }

    public boolean maxLength(String value, int maxValue, boolean ignoreSpace){
        if(ignoreSpace){
            return String.valueOf(value).trim().length() <= maxValue;
        }
        return String.valueOf(value).length() <= maxValue;
    }

    public boolean rangeLength(String value, int minValue, int maxValue){
        return rangeLength(value, minValue, maxValue, false);
    }

    public boolean rangeLength(String value, int minValue, int maxValue, boolean isIgnoreSpace){
        String string= String.valueOf(value);
        if(isIgnoreSpace){
            return string.trim().length() >= minValue && string.trim().length() <= maxValue;
        }
        return string.length() >= minValue && string.length() <= maxValue;
    }

    public boolean minValue(int value, int minValue){
        return value >= minValue;
    }

    public boolean minValue(float value, float minValue){
        return value >= minValue;
    }

    public boolean minValue(double value, double minValue){
        return value >= minValue;
    }

    public boolean maxValue(int value, int maxValue){
        return value >= maxValue;
    }

    public boolean maxValue(float value, float maxValue){
        return value >= maxValue;
    }

    public boolean maxValue(double value, double maxValue){
        return value >= maxValue;
    }

    public boolean rangeValue(int value, int minValue, int maxValue){
        return value >= minValue && value <= maxValue;
    }

    public boolean rangeValue(float value, float minValue, float maxValue){
        return value >= minValue && value <= maxValue;
    }

    public boolean rangeValue(double value, double minValue, double maxValue){
        return value >= minValue && value <= maxValue;
    }

    public boolean isUnique(String value, String[] dataSet){
        for (String data:dataSet){
            if(data.equals(value)){
                return false;
            }
        }
        return true;
    }

    public boolean isUnique(int value, int[] dataSet){
        for (int data:dataSet){
            if(data == value){
                return false;
            }
        }
        return true;
    }

    public boolean isMemberOf(String value, String[] dataSet){
        for (String data:dataSet){
            if(data.equals(value)){
                return true;
            }
        }
        return false;
    }

    public boolean isMemberOf(int value, int[] dataSet){
        for (int data:dataSet){
            if(data == value){
                return true;
            }
        }
        return false;
    }

    public boolean isValid(String value, String regex){
        return value.matches(regex);
    }

    public interface ViewValidation{
        void preValidation();
        boolean onValidateView();
        void postValidation(boolean isValid);
    }
}

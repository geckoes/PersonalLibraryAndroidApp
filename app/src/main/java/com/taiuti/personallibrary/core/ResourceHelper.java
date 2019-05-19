package com.taiuti.personallibrary.core;

import android.content.Context;
import android.content.res.TypedArray;

import com.taiuti.personallibrary.R;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filippo on 23/01/18.
 */

public class ResourceHelper {

    public static List<TypedArray> getMultiTypedArray(Context context, String key) {
        List<TypedArray> array = new ArrayList<>();

        try {
            Class<R.array> res = R.array.class;
            Field field;
            int counter = 0;

            do {
                field = res.getField(key + "_" + counter);
                array.add(context.getResources().obtainTypedArray(field.getInt(null)));
                counter++;
            } while (field != null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return array;
        }
    }
}

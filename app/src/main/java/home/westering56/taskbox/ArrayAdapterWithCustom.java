package home.westering56.taskbox;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ArrayAdapterWithCustom<T> extends ArrayAdapter<T> {
    private T mCustomValue;

    public ArrayAdapterWithCustom(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Nullable
    public T getCustomValue() {
        return mCustomValue;
    }

    public void setCustomValue(@Nullable T customValue) {
        final T firstItem = getItem(0);
        setNotifyOnChange(false);
        if (firstItem == mCustomValue) {
            // remove in order to update
            remove(firstItem);
        }
        if (customValue != null) {
            insert(customValue, 0);
        }
        mCustomValue = customValue;
        notifyDataSetChanged();
    }

    public void clearCustomValue() {
        setCustomValue(null);
    }

    public boolean hasCustomValue() {
        return mCustomValue != null;
    }


}

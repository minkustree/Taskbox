package home.westering56.taskbox.widget;

import android.database.DataSetObserver;
import android.service.autofill.AutofillService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import home.westering56.taskbox.R;

/**
 * A delegating Spinner adapter that includes a 'custom' option, inserting the selected custom value
 * at the start of the list (and the 'custom' selector at the end.)
 *
 * @param <T> the type of the values stored in this adapter, see {@link #getCustomValue()},
 *        {@link #setCustomValue(Object)} and {@link #positionOf(Object)}, as well as
 *        {@link #setGetValueFromItemFn(Function)} for how to extract values from more complex
 *        stored items
 */
public class CustomSpinnerAdapter<T> implements SpinnerAdapter {
    private static final String TAG = "CustomSpinnerAdapter";
    private static final int CUSTOM_VALUE_POSITION = 0; // if this changes, check to/fromDelegatePosition

    @SuppressWarnings("WeakerAccess") // used as an external sentinel value. Maybe.
    public static final Object CUSTOM_PICK_ITEM = new Object();

    private final SpinnerAdapter mDelegate;

    private T mCustomValue;
    private ViewBinder<T> mViewBinder;
    private Function<Object, ? extends T> mGetValueFromItemFn;

    /**
     * If the view resources in the delegate are not simple textViews, then implement a view binder
     */
    public CustomSpinnerAdapter(@NonNull SpinnerAdapter delegate) {
        if (delegate.getViewTypeCount() != 1) {
            throw new IllegalArgumentException("Delegate adapter view type count must be 1, for use with spinners");
        }
        mDelegate = delegate;
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public T getCustomValue() {
        return mCustomValue;
    }

    /**
     * @return the position at which the custom value can be found
     */
    public int setCustomValue(@Nullable T customValue) {
        mCustomValue = customValue;
        return getCustomValuePosition();
    }

    public void clearCustomValue() {
        setCustomValue(null);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean hasCustomValue() {
        return mCustomValue != null;
    }

    public int getCustomPickPosition() {
        return getCount() - 1; // position is zero-based, count is one-based
    }

    /**
     * Return the position of the custom value if set, or -1 if {@link #hasCustomValue()} is false
     */
    public int getCustomValuePosition() {
        return hasCustomValue() ? 0 : -1;
    }

    /**
     * Convert from position in this adapter to position within the delegate adapter.
     */
    private int toDelegatePosition(int position) {
        int delegatePosition = position;
        if (hasCustomValue()) delegatePosition -= 1; // easy when CUSTOM_VALUE_POSITION is 0
        return delegatePosition;
    }

    /**
     * Convert from position in the delegate adapter to position in this delegate adapter.
     */
    private int fromDelegatePosition(int delegatePosition) {
        int position = delegatePosition;
        if (hasCustomValue()) position += 1; // easy when CUSTOM_VALUE_POSITION is 0
        return position;
    }

    /**
     * Linear search through the adapter's contents to find the position that holds an item which
     * has a value is equal (by Object{@link #equals(Object)}) to value
     * <p>
     * If a position transform function is set ({@link #setGetValueFromItemFn(Function)}, it is
     * used to determine the value of each item retrieved before comparing.
     * <p>
     * null values are permitted
     *
     * @return the position of the item whose value equals <tt>value</tt>, or -1 if not found
     */
    public int positionOf(@Nullable T value) {
        for (int i = 0; i < getCount(); i++) {
            if (i == getCustomPickPosition()) continue; // nothing matches the custom pick position
            final T itemValue = getValue(i);
            if (Objects.equals(itemValue, value)) return i;
        }
        return -1;
    }

    /**
     * Set the function that converts from a stored adapter item to a value of use. Used by
     * {@link #positionOf(Object)} and {@link #getValue(int)} to get the value, rather than the item.
     * If it's not set, we assume that the value and the item in the adapter are the same.
     * <p>
     * The positionOf method iterates over each item in delegated adapter. If this
     * function is set, then it will be applied to each object that returns to turn it into something
     * of type T that can be tested for equality with the item being searched for.
     */
    public void setGetValueFromItemFn(Function<Object, ? extends T> fn) {
        mGetValueFromItemFn = fn;
    }

    /**
     * Register an observer that is called when changes happen to the data used by this adapter.
     *
     * @param observer the object that gets notified when the data set changes.
     */
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDelegate.registerDataSetObserver(observer);
    }

    /**
     * Unregister an observer that has previously been registered with this
     * adapter via {@link #registerDataSetObserver}.
     *
     * @param observer the object to unregister.
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDelegate.unregisterDataSetObserver(observer);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        // +1 for the custom pick position that's always there
        return fromDelegatePosition(mDelegate.getCount()) + 1;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position. This will be {@link #CUSTOM_PICK_ITEM} if
     * <tt>position</tt> equals {@link #getCustomPickPosition()}
     */
    @Override
    public Object getItem(int position) {
        if (position == CUSTOM_VALUE_POSITION && hasCustomValue()) return getCustomValue();
        if (position == getCustomPickPosition()) return CUSTOM_PICK_ITEM;
        return mDelegate.getItem(toDelegatePosition(position));
    }

    /**
     * @throws IllegalArgumentException if <tt>position</tt> is == {@link #getCustomPickPosition()}
     */
    @Nullable
    public T getValue(int position) {
        if (position == CUSTOM_VALUE_POSITION && hasCustomValue()) return getCustomValue();
        if (position == getCustomPickPosition()) throw new IllegalArgumentException("No value for the custom value picker entry");
        if (mGetValueFromItemFn == null) {
            //noinspection unchecked - cast and hope
            return (T)getItem(position);
        }
        return mGetValueFromItemFn.apply(getItem(position));
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        // Min and Max values for Long seem unlikely to conflict with item Ids from delegate
        if (position == CUSTOM_VALUE_POSITION && hasCustomValue()) {
            return Long.MIN_VALUE;
        }
        if (position == getCustomPickPosition()) {
            return Long.MAX_VALUE;
        }
        return mDelegate.getItemId(toDelegatePosition(position));
    }

    /**
     * Indicates whether the item ids are stable across changes to the
     * underlying data.
     *
     * @return True if the same id always refers to the same object.
     */
    @Override
    public boolean hasStableIds() {
        return mDelegate.hasStableIds();
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == CUSTOM_VALUE_POSITION && hasCustomValue() || position == getCustomPickPosition()) {
            // all views must be the same for use in a spinner, so let the delegate do the hard work
            // then rebind
            View v = mDelegate.getView(0, null, parent);
            bindCustomView(v, position);
            return v;
        }
        return mDelegate.getView(toDelegatePosition(position), convertView, parent);
    }

    /**
     * Gets a {@link View} that displays in the drop down popup
     * the data at the specified position in the data set.
     *
     * @param position    index of the item whose view we want.
     * @param convertView the old view to reuse, if possible. Note: You should
     *                    check that this view is non-null and of an appropriate type before
     *                    using. If it is not possible to convert this view to display the
     *                    correct data, this method can create a new view.
     * @param parent      the parent that this view will eventually be attached to
     * @return a {@link View} corresponding to the data at the
     * specified position.
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (position == CUSTOM_VALUE_POSITION && hasCustomValue() || position == getCustomPickPosition()) {
            // all views must be the same for use in a spinner, so let the delegate do the hard work
            // then rebind
            View v = mDelegate.getDropDownView(0, null, parent);
            bindCustomView(v, position);
            return v;
        }
        return mDelegate.getDropDownView(toDelegatePosition(position), convertView, parent);
    }

    private void bindCustomView(View v, int position) {
        boolean viewBound = false;
        final T customValue;
        final String customValueText;

        // what value are we using - picker or custom value?
        // did the user specify custom binding? If so, use it
        if (position == getCustomPickPosition()) {
            final String customPickString = v.getContext().getString(R.string.custom_spinner_adapter_custom_picker);
            if (mViewBinder != null) viewBound = mViewBinder.bindPickerView(v, customPickString);
            customValueText = customPickString;
        } else {
            customValue = Objects.requireNonNull(getCustomValue(), "custom value");
            if (mViewBinder != null) viewBound = mViewBinder.bindCustomValueView(v, customValue);
            customValueText = customValue.toString();
        }

        if (!viewBound) {
            // otherwise, we'll populate the view by:
            //  a) assuming the whole view is a text view
            //  b) setting the text view's value to customObject.toString()
            TextView text;
            try {
                text = (TextView) v;
            } catch (ClassCastException e) {
                Log.e(TAG, "You must supply a resource ID for a TextView, or use a view binder");
                throw new IllegalStateException(
                        "CustomSpinnerAdapter requires the resource ID to be a TextView or a view binder to be used", e);
            }
            text.setText(customValueText);
        }
    }

    public void setViewBinder(@Nullable ViewBinder<T> custom) {
        mViewBinder = custom;
    }

    @SuppressWarnings("SameReturnValue")
    public interface ViewBinder<T> {
        /**
         * @return true if the view was bound, otherwise false.
         */
        boolean bindCustomValueView(@NonNull View v, T customValue);

        /**
         * @return true if the view was bound, otherwise false.
         */
        boolean bindPickerView(@NonNull View v, String customPickerValue);
    }


    /**
     * Get the type of View that will be created by {@link #getView} for the specified item.
     *
     * @param position The position of the item within the adapter's data set whose view type we
     *                 want.
     * @return An integer representing the type of View. Two views should share the same type if one
     * can be converted to the other in {@link #getView}. Note: Integers must be in the
     * range 0 to {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
     * also be returned.
     * @see #IGNORE_ITEM_VIEW_TYPE
     */
    @Override
    public int getItemViewType(int position) {
        /*
          In Spinner#setAdapter we're told that there can only be one item view type when adapters
          are added to spinners ... so delegate
         */
        if (position == CUSTOM_VALUE_POSITION && hasCustomValue() || position == getCustomPickPosition()) {
            return 0;
        }
        return mDelegate.getItemViewType(toDelegatePosition(position));
    }

    /**
     * <p>
     * Returns the number of types of Views that will be created by
     * {@link #getView}. Each type represents a set of views that can be
     * converted in {@link #getView}. If the adapter always returns the same
     * type of View for all items, this method should return 1.
     * </p>
     * <p>
     * This method will only be called when the adapter is set on the {@link android.widget.AdapterView}.
     * </p>
     *
     * @return The number of types of Views that will be created by this adapter
     */
    @Override
    public int getViewTypeCount() {
        return mDelegate.getViewTypeCount(); // delegates must be 0, or Spinner#setAdapter will throw
    }

    /**
     * @return true if this adapter doesn't contain any data.  This is used to determine
     * whether the empty view should be displayed.  A typical implementation will return
     * getCount() == 0 but since getCount() includes the headers and footers, specialized
     * adapters might want a different behavior.
     */
    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    /**
     * Gets a string representation of the adapter data that can help
     * {@link AutofillService} autofill the view backed by the adapter.
     *
     * <p>
     * It should only be set (i.e., non-{@code null} if the values do not represent PII
     * (Personally Identifiable Information - sensitive data such as email addresses,
     * credit card numbers, passwords, etc...). For
     * example, it's ok to return a list of month names, but not a list of user names. A good rule of
     * thumb is that if the adapter data comes from static resources, such data is not PII - see
     * {@link ViewStructure#setDataIsSensitive(boolean)} for more info.
     *
     * @return {@code null} by default, unless implementations override it.
     */
    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return mDelegate.getAutofillOptions();
    }

}

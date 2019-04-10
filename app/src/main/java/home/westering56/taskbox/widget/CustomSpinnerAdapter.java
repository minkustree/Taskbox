package home.westering56.taskbox.widget;

import android.content.Context;
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

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import home.westering56.taskbox.R;

/**
 * A delegating Spinner adapter that includes a 'custom' option, inserting the selected custom value
 * at the start of the list (and the 'custom' selector at the end.)
 */
public class CustomSpinnerAdapter implements SpinnerAdapter {
    private static final String TAG = "CustomSpinnerAdapter";
    private static final int CUSTOM_VALUE_POSITION = 0; // if this changes, check to/fromDelegatePosition

    @SuppressWarnings("WeakerAccess") // used as an external sentinel value. Maybe.
    public static final Object CUSTOM_PICK_ITEM = new Object();

    private final SpinnerAdapter mDelegate;
    private final LayoutInflater mInflater;
    private final int mCustomResource;
    private int mCustomDropDownViewResource;

    private Object mCustomValue;

    /**
     * Throws {@link IllegalArgumentException} if the supplied delegate has a view type count of
     * anything other than 1
     */
    public CustomSpinnerAdapter(@NonNull Context context, @NonNull SpinnerAdapter delegate, @LayoutRes int customResource, @LayoutRes int customDropDownResource) {
        if (delegate.getViewTypeCount() != 1) {
            throw new IllegalArgumentException("Only adapters with a single view type are not supported");
        }
        mDelegate = delegate;
        mInflater = context.getSystemService(LayoutInflater.class);
        mCustomResource = customResource;
        mCustomDropDownViewResource = customDropDownResource;
    }

    @Nullable
    public Object getCustomValue() {
        return mCustomValue;
    }

    public void setCustomValue(@Nullable Object customValue) {
        mCustomValue = customValue;
    }

    public void clearCustomValue() {
        setCustomValue(null);
    }

    public boolean hasCustomValue() {
        return mCustomValue != null;
    }

    public int getCustomPickPosition() {
        return getCount() - 1; // position is zero-based, count is one-based
    }

    /** Convert from position in this adapter to position within the delegate adapter. */
    private int toDelegatePosition(int position) {
        int delegatePosition = position;
        if (hasCustomValue()) delegatePosition -= 1; // easy when CUSTOM_VALUE_POSITION is 0
        return delegatePosition;
    }

    /** Convert from position in the delegate adapter to position in this delegate adapter. */
    private int fromDelegatePosition(int delegatePosition) {
        int position = delegatePosition;
        if (hasCustomValue()) position += 1; // easy when CUSTOM_VALUE_POSITION is 0
        return position;
    }

    /**
     * Gets a {@link View} that displays in the drop down popup
     * the data at the specified position in the data set.
     *
     * @param position index of the item whose view we want.
     * @param convertView the old view to reuse, if possible. Note: You should
     *        check that this view is non-null and of an appropriate type before
     *        using. If it is not possible to convert this view to display the
     *        correct data, this method can create a new view.
     * @param parent the parent that this view will eventually be attached to
     * @return a {@link View} corresponding to the data at the
     *         specified position.
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if ((position == CUSTOM_VALUE_POSITION && hasCustomValue()) || position == getCustomPickPosition()) {
            return createCustomViewFromResource(mInflater, position, convertView, parent, mCustomDropDownViewResource);
        }
        return mDelegate.getDropDownView(toDelegatePosition(position), convertView, parent);
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
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        if (position == CUSTOM_VALUE_POSITION && hasCustomValue()) return Long.MIN_VALUE; // TODO: What's the ID of the custom value?
        if (position == getCustomPickPosition()) return Long.MAX_VALUE; // TODO: and the ID of the picker?
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
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if ((position == CUSTOM_VALUE_POSITION && hasCustomValue()) || position == getCustomPickPosition()) {
            return createCustomViewFromResource(mInflater, position, convertView, parent, mCustomResource);
        }
        return mDelegate.getView(toDelegatePosition(position), convertView, parent);
    }

    private View createCustomViewFromResource(LayoutInflater inflater, int position, View convertView,
                                              ViewGroup parent, int resource) {
        final View v;
        final TextView text;
        if (convertView == null) {
            v = inflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }

        /* assume that we'll populate the view by:
         a) assuming the whole view is a text view
         b) setting the text view's value to customObject.toString()
         */
        try {
            text = (TextView) v;
        } catch (ClassCastException e) {
            Log.e(TAG, "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "CustomSpinnerAdapter requires the resource ID to be a TextView", e);
        }
        final Object customValue;
        if (position == getCustomPickPosition()) {
            customValue = inflater.getContext().getString(R.string.custom_spinner_adapter_custom_picker);
        } else {
            customValue = Objects.requireNonNull(getCustomValue(), "custom value");
        }
        if (customValue instanceof CharSequence) {
            text.setText((CharSequence) customValue);
        } else {
            text.setText(customValue.toString());
        }
        return v;
    }

    /**
     * Get the type of View that will be created by {@link #getView} for the specified item.
     *
     * @param position The position of the item within the adapter's data set whose view type we
     *        want.
     * @return An integer representing the type of View. Two views should share the same type if one
     *         can be converted to the other in {@link #getView}. Note: Integers must be in the
     *         range 0 to {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
     *         also be returned.
     * @see #IGNORE_ITEM_VIEW_TYPE
     */
    @Override
    public int getItemViewType(int position) {
        /*
          This should always be the same view type, as we have enforced getViewTypeCount == 1 at
          construction time.
         */
        if (position == CUSTOM_VALUE_POSITION && hasCustomValue()) return 0;
        if (position == getCustomPickPosition()) return 0;
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
        // This should always be 1, as we checked it on construction
        return 1;
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

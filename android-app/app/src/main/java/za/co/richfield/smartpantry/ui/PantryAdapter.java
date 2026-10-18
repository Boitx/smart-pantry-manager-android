package za.co.richfield.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.model.PantryItem;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {
    public interface ItemActionListener {
        void onAction(PantryItem item);
    }

    private final ItemActionListener editListener;
    private final ItemActionListener deleteListener;
    private List<PantryItem> items = java.util.Collections.emptyList();
    private boolean showExpiryAlerts;

    public PantryAdapter(ItemActionListener editListener, ItemActionListener deleteListener) {
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public void submitList(List<PantryItem> newItems, boolean showExpiryAlerts) {
        this.items = newItems == null ? java.util.Collections.emptyList() : newItems;
        this.showExpiryAlerts = showExpiryAlerts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.quantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());
        if (item.getExpiryDate().isEmpty()) {
            holder.expiry.setVisibility(View.GONE);
        } else {
            holder.expiry.setVisibility(View.VISIBLE);
            holder.expiry.setText(showExpiryAlerts && isExpiringSoon(item.getExpiryDate())
                    ? "Expiring soon · " + item.getExpiryDate()
                    : "Expires " + item.getExpiryDate());
        }
        holder.editButton.setOnClickListener(view -> editListener.onAction(item));
        holder.deleteButton.setOnClickListener(view -> deleteListener.onAction(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatQuantity(double quantity) {
        return quantity == Math.rint(quantity)
                ? Long.toString((long) quantity)
                : String.format(Locale.getDefault(), "%s", quantity);
    }

    private boolean isExpiringSoon(String value) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        Date expiryDate = format.parse(value, position);
        if (expiryDate == null || position.getIndex() != value.length()) return false;

        Calendar today = Calendar.getInstance();
        clearTime(today);
        Calendar expiry = Calendar.getInstance();
        expiry.setTime(expiryDate);
        clearTime(expiry);
        Calendar weekFromToday = (Calendar) today.clone();
        weekFromToday.add(Calendar.DAY_OF_YEAR, 7);
        return !expiry.before(today) && !expiry.after(weekFromToday);
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    static final class PantryViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView quantity;
        private final TextView expiry;
        private final MaterialButton editButton;
        private final MaterialButton deleteButton;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.pantryItemName);
            quantity = itemView.findViewById(R.id.pantryItemQuantity);
            expiry = itemView.findViewById(R.id.pantryItemExpiry);
            editButton = itemView.findViewById(R.id.editPantryItemButton);
            deleteButton = itemView.findViewById(R.id.deletePantryItemButton);
        }
    }
}

package levkaantonov.com.study.langlock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import levkaantonov.com.study.langlock.R;

import java.util.HashSet;
import java.util.Set;

import io.realm.Case;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;

public class WordsRecycleViewAdapter extends RealmRecyclerViewAdapter<Word, WordsRecycleViewAdapter.WordViewHolder>
    implements Filterable {
    //region Fields
    private OnItemClickListener listener;
    private boolean                          inDeletionMode   = false;
    private Set<Integer>                     countersToDelete = new HashSet<>();
    private Realm                            realm;
    //endregion

    //region Ctors
    public WordsRecycleViewAdapter(@Nullable OrderedRealmCollection<Word> data, Realm realm) {
        super(data, true, true);
        this.realm = realm;
        setHasStableIds(true);
    }
    //endregion

    //region Methods

    //region Bind and create ViewHolder
    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.word_item, viewGroup, false);
        return new WordViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        final Word data = getItem(position);
        holder.data = data;
        final int itemId = data.getId();
        holder.wordTextView.setText(data.getWord());
        holder.translateTextView.setText(data.getTranslate());

        holder.deleteCheckBox.setChecked(countersToDelete.contains(itemId));
        if (inDeletionMode) {
            holder.deleteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        countersToDelete.add(itemId);
                    } else {
                        countersToDelete.remove(itemId);
                    }
                }
            });
        } else {
            holder.deleteCheckBox.setOnCheckedChangeListener(null);
        }
        holder.deleteCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
    }
    //endregion

    @Override
    public long getItemId(int index) {
        return getItem(index).getId();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //region Deleting
    public Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    public void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if(!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }
    //endregion

    //region Filtering
    public void filterResults(String text) {
        text = text == null ? null : text.toLowerCase().trim();
        RealmQuery<Word> query = realm.where(Word.class);
        if(!(text == null || "".equals(text))) {
            query.contains("word", text, Case.INSENSITIVE);
        }
        updateData(query.findAllAsync());
    }

    @Override
    public Filter getFilter() {
        return new WordsFilter(this);
    }
    //endregion

    //endregion

    //region Interfaces
    public interface OnItemClickListener {
        void onEditClick(Word word);
    }
    //endregion

    //region Classes
    class WordViewHolder extends RecyclerView.ViewHolder {

        TextView  wordTextView;
        TextView  translateTextView;
        ImageView editWordImage;
        CheckBox  deleteCheckBox;
        public Word data;

        public WordViewHolder(@NonNull final View view, final OnItemClickListener listener) {
            super(view);
            wordTextView = view.findViewById(R.id.item_word);
            translateTextView = view.findViewById(R.id.item_translate);
            deleteCheckBox = view.findViewById(R.id.item_checkBox_delete);
            editWordImage = view.findViewById(R.id.item_edit_word);
            editWordImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        listener.onEditClick(data);
                    }
                }
            });
        }
    }

    private class WordsFilter extends Filter {
        private final WordsRecycleViewAdapter adapter;

        private WordsFilter(WordsRecycleViewAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.filterResults(constraint.toString());
        }
    }
    //endregion
}

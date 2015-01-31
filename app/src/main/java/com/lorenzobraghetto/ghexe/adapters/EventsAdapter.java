package com.lorenzobraghetto.ghexe.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lorenzobraghetto.ghexe.R;
import com.lorenzobraghetto.ghexe.controller.CurrentUser;
import com.lorenzobraghetto.ghexe.controller.GhexeRESTClient;
import com.lorenzobraghetto.ghexe.controller.HttpCallback;
import com.lorenzobraghetto.ghexe.model.Event;
import com.lorenzobraghetto.ghexe.model.Presence;
import com.lorenzobraghetto.ghexe.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by monossido on 14/12/14.
 */
public class EventsAdapter extends RecyclerView.Adapter {

    private final List<Object> mItems;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM_EVENT = 1;
    private final Context mContext;
    private final String[] day_of_week;

    public EventsAdapter(Context context, List<Object> items) {
        mItems = items;
        mContext = context;
        day_of_week = mContext.getResources().getStringArray(R.array.day_of_week);
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) instanceof Presence)
            return VIEW_TYPE_ITEM_EVENT;
        else if (mItems.get(position) instanceof Event)
            return VIEW_TYPE_HEADER;
        return 0;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if (viewType == VIEW_TYPE_HEADER) {
            v = LayoutInflater.from(mContext).inflate(R.layout.raweventheader, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == VIEW_TYPE_ITEM_EVENT) {
            v = LayoutInflater.from(mContext).inflate(R.layout.rawevent, parent, false);
            return new EventViewHolder(v);
        }
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof HeaderViewHolder) {
            HeaderViewHolder eventViewHolder = (HeaderViewHolder) viewHolder;
            Event event = (Event) mItems.get(position);
            eventViewHolder.eventTitle.setText(event.getTitle());

            try {
                SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date dateStr;
                CharSequence time = "";
                dateStr = parserSDF.parse(event.getTime());
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); //like "HH:mm" or just "mm", whatever you want
                String stringRepresetnation = sdf.format(dateStr);

                eventViewHolder.eventTime.setText(stringRepresetnation);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            eventViewHolder.dayofweek_text.setText(day_of_week[event.getDayofweek()]);
        } else if (viewHolder instanceof EventViewHolder) {
            EventViewHolder eventViewHolder = (EventViewHolder) viewHolder;
            Presence presence = (Presence) mItems.get(position);
            User user = presence.getUser();
            eventViewHolder.userText.setText(user.getFirst_name());
            eventViewHolder.switchEvent.setChecked(presence.isPresence());
            eventViewHolder.switchEvent.setText(presence.isPresence() ? R.string.yes : R.string.no);
            eventViewHolder.switchEvent.setEnabled(user.getId() == CurrentUser.getInstance().getId());
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addAllFromZero(List<Object> resultList) {
        int lastCount = getItemCount();
        mItems.clear();
        if (lastCount > 0)
            notifyItemRangeRemoved(0, lastCount);
        mItems.addAll(resultList);
        notifyItemRangeInserted(0, getItemCount());
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView eventTitle;
        TextView eventTime;
        TextView dayofweek_text;

        public HeaderViewHolder(View view) {
            super(view);
            eventTitle = (TextView) view.findViewById(R.id.eventTitle);
            eventTime = (TextView) view.findViewById(R.id.eventTime);
            dayofweek_text = (TextView) view.findViewById(R.id.dayofweek_text);
        }
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView userText;
        SwitchCompat switchEvent;
        ProgressBar progressEvent;

        public EventViewHolder(View view) {
            super(view);

            userText = (TextView) view.findViewById(R.id.userText);
            switchEvent = (SwitchCompat) view.findViewById(R.id.switchEvent);
            progressEvent = (ProgressBar) view.findViewById(R.id.progressEvent);

            switchEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressEvent.setVisibility(View.VISIBLE);
                    switchEvent.setText("");
                    final Presence presence = ((Presence) mItems.get(getPosition()));
                    GhexeRESTClient.getInstance(mContext).updatePresence(mContext, presence.getId(), switchEvent.isChecked(), new HttpCallback() {

                        @Override
                        public void onSuccess(List<Object> resultList) {
                            presence.setIsPresence(switchEvent.isChecked());
                            switchEvent.setText(presence.isPresence() ? R.string.yes : R.string.no);
                            progressEvent.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                }
            });
        }
    }

}

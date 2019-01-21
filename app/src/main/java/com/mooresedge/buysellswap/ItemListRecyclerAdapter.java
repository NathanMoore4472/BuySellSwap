package com.mooresedge.buysellswap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Created by echessa on 7/24/15.
 */
public class ItemListRecyclerAdapter extends RecyclerView.Adapter<ItemListRecyclerAdapter.ViewHolder> {

    final int VIEW_TYPE_FOOTER = 0;
    final int VIEW_TYPE_CELL = 1;
    final int WATCHING_LIST_TYPE = 3;
    int mListType;
    Context mContext;

    public List<Item> mItems;

    ItemListRecyclerAdapter(List<Item> items, int listType) {
        mItems = items;
        mListType = listType;
    }

    @Override
    public int getItemViewType(int position)
    {
        return (position == mItems.size())? VIEW_TYPE_FOOTER : VIEW_TYPE_CELL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = null;
        mContext = viewGroup.getContext();

        if(i == VIEW_TYPE_CELL)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, viewGroup, false);
        else if(i == VIEW_TYPE_FOOTER)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.load_more_layout, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        if(i < mItems.size())
        {
            String item = mItems.get(i).getName();
            viewHolder.mTextView.setText(item);
            viewHolder.mItemDescription.setText(mItems.get(i).getDescription());
            if(mItems.get(i).mSold)
            {
                viewHolder.mSold.setVisibility(View.VISIBLE);
            }

            if (mItems.get(i).getPrice() == 0)
                viewHolder.mItemPrice.setText("FREE");
            else {
                NumberFormat nf = new DecimalFormat("##.##");
                viewHolder.mItemPrice.setText("£" + nf.format(mItems.get(i).getPrice()));
            }

            Picasso.with(mContext)
                    .load("http://www.tempman.ie/dbss/images/" + mItems.get(i).getmainimage() + ".jpg")
                    .error(R.drawable.ic_emoticon).into(viewHolder.mMainImage);


            if(mListType == WATCHING_LIST_TYPE) {
                viewHolder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("Do you want to remove this item from your watchlist?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        new WatchList.RemoveFromWatchList(String.valueOf(mItems.get(viewHolder.getPosition()).getDatabaseid())).execute();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Toast.makeText(mContext, "cancelled", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        // Create the AlertDialog object and return it
                        builder.create().show();
                        return false;
                    }
                });
            }

            viewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemActivity.class);

                    String pos = Integer.toString(mItems.get(viewHolder.getPosition()).getDatabaseid());
                    intent.putExtra("position", pos);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        if (mItems != null) {
            if (mItems.size() > 0) {
                if (mItems.get(mItems.size() - 1).getEnd())
                    return mItems.size();
                else
                    return mItems.size() + 1;
            } else
                return 0;
        }
        return  0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTextView;
        private final TextView mItemDescription;
        private final TextView mItemPrice;
        private final ImageView mMainImage;
        private final CardView mCardView;
        private final ImageView mSold;

        ViewHolder(View v) {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.item_title);
            mItemDescription = (TextView)v.findViewById(R.id.item_description);
            mItemPrice = (TextView)v.findViewById(R.id.item_price);
            mMainImage = (ImageView)v.findViewById(R.id.item_list_image4);
            mSold = (ImageView)v.findViewById(R.id.item_sold);
            mCardView = (CardView)v.findViewById(R.id.card);
        }
    }
}

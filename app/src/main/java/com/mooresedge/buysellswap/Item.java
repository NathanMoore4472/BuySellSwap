package com.mooresedge.buysellswap;

/**
 * Created by Nathan on 31/01/2016.
 */
public class Item {

    private String mId;
    private String mName, mDescription;
    private double mPrice;
    private String mCategory;
    private String mImageIds;
    private int Databaseid;
    private String mMainImage;
    boolean mEnd;
    boolean mSold;

    public Item(String id, String name, String desription, double price, String category, String imageids, int pid, String mainImage,boolean sold, boolean end)
    {
        mId = id;
        mName = name;
        mDescription = desription;
        mPrice = price;
        mCategory = category;
        mImageIds = imageids;
        Databaseid = pid;
        mMainImage = mainImage;
        mEnd = end;
        mSold = sold;
    }

    public String getID()
    {
        return mId;
    }
    public String getName()
    {
        return mName;
    }
    public String getDescription()
    {
        return mDescription;
    }
    public double getPrice() {return mPrice;}
    public String getImageids() {return mImageIds;}
    public int getDatabaseid() {return Databaseid;}
    public String getCategory() { return mCategory;}
    public void setDatabaseid(int set) {Databaseid = set;}
    public String getmainimage(){return mMainImage;}
    public boolean getEnd() {return mEnd;}
}

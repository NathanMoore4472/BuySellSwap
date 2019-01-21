package com.mooresedge.buysellswap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SellFragment extends android.support.v4.app.Fragment {

    ArrayList<Bitmap> mImages = new ArrayList<>();
    LinearLayout imageHolder;
    int categoryHint = 0;
    String pid;
    boolean editting = false;

    SharedPreferences app = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

    EditText Etitle;
    CustomEditText Eprice;
    EditText Edescription;
    Switch FreeSwitch;
    String ProfileID;
    String longimageids = "";
    Button categoriesButton;

    String Title, Price, Description, Category;

     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         //allow options menu to be inflated from a fragment
         setHasOptionsMenu(true);

         //get the profileID of current user
         ProfileID = app.getString("ProfileID", "");

         //check if user is editing an existing item
         Bundle bundle = getArguments();
         if(bundle != null)
             editting = true;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the view
        View view = inflater.inflate(R.layout.fragment_sell, container, false);

        //get references
        Etitle = (EditText) view.findViewById(R.id.TitleEditText);
        Edescription = (EditText) view.findViewById(R.id.DescriptionEditText);
        FreeSwitch = (Switch) view.findViewById(R.id.freeSwitch);

        //Create CustomEditText for currency input
        Eprice = (CustomEditText) view.findViewById(R.id.PriceEditText);
        Eprice.Init();

        if(editting) {
            //get the existing fields and assign them
            Bundle bundle = getArguments();
            Etitle.setText(bundle.getString("Title"));
            Eprice.setText(bundle.getString("Price"));
            Edescription.setText(bundle.getString("Description"));
        }

        //get references to spinners(dialogs)
        categoriesButton = (Button) view.findViewById(R.id.categoriesButton);
        final Spinner spinner = (Spinner) view.findViewById(R.id.CategorySpinner);

        //work around for having a hint on a spinner without it being a selectable option
        categoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.performClick();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(categoryHint != 0) {
                    categoriesButton.setText(spinner.getSelectedItem().toString());
                }
                categoryHint++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //more references
        ImageView browseButton = (ImageView) view.findViewById(R.id.add_image_button);
        FloatingActionButton saveButton = (FloatingActionButton)  getActivity().findViewById(R.id.fab);
        saveButton.setImageResource(R.drawable.ic_done);
        imageHolder = (LinearLayout) view.findViewById(R.id.browselinlayout);

        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mImages.size() < 5)
                    pickImageFromGallery();
                else
                    Toast.makeText(getActivity(), "You are allowed a maximum of 5 images", Toast.LENGTH_SHORT).show();
            }

        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if all the require fields have been filled
                if (Etitle.getText().toString().trim().length() != 0 && Edescription.getText().toString().trim().length() != 0 && Eprice.getText().toString().trim().length() != 0 && mImages.size() != 0 && !categoriesButton.getText().toString().equals("Choose a Group")) {

                    Title = Etitle.getText().toString();
                    Price = Eprice.getText().toString().replaceAll("£", "");

                    Description = Edescription.getText().toString();
                    Category = categoriesButton.getText().toString();

                    //create dialog to confirm user wants to upload for free
                    if( Eprice.getText().toString().equals("£0.00"))
                    {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Free")
                                .setMessage("Do you really want to upload this item for free?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new uploadImages().execute();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                    else
                        new uploadImages().execute(); //start uploading item

                } else {
                    Toast.makeText(getActivity(), "you cannot leave any fields blank, and you must upload a photo", Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }


    private void pickImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 12345);
    }

    @Override
    public final void onActivityResult(final int requestCode, final int resultCode, final Intent i){
        super.onActivityResult(requestCode, resultCode, i);

        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case 12345:
                    Bitmap bitmap = null;
                    Uri imageUri = i.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        mImages.add(bitmap);
                    }catch (Exception e) {e.printStackTrace();}

                    addImageToView(bitmap);
            }
        }
    }

    public void addImageToView(final Bitmap bitmap)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        //byte[] array = out.toByteArray();
        //Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        final ImageView imageView = new ImageView(getActivity());
        int sizeToDp = (int) getResources().getDimension(R.dimen.SellImageSize);
        int marginToDp = (int) getResources().getDimension(R.dimen.SellImageMargin);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeToDp, sizeToDp);
        params.setMargins(marginToDp, marginToDp, marginToDp, marginToDp);
        imageView.setLayoutParams(params);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete?")
                        .setMessage("Do you want to remove this image?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mImages.remove(bitmap);
                                imageHolder.removeView(imageView);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();

                return false;
            }
        });

        imageHolder.addView(imageView, imageHolder.getChildCount()-1);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_sell, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected class uploadImages extends AsyncTask<String, Integer, String> {

        String image_str = "";
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Uploading Images..." + "\n " + 0 + "%");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            for(int i = 0; i < mImages.size(); i++) {

                //declarations
                Bitmap MainImage = mImages.get(i);
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                final BitmapFactory.Options options = new BitmapFactory.Options();

                //scale image and compress
                Bitmap scaledimage = getScaledBitmap(MainImage, 1024);
                scaledimage.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                //change colour scheme to compress
                byte[] byte_arr = stream.toByteArray();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                scaledimage = BitmapFactory.decodeByteArray(byte_arr, 0, byte_arr.length, options);

                //putting image into Base64 for upload
                scaledimage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte_arr = stream.toByteArray();
                image_str = Base64.encodeToString(byte_arr, Base64.DEFAULT);

                //giving the image a unique id
                String dateTime = DateFormat.getDateTimeInstance().format(new Date());
                String imageID = ProfileID + dateTime + i;
                imageID = imageID.replaceAll(" ", "").replaceAll(":", "");

                //preparing data for upload
                nameValuePairs.add(new BasicNameValuePair("image", image_str));
                nameValuePairs.add(new BasicNameValuePair("imageids", imageID));
                ArrayList<NameValuePair> mainImagePairs = new ArrayList<>();
                if(i == 0) {
                    //if it is the first image, then set as the main image
                    mainImagePairs.add(new BasicNameValuePair("image", image_str));
                    mainImagePairs.add(new BasicNameValuePair("mainimageid", "m" + imageID));
                }
                //string holding all the imageids
                longimageids = longimageids + imageID + ",";

                //attempt to upload the images
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://www.tempman.ie/dbss/images/upload_image.php");
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    if(i == 0 ){
                        HttpPost Mhttppost = new HttpPost("http://www.tempman.ie/dbss/images/upload_main_image.php");
                        Mhttppost.setEntity(new UrlEncodedFormEntity(mainImagePairs));
                    }
                    httpclient.execute(httppost);

                } catch (Exception e) {
                    System.out.println("Error in http connection " + e.toString());
                }
                //give the user feedback on the progress of the upload
                double val = ((i + 1.0) / mImages.size()) * 100;
                    publishProgress((int)val);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mImages.clear();
            pDialog.dismiss();

            new createNewProduct().execute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage("Uploading Images..." + "\n" + values[0] + "%");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public static Bitmap getScaledBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    protected class createNewProduct extends AsyncTask<String, String, String>{

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... strings) {

            List<NameValuePair> params = new ArrayList<>();
            String ConvertedTitle = Title.replaceAll("'", "%27");
            String ConvertedDesc = Description.replaceAll("'", "%27");
            params.add(new BasicNameValuePair("name", ConvertedTitle));
            params.add(new BasicNameValuePair("price", Price));
            params.add(new BasicNameValuePair("description", ConvertedDesc));
            params.add(new BasicNameValuePair("imageids", longimageids));
            params.add(new BasicNameValuePair("profileid", app.getString("ProfileID" ,"")));
            params.add(new BasicNameValuePair("category", Category));

            if(editting) {
                pid = getArguments().getString("pid");
                params.add(new BasicNameValuePair("pid", pid));
                JSONObject response = new JSONParser().makeHttpRequest("http://www.tempman.ie/dbss/update_product.php",
                        "GET", params);
                response.toString();
            }
            else {
                JSONObject response = new JSONParser().makeHttpRequest("http://www.tempman.ie/dbss/create_product.php",
                        "GET", params);
                try {
                    pid = response.getString("pid");
                }catch (Exception e){}

            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();

            //reset all fields
            Eprice.setText("£0.00");
            Edescription.setText("");
            Etitle.setText("");
            categoriesButton.setText("Choose a Group");
            categoryHint = 0;
            mImages.clear();

            int childCount = imageHolder.getChildCount();

            for (int i = childCount-2; i >= 0; i--) {
                    imageHolder.removeViewAt(i);
                }

            new AlertDialog.Builder(getActivity())
                    .setTitle("Upload Successful")
                    .setMessage("Item " + Title + " was successfully uploaded")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), ItemActivity.class);
                            intent.putExtra("position", pid);
                            getActivity().startActivity(intent);
                        }
                    })
                    .show();
        }
    }
}

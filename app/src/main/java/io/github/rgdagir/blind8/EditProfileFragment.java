package io.github.rgdagir.blind8;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import io.github.rgdagir.blind8.utils.Utils;

public class EditProfileFragment extends Fragment {
    private Context context;
    private EditText editName;
    private EditText editEmail;
    private EditText editBio;
    // private TextView editBirthDate;
    private FloatingActionButton changeProfilePic;
    private Spinner myGenderSpinner;
    private Spinner interestedInSpinner;
    private SeekBar rangeSeekBar;
    private ImageView profilePic;
    private ParseUser currUser;
    private TextView displayProgress;
    private HashMap changes;
    private EditProfileFragment.OnFragmentInteractionListener mListener;
    public final static int PICK_PHOTO_CODE = 1046;
    ArrayList<String> images;
    PickGalleryAdapter galleryAdapter;
    RecyclerView rvGalleryPicker;
    private ImageView submitChanges;
    private ImageView arrowBack;

    public EditProfileFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        context = getActivity();
        currUser = ParseUser.getCurrentUser();
        changes = new HashMap<String, String>();
        View v = inflater.inflate(R.layout.new_fragment_edit_profile, container, false);
        setupViews(v);
        fetchCurrentUserAndLoadPage();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void setupViews(View v){
        // associating views from xml file with the Java class
        editName = v.findViewById(R.id.editName);
        editEmail = v.findViewById(R.id.editEmail);
        editBio = v.findViewById(R.id.editBio);
        // editBirthDate = v.findViewById(R.id.editBirthDate);
        changeProfilePic = v.findViewById(R.id.changeProfilePicBtn);
        profilePic = v.findViewById(R.id.profilePic);
        myGenderSpinner = v.findViewById(R.id.myGender);
        interestedInSpinner = v.findViewById(R.id.interestedInGender);
        rangeSeekBar = v.findViewById(R.id.rangeSeekBar);
        displayProgress = v.findViewById(R.id.ageProgress);
        submitChanges = v.findViewById(R.id.done);
        arrowBack = v.findViewById(R.id.goBackArrow);

        setupButtonListeners();
        setupTextContainerListeners();
        setupGallery(v);
        setupSpinners(v);
        setupRangeBar();
        setupCrystalSeekBar(v);
    }

    private void setupButtonListeners() {
        // adding listeners to buttons
        submitChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Done button", "pressed!");
                saveUpdatedUser();
                mListener.goBackToProfile();
            }
        });
        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Arrow button", "pressed!");

                mListener.goBackToProfile();
            }
        });
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });

    }

    public void setupGallery(View v){
        rvGalleryPicker = v.findViewById(R.id.rvGalleryPics);
        images = fetchCoverImages(ParseUser.getCurrentUser());
        galleryAdapter = new PickGalleryAdapter(images, this);
        rvGalleryPicker.setAdapter(galleryAdapter);
        rvGalleryPicker.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    }

    public void setupCrystalSeekBar(View view){
        // get seekbar from view
        final CrystalRangeSeekbar rangeSeekbar = view.findViewById(R.id.crystalRangeSeekBar);

        // get min and max text view
        final TextView tvMin = view.findViewById(R.id.rangeSeekBarMin);
        final TextView tvMax = view.findViewById(R.id.rangeSeekBarMax);

        // set listener
        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvMin.setText(String.valueOf(minValue));
                tvMax.setText(String.valueOf(maxValue));
            }
        });

        // set final value listener
        rangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                Log.d("CRS=>", String.valueOf(minValue) + " : " + String.valueOf(maxValue));
            }
        });
    }

    public ArrayList<String> fetchCoverImages(ParseUser user){
        ArrayList<String> coverImages = new ArrayList<>();
        ParseFile photo0 = user.getParseFile("coverPhoto0");
        ParseFile photo1 = user.getParseFile("coverPhoto1");
        ParseFile photo2 = user.getParseFile("coverPhoto2");
        ParseFile photo3 = user.getParseFile("coverPhoto3");
        if (photo0 != null) {
            coverImages.add(photo0.getUrl());
        } else {
            coverImages.add("placeholder");
        }
        if (photo1 != null) {
            coverImages.add(photo1.getUrl());
        } else {
            coverImages.add("placeholder");
        }
        if (photo2 != null) {
            coverImages.add(photo2.getUrl());
        } else {
            coverImages.add("placeholder");
        }
        if (photo3 != null) {
            coverImages.add(photo3.getUrl());
        } else {
            coverImages.add("placeholder");
        }
        return coverImages;
    }

//    private void showDatePicker() {
//        DatePickerFragment dateFragment = new DatePickerFragment();
//
//        // Set up current date Into dialog
//        Calendar cal = Calendar.getInstance();
//        Bundle args = new Bundle();
//        args.putInt("year", cal.get(Calendar.YEAR));
//        args.putInt("month", cal.get(Calendar.MONTH));
//        args.putInt("day", cal.get(Calendar.DAY_OF_MONTH));
//        dateFragment.setArguments(args);
//
//        // Set up callback to retrieve date info
//        dateFragment.setCallBack(new DatePickerDialog.OnDateSetListener(){
//            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
//                editBirthDate.setText(String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear+1) + "/"+String.valueOf(year));
//            }
//        });
//        dateFragment.show(getFragmentManager(), "Date Picker");
//
//    }

    private void setupTextContainerListeners() {
        // adding listeners to text containers
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                changes.put("firstName", s);
            }
        });
        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                changes.put("email", s.toString());
                changes.put("username", s.toString());
            }
        });

        editBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                changes.put("bio", s.toString());
            }
        });
//        editBirthDate.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                changes.put("dob", s.toString());
//            }
//        });
    }

    public void fetchCurrentUserAndLoadPage(){
        // populate screen
        editName.setText(currUser.getString("firstName"));
        editEmail.setText(currUser.getString("email"));
        editBio.setText(currUser.getString("bio"));
        Date inputDate = currUser.getDate("dob");
        DateFormat dataFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault());
        String strDate = dataFormat.format(inputDate);
        DateFormat inputFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault());
        Date date = null;
        try {
            date = inputFormatter.parse(strDate);
            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            String output = outputFormatter.format(date);
            // editBirthDate.setText(output);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Glide.with(context).load(currUser.getParseFile("profilePic").getUrl())
                .asBitmap().centerCrop().dontAnimate()
                .placeholder(R.mipmap.ic_picture)
                .error(R.mipmap.ic_picture)
                .into(new BitmapImageViewTarget(profilePic) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        profilePic.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public void setupSpinners(View v){
        String userGender = currUser.getString("gender");
        String userInterest = currUser.getString("interestedIn");
        final String[] genders = getResources().getStringArray(R.array.genders);
        // create spinners
        myGenderSpinner = v.findViewById(R.id.myGender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.genders, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        myGenderSpinner.setAdapter(adapter);
        myGenderSpinner.setSelection(adapter.getPosition(userGender), true);
        interestedInSpinner = v.findViewById(R.id.interestedInGender);
        // Apply the adapter to the spinner
        interestedInSpinner.setAdapter(adapter);
        interestedInSpinner.setSelection(adapter.getPosition(userInterest), true);
        myGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changes.put("gender", genders[position]);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        interestedInSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changes.put("interestedIn", genders[position]);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void setupRangeBar(){
        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                displayProgress.setText(Integer.toString(progress));
                changes.put("matchRange", Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void saveUpdatedUser(){
        Iterator it = changes.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) it.next();
            if (entry.getKey().toString().equals("dob")) { // handle birth dates
                String sDate = entry.getValue().toString();
                try {
                    Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(sDate);
                    currUser.put("dob", date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (entry.getKey().toString().equals("matchRange")) {
                int range = Integer.parseInt(entry.getValue().toString());
                currUser.put("matchRange", range);
                continue;
            }
            currUser.put(entry.getKey().toString(), entry.getValue().toString());
            System.out.println("key: " + entry.getKey().toString() + "value: " + entry.getValue().toString());
            it.remove(); // avoids a ConcurrentModificationException
        }
        currUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                Log.d("EditProfile", "Success!");
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditProfileFragment.OnFragmentInteractionListener) {
            mListener = (EditProfileFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // Placeholder, to be inserted when clicking is introduced
        void goBackToProfile();
    }


    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (i.resolveActivity(context.getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(i, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri photoUri = data.getData();
            // Do something with the photo based on Uri
            Bitmap selectedImage;
            byte[] img;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
                img = Utils.getbytearray(selectedImage);
                if (requestCode == PICK_PHOTO_CODE){ // profile pic
                    // Load the selected image into a preview
                    Glide.with(context).load(photoUri)
                            .asBitmap().centerCrop().dontAnimate()
                            .placeholder(R.drawable.ic_action_name)
                            .error(R.drawable.ic_action_name)
                            .into(new BitmapImageViewTarget(profilePic) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable circularBitmapDrawable =
                                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                    circularBitmapDrawable.setCircular(true);
                                    profilePic.setImageDrawable(circularBitmapDrawable);
                                }
                            });
                    ParseFile imageFile = new ParseFile(currUser.getObjectId() + "profilePic.jpg", img);
                    currUser.put("profilePic", imageFile);
                    currUser.saveInBackground();
                } else if (requestCode == PICK_PHOTO_CODE + 1) { // first gallery pic
                    processNewCoverPhoto(0, photoUri.toString(), img);
                } else if (requestCode == PICK_PHOTO_CODE + 2) { // second gallery pic
                    processNewCoverPhoto(1, photoUri.toString(), img);
                } else if (requestCode == PICK_PHOTO_CODE + 3) { // third gallery pic
                    processNewCoverPhoto(2, photoUri.toString(), img);
                } else if (requestCode == PICK_PHOTO_CODE + 4) { // third gallery pic
                    processNewCoverPhoto(3, photoUri.toString(), img);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processNewCoverPhoto(int position, String fileUri, byte[] image) {
        images.remove(position);
        images.add(position, fileUri);
        galleryAdapter.notifyDataSetChanged();
        rvGalleryPicker.scrollToPosition(0);
        ParseFile imageFile = new ParseFile(currUser.getObjectId() + "galleryPic.jpg", image);
        currUser.put("coverPhoto" + Integer.toString(position), imageFile);
        currUser.saveInBackground();
    }
}

package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.nio.file.StandardCopyOption;

import retrofit2.Retrofit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;
import android.webkit.MimeTypeMap;

public class Fragment2 extends Fragment {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int PICK_IMAGES = 2;
    public static final int STORAGE_PERMISSION = 100;

    private final String ORIGINAL_DIRECTORY_NAME = "original";
    private final String THUMBNAIL_DIRECTORY_NAME = "thumbnail";

    private List<Image> internalImageFilepaths;
    private final int REQUEST_GET_IMAGE = 0;

    ArrayList<ImageModel> imageList;
    ArrayList<String> selectedImageList;
    RecyclerView imageRecyclerView, selectedImageRecyclerView;
    //int[] resImg = {R.drawable.ic_camera_white_30dp, R.drawable.ic_folder_white_30dp};
    //String[] title = {"Camera", "Folder"};
    String mCurrentPhotoPath;
    SelectedImageAdapter selectedImageAdapter;
    ImageAdapter imageAdapter;
    String[] projection = {MediaStore.MediaColumns.DATA};
    File image;
    Button done;
    ImageService imageService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_images, container, false);
        //Button bt = view.findViewById(R.id.bt1);

        init(view);
        getAllImages();
        setImageList();
        setSelectedImageList();

        internalImageFilepaths = loadInternalImageFilepaths();
        imageService = new Retrofit.Builder()
                .baseUrl("http://192.249.18.168:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ImageService.class);
        Call<List<Image>> call = imageService.getAllImageName();

        call.enqueue(new Callback<List<Image>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Image>> call, Response<List<Image>> response) {
                if(response.isSuccessful()) {
                    syncImages(internalImageFilepaths, response.body());
                }
                else
                    Toast.makeText(getContext(), "getAllImageName: DB에서 이미지 리스트를 불러오는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Image>> call, Throwable t) {
                Toast.makeText(getContext(), "getAllImageName: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });


//        view.findViewById(R.id.button_get_image).setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("image/*");
//            startActivityForResult(Intent.createChooser(intent, "Get Album"), REQUEST_GET_IMAGE);
//        });

        return view;
    }

    public void init(View view) {

        imageRecyclerView = view.findViewById(R.id.recycler_view);
        selectedImageRecyclerView = view.findViewById(R.id.selected_recycler_view);
        done = view.findViewById(R.id.done);
        selectedImageList = new ArrayList<>();
        imageList = new ArrayList<>();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < selectedImageList.size(); i++) {
                    Toast.makeText(getContext().getApplicationContext(), selectedImageList.get(i), Toast.LENGTH_LONG).show();
                }
            }
        });
        //getDBImageAndStoreAsync();
    }

    public void setImageList() {
        imageRecyclerView.setLayoutManager(new GridLayoutManager(getContext().getApplicationContext(), 4));
        imageAdapter = new ImageAdapter(getContext().getApplicationContext(), imageList);
        imageRecyclerView.setAdapter(imageAdapter);

        imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                /*if (position == 0) {
                    takePicture();
                } else if (position == 1) {
                    getPickImageIntent();
                }*/
                try {
                    if (!imageList.get(position).isSelected) {
                        selectImage(position);
                    } else {
                        unSelectImage(position);
                    }
                } catch (ArrayIndexOutOfBoundsException ed) {
                    ed.printStackTrace();
                }

            }
        });
        setImagePickerList();
    }

    public void setSelectedImageList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        selectedImageRecyclerView.setLayoutManager(layoutManager);
        selectedImageAdapter = new SelectedImageAdapter(getActivity(), selectedImageList);
        selectedImageRecyclerView.setAdapter(selectedImageAdapter);
    }

    // Add Camera and Folder in ArrayList
    public void setImagePickerList() {
        /*for (int i = 0; i < resImg.length; i++) {
            ImageModel imageModel = new ImageModel();
            imageModel.setResImg(resImg[i]);
            imageModel.setTitle(title[i]);
            imageList.add(i, imageModel);
        }
        imageAdapter.notifyDataSetChanged();*/
    }

    // get all images from external storage
    public void getAllImages() {
        imageList.clear();
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        while (cursor.moveToNext()) {
            String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            ImageModel ImageModel = new ImageModel();
            ImageModel.setImage(absolutePathOfImage);
            imageList.add(ImageModel);
        }
        cursor.close();
    }

    // start the image capture Intent
    public void takePicture() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Continue only if the File was successfully created;
        File photoFile = createImageFile();
        if (photoFile != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void getPickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES);
    }

    // Add image in SelectedArrayList
    public void selectImage(int position) {
        // Check before add new item in ArrayList;
        if (!selectedImageList.contains(imageList.get(position).getImage())) {
            imageList.get(position).setSelected(true);
            selectedImageList.add(0, imageList.get(position).getImage());
            selectedImageAdapter.notifyDataSetChanged();
            imageAdapter.notifyDataSetChanged();
        }
    }

    // Remove image from selectedImageList
    public void unSelectImage(int position) {
        for (int i = 0; i < selectedImageList.size(); i++) {
            if (imageList.get(position).getImage() != null) {
                if (selectedImageList.get(i).equals(imageList.get(position).getImage())) {
                    imageList.get(position).setSelected(false);
                    selectedImageList.remove(i);
                    selectedImageAdapter.notifyDataSetChanged();
                    imageAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public File createImageFile() {
        // Create an image file name
        String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + dateTime + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (mCurrentPhotoPath != null) {
                    addImage(mCurrentPhotoPath);
                }
            } else if (requestCode == PICK_IMAGES) {
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        getImageFilePath(uri);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    getImageFilePath(uri);
                }
            }
        }
    }

    // Get image file path
    public void getImageFilePath(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                if (absolutePathOfImage != null) {
                    checkImage(absolutePathOfImage);
                } else {
                    checkImage(String.valueOf(uri));
                }
            }
        }
    }

    // add image in selectedImageList and imageList
    public void checkImage(String filePath) {
        // Check before adding a new image to ArrayList to avoid duplicate images
        if (!selectedImageList.contains(filePath)) {
            for (int pos = 0; pos < imageList.size(); pos++) {
                if (imageList.get(pos).getImage() != null) {
                    if (imageList.get(pos).getImage().equalsIgnoreCase(filePath)) {
                        imageList.remove(pos);
                    }
                }
            }
            addImage(filePath);
        }
    }

    // add image in selectedImageList and imageList
    public void addImage(String filePath) {
        ImageModel imageModel = new ImageModel();
        imageModel.setImage(filePath);
        imageModel.setSelected(true);
        imageList.add(2, imageModel);
        selectedImageList.add(0, filePath);
        selectedImageAdapter.notifyDataSetChanged();
        imageAdapter.notifyDataSetChanged();
    }

    public boolean isStoragePermissionGranted() {
        int ACCESS_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if ((ACCESS_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //init();
            getAllImages();
            setImageList();
            setSelectedImageList();
        }
    }

    private void dbCreateImage(File originalFile){
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(originalFile.toURI().toString()));
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), originalFile);

        MultipartBody.Part body = MultipartBody.Part.createFormData("image", originalFile.getName(), requestFile);

        RequestBody description = RequestBody.create(MultipartBody.FORM, "description");

        imageService.createImage(description, body).enqueue(new Callback<ResponseBody>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful())
                    Toast.makeText(getContext(), "dbPostImage: DB에 이미지를 업로드하는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "dbPostImage: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });
    }


    private <T> T nextOrNull(Iterator<T> iterator){
        if(iterator.hasNext())
            return iterator.next();
        else
            return null;
    }

    private void syncImages(List<Image> internalImages, List<Image> dbImages){
        /*dbImages.sort((l, r) -> l.name.compareTo(r.name));

            Iterator<Image> internalIterator = internalImages.listIterator();
            Iterator<Image> dbIterator = dbImages.iterator();

            Image internalImage = nextOrNull(internalIterator);
            Image dbImage = nextOrNull(dbIterator);

            while(internalImage != null && dbImage != null){
                int compare = internalImage.name.compareTo(dbImage.name);

            if(compare == 0){
                internalImage = nextOrNull(internalIterator);
                dbImage = nextOrNull(dbIterator);
            } else if(compare < 0) {
                dbCreateImage(internalImage.original);

                internalImage = nextOrNull(internalIterator);
            } else{
                getDBImageAndStoreAsync(dbImage.name);

                dbImage = nextOrNull(dbIterator);
            }
        }

        while(internalImage != null){
            dbCreateImage(internalImage.original);

            internalImage = nextOrNull(internalIterator);
        }*/
        Iterator<Image> internalIterator = internalImages.listIterator();
        Iterator<Image> dbIterator = dbImages.iterator();

        Image internalImage = nextOrNull(internalIterator);
        Image dbImage = nextOrNull(dbIterator);
        for(int i=0; i<dbImages.size(); i++) {
            getDBImageAndStoreAsync(dbImages.get(i).name);

            //dbImage = nextOrNull(dbIterator);
        }
    }

    private void getDBImageAndStoreAsync(String imageFilepath){
        imageService.getImage(imageFilepath).enqueue(new Callback<ResponseBody>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    File original = new File(new File(getContext().getFilesDir(), ORIGINAL_DIRECTORY_NAME), imageFilepath);
                    File thumbnail = new File(new File(getContext().getFilesDir(), THUMBNAIL_DIRECTORY_NAME), imageFilepath);
                    try {
                        try(InputStream responseIS = response.body().byteStream()) {
                            //Files.copy(responseIS, original.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            Bitmap bmp = BitmapFactory.decodeStream(responseIS);

                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path1 = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bmp, "Title", null);

                            getAllImages();
                            setImageList();
                            setSelectedImageList();
                            imageAdapter.notifyDataSetChanged();
                        }

                        if(!thumbnail.exists())
                            thumbnail.createNewFile();

                        try(OutputStream thumbnailOS = new FileOutputStream(thumbnail)) {
                            //decodeThumbnailFromFile(original.getPath(), 360, 360).compress(Bitmap.CompressFormat.PNG, 100, thumbnailOS);
                        }

                        internalImageFilepaths.add(new Image(imageFilepath, original, thumbnail));
                        internalImageFilepaths.sort((l, r) -> l.name.compareTo(r.name));
                        imageAdapter.updateImages(internalImageFilepaths);
                    }catch (IOException e){
                        e.printStackTrace();
                        //Toast.makeText(getContext(), "getImage: DB에서 가져온 이미지를 저장하는데 실패했습니다", Toast.LENGTH_LONG).show();

                        if(original.exists())
                            original.delete();

                        if(thumbnail.exists())
                            thumbnail.delete();
                    }
                }
                else
                    Toast.makeText(getContext(), "getImage: DB에서 이미지를 가져오는데 실패했습니다\nHTTP status code: " + response.code(), Toast.LENGTH_LONG).show();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "getImage: DB와 연결하는데 실패했습니다", Toast.LENGTH_LONG).show();
            }
        });
    }
    public static Bitmap decodeThumbnailFromFile(String pathName, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    // copied from android document
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
                inSampleSize *= 2;
        }

        return inSampleSize;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<Image> loadInternalImageFilepaths(){
        File originalDirectory = new File(getContext().getFilesDir(), ORIGINAL_DIRECTORY_NAME);
        File thumbnailDirectory = new File(getContext().getFilesDir(), THUMBNAIL_DIRECTORY_NAME);

        if(!originalDirectory.exists())
            originalDirectory.mkdir();

        if(!thumbnailDirectory.exists())
            thumbnailDirectory.mkdir();

        File[] originalFiles = originalDirectory.listFiles();
        File[] thumbnailFiles = thumbnailDirectory.listFiles();

        if(originalFiles == null || thumbnailFiles == null) {
//            Toast.makeText(getContext(), "loadInternalImageFilepaths error", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }

        if(originalFiles.length != thumbnailFiles.length) {
//            Toast.makeText(getContext(), "original file number does not equals with thumbnail file number", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }

        List<Image> res = new ArrayList<>();

        for(int i = 0; i < originalFiles.length; i++)
            res.add(new Image(originalFiles[i].getName(), originalFiles[i], thumbnailFiles[i]));

        res.sort((l, r) -> l.name.compareTo(r.name));

        return res;
    }
}
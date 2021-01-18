package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

//import static com.example.myapplication.Frame1.URL;


public class Frame3 extends Fragment {

    View view;
    ArrayList<TaxiData> arrayList = new ArrayList<>();
    CustomAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String strHour, strMin;
    static final String URL = "http://192.249.18.168:8080/";
    TextView reserve;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.frame_3, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_taxi);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        adapter = new CustomAdapter(arrayList);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                arrayList.clear();
                showTaxi();

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(adapter);

            }
        });

        showTaxi();



        TextView textView = (TextView) view.findViewById(R.id.reservation);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
                View view_ = LayoutInflater.from(getActivity()).inflate(R.layout.edit_box, null, false);
                builder.setView(view_);

                Spinner spinner_hour = (Spinner)view_.findViewById(R.id.spinner_hour);
                Spinner spinner_min = (Spinner)view_.findViewById(R.id.spinner_min);

                spinner_hour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        strHour = (String) parent.getItemAtPosition(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spinner_min.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        strMin = (String) parent.getItemAtPosition(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                EditText editText_location = (EditText)view_.findViewById(R.id.edit_location);
                EditText editText_person = (EditText)view_.findViewById(R.id.edit_person);

                AlertDialog dialog = builder.create();

                Button button_submit = (Button)view_.findViewById(R.id.button_submit);
                button_submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strLocation = editText_location.getText().toString();
                        int intPerson = Integer.parseInt(editText_person.getText().toString());

                        TaxiData td = new TaxiData();
                        td.setLocation(strLocation);
                        td.setPerson(intPerson);
                        td.setWhen(strHour+":"+strMin);
                        createTaxi(td);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        reserve = (TextView) view.findViewById(R.id.reserve_complete);

        return view;
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private ArrayList<TaxiData> mArrayList = null;

        public CustomAdapter(ArrayList<TaxiData> arrayList){
            this.mArrayList = arrayList;
        }

        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.taxi_recycler, parent, false);
            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            holder.textViewWhen.setText(mArrayList.get(position).getWhen());
            holder.textViewLocation.setText("where: "+mArrayList.get(position).getLocation());
            holder.textViewPerson.setText("Available seats: "+Integer.toString(mArrayList.get(position).getPerson()));
            if (mArrayList.get(position).getPerson() == 0){
                holder.textViewPerson.setTextColor(Color.parseColor("#FFB30000"));
            }

            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mArrayList.get(position).setPerson(mArrayList.get(position).getPerson()-1);
                    notifyDataSetChanged();
                    // db에 업데이트
                    joinTaxi(mArrayList.get(position));

//                    for (int i =0; i<mArrayList.size(); i++){
//
//                    }

                    holder.imageButton.setImageResource(R.drawable.join);
                    holder.imageButton.setClickable(false);
                    holder.imageButton.setEnabled(false);








                    reserve.setText("You have a reservation at "+mArrayList.get(position).getWhen());




                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewWhen, textViewLocation, textViewPerson;
            ImageButton imageButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.textViewWhen = (TextView) itemView.findViewById(R.id.when_taxi);
                this.textViewLocation = (TextView) itemView.findViewById(R.id.location_taxi);
                this.textViewPerson = (TextView) itemView.findViewById(R.id.person_taxi);
                this.imageButton = (ImageButton) itemView.findViewById(R.id.join_taxi);

            }
        }
    }

    public class TaxiData{
        String location;
        String when;
        String _id;
        int person;

        public String getLocation(){
            return location;
        }

        public String getWhen(){
            return when;
        }

        public String getTaxi_id(){
            return _id;
        }

        public int getPerson(){
            return person;
        }

        public void setLocation(String location){
            this.location = location;
        }

        public void setWhen(String when){
            this.when = when;
        }

        public void setTaxi_id(String taxi_id) {
            this._id = taxi_id;
        }

        public void setPerson(int person){
            this.person = person;
        }
    }

    public class TaxiUser{
        String taxi_id;
        String faceBookId;

        public String getTaxi_id(){
            return taxi_id;
        }
        public String getFaceBookId(){
            return faceBookId;
        }

        public void setFaceBookId(String faceBookId){
            this.faceBookId = faceBookId;
        }
        public void setTaxi_id(String taxi_id){
            this.taxi_id = taxi_id;
        }
    }

    public interface RetrofitService {
        @GET("api/taxis")
        Call<List<TaxiData>> getTaxis();

        @POST("api/taxis")
        Call<TaxiData> postTaxis(@Body TaxiData data);

        @PUT("api/taxis/{taxi_id}")
//        Call<ResponseBody> putTaxi(@Path("taxi_id") String taxi_id);
        Call<ResponseBody> putTaxi(@Path("taxi_id") String taxi_id, @Body TaxiUser taxiUser);

//        @DELETE("api/contacts/{contact_id}")
//        Call<ResponseBody> deleteContact(@Path("contact_id") String contact_id);
    }

    public void showTaxi() {

//        arrayList = new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        Call<List<TaxiData>> call = retrofitService.getTaxis();
        call.enqueue(new Callback<List<TaxiData>>() {
            @Override
            public void onResponse(Call<List<TaxiData>> call, retrofit2.Response<List<TaxiData>> response) {
                if (response.isSuccessful()) {
                    List<TaxiData> mList = response.body();
                    for (int i = 0; i < mList.size(); i++) {
                        TaxiData data = new TaxiData();
                        data.setLocation(mList.get(i).getLocation());
                        data.setPerson(mList.get(i).getPerson());
                        data.setWhen(mList.get(i).getWhen());
                        data.setTaxi_id(mList.get(i).getTaxi_id());

                        arrayList.add(data);
                    }
                } else {

                }

                //이름순 정렬
                Collections.sort(arrayList, new Comparator<TaxiData>(){
                    @Override
                    public int compare(TaxiData rhs, TaxiData lhs){
                        return rhs.getWhen().compareTo(lhs.getWhen());
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<TaxiData>> call, Throwable t) {

            }
        });
    }

    public void createTaxi(TaxiData taxiData){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        Call<TaxiData> call = retrofitService.postTaxis(taxiData);
        call.enqueue(new Callback<TaxiData>() {
            @Override
            public void onResponse(Call<TaxiData> call, retrofit2.Response<TaxiData> response) {
                TaxiData td = response.body();
                taxiData.setTaxi_id(td.getTaxi_id());
                arrayList.add(taxiData);

                //이름순 정렬
                Collections.sort(arrayList, new Comparator<TaxiData>(){
                    @Override
                    public int compare(TaxiData rhs, TaxiData lhs){
                        return rhs.getWhen().compareTo(lhs.getWhen());
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<TaxiData> call, Throwable t) {

            }
        });

    }

    public void joinTaxi(TaxiData taxiData){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
//        Log.e("er", taxiData.getTaxi_id());

        TaxiUser taxiUser = new TaxiUser();
        taxiUser.setFaceBookId(Frame1.facebookId);
        taxiUser.setTaxi_id(taxiData.getTaxi_id());
        Call<ResponseBody> call = retrofitService.putTaxi(taxiData.getTaxi_id(), taxiUser);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        arrayList.clear();
        super.onResume();
    }

}
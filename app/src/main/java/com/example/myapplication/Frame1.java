package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.facebook.CallbackManager;

public class Frame1 extends Fragment {
    View view;
    RecyclerView recyclerView;
    ArrayList<UserData> arrayList;
    EditText searchView;
    CallbackManager callbackManager;
    CustomAdapter adapter;
    static final String URL = "http://192.249.18.168:8080/";
    static String facebookId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.frame_1, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        arrayList = new ArrayList<>();
        adapter = new CustomAdapter(getActivity(), arrayList);
        recyclerView.setAdapter(adapter);
        showContact();


        searchView = (EditText) view.findViewById(R.id.searchView);
        searchView.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        ImageButton add_button = (ImageButton) view.findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view_ = LayoutInflater.from(getActivity()).inflate(R.layout.edit_contacts, null, false);
                builder.setView(view_);

                final Button Button_submit = (Button) view_.findViewById(R.id.button_submit);
                final EditText editText_name = (EditText) view_.findViewById(R.id.edittext_name);
                final EditText editText_phone = (EditText) view_.findViewById(R.id.edittext_phone);

                Button_submit.setText("추가");
                final AlertDialog dialog = builder.create();

                Button_submit.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        String strName = editText_name.getText().toString();
                        String strPhone = editText_phone.getText().toString();

                        UserData ud = new UserData();
                        ud.setName(strName);
                        ud.setNumber(strPhone);
                        ud.setFacebookId(facebookId);

                        createContact(ud);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        return view;
    }

    @Override
    public void onResume(){
        arrayList.clear();
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.viewHolder> implements Filterable{
        private ArrayList<UserData> mArrayList;
        private ArrayList<UserData> filtered_arrayList;
        private Context mContext;

        public CustomAdapter(Context context, ArrayList<UserData> arrayList) {
            this.mArrayList = arrayList;
            this.filtered_arrayList = arrayList;
            this.mContext = context;
        }

        @Override
        public viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_list, viewGroup, false);
            return new viewHolder(view);
        }

        @Override
        public void onBindViewHolder(viewHolder viewHolder, int position) {
            viewHolder.textViewName.setText(filtered_arrayList.get(position).getName());
            viewHolder.textViewNumber.setText(filtered_arrayList.get(position).getNumber());
        }

        @Override
        public int getItemCount() {
            return filtered_arrayList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint){
                    String charString = constraint.toString();
                    if(charString.isEmpty()){
                        filtered_arrayList = arrayList;
                    }
                    else{
                        ArrayList<UserData> filtering_arrayList = new ArrayList<>();
                        for (int i=0; i<arrayList.size(); i++){
                            if(arrayList.get(i).getName().toLowerCase().contains(charString.toLowerCase())){
                                filtering_arrayList.add(arrayList.get(i));
                            }
                        }
                        filtered_arrayList = filtering_arrayList;
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filtered_arrayList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results){
                    filtered_arrayList = (ArrayList<UserData>)results.values;
                    notifyDataSetChanged();
                }
            };
        }

        public class viewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
            TextView textViewName;
            TextView textViewNumber;
            ImageButton edit_delete_button;

            public viewHolder(View view) {
                super(view);
                this.textViewName = (TextView) view.findViewById(R.id.name);
                this.textViewNumber = (TextView) view.findViewById(R.id.number);
                this.edit_delete_button = (ImageButton) view.findViewById(R.id.edit_delete_button);

                this.edit_delete_button.setOnCreateContextMenuListener(this);
                // 클래스 내에서 onCreateContextMenu 구현
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) { // contextmenu 생성, 항목 선택시 호출되는 리스너 등록
                MenuItem Edit = menu.add(Menu.NONE, 1001, 1, "EDIT");
                MenuItem Delete = menu.add(Menu.NONE, 1002, 2, "DELETE");
                Edit.setOnMenuItemClickListener(onEditMenu);
                Delete.setOnMenuItemClickListener(onEditMenu);
            }

            private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case 1001: //edit
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            View view = LayoutInflater.from(mContext).inflate(R.layout.edit_contacts, null, false);
                            builder.setView(view);
                            builder.setView(view);
                            final Button Button_submit = (Button) view.findViewById(R.id.button_submit);
                            final EditText edittext_name = (EditText) view.findViewById(R.id.edittext_name);
                            final EditText edittext_phone = (EditText) view.findViewById(R.id.edittext_phone);

                            // 입력되어있던 데이터 보여줌
                            edittext_name.setText(arrayList.get(getAdapterPosition()).getName());
                            edittext_phone.setText(arrayList.get(getAdapterPosition()).getNumber());

                            final AlertDialog dialog = builder.create();
                            Button_submit.setOnClickListener(new View.OnClickListener() {
                                // 추가 눌렀을 때 현재 UI에 입력되어있는 내용으로
                                public void onClick(View v) {
                                    String strName = edittext_name.getText().toString();
                                    String strNumber = edittext_phone.getText().toString();

                                    UserData userdata = arrayList.get(getAdapterPosition());
                                    userdata.setName(strName);
                                    userdata.setNumber(strNumber);

                                    // arrayList 데이터 변경
                                    updateContact(userdata.getContact_id(), userdata, getAdapterPosition());

                                    dialog.dismiss();
                                }
                            });

                            dialog.show();

                            break;
                        case 1002: //delete
                            removeContact(arrayList.get(getAdapterPosition()).getContact_id(), getAdapterPosition());
                            break;
                    }
                    return true;
                }
            };
        }
    }

    public class UserData {
        String name;
        String number;
        String _id;
        String facebookId;

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        public String getContact_id(){
            return _id;
        }

        public String getFacebookId(){
            return facebookId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public void setContact_id(String contact_id) {
            this._id = contact_id;
        }

        public void setFacebookId(String facebookId){
            this.facebookId = facebookId;
        }
    }

    public interface RetrofitService {
        @GET("api/contacts/{facebookId}")
        Call<List<UserData>> getContacts(@Path("facebookId") String facebookId);

        @POST("api/contacts")
        Call<UserData> postContacts(@Body UserData user);

        @PUT("api/contacts/{contact_id}")
        Call<ResponseBody> putContact(@Path("contact_id") String contact_id, @Body UserData user);

        @DELETE("api/contacts/{contact_id}")
        Call<ResponseBody> deleteContact(@Path("contact_id") String contact_id);
    }


    public void showContact() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        arrayList.clear();

        Call<List<UserData>> call = retrofitService.getContacts(facebookId);
        call.enqueue(new Callback<List<UserData>>() {
            @Override
            public void onResponse(Call<List<UserData>> call, retrofit2.Response<List<UserData>> response) {
                if (response.isSuccessful()) {
                    List<UserData> mList = response.body();
                    for (int i = 0; i < mList.size(); i++) {
                        UserData userData = new UserData();
                        userData.setName(mList.get(i).getName());
                        userData.setNumber(mList.get(i).getNumber());
                        userData.setContact_id(mList.get(i).getContact_id());
                        userData.setFacebookId(mList.get(i).getFacebookId());
                        arrayList.add(userData);
                    }

                } else {

                }

                Collections.sort(arrayList, new Comparator<UserData>(){
                    @Override
                    public int compare(UserData rhs, UserData lhs){
                        return rhs.getName().compareTo(lhs.getName());
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<UserData>> call, Throwable t) {

            }
        });
    }

    public void createContact(UserData userData){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        Call<UserData> call = retrofitService.postContacts(userData);
        call.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, retrofit2.Response<UserData> response) {

                UserData ud = response.body();
                userData.setContact_id(ud.getContact_id());
                arrayList.add(userData);
                Collections.sort(arrayList, new Comparator<UserData>(){
                    @Override
                    public int compare(UserData rhs, UserData lhs){
                        return rhs.getName().compareTo(lhs.getName());
                    }
                });

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {

            }
        });
    }

    public void removeContact(String contact_id, int position){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        Call<ResponseBody> call = retrofitService.deleteContact(contact_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                arrayList.remove(position);
                adapter.notifyDataSetChanged();
                Collections.sort(arrayList, new Comparator<UserData>(){
                    @Override
                    public int compare(UserData rhs, UserData lhs){
                        return rhs.getName().compareTo(lhs.getName());
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    public void updateContact(String string, UserData userData, int position){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService retrofitService = retrofit.create(RetrofitService.class);

        Call<ResponseBody> call = retrofitService.putContact(string, userData);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                arrayList.set(position, userData);
                adapter.notifyDataSetChanged();
                Collections.sort(arrayList, new Comparator<UserData>(){
                    @Override
                    public int compare(UserData rhs, UserData lhs){
                        return rhs.getName().compareTo(lhs.getName());
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

}

package com.findmyelderly.findmyelderly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.findmyelderly.findmyelderly.Constant.Edit;



public class EditListAdapter extends RecyclerView.Adapter<EditListAdapter.ViewHolder> {
    private RecyclerView mPostList;
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase;
    private Query mQueryFollowingPost;
    private LinearLayoutManager mLayoutManager;
    public static String elderlyemail = "";



    private List<Edit> mAddedDraft;
    private List<String> mAddedDraftId=new ArrayList<>();
    private FragmentManager fragmentManager;

    // Store the context for easy access
    private Context mContext;
    private static LayoutInflater inflater=null;

    // Pass in the contact array into the constructor
    public EditListAdapter(Context context, List<String> addedDraftId, List<Edit> addedDraft, FragmentManager fragmentManager) {
        mAddedDraftId= addedDraftId;
        mAddedDraft=addedDraft;
        mContext = context;
        this.fragmentManager=fragmentManager;
        Log.d("here",mAddedDraftId.size()+"");
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View postView = inflater.inflate(R.layout.editlist, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder{
            View mView;
            Button mEditButton;
            LinearLayout rowClick;
            DatabaseReference mDatabaseAdd;
            DatabaseReference mDatabaseLike;
            FirebaseUser mAuth;

            public ViewHolder(View itemView) {
                super(itemView);
                mView=itemView;
                mEditButton=(Button)mView.findViewById(R.id.editbutton);
                rowClick=(LinearLayout)mView.findViewById(R.id.draft_row);
                mAuth= FirebaseAuth.getInstance().getCurrentUser();;

            }
            public void setElderlyEmail(String email){
                TextView elderlyEmail=(TextView)mView.findViewById(R.id.elderlyemail);
                elderlyEmail.setText(email);
            }
        public void setUserName(String userName){
            TextView UserName=(TextView)mView.findViewById(R.id.username);
            UserName.setText(userName);
        }


//

        }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final int thePosition=position;
        // Get the data model based on position
        final Edit draft = mAddedDraft.get(position);
        final String draftId= mAddedDraftId.get(position);
        Log.d("draft","bind");


        viewHolder.setElderlyEmail("帳户:"+draft.getEmail());
        viewHolder.setUserName("長者姓名:"+draft.getUserName());



        //click post
        viewHolder.mEditButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                view.getContext().startActivity(new Intent(view.getContext(),EditActivity.class));


            }
        });


    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mAddedDraft.size();
    }
}
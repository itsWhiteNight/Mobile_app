package com.example.appli_mobile;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Leaderboard extends AppCompatActivity {

    private static final String TAG = "Leaderboard";

    private ListView listView;
    private ArrayList<String> leaderboardList;
    private ArrayAdapter<String> adapter;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard);

        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // Initialize UI components
        listView = findViewById(R.id.listView);
        if (listView == null) {
            Log.e(TAG, "ListView is null. Check the layout file for the correct ID.");
        } else {
            Log.d(TAG, "ListView successfully initialized.");
        }

        leaderboardList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.list_item, leaderboardList);
        listView.setAdapter(adapter);

        Log.d(TAG, "Adapter set for ListView.");

        // Fetch and display leaderboard data
        displayLeaderboard();
    }

    private void displayLeaderboard() {
        // Add ValueEventListener to fetch data from database
        usersRef.orderByChild("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the leaderboard list before adding new data
                leaderboardList.clear();

                // Iterate through each child node (user ID) in the "users" node
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Log the entire snapshot to see its structure
                    Log.d(TAG, "UserSnapshot: " + userSnapshot.toString());

                    // Get the username and score of each user
                    String username = userSnapshot.child("username").getValue(String.class);
                    Integer scoreValue = userSnapshot.child("score").getValue(Integer.class);

                    // Log the user ID
                    String userId = userSnapshot.getKey();
                    Log.d(TAG, "UserID: " + userId);

                    // Check if the username and score value exist
                    if (username != null && scoreValue != null) {
                        int score = scoreValue;
                        // Add the username and score to the leaderboard list
                        leaderboardList.add(username + ": " + score);
                        Log.d(TAG, "User: " + username + ", Score: " + score);
                    } else {
                        Log.e(TAG, "Null values found for user ID: " + userId);
                    }
                }

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Adapter notified of data change. List size: " + leaderboardList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e(TAG, "DatabaseError: " + databaseError.getMessage());
            }
        });
    }
}

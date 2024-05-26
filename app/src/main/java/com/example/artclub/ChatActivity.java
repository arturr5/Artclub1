package com.example.artclub;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    String receiverId, receiverName, senderRoom, receiverRoom;
    FirebaseFirestore db;
    ImageView sendBtn;
    EditText messageText;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    private static int messageIdCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        receiverId = getIntent().getStringExtra("id");
        receiverName = getIntent().getStringExtra("name");

        getSupportActionBar().setTitle(receiverName);
        if(receiverId!=null){
            senderRoom = FirebaseAuth.getInstance().getUid()+receiverId;
            receiverRoom = receiverId+FirebaseAuth.getInstance().getUid();
        }
        sendBtn = findViewById(R.id.sendMessageIcon);
        messageAdapter = new MessageAdapter(this);
        recyclerView = findViewById(R.id.chatRecycler);
        messageText = findViewById(R.id.messageEdit);

        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CollectionReference chatsRef = db.collection("chats");
        chatsRef.document(senderRoom).collection("messages").orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("ChatActivity", "Listen failed.", error);
                    return;
                }

                if (value != null) {
                    List<MessageModel> messages = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        MessageModel messageModel = doc.toObject(MessageModel.class);
                        messages.add(messageModel);
                    }
                    messageAdapter.clear();
                    for (MessageModel message: messages){
                        messageAdapter.add(message);
                    }
                    messageAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
                } else {
                    Log.d("ChatActivity", "Current data: null");
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageText.getText().toString();
                if(message.trim().length()>0){
                    SendMassage(message);
                }else{
                    Toast.makeText(ChatActivity.this, "Meassage cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendMassage(String message){
        String messageId = generateMessageId().toString();
        MessageModel messageModel = new MessageModel(messageId,FirebaseAuth.getInstance().getUid(),message);
        messageAdapter.add(messageModel);

        CollectionReference chatsRef = db.collection("chats");
        DocumentReference senderRoomRef = chatsRef.document(senderRoom);
        DocumentReference receiverRoomRef = chatsRef.document(receiverRoom);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message);
        messageData.put("senderId", FirebaseAuth.getInstance().getUid());
        messageData.put("receiverId", receiverId);
        messageData.put("timestamp", Timestamp.now());

        senderRoomRef.collection("messages").document(messageId).set(messageData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                receiverRoomRef.collection("messages").document(messageId).set(messageData);
                recyclerView.scrollToPosition(messageAdapter.getItemCount()-1);
                messageText.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Failed to send the message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_manu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logoutm){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChatActivity.this,SingInActivity.class));
            finish();
            return true;
        }
        if(item.getItemId()==R.id.profile){
            Intent intent = new Intent(ChatActivity.this, Profile.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }
    public static String generateMessageId() {
        return String.format("MSG-%04d", ++messageIdCounter);
    }
}
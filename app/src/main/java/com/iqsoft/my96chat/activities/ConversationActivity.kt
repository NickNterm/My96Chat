package com.iqsoft.my96chat.activities

import android.content.Intent
import android.content.res.TypedArray
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.iqsoft.my96chat.R
import com.iqsoft.my96chat.adapters.MessageListAdapter
import com.iqsoft.my96chat.firebase.FirestoreClass
import com.iqsoft.my96chat.models.Conversation
import com.iqsoft.my96chat.models.Message
import com.iqsoft.my96chat.models.User
import com.iqsoft.my96chat.utils.Constants
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {
    private lateinit var myUser: User
    private lateinit var otherUser: User
    private lateinit var conversation: Conversation
    private lateinit var mAdapter: MessageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        loadIntent()
        setupActionBar()
        setupRecyclerViewer()
        conversation_send_message_button.setOnClickListener {
            if (conversation_edit_text_message.text.isNotEmpty()) {
                val message = Message(conversation_edit_text_message.text.toString(), myUser.id)
                FirestoreClass().createMessage(
                    this,
                    conversation.messages,
                    message,
                    conversation.id
                )

                conversation.messages.removeAt(conversation.messages.size - 1)
                conversation_edit_text_message.setText("")
            }
        }
    }

    private fun realtime() {
        val docRef =
            FirebaseFirestore.getInstance().collection(Constants.CONV).document(conversation.id)
        docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) {
                Log.w("testt", "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                "Local"
            else
                "Server"

            if (snapshot != null && snapshot.exists()) {
                val map =
                    snapshot.get(Constants.CONV_MESSAGES) as ArrayList<HashMap<String, String>>
                if (map.size > 0) {
                    if (conversation.messages.size != map.size) {
                        Log.d("testt", "change")
                        val newMessage = Message(
                            map[map.size - 1]["text"].toString(),
                            map[map.size - 1]["sender"].toString()
                        )
                        conversation.messages.add(newMessage)
                        mAdapter.notifyItemInserted(conversation.messages.size - 1)
                        conversation_recycler.scrollToPosition(conversation.messages.size - 1)
                        if(newMessage.sender == myUser.id) {
                            FirestoreClass().sendNotification(
                                this,
                                otherUser.fcm,
                                myUser.name,
                                newMessage.text
                            )
                        }
                    }
                }
            } else {
                Log.d("testt", "$source data: null")
            }
        }
    }

    fun messageCreated() {

    }

    private fun setupRecyclerViewer() {
        conversation_recycler.layoutManager = LinearLayoutManager(this)
        mAdapter = MessageListAdapter(this, conversation.messages, myUser, otherUser)
        conversation_recycler.adapter = mAdapter
        conversation_recycler.scrollToPosition(conversation.messages.size - 1)
        realtime()
    }

    private fun setupActionBar() {
        setSupportActionBar(conversation_toolbar)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        actionBar.title = otherUser.name
        conversation_toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun loadIntent() {
        conversation = intent.getParcelableExtra(Constants.INTENT_CONV)!!
        myUser = intent.getParcelableExtra(Constants.INTENT_MY_USER)!!
        otherUser = intent.getParcelableExtra(Constants.INTENT_OTHER_USER)!!
        Log.e("testt", conversation.toString())
    }
}
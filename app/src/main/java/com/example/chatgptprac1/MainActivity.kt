package com.example.chatgptprac1

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient


//https://m.blog.naver.com/withsoft_/223055180268
//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


var client = OkHttpClient()
var gpt = chatGPT()


private var messages = gpt.messages

class MainActivity : ComponentActivity() {
    companion object{
        lateinit var ps: SharedPreferences
        var num=0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatApp()
        }
    }
}

@Composable
fun ChatApp() {
    val messagesState = remember { messages }
    var context= LocalContext.current

    MainActivity.ps = context.getSharedPreferences("chatRecord",0)
    //MainActivity.ps.edit().clear().apply() //저장 초기화
    MainActivity.num = MainActivity.ps.getInt("chatNum",0)

    for(i in 1..MainActivity.num){ //대화 내용 복구
        MainActivity.ps.getString("me"+i,"")?.let { Message(true, it) }?.let { messagesState.add(it) }
        MainActivity.ps.getString("gpt"+i,"")?.let { Message(false, it) }?.let { messagesState.add(it) }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        RecyclerViewContent(messagesState)
        Footer(onSendMessage = { message -> messagesState.add(Message(true, message)) })
    }
}

@Composable
fun RecyclerViewContent(messages: List<Message>) {

    val lazyListState  = rememberLazyListState()

    LaunchedEffect(messages) {
        // Scroll to the bottom when the message list changes
        if(messages.size>1){
            lazyListState.scrollToItem(messages.size - 1)
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxHeight(0.92f).fillMaxWidth(),
        contentPadding = PaddingValues(8.dp, 8.dp),
        state = lazyListState
    ) {

        items(messages) { message ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp, 4.dp),
                horizontalAlignment = if(message.me)Alignment.End else Alignment.Start,

            ) {
                Box(
                    modifier = Modifier
                        .background(if (message.me) Color(0xFFddddff) else Color(0xFFddeeee))
                        .fillMaxWidth(0.6f).padding(16.dp)
                ) {
                    Text(text = message.chat)

                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Footer(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue()) }

    Row(modifier = Modifier.fillMaxHeight()) {
        TextField(
            value = text,
            onValueChange = { newText -> text = newText },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(),
            singleLine = true,
        )
        SendButton(onClick = {
            if (text.text.isNotEmpty()) {
                onSendMessage(text.text)
                gpt.callAPI(text.text)
                MainActivity.ps.edit().putString("me"+(MainActivity.num+1),text.text).apply()
                text = TextFieldValue()

            }
        })
    }
}

@Composable
fun SendButton(onClick: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.ic_send),
        contentDescription = null,
        Modifier
            .fillMaxSize()
            .background(Color(0xFFeeeeff), shape = RoundedCornerShape(15))
            .clickable { onClick() }
    )
}

data class Message(
    val me: Boolean,
    val chat: String
)

@Preview
@Composable
fun PreviewChatApp() {
    ChatApp()
}

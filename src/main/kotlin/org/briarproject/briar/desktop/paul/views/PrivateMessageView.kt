package org.briarproject.briar.desktop.paul.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.v1.DialogProperties
import org.briarproject.briar.desktop.paul.data.ContactList
import org.briarproject.briar.desktop.paul.model.Contact
import org.briarproject.briar.desktop.paul.model.Message
import org.briarproject.briar.desktop.paul.theme.*


val HEADER_SIZE = 66.dp;

@Composable
fun privateMessageView(UIContact: Contact, onContactSelect: (Contact) -> Unit) {
    //Local State for managing the Add Contact Popup
    val (AddContactDialog, onCancelAdd) = remember { mutableStateOf(false) }
    addContactDialog(AddContactDialog, onCancelAdd)
    Column(modifier = Modifier.fillMaxHeight()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Divider(color = divider, modifier = Modifier.fillMaxHeight().width(1.dp))
            Column(modifier = Modifier.fillMaxHeight().background(color = briarBlack).width(275.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(HEADER_SIZE).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Contacts",
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    IconButton(
                        onClick = { onCancelAdd(true) },
                        modifier = Modifier.align(Alignment.CenterVertically).background(color = briarDarkGray)
                    ) {
                        Icon(Icons.Filled.Add, "add contact", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Divider(color = divider, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    for (c in ContactList.contacts) {
                        contactCard(c, UIContact, onSel = onContactSelect)
                    }
                }

            }
            Divider(color = divider, modifier = Modifier.fillMaxHeight().width(1.dp))
            Column(modifier = Modifier.weight(1f).fillMaxHeight().background(color = darkGray)) {
                drawMessageRow(UIContact)
            }
        }
    }
}

@Composable
fun addContactDialog(isVisible: Boolean, onCancel: (Boolean) -> Unit) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = {
                onCancel(false)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                        Text(
                            text = "Add Contact at a Distance",
                            fontSize = 24.sp,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Contact's Link",
                            Modifier.width(128.dp).align(Alignment.CenterVertically),
                            color = lightGray
                        )
                        TextField("", onValueChange = {}, modifier = Modifier.fillMaxWidth())
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Contact's Name",
                            Modifier.width(128.dp).align(Alignment.CenterVertically),
                            color = lightGray
                        )
                        TextField("", onValueChange = {}, modifier = Modifier.fillMaxWidth())
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Your Link",
                            modifier = Modifier.width(128.dp).align(Alignment.CenterVertically),
                            color = lightGray
                        )
                        TextField(
                            "briar://ksdjlfgakslhjgaklsjdhglkasjdlk3j12h4lk2j3tkj4",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth()
                        );
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancel(false)
                    },
                    modifier = Modifier.background(briarGreen)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCancel(false)
                    }, modifier = Modifier.background(briarBlack)
                ) {
                    Text("Cancel")
                }
            },

            backgroundColor = briarBlue,
            contentColor = Color.White,
            modifier = Modifier.border(1.dp, color = divider),
            properties = DialogProperties(resizable = false, undecorated = true, size = IntSize(600, 300))
        )
    }
}

@Composable
fun contactCard(contact: Contact, selContact: Contact, onSel: (Contact) -> Unit) {
    var bgColor = briarBlack
    if (selContact.name == contact.name) {
        bgColor = darkGray
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(HEADER_SIZE).background(bgColor)
            .clickable(onClick = { onSel(contact) }), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 16.dp)) {
            Image(
                bitmap = imageFromResource("images/profile_images/" + contact.profile_pic),
                "image",
                modifier = Modifier.size(40.dp).align(Alignment.CenterVertically).clip(
                    CircleShape
                ).border(2.dp, color = Color.White, CircleShape)
            )
            Column(modifier = Modifier.align(Alignment.CenterVertically).padding(start = 12.dp)) {
                Text(
                    contact.name,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 2.dp)
                )
                Text(
                    contact.last_heard,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
        androidx.compose.foundation.Canvas(
            modifier = Modifier.padding(horizontal = 29.dp).size(22.dp).align(Alignment.CenterVertically), onDraw = {
                val size = 16.dp.toPx()
                drawCircle(
                    color = Color.White,
                    radius = size / 2f
                )
                if (contact.online) {
                    drawCircle(
                        color = briarGreen,
                        radius = 14.dp.toPx() / 2f
                    )
                } else {
                    drawCircle(
                        color = briarBlack,
                        radius = 14.dp.toPx() / 2f
                    )
                }
            })
    }

    Divider(color = divider, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
}

@Composable
fun textBubble(m: Message) {
    Column(Modifier.fillMaxWidth()) {
        if (m.from == null) {
            Column(Modifier.fillMaxWidth(fraction = 0.9f).align(Alignment.End)) {
                Column(Modifier.background(briarBlueMsg).padding(8.dp).align(Alignment.End)) {
                    Text(m.message, fontSize = 14.sp, color = Color.White, modifier = Modifier.align(Alignment.Start))
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        Text(m.time, Modifier.padding(end = 4.dp), fontSize = 10.sp, color = Color.LightGray)
                        if (m.delivered) {
                            Icon(
                                Icons.Filled.Check,
                                "sent",
                                tint = Color.LightGray,
                                modifier = Modifier.size(10.dp).align(Alignment.CenterVertically)
                            )
                        } else {
                            Icon(
                                Icons.Filled.Send,
                                "sending",
                                tint = Color.LightGray,
                                modifier = Modifier.size(10.dp).align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxWidth(fraction = 0.9f).align(Alignment.Start)) {
                Column(Modifier.background(briarGrayMsg).padding(8.dp).align(Alignment.Start)) {
                    Text(m.message, fontSize = 14.sp, color = Color.White, modifier = Modifier.align(Alignment.Start))
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        Text(m.time, Modifier.padding(end = 4.dp), fontSize = 10.sp, color = Color.LightGray)
                        if (m.delivered) {
                            Icon(
                                Icons.Filled.Check,
                                "sent",
                                tint = Color.LightGray,
                                modifier = Modifier.size(10.dp).align(Alignment.CenterVertically)
                            )
                        } else {
                            Icon(
                                Icons.Filled.Send,
                                "sending",
                                tint = Color.LightGray,
                                modifier = Modifier.size(10.dp).align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun drawTextBubbles(msgList: List<Message>) {
    LazyColumn(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(msgList) { m ->
            textBubble(m)
        }
    }
}

@Composable
fun drawMessageRow(UIContact: Contact) {
    Box(Modifier.fillMaxHeight()) {
        Box(modifier = Modifier.fillMaxWidth().height(HEADER_SIZE + 1.dp)) {
            Row(modifier = Modifier.align(Alignment.Center)) {
                Image(
                    bitmap = imageFromResource("images/profile_images/" + UIContact.profile_pic),
                    "sel_contact_prof",
                    modifier = Modifier.size(36.dp).align(
                        Alignment.CenterVertically
                    ).clip(
                        CircleShape
                    ).border(2.dp, color = Color.White, CircleShape)
                )
                Text(
                    UIContact.name,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 12.dp),
                    fontSize = 24.sp
                )
            }
            IconButton(onClick = {}, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                Icon(Icons.Filled.MoreVert, "contact info", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Divider(color = divider, thickness = 1.dp, modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter))
        }
        Box(Modifier.padding(top = HEADER_SIZE + 1.dp, bottom = HEADER_SIZE)) {
            drawTextBubbles(UIContact.privateMessages)
        }
        var text by remember { mutableStateOf(TextFieldValue("")) }
        Box(Modifier.align(Alignment.BottomCenter).background(darkGray)) {
            OutlinedTextField(
                value = text,
                trailingIcon = { Icon(Icons.Filled.Send, "send message", tint = briarGreen) },
                leadingIcon = { Icon(Icons.Filled.AddCircle, contentDescription = "add file") },
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp).fillMaxWidth(),
                label = { Text(text = "Message") },
                textStyle = TextStyle(color = Color.White),
                placeholder = { Text(text = "Your message to " + UIContact.name) },
                onValueChange = {
                    text = it
                },
            )
        }
    }
}
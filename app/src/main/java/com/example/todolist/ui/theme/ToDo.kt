@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.todolist.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date

data class ShoppingItem @OptIn(ExperimentalMaterial3Api::class)
constructor(val id: Int, var Task: String, var Description: String, var IsEditied: Boolean = false, var IsComplete: Boolean = false, var date1: Long?, var time_1:TimePickerState?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoListApp() {
    var Items by remember { mutableStateOf(listOf<ShoppingItem>()) }
    var TasksName by remember { mutableStateOf("") }
    var TaskDescription by remember { mutableStateOf("") }
    var itemComplete by remember { mutableStateOf(false) }
    var itemEditied by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime: TimePickerState? by remember { mutableStateOf(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { itemComplete = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add Task")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(Items) { item ->
                if (item.IsComplete) {
                    ShoppingItemEditor(
                        item = item,
                        onEditComplete = { editedName, editedQuantity ->
                            Items = Items.map { it.copy(IsComplete = false) }
                            val editedItem = Items.find { it.id == item.id }
                            editedItem?.let {
                                it.Task = editedName
                                it.Description = editedQuantity
                            }
                        }
                    )
                } else {
                    ShoppingListItem(
                        item = item,
                        isChecked = item.IsEditied,
                        onCheckedChange = {isChecked ->
                            Items = Items.map {
                                if (it.id == item.id) it.copy(IsEditied = isChecked) else it
                            }},
                        onEditClick = { Items = Items.map { it.copy(IsComplete = it.id == item.id) } },
                        onDeleteClick = { Items = Items - item }
                    )
                }
            }
        }
    }

    if (itemComplete) {
        TasksName="Enter Task"
        TaskDescription = "No Task Description"
        AlertDialog(
            onDismissRequest = { itemComplete = false },
            title = { Text("Add TASK to perform") },
            text = {
                Column {
                    OutlinedTextField(
                        value = TasksName,
                        onValueChange = { TasksName = it },
                        singleLine = false,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    OutlinedTextField(
                        value = TaskDescription,
                        onValueChange = {
                            TaskDescription = it

                        },
                        singleLine = false,
                        maxLines = 20,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Button(onClick = { showDatePicker = true }) {
                        Text("Pick a Date")
                    }

                    selectedDate?.let {
                        Text("Selected Date: ${DateFormat.getDateInstance().format(Date(it))}")
                    }
                    if (showDatePicker) {
                        DatePickerModalInput(
                            onDateSelected = { date ->
                                selectedDate = date
                                showDatePicker = false
                            },
                            onDismiss = {
                                showDatePicker = false
                            }
                        )
                    }

                    Button(onClick = { showTimePicker = true }) {
                        Text("Pick a Time")
                    }

                    selectedTime?.let {
                        val formattedTime = formatTime(it.hour, it.minute)
                        Text("Selected Time: $formattedTime")
                    }
                    if (showTimePicker) {
                        DialWithDialogExample(
                            onConfirm = { time ->
                                selectedTime = time
                                showTimePicker = false
                            },
                            onDismiss = {
                                showTimePicker = false
                            }
                        )
                    }



                }

            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (TasksName.isNotBlank()) {
                                val newItem = ShoppingItem(
                                    id = Items.size + 1,
                                    Task = TasksName,
                                    Description = TaskDescription,
                                    date1 = selectedDate,
                                    time_1=selectedTime
                                )
                                Items = Items + newItem
                                

                                itemComplete = false
                                itemEditied=false

                                TasksName = ""
                            }
                        }
                    ) {
                        Text("Add", fontSize = 18.sp)
                    }
                    Button(
                        onClick = { itemComplete = false }
                    ) {
                        Text("Cancel", fontSize = 18.sp)
                    }
                }
            }
        )
    }
}

@Composable
fun ShoppingListItem(item: ShoppingItem,
                     isChecked: Boolean,
                     onCheckedChange: (Boolean) -> Unit,
                     onEditClick: () -> Unit,
                     onDeleteClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(
                border = BorderStroke(2.dp, Color(0XFF018786)),
                shape = RoundedCornerShape(5)
            ),

        verticalArrangement  = Arrangement.SpaceBetween
    ) {
        Row {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "Done",
                modifier = Modifier.padding(16.dp),
                fontSize = 22.sp
            )
        }

        Row {


            Text(
                text = item.Task,
                modifier = Modifier.padding(8.dp),
                color = if (isChecked) Color.Gray else Color.Black,
                fontSize = 20.sp
            )

            if (!isChecked) {
                    IconButton(
                        onClick = onEditClick,
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    }
                    IconButton(
                        onClick = onDeleteClick,
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
            }
        }
        Text(
            text = "Description \n ${item.Description}",
            modifier = Modifier.padding(8.dp),
            color = if (isChecked) Color.LightGray else Color.Black,
            fontSize = 12.sp
        )
        Text(
            text = "Deadline: ${
                item.date1?.let { DateFormat.getDateInstance().format(Date(it)) } ?: "No Date"
            }",
            modifier = Modifier.padding(8.dp),
            color = if (isChecked) Color.Gray else Color.Red,
            fontSize = 16.sp
        )
        Text(
            text = "Time: ${
                item.time_1?.let { formatTime(it.hour, it.minute) } ?: "No Time"
            }",
            modifier = Modifier.padding(8.dp),
            color = if (isChecked) Color.Gray else Color.Red,
            fontSize = 16.sp
        )

    }
}

@Composable
fun ShoppingItemEditor(item: ShoppingItem,onEditComplete: (String, String) -> Unit) {
    var newTask by remember { mutableStateOf(item.Task) }
    var newDescription by remember { mutableStateOf(item.Description) }
    var Iscom by remember { mutableStateOf(item.IsComplete) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {
            BasicTextField(
                value = newTask,
                onValueChange = { newTask = it },
                singleLine = false,
                maxLines = 5,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                textStyle = TextStyle(fontSize = 20.sp)
            )

            BasicTextField(
                value = newDescription,
                onValueChange = { newDescription = it },
                singleLine = false,
                maxLines = 20,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                textStyle = TextStyle(fontSize = 20.sp)
            )
        }
        Button(
            onClick = {
                Iscom = false
                onEditComplete(newTask, newDescription)
            }
        ) {
            Text("Save", fontSize = 18.sp)
        }
    }
}

@Preview
@Composable
fun ShoppingListPreview() {
    ToDoListApp()
}
package com.example.todolist.ui.theme


import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date
import com.example.todolist.AlarmScheduler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha

// Data class representing a shopping/task item with all relevant fields
// - id: unique identifier
// - Task: task name
// - Description: task description
// - IsEditied: whether the item is being edited
// - IsComplete: whether the item is in edit mode
// - date1: selected date in millis
// - time_1: selected time state
//
data class ShoppingItem @OptIn(ExperimentalMaterial3Api::class)
constructor(val id: Int, var Task: String, var Description: String, var IsEditied: Boolean = false, var IsComplete: Boolean = false, var date1: Long?, var time_1:TimePickerState?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoListApp() {
    // State for the list of items
    var Items by remember { mutableStateOf(listOf<ShoppingItem>()) }
    // State for the task name input
    var TasksName by remember { mutableStateOf("") }
    // State for the task description input
    var TaskDescription by remember { mutableStateOf("") }
    // State to control if the add task dialog is shown
    var itemComplete by remember { mutableStateOf(false) }
    // State to control if an item is being edited
    var itemEditied by remember { mutableStateOf(false) }

    // State for date picker dialog
    var showDatePicker by remember { mutableStateOf(false) }
    // State for selected date
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    // State for time picker dialog
    var showTimePicker by remember { mutableStateOf(false) }
    // State for selected time
    var selectedTime: TimePickerState? by remember { mutableStateOf(null) }

    val context = LocalContext.current
    // AlarmScheduler instance for scheduling/canceling alarms
    val alarmScheduler = remember { AlarmScheduler(context) }

    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFFCE4EC)),
                    tileMode = TileMode.Clamp
                )
            ),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Button to open the add task dialog
        Button(
            onClick = { itemComplete = true }, // Show add task dialog
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add Task", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Animated list of tasks
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(Items, key = { it.id }) { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(500)),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    if (item.IsComplete) {
                        // If item is in edit mode, show the editor
                        ShoppingItemEditor(
                            item = item,
                            onEditComplete = { editedName, editedQuantity ->
                                // On edit complete, update the item and exit edit mode
                                Items = Items.map { it.copy(IsComplete = false) }
                                val editedItem = Items.find { it.id == item.id }
                                editedItem?.let {
                                    it.Task = editedName
                                    it.Description = editedQuantity
                                    // If date or time changes, cancel old alarm and schedule new one
                                    // This part needs more complex logic if date/time can be edited in ShoppingItemEditor
                                    // For now, assuming date/time are set only during initial creation
                                }
                                Toast.makeText(context, "Task updated!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        // Show the task item in the list
                        ShoppingListItem(
                            item = item,
                            isChecked = item.IsEditied,
                            onCheckedChange = { isChecked ->
                                // Toggle the checked state (done/undone)
                                Items = Items.map {
                                    if (it.id == item.id) it.copy(IsEditied = isChecked) else it
                                }
                                Toast.makeText(
                                    context,
                                    if (isChecked) "Task marked as done!" else "Task marked as not done!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onEditClick = {
                                Items = Items.map { it.copy(IsComplete = it.id == item.id) } // Enter edit mode for this item
                            },
                            onDeleteClick = {
                                alarmScheduler.cancel(item) // Cancel alarm when item is deleted
                                Items = Items - item // Remove item from list
                                Toast.makeText(context, "Task deleted!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (itemComplete) {
        TasksName = "Enter Task"
        TaskDescription = "No Task Description"
        AlertDialog(
            onDismissRequest = { itemComplete = false }, // Close dialog on dismiss
            title = { Text("Add TASK to perform", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Input for task name
                    OutlinedTextField(
                        value = TasksName,
                        onValueChange = { TasksName = it },
                        singleLine = false,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    // Input for task description
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
                    // Button to show date picker
                    Button(onClick = { showDatePicker = true }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Pick a Date")
                    }
                    // Show selected date
                    selectedDate?.let {
                        Text("Selected Date: ${DateFormat.getDateInstance().format(Date(it))}", fontWeight = FontWeight.SemiBold)
                    }
                    // Date picker dialog
                    if (showDatePicker) {
                        DatePickerModalInput(
                            onDateSelected = { date ->
                                selectedDate = date // Save selected date
                                showDatePicker = false // Close date picker
                            },
                            onDismiss = {
                                showDatePicker = false // Close date picker
                            }
                        )
                    }
                    // Button to show time picker
                    Button(onClick = { showTimePicker = true }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Pick a Time")
                    }
                    // Show selected time
                    selectedTime?.let {
                        val formattedTime = formatTime(it.hour, it.minute)
                        Text("Selected Time: $formattedTime", fontWeight = FontWeight.SemiBold)
                    }
                    // Time picker dialog
                    if (showTimePicker) {
                        DialWithDialogExample(
                            onConfirm = { time ->
                                selectedTime = time // Save selected time
                                showTimePicker = false // Close time picker
                            },
                            onDismiss = {
                                showTimePicker = false // Close time picker
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
                    // Add button to confirm adding the task
                    Button(
                        onClick = {
                            if (TasksName.isNotBlank() && selectedDate != null && selectedTime != null) {
                                val newItem = ShoppingItem(
                                    id = Items.size + 1,
                                    Task = TasksName,
                                    Description = TaskDescription,
                                    date1 = selectedDate,
                                    time_1 = selectedTime
                                )
                                Items = Items + newItem // Add new item to list
                                // Schedule alarm if date and time are set
                                alarmScheduler.schedule(newItem)
                                Toast.makeText(
                                    context,
                                    "Alarm set for "+
                                        DateFormat.getDateInstance().format(Date(selectedDate!!))+
                                        " at "+formatTime(selectedTime!!.hour, selectedTime!!.minute),
                                    Toast.LENGTH_LONG
                                ).show()
                                itemComplete = false // Close dialog
                                itemEditied = false
                                TasksName = ""
                                TaskDescription = ""
                                selectedDate = null
                                selectedTime = null
                            } else {
                                Toast.makeText(context, "Please enter all fields, pick a date and time!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = TasksName.isNotBlank() && selectedDate != null && selectedTime != null
                    ) {
                        Text("Add", fontSize = 18.sp)
                    }
                    // Cancel button to close dialog
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

// Animated and dynamic ShoppingListItem
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Animate alpha for checked/unchecked
    val alpha = animateFloatAsState(targetValue = if (isChecked) 0.5f else 1f, label = "alphaAnim")
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(
                border = BorderStroke(2.dp, if (isChecked) Color.Gray else Color(0XFF018786)),
                shape = RoundedCornerShape(5)
            )
            .background(
                if (isChecked)
                    Brush.horizontalGradient(listOf(Color.LightGray, Color.White))
                else
                    Brush.horizontalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFFCE4EC)))
            )
            .alpha(alpha.value),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Animated checkbox
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(4.dp),
                enabled = true
            )
            Text(
                text = if (isChecked) "Done" else "Active",
                modifier = Modifier.padding(16.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isChecked) Color.Gray else Color(0xFF018786)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.Task,
                modifier = Modifier.padding(8.dp),
                color = if (isChecked) Color.Gray else Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            if (!isChecked) {
                IconButton(
                    onClick = onEditClick,
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Task")
                }
                IconButton(
                    onClick = onDeleteClick,
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Task")
                }
            }
        }
        Text(
            text = "Description: ${item.Description}",
            modifier = Modifier.padding(8.dp),
            color = if (isChecked) Color.LightGray else Color.Black,
            fontSize = 14.sp
        )
        Text(
            text = "Deadline: ${item.date1?.let { DateFormat.getDateInstance().format(Date(it)) } ?: "No Date"}",
            modifier = Modifier.padding(8.dp),
            color = if (isChecked) Color.Gray else Color.Red,
            fontSize = 16.sp
        )
        Text(
            text = "Time: ${item.time_1?.let { formatTime(it.hour, it.minute) } ?: "No Time"}",
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


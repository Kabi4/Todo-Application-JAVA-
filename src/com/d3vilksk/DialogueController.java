package com.d3vilksk;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogueController {
    @FXML
    private TextField newTodoTitle;
    @FXML
    private TextArea newTodoDetials;
    @FXML
    private DatePicker newTodoDeadline;

    public TodoItem processResults(){
        String title = newTodoTitle.getText().trim();
        String details = newTodoDetials.getText().trim();
        LocalDate deadLine = newTodoDeadline.getValue();
        TodoItem newItem = new TodoItem(title,details,deadLine);
        TodoData.getInstance().addTodoItem(newItem);
        return newItem;
    }
}




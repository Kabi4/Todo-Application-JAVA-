package com.d3vilksk;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    private List<TodoItem> todoItems;

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private TextArea todoTextArea;

    @FXML
    private Label todoLabelDue;

    @FXML
    private Label todoLabelDeadline;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<TodoItem> filteredList;

    private Predicate<TodoItem> allItems;
    private Predicate<TodoItem> todayItems;

    public void initialize(){
        allItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return (todoItem.getDeadLine().equals(LocalDate.now())||todoItem.getDeadLine().isAfter(LocalDate.now()));
            }
        };

        todayItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return (todoItem.getDeadLine().equals(LocalDate.now()));
            }
        };

        listContextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        listContextMenu.getItems().addAll(deleteItem);
        this.todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observableValue, TodoItem oldItem, TodoItem newItem) {
                if (newItem != null){
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    todoTextArea.setText(item.getDetails());
                    todoLabelDeadline.setText(df.format(item.getDeadLine()));
                }
            }
        });

        this.filteredList = new FilteredList<>(TodoData.getInstance().getTodoItems(),allItems );
        SortedList<TodoItem> sortedList = new SortedList<>(this.filteredList, new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadLine().compareTo(o2.getDeadLine());
            }
        });

        this.todoListView.setItems(sortedList);
        this.todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.todoListView.getSelectionModel().selectFirst();
        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {

                ListCell<TodoItem> cell = new ListCell<TodoItem>(){
                    @Override
                    protected void updateItem(TodoItem todoItem, boolean b) {
                        super.updateItem(todoItem, b);
                        if(b){
                            setText(null);
                        }else{
                            setText(todoItem.getTitle());
                            if(todoItem.getDeadLine().equals(LocalDate.now())){
                                setTextFill(Color.RED);
                            }else if(todoItem.getDeadLine().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.ORANGERED);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obj,wasempty,nowEmpty)->{
                            if(nowEmpty){
                                cell.setContextMenu(null);
                            }else{
                                cell.setContextMenu(listContextMenu);
                            }
                }
                );
                return cell;
            }
        });
    }

    public void handleKeyPressed(KeyEvent event){
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        if(item!=null&event.getCode().equals(KeyCode.DELETE)){
            deleteItem(item);
        }
    }

    public void deleteItem(TodoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete A Todo Item");
        alert.setHeaderText("Delete item: "+item.getTitle());
        alert.setContentText("Are you sure? Press OK to confirm or Cancel");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK){
            TodoData.getInstance().deleteTodoItem(item);
        }
    }

    @FXML
    public void showNewItemDialogueBox(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialogue.fxml"));
        try {

            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch (IOException e){
            System.out.println("Couldn't load the Dialog Window!");
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK){
            DialogueController controller = fxmlLoader.getController();
            TodoItem newItem = controller.processResults();
            todoListView.getSelectionModel().select(newItem);
        }else if(result.isPresent() && result.get()==ButtonType.CANCEL){
            System.out.println("Closing Application..!");
        }else{
            System.out.println("Nothing Pressed!");
        }
    }

    @FXML
    public void handleClickListView(){
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        todoTextArea.setText(item.getDetails());
        todoLabelDeadline.setText(item.getDeadLine().toString());

    }

    @FXML
    public  void handleFilterButton(){
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();

        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(todayItems);
            if(filteredList.isEmpty()){
                todoTextArea.clear();
                todoLabelDeadline.setText("");
            }else if(filteredList.contains(item)){
                todoListView.getSelectionModel().select(item);
            }else {
                todoListView.getSelectionModel().selectFirst();
            }
        }else{
            filteredList.setPredicate(allItems);
            todoListView.getSelectionModel().select(item);
        }
    }
    public void handleExit(){
        Platform.exit();
    }
}

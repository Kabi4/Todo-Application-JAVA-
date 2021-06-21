package com.d3vilksk;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public class TodoData {
    private static TodoData instance = new TodoData();
    private static String filename = "TodoData.txt";

    private ObservableList<TodoItem> todoItems;
    private DateTimeFormatter dateTimeFormatter;

    public static TodoData getInstance() {
        return instance;
    }

    private TodoData(){
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    }


    public ObservableList<TodoItem> getTodoItems() {
        return todoItems;
    }

    public void loadTodoItems() throws IOException {
        todoItems = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);
        String input;

        try{
            while((input= br.readLine())!=null){
                String[] itemParts = input.split("\t");
                String title = itemParts[0];
                String details = itemParts[1];
                String date = itemParts[2];
                LocalDate dateLocal = LocalDate.parse(date,dateTimeFormatter);
                TodoItem newItem = new TodoItem(title,details,dateLocal);
                todoItems.add(newItem);
            }
        }finally {
            if(br!=null){
                br.close();
            }
        }
    }

    public void saveTodoItems() throws IOException{
        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);
        try{

            Iterator<TodoItem> todoIterator = this.todoItems.iterator();
            while(todoIterator.hasNext()){

                TodoItem item = todoIterator.next();
                bw.write(String.format("%s\t%s\t%s",item.getTitle(),item.getDetails(),item.getDeadLine().format(dateTimeFormatter)));
                bw.newLine();
            }
        }finally {
            if(bw!=null){
                bw.close();
            }
        }
    }

    public void addTodoItem(TodoItem newItem){

        this.todoItems.add(newItem);
    }
    public void deleteTodoItem(TodoItem item){

        this.todoItems.remove(item);
    }
}

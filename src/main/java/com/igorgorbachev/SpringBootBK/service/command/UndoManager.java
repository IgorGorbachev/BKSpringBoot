//package com.igorgorbachev.SpringBootBK.service.command;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.annotation.SessionScope;
//
//import java.util.Stack;
//@Component
//@SessionScope
//public class UndoManager {
//    private final Stack<Command> undoStack = new Stack<>();
//    private final Stack<Command> redoStack = new Stack<>();
//    private final int MAX_HISTORY = 50; // Ограничение истории
//
//    public void executeCommand(Command command) {
//        command.execute();
//        undoStack.push(command);
//        redoStack.clear();
//
//        // Ограничиваем размер истории
//        if (undoStack.size() > MAX_HISTORY) {
//            undoStack.remove(0);
//        }
//    }
//
//    public void undo() {
//        if (!undoStack.isEmpty()) {
//            Command command = undoStack.pop();
//            command.undo();
//            redoStack.push(command);
//        }
//    }
//
//    public void redo() {
//        if (!redoStack.isEmpty()) {
//            Command command = redoStack.pop();
//            command.redo();
//            undoStack.push(command);
//        }
//    }
//
//    public boolean canUndo() {
//        return !undoStack.isEmpty();
//    }
//
//    public boolean canRedo() {
//        return !redoStack.isEmpty();
//    }
//
//    public void clearHistory() {
//        undoStack.clear();
//        redoStack.clear();
//    }
//}

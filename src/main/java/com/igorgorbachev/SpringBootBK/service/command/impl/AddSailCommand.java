//package com.igorgorbachev.SpringBootBK.service.command.impl;
//
//
//import com.igorgorbachev.SpringBootBK.entity.Sail;
//import com.igorgorbachev.SpringBootBK.service.SailService;
//import com.igorgorbachev.SpringBootBK.service.command.InitializableCommand;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//@Component
//@Scope("prototype")
//public class AddSailCommand implements InitializableCommand {
//
//    private SailService sailService;
//    private Sail sail;
//    private Long klientId;
//    private Long savedSailId;
//
//    @Override
//    public void initialize(Object... params) {
//        this.sailService = (SailService) params[0];
//        this.sail = (Sail) params[1];
//        this.klientId = (Long) params[2];
//    }
//
//    @Override
//    public void execute() {
//        sailService.addSail(sail, klientId);
//        this.savedSailId = sail.getId();
//    }
//
//    @Override
//    public void undo() {
//        if (savedSailId != null) {
//            sailService.deleteSail(savedSailId);
//        }
//    }
//
//    @Override
//    public void redo() {
//        execute();
//    }
//
//}

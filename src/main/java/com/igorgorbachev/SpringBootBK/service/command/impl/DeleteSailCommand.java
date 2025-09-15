//package com.igorgorbachev.SpringBootBK.service.command.impl;
//
//import com.igorgorbachev.SpringBootBK.entity.Sail;
//import com.igorgorbachev.SpringBootBK.service.SailService;
//import com.igorgorbachev.SpringBootBK.service.command.InitializableCommand;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//
//
//@Component
//@Scope("prototype")
//public class DeleteSailCommand implements InitializableCommand {
//
//    private SailService sailService;
//    private Long sailId;
//    private Sail originalSailState;
//
//    @Override
//    public void initialize(Object... params) {
//        this.sailService = (SailService) params[0];
//        this.sailId = (Long) params[1];
//
//        saveOriginalState();
//    }
//
//    private void saveOriginalState() {
//        this.originalSailState = sailService.getSailById(sailId);
//    }
//
//    @Override
//    public void execute() {
//        sailService.deleteSail(sailId);
//    }
//
//    @Override
//    public void undo() {
//        if (originalSailState != null) {
//            Sail restoredSail = new Sail();
//            // Восстанавливаем все поля
//            System.out.println("******************" + originalSailState);
//            restoredSail.setId(originalSailState.getId());
//            restoredSail.setNameSail(originalSailState.getNameSail());
//            restoredSail.setArticul(originalSailState.getArticul());
//            restoredSail.setZakupka(originalSailState.getZakupka());
//            restoredSail.setPrice(originalSailState.getPrice());
//            restoredSail.setKolichestvo(originalSailState.getKolichestvo());
//            restoredSail.setStatus(originalSailState.getStatus());
//            restoredSail.setOplata(originalSailState.getOplata());
//            restoredSail.setKlient(originalSailState.getKlient());
//            restoredSail.setToDay(originalSailState.getToDay());
//
//            System.out.println("****************" + restoredSail);
//
//            sailService.saveSail(restoredSail);
//        }
//    }
//
//    @Override
//    public void redo() {
//        execute();
//    }
//
//}

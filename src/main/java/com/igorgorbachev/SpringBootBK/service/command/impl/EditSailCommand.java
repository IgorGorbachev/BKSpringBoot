//package com.igorgorbachev.SpringBootBK.service.command.impl;
//
//import com.igorgorbachev.SpringBootBK.entity.Sail;
//import com.igorgorbachev.SpringBootBK.service.SailService;
//import com.igorgorbachev.SpringBootBK.service.command.InitializableCommand;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//
//@Component
//@Scope("prototype")
//public class EditSailCommand implements InitializableCommand {
//    private SailService sailService;
//    private Sail sail;
//    private Long statusId;
//    private Long oplataId;
//    private String nameSail;
//    private String articul;
//    private BigDecimal zakupka;
//    private BigDecimal price;
//    private BigDecimal kolichestvo;
//
//    private Sail originalSailState;
//
//    @Override
//    public void initialize(Object... params) {
//        this.sailService = (SailService) params[0];
//        this.sail = (Sail) params[1];
//        this.statusId = (Long) params[2];
//        this.oplataId = (Long) params[3];
//        this.nameSail = (String) params[4];
//        this.articul = (String) params[5];
//        this.zakupka = (BigDecimal) params[6];
//        this.price = (BigDecimal) params[7];
//        this.kolichestvo = (BigDecimal) params[8];
//
//        saveOriginalState();
//    }
//
//    private void saveOriginalState() {
//        if (sail.getId() != null) {
//            this.originalSailState = sailService.getSailById(sail.getId());
//        }
//    }
//
//    @Override
//    public void execute() {
//        sailService.changeSail(sail, statusId, oplataId,
//                nameSail, articul, zakupka, price, kolichestvo);
//    }
//
//    @Override
//    public void undo() {
//        if (originalSailState != null) {
//            sailService.changeSail(sail,
//                    originalSailState.getStatus().getId(),
//                    originalSailState.getOplata().getId(),
//                    originalSailState.getNameSail(),
//                    originalSailState.getArticul(),
//                    originalSailState.getZakupka(),
//                    originalSailState.getPrice(),
//                    originalSailState.getKolichestvo()
//            );
//        }
//    }
//
//    @Override
//    public void redo() {
//        execute();
//    }
//}

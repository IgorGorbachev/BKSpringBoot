//package com.igorgorbachev.SpringBootBK.service.command;
//
//import com.igorgorbachev.SpringBootBK.entity.Sail;
//import com.igorgorbachev.SpringBootBK.service.SailService;
//
//
//
//public abstract class BaseSailCommand implements Command {
//
//    protected final SailService sailService;
//    protected final Sail sail;
//    protected final Long klientId;
//    protected final Long statusId;
//    protected final Long oplataId;
//
//    public BaseSailCommand(SailService sailService, Sail sail, Long klientId,
//                           Long statusId, Long oplataId) {
//        this.sailService = sailService;
//        this.sail = sail;
//        this.klientId = klientId;
//        this.statusId = statusId;
//        this.oplataId = oplataId;
//    }
//
//    protected Sail originalSailState;
//
//    protected void saveOriginalState(){
//        if (sail.getId() != null){
//            this.originalSailState = sailService.getSailById(sail.getId());
//        }
//    }
//
//    protected void restoreOriginalState(){
//        if (originalSailState != null){
//            sail.setNameSail(originalSailState.getNameSail());
//            sail.setArticul(originalSailState.getArticul());
//            sail.setZakupka(originalSailState.getZakupka());
//            sail.setPrice(originalSailState.getPrice());
//            sail.setKolichestvo(originalSailState.getKolichestvo());
//            sail.setStatus(originalSailState.getStatus());
//            sail.setOplata(originalSailState.getOplata());
//        }
//    }
//}

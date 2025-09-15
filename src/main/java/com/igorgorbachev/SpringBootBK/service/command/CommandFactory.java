//package com.igorgorbachev.SpringBootBK.service.command;
//
//import com.igorgorbachev.SpringBootBK.entity.Sail;
//
//import com.igorgorbachev.SpringBootBK.service.SailService;
//import com.igorgorbachev.SpringBootBK.service.command.impl.AddSailCommand;
//import com.igorgorbachev.SpringBootBK.service.command.impl.EditSailCommand;
//import com.igorgorbachev.SpringBootBK.service.command.impl.DeleteSailCommand;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//
//
//@Component
//public class CommandFactory {
//    private final ApplicationContext applicationContext;
//
//
//    public CommandFactory(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }
//
//    public Command createAddSailCommand(SailService sailService, Sail sail, Long klientId) {
//        AddSailCommand command = applicationContext.getBean(AddSailCommand.class);
//        command.initialize(sailService, sail, klientId);
//        return command;
//    }
//
//    public Command createEditSailCommand(SailService sailService, Sail sail, Long statusId, Long oplataId,
//                                         String nameSail, String articul,
//                                         BigDecimal zakupka, BigDecimal price, BigDecimal kolichestvo) {
//        EditSailCommand command = applicationContext.getBean(EditSailCommand.class);
//        command.initialize(sailService, sail, statusId, oplataId,
//                nameSail, articul, zakupka, price, kolichestvo);
//        return command;
//    }
//
//    public Command createDeleteSailCommand(SailService sailService, Long sailId) {
//        DeleteSailCommand command = applicationContext.getBean(DeleteSailCommand.class);
//        command.initialize(sailService, sailId);
//        return command;
//    }
//}

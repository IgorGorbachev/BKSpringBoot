package com.igorgorbachev.SpringBootBK.rest;

import com.igorgorbachev.SpringBootBK.entity.Oplata;
import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.entity.Status;
import com.igorgorbachev.SpringBootBK.service.OplataService;
import com.igorgorbachev.SpringBootBK.service.SailService;
import com.igorgorbachev.SpringBootBK.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SailRestController {

    private final SailService sailService;
    private final StatusService statusService;
    private final OplataService oplataService;


    @PostMapping("/changeSailStatus")
    @ResponseBody
    public ResponseEntity<String> changeSailStatus(@RequestParam Long sailId,
                                                   @RequestParam Long statusId) {
        try {
            Sail sail = sailService.getSailById(sailId);
            Status status = statusService.getStatusById(statusId);
            sail.setStatus(status);
            sailService.changeSail(sail,
                    sail.getStatus().getId(),
                    sail.getOplata().getId(),
                    sail.getNameSail(),
                    sail.getArticul(),
                    sail.getZakupka(),
                    sail.getPrice(),
                    sail.getKolichestvo());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/changeSailOplata")
    @ResponseBody
    public ResponseEntity<String> changeSailOplata(@RequestParam Long sailId,
                                                   @RequestParam Long oplataId) {
        try {
            Sail sail = sailService.getSailById(sailId);
            Oplata oplata = oplataService.findOplatasById(oplataId);
            sail.setOplata(oplata);
            sailService.changeSail(sail,
                    sail.getStatus().getId(),
                    sail.getOplata().getId(),
                    sail.getNameSail(),
                    sail.getArticul(),
                    sail.getZakupka(),
                    sail.getPrice(),
                    sail.getKolichestvo());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

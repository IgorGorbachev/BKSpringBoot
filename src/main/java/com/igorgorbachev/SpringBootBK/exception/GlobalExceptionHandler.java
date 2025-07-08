package com.igorgorbachev.SpringBootBK.exception;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalStateException(Exception exception){
        System.out.println("Исключение поймано: " + exception.getMessage());
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        return modelAndView;
    }

}

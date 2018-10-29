package com.xskr.onw.wxs.web;

import com.xskr.onw.wxs.core.Hall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/onw/hall")
public class HallController {

    @Autowired
    private Hall hall;

    @RequestMapping("/create")
    public int create(){
        return hall.create();
    }

    @RequestMapping("/join/{openID}/{userName}/{roomID}")
    public boolean join(@PathVariable String openID, @PathVariable String userName, @PathVariable int roomID){
        return hall.join(openID, userName, roomID);
    }

    @RequestMapping("/leave/{openID}/{roomID}")
    public boolean leave(@PathVariable String openID, @PathVariable int roomID){
        return hall.leave(openID, roomID);
    }
}

package com.xskr.onw.wxs.web;

import com.xskr.onw.wxs.core.Hall;
import com.xskr.onw.wxs.core.WxUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/onw/hall")
public class HallController {

    @Autowired
    private Hall hall;

    @RequestMapping("/create")
    public int create(){
        return hall.create();
    }

    @RequestMapping(path = "/join", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public boolean join(@RequestBody Map<String, String> param){
        String openid = param.get("openid");
        String nickName = param.get("nickName");
        String avatarUrl = param.get("avatarUrl");
        int roomID = Integer.parseInt(param.get("roomID"));
        WxUser wxUser = new WxUser(openid, nickName, avatarUrl);
        return hall.join(wxUser, roomID);
    }

    @RequestMapping("/leave/{openid}/{roomID}")
    public boolean leave(@PathVariable String openid, @PathVariable int roomID){
        return hall.leave(openid, roomID);
    }
}

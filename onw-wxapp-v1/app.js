//app.js
// var stompClient;
App({
  onLaunch: function () {
    var that = this;
    // 登录
    wx.login({
      success: res => {
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
        //console.log(res)
        wx.request({
          url: 'https://api.weixin.qq.com/sns/jscode2session?appid=wxf74e7028726891bd&secret=60c570f7a4343d9645c6ba44be2baad2&js_code=' + res.code + '&grant_type=authorization_code',
          data: {},
          method: 'GET', // OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT  
          // header: {}, // 设置请求的 header  
          success: function (res) {
            // var obj = {};
            // obj.openid = res.data.openid;
            // obj.expires_in = Date.now() + res.data.expires_in;
            //console.log(res);
            wx.setStorageSync('openid', res.data.openid);//存储openid
            // that.globalData.openid = res.data.openid;
            console.log("set openID: " + res.data.openid);
          }
        });
      }
    })
    // 获取用户信息
    wx.getSetting({
      success: res => {
        if (res.authSetting['scope.userInfo']) {
          // 已经授权，可以直接调用 getUserInfo 获取头像昵称，不会弹框
          wx.getUserInfo({
            success: res => {
              // 可以将 res 发送给后台解码出 unionId
              this.globalData.userInfo = res.userInfo
              console.log(res);
              wx.setStorageSync('nickName', res.userInfo.nickName);

              // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
              // 所以此处加入 callback 以防止这种情况
              if (this.userInfoReadyCallback) {
                this.userInfoReadyCallback(res)
              }
            }
          })
        }
      } 
    })
    // this.initSocket();
  },
  globalData: {
    userInfo: null,
    stompClient: undefined,
    httpAPI: 'https://192.168.0.106:8443/onw',
    wssAPI: 'wss://192.168.0.106:8443/onw/endpoint',
  }
  // initSocket: function () {
  //   var that = this;
  //   var socketOpen = false

  //   function sendSocketMessage(msg) {
  //     console.log('send msg:' + msg)
  //     if (socketOpen) {
  //       wx.sendSocketMessage({
  //         data: msg
  //       })
  //     } else {
  //       //todo ?
  //       socketMsgQueue.push(msg)
  //     }
  //   }

  //   var ws = {
  //     send: sendSocketMessage
  //   }

  //   wx.connectSocket({
  //     url: that.globalData.wssAPI 
  //   })
  //   wx.onSocketOpen(function (res) {
  //     socketOpen = true
  //     ws.onopen()
  //   })

  //   wx.onSocketMessage(function (res) {
  //     console.log(res)
  //     ws.onmessage(res)
  //   })

  //   if(that.stompClient == null){
  //     var Stomp = require('utils/stomp.min.js').Stomp;
  //     Stomp.setInterval = function () { }
  //     Stomp.clearInterval = function () { }
  //     that.globalData.stompClient = Stomp.over(ws);

  //     that.globalData.stompClient.connect({}, function (sessionId) {

  //       let openid = wx.getStorageSync('openid');
  //       // let openid = that.globalData.openid;
  //       console.log('openid = ' + JSON.stringify(openid));

  //       //subscribe topic
  //       that.globalData.stompClient.subscribe('/topic/sendTo0', function (body, headers) {
  //         console.log('From MQ /topic/greetings:', body);   
  //       }); 
  //       that.globalData.stompClient.send('/messageMapping0', { 'openid': openid }, "I'm topic!");  

  //       // subscribe queue  
  //       that.globalData.stompClient.subscribe('/user/' + openid + '/message', function (body, headers) {
  //         wx.vibrateLong()
  //         console.log('From MQ to user /user/' + openid + '/message:', body);
  //       });
  //       that.globalData.stompClient.send('/messageMapping1', { 'openid': openid }, "I'm queue!");
  //     }) 
  //   // send message to fire topic
  //   }
  // }
})
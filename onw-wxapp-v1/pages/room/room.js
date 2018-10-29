// pages/room/room.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    //服务器返回的房间信息
    room: undefined,
    //websocket客户端
    stompClient: undefined,
    //广播订阅
    subscribedTopic: undefined,
    //点对点订阅
    subscribedMessage: undefined,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var roomID = wx.getStorageSync('roomID');
    console.log("ROOM_ID: " + roomID);
    //设定本页面中标题栏显示房间号
    wx.setNavigationBarTitle({
      title: '房间号 ' + roomID,
    })
    this.initSocket();
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {
    //卸载ws订阅
    var that = this;

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {

  },
  bindtapExit: function(){
    if(this.data.subscribedTopic != null){
      this.data.subscribedTopic.unsubscribe();
      console.info("Unsubscribe topic: " + this.data.subscribedTopic);
    }
    if(this.data.subscribedMessage != null){
      this.data.subscribedMessage.unsubscribe();
      console.info("Unsubscribe message: " + this.data.subscribedMessage);
    } 
    if(this.data.stompClient != null){
      this.data.stompClient.disconnect(function () {
        console.info("Disconnect stompClient: ");
        var openid = wx.getStorageSync('openid');
        var roomID = wx.getStorageSync('roomID');
        wx.request({
          url: getApp().globalData.httpAPI + '/hall/leave/' + openid + '/' + roomID,
          success (res) {
            wx.navigateTo({
              url: '../index/index',
            })
            wx.setStorageSync('roomID', undefined);
          },
          fail (res) {
            wx.navigateTo({
              url: '../index/index',
            })
          }
        })
      })
    }
  },
  initSocket: function () {
    var that = this;
    var socketOpen = false;
    var socketMsgQueue = []
    var ws = {
      // send: sendSocketMessage
      send: function (msg) {
        console.log('send msg:' + msg)
        if (socketOpen) {
          wx.sendSocketMessage({
            data: msg
          })
        } else {
          //todo ?
          socketMsgQueue.push(msg)
        }
      },
      close: function(){

      }
    }

    wx.connectSocket({
      url: getApp().globalData.wssAPI
    })
    
    wx.onSocketOpen(function (res) {
      socketOpen = true
      console.log(res);
      ws.onopen();
    })

    wx.onSocketMessage(function (res) {
      console.log(res);
      ws.onmessage(res);
    })

    wx.onSocketClose(function (res) {
      console.log('WebSocket 已关闭！');
      socketOpen = false;
      setTimeout(function(){
        wx.connectSocket({
          url: getApp().globalData.wssAPI
        })
      }, 2000);
    })

    if (that.stompClient == null) {
      var Stomp = require('../../utils/stomp.min.js').Stomp;
      Stomp.setInterval = function () { }
      Stomp.clearInterval = function () { }
      that.data.stompClient = Stomp.over(ws);

      that.data.stompClient.connect({}, function (sessionId) { 

        let openid = wx.getStorageSync('openid');
        let roomID = wx.getStorageSync('roomID');
        // let openid = getApp().globalData.openid;
        console.log('openid = ' + JSON.stringify(openid));

        // subscribe topic
        that.data.subscribedTopic = that.data.stompClient.subscribe('/onw/room/topic/' + roomID, function (body, headers) {
          console.log('From::::::::: /topic/' + roomID);
          console.log('head: ' + JSON.stringify(headers));
          console.log('body: ' + JSON.stringify(body));
        }); 
        // that.data.stompClient.send('/messageMapping0', { 'openid': openid }, "I'm topic!");  

        // subscribe queue 
        that.data.subscribedMessage = that.data.stompClient.subscribe('/user/' + openid + '/message', function (content, headers) {
          console.log('From:::::::::: /user/' + openid + '/message:', content);
          var xskrMessage = JSON.parse(content.body);
          console.log(xskrMessage.action);
          if(xskrMessage.action == 'ROOM_CHANGED'){
            that.setData({
              room: xskrMessage.data
            });
          }
          console.log(that.data.room);
        });
        that.data.stompClient.send('/onw/room/info', { 'openid': openid, 'roomID': roomID}, "Request room infomation.");
      })
      //send message to fire topic
    }
  }
})
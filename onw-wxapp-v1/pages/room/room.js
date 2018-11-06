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
    //socket的连接状态
    socketOpen: false,
    //socket被期待的连接状态
    socketNeedOpen: true,
    //广播订阅
    subscribedTopic: undefined,
    //点对点订阅
    subscribedMessage: undefined,
    //消息框标题
    messageTitleText: '消息 ▼',
    //消息内容隐藏
    messageContentViewVisibility: 'visible',
    operateTabViewVisibility: 'visible',
    summaryTabViewVisibility: 'hidden',
    detailTabViewVisibility: 'hidden',
    //openid
    openid: undefined
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var roomID = wx.getStorageSync('roomID');
    this.setData({
      openid: wx.getStorageSync('openid')
    });
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
    console.log('onUnload');
    var that = this;
    this.socketNeedOpen = false;

    if (this.data.subscribedTopic != null) {
      this.data.subscribedTopic.unsubscribe();
      console.info("Unsubscribe topic: " + this.data.subscribedTopic);
    }
    if (this.data.subscribedMessage != null) {
      this.data.subscribedMessage.unsubscribe();
      console.info("Unsubscribe message: " + this.data.subscribedMessage);
    }
    if (this.data.stompClient != null) {
      this.data.stompClient.disconnect(function () {
        console.info("Disconnect stompClient: ");
        var openid = wx.getStorageSync('openid');
        var roomID = wx.getStorageSync('roomID');
        wx.request({
          url: getApp().globalData.httpAPI + '/hall/leave/' + openid + '/' + roomID
        })
        wx.setStorageSync('roomID', undefined);
      })
    }
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

  },
  bindtapRoleCard: function (event){
    var openid = wx.getStorageSync('openid');
    var roomID = wx.getStorageSync('roomID');
    var roleCardIDString = event.target.id.substring('roleCard_'.length);
    var roleCardID = new Number(roleCardIDString);
    console.log("click: " + roleCardID);
    this.data.stompClient.send('/onw/room/roleCard', { 'openid': openid, 'roomID': roomID, 'roleCardID': roleCardID }, "Click card card.");
  },
  bindtapReady: function(){
    var openid = wx.getStorageSync('openid');
    var roomID = wx.getStorageSync('roomID');

    this.data.stompClient.send('/onw/room/ready', { 'openid': openid, 'roomID': roomID }, "Request ready.");
  },
  bindtapDesktopCard: function(event){
    var openid = wx.getStorageSync('openid');
    var roomID = wx.getStorageSync('roomID');
    var desktopCardIDString = event.currentTarget.id.substring('desktopCard_'.length);
    var desktopCardID = new Number(desktopCardIDString);
    console.log(event);
    console.log("click desktop card: " + desktopCardID);
    this.data.stompClient.send('/onw/room/desktopCard', { 'openid': openid, 'roomID': roomID, 'desktopCardID': desktopCardID }, "Click desktop card.");
  },
  bindtapMessageTitleText: function(event){
    var messageTitleText = event.target.id;
    console.log(messageTitleText)
    if (this.data.messageTitleText == '消息 ▼'){
      this.setData({
        messageTitleText: '消息 ◀',
        messageContentViewVisibility: 'hidden'
      })
    } else{
      this.setData({
        messageTitleText: '消息 ▼',
        messageContentViewVisibility: 'visible'
      })
    }
  },
  bindtapSeat: function(event){
    console.log(event);
    var openid = wx.getStorageSync('openid');
    var roomID = wx.getStorageSync('roomID');
    var seatIDString = event.target.id.substring('seat_'.length);
    var seatID = new Number(seatIDString);
    console.log("click seat: " + seatID);
    this.data.stompClient.send('/onw/room/seat', { 'openid': openid, 'roomID': roomID, 'seatID': seatID }, "Click seat.");
  },
  initSocket: function () {
    var that = this;
    this.socketNeedOpen = true;
    var socketMsgQueue = []
    var ws = {
      // send: sendSocketMessage
      send: function (msg) {
        console.log('send msg:' + msg)
        if (that.socketOpen) {
          if(socketMsgQueue.length > 0){
            for(var idx in socketMsgQueue){
              wx.sendSocketMessage({
                data: socketMsgQueue[idx]
              })
            }
            socketMsgQueue = []
          }
          wx.sendSocketMessage({
            data: msg
          })
        } else {
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
      that.socketOpen = true
      console.log(res);
      ws.onopen();
    })

    wx.onSocketMessage(function (res) {
      console.log(res);
      ws.onmessage(res);
    })

    wx.onSocketClose(function (res) {
      console.log('WebSocket 已关闭！');
      that.socketOpen = false;
      if(that.socketNeedOpen){
        setTimeout(function () {
          wx.connectSocket({
            url: getApp().globalData.wssAPI
          })
        }, 2000);
      }
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
        that.data.subscribedTopic = that.data.stompClient.subscribe('/topic/' + roomID, function (content, headers) {
          console.log('From::::::::: /topic/' + roomID);
          console.log(content.body);
          var xskrMessage = JSON.parse(content.body);
          if (xskrMessage.action == 'ROOM_CHANGED') {
            that.setData({
              room: xskrMessage.data
            });
          }
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

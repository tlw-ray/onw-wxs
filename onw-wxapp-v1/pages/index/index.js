//index.js
//获取应用实例
const app = getApp();
const httpAPI = app.globalData.httpAPI;
const util = require('../../utils/util.js')

Page({
  data: {
    userInfo: {},
    hasUserInfo: false,
    canIUse: wx.canIUse('button.open-type.getUserInfo'),
    message: "",
    join_room_id: undefined
  },
  //事件处理函数
  bindCreateRoomTap: function() {
    var that = this;
    wx.request({
      //创建房间
      url: httpAPI + '/hall/create/',
      success (res) {
        console.log("Created room ID: " + res.data);
        //加入房间
        that.joinRoom(res.data);
      }
    })
  },
  bindJoinRoomInput: function (e) {
    this.setData({
      'join_room_id': e.detail.value
    });
  },
  bindJoinRoomTap: function (e) {
    if (util.isNumeric(this.data.join_room_id)){
      this.joinRoom(this.data.join_room_id);
    }else{
        this.setData({
          message: "请输入房间号!"
        })
    }
  },
  joinRoom: function(roomID){
    var that = this;
    var openid = wx.getStorageSync('openid');
    var nickName = wx.getStorageSync('nickName');
    wx.request({
      //加入房间
      url: httpAPI + '/hall/join/' + openid + '/' + nickName + '/' + roomID,
      success(res) {
        if(res.data){
          //记录房间号
          wx.setStorageSync('roomID', roomID);
          //页面显示转跳至房间
          wx.navigateTo({
            url: '../room/room'
          })
        }else{
          that.setData({
            message: '加入房间失败!'
          })
        }
      }
    })
  },
  onLoad: function () {
    if (app.globalData.userInfo) {
      this.setData({
        userInfo: app.globalData.userInfo,
        hasUserInfo: true
      })
    } else if (this.data.canIUse){
      // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
      // 所以此处加入 callback 以防止这种情况
      app.userInfoReadyCallback = res => {
        this.setData({
          userInfo: res.userInfo,
          hasUserInfo: true
        })
      }
    } else {
      // 在没有 open-type=getUserInfo 版本的兼容处理
      wx.getUserInfo({
        success: res => {
          app.globalData.userInfo = res.userInfo
          this.setData({
            userInfo: res.userInfo,
            hasUserInfo: true
          })
        }
      })
    }
  },
  getUserInfo: function(e) {
    console.log(e)
    app.globalData.userInfo = e.detail.userInfo
    this.setData({
      userInfo: e.detail.userInfo,
      hasUserInfo: true
    })
  }
})

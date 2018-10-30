// pages/create/seat/seat.js
Component({
  /**
   * 组件的属性列表
   */
  properties: {
    seatNumber: {
      type: String,
      value: '01'
    },
    guessCard: {
      type: String,
      value: '?'
    },
    playerName: {
      type: String,
      value: 'VINCENT'
    },
    playerReady:{
      type: Boolean,
      value: false
    },
    seatEnabled:{
      type: Boolean,
      value: true
    }
    //TODO 需要不可点选、可选、被选择等若干状态
  },

  /**
   * 组件的初始数据
   */
  data: {

  },

  /**
   * 组件的方法列表
   */
  methods: {

  }
})

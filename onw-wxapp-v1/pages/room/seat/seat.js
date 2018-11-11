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
    playerName: {
      type: String,
      value: undefined
    },
    playerIcon: {
      type: String,
      value: undefined
    },
    seat: {
      type: Object,
      value: undefined
    },
    room: {
      type: Object,
      value: undefined
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
    seatID: function(){
      return getID();
      // return new Number(id.substring("seat_".length));
    }
  }
})

package com.xskr.onw.wxs.core;

import com.alibaba.fastjson.JSON;
import com.xskr.onw.wxs.core.card.Card;
import com.xskr.onw.wxs.core.message.OnwMessage;
import com.xskr.onw.wxs.core.message.SeatMessage;
import com.xskr.onw.wxs.stomp.RoomController;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

//一号座位为房主位

public class Room {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String KEY_EVENT = "event";
    public static final String KEY_DATE = "date";
    public static final String KEY_MESSAGE = "message";

    //桌上三张牌
    public static final Range<Integer> DESKTOP_CARD_RANGE = Range.between(0, 2);
    static final int TABLE_DECK_THICKNESS = 3;
    //支持12个玩家
    public static final Range<Integer> PLAYER_RANGE = Range.between(0, 11);
    private static final int MAX_SEAT = 12;

    private CardFactory cardFactory = new CardFactory();

    //房间号
    private int id;
    // 该房间支持的所有卡牌被选中的状态
    private boolean[] cardPicked = new boolean[Card.ROLE_COUNT];
    // 房间内的座位
    private List<Seat> seats = new ArrayList(MAX_SEAT);
    //进入房间还但没有座位的玩家
    private Set<String> observers = new TreeSet();
    // 发牌后剩余的桌面3张牌垛
    private TreeMap<Integer, Card> desktopCards = new TreeMap();

    // 房主用户名
    private Map<String, WxUser> openidUserMap = new HashMap();

    // 本房间一场比赛的操作记录
    private List<Object> operations = new ArrayList();

    // 玩家夜间操作
    private Integer singleWolfCheckDesktopCard;
    private Integer seerCheckDesktopCard1;
    private Integer seerCheckDesktopCard2;
    private Integer seerCheckPlayerSeat;
    private Integer robberSnatchPlayerSeat;
    private Integer troublemakerExchangePlayerSeat1;
    private Integer troublemakerExchangePlayerSeat2;
    private Integer drunkExchangeDesktopCard;
    private boolean hunterVote = false;

    //游戏是否处于开始状态，如果是就不能再进行准备切换
    private Scene scene = Scene.PREPARE;

    //用于发送WebSocket信息
    private SimpMessagingTemplate simpMessagingTemplate;

    private Date date = new Date();

    public Room(int id){
        logger.debug("new Room(id={})", id);
        this.id = id;
        //默认初始卡牌
        this.pickRoleCards(cardFactory.WOLF_0, cardFactory.MINION, cardFactory.MASON_0, cardFactory.MASON_1);
        Set<Card> pickedCards = getPickedCards();
        for(int i=0;i<MAX_SEAT;i++){
            Seat seat = new Seat();
            seat.setEnable(i<pickedCards.size());
            seats.add(seat);
        }
        refreshSeatEnable();
    }

    /**
     * 获得可用的座位数
     * @return
     */
    public int getAvailableSeatCount(){
        return getPickedCards().size() - TABLE_DECK_THICKNESS;
    }

    /**
     * 玩家进入房间
     * @param wxUser
     * @return 返回房间信息，用来区分断线重连
     */
    public void join(WxUser wxUser){
        String openid = wxUser.getOpenid();
        String userName = wxUser.getNickName();
        logger.debug("join(openID = {}, userName = {})", openid, userName);
        openidUserMap.put(openid, wxUser);
        //游戏在不同状态有人加入
        if(scene == Scene.PREPARE){
            //加入准备状态的房间,默认行为是找一个空座位坐下
            boolean seated = false;
            for(int i=0;i<getAvailableSeatCount();i++){
                Seat seat = getSeats().get(i);
                if(seat.getOpenid() == null){
                    seat.setOpenid(openid);
                    seated = true;
                    break;
                }
            }
            //如果没有空座位了就加入observer
            if(!seated){
                observers.add(userName);
            }else{
                //do nothing
            }
            //告知大家新玩家进入和坐下，座位状态变化了
            OnwMessage roomChangedMessage = new OnwMessage("Seat changed...", ClientAction.ROOM_CHANGED, this);
            sendMessage(roomChangedMessage);
        }else{
            //游戏在进行中有人加入
            Seat oldUserSeat = getSeatByOldUserName(userName);
            if(oldUserSeat != null){
                //玩家之前从座位上离开了，现在回到了座位，断线重连
                oldUserSeat.setOpenid(userName);
                //告知所有玩家xxx回来了
                String message = String.format("%s回来了", userName);
                OnwMessage onwMessage = new OnwMessage(message, ClientAction.ROOM_CHANGED, this);
                sendMessage(onwMessage);
//                //告知自己接下来该做的操作
//                OnwMessage reconnectonwMessage = new OnwMessage(null, ClientAction.RECONNECT, this);
//                sendMessage(userName, reconnectonwMessage);
            }else{
                //加入observer
                observers.add(userName);
                OnwMessage onwMessage = new OnwMessage("Join observers", ClientAction.ROOM_CHANGED, this);
                sendMessage(userName, onwMessage);
            }
        }
    }

    /**
     * 玩家离开房间
     * 普通游戏进行时应该不允许玩家离开房间，但桌游可能有玩家离开后由observer加入到游戏中来接替他
     * @param userName
     */
    public void leave(String userName){
        logger.debug("leave(userName = {})", userName);
        if(observers.remove(userName)){
            //从观看者中移除

        }else{
            //从座位上移除该玩家
            Seat playerSeat = getSeatByPlayerName(userName);
            if(playerSeat != null) {
                playerSeat.setOpenid(null);
                String message = String.format("%s离开房间", userName);
                OnwMessage roomChangedMessage = new OnwMessage(message, ClientAction.ROOM_CHANGED, this);
                sendMessage(roomChangedMessage);
            }else{
                String message = String.format("%s号房间内不存在玩家%s.", id, userName);
                logger.error(message);
            }
        }
//        OnwMessage roomChangedMessage = new OnwMessage(null, ClientAction.LEAVE_ROOM, this);
//        sendMessage(userName, roomChangedMessage);
    }

    /**
     * 玩家坐到指定位置, 当所有玩家都准备好时游戏自动开始
     * TODO 或许应该加个倒数5,4,3,2,1
     * @param openid 玩家
     * @return 该玩家最终处于的ready状态
     */
    public void switchReady(String openid){
        if(scene == Scene.ACTIVATE || scene == Scene.VOTE) {
            //如果已经开始或正在投票则不可改变ready状态
        }else if(scene == Scene.PREPARE){
            Seat seat = getSeatByPlayerName(openid);
            if(seat != null) {
                //玩家准备状态改变
                boolean ready = !seat.isReady();
                seat.setReady(ready);
                OnwMessage onwMessage = new OnwMessage("Player click ready.", ClientAction.ROOM_CHANGED, this);
                sendMessage(onwMessage);
                System.out.println(JSON.toJSONString(this, true));
                if(ready) {
                    //检查是否能够触发游戏开始事件
                    //如果玩家数量达到座位数量，且玩家都是ready状态则触发新游戏事件
                    boolean allReady = true;
                    for (int i = 0; i < getAvailableSeatCount(); i++) {
                        Seat anySeat = seats.get(i);
                        if (!anySeat.isReady() || anySeat.getOpenid() == null) {
                            allReady = false;
                            break;
                        }
                    }
                    if (allReady) {
                        newGame();
                    } else {
                        //人数未达到座位数游戏无法开始
                    }
                }else{
                    //如果玩家取消准备那么不需要检查游戏是否开始
                }
            }else{
                if(observers.contains(openid)){
                    String message = String.format("玩家%s是观看者，无法设定准备状态。", openid);
                    throw new RuntimeException(message);
                }else{
                    String message = String.format("玩家%s不在该房间，无法设定准备状态。", openid);
                    throw new RuntimeException(message);
                }
            }
        }else{
            //TODO 客户端应根据场景控制Ready按钮可用性，禁止用户发出此请求
            throw new RuntimeException("未支持的场景: " + scene);
        }
    }

    /**
     * 根据初始卡牌查询用户
     * @param card
     * @return
     */
    protected Seat getSeatByInitializeCard(Card card){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getInitializeCard() == card){
                return playerSeat;
            }
        }
        return null;
    }

    protected Seat getSeatByPlayerName(String userName){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getOpenid()!=null && playerSeat.getOpenid().equals(userName)){
                return playerSeat;
            }
        }
        return null;
    }

    //TODO 这里可以写为函数式，在getPlayer方法中增加一个过滤条件
    protected Seat getSeatByUserName(String userName){
        for(int i=0;i<MAX_SEAT;i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getOpenid()!=null && playerSeat.getOpenid().equals(userName)){
                return playerSeat;
            }
        }
        return null;
    }
    protected Seat getSeatByOldUserName(String oldUserName){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat oldUserSeat = seats.get(i);
            if(oldUserSeat.getOpenid()==null && oldUserSeat.getOldUserName().equals(oldUserName)){
                return oldUserSeat;
            }
        }
        return null;
    }

    /**
     * 根据卡牌查询用户，卡牌是夜晚行动过后被交换过的
     * @param card
     * @return
     */
    protected Seat getPlayerSeatByCard(Card card){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getCard() == card){
                return playerSeat;
            }
        }
        return null;
    }

    /**
     * 初始化一局游戏
     * @return 如果开始新一局游戏返回true， 若未能开始返回false
     */
    private void newGame(){
//        //如果有座位空着或者座位上的玩家不在ready状态则无法开始新游戏
//        for(int i=0;i<getAvailableSeatCount();i++){
//            Seat playerSeat = seats.get(i);
//            if(playerSeat.getOpenid() == null){
//                String message = String.format("%s号座位没有玩家，游戏无法开始。", i);
//                OnwMessage onwMessage = new OnwMessage(message, null, null);
//                sendMessage(onwMessage);
//                return;
//            }else if(!playerSeat.isReady()){
//                String message = String.format("%s号座位玩家未进入准备状态，游戏无法开始。", i);
//                OnwMessage onwMessage = new OnwMessage(message, null, null);
//                sendMessage(onwMessage);
//                return;
//            }else{
//                //do nothing
//            }
//        }

        //标记进入游戏状态
        scene = Scene.ACTIVATE;
        logger.debug("newGame()");
        //清空结局数据
        for(int i=0;i<getAvailableSeatCount();i++){
            seats.get(i).reset();
        }
        this.operations.clear();

        Deck deck = new Deck(getPickedCards().toArray(new Card[0]));

        //清空上一局所有角色的操作状态
        singleWolfCheckDesktopCard = null;
        seerCheckDesktopCard1 = null;
        seerCheckDesktopCard2 = null;
        seerCheckPlayerSeat = null;
        robberSnatchPlayerSeat = null;
        troublemakerExchangePlayerSeat1 = null;
        troublemakerExchangePlayerSeat2 = null;
        drunkExchangeDesktopCard = null;

        //洗牌
        deck.shuffle(200);
        System.out.println(Arrays.toString(deck.getCards()));

        //为所有人发牌，清空玩家状态
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            Card card = deck.deal();
            playerSeat.setCard(card);
            playerSeat.setInitializeCard(card);
            playerSeat.setVoteSeat(null);
        }

        //建立桌面剩余的牌垛
        desktopCards.put(0, deck.deal());
        desktopCards.put(1, deck.deal());
        desktopCards.put(2, deck.deal());

        logger.debug("playerAction");
        //需要主动行动的玩家
        Seat singleWolfSeat = getSingleWolfSeat();
        Seat seerSeat = getSeatByInitializeCard(cardFactory.SEER);
        Seat robberSeat = getSeatByInitializeCard(cardFactory.ROBBER);
        Seat troublemakerSeat = getSeatByInitializeCard(cardFactory.TROUBLEMAKER);
        Seat drunkSeat = getSeatByInitializeCard(cardFactory.DRUNK);

        //发牌结束后根据身份为每个玩家发送行动提示信息
        boolean directVote = true;
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            String message;
            ClientAction clientAction = null;
            if(playerSeat == singleWolfSeat){
                message = String.format("请点选牌1、牌2、牌3中的一张。");
                clientAction = ClientAction.SINGLE_WOLF_ACTION;
                directVote = false;
            }else if (playerSeat == seerSeat) {
                message = "请点选牌1、牌2、牌3中的任意两个，或者一位玩家。";
                clientAction = ClientAction.SEER_ACTION;
                directVote = false;
            }else if(playerSeat == robberSeat){
                message = "请输点选任意玩家，查阅其卡牌并交换身份。";
                clientAction = ClientAction.ROBBER_ACTION;
                directVote = false;
            }else if(playerSeat == troublemakerSeat){
                message = "请点选除您之外两个玩家，交换他们的身份。";
                clientAction = ClientAction.TROUBLEMAKER_ACTION;
                directVote = false;
            }else if(playerSeat == drunkSeat){
                message = "请点选牌1、牌2、牌3中任意一张，与之交换身份。";
                clientAction = ClientAction.DRUNK_ACTION;
                directVote = false;
            }else{
                message = "请稍候...";
            }
            //预备玩家身份和操作信息
            String firstMessage = String.format("初始身份:%s。\n" + message, playerSeat.getCard().getDisplayName());
            SeatMessage seatMessage = new SeatMessage(firstMessage, null, null);
            keepKeyMessage(playerSeat, seatMessage);
        }
        //向玩家发送身份和操作提示信息
        OnwMessage onwMessage = new OnwMessage("New Game...", ClientAction.ROOM_CHANGED, this);
        sendMessage(onwMessage);
        System.out.println(JSON.toJSONString(this, true));
        if(directVote){
            //没有任何玩家需要行动，直接进入投票阶段
//            Random random = new Random();
            //随机等待约10秒，至少3秒，模拟有人在行动的情况
//            long span = 1000 + random.nextInt(1000);
//            try {
//                Thread.sleep(span);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    attemptNightAction();
                }
            };
            //这里等待后触发，多线程是否有问题
            Timer timer = new Timer();
            timer.schedule(timerTask, 1000);
        }else{
            // do nothing
        }
    }

    public TreeMap<Integer, Card> getDesktopCards() {
        return desktopCards;
    }

    //是否能够进入白天，当所有需要操作的玩家行动完毕才能进入白天
    private boolean canNightAction(){
        logger.debug("canNightAction()");

        // 检查孤狼是否已经行动
        if(getSingleWolfSeat() != null){
            //如果存在孤狼的角色
            if(singleWolfCheckDesktopCard == null){
                //如果孤狼没有指定要看的桌面上的一张牌
                logger.debug("singleWolf not work!");
                return false;
            }
        }

        //检查预言家是否已经行动
        if(getSeatByInitializeCard(cardFactory.SEER) != null){
            //如果存在预言家角色
            if((seerCheckDesktopCard1 == null || seerCheckDesktopCard2 == null) &&
                    seerCheckPlayerSeat == null){
                //如果预言家既没有指定要看桌面上的那两张牌，也没有指定要验证的玩家的身份
                logger.debug("seer not work!");
                return false;
            }
        }

        // 检查强盗是否已经行动
        if(getSeatByInitializeCard(cardFactory.ROBBER) != null){
            //如果存在强盗玩家
            if(robberSnatchPlayerSeat == null){
                // 如果强盗没有指定要交换的位置
                logger.debug("robber not work!");
                return false;
            }
        }

        //检查捣蛋鬼是否已经行动
        if(getSeatByInitializeCard(cardFactory.TROUBLEMAKER) != null){
            //如果存在捣蛋鬼玩家
            if(troublemakerExchangePlayerSeat1 == null
                    || troublemakerExchangePlayerSeat2 == null){
                //如果捣蛋鬼没有指定要交换的位置
                logger.debug("troublemaker not work!");
                return false;
            }
        }

        //检查酒鬼是否行动
        if(getSeatByInitializeCard(cardFactory.DRUNK) != null){
            if(drunkExchangeDesktopCard == null){
                logger.debug("drunk not work!");
                return false;
            }
        }

        //通过了所有的检查
        return true;
    }

    //获得孤狼玩家
    protected Seat getSingleWolfSeat(){
        Seat wolf1 = getSeatByInitializeCard(cardFactory.WOLF_0);
        Seat wolf2 = getSeatByInitializeCard(cardFactory.WOLF_1);
        if(wolf1 == null && wolf2 != null){
            return wolf2;
        }else if(wolf1 != null && wolf2 == null){
            return wolf1;
        }else{
            return null;
        }
    }

    //夜间行动: 如果所有玩家均已声明行动完毕，开始真正处理玩家们的行动，处理所有流程并发布最新的信息，否则什么也不做
    protected void attemptNightAction(){
        logger.debug("attemptNightAction()");
        if(canNightAction()){
//          Seat doopelganger = initializeCardPlayerMap.get(Card.DOPPELGANGER);
            Seat singleWolfSeat = getSingleWolfSeat();
            Seat wolf1Seat = getSeatByInitializeCard(cardFactory.WOLF_0);
            Seat wolf2Seat = getSeatByInitializeCard(cardFactory.WOLF_1);
            Seat minionSeat = getSeatByInitializeCard(cardFactory.MINION);
            Seat meson1Seat = getSeatByInitializeCard(cardFactory.MASON_0);
            Seat meson2Seat = getSeatByInitializeCard(cardFactory.MASON_1);
            Seat seerSeat = getSeatByInitializeCard(cardFactory.SEER);
            Seat robberSeat = getSeatByInitializeCard(cardFactory.ROBBER);
            Seat troublemakerSeat = getSeatByInitializeCard(cardFactory.TROUBLEMAKER);
            Seat drunkSeat = getSeatByInitializeCard(cardFactory.DRUNK);
            Seat insomniacSeat = getSeatByInitializeCard(cardFactory.INSOMNIAC);
            //下面的ClientAction可能是投票
            //狼的回合
            if(singleWolfSeat != null){
                //场面上是一头孤狼
                String message = String.format("查看桌面上第%s张牌是%s", singleWolfCheckDesktopCard + 1, desktopCards.get(singleWolfCheckDesktopCard).getDisplayName());
                SeatMessage seatMessage = new SeatMessage(message, null, null);
                keepKeyMessage(singleWolfSeat, seatMessage);
                String operation = "[孤狼]: " + getNickName(singleWolfSeat) + message;
                operations.add(operation);
            }else if(wolf1Seat != null && wolf2Seat != null){
                //有两个狼玩家
                //TODO 根据座位号排序
                String message = String.format("狼人是%s号玩家%s和%s号玩家%s.",
                        getLocation(wolf1Seat), getNickName(wolf1Seat),
                        getLocation(wolf2Seat), getNickName(wolf2Seat));
                SeatMessage wolfMessage = new SeatMessage(message, null, null);
                keepKeyMessage(wolf1Seat, wolfMessage);
                keepKeyMessage(wolf2Seat, wolfMessage);
                String operation = String.format("[狼人]: 狼人是%s和%s", getNickName(wolf1Seat), getNickName(wolf2Seat));
                operations.add(operation);
            }else if(wolf1Seat == null && wolf2Seat == null){
                //场面上没有狼，不需要给任何狼发消息
                operations.add("[狼人]: 场上无狼");
            }

            // 爪牙的回合
            if(minionSeat != null){
                String message;
                if(wolf1Seat != null && wolf2Seat != null){
                    //双狼
                    message = String.format("看到两头狼，%s号玩家%s和%s号玩家%s。", getLocation(wolf1Seat),
                            getNickName(wolf1Seat), getLocation(wolf2Seat), getNickName(wolf2Seat));
                }else if(singleWolfSeat != null){
                    //孤狼
                    message = String.format("看到了一头孤狼，%s号玩家%s。",
                            getLocation(singleWolfSeat), getNickName(singleWolfSeat));
                }else{
                    //无狼
                    message = "场面上没有狼。";
                }
                SeatMessage seatMessage = new SeatMessage(message, null, null);
                keepKeyMessage(minionSeat, seatMessage);
                String operation = String.format("[爪牙]: %s查看狼", getNickName(minionSeat));
                operations.add(operation);
            }

            // 守夜人的回合
            if(meson1Seat == null && meson2Seat != null){
                //单守夜
                SeatMessage seatMessage = new SeatMessage("单守夜人没有同伴。", null, null);
                keepKeyMessage(meson2Seat, seatMessage);
                String operation = String.format("[守夜人]: %s没有同伴", getNickName(meson2Seat));
                operations.add(operation);
            }else if(meson1Seat != null && meson2Seat == null){
                SeatMessage seatMessage = new SeatMessage("单守夜人没有同伴。", null, null);
                keepKeyMessage(meson1Seat, seatMessage);
                String operation = String.format("[守夜人]: %s没有同伴", getNickName(meson1Seat));
                operations.add(operation);
            }else if(meson1Seat != null && meson2Seat != null){
                //双守夜
                String messageTemplate = "看到另一位守夜人，%s号玩家%s。";
                SeatMessage meson1Message = new SeatMessage(String.format(messageTemplate, getLocation(meson2Seat),
                        getNickName(meson2Seat)), null, null);
                SeatMessage meson2Message = new SeatMessage(String.format(messageTemplate, getLocation(meson1Seat),
                        getNickName(meson1Seat)), null, null);
                keepKeyMessage(meson1Seat, meson1Message);
                keepKeyMessage(meson2Seat, meson2Message);
                String operation = String.format("[守夜人]: %s和%s互认", getNickName(meson1Seat), getNickName(meson2Seat));
                operations.add(operation);
            }else{
                //无守夜
            }

            //预言家回合
            if(seerSeat != null){
                //查看一位玩家
                String message = "";
                if(seerCheckPlayerSeat != null){
                    Seat player = seats.get(seerCheckPlayerSeat);
                    message = String.format("查看%s号玩家%s的身份是: %s", getLocation(player),
                            openidUserMap.get(player.getOpenid()).getNickName(), player.getCard().getDisplayName());
                }else{
                    Card card1 = desktopCards.get(seerCheckDesktopCard1);
                    Card card2 = desktopCards.get(seerCheckDesktopCard2);
                    String card1Name = card1.getDisplayName();
                    String card2Name = card2.getDisplayName();
                    message = String.format("翻开桌上第%s和%s张卡牌，看到了%s和%s",
                            seerCheckDesktopCard1, seerCheckDesktopCard2, card1Name, card2Name);
                }
                SeatMessage seatMessage = new SeatMessage(message, null, null);
                keepKeyMessage(seerSeat, seatMessage);
                String operation = String.format("[预言家]: %s" + message, getNickName(seerSeat));
                operations.add(operation);
            }

            if(robberSeat != null){
                Seat player = seats.get(robberSnatchPlayerSeat);
                Card swapCard = player.getCard();
                player.setCard(robberSeat.getCard());
                robberSeat.setCard(swapCard);
                String message = String.format("交换了%s号玩家%s的身份牌%s。", getLocation(player),
                        getNickName(player), swapCard.getDisplayName());
                SeatMessage seatMessage = new SeatMessage(message, null, null);
                keepKeyMessage(robberSeat, seatMessage);
                String operation = String.format("[强盗]: %s交换了%s的身份牌%s",
                        getNickName(robberSeat), getNickName(player), swapCard.getDisplayName());
                operations.add(operation);
            }
            if(troublemakerSeat != null){
                Seat player1 = seats.get(troublemakerExchangePlayerSeat1);
                Seat player2 = seats.get(troublemakerExchangePlayerSeat2);
                Card swapCard = player1.getCard();
                player1.setCard(player2.getCard());
                player2.setCard(swapCard);
                String message = String.format("交换了%s号玩家%s和%s号玩家%s的身份牌。",
                        getLocation(player1), getNickName(player1), getLocation(player2), getNickName(player2));
                SeatMessage seatMessage = new SeatMessage(message, null, null);
                keepKeyMessage(troublemakerSeat, seatMessage);
                String operation = String.format("[捣蛋鬼]: %s交换了%s和%s的身份",
                        getNickName(troublemakerSeat), getNickName(player1), getNickName(player2));
                operations.add(operation);
            }
            if(drunkSeat != null){
                Card swapCard = desktopCards.get(drunkExchangeDesktopCard);
                desktopCards.put(drunkExchangeDesktopCard, drunkSeat.getCard());
                drunkSeat.setCard(swapCard);
                String message = String.format("交换了桌面第%s张牌。", drunkExchangeDesktopCard);
                SeatMessage seatMessage = new SeatMessage(message, null, null);
                keepKeyMessage(drunkSeat, seatMessage);
                String operation = String.format("[酒鬼]: %s交换了桌面第%s张牌%s",
                        getNickName(drunkSeat), drunkExchangeDesktopCard + 1, swapCard.getDisplayName());
                operations.add(operation);
            }
            if(insomniacSeat != null){
                String message, operation;
                if(insomniacSeat.getCard() == cardFactory.INSOMNIAC){
                    message = "牌没有被换过。";
                    operation = "[失眠者]: " + getNickName(insomniacSeat) + message;
                }else{
                    message = String.format("牌被换为%s。", insomniacSeat.getCard().getDisplayName());
                    operation = String.format("[失眠者]: %s看到自己的牌被换为%s",
                            getNickName(insomniacSeat), insomniacSeat.getCard().getDisplayName());
                }
                SeatMessage seatMessage = new SeatMessage(message, null, null);
                keepKeyMessage(insomniacSeat, seatMessage);
                operations.add(operation);
            }
            //ClientAction.VOTE_ACTION
            SeatMessage seatMessage = new SeatMessage("三轮讨论后请点击投票按钮。", null, null);
            keepTopicMessage(seatMessage);
            OnwMessage onwMessage = new OnwMessage("To Vote", ClientAction.ROOM_CHANGED, this);
            sendMessage(onwMessage);
            scene = Scene.VOTE;
        }
    }

    private String getNickName(Seat seat){
        return String.format("'%s'", openidUserMap.get(seat.getOpenid()).getNickName());
    }

    private void keepTopicMessage(SeatMessage seatMessage) {
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            keepKeyMessage(playerSeat, seatMessage);
        }
    }

    //接受玩家投票，计算，并公布获胜信息
    public void vote(String userName, int seat){
        logger.debug("vote(userName = {}, seat= {})", userName, seat);
        Seat playerSeat = getSeatByPlayerName(userName);
        if(playerSeat.getVoteSeat() == null) {
            playerSeat.setVoteSeat(seat);
            //票数在finishGame时统计，这里仅做投票
            if (canStatVote()) {
                finishGame();
            }
        }
    }

    //如果所有玩家都已经投票，那么可以统计投票数
    private boolean canStatVote(){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getVoteSeat() == null){
                return false;
            }
        }
        return true;
    }

    //如果统计的最高获票玩家中包含猎人，则由猎人独立投票，否则计算获胜阵营
    private VoteStat statVote(){
        //根据玩家的投票情况对每个玩家进行票数统计
        //并判断玩家队伍中是否存在狼
        boolean hasWolfInPlayers = false;
        boolean hasHunterInPlayers = false;
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            int voteSeat = playerSeat.getVoteSeat();
            Seat votedPlayer = seats.get(voteSeat);
            votedPlayer.beVote();
            if(playerSeat.getCard() == cardFactory.WOLF_0 || playerSeat.getCard() == cardFactory.WOLF_1){
                hasWolfInPlayers = true;
            }
            if(playerSeat.getCard() == cardFactory.HUNTER){
                hasHunterInPlayers = true;
            }
        }

        //找到被投票最多的票数
        int maxVoteCount = 0;
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getVotedCount() > maxVoteCount){
                maxVoteCount = playerSeat.getVotedCount();
            }
        }

        //找到该投票次数的玩家
        Set<Seat> maxVotedPlayerSet = new HashSet();
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getVotedCount() == maxVoteCount){
                maxVotedPlayerSet.add(playerSeat);
            }
        }
        return new VoteStat(hasWolfInPlayers, hasHunterInPlayers, maxVoteCount, maxVotedPlayerSet);
    }

    private void finishGame() {
        logger.debug("finishGame()");
        VoteStat voteStat = statVote();

        //分析获胜阵营: 狼人、村民、皮匠
        //按照如下顺序判定:
        //1. 如果玩家中没有狼人, 且每人得票数为1, 则共同获胜
        //2. 如果获得最大投票数的玩家中包含猎人身份，则由猎人获得当前投票结果后独立投票另一位玩家
        //3. 否则，如果只有皮匠获得最大票数，则皮匠阵营获胜
        //4. 否则，如果获得最大票数的角色中有狼，则村民获胜，皮匠和狼失败; 否则狼获胜，村民和皮匠失败

        // 统计并广播获胜阵营信息，游戏结束
        Set<Camp> victoryCamp = new TreeSet();
        Set<Camp> defeatCamp = new TreeSet();

        //只要皮匠被投出，皮匠即获胜
        if(voteStat.voted(cardFactory.TANNER)){
            victoryCamp.add(Camp.TANNER);
        }else{
            defeatCamp.add(Camp.TANNER);
        }

        //根据玩家中有无狼来判断狼阵营和村阵营的输赢状况
        if (voteStat.hasWolfInPlayers()) {
            // 如果所有玩家中有狼
            if(voteStat.voted(cardFactory.HUNTER)){
                // 如果有猎人被投中，则触发猎人技能

                // 告知猎人当前投票信息， 提示猎人由他独立投票
                StringBuilder report = new StringBuilder();
                for(int i=0;i<getAvailableSeatCount();i++){
                    Seat playerSeat = seats.get(i);
                    Seat votedPlayer = seats.get(playerSeat.getVoteSeat());
                    report.append(getLocation(playerSeat));
                    report.append("号玩家'");
                    report.append(playerSeat.getOpenid());
                    report.append("'投");
                    report.append(playerSeat.getVoteSeat());
                    report.append("号玩家'");
                    report.append(votedPlayer.getOpenid());
                    report.append("'\n");
                }
                //广播当前投票信息
                sendMessage(new OnwMessage(report.toString(), null, null));
                Seat hunter = getPlayerSeatByCard(cardFactory.HUNTER);
                hunterVote = true;
                //TODO 数据部分需要所有玩家信息？
//                OnwMessage hunterMessage = new OnwMessage("请投票", ClientAction.HUNTER_VOTE_ACTION, null);
                //ClientAction.HUNTER_VOTE_ACTION
                SeatMessage seatMessage = new SeatMessage(report.toString());
                keepKeyMessage(hunter, seatMessage);
//                sendMessage(hunter.getOpenid(), hunterMessage);
                OnwMessage onwMessage = new OnwMessage("Game Over", ClientAction.ROOM_CHANGED, this);
                sendMessage(hunter.getOpenid(), onwMessage);
                return ;
            }else if(voteStat.voted(cardFactory.WOLF_0) || voteStat.voted(cardFactory.WOLF_1)){
                victoryCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.WOLF);
            }else{
                victoryCamp.add(Camp.WOLF);
                defeatCamp.add(Camp.VILLAGER);
            }
        }else{
            //如果没有狼
            if (voteStat.getMaxVoteCount() == 1) {
                //共同获胜
                victoryCamp.add(Camp.VILLAGER);
                victoryCamp.add(Camp.WOLF);
            }else{
                //共同失败
                defeatCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.WOLF);
            }
        }
        String outcomeInfo = String.format("[结局]: 村民(%s), 狼人(%s), 皮匠(%s)",
                getCampOutcome(Camp.VILLAGER, victoryCamp),
                getCampOutcome(Camp.WOLF, victoryCamp),
                getCampOutcome(Camp.TANNER, victoryCamp));

        SeatMessage outcomeMessage = new SeatMessage(outcomeInfo);
        keepAllKeyMessage(outcomeMessage);
//        operations.add(outcomeInfo);
        gameFinish(victoryCamp);
    }

    private String getCampOutcome(Camp camp, Set<Camp> victoryCamp){
        if(victoryCamp.contains(camp)){
            return "胜";
        }else{
            return "败";
        }
    }

    private void gameFinish(Set<Camp> victoryCamp) {
        //生成游戏结局
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            Card card = playerSeat.getCard();
            Camp camp = Camp.getCamp(card);
            playerSeat.setOutcome(victoryCamp.contains(camp));
        }
        //解除所有玩家的准备状态，本局游戏结束
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            playerSeat.setReady(false);
        }
        hunterVote = false;
        //TODO 通知所有客户端 ClientAction.GAME_FINISH
        SeatMessage unreadyMessage = new SeatMessage("重新勾选‘准备’进入下一局...");
        keepTopicMessage(unreadyMessage);

        OnwMessage onwMessage = new OnwMessage("Next Game", ClientAction.ROOM_CHANGED, this);
        sendMessage(onwMessage);
//        sendMessage(unreadyMessage);
        //游戏进入停止状态，可以重新准备触发下一轮开始
        scene = Scene.PREPARE;
    }

    //猎人投票
    protected void hunterVote(String userName, int seat){
        //TODO 判定该事件是否能够触发
        Seat hunterPlayer = getSeatByPlayerName(userName);
        if(hunterPlayer.getCard() == cardFactory.HUNTER) {
            if (hunterVote) {
                logger.debug("hunterVote(userName = {}, seat = {})", userName, seat);
                Seat playerSeat = seats.get(seat);
                Set<Camp> victoryCampSet = new TreeSet();
                if (playerSeat.getCard() == cardFactory.TANNER) {
                    sendMessage(new OnwMessage("皮匠获胜", null, null));
                    victoryCampSet.add(Camp.TANNER);
                } else if (playerSeat.getCard() == cardFactory.WOLF_0 || playerSeat.getCard() == cardFactory.WOLF_1) {
                    sendMessage(new OnwMessage("村民阵营获胜", null, null));
                    victoryCampSet.add(Camp.VILLAGER);
                } else {
                    sendMessage(new OnwMessage("狼人阵营获胜", null, null));
                    victoryCampSet.add(Camp.WOLF);
                }
                gameFinish(victoryCampSet);
            }else{
                logger.error("非猎人技能触发时机，试图进行猎人投票");
            }
        }else{
            logger.error("非猎人玩家试图进行猎人投票.");
        }
    }

    /**
     * 房主点击某个卡，来调整房间的座位以及卡牌设定
     */
    public void pickRoleCard(String openID, int roleCardID){
        Map<String, Object> info = new HashMap();
        info.put(KEY_EVENT, "pickRoleCard");
        info.put(KEY_DATE, new Date());
        info.put(RoomController.KEY_OPEN_ID, openID);
        info.put(RoomController.KEY_ROLE_CARD_ID, roleCardID);
        if(openID.equals(getOwner())){
            if(scene == Scene.PREPARE){
                //房主修改房间卡牌设定
                pickRoleCard(roleCardID);
                refreshSeatEnable();
                //群体发送消息到客户端告知房间设置发生了变化
                OnwMessage roomChangedMessage = new OnwMessage("房主修改房间卡牌设定。", ClientAction.ROOM_CHANGED, this);
                sendMessage(roomChangedMessage);
            }else{
                logger.error("只有准备阶段才能修改房间卡牌设定。");
            }
        }else{
            logger.error("只有房主才能调整房间卡牌设定。");
        }
    }

    public String getOwner(){
        return getSeats().get(0).getOpenid();
    }

    public synchronized void pickDesktopCard(String userName, int location){
        logger.debug("pickDesktopCard(userName={}, card={})", userName, location);
        Seat playerSeat = getSeatByPlayerName(userName);
        //如果卡牌序号信息是合理的
        if (location >= 0 && location < TABLE_DECK_THICKNESS) {
            if(scene == Scene.ACTIVATE) {
                if (playerSeat.getInitializeCard() == cardFactory.SEER) {
                    if (seerCheckPlayerSeat == null) {
                        if (seerCheckDesktopCard1 == null) {
                            //预言家尚未验证第一张牌
                            seerCheckDesktopCard1 = location;
                            //TODO 返回"请再选择另外一张"的消息
                        } else if (seerCheckDesktopCard2 == null && location != seerCheckDesktopCard1) {
                            //预言家验证了第一张牌，但尚未验证第二张， 且此张非第一张
                            seerCheckDesktopCard2 = location;
                            attemptNightAction();
                            //TODO 返回"请等待所有玩家行动结束，系统会给出下一步指示"的消息
                        } else {
                            //do nothing
                        }
                    } else {
                        //do nothing
                    }
                } else if (getSingleWolfSeat() != null &&
                        (playerSeat.getInitializeCard() == cardFactory.WOLF_0 ||
                                playerSeat.getInitializeCard() == cardFactory.WOLF_1)) {
                    if (singleWolfCheckDesktopCard == null) {
                        singleWolfCheckDesktopCard = location;
                        if (desktopCards.get(location) == cardFactory.WOLF_1
                                || desktopCards.get(location) == cardFactory.WOLF_0) {
                            //TODO 返回"是狼牌可以再验一张的消息"

                        } else {
                            singleWolfCheckDesktopCard = location;
                            attemptNightAction();
                        }
                    }
                } else if (playerSeat.getInitializeCard() == cardFactory.DRUNK) {
                    if (drunkExchangeDesktopCard == null) {
                        drunkExchangeDesktopCard = location;
                        //TODO 返回"请等待所有玩家行动结束，系统会给出下一步指示"的消息
                        attemptNightAction();
                    }
                } else {
                    //do nothing
                }
            }else if(scene == Scene.VOTE){
                //非行动时间进行行动请求
                //do nothing
            } else if(scene == Scene.PREPARE){
                //do nothing
            }else{
                throw new RuntimeException("Unsupported scene: " + scene);
            }
        } else {
            throw new RuntimeException("卡牌序号" + location + "不合理");
        }
    }

    public synchronized void pickSeat(String userName, int location){
        logger.debug("pickSeat(userName={}, seat={}) scene={}", userName, location, scene);
        Seat playerSeat = getSeatByUserName(userName);
        //验证座位的合理性
        if (location >= 0 && location < getAvailableSeatCount()) {
            if (scene == Scene.ACTIVATE) {
                if(playerSeat.getOpenid() != null) {
                    if (location != getLocation(playerSeat)) {
                        if (playerSeat.getInitializeCard() == cardFactory.SEER) {
                            //预言家没有验过牌也没有验过人且所验玩家不是自己
                            if (seerCheckDesktopCard1 == null && seerCheckDesktopCard2 == null && seerCheckPlayerSeat == null && location != getLocation(playerSeat)) {
                                seerCheckPlayerSeat = location;
                                //TODO 返回等待消息
                                attemptNightAction();
                            } else {
                                //do nothing
                            }
                        } else if (playerSeat.getInitializeCard() == cardFactory.ROBBER) {
                            //强盗还没抢过人
                            if (robberSnatchPlayerSeat == null) {
                                robberSnatchPlayerSeat = location;
                                //TODO 返回等待消息
                                attemptNightAction();
                            } else {
                                //do nothing
                            }
                        } else if (playerSeat.getInitializeCard() == cardFactory.TROUBLEMAKER) {
                            if (location != getLocation(playerSeat)) {
                                if (troublemakerExchangePlayerSeat1 == null &&
                                        location != getLocation(playerSeat)) {
                                    //不能换自己的牌
                                    troublemakerExchangePlayerSeat1 = location;
                                    //TODO 返回再选一张消息
                                } else if (troublemakerExchangePlayerSeat2 == null &&
                                        location != getLocation(playerSeat) &&
                                        location != troublemakerExchangePlayerSeat1) {
                                    //不能换自己的牌且不能与第一张选中的牌相同
                                    troublemakerExchangePlayerSeat2 = location;
                                    //TODO 返回等待消息
                                    attemptNightAction();
                                } else {
                                    //do nothing
                                }
                            } else {
                                //do nothing
                            }
                        }
                    } else {
                        //do nothing
                    }
                }else{
                    //玩家不在房间内无法行动
                    String message = String.format("玩家%s不在房间内无法行动。", userName);
                    throw new RuntimeException(message);
                }
            } else if (scene == Scene.VOTE) {
                if(playerSeat != null) {
                    if(playerSeat.getOpenid() != null) {
                        if (hunterVote) {
                            if (playerSeat.getInitializeCard() == cardFactory.HUNTER) {
                                //猎人技能投票
                                hunterVote(userName, location);
                            } else {
                                //do nothing
                            }
                        } else {
                            //普通vote
                            playerSeat.setVoteSeat(location);
                            if (canStatVote()) {
                                finishGame();
                            }
                        }
                    }else{
                        throw new RuntimeException(String.format("%s未在座位上不能触发行动。", userName));
                    }
                }else{
                    //玩家不在房间内无法行动
                    String message = String.format("玩家%s不在房间内无法投票。", userName);
                    throw new RuntimeException(message);
                }
            } else if(scene == Scene.PREPARE){
                //准备状态点击座位，表示交换座位
                Seat targetSeat = seats.get(location);
                if(playerSeat != null){
                    //如果是玩家，存在离开座位、换到空位，两种情况
                    //如果已经准备了就不能再换位子了
                    if(playerSeat.isReady()){
                        //如果该玩家已经准备了不能换座位,提醒他一下
                        OnwMessage onwMessage = new OnwMessage("已经准备不能换座", null, null);
                        sendMessage(playerSeat.getOpenid(), onwMessage);
                    }else{
                        if(targetSeat.getOpenid() == null){
                            //换到空座位
                            String tempUserName = playerSeat.getOpenid();
                            playerSeat.setOpenid(null);
                            targetSeat.setOpenid(tempUserName);
                            //发送一个刷新房间信息的(换座)事件
                            String message = String.format("换座位到%s", location);
                            ClientAction clientAction = ClientAction.ROOM_CHANGED;
                            Object data = this;
                            OnwMessage roomChangedMessage = new OnwMessage(message, clientAction, data);
                            sendMessage(roomChangedMessage);
                        }else if(playerSeat == targetSeat){
                            //玩家离开座位
                            playerSeat.setOpenid(null);
                            observers.add(userName);
                            //发送一个刷新房间信息的(换座)事件
                            String message = String.format("%s离开座位%s", userName, location);
                            ClientAction clientAction = ClientAction.ROOM_CHANGED;
                            Object data = this;
                            OnwMessage roomChangedMessage = new OnwMessage(message, clientAction, data);
                            //发消息给所有用户
                            sendMessage(roomChangedMessage);
                        }else{
                            //该座位已经有人了, 可以在客户端判断一下，减少通讯
                            String message = String.format("座位%s已经有玩家%s.", location, targetSeat.getOpenid());
                            logger.warn(message);
                        }
                    }
                }else if(isObserver(userName)){
                    System.out.println("------------------------------");
                    System.out.println(JSON.toJSONString(this));
                    //如果请求换座位的是观察者
                    if(targetSeat.getOpenid() == null){
                        //如果请求的座位没有人坐，则观察者坐座位变为玩家
                        targetSeat.setOpenid(userName);
                        observers.remove(userName);
                        String message = String.format("选择%s号座位", location);
                        ClientAction clientAction = ClientAction.ROOM_CHANGED;
                        Object data = this;
                        OnwMessage roomChangedMessage = new OnwMessage(message, clientAction, data);
                        sendMessage(roomChangedMessage);
                    }else{
                        //该座位已经有人了, 可以在客户端判断一下，减少通讯
                        String message = String.format("座位%s已经有玩家%s.", location, targetSeat.getOpenid());
                        logger.warn(message);
                    }
                }else{
                    String message = String.format("用户%s既不属于观看者也不属于玩家。", userName);
                    throw new RuntimeException(message);
                }
            }else {
                throw new RuntimeException("不支持的场景: " + scene);
            }
        } else {
            String message = String.format("座位序号%s不合理，应取值在%s到%s之间。", location, 0, getAvailableSeatCount() - 1);
            logger.warn(message);
        }
    }

    private boolean isObserver(String userName){
        return observers.contains(userName);
    }

    protected int getLocation(Seat seat){
        for(int i=0;i<seats.size();i++){
            if(seat == seats.get(i)){
                return i + 1;
            }
        }
        throw new RuntimeException("无法找到座位: " + seat);
    }

    public void setSimpMessagingTemplate(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendMessage(String userName, OnwMessage message){
        String roomWebSocketQueue = "/message";
        this.date = new Date();
        if(simpMessagingTemplate != null){
            System.out.println("send message: " + roomWebSocketQueue);
            simpMessagingTemplate.convertAndSendToUser(userName, roomWebSocketQueue, message);
        }else{
            System.out.println(String.format("sendMessage to %s: %s", userName, JSON.toJSONString(message, true)));
        }
    }

    public void sendMessage(Seat seat, OnwMessage message){
        sendMessage(seat.getOpenid(), message);
    }

    public void sendMessage(OnwMessage message){
        String roomWebSocketTopic = "/topic/" + id;
        this.date = new Date();
        if(simpMessagingTemplate != null) {
            System.out.println("send topic: " + roomWebSocketTopic);
            simpMessagingTemplate.convertAndSend("/topic/" + id, message);
        }else{
            System.out.println(String.format("sendMessage to All: %s", JSON.toJSONString(message, true)));
        }
    }

    public int getID() {
        return id;
    }

    private void keepKeyMessage(Seat player, SeatMessage seatMessage){
        player.getInformation().add(seatMessage);
    }

    private void keepAllKeyMessage(SeatMessage seatMessage){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat seat = seats.get(i);
            seat.getInformation().add(seatMessage);
        }
    }

    protected List<String> getKeyMessages(String userName){
        Seat playerSeat = getSeatByPlayerName(userName);
        if(playerSeat != null) {
            List<SeatMessage> seatMessages = playerSeat.getInformation();
            List<String> messages = new ArrayList();
            if (seatMessages != null) {
                for (SeatMessage onwMessage : seatMessages) {
                    messages.add(onwMessage.getMessage());
                }
            }
            return messages;
        }else{
            return null;
        }
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public Set<String> getObservers() {
        return observers;
    }

    public boolean[] getCardPicked() {
        return cardPicked;
    }

    public Map<String, WxUser> getOpenidUserMap() {
        return openidUserMap;
    }

    private void refreshSeatEnable(){
        for(int i=0;i<MAX_SEAT;i++){
            this.seats.get(i).setEnable(i<getAvailableSeatCount());
        }
    }

    private void pickRoleCards(Card... cards){
        for(Card card:cards){
            int index = cardFactory.index(card);
            System.out.println(index);
            pickRoleCard(index);
        }
    }

    private void pickRoleCard(int idx){
        cardPicked[idx] = !cardPicked[idx];
    }

    private Set<Card> getPickedCards(){
        Set<Card> cards = new HashSet();
        for(int i=0;i<cardPicked.length;i++){
            if(cardPicked[i]){
                cards.add(cardFactory.CARDS[i]);
            }
        }
        return cards;
    }

    public String getSeatTitle(Seat seat){
        int seatPosition = getSeats().indexOf(seat) + 1;
        String openid = seat.getOpenid();
        String nickName = getOpenidUserMap().get(openid).getNickName();
        return String.format("%s. [%s], ", seatPosition, nickName);
    }

    public Scene getScene() {
        return scene;
    }

    public List<Object> getOperations() {
        return operations;
    }

    public Date getDate() {
        return date;
    }
}

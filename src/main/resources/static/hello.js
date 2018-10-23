var stompClient = null;

function connect() {
    var socket = new SockJS('/onw/endpoint');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected...: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            console.log(JSON.parse(greeting.body).content);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log('Disconnected...');
}

function sendName() {
    stompClient.send('/onw/hello', {}, JSON.stringify({'request':'word'}));
}

$(function () {
    connect()
    $('#connect_button').click(function(){
        connect()
    })
    $('#greeting_button').click(function(){
        sendName()
    })
    $('#disconnect_button').click(function(){
        disconnect()
    })
});

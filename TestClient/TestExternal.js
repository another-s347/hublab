const request = require('request')

var WebSocket = require('ws')

function openNotification() {
    var connection = new WebSocket('ws://localhost:8080/api/external/register/websocket')
    console.log("connecting")
    connection.onopen = function () {
        connection.send(JSON.stringify({"name":"test"}))
    }
    connection.onerror = function (error) {
        console.error('WebSocket Error ' + error);
    };
    // Log messages from the server
    connection.onmessage = function (e) {

        console.log("receive",e.data.toString())
    };
}

function msleep(n) {
    Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, n);
}
function sleep(n) {
    msleep(n * 1000);
}

function test(){
    openNotification()
    sleep(10)
}

test()
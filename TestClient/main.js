const request = require('request')

var WebSocket = require('ws')

function login() {
    var loginBody = JSON.stringify({
        username: "test",
        deviceName: "node",
        password: "test"
    })

    request.post({
        url: "http://localhost:8080/api/login",
        body: loginBody
    }, (error, response, body) => {
        //console.log(error, response, body)
        openNotification(body)
    })
}

function openNotification(sessionId) {
    var connection = new WebSocket('ws://localhost:8080/api/notification/register/websocket')
    console.log("connecting")
    connection.onopen = function () {
        console.log("session",sessionId)
        connection.send(JSON.stringify({"session":sessionId}))
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

login()
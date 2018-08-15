const request = require('request')

var WebSocket = require('ws')

function login() {
    var loginBody = JSON.stringify({
        username: "test",
        deviceName: "node",
        password: "test"
    })

    request.post({
        url: "http://localhost:8080/login",
        body: loginBody
    }, (error, response, body) => {
        console.log(error, response, body)
        openNotification(body)
    })
}

function openNotification(sessionId) {
    var connection = new WebSocket('ws://localhost:8080/notification/register/websocket')
    connection.onopen = function () {
        connection.send(JSON.stringify({"session":sessionId}))
    }
    connection.onerror = function (error) {
        console.error('WebSocket Error ' + error);
    };
    // Log messages from the server
    connection.onmessage = function (e) {

    };
}

function msleep(n) {
    Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, n);
}
function sleep(n) {
    msleep(n * 1000);
}

login()
sleep(10)
// websocket.js — SockJS + STOMP client

let stompClient = null;

function connectWebSocket(onConnected) {
  const socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.debug = null; // suppress debug output
  stompClient.connect({}, function(frame) {
    if (onConnected) onConnected(stompClient);
  }, function(error) {
    console.error('WebSocket connection error:', error);
    setTimeout(() => connectWebSocket(onConnected), 5000);
  });
}

function connectOrderStatus(orderId, callback) {
  connectWebSocket(function(client) {
    client.subscribe('/topic/orders/' + orderId, function(message) {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (e) {
        console.error('Failed to parse order status message', e);
      }
    });
  });
}

function connectStaffOrders(callback) {
  connectWebSocket(function(client) {
    client.subscribe('/topic/staff/orders', function(message) {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (e) {
        console.error('Failed to parse staff order message', e);
      }
    });
  });
}

function connectAdminAlerts(callback) {
  connectWebSocket(function(client) {
    client.subscribe('/topic/admin/alerts', function(message) {
      try {
        const body = JSON.parse(message.body);
        callback(body);
      } catch (e) {
        console.error('Failed to parse admin alert message', e);
      }
    });
  });
}

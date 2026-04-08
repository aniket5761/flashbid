import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

function getWebSocketHttpUrl() {
  const configuredBaseUrl = import.meta.env.VITE_WS_BASE_URL || import.meta.env.VITE_API_BASE_URL;
  if (configuredBaseUrl) {
    const url = new URL(configuredBaseUrl, window.location.origin);
    url.protocol = url.protocol === "wss:" ? "https:" : "http:";
    url.pathname = "/ws";
    url.search = "";
    url.hash = "";
    return url.toString();
  }

  return `${window.location.origin}/ws`;
}

function createBaseClient({ onConnect, onDisconnect, onError, onReady }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(getWebSocketHttpUrl()),
    reconnectDelay: 3000,
    onConnect: () => {
      onConnect?.();
      onReady?.(client);
    },
    onStompError: (frame) => {
      onError?.(new Error(frame.headers.message || "WebSocket broker error."));
    },
    onWebSocketError: (event) => {
      onError?.(new Error(event?.message || "WebSocket connection error."));
    },
    onWebSocketClose: () => {
      onDisconnect?.();
    }
  });

  client.activate();
  return client;
}

function parseAuctionMessage(message, onMessage, onError) {
  try {
    const payload = JSON.parse(message.body);
    onMessage?.(payload);
  } catch (error) {
    onError?.(error);
  }
}

export function createAuctionClient(productId, { onConnect, onDisconnect, onMessage, onError }) {
  return createBaseClient({
    onConnect,
    onDisconnect,
    onError,
    onReady: (client) => {
      client.subscribe(`/topic/auctions/${productId}`, (message) => {
        parseAuctionMessage(message, onMessage, onError);
      });
    }
  });
}

export function createAuctionListClient(productIds, { onConnect, onDisconnect, onMessage, onError }) {
  return createBaseClient({
    onConnect,
    onDisconnect,
    onError,
    onReady: (client) => {
      productIds.forEach((productId) => {
        client.subscribe(`/topic/auctions/${productId}`, (message) => {
          parseAuctionMessage(message, onMessage, onError);
        });
      });
    }
  });
}

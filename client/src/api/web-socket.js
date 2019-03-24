import Stomp from "stompjs";
import SockJS from "sockjs-client";
import BASE_URL from "./constants";

export default {
	stompClient: {
		onEvent: (event, callback) => {
			const socket = new SockJS(`http://${BASE_URL}/online-bank`);
			const stompClient = Stomp.over(socket);
			stompClient.connect({}, () => {
				stompClient.subscribe(event, response => {
					callback(JSON.parse(response.body));
				});
			});
		}
	}
};

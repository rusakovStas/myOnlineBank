import axios from "axios";
import BASE_URL from "./constants";

export default {
	user: {
		login: cred => {
			const bt = btoa(`client-id:secret`);
			const config = {
				headers: { Authorization: `Basic ${bt}` }
			};
			return axios
				.post(
					`http://${BASE_URL}/oauth/token?grant_type=password&username=${
						cred.email
					}&password=${cred.password}`,
					"",
					config
				)
				.then(res => res.data);
		},
		getroles: () =>
			axios.get(`http://${BASE_URL}/users/myroles`).then(res => res.data)
	},
	admin: {
		getAllAccounts: () =>
			axios.get(`http://${BASE_URL}/accounts/all`).then(res => res.data),
		getAllUsers: () =>
			axios.get(`http://${BASE_URL}/users/all`).then(res => res.data),
		addUser: user =>
			axios
				.post(`http://${BASE_URL}/users`, user)
				.then(response => response.data),
		deleteUser: user =>
			axios
				.delete(`http://${BASE_URL}/users?username=${user.username}`)
				.then(response => response.data)
	},
	account: {
		getMyAccounts: () =>
			axios.get(`http://${BASE_URL}/accounts/my`).then(res => res.data),
		deleteAccount: accountId =>
			axios.delete(`http://${BASE_URL}/accounts/?id=${accountId}`),
		getSuggestions: () =>
			axios
				.get(`http://${BASE_URL}/accounts/suggestions`)
				.then(res => res.data),
		createTransaction: transaction =>
			axios.post(`http://${BASE_URL}/accounts/transaction`, transaction),
		createAccount: account =>
			axios
				.post(`http://${BASE_URL}/accounts`, account)
				.then(res => res.data)
	}
};

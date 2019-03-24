import {
	GET_ACCOUNTS,
	ADD_ACCOUNT,
	UPDATE_ACCOUNT,
	DELETE_ACCOUNT
} from "./types";
import api from "../api/api";
import webSocket from "../api/web-socket";

export const getAccountsFromRs = accounts => ({
	type: GET_ACCOUNTS,
	accounts
});

export const addAccount = account => ({
	type: ADD_ACCOUNT,
	account
});

export const updateAccount = account => ({
	type: UPDATE_ACCOUNT,
	account
});

export const del = account => ({
	type: DELETE_ACCOUNT,
	account
});

export const getAllAccounts = username => dispatch => {
	api.admin.getAllAccounts().then(accounts => {
		dispatch(getAccountsFromRs(accounts));
	});
	webSocket.stompClient.onEvent(`/topic/accounts/${username}`, response => {
		dispatch(updateAccount(response));
	});
};

export const getMyAccounts = username => dispatch => {
	api.account.getMyAccounts().then(accounts => {
		dispatch(getAccountsFromRs(accounts));
	});
	webSocket.stompClient.onEvent(`/topic/accounts/${username}`, response => {
		dispatch(updateAccount(response));
	});
};

export const deleteAccount = accountId => dispatch =>
	api.admin.deleteUser(accountId).then(() => {
		dispatch(del(accountId));
	});
